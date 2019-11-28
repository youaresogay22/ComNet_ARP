package ARP;

public class RoutingTable {
	
	private byte[] RT_dstAddr;		// Destination address
	private byte[] RT_subnetMask;		// Subnet Mask
	private String RT_gateway;			// Gateway, 연결(*) 표시를 위해 String
	private boolean RT_flag_Up;			// Flag U
	private boolean RT_flag_Gateway;	// Flag UG
	private int RT_Interface;			// Interface
	
	public RoutingTable() {
		
		RT_dstAddr = new byte[4];
		RT_subnetMask = new byte[4];
		RT_gateway = null;
		RT_flag_Up = false;
		RT_flag_Gateway = false;
		RT_Interface = 0;
	}
	
	// Routing table에 add하는 함수
	public void addRoutingEntry(byte[] dstAddr, byte[] subnetMask, String gateway, boolean flag_Up, boolean flag_Gateway, int Interface) {

		RoutingTable rt = null;
		
		rt.RT_dstAddr = dstAddr;
		rt.RT_subnetMask = subnetMask;
		rt.RT_gateway = gateway;
		rt.RT_flag_Up = flag_Up;
		rt.RT_flag_Gateway = flag_Gateway;
		rt.RT_Interface = Interface;
		
	}
	
	// 아래는 Get 함수들
	public byte[] getDstAddr() {
		return this.RT_dstAddr;
	}
	
	public byte[] getSubnetMask() {
		return this.RT_subnetMask;
	}
	
	public String getGateway() {
		return this.RT_gateway;
	}
	
	public boolean getFlagUp() {
		return this.RT_flag_Up;
	}
	
	public boolean getFlagGateway() {
		return this.RT_flag_Gateway;
	}
	
	public int getInterface() {
		return this.RT_Interface;
	}
	
}
