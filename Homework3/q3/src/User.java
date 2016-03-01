import java.util.ArrayList;
import java.util.List;

public class User {
	String name;
	List<Order> orderHistory;
	
	public User(String name) {
		this.name = name;
		orderHistory = new ArrayList<Order>();
	}
	
	public String getUserName(){
		return name;
	}
	
	synchronized public List<Order> getOrderHistory(){
		return orderHistory;
	}
	
	synchronized public Order getOrder(int orderId){
		for(int i = 0; i < orderHistory.size(); i++) {
			if(orderHistory.get(i).getId() == orderId) {
				return orderHistory.get(i);
			}
		}
		return new Order();
	}
	
	synchronized public void addingOrder(Order neworder){
		orderHistory.add(neworder);
	}
	
	synchronized public int removeorder(int orderid){
		for(int i = 0; i<orderHistory.size(); i++){
			if(orderHistory.get(i).getId() == orderid){
				orderHistory.remove(i);
				return 1;
			}
		}
		return -1;
	}
	
}
