import java.util.ArrayList;
import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Client {
    static ArrayList<Address> serverList = new ArrayList<Address>();
    
	private static String sendTCP(String hostname, int tcpPort, String cmd) {
		String response;  
		PrintStream pout;
		Scanner din;
		try {  
		  Socket clientSocket = new Socket(hostname, tcpPort);  
		  clientSocket.setSoTimeout(100);
		  din = new Scanner(clientSocket.getInputStream());
		  pout = new PrintStream(clientSocket.getOutputStream());
		  pout.println(cmd);
		  pout.flush();
		  response = din.nextLine();
		  clientSocket.close();
		  return response;
		} catch(SocketTimeoutException e){
			serverList.remove(0);
			if(serverList.size()>0){
				sendTCP(serverList.get(0).getIp(), serverList.get(0).getPort(), cmd);
			}
		}
		catch(IOException e) {
		  System.err.println(e);
		}
		return new String();
	  }   
	
  public static void main (String[] args) {
    @SuppressWarnings("resource")
	Scanner sc = new Scanner(System.in);
    int numServer = sc.nextInt();
    
    for (int i = 0; i < numServer; i++) {
      String[] splitting = sc.nextLine().split(":");
      Address server = new Address(splitting[0], Integer.parseInt(splitting[1]));
      serverList.add(server);
    }

    while(sc.hasNextLine()) {
      String cmd = sc.nextLine();
      String[] tokens = cmd.split(" ");

      if (tokens[0].equals("purchase")) {
    	String response = sendTCP(serverList.get(0).getIp(), serverList.get(0).getPort(), cmd);
    	String[] rtokens = response.split(", ");
    	if (response.equals("Out of Stock")) {
    	  System.out.println("Not Available - Not enough items");
    	} else if (response.equals("Invalid Product Name")) {
  		  System.out.println("Not Available - We do not sell this product");
    	} else {
	    	System.out.println("Your order has been placed, " + Integer.parseInt(rtokens[0])
    		+ " " + rtokens[1] + " " + rtokens[2] + " " + Integer.parseInt(rtokens[3]));
    	}
      } 
      else if (tokens[0].equals("cancel")) {
    	String response = sendTCP(serverList.get(0).getIp(), serverList.get(0).getPort(), cmd);
    	if(response.equals("Not Found")) {
    	  System.out.println(tokens[1] + " not found, no such order.");
    	} else {
    	  System.out.println("Order " + tokens[1] + " is canceled");
    	}
      } 
      else if (tokens[0].equals("search")) {
    	String response = sendTCP(serverList.get(0).getIp(), serverList.get(0).getPort(), cmd);
    	if(response.equals("0")) {
    	  System.out.println("No orders found for " + tokens[1]);
    	} else {
    	  String[] rtokens = response.split(", ");
    	  for(int i = 1; i < Integer.parseInt(rtokens[0]) + 1; i++) {
    		String[] orderTokens = rtokens[i].split(" ");
    		System.out.println(Integer.parseInt(orderTokens[0]) + ", " + 
    				orderTokens[1] + ", " + Integer.parseInt(orderTokens[2]));
    	  }
    	}
      } 
      else if (tokens[0].equals("list")) {
    	String response = sendTCP(serverList.get(0).getIp(), serverList.get(0).getPort(), cmd);
    	String[] rtokens = response.split(", ");
  	    for(int i = 1; i < (Integer.parseInt(rtokens[0]) *2) + 1; i += 2) {
  		  System.out.println(rtokens[i] + " " + Integer.parseInt(rtokens[i+1]));
  	    }
      } 
      else {
        System.out.println("ERROR: No such command");
      }
    }
  }
}
