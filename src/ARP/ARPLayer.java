package ARP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.net.NetworkInterface;
import java.net.SocketException;
import static ARP.EthernetLayer.byte4To2;
import static ARP.EthernetLayer.intToByte;
import static ARP.IPLayer._IP_ADDR;

public class ARPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public Map<String, _Cache_Entry> cache_Table = new HashMap<String, _Cache_Entry>();
	public Map<String, _Proxy_Entry> proxy_Table = new HashMap<String, _Proxy_Entry>();
	public Set<String> cache_Itr = cache_Table.keySet();
	private final byte[] OP_ARP_REQUEST = byte4To2(intToByte(1));
	private final byte[] OP_ARP_REPLY = byte4To2(intToByte(2));
	public final _IP_ADDR MY_IP_ADDRESS = new _IP_ADDR();
	public final _ETHERNET_ADDR MY_MAC_ADDRESS = new _ETHERNET_ADDR();

	public class _Cache_Entry {
		// basic cache
		byte[] cache_ethaddr;
		String cache_status;
		int cache_ttl; // time to live

		public _Cache_Entry(byte[] ethaddr, String status, int ttl) {
			cache_ethaddr = ethaddr;
			cache_status = status;
			cache_ttl = ttl;
		}
	}

	public class _Proxy_Entry {
		byte[] proxy_ethaddr;
		String proxy_device;

		public _Proxy_Entry(byte[] ethaddr, String device) {
			proxy_ethaddr = ethaddr;
			proxy_device = device;
		}
	}

	private class _ETHERNET_ADDR {
		private byte[] addr = new byte[6];

		public _ETHERNET_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
			this.addr[4] = (byte) 0x00;
			this.addr[5] = (byte) 0x00;
		}

		// 10/25 추가:_ETHERNET_ADDR toString 시 이제 11:11:11:11:11:11 꼴의 스트링을 반환합니다
		public String toString() {
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < 6; ++i) {
				sb.append(String.format("%02X", this.addr[i]));
				sb.append(":");
			}

			sb.deleteCharAt(sb.length() - 1);
			return sb.toString();
		}
	}

	private class _ARP_HEADER {
		byte[] arp_hdType; // Ethernet = 1
		byte[] arp_prototype; // Ipv4 = 0x0800
		byte arp_hdLength; // MAC address = 6
		byte arp_protoLength; // IP address = 4
		byte[] arp_op; // request = 1, reply = 2
		_ETHERNET_ADDR arp_srcHdAddr; // Sender's Ethernet Address
		_IP_ADDR arp_srcProtoAddr;// Sender's IP Address
		_ETHERNET_ADDR arp_destHdAddr; // Receiver's Ethernet Address
		_IP_ADDR arp_destProtoAddr; // Receiver's IP Address

		public _ARP_HEADER() { // 28byte
			this.arp_hdType = new byte[2];
			this.arp_prototype = new byte[2];
			this.arp_hdLength = (byte) 0x00;// 4
			this.arp_protoLength = (byte) 0x00;// 5
			this.arp_op = new byte[2];// 6~7
			this.arp_srcHdAddr = new _ETHERNET_ADDR(); // 8~13
			this.arp_srcProtoAddr = new _IP_ADDR(); // 14~17
			this.arp_destHdAddr = new _ETHERNET_ADDR();// 18~23
			this.arp_destProtoAddr = new _IP_ADDR(); // 24~27
		}
	}

	_ARP_HEADER m_aHeader = new _ARP_HEADER();

	public ARPLayer(String pName) throws SocketException {
		// super(pName);
		pLayerName = pName;
		getMyPCAddr();
		ResetHeader();
		run_Clean_Thread(cache_Table, cache_Itr);
		// 캐시테이블 자동 제거 스레드
	}

	public void ResetHeader() {
		for (int i = 0; i < 6; i++) {
			m_aHeader.arp_srcHdAddr.addr[i] = (byte) 0x00;
			m_aHeader.arp_destHdAddr.addr[i] = (byte) 0x00;
		}
		for (int i = 0; i < 4; i++) {
			m_aHeader.arp_srcProtoAddr.addr[i] = (byte) 0x00;
			m_aHeader.arp_destProtoAddr.addr[i] = (byte) 0x00;
		}
		for (int i = 0; i < 2; i++) {
			m_aHeader.arp_hdType[i] = (byte) 0x00;
			m_aHeader.arp_prototype[i] = (byte) 0x00;
			m_aHeader.arp_op[i] = (byte) 0x00;
		}
		m_aHeader.arp_hdLength = (byte) 0x00;
		m_aHeader.arp_protoLength = (byte) 0x00;
	}

	public byte[] ObjToByte(_ARP_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 28];

		buf[0] = Header.arp_hdType[0];
		buf[1] = Header.arp_hdType[1];
		buf[2] = Header.arp_prototype[0];
		buf[3] = Header.arp_prototype[1];
		buf[4] = Header.arp_hdLength;
		buf[5] = Header.arp_protoLength;
		buf[6] = Header.arp_op[0];
		buf[7] = Header.arp_op[1];

		buf[8] = Header.arp_srcHdAddr.addr[0];
		buf[9] = Header.arp_srcHdAddr.addr[1];
		buf[10] = Header.arp_srcHdAddr.addr[2];
		buf[11] = Header.arp_srcHdAddr.addr[3];
		buf[12] = Header.arp_srcHdAddr.addr[4];
		buf[13] = Header.arp_srcHdAddr.addr[5];

		buf[14] = Header.arp_srcProtoAddr.addr[0];
		buf[15] = Header.arp_srcProtoAddr.addr[1];
		buf[16] = Header.arp_srcProtoAddr.addr[2];
		buf[17] = Header.arp_srcProtoAddr.addr[3];

		buf[18] = Header.arp_destHdAddr.addr[0];
		buf[19] = Header.arp_destHdAddr.addr[1];
		buf[20] = Header.arp_destHdAddr.addr[2];
		buf[21] = Header.arp_destHdAddr.addr[3];
		buf[22] = Header.arp_destHdAddr.addr[4];
		buf[23] = Header.arp_destHdAddr.addr[5];

		buf[24] = Header.arp_destProtoAddr.addr[0];
		buf[25] = Header.arp_destProtoAddr.addr[1];
		buf[26] = Header.arp_destProtoAddr.addr[2];
		buf[27] = Header.arp_destProtoAddr.addr[3];

		for (int i = 0; i < length; i++)
			buf[28 + i] = input[i];

		return buf;
	}

	// ARPLayer에서 만든 cache_Table을 다른 Layer에서도 사용할 수 있게끔 해주는 기능.
	public Map<String, _Cache_Entry> getCacheList() {
		return cache_Table;
	}

	public Map<String, _Proxy_Entry> getProxyList() {
		return proxy_Table;
	}

	public void setProxyTable(String key, byte[] ethaddr, String device) {
		if (!proxy_Table.containsKey(key))
			proxy_Table.put(key, new _Proxy_Entry(ethaddr, device));
		System.out.println("ProxyTable = " + proxy_Table); // 디버깅
	}

	public boolean isProxyTableEmpty() {
		if (proxy_Table.size() == 0)
			return true;
		return false;
	}

	public boolean Send(byte[] input, int length) {
		if (cameFromDlg(input)) { // 비어 있는 byte[]다 -> Dlg에서 왔다 -> Send Request Message.
			setARPHeaderBeforeSend(); // opCode를 포함한 hdtype,prototype,hdLen,protoLen 초기화. opCode의 default는 1이다.
			setSrcMAC(MY_MAC_ADDRESS.addr);
			setSrcIPAddr(MY_IP_ADDRESS.addr);
			// setDst는 GUI에서 이루어지고 있다.
			byte[] ARP_header_added_bytes = ObjToByte(m_aHeader, input, length);

			String target_IP = getDstAddrFromHeader(ARP_header_added_bytes);
			_Cache_Entry cache_Entry = // dst_Addr를 KEY로 갖고 cache_Entry를 VALUE로 갖는 hashMap 생성
					new _Cache_Entry(new byte[6], "Incomplete", 80);

			if (!cache_Table.containsKey(target_IP))
				cache_Table.put(target_IP, cache_Entry);

			this.GetUnderLayer().Send(ARP_header_added_bytes, ARP_header_added_bytes.length); // Send Request Message
		} else { // byte[]가 비어있지 않다 -> Send Reply Message
			for (int i = 0; i < 6; i++)
				input[i + 18] = MY_MAC_ADDRESS.addr[i]; // 패킷의 ???(target MAC)를 내 PC의 MAC 주소로 갱신
			input = swappingAddr(input); // src 주소 <-> target 주소 swapping
			input[6] = (byte) 0x00; // setOpCode(2)
			input[7] = (byte) 0x02;
			this.GetUnderLayer().Send(input, input.length); // Send Reply Message
		}
		return false;
	}

	public boolean cameFromDlg(byte[] input) {
		for (int i = 0; i < input.length; i++) {
			if (input[i] == 0)
				continue;
			else
				return false;
		}
		return true;
	}

	// 10/25 수정: 같은 네트워크 상에서 ARP를 받아도 target이 자신이 아니면 캐시 테이블 업데이트는 하지 않는 것 같습니다.

	public boolean Receive(byte[] input) {
		boolean Mine = IsItMine(input);
		if (isRequest(input)) {// ARP request 인 경우
			if (isProxyARP(input)) {// proxy ARP request 인 경우
				boolean proxyTrue = proxyRQReceive(input, input.length);
				if (proxyTrue == true) {
					proxyRPSend(input, input.length);
					return true;
				}
				return false;
			} else if (isGratuitousARP(input)) {// Gratuitous ARP request 인 경우
				updateCache(input);
				return true;
			} else {// basic ARP request 인 경우
				if (Mine) {
					updateCache(input);
					Send(input, input.length);// 내 ip 주소로 온 ARP일 때만 reply
					return true;
				} else
					return false;

			}
		} else if (isReply(input)) {// ARP reply 인 경우
			if (isProxyARP(input)) {// proxy ARP reply 인 경우
				if (Mine) {// then proxy send
					proxyRPReceive(input);
					return true;
				} else
					return false;
			} // else if (isGratuitousARP(input)) {
				// if (Mine) {Gratuitous ARP reply 인 경우: 추가 구현 사항이므로 구현하지 않음
				// then Gratuitous send
				// return true;
				// } else
				// return false;
				// }

			else {// basic ARP reply 인 경우
				if (Mine) {
					updateCache(input);
					return true;
				} else
					return false;
			}
		} else
			return false;
	}

	// Grat Send
	public boolean GratSend(byte[] input, int length) {
		// ARP헤더 초기 세팅
		setARPHeaderBeforeSend();
		// Sender's hardware address를 세팅,
		// 바꿀 MAC주소를 가져와서 넣기(Dlg)
		// Sender's protocol address를 세팅
		setSrcIPAddr(MY_IP_ADDRESS.addr); // 자기 IP주소 가져와서 넣기
		// Target's protocol address를 세팅
		setDstIPAddr(MY_IP_ADDRESS.addr); // 자기 IP주소 가져와서 넣기

		// Gratuitous ARP Message 생성
		byte[] grat_message = ObjToByte(m_aHeader, input, length); // ARPMessage

		// Gratuitous ARP Message 내려보내기
		this.GetUnderLayer().Send(grat_message, grat_message.length);
		return true;
	}

	public boolean proxyRQReceive(byte[] input, int length) {
		// 1. arp cache table 업데이트
		_ETHERNET_ADDR newHdAddr = new _ETHERNET_ADDR();
		for (int i = 0; i < 6; i++) {
			newHdAddr.addr[i] = input[8 + i];
		}
		String srcIP = getDstAddrFromSwappedHeader(input);
		_Cache_Entry newEntry = new _Cache_Entry(newHdAddr.addr, "Complete", 12);
		cache_Table.put(srcIP, newEntry);
		// 2. target protocol address가 proxy table에 있는지 확인
		// 헤더의 target protocol address를 가져온다.
		_IP_ADDR target = new _IP_ADDR();
		for (int i = 0; i < 4; i++) {
			target.addr[i] = input[i + 24];
		}
		// cache table에서 찾기 위해 target protocol address를 string으로 변환
		String tIpAddr = target.toString();
		// proxy table에 target proto addr이 존재하면 proxy reply send
		if (proxy_Table.containsKey(tIpAddr) == true) {
			return true;
		}
		// cache_Table에 target proto addr이 존재하지 않으면 message drop
		return false;
	}

	public boolean proxyRPSend(byte[] input, int length) {
		// 1. arp message의 target protocol address가 proxy entry에 있는지 이미 proxyRQReceive에서
		// 확인함.
		// 2. arp message 작성
		// setARPHeaderBeforeSend();
		// _IP_ADDR target = new _IP_ADDR();
		// for (int i = 0; i < 4; i++) {
		// target.addr[i] = input[i + 24];
		// }
		// cache table에서 찾기 위해 target protocol address를 string으로 변환
		// String tIpAddr = target.addr.toString();
		// 찾은 ip주소로 target hardware address를 가져온다.
		// mac에는 target hardware address가 들어있다.
		// byte[] mac = proxy_Table.get(tIpAddr).proxy_ethaddr;
		// header의 target hardware address에 자기의 맥주소를 넣어야 한다.(10/27)
		for (int i = 0; i < 6; i++) {
			input[i + 18] = MY_MAC_ADDRESS.addr[i];
		}
		// ※ 주소 swapping
		input = swappingAddr(input);
		// opcode를 reply(2)로 변경
		input[6] = (byte) 0x00;
		input[7] = (byte) 0x02;
		// byte[] bytes = ObjToByte(m_aHeader, input, length);
		this.GetUnderLayer().Send(input, input.length);
		return true;
	}

	public boolean proxyRPReceive(byte[] input) {
		// 헤더에서 target ip, target mac 가져오기
		_IP_ADDR targetIP = new _IP_ADDR();
		for (int i = 0; i < 4; i++) {
			targetIP.addr[i] = input[i + 24];
		}
		_ETHERNET_ADDR targetMAC = new _ETHERNET_ADDR();
		for (int i = 0; i < 6; i++) {
			targetMAC.addr[i] = input[i + 18];
		}
		// 1. 내게 온 ARP RP message가 맞는지 확인
		// 나의 ip주소와 target ip가 일치하는지 확인, 나의 mac주소와 target mac주소가 일치하는지 확인
		if (Arrays.equals(MY_IP_ADDRESS.addr, targetIP.addr) && Arrays.equals(MY_MAC_ADDRESS.addr, targetMAC.addr)) {
			// 2. sender protocol address가 내 cache_Table에 있는지 확인
			_IP_ADDR senderIP = new _IP_ADDR();
			for (int i = 0; i < 4; i++) {
				senderIP.addr[i] = input[i + 14];
			}
			String srcIpAddr = senderIP.toString();
			_ETHERNET_ADDR senderMAC = new _ETHERNET_ADDR();
			for (int i = 0; i < 6; i++) {
				senderMAC.addr[i] = input[i + 8];
			}
			// 내 cache_Table에 있다면 내 cache_table에 sender hd address 업데이트
			if (cache_Table.containsKey(srcIpAddr) == true) {
				cache_Table.replace(srcIpAddr, new _Cache_Entry(senderMAC.addr, "Complete", 10));
			}
			return true;
		}
		return false;
	}

	public void getMyPCAddr() throws SocketException {
		Enumeration<NetworkInterface> interfaces = null;
		interfaces = NetworkInterface.getNetworkInterfaces(); // 현재 PC의 모든 NIC를 열거형으로 받는다.

		// isUP 중에 MAC과 IP 주소를 출력
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			if (networkInterface.isUp()) {
				if (networkInterface.getHardwareAddress() != null) {// loop back Interface가 null이므로, 걸러준다.
					MY_MAC_ADDRESS.addr = networkInterface.getHardwareAddress(); // MAC주소 받기
					MY_IP_ADDRESS.addr = networkInterface.getInetAddresses().nextElement().getAddress(); // IP주소 받기

					break; // 현재 사용중인 NIC 이외에는 필요 없다. 반복문 탈출
				}
			}
		}
	}

	public void setARPHeaderBeforeSend() {
		this.m_aHeader.arp_hdType = byte4To2(intToByte(1));
		this.m_aHeader.arp_prototype[0] = (byte) 0x08;
		this.m_aHeader.arp_prototype[1] = (byte) 0x00;
		this.m_aHeader.arp_hdLength = 6;
		this.m_aHeader.arp_protoLength = 4;
		this.m_aHeader.arp_op[1] = 1;

	}

	private class tableCleanThread implements Runnable {
		private Map<String, _Cache_Entry> my_cache_Table;
		private Set<String> my_cache_Itr;

		public tableCleanThread(Map<String, _Cache_Entry> cachetable, Set<String> cacheIterator) {
			this.my_cache_Table = cachetable;
			this.my_cache_Itr = cacheIterator;
		}

		@Override
		public void run() {
			ArrayList<String> willRemoved = new ArrayList<String>();
			while (true) {

				for (String ipAddr : willRemoved) {
					System.out.println("-------TTL 발동 ------- " + ipAddr + " 삭제했음");
					my_cache_Table.remove(ipAddr);
					willRemoved.remove(ipAddr); // new
					break; // new
				}

				try {
					for (String ipAddr : my_cache_Itr) {
						_Cache_Entry cacheEntry = my_cache_Table.get(ipAddr);
						cacheEntry.cache_ttl--;
						if (cacheEntry.cache_ttl < 1) {
							willRemoved.add(ipAddr);
						}
					}
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean run_Clean_Thread(Map<String, _Cache_Entry> table, Set<String> cacheIterator) {
		tableCleanThread cleanThread = new tableCleanThread(table, cacheIterator);
		Thread thread = new Thread(cleanThread, "TableCleanthread");
		thread.start();
		return true;
	}

	public byte[] swappingAddr(byte[] input) {
		byte[] tempMACAddr = new byte[6];
		byte[] tempProtoAddr = new byte[4];
		int macI, ipI;

		// temp에 src를 저장
		for (macI = 0; macI < 6; macI++)
			tempMACAddr[macI] = input[8 + macI];
		for (ipI = 0; ipI < 4; ipI++)
			tempProtoAddr[ipI] = input[14 + ipI];
		// src에 dst를 저장
		for (macI = 0; macI < 6; macI++)
			input[8 + macI] = input[18 + macI];
		for (ipI = 0; ipI < 4; ipI++)
			input[14 + ipI] = input[24 + ipI];
		// dst에 src를 저장
		for (macI = 0; macI < 6; macI++)
			input[18 + macI] = tempMACAddr[macI];
		for (ipI = 0; ipI < 4; ipI++)
			input[24 + ipI] = tempProtoAddr[ipI];

		return input;
	}

	// ARP HEADER의 dst_addr을 byte[] -> String으로 변환 ex) xxx.xxx.xxx.xxx
	public String getDstAddrFromHeader(byte[] input) {
		byte[] bytes = new byte[4];

		String dst_Addr = "";
		System.arraycopy(input, 24, bytes, 0, 4);
		for (byte b : bytes) {
			dst_Addr += Integer.toString(b & 0xFF) + ".";
		}
		return dst_Addr.substring(0, dst_Addr.length() - 1);
	}

	public String getDstAddrFromSwappedHeader(byte[] input) {
		byte[] bytes = new byte[4];

		String dst_Addr = "";
		System.arraycopy(input, 14, bytes, 0, 4);
		for (byte b : bytes) {
			dst_Addr += Integer.toString(b & 0xFF) + ".";
		}
		return dst_Addr.substring(0, dst_Addr.length() - 1);
	}

	private boolean isRequest(byte[] input) {
		for (int i = 0; i < 2; i++) {
			if (OP_ARP_REQUEST[i] == input[i + 6])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	private boolean isReply(byte[] input) {
		for (int i = 0; i < 2; i++) {
			if (OP_ARP_REPLY[i] == input[i + 6])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	// target IP address가 나의 ip주소는 아니지만,
	// 내 proxy_Table에 target ip address가 있을 때 proxy arp
	// 10/25 수정: 캐시테이블 대신 프록시 테이블 내부를 확인하도록 변경했습니다.
	private boolean isProxyARP(byte[] input) {
		// 나의 IP를 가져온다
		_IP_ADDR myIp = new _IP_ADDR();
		for (int i = 0; i < 4; i++) {
			myIp.addr[i] = MY_IP_ADDRESS.addr[i];
		}

		_IP_ADDR target = new _IP_ADDR();
		for (int i = 0; i < 4; i++) {
			target.addr[i] = input[i + 24];
		} // ※ target.addr -> target.addr.toString()

		System.out.println("ip가 다른가:" + Arrays.equals(myIp.addr, target.addr));
		System.out.println("프록시 테이블에 있는가:" + proxy_Table.containsKey(target.toString()));
		System.out.println("ip tostring:" + target.toString());

		if (!Arrays.equals(myIp.addr, target.addr) && proxy_Table.containsKey(target.toString()) == true) {
			return true;
		} else
			return false;
	}

	private boolean isGratuitousARP(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (input[i + 14] == input[i + 24])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	private boolean IsItMine(byte[] input) {
		for (int i = 0; i < 4; i++) {
			if (MY_IP_ADDRESS.addr[i] == input[i + 24])
				continue;// 내 IP 주소 = destProtoAddr인지 탐색
			else {
				return false;
			}
		}
		return true;
	}

	public void updateCache(byte[] input) {
		// ARP Reply수신 시 swap된 패킷이 전송되기 때문에, 기존 24를 참조하던 코드를 14로 변경.
		if (cache_Table.containsKey(getDstAddrFromSwappedHeader(input))) {// ip주소가 테이블에 존재하는 경우 == ARP reply 수신 시
			_Cache_Entry tableToUpdate = cache_Table.get(getDstAddrFromSwappedHeader(input));
			System.arraycopy(input, 8, tableToUpdate.cache_ethaddr, 0, 6);// cache table 내부 이더넷 주소 update
			tableToUpdate.cache_status = "Complete";// 테이블 내부 상태 변경
			tableToUpdate.cache_ttl = 80; // complete인 경우 ttl = 20
			// Request Message를 수신한 PC에서는 dst가 아닌 패킷 Src(14)의 주소를 테이블에 put해야 한다
		} else if (!cache_Table.containsKey(getDstAddrFromHeader(input))) { // ip주소가 테이블에 존재하지 않는 경우 == ARP request 수신 시
			String request_ip_string = getDstAddrFromSwappedHeader(input);
			byte[] request_ether_addr = new byte[6];
			System.arraycopy(input, 8, request_ether_addr, 0, 6);
			_Cache_Entry request_cache_Entry = new _Cache_Entry(request_ether_addr, "Complete", 80);// complete인 경우 ttl
																									// = 20

			cache_Table.put(request_ip_string, request_cache_Entry);
		}
	}

	// opcode getter & setter
	public byte[] getOpcode() {
		return this.m_aHeader.arp_op;
	}

	public void setOpCode(int value) {
		this.m_aHeader.arp_op = byte4To2(intToByte(value));
	}

	// SrcMAC getter & setter
	public _ETHERNET_ADDR getSrcMAC() {
		return this.m_aHeader.arp_srcHdAddr;
	}

	public void setSrcMAC(byte[] srcMAC) {
		for (int i = 0; i < srcMAC.length; i++) {
			this.m_aHeader.arp_srcHdAddr.addr[i] = srcMAC[i];
		}
	}

	// SrcIPAddr getter & setter
	public _IP_ADDR getSrcIPAddr() {
		return this.m_aHeader.arp_srcProtoAddr;
	}

	public void setSrcIPAddr(byte[] srcAddr) {
		for (int i = 0; i < srcAddr.length; i++) {
			this.m_aHeader.arp_srcProtoAddr.addr[i] = srcAddr[i];
		}
	}

	// DstMAC getter & setter
	public _ETHERNET_ADDR getDstMAC() {
		return this.m_aHeader.arp_destHdAddr;
	}

	public void setDstMAC(byte[] dstMAC) {
		for (int i = 0; i < dstMAC.length; i++) {
			this.m_aHeader.arp_destHdAddr.addr[i] = dstMAC[i];
		}
	}

	// DstIpAddr getter & setter
	public _IP_ADDR getDstIpAddr() {
		return this.m_aHeader.arp_destProtoAddr;
	}

	public void setDstIPAddr(byte[] dstAddr) {
		for (int i = 0; i < dstAddr.length; i++) {
			this.m_aHeader.arp_destProtoAddr.addr[i] = dstAddr[i];
		}
	}

	// 10/25 수정: 4바이트 배열 입력 시 1.1.1.1꼴의 ip 형식 스트링 리턴
	private String ipByteToString(byte[] input) {
		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			temp.append((int) input[i]);
			temp.append(".");
		}
		temp.deleteCharAt(temp.length() - 1);
		return temp.toString();
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
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
