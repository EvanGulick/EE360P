import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server {
  private static ExecutorService es;
  static List<Item> Inventory;
  static List<User> UserDatabase;
  
  static public class Command implements Callable<String> {
	String cmd;
	
	public Command(String cmd) {
		this.cmd = cmd;
	}
	
	@Override
	public String call() throws Exception {
	  return execute(cmd);
	}
  }
  
  private static void TCPServer(int tcpPort) {
	String cmd = "";
	// Infinite Loop wait for a client
	// Connect to socket
	// Receive Command
	Command cmdObj = new Command(cmd);
	Future<String> result = es.submit(cmdObj);
	// Send result back
  }
  
  private static void UDPServer(int updPort) {
	String cmd = "";
	// Infinite Loop wait for a client
	// Connect to socket
	// Receive Command
	Command cmdObj = new Command(cmd);
	Future<String> result = es.submit(cmdObj);
	// Send result back
  }
  
  public static String execute(String cmd) {
	String tokens[] = cmd.split(" ");
	if(tokens[0].equals("purchase")) {
	  return executePurchase(tokens[1], tokens[2], Integer.parseInt(tokens[3]));
	} else if (tokens[0].equals("cancel")) {
	  return executeCancel(Integer.parseInt(tokens[1]));
	} else if (tokens[0].equals("search")) {
	  return executeSearch(tokens[1]);
	} else if (tokens[0].equals("list")) {
	  return executeList();
	} else {
	  return "invalid command";
	}
  }
  
  private static String executePurchase(String username, String product, int quantity) {
	  String putittogether = "";
	for(int i = 0; i<Inventory.size(); i++){
	  if(Inventory.get(i).getName().equalsIgnoreCase(product)){
	    if(Inventory.get(i).getQuantity() >= quantity){
	      Inventory.get(i).setQuantity(quantity);
	      Order neworder = new Order(product, quantity);
	      putittogether = Integer.toString(neworder.getId()) + ", "
	     + username + ", " + product + ", " + Integer.toString(quantity);
	      for(int k = 0; k<UserDatabase.size(); k++){
	    	  if(UserDatabase.get(k).getUserName().equals(username)){
	    		  UserDatabase.get(k).addingOrder(neworder);
	    		  return putittogether;
	    	  }
	      }
	      User newuser = new User(username);
	      newuser.orderHistory.add(neworder);
	      UserDatabase.add(newuser);
	      return putittogether;
		}
	    else{
	      return "Out of Stock";
	    }
	  }
	}
	return "Invalid Product Name";
  }
  
  private static String executeCancel(int orderId) {
	for(int i = 0; i<UserDatabase.size(); i++){
	  if(UserDatabase.get(i).removeorder(orderId) == 1){
		  return "Check";
	  }
	}
	return "Not Found";
  }
  
  private static String executeSearch(String username) {
	  for(int i = 0; i<UserDatabase.size(); i++){
		if(UserDatabase.get(i).getUserName().equals(username)){
		  String concattedorders = "";
		  concattedorders = Integer.toString(UserDatabase.get(i).getOrderHistory().size()) + ", ";
		  for(int k = 0; k < UserDatabase.get(i).getOrderHistory().size(); k++){
			  concattedorders = concattedorders + 
			Integer.toString(UserDatabase.get(i).getOrderHistory().get(k).getId()) + " " +
			UserDatabase.get(i).getOrderHistory().get(k).getProductName() + " " +
			Integer.toString(UserDatabase.get(i).getOrderHistory().get(k).getQuantity()) + ", ";
		  }
		  return concattedorders;
		}
	  }
	  return "0";
  }
  
  private static String executeList() {
	String theWholeInventory = "";
	theWholeInventory = Integer.toString(Inventory.size()) + ", ";
	for(int i = 0; i< Inventory.size(); i++){
		theWholeInventory = theWholeInventory + Inventory.get(i).getName() + 
				", " + Integer.toString(Inventory.get(i).getQuantity()) + ", ";
	}
	return theWholeInventory;
  }
  
  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    Inventory = new ArrayList<Item>();
    UserDatabase = new ArrayList<User>();
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(2) <udpPort>: the port number for UDP connection");
      System.out.println("\t(3) <file>: the file of inventory");

      System.exit(-1);
    }
    tcpPort = Integer.parseInt(args[0]);
    udpPort = Integer.parseInt(args[1]);
    String fileName = args[2];
    
    ExecutorService es = Executors.newCachedThreadPool();
    
    // parse the inventory file
    parseFile(fileName);
    
    // TODO: handle request from clients
    Thread tcpthread = new Thread(){
		public void run(){
			try {
				TCPServer(tcpPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	Thread udpthread = new Thread(){
		public void run(){
			try{
				UDPServer(udpPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	tcpthread.start();	
    udpthread.start();
  }
  
  public static void parseFile(String fileName) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      String fileRead = br.readLine();
      
      while (fileRead != null) {
    	// use string.split to load a string array with the values from each line of
    	// the file, using a comma as the delimiter
    	String[] tokenize = fileRead.split(" ");
			
    	// assume file is made correctly
    	// and make temporary variables for the three types of data
    	String tempItem = tokenize[0];
    	int tempQty = Integer.parseInt(tokenize[1]);
			
    	// create temporary instance of Inventory object
    	// and load with three data values
    	Item tempitem = new Item(tempItem, tempQty);
			
    	// add to array list
    	Inventory.add(tempitem);
			
    	// read next line before looping
    	// if end of file reached 
    	fileRead = br.readLine();
      }
      //close file stream
      br.close();
    } catch (FileNotFoundException e) {
    	System.out.println("file not found");
	} catch (IOException e) {   // parse the inventory file
		e.printStackTrace();
	}
  }
}
