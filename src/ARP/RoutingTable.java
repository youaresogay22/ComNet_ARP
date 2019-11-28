package ARP;

public class RoutingTable {
	
	private byte[] RT_dstAddress;
	private byte[] RT_subnetMask;
	private String RT_gateway;
	private boolean RT_flag_Up;
	private boolean RT_flag_Gateway;
	private int RT_Interface;
	
	public RoutingTable() {
		
		RT_dstAddress = new byte[4];
		RT_subnetMask = new byte[4];
		RT_gateway = null;
		RT_flag_Up = false;
		RT_flag_Gateway = false;
		RT_Interface = 0;
	}
	
	// routing table에 add하는 함수
	public void addRoutingEntry(byte[] dstAddress, byte[] subnetMask, String gateway, boolean flag_Up, boolean flag_Gateway, int Interface) {

		RoutingTable rt = null;
		
		rt.RT_dstAddress = dstAddress;
		rt.RT_subnetMask = subnetMask;
		rt.RT_gateway = gateway;
		rt.RT_flag_Up = flag_Up;
		rt.RT_flag_Gateway = flag_Gateway;
		rt.RT_Interface = Interface;
	}
}
