/**
 * Item.java
 * @author Scott Larson and Evan Gulick
 */

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
	
  synchronized int getQuantity() {
	return quantity;
  }
	
  synchronized void setQuantity(int purchased){
	quantity -= purchased;
  }
	
  synchronized void returnItem(int rQuantity) {
	quantity += rQuantity;
  }
}
