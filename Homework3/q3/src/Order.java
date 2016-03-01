
public class Order {
	int id;
	String productName;
	int quantity;
	static int idnumber = 0;
	
	Order(String name, int quantity) {
		idnumber += 1;
		this.id = idnumber;
		productName = name;
		this.quantity = quantity;
	}
	
	int getId() {
		return id;
	}
	
	String getProductName(){
		return productName;
	}
	
	int getQuantity(){
		return quantity;
	}
}
