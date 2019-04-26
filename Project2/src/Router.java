import java.util.HashMap;

public class Router {
	public String node;
	public HashMap<String, Double> nextNode;
	public HashMap<String, costCal> linkCost;
	public Boolean routeChange;
	

	public Router(String node) {
		this.node = node;
		this.routeChange = true;
		this.nextNode = new HashMap<>();
		this.linkCost = new HashMap<>();
	}

	public Router(byte[] nodeData){

		String nodeDataRec = new String(nodeData);

		String[] data = nodeDataRec.split(",");

		this.routeChange = true;
		
		this.node = data[0].trim();
		
		this.linkCost = new HashMap<>();
		
		costCal DVdata = new costCal();
		DVdata.cost = 0.0;
		this.linkCost.put(this.node, DVdata);
		
		for(int i = 1; i < data.length; i++){
			String[] linkValue = data[i].split(":");
			Double cost = Double.parseDouble(linkValue[1].trim());
			costCal DV = new costCal();
			DV.cost = cost;
			
			this.linkCost.put(linkValue[0].trim(), DV);
		}
	}

	public void broadcast(){
		this.routeChange = false;
	}

	public void newNode(String nxtNode, double cost, String count, Boolean addLink){
		this.routeChange = true;

		double oldCost = Double.POSITIVE_INFINITY;

		if(this.nextNode.get(nxtNode) != null){
			oldCost = this.nextNode.get(nxtNode);
		}

		this.nextNode.put(nxtNode, cost);

		if(addLink || oldCost != cost){
			this.updateLinkCost(nxtNode, cost, count);
		}
	}
	

	public void updateLinkCost(String nxtNode, double cost, String count){
		this.routeChange = true;
		
		costCal DVdata = this.linkCost.get(nxtNode);

		if(DVdata != null){
			DVdata.cost = cost;
			DVdata.cal = count;
		} else{
			this.linkCost.put(nxtNode, new costCal(cost, count));
		}
		
	}

	public String updateLinkData(String lData){
		
		String data = "";
		
		for(String node : this.linkCost.keySet()){
			
			if(node.equals(this.node)){
				continue;
			}
			
			costCal DV = this.linkCost.get(node);
			if(!DV.cal.equals(lData)){
				data += node + ":" + DV.cost + ",";
			}
		}

		if(data.equals("")){
			return this.node;
		}
		
		return this.node + "," + data.substring(0, data.length() - 1);
	}

	@Override
	public String toString() {
		
		String output = "";
		for(String node : linkCost.keySet()){
			if(node.equals(this.node)){
				continue;
			}
			
			costCal DV = this.linkCost.get(node);
			output += "shortest path " + this.node + "-" + node + ": the next hop is " + DV.cal + " and the cost is " + DV.cost + "\n";

		}
		
		return output;
	}
	
	
}
