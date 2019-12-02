package ARP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ARP.IPLayer._IP_ADDR;

public class RoutingTable {
	
	public Map<String, _Routing_Entry> routing_Table = new LinkedHashMap<String, _Routing_Entry>();
	public Set<String> routing_Table_Itr = routing_Table.keySet();
	
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
	
	// router additional implementation
	public class _Routing_Entry implements Comparable<_Routing_Entry> {
		public byte[] getSubnetMask() {
			byte[] a = new byte[4];
			for (int i = 0; i < 4; i++)
				a[i] = this.subnetMask.addr[i];
			return a;
		}

		public void setSubnetMask(_IP_ADDR subnetMask) {
			this.subnetMask = subnetMask;
		}

		public String getGateway() {
			return gateway;
		}

		public void setGateway(String gateway) {
			this.gateway = gateway;
		}

		public boolean isFlag_Up() {
			return flag_Up;
		}

		public void setFlag_Up(boolean flag_Up) {
			this.flag_Up = flag_Up;
		}

		public boolean isFlag_Gateway() {
			return flag_Gateway;
		}

		public void setFlag_Gateway(boolean flag_Gateway) {
			this.flag_Gateway = flag_Gateway;
		}

		public int getRoute_Interface() {
			return route_Interface;
		}

		public void setRoute_Interface(int route_Interface) {
			this.route_Interface = route_Interface;
		}

		public int getSubnetLen() {
			return subnetLen;
		}

		public void setSubnetLen(int subnetLen) {
			this.subnetLen = subnetLen;
		}

		private _IP_ADDR subnetMask;
		private String gateway;
		private boolean flag_Up; 		// U flag
		private boolean flag_Gateway; 	// G flag
		private int route_Interface;
		private int subnetLen;

		public _Routing_Entry(byte[] input_subnetMask, String input_gateway, boolean input_flag_Up,
				boolean input_flag_Gateway, int input_route_Interface) {
			setSubnetMask(new _IP_ADDR());
			for (int i = 0; i < input_subnetMask.length; i++) {
				this.subnetMask.addr[i] = input_subnetMask[i];
			}
			this.gateway = input_gateway;
			this.flag_Up = input_flag_Up;
			this.flag_Gateway = input_flag_Gateway;
			this.route_Interface = input_route_Interface;
		}

		// subnet mask 내림차순 정렬. int형 subnetLen끼리 비교.
		@Override
		public int compareTo(_Routing_Entry re) {
			if (this.getSubnetLen() > re.getSubnetLen())
				return 1;
			else if (this.getSubnetLen() < re.getSubnetLen())
				return -1;
			else
				return 0;
		}
	}

	// routing table에 add하는 함수
	public void addRoutingEntry(String input_destAddress, byte[] input_subnetMask, String input_gateway,
			boolean input_flag_Up, boolean input_flag_Gateway, int input_route_Interface) {

		if (!routing_Table.containsKey(input_destAddress)) { // 중복방지
			_Routing_Entry additional = new _Routing_Entry(input_subnetMask, input_gateway, input_flag_Up,
					input_flag_Gateway, input_route_Interface);

			String str = byteArrayToBinaryString(input_subnetMask); 	// byte[] subnetMask를 binary String으로 변환
			additional.setSubnetLen(checkOne(str)); 					// binary String에서 1의 개수를 구해서 자료구조 subnetLen으로 set
			if (additional.getSubnetLen() == 0) 						// subnetLen이 0이면 GUI에서 subnet 입력이 잘못된 것, 따라서 자료구조에 put하지 않는다
				System.out.println("올바른 형식의 서브넷 마스크를 입력하시오");
			else
				routing_Table.put(input_destAddress, additional);
		}
		// sort
		routing_Table = sortByValue(routing_Table);
		// print for debug
		printRoutingTable();
	}

	// input byte[4], then returns 00000000 00000000 00000000 00000000 Binary String
	// (0 or 1)
	// 이 바이너리 스트링은 subnetLen을 계산하는 데 쓰인다. 그리고 subnetLen은 sort할 때 쓰인다.
	public static String byteArrayToBinaryString(byte[] n) {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < 4; i++) {
			StringBuilder sb = new StringBuilder("00000000");
			for (int bit = 0; bit < 8; bit++) {
				if (((n[i] >> bit) & 1) > 0) {
					sb.setCharAt(7 - bit, '1');
				}
			}
			result.append(sb);
		}
		return result.toString();
	}

	// BinaryString에서 연속되는 1이 몇 개인지 check. subnet은 연속되는 1이므로
	public static int checkOne(String str) {
		int count = 0;
		for (int bit = 0; bit < str.length(); bit++) {
			if (str.charAt(bit) == "1".charAt(0)) 									// str.charAt(bit) == 1
				count++; 															// 1이 연속될 때만 count한다.
			else { 																	// 1의 연속이 끊겼다
				for (int bitAfter = bit + 1; bitAfter < str.length(); bitAfter++) { // 1의 연속이 끝난 이후의 binaryString을 탐색
					if (str.charAt(bitAfter) == "1".charAt(0))
						return 0; 					// 1의 연속이 끝난 시점 이후에 1이 포함되어있다 -> 사용자가 올바른 형식의 서브넷을 입력하지 않은 것
				}
			}
		}
		return count;
	}

	// longest prefix match를 위한 subnet mask 내림차순 정렬
	public static Map<String, _Routing_Entry> sortByValue(Map<String, _Routing_Entry> map) {
		List<String> list = new ArrayList();
		list.addAll(map.keySet()); 											// key를 보유한 list

		Collections.sort(list, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				Object compare1 = map.get(o1);
				Object compare2 = map.get(o2);

				return ((Comparable<Object>) compare1).compareTo(compare2); // _Routing_Entry에 정의해 둔 comparTo를 실행
			}
		});
		Collections.reverse(list); // 내림차순

		Map<String, _Routing_Entry> resultMap = new LinkedHashMap<>();
		for (String s : list)
			resultMap.put(s, map.get(s));

		return resultMap;
	}

	// print for debug
	public void printRoutingTable() {
		routing_Table_Itr = routing_Table.keySet();

		System.out.println("--------------------------------------");
		for (String key : routing_Table_Itr)
			System.out.println(String.format("%18s", key) + String.format("%18s", routing_Table.get(key).subnetMask)
					+ String.format("%15s", routing_Table.get(key).gateway)
					+ String.format("%5s", routing_Table.get(key).subnetLen));
	}

	public Map<String, _Routing_Entry> getRoutingList() {
		return routing_Table;
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
