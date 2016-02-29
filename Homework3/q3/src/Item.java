
public class Item {
	String name;
	int quantity;
	
	Item(String name, int quantity) {
		this.name = name;
		this.quantity = quantity;
	}
	
	String getName() {
		return name;
	}
	
	int getQuantity() {
		return quantity;
	}
}
