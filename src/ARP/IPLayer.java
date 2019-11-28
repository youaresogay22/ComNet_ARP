package ARP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import ARP.ARPLayer._Cache_Entry;
import ARP.ARPLayer._Proxy_Entry;

import static ARP.EthernetLayer.byte4To2;
import static ARP.EthernetLayer.intToByte;

public class IPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	RoutingTable[] routingTable;	// 라우팅테이블
	
	// router additional implementation

	// map은 중복되는 key를 허용하지 않으므로 다른 자료구조를 찾아봐야 할 것 같습니다.
	public Map<String, _Routing_Entry> routing_Table = new LinkedHashMap<String, _Routing_Entry>();
	public Set<String> routing_Table_Itr = routing_Table.keySet();

	// router: _IP_ADDR class moved from ARPlayer to IPlayer
	public static class _IP_ADDR {
		public byte[] addr = new byte[4];

		public _IP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
		}

		// 10/25 추가:_IP_ADDR toString 시 이제 1.1.1.1 꼴의 스트링을 반환합니다
		@Override
		public String toString() {
			String ipString = "";
			for (byte b : this.addr) {
				ipString += Integer.toString(b & 0xFF) + ".";
			}
			return ipString.substring(0, ipString.length() - 1);
		}

		// ip byte끼리의 and 연산 함수
		public byte[] do_And_operation(byte[] sourceIP) {
			byte[] temp = new byte[4];
			for (int i = 0; i < temp.length; i++) {
				temp[i] = (byte) (temp[i] & sourceIP[i]);
			}
			return temp;
		}
	}

	// router additional implementation
	public class _Routing_Entry implements Comparable<_Routing_Entry>{
		public byte[] getSubnetMask() {
			byte[] a = new byte[4];
			for(int i=0; i<4; i++)
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
		private boolean flag_Up; 	   // U flag
		private boolean flag_Gateway;  // G flag
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
		
		//subnet mask 내림차순 정렬. int형 subnetLen끼리 비교.
		@Override
		public int compareTo(_Routing_Entry re) {
			if(this.getSubnetLen() > re.getSubnetLen())
				return 1;
			else if (this.getSubnetLen() < re.getSubnetLen())
				return -1;
			else
				return 0;
		}
	}

	private class _IP_HEADER { // 20byte
		byte ip_verlen; // ip version -> IPv4 : 4 (O, 1byte)
		byte ip_tos; // type of service (X, 1byte)
		byte[] ip_len; // total packet length (△, 2byte)
		byte[] ip_id; // datagram id (X, 2byte)
		byte[] ip_fragoff; // fragment offset (X, 2byte)
		byte ip_ttl; // time to live in gatewaye hopes(X, 1byte)
		byte ip_proto; // IP protocol (X, 1byte)
		byte[] ip_cksum; // header checksum (X, 2byte)
		_IP_ADDR ip_src; // IP address of source (O, 4byte)
		_IP_ADDR ip_dst; // IP address of destination (O, 4byte)
		byte[] ip_data; // new (O, IP_DATA_SIZE)

		public _IP_HEADER() {
			this.ip_verlen = (byte) 0x00; // 0
			this.ip_tos = (byte) 0x00; // 1
			this.ip_len = new byte[2]; // 2-3
			this.ip_id = new byte[2]; // 4-5
			this.ip_fragoff = new byte[2]; // 6-7
			this.ip_ttl = (byte) 0x00; // 8
			this.ip_proto = (byte) 0x00; // 9
			this.ip_cksum = new byte[2]; // 10-11
			this.ip_src = new _IP_ADDR(); // 12-15
			this.ip_dst = new _IP_ADDR(); // 16-19
			this.ip_data = null;
		}
	}

	_IP_HEADER m_iHeader = new _IP_HEADER();

	// routing table에 add하는 함수
	public void addRoutingEntry(String input_destAddress, byte[] input_subnetMask, String input_gateway,
			boolean input_flag_Up, boolean input_flag_Gateway, int input_route_Interface) {
		
		if(!routing_Table.containsKey(input_destAddress)) { //중복방지
			_Routing_Entry additional = new _Routing_Entry(input_subnetMask, input_gateway, input_flag_Up,
					input_flag_Gateway, input_route_Interface);

			String str = byteArrayToBinaryString(input_subnetMask);	// byte[] subnetMask를 binary String으로 변환
			additional.setSubnetLen(checkOne(str));					// binary String에서 1의 개수를 구해서 자료구조 subnetLen으로 set
			if(additional.getSubnetLen() == 0) 						// subnetLen이 0이면 GUI에서 subnet 입력이 잘못된 것, 따라서 자료구조에 put하지 않는다
				System.out.println("올바른 형식의 서브넷 마스크를 입력하시오");
			else
				routing_Table.put(input_destAddress, additional);
		}
		//sort
		routing_Table = sortByValue(routing_Table);
		//print for debug
		printRoutingTable();
	}
	
	// input byte[4], then returns 00000000 00000000 00000000 00000000 Binary String (0 or 1)
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
			if (str.charAt(bit) == "1".charAt(0))		// str.charAt(bit) == 1
				count++;								// 1이 연속될 때만 count한다.
			else {										// 1의 연속이 끊겼다
				for(int bitAfter = bit+1; bitAfter < str.length(); bitAfter++) {	//1의 연속이 끝난 이후의 binaryString을 탐색
					if(str.charAt(bitAfter) == "1".charAt(0))	
						return 0;						// 1의 연속이 끝난 시점 이후에 1이 포함되어있다 -> 사용자가 올바른 형식의 서브넷을 입력하지 않은 것
				}
			}
		}
		return count;
	}
	
	// longest prefix match를 위한 subnet mask 내림차순 정렬
	public static Map<String, _Routing_Entry> sortByValue(Map<String,_Routing_Entry> map) {
		List<String> list = new ArrayList();
		list.addAll(map.keySet());			// key를 보유한 list
		
		Collections.sort(list, new Comparator<Object>(){
			public int compare(Object o1, Object o2) {
				Object compare1 = map.get(o1);
				Object compare2 = map.get(o2);
				
				return ((Comparable<Object>) compare1).compareTo(compare2);	// _Routing_Entry에 정의해 둔 comparTo를 실행
			}
		});
		Collections.reverse(list);	// 내림차순
		
		Map<String, _Routing_Entry> resultMap = new LinkedHashMap<>();
		for (String s : list)
			resultMap.put(s, map.get(s));
		
		return resultMap;
	}
	
	//print for debug
	public void printRoutingTable() {
		routing_Table_Itr = routing_Table.keySet();
		
		System.out.println("--------------------------------------");
		for (String key : routing_Table_Itr) 
			System.out.println(String.format("%18s", key) + String.format("%18s", routing_Table.get(key).subnetMask) 
			+ String.format("%15s", routing_Table.get(key).gateway) +String.format("%5s", routing_Table.get(key).subnetLen));
	}
	
	public Map<String, _Routing_Entry> getRoutingList() {
		return routing_Table;
	}

	public void setSrcAddr(String ip) {
		StringTokenizer st = new StringTokenizer(ip, ".");

		for (int i = 0; i < 4; i++) {
			m_iHeader.ip_src.addr[i] = (byte) Integer.parseInt(st.nextToken());
		}
	}

	public void setDstAddr(String ip) {
		StringTokenizer st = new StringTokenizer(ip, ".");

		for (int i = 0; i < 4; i++)
			m_iHeader.ip_src.addr[i] = (byte) Integer.parseInt(st.nextToken());
	}

	public IPLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 4; i++) {
			m_iHeader.ip_src.addr[i] = (byte) 0x00;
			m_iHeader.ip_dst.addr[i] = (byte) 0x00;
		}
		for (int i = 0; i < 2; i++) {
			m_iHeader.ip_len[i] = (byte) 0x00;
			m_iHeader.ip_id[i] = (byte) 0x00;
			m_iHeader.ip_fragoff[i] = (byte) 0x00;
			m_iHeader.ip_cksum[i] = (byte) 0x00;
		}
		m_iHeader.ip_verlen = (byte) 0x00;
		m_iHeader.ip_tos = (byte) 0x00;
		m_iHeader.ip_ttl = (byte) 0x00;
		m_iHeader.ip_proto = (byte) 0x00;

		m_iHeader.ip_data = null;
	}

	public byte[] ObjToByte(_IP_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 20];

		buf[0] = Header.ip_verlen; // buf에 1byte verlen 초기화
		buf[1] = Header.ip_tos; // 1byte tos
		System.arraycopy(Header.ip_len, 0, buf, 2, 2); // 2byte len
		System.arraycopy(Header.ip_id, 0, buf, 4, 2); // 2byte id
		System.arraycopy(Header.ip_fragoff, 0, buf, 6, 2); // 2byte fragoff
		buf[8] = Header.ip_ttl; // 1byte ttl
		buf[9] = Header.ip_proto; // 1byte proto
		System.arraycopy(Header.ip_cksum, 0, buf, 10, 2); // 2byte cksum
		System.arraycopy(Header.ip_src.addr, 0, buf, 12, 4); // 4byte src
		System.arraycopy(Header.ip_dst.addr, 0, buf, 16, 4); // 4byte dst
		System.arraycopy(input, 0, buf, 20, length); // IP header에 TCP header 붙이기

		for (int i = 0; i < length; i++)
			buf[20 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) {
		// send 내에 구현사항을 많이 넣으면 가독성이 떨어져서 ObjToByte로 분리했습니다.
		byte[] IP_header_added_bytes = ObjToByte(m_iHeader, input, length);

		this.GetUnderLayer().Send(IP_header_added_bytes, IP_header_added_bytes.length);
		return false;
	}

	public boolean GratSend(byte[] input, int length) {
		byte[] TCP_header_added_bytes = ObjToByte(m_iHeader, input, length);

		this.GetUnderLayer().GratSend(TCP_header_added_bytes, TCP_header_added_bytes.length);
		return false;
	}

	public byte[] arrangeIPHeader(byte[] input, int length) {
		// IP header 제거하지 않고 목적지와 발신지만 수정
		return null;
	}

	public boolean Receive(byte[] input) {
		// routing table 탐색 후
		// 맞는 interface로 전송
		// arrangeIPHeader(input)
				
		byte[] data;
		data = new byte[input.length];
		System.arraycopy(input, 0, data, 0, input.length);
				
		byte[] dstAddr = new byte[4];	// input의 목적주소만 빼서 따로 저장
		dstAddr[0] = input[16];
		dstAddr[1] = input[17];
		dstAddr[2] = input[18];
		dstAddr[3] = input[19];
		
		for (int i=0; i<10; i++) {	// Routing Table 검색을 위한 for문, Routing 테이블 한줄한줄 읽기
									// * 임시로 i<10 넣어둠, 수정해야함
			
			// rt_dstAddr에 RoutingTable의 모든 Destination Address 저장
			byte[] rt_dstAddr = routingTable[i].getDstAddr();
			
			// rt_subnetMask에 RoutingTable의 모든 Subnet mask 저장
			for (int j=0; j<4; j++) {
				byte[] rt_subnetMask = routingTable[j].getSubnetMask();
				
				// input의 목적주소와 Routing Table의 서브넷 마스크를 연산하여
				// 같으면 IPLayer1의 하위 레이어로 보내고 다르면 IPLayer2의 하위 레이어로 보냄
				if (rt_dstAddr[j] == (dstAddr[j] & rt_subnetMask[j])) {
					
				} else {
					
				}
			}
		}
		return false;
	}

	public boolean Receive() {
		// baselayer 기본 함수, 쓸 일 없을 듯
		return false;
	}

	public boolean Send(String filename) {
		// baselayer 기본 함수, 쓸 일 없을 듯
		return false;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}
}
