import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;

public class ComputeDVR {
	

	public Router myNode;
	public int round;
	public static long frequency = 15000;


	public ComputeDVR(Router myNode) {
		this.myNode = myNode;
		this.round = 1;
	}
	
	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.out.println("This is not a valid Format" + "\n" + "Please enter valid filename");
            return;
        }


		String filename = args[0];

		String myRouter = filename.split(".dat")[0];

		Router myNode = new Router(myRouter);

		ComputeDVR vector = new ComputeDVR(myNode);

		long currTime = System.currentTimeMillis();
		
		try {


			int Port = 9000 + (int) myRouter.charAt(0);

			DatagramSocket Client = new DatagramSocket(Port);
			
			InetAddress IP = InetAddress.getByName("localhost");
			

			while(true){

				if(vector.myNode.routeChange){

					try{

						BufferedReader reader = new BufferedReader(new FileReader(filename));
						
						String page = reader.readLine();

						HashMap<String, Double> nextNode = new HashMap<>();

						while((page = reader.readLine()) != null) {
							
							StringTokenizer token = new StringTokenizer(page);
							
							if(token.countTokens() != 2){
								System.err.println("Please provide valid data");
								reader.close();
								return;
							}
							
							String nodeDetail = token.nextToken().trim();
							Double distance = Double.parseDouble(token.nextToken().trim());

							nextNode.put(nodeDetail, distance);
						}
						reader.close();

						Set<String> addedLink = new HashSet<String>();
						addedLink.addAll(nextNode.keySet());
						addedLink.addAll(vector.myNode.nextNode.keySet());

						for(String nodeDetail : addedLink){
							Double distance = nextNode.get(nodeDetail);

							if(distance == null){
								distance = Double.POSITIVE_INFINITY;
							}
							
							vector.myNode.newNode(nodeDetail, distance, nodeDetail, vector.round == 1);
						}
						
					} catch(FileNotFoundException e){
						System.err.println("File does not exist");
						return;
					} catch(NumberFormatException e){
						System.err.println("Data is invalid");
						return;
					} catch(IOException e){
						System.err.println("No valid data!");
						return;
					}

					System.out.println("output number " + vector.round++);

					System.out.println(vector.myNode);

					System.out.println();
					
					for(String closeLink : vector.myNode.nextNode.keySet()){
						
						if(vector.myNode.nextNode.get(closeLink) == Double.POSITIVE_INFINITY){
							continue;
						}

						String linkCost = myNode.updateLinkData(closeLink);
					
						byte[] data = linkCost.getBytes();
						
						int ClientPort = 9000 + (int) closeLink.charAt(0);
						
						DatagramPacket dataPacket = new DatagramPacket(data, data.length, IP, ClientPort);
						Client.send(dataPacket);
					}
					
					vector.myNode.broadcast();
				}

				try{
					
					long TimeOut = frequency - (System.currentTimeMillis() - currTime);
					
					if (TimeOut < 0) {
						throw new SocketTimeoutException();
					}
					
					byte[] DataPack = new byte[1024];
					DatagramPacket PacketData = new DatagramPacket(DataPack, DataPack.length);

					Client.setSoTimeout((int) TimeOut);
					
					Client.receive(PacketData);
					byte[] nodeData = PacketData.getData();
					
					Router routerData = new Router(nodeData);
					
					for(String node : routerData.linkCost.keySet()){

						costCal linkValue = vector.myNode.linkCost.get(node);

						Double CostOfLink = Double.POSITIVE_INFINITY;
						String count = null;

						if(linkValue != null){
							CostOfLink = linkValue.cost;
							count = linkValue.cal;
						}
						
						Double DVR = routerData.linkCost.get(node).cost + vector.myNode.nextNode.get(routerData.node);


						if(CostOfLink > DVR){
							vector.myNode.updateLinkCost(node, DVR, routerData.node);
						} else if(count != null && count.equals(routerData.node) && !CostOfLink.equals(DVR)){
							vector.myNode.updateLinkCost(node, DVR, routerData.node);
						}
					}
				} catch(SocketTimeoutException e){
					vector.myNode.routeChange = true;
					currTime = System.currentTimeMillis();
				}
				
				
			}
			
			
			
		} catch (SocketException e) {
			e.printStackTrace();
		}  catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
	}

}
