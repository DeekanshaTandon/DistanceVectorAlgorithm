public class costCal {
	public double cost;
	public String cal;
	
	public costCal() {
		this.cost = Double.POSITIVE_INFINITY;
		this.cal = null;
	}
	
	public costCal(double cost, String cal) {
		this.cost = cost;
		this.cal = cal;
	}
}
