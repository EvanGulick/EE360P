
public class Order {
	int id;
	String productName;
	int quantity;
	
	Order(int id, String name, int quantity) {
		this.id = id;
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
