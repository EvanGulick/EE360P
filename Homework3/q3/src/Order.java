
public class Order {
	int id;
	String productName;
	int quantity;
	static int idnumber = 0;
	
	public Order(String name, int quantity) {
		idnumber += 1;
		this.id = idnumber;
		productName = name;
		this.quantity = quantity;
	}
	
	// make a dummy Order that basically is null
	public Order() {
	  id = -1;
	}
	
	public int getId() {
		return id;
	}
	
	public String getProductName(){
		return productName;
	}
	
	public int getQuantity(){
		return quantity;
	}
}
