import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Client {
  private static String sendUDP(String hostname, int udpPort, String cmd) {
	  byte[] rbuffer = new byte[1024];
	  DatagramPacket sPacket, rPacket;
	  DatagramSocket datasocket = null;
	  try{
		  InetAddress ia = InetAddress.getByName(hostname);
		  datasocket = new DatagramSocket();
		  byte[] buffer = new byte[cmd.length()];
		  buffer = cmd.getBytes();
		  sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
		  datasocket.send(sPacket);
		  rPacket = new DatagramPacket(rbuffer, rbuffer.length);
		  datasocket.receive(rPacket);
		  return new String(rPacket.getData(), 0, rPacket.getLength());
	  } catch(UnknownHostException e){
		  System.err.println(e);
	  } catch(SocketException e) {
		  System.err.println(e);
	  } catch(IOException e){
		  System.err.println(e);
	  } finally {
		  datasocket.close();
	  }
	  return new String();
  }
  
  private static String sendTCP(String hostname, int tcpPort, String cmd) {
	  return "not done";
  }
  
  private static String send(String hostAddress, int tcpPort, int udpPort, String cmd, String protocol) {
	if(protocol == "U") {
  		return sendUDP(hostAddress, udpPort, cmd);
  	} else if(protocol == "T") {
  		return sendTCP(hostAddress, tcpPort, cmd);
  	} else {
  		return "u stoopid";
  	}
  }
  
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <hostAddress>: the address of the server");
      System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(3) <udpPort>: the port number for UDP connection");
      System.exit(-1);
    }

    hostAddress = args[0];
    tcpPort = Integer.parseInt(args[1]);
    udpPort = Integer.parseInt(args[2]);

    Scanner sc = new Scanner(System.in);
    while(sc.hasNextLine()) {
      String cmd = sc.nextLine();
      String[] tokens = cmd.split(" ");

      if (tokens[0].equals("purchase")) {
    	String response = send(hostAddress, tcpPort, udpPort, cmd, tokens[tokens.length-1]);
    	String[] rtokens = response.split(", ");
    	if (response == "Out of Stock") {
    	  System.out.println("Not Available - Not enough items");
    	} else if (response == "Invalid Product Name") {
  		  System.out.println("Not Available - We do not sell this product");
    	} else {
	    	System.out.println("Your order has been placed, " + Integer.parseInt(rtokens[0])
    		+ " " + rtokens[1] + " " + rtokens[2] + " " + Integer.parseInt(rtokens[3]));
    	}
      } 
      else if (tokens[0].equals("cancel")) {
    	String response = send(hostAddress, tcpPort, udpPort, cmd, tokens[tokens.length-1]);
    	if(response == "Not Found") {
    	  System.out.println(tokens[1] + " not found, no such order.");
    	} else {
    	  System.out.println("Order " + tokens[1] + " is canceled");
    	}
      } 
      else if (tokens[0].equals("search")) {
    	String response = send(hostAddress, tcpPort, udpPort, cmd, tokens[tokens.length-1]);
    	if(response == "0") {
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
    	String response = send(hostAddress, tcpPort, udpPort, cmd, tokens[tokens.length-1]);
    	String[] rtokens = response.split(", ");
  	    for(int i = 1; i < Integer.parseInt(rtokens[0]) + 1; i++) {
  		  System.out.println(Integer.parseInt(rtokens[i]));
  	    }
      } 
      else {
        System.out.println("ERROR: No such command");
      }
    }
  }
}
