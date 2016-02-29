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
}
