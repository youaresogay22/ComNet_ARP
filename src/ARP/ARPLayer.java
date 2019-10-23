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

public class ARPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public Map<String, _Cache_Entry> cache_Table =new HashMap<String, _Cache_Entry>();
	public Map<String, _Proxy_Entry> proxy_Table = new HashMap<String, _Proxy_Entry>();
	public Set<String> cache_Itr = cache_Table.keySet();
	private final byte[] OP_ARP_REQUEST = byte4To2(intToByte(1));
	private final byte[] OP_ARP_REPLY = byte4To2(intToByte(2));
	private final byte[] TYPE_ARP = byte4To2(intToByte(0x0806));
	private final byte[] PROTOCOL_TYPE_IP = byte4To2(intToByte(0x0800));
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
	
	public class _Proxy_Entry{
		byte[] proxy_ethaddr;
		String proxy_device;
		
		public _Proxy_Entry(byte[] ethaddr, String device) {
			proxy_ethaddr = ethaddr;
			proxy_device = device;
		}
	}

	private class _IP_ADDR {
		private byte[] addr = new byte[4];

		public _IP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
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
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		getMyPCAddr();
		ResetHeader();
		run_Clean_Thread(cache_Table, cache_Itr);
		// 캐시테이블 자동 제거 스레드
	}

	public void ResetHeader() {
		for (int i = 0; i < 5; i++) {
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
		if(!proxy_Table.containsKey(key)) 
			proxy_Table.put(key,new _Proxy_Entry(ethaddr,device));
		System.out.println("ProxyTable = " + proxy_Table);			//디버깅
	}
	public boolean isProxyTableEmpty() {
		if(proxy_Table.size() == 0)
			return true;
		return false;
	}

	public boolean Send(byte[] input, int length) {
		setARPHeaderBeforeSend();   		// opCode를 포함한 hdtype,prototype,hdLen,protoLen 초기화. opCode의 default는 1이다.
		setSrcMAC(MY_MAC_ADDRESS.addr);
		setSrcIPAddr(MY_IP_ADDRESS.addr);
		byte[] ARP_header_added_bytes = ObjToByte(m_aHeader, input, length);
		
		if (isTargetHdAddrQuestion(ARP_header_added_bytes)) {	// Target's hardware addr이 ???이면, IP에서 내려온 send. -> send request message.
			String target_IP = getDstAddrFromHeader(ARP_header_added_bytes);
			_Cache_Entry cache_Entry = 									 // dst_Addr를 KEY로 갖고 cache_Entry를 VALUE로 갖는 hashMap 생성
					new _Cache_Entry(new byte[6], "Incomplete", 10);
			
			//Map의 put 메소드는 기본적으로 put의 인자로 전달받은 key(new)가 이전의 key(old)를 replace하게끔 구현되어 있다.
			//따라서 1.1.1.1을 Map에 put한 다음에, 1.1.1.1이 ttl로 삭제되기 이전에 1.1.1.1을 다시 Map에 put하면은, ttl이 초기화된다.
			//아래 if문은 new key가 old key를 대체하지 않게끔 중복체크하여 ttl이 초기화되는 일을 방지함.
			if(!cache_Table.containsKey(target_IP))
				cache_Table.put(target_IP, cache_Entry);

			System.out.println("Send MAP == " + target_IP);	// 디버깅
			
			this.GetUnderLayer().Send(ARP_header_added_bytes, ARP_header_added_bytes.length);	//Send Request Message
		}else {  //Target's hadrware addr이 xx:xx이면, Receive에서 온 Send. Receive에서 UpdateCache를 통해 Complete된 목록이 있음. -> Send Reply Message
			if(AreMyPcIPAndPacketIPtheSame(input)) { 				//내 PC의 IP == 패킷의 target IP,
				for(int i=0; i<6; i++)
					input[i+18] = MY_MAC_ADDRESS.addr[i];					//패킷의 ???(target MAC)를 내 PC의 MAC 주소로 갱신
				ARP_header_added_bytes = swappingAddr(input);	//src 주소 <-> target 주소 swapping
				setOpCode(2); 											//setOpcode(2) to reply
				this.GetUnderLayer().Send(ARP_header_added_bytes, ARP_header_added_bytes.length);	//Send Reply Message
			}else {		//내 PC의 IP 주소 != 패킷의 target IP, 
						//DROP. Do not send reply packet. do nothing. cache table update only -> (from Receive) PT03 23page
			}
		}
		return false;
	}
	
	public boolean isTargetHdAddrQuestion(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (input[18 + i] == 0)
				return true;
			else
				return false;
		}
		return false;
	}
	
	// 내 PC의 IP주소와 Packet의 target IP가 같은지 확인한다
	public boolean AreMyPcIPAndPacketIPtheSame(byte[] input) {
		for(int i=0; i<4; i++) {
			if(MY_IP_ADDRESS.addr[i] == input[i+24])
				continue;
			else
				return false;
		}
		return true;
	}
	
	public boolean Receive(byte[] input) {
		boolean Mine = IsItMine(input);

		if (isRequest(input)) {// ARP request 인 경우
			if (isProxyARP(input)) {// proxy ARP request 인 경우
				if (Mine) {// then proxy send
					proxyRQReceive(input, input.length);
					updateCache(input);
					proxyRPSend(input, input.length);
					return true;
				} else
					return false;

			} else if (isGratuitousARP(input)) {// Gratuitous ARP request  인 경우
				if (Mine) {// then Gratuitous send
					// do nothing, cache table update
					updateCache(input);
					return true;
				} else
					return false;

			} else {// basic ARP request 인 경우
				if (Mine) {
					// then basic send
					Send("".getBytes(), 0);
					return true;
				} else
					return false;
			}
		} else if (isReply(input)) {// ARP reply 인 경우

			if (isProxyARP(input)) {// proxy ARP reply 인 경우
				if (Mine) {// then proxy send
					updateCache(input);
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
					Send("".getBytes(), 0);
					return true;
				} else
					return false;
			}
		} else
			return false;
	}

	   // Grat Send
	   public boolean Grat_Send(byte[] input, int length) {
	      // ARP헤더 초기 세팅
	      setARPHeaderBeforeSend();
	      // Sender's hardware address를 세팅
	      setSrcMAC(MY_MAC_ADDRESS.addr); // 자기 MAC주소 가져와서 넣기
	      // Sender's protocol address를 세팅
	      setSrcIPAddr(MY_IP_ADDRESS.addr); //자기 IP주소 가져와서 넣기
	      // Target's protocol address를 세팅
	      setDstIPAddr(MY_IP_ADDRESS.addr); //자기 IP주소 가져와서 넣기

	      // Gratuitous ARP Message 생성
	      byte[] grat_message = ObjToByte(m_aHeader, input, length); // ARPMessage

	      // Gratuitous ARP Message 내려보내기
	      this.GetUnderLayer().Send(grat_message, grat_message.length);

	      return true;
	   }
	   
	// 각 send 함수에서 header를 세팅해서 ObjToByte로 보내주기 때문에 
    // ObjToByte는 이미 set 되어 있는 헤더의 필드 값을 넣어주기만 한다.
    // ∴header setting은 각 send 함수에서 해주어야 함.
	public boolean proxyRQSend(byte[] input, int length) {
		// 1.ip주소를 cache table에 추가하는 과정
		// 2. arp message 작성
		// setHdtype(1);
		// setProtoType(0x800);
		// setLengthOfHdAddr(6);
		// setLengthOfProtoAddr(4);
		setOpCode(1); // request
		setSrcMAC(MY_MAC_ADDRESS.addr); // // 자기 맥주소 가져와서 넣기
		setSrcIPAddr(MY_IP_ADDRESS.addr); //자기 ip주소 가져와서 넣기
		// setDstMac에 00:00:00:00:00:00 넣기
		_ETHERNET_ADDR dstMac = new _ETHERNET_ADDR();
		for (int i = 0; i < 6; i++) {
			dstMac.addr[i] = (byte) 0x00;
		}
		setDstMAC(dstMac.addr);
		// setDstIPAddr();  ui로 입력받은 ip주소 넣기
		_IP_ADDR dstIp = new _IP_ADDR();
		for (int i = 0; i < 4; i++) {
			dstIp.addr[i] = input[24 + i];
		}
		setDstIPAddr(dstIp.addr);
		byte[] bytes = ObjToByte(m_aHeader, input, length);
		// 3.ethernet으로 보낸다.
		this.GetUnderLayer().Send(bytes, length + 28);
		return true;
	}

	// input은 데이터, length는 데이터 length
	public boolean proxyRPSend(byte[] input, int length) {
		// 1. arp message의 target protocol address가 proxy entry에 있는지 이미 proxyRQReceive에서 확인함.
		// 2. arp message 작성
		// setHdtype(1);
		// setProtoType(0x800);
		// setLengthOfHdAddr(6);
		// setLengthOfProtoAddr(4);
		// target protocol address를 가져온다.
		_IP_ADDR target = new _IP_ADDR();
		for (int i = 0; i < 4; i++) {
			target.addr[i] = input[i + 24];
		}
		// cache table에서 찾기 위해 target protocol address를 string으로 변환
		String tIpAddr = target.toString();
		// 찾은 ip주소로 target hardware address를 가져온다.
		// mac에는 target hardware address가 들어있다.
		byte[] mac = cache_Table.get(tIpAddr).cache_ethaddr;
		// header의 target hardware address에 알아낸 맥주소를 넣는다.
		setDstMAC(mac);
		// 주소 swapping
		_IP_ADDR tempIP = m_aHeader.arp_srcProtoAddr;
		_ETHERNET_ADDR tempMAC = m_aHeader.arp_srcHdAddr;
		m_aHeader.arp_srcProtoAddr = m_aHeader.arp_destProtoAddr;
		m_aHeader.arp_srcHdAddr = m_aHeader.arp_destHdAddr;
		m_aHeader.arp_destProtoAddr = tempIP;
		m_aHeader.arp_destHdAddr = tempMAC;
		// opcode를 reply(2)로 변경
		setOpCode(2);
		byte[] bytes = ObjToByte(m_aHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 28);
		return true;
	}

	public boolean proxyRQReceive(byte[] input, int length) {
		// 1. arp cache table 업데이트
		_Cache_Entry newEntry = new _Cache_Entry(m_aHeader.arp_srcHdAddr.addr, "Complete", 12);
		cache_Table.put(m_aHeader.arp_srcProtoAddr.toString(), newEntry);
		// 2. target protocol address가 cache table에 있는지 확인
		// 헤더의 target protocol address를 가져온다.
		_IP_ADDR target =new _IP_ADDR();
		for(int i = 0; i < 4; i++) {
			target.addr[i] = input[i+24];
		}
		// cache table에서 찾기 위해 target protocol address를 string으로 변환
		String tIpAddr = target.toString();
		// cache_Table에 target proto addr이 존재하면 proxy reply send
		if (cache_Table.containsKey(tIpAddr) == true) {
			proxyRPSend(input, length);
			return true;
		}
		// cache_Table에 target proto addr이 존재하지 않으면 message drop
		return false;
	}

	public boolean proxyRPReceive(byte[] input) {
		// 헤더에서 target ip, target mac 가져오기
		_IP_ADDR targetIP = getDstIpAddr();
		_ETHERNET_ADDR targetMAC = getDstMAC();
		// 1. 내게 온 ARP RP message가 맞는지 확인
		// 나의 ip주소와 target ip가 일치하는지 확인, 나의 mac주소와 target mac주소가 일치하는지 확인
		if (Arrays.equals(MY_IP_ADDRESS.addr, targetIP.addr) && Arrays.equals(MY_MAC_ADDRESS.addr, targetMAC.addr)) {
			// 2. sender protocol address가 내 cache_Table에 있는지 확인
			_IP_ADDR senderIP = getSrcIPAddr();
			String srcIpAddr = senderIP.toString();
			_ETHERNET_ADDR senderMAC = getSrcMAC();
			// 내 cache_Table에 있다면 내 cache_table에 sender hd address 업데이트
			if (cache_Table.containsKey(srcIpAddr) == true) {
				cache_Table.replace(srcIpAddr, new _Cache_Entry(senderMAC.addr, "Complete", 10));
			}
			return true;
		}
		return false;
	}
	
	
	// ★ Q. 나의 mac주소와 ip주소를 받아오는 함수로 보입니다.
	// 코드 25-26줄의 
	// public final _IP_ADDR MY_IP_ADDRESS = new _IP_ADDR();
	// public final _ETHERNET_ADDR MY_MAC_ADDRESS = new _ETHERNET_ADDR();
	// 내 ip주소와 mac주소가 자주 쓰이니까 처음에 MY_IP_ADDRESS, MY_MAC_ADDRESS 변수에 각각 담아놓고 가져다 쓰는 건 어떻게 생각하시나요?  
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

	// ★ intToByte, byte4To2 EhternetLayer에서 import.(구글로 못찾겠어서 주석 남겨요 ㅠㅠ)
	// A. import static ARP.EthernetLayer.byte4To2; 꼴로 import 하시면 됩니다. 
	// import 해두었습니다.

	// Hadrware type =1
	// Protocol type = 0x0800
	// Length of hardware address = 6
	// Length of protool address = 4
	// OP Code = 1
	// Seder's hardware addr = GUI에서 Send버튼을 눌렀을 때 설정
	// Sender's protocol addr = GUI에서 Send버튼을 눌렀을 때 설정
	// Target's hardware addr = ??? (000)
	// Target's protocol addr = GUI에서 Send버튼을 눌렀을 때 설정
	
	// ★ GUI에서 send버튼을 누를 때  ARPLayer의 헤더를 세팅하는 것으로 이해되는데
	// 개인적으로 application layer에서 arp layer의 필드를 직접적으로 건들이는게 계층 구조를 무너뜨리는 것 같은 느낌이 듭니다. 
	// 아니면 구현의 편의를 위해 어쩔 수 없이 쓰고 계시는 건가요?
	public void setARPHeaderBeforeSend() {
		this.m_aHeader.arp_hdType[0] = 1;
		this.m_aHeader.arp_prototype[0] = (byte) 0x0800;
		this.m_aHeader.arp_hdLength = 6;
		this.m_aHeader.arp_protoLength = 4;
		this.m_aHeader.arp_op[0] = 1;
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
					System.out.println("-------TTL 발동 ------- " + ipAddr +" 삭제했음");
					my_cache_Table.remove(ipAddr);
					willRemoved.remove(ipAddr);		//new
					break;							//new
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
					// TODO Auto-generated catch block
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
	
	// setRequest, setReply 사용하지 않아서 삭제 했습니다.

	public byte[] swappingAddr(byte[] input) {
		byte[] tempMACAddr = new byte[6];
		byte[] tempProtoAddr = new byte[6];

		// temp에 src주소 저장
		for (int i = 0; i < 6; i++) {
			tempMACAddr[i] = input[8 + i];
			tempProtoAddr[i] = input[14 + i];
		}
		for (int j = 0; j < 6; j++) {
			// src에 dst를 저장
			input[8 + j] = input[18 + j];
			input[14 + j] = input[24 + j];
			// dst에 src를 저장
			input[18 + j] = tempMACAddr[j];
			input[24 + j] = tempProtoAddr[j];
		}
		return input;
	}

	// public String extractIPString(byte[] input)와 겹칩니다.
	// getDstAddrFromHeade send함수 안에서 1회 사용됨
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
	// 내 cache_Table에 target ip address가 있을 때 proxy arp
	private boolean isProxyARP(byte[] input) {
		// 나의 IP를 가져온다
		_IP_ADDR myIp = new _IP_ADDR();
		_IP_ADDR target = new _IP_ADDR();
		for (int i = 0; i < 6; i++) {
			target.addr[i] = input[i + 24];
		}
		if (!Arrays.equals(myIp.addr, target.addr) && cache_Table.containsKey(target.addr) == true) {
			return true;
		}
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

	
	// ★ Q.제가 이해한게 맞다면 이 함수가 receive안에서 쓰이던데
	// 나에게 온 arp message인지 아닌지 확인하는 역할을 하는 것으로 보여서요. 그렇담 dstIpAddr을 확인해야하는게 아닐까요?
	// A. 둘다 확인하도록 변경했습니다.
	private boolean IsItMine(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (MY_MAC_ADDRESS.addr[i] == input[i + 18])
				continue;//내 맥 주소 = destHdAddr인지 탐색
			else {
				return false;
			}
		}
		for (int i = 0; i < 4; i++) {
			if (MY_IP_ADDRESS.addr[i] == input[i + 24])
				continue;//내 IP 주소 = destProtoAddr인지 탐색
			else {
				return false;
			}
		}
		return true;
	}
	
	//extractIPString: 삭제했습니다.
	
	// ★ updateCache내에서 쓰이는  extractIPString함수를 보면 
	// input의 12번째부터 가져오시는데 IP layer 헤더에 있는 ip_src부분을 가져오는 것으로 보입니다. 
	// 그렇다면 ethernet layer로부터 receive한 경우에는 input의 자료구조가 다르므로 (receive한 경우 protoAddr의 위치가 12가 아니어서)
	// 이 updateCache 함수는 ip layer에서 request를 받은 경우에만 사용할 용도로 만드신거로 이해하면 될까요? 
	// A. reply 수신 시 동작도 구현하였습니다.
	public void updateCache(byte[] input) {
		if (cache_Table.containsKey(getDstAddrFromHeader(input))) {//ip주소가 테이블에 존재하는 경우 == ARP reply 수신 시
			byte[] tableEtherAddr = cache_Table.get(getDstAddrFromHeader(input)).cache_ethaddr;
			System.arraycopy(input, 8, tableEtherAddr, 0, 6);// cache table 내부 이더넷 주소만 update
		} else { //ip주소가 테이블에 존재하지 않는 경우 == ARP request 수신 시
			String request_ip_string = getDstAddrFromHeader(input);
			byte[] request_ether_addr = new byte[6];
			System.arraycopy(input, 8, request_ether_addr, 0, 6);
			_Cache_Entry request_cache_Entry = new _Cache_Entry(new byte[6], "Complete", 10);
			
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
