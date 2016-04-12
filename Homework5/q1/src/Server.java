/**
 * Server.java
 * @author Scott Larson and Evan Gulick
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
  private static ExecutorService es;
  static List<Address> ServerList;
  static List<Integer> ServerBlackList;
  static List<Item> Inventory;
  static List<User> UserDatabase;
  
  static AtomicInteger ThreadTicket;
  static PriorityQueue<Integer> ServerTicketList = new PriorityQueue<Integer>();
  
  static int myID;
  static int NumServers;
  static int[] ServerPorts;
  static int[] EarlyMessages;

  static public class ClientListen implements Runnable {
	@Override
	public void run() {
	  try {
		@SuppressWarnings("resource")
		ServerSocket clientSocket = new ServerSocket(ServerList.get(myID).getPort());	// Connect to socket
		while(true) {	// Infinite Loop wait for a client
		  Socket connectionSocket = clientSocket.accept();
		  es.submit(new ClientTask(connectionSocket)); // spawn new client thread to perform task
		}
	  } catch (IOException e) {
		//e.printStackTrace();
	  }
	}
  }
  
  static public class ServerListen implements Runnable {
	@Override
	public void run(){
	  try {
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(ServerPorts[myID]);	// Connect to socket
		while(true) {	// Infinite Loop wait for a server connection
		  Socket connectionSocket = serverSocket.accept();
		  es.submit(new ServerTask(connectionSocket));	// spawn new server thread to perform task
		}
	  } catch(IOException e) {
	    //System.err.println(e);
	  }
	}
  }
  
  static public class ClientTask implements Runnable {
	Socket ClientSocket;
	ClientTask(Socket clientSocket) {
		ClientSocket = clientSocket;
	}
	@Override
	public void run() {
	  ServeClient(ClientSocket);
	}
  }
  
  static public class ServerTask implements Runnable {
	Socket ServerSocket;
	ServerTask(Socket clientSocket) {
	  ServerSocket = clientSocket;
	}
	@Override
	public void run() {
	  ServeServer(ServerSocket);
	}
  }
  
  static public class TalkToServer implements Runnable {
	String ip;
	int port;
	String msg;
	int serverIndex;
	TalkToServer(int serverIndex, String msg) {
		this.ip = ServerList.get(serverIndex).getIp();
		this.port = ServerPorts[serverIndex];
		this.msg = msg;
		this.serverIndex = serverIndex;
	}
	@Override
	public void run() {
	  String response;  
	  PrintStream pout = null;
	  Scanner din = null;
	  Socket clientSocket = new Socket();
	  try {
		clientSocket.connect(new InetSocketAddress(ip, port), 100);  
		clientSocket.setSoTimeout(100);
		din = new Scanner(clientSocket.getInputStream());
		pout = new PrintStream(clientSocket.getOutputStream());
		pout.println(msg);
		pout.flush();
		while(din.hasNextLine()) {
	      response = din.nextLine();
	      if(response != null) break;
		}
	  } catch(SocketTimeoutException e){
		ServerBlackList.add(serverIndex);
		ServerList.remove(serverIndex);
	  } catch(ConnectException e){
		ServerBlackList.add(serverIndex);
		ServerList.remove(serverIndex);
	  } catch(IOException e) {
		//System.err.println(e);
	  } finally {
		try {
		  din.close();
		  pout.close();
		  clientSocket.close();
		}
		catch (NullPointerException n) {}
		catch (IOException n) {}
	  }
	}  
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
	  return "invalid command: " + cmd;
	}
  }
	  
  private static String executePurchase(String username, String product, int quantity) {
	String putittogether = "";
	for(int i = 0; i < Inventory.size(); i++){
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
	for(int i = 0; i < UserDatabase.size(); i++){
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
	  for(int i = 0; i < UserDatabase.size(); i++){
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
	for(int i = 0; i < Inventory.size(); i++){
	  theWholeInventory = theWholeInventory + Inventory.get(i).getName() + 
			", " + Integer.toString(Inventory.get(i).getQuantity()) + ", ";
	}
	return theWholeInventory;
  }
  
  public static void main (String[] args) {
    Scanner sc = new Scanner(System.in);
    String[] firstLine = sc.nextLine().split(" ");
    myID = Integer.parseInt(firstLine[0]) - 1;
    NumServers = Integer.parseInt(firstLine[1]);
    String inventoryPath = firstLine[2];
    
    ServerPorts = new int[NumServers];
    EarlyMessages = new int[NumServers];
    ServerList = new ArrayList<Address>();
    ServerBlackList = new ArrayList<Integer>();
    UserDatabase = new ArrayList<User>();
    Inventory = new ArrayList<Item>();
    
    es = Executors.newCachedThreadPool();

    for (int i = 0; i < NumServers; i++) {
      String[] splitting = sc.nextLine().split(":");
      Address server = new Address(splitting[0], Integer.parseInt(splitting[1]));
      ServerList.add(server);
      ServerPorts[i] = 9451 + i;
      EarlyMessages[i] = 0;
    }
    sc.close();
    
    parseFile(inventoryPath);
    
    ThreadTicket = new AtomicInteger(NumServers + 1);
    
    es.submit(new ClientListen());
    es.submit(new ServerListen());
  }
  
  private static void ServeClient(Socket connectionSocket) {
	String cmd;
	PrintStream pout;
	Scanner din;
	try {
	  din = new Scanner(connectionSocket.getInputStream());
	  pout = new PrintStream(connectionSocket.getOutputStream());
	  cmd = din.nextLine();	// Receive Command
	  requestCS();
	  String result = execute(cmd); // CS
	  forwardChanges(cmd);
	  pout.println(result);
	} catch(IOException e) {
	  //System.err.println(e);
	}
  }
  
  private static void requestCS() {
	Integer threadID = ThreadTicket.getAndIncrement();
	ServerTicketList.add(threadID);
	String msg = "0:" + myID; // 0 is request
	sendToAll(msg);
	while(ServerBlackList.contains(ServerTicketList.peek())) { ServerTicketList.poll(); }
	while(ServerTicketList.peek() != threadID) {}
  }
  
  private static void forwardChanges(String cmd) {
	String msg = "1:" + cmd + ":" + myID; // 1 is release
	sendToAll(msg);
	ServerTicketList.poll();
  }
  
  private static void sendToAll(String msg) {
	for(int i = 0; i < NumServers; i++) {
	  if(i != myID && !ServerBlackList.contains(i)) {
		es.submit(new TalkToServer(i, msg));
	  }
	}
  }
  
  private static void ServeServer(Socket connectionSocket) {
	String msg;
	PrintStream pout;
	Scanner din;
	try {
	  din = new Scanner(connectionSocket.getInputStream());
	  pout = new PrintStream(connectionSocket.getOutputStream());
	  msg = din.nextLine();	// Receive Command
	  String[] tokens = msg.split(":");
	  if(tokens[0].equals("0")) {
		int reqThread = Integer.parseInt(tokens[1]);
		if(EarlyMessages[reqThread] > 0) {
		  EarlyMessages[reqThread] -= 1;
		} else {
		  ServerTicketList.add(reqThread);
		}
	  } else {
		execute(tokens[1]);
		int reqThread = Integer.parseInt(tokens[2]);
		if(!ServerTicketList.contains(reqThread)) {
		  EarlyMessages[reqThread] += 1;
		} else {
		  ServerTicketList.poll();
		}
	  }
	  pout.println("k");
	} catch(IOException e) {
	  System.err.println(e);
	}
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
	    Item tmpItem = new Item(tempItem, tempQty);
			
	    // add to array list
	    Inventory.add(tmpItem);
			
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
