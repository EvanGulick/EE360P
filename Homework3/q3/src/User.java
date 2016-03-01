import java.util.ArrayList;
import java.util.List;

public class User {
	String name;
	List<Order> orderHistory;
	
	User(String name) {
		this.name = name;
		orderHistory = new ArrayList<Order>();
	}
	
	String getUserName(){
		return name;
	}
	
	List<Order> getOrderHistory(){
		return orderHistory;
	}
	
	void addingOrder(Order neworder){
		orderHistory.add(neworder);
	}
	
	int removeorder(int orderid){
		for(int i = 0; i<orderHistory.size(); i++){
			if(orderHistory.get(i).getId() == orderid){
				orderHistory.remove(i);
				return 1;
			}
		}
		return -1;
	}
	
}
