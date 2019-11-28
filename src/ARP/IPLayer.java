package ARP;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static ARP.EthernetLayer.byte4To2;
import static ARP.EthernetLayer.intToByte;

public class IPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	// router additional implementation

	// map은 중복되는 key를 허용하지 않으므로 다른 자료구조를 찾아봐야 할 것 같습니다.
	public Map<byte[], _Routing_Entry> routing_Table = new LinkedHashMap<byte[], _Routing_Entry>();
	public Set<byte[]> routing_Table_Itr = routing_Table.keySet();

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
	private class _Routing_Entry {
		public _IP_ADDR getSubnetMask() {
			return subnetMask;
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

		private _IP_ADDR subnetMask;
		private String gateway;
		private boolean flag_Up; // U flag
		private boolean flag_Gateway; // G flag
		private int route_Interface;

		public _Routing_Entry(byte[] input_subnetMask, String input_gateway, boolean input_flag_Up,
				boolean input_flag_Gateway, int input_route_Interface) {
			for (int i = 0; i < input_subnetMask.length; i++) {
				this.subnetMask.addr[i] = input_subnetMask[i];
			}
			this.gateway = input_gateway;
			this.flag_Up = input_flag_Up;
			this.flag_Gateway = input_flag_Gateway;
			this.route_Interface = input_route_Interface;
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
	// gateway 별표
	public void addRoutingEntry(byte[] input_destAddress, byte[] input_subnetMask, String input_gateway,
			boolean input_flag_Up, boolean input_flag_Gateway, int input_route_Interface) {

		_Routing_Entry additional = new _Routing_Entry(input_subnetMask, input_gateway, input_flag_Up,
				input_flag_Gateway, input_route_Interface);

		routing_Table.put(input_destAddress, additional);
		// sort()
	}

	public void sort() {
		// routing table sorting 함수
	}

	public void setSrcAddr(String ip) {
		StringTokenizer st = new StringTokenizer(ip, ".");

		for (int i = 0; i < 4; i++) {
			m_iHeader.ip_src.addr[i] = (byte) Integer.parseInt(st.nextToken());
		} // 인코딩
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
		byte[] data;
		// routing table 탐색 후
		// 맞는 interface로 전송
		// arrangeIPHeader(input)
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
