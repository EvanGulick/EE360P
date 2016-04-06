public class Address {
	String ipAddress;
	int portnum;
	
	public Address(String ipAddress, int portnum) {
		this.ipAddress = ipAddress;
		this.portnum = portnum;
	}
	
	public int getPort() {
		return portnum;
	}
	
	public String getIp(){
		return ipAddress;
	}
}