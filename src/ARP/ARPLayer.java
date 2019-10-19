package ARP;

import java.util.ArrayList;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static ARP.EthernetLayer.intToByte;
import static ARP.EthernetLayer.byte4To2;;

public class ARPLayer implements BaseLayer {
	
	   public void setSrcIPAddr(byte[] srcAddr) {
		      for (int i = 0; i < srcAddr.length; i++) {
		         this.m_aHeader.arp_srcProtoAddr.addr[i] = srcAddr[i];
		      }
		   }

		   public void setSrcMAC(byte[] srcMAC) {
		      for (int i = 0; i < srcMAC.length; i++) {
		         this.m_aHeader.arp_srcHdAddr.addr[i] = srcMAC[i];
		      }
		   }
		   
		   public void setDstIPAddr(byte[] dstAddr) {
		      for (int i = 0; i < dstAddr.length; i++) {
		         this.m_aHeader.arp_destProtoAddr.addr[i] = dstAddr[i];
		      }
		   }
		   
		   public void setDstMAC(byte[] dstMAC) {
		      for (int i = 0; i < dstMAC.length; i++) {
		         this.m_aHeader.arp_destHdAddr.addr[i] = dstMAC[i];
		      }
		   }
		   
		   public _ETHERNET_ADDR getDstMAC() {
		      return this.m_aHeader.arp_destHdAddr;
		   }
		   
		   public _ETHERNET_ADDR getSrcMAC() {
		      return this.m_aHeader.arp_srcHdAddr;
		   }
		   
		   public _IP_ADDR getScrIPAddr() {
		      return this.m_aHeader.arp_srcProtoAddr;
		   }
		   
		   public _IP_ADDR getDstIpAddr() {
		      return this.m_aHeader.arp_destProtoAddr;
		   }
		   
		   public void setOpCode(int value) {
		      this.m_aHeader.arp_op = byte4To2(intToByte(value));
		   }
		   
		   public byte[] getOpcode() {
		      return this.m_aHeader.arp_op;
		   }
		   
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public Map<String, _Cache_Entry> cache_Table = Collections.synchronizedMap(new HashMap<String, _Cache_Entry>());
	public Set<String> cache_Itr = cache_Table.keySet();
	private final byte[] OP_ARP_REQUEST = byte4To2(intToByte(1));
	private final byte[] OP_ARP_REPLY = byte4To2(intToByte(2));
	private final byte[] TYPE_ARP = byte4To2(intToByte(0x0806));
	private final byte[] PROTOCOL_TYPE_IP = byte4To2(intToByte(0x0800));

	public class _Cache_Entry {
		byte[] cache_ethaddr;
		String cache_status;
		int cache_ttl; // time to live

		public _Cache_Entry(byte[] ethaddr, String status, int ttl) {
			cache_ethaddr = ethaddr;
			cache_status = status;
			cache_ttl = ttl;
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

	public ARPLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
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

	//Dlg에서 Send버튼을 눌렀을 때 srcAddr,srcMAC,dstAddr을 받아와 Set.
	public void setSrcProtoAddr(byte[] srcProtoAddr) {
		for (int i = 0; i < srcProtoAddr.length; i++) 
			this.m_aHeader.arp_srcProtoAddr.addr[i] = srcProtoAddr[i];
	}
	public void setSrcHdAddr(byte[] srcHdAddr) {
		for (int i = 0; i < srcHdAddr.length; i++) 
			this.m_aHeader.arp_srcHdAddr.addr[i] = srcHdAddr[i];
	}
	
	public void setDestProtoAddr(byte[] destProtoAddr) {
		for(int i=0; i< destProtoAddr.length; i++)
			this.m_aHeader.arp_destProtoAddr.addr[i] = destProtoAddr[i];
	}
	
	public void setOPCode(byte i) {
		this.m_aHeader.arp_op[0] = i;
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
		buf[19] = Header.arp_destHdAddr.addr[0];
		buf[20] = Header.arp_destHdAddr.addr[0];
		buf[21] = Header.arp_destHdAddr.addr[0];
		buf[22] = Header.arp_destHdAddr.addr[0];
		buf[23] = Header.arp_destHdAddr.addr[0];
		
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

	public synchronized boolean Send(byte[] input, int length) {
		String dst_Addr = getDstAddrFromHeader(input, length);	//IP Header의 dst_Addr 부분을 String으로 변환.
		
		// dst_Addr를 KEY로 갖고 cache_Entry를 VALUE로 갖는 hashMap 생성
		_Cache_Entry cache_Entry = new _Cache_Entry(new byte[6], "Incomplete", 10);
		cache_Table.put(dst_Addr, cache_Entry);
		
		System.out.println("MAP == " + cache_Table);
		System.out.println("SIZE == " + cache_Table.size());
		
		byte[] ARP_header_added_bytes = ObjToByte(m_aHeader, input, length);
		
			//if Request. 03 12page
			
			//if Reply. 03 28page
			//op코드를 2로 바꾸고
			//target의 MAC주소를 확보한 상태
			
		this.GetUnderLayer().Send(ARP_header_added_bytes, ARP_header_added_bytes.length);
		return false;
	}
	
	//Grat Send
    public boolean Grat_Send(byte[] input, int length) {
        
        // 변경할 MAC주소 받기, Dlg에서 가져오기
        byte[] mac_bytes = new byte[6];
        setSrcMAC(mac_bytes);
        
        // Target's protocol address 변경, EthernetLayer에서 가져오기
        byte[] tp_bytes = new byte[6];
        setDestProtoAddr(tp_bytes);
        
        ObjToByte(m_aHeader, null, length); // ARPMessage
        
        // 내려보내기
        this.GetUnderLayer().Send();
        
        return true;
     }
	
	// IP HEADER의 dst_addr을 byte[] -> String으로 변환 ex) xxx.xxx.xxx.xxx
	public String getDstAddrFromHeader(byte[] input, int length) {
		byte[] bytes = new byte[4];
		
		String dst_Addr = "";
		System.arraycopy(input, 12, bytes, 0, 4);
		for (byte b : bytes) {
			dst_Addr += Integer.toString(b & 0xFF) + ".";
		}
		return dst_Addr.substring(0, dst_Addr.length() - 1);
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
					my_cache_Table.remove(ipAddr);
				}

				try {
					for (String ipAddr : my_cache_Itr) {
						_Cache_Entry cacheEntry = my_cache_Table.get(ipAddr);
						cacheEntry.cache_ttl--;
						if (cacheEntry.cache_ttl < 0) {
							willRemoved.add(ipAddr);
						}
					}
					Thread.sleep(1000);
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

	public boolean setRequest() {
		return false;
	}

	public boolean setReply() {
		return false;
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

	private boolean isProxyARP(byte[] input) {
		return false;
	}

	private boolean isGratuitousARP(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (input[i+14] == input[i + 24])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	private boolean IsItMine(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (m_aHeader.arp_srcHdAddr.addr[i] == input[i + 18])
				continue;
			else {
				return false;
			}
		}
		return true;
	}
	public String extractIPString(byte[] input) {//ARP 데이터구조에서 IP string을 추출
		byte[] bytes = new byte[4];
		String ar = "";
		System.arraycopy(input, 12, bytes, 0, 4);
		for (byte b : bytes) {
			ar += Integer.toString(b & 0xFF) + ".";
		}
		ar = ar.substring(0, ar.length() - 1);
		return ar;
	}

	public boolean Receive(byte[] input) {
		byte[] data;
		boolean Mine = IsItMine(input);

		if (isRequest(input)) {// ARP request 인 경우
			if (isProxyARP(input)) {// proxy ARP request 인 경우
				if (Mine) {
					// then proxy send
					return true;
				} else
					return false;
			} else if (isGratuitousARP(input)) {// Gratuitous ARP request 인 경우
				if (Mine) {
					// then Gratuitous send
					return true;
				} else
					return false;
			} else {// basic ARP request 인 경우
				if (Mine) {
					// then basic send
					return true;
				} else
					return false;
			}
		}

		else if (isReply(input)) {// ARP reply 인 경우
			byte[] tableEtherAddr = cache_Table.get(extractIPString(input)).cache_ethaddr;
			System.arraycopy(input, 0, tableEtherAddr, 0, 6);//cache table update 실행
			
			if (isProxyARP(input)) {// proxy ARP reply 인 경우
				if (Mine) {
					// then proxy send
					return true;
				} else
					return false;
			} else if (isGratuitousARP(input)) {// Gratuitous ARP reply 인 경우
				if (Mine) {
					// then Gratuitous send
					return true;
				} else
					return false;
			} else {// basic ARP reply 인 경우
				if (Mine) {
					// then basic send
					return true;
				} else
					return false;
			}
		}

		else
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
