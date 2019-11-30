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
			
			// Routing Table의  Destination Address
			byte[] rt_dstAddr = routingTable[i].getDstAddr();
			// Routing Table의 FlagUp
			boolean rt_flagUp = routingTable[i].getFlagUp();
			// * Routing Table의 FlagGateway, 필요 없을 지도?
			boolean rt_flagGateway = routingTable[i].getFlagGateway();
			
			// * 시나리오
			// 1. Host Address 검색 -> 있으면 flag 확인하여 하위레이어로 보냄
			// 2. 서브넷마스크 연산하여 검색 -> 있으면 flag 확인하여 하위레이어로 보냄
			// 3. * Default entry 검색 -> ???
			
			if (rt_dstAddr[i] == dstAddr[i]) {
				if (rt_flagUp == true) {
					// * IPLayer1 하위레이어로 보내는거 추가해야함
				} else {
					// * IPLayer2 하위레이어로 보내는거 추가해야함
				}
			} else {
				// Routing Table의 Subnet mask
				byte[] rt_subnetMask = routingTable[i].getSubnetMask();
				
				for (int j=0; j<4; j++) {
					// * 연산하는 법, 클래스 확인
					if (rt_dstAddr[j] == (dstAddr[j] & rt_subnetMask[j])) {
						if (rt_flagUp == true) {
							// * IPLayer1 하위레이어로 보내는거 추가해야함
						} else {
							// * IPLayer2 하위레이어로 보내는거 추가해야함
						}
					} else {
						// * Default entry 검색 추가해아함	
					}
				}
			}
		}
		return true;
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
