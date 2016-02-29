import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Server {
  public static void main (String[] args) {
    int N;
    int tcpPort;
    int udpPort;
    List<Item> Inventory = new ArrayList<Item>();
    List<User> UserDatabase = new ArrayList<User>();
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(2) <udpPort>: the port number for UDP connection");
      System.out.println("\t(3) <file>: the file of inventory");

      System.exit(-1);
    }
    N = Integer.parseInt(args[0]);
    tcpPort = Integer.parseInt(args[1]);
    udpPort = Integer.parseInt(args[2]);
    String fileName = args[3];
    
    // parse the inventory file
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
    
    // TODO: handle request from clients
    
    
  }
}
