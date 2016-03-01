/**
 * Server.java
 * @author Scott Larson and Evan Gulick
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server {
  private static ExecutorService es;
  static List<Item> Inventory;
  static List<User> UserDatabase;
  
  // Nested static class Command for multi-threading the execution of a command
  static public class Command implements Callable<String> {
	String cmd;
	
	public Command(String cmd) {
		this.cmd = cmd;
	}
	
	public String getString() {
		return cmd;
	}
	
	@Override
	public String call() throws Exception {
	  return execute(cmd);
	}
  }
  
  // Thread for TCP Server
  private static void TCPServer(int tcpPort) throws IOException {
	String cmd;
	PrintStream pout;
	Scanner din;
	try {
	  ServerSocket welcomeSocket = new ServerSocket(tcpPort);	// Connect to socket
	  while(true) {	// Infinite Loop wait for a client
	 	Socket connectionSocket = welcomeSocket.accept();
	 	din = new Scanner(connectionSocket.getInputStream());
	 	pout = new PrintStream(connectionSocket.getOutputStream());
	 	cmd = din.nextLine();	// Receive Command
		Command cmdObj = new Command(cmd);
		Future<String> result = es.submit(cmdObj);
		String strResult = result.get();
		pout.println(strResult);      
	  }
	} catch(IOException e) {
	  System.err.println(e);
	} catch(InterruptedException e) {
	  System.err.println(e);
	} catch(ExecutionException e) {
	  System.err.println(e);
	}
  }
  
  // Thread for the UDP Server
  private static void UDPServer(int updPort) {
	String cmd;
	DatagramPacket datapacket, returnpacket;
	try {
	  DatagramSocket datasocket = new DatagramSocket(updPort);	// Connect to socket
	  byte[] buf = new byte[1024];
	  byte[] rbuf = new byte[1024];
	  while(true){	// Infinite Loop wait for a client
		datapacket = new DatagramPacket(buf, buf.length);
		datasocket.receive(datapacket);		// Receive Command
		cmd = new String(datapacket.getData(), 0, datapacket.getLength());
		Command cmdObj = new Command(cmd);
		Future<String> result = es.submit(cmdObj);
		String strResult = result.get();
		rbuf = new byte[strResult.length()];
		rbuf = strResult.getBytes();
		returnpacket = new DatagramPacket(rbuf, 
						rbuf.length, 
						datapacket.getAddress(),
						datapacket.getPort());
		datasocket.send(returnpacket);
	  }
	} catch (SocketException e) {
	  System.err.println(e);
	} catch (IOException e) {
	  System.err.println(e);
	} catch (InterruptedException e) {
	  System.err.println(e);
	} catch (ExecutionException e) {
		e.printStackTrace();
	}
  }
  
  // Decide which function to execute based on first word of the command
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
	  return "invalid command: " + cmd;
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
	  Order order = UserDatabase.get(i).getOrder(orderId);
	  if(order.getId() != -1) {
		// remove order
		if(UserDatabase.get(i).getOrderHistory().remove(order)) {
		  // return item to inventory
		  String rItem = order.getProductName();
		  int rQuantity = order.getQuantity();
		  for(int j = 0; j < Inventory.size(); j++){
			if(Inventory.get(j).getName().equals(rItem)) {
			  Inventory.get(j).returnItem(rQuantity);
			}
		  }
		  return "Check";
		}
	  }
	}
	return "Not Found";
  }
  
  private static String executeSearch(String username) {
	  for(int i = 0; i<UserDatabase.size(); i++){
		if(UserDatabase.get(i).getUserName().equals(username)){
		  if(UserDatabase.get(i).getOrderHistory().size() == 0) { return "0"; }
		  String concattedorders = "";
		  concattedorders = Integer.toString(
				  UserDatabase.get(i).getOrderHistory().size()) + ", ";
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
    
   es = Executors.newCachedThreadPool();
    
    // parse the inventory file
    parseFile(fileName);
    
    // handle request from clients
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
		UDPServer(udpPort);
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
