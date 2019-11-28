package ARP;

public class RoutingTable {
	
	private byte[] RT_dstAddr;			// Destination address
	private byte[] RT_subnetMask;		// Subnet Mask
	private String RT_gateway;			// Gateway, 연결(*) 표시를 위해 String
	private boolean RT_flag_Up;			// Flag U
	private boolean RT_flag_Gateway;	// Flag UG
	private int RT_Interface;			// Interface
//	private int RT_Class;				// Subnet 클래스 구분, 필요하면 추가
	
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

		RT_dstAddr = dstAddr;
		RT_subnetMask = subnetMask;
		RT_gateway = gateway;
		RT_flag_Up = flag_Up;
		RT_flag_Gateway = flag_Gateway;
		RT_Interface = Interface;
		
//		// Subnet 클래스 구분하는 if문, 필요하면 추가
//		if (subnetMask[0]!=0x00) {			// 255.255.255.255
//			RT_Class = 0;
//		} else if (subnetMask[1]!=0x00) {	// 255.255.255.0 : Class C
//			RT_Class = 1;
//		} else if (subnetMask[2]!=0x00) {	// 255.255.0.0 : Class B
//			RT_Class = 2;
//		} else if (subnetMask[3]!=0x00) {	// 255.0.0.0 : Class A
//			RT_Class = 3;
//		}
		
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
