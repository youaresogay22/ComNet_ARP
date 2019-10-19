package ARP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static ARP.EthernetLayer.intToByte;
import static ARP.EthernetLayer.byte4To2;;

public class ARPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	public Map<String, _Cache_Entry> cache_Table = Collections.synchronizedMap(new HashMap<String, _Cache_Entry>());
	public Set<String> cache_Itr = cache_Table.keySet();
	private final int OP_ARP_REQUEST = 1;
	private final int OP_ARP_REPLY = 2;
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
			this.arp_hdLength = (byte) 0x00;
			this.arp_protoLength = (byte) 0x00;
			this.arp_op = new byte[2];
			this.arp_srcHdAddr = new _ETHERNET_ADDR();
			this.arp_srcProtoAddr = new _IP_ADDR();
			this.arp_destHdAddr = new _ETHERNET_ADDR();
			this.arp_destProtoAddr = new _IP_ADDR();
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
	
	public void setSrcAddr(byte[] srcAddr) {
		for(int i=0; i<srcAddr.length; i++) {
			this.m_aHeader.arp_srcProtoAddr.addr[i] = srcAddr[i];
		}
	}
	
	public void setSrcMAC(byte[] srcMAC) {
		for(int i=0; i<srcMAC.length; i++) {
			this.m_aHeader.arp_srcHdAddr.addr[i] = srcMAC[i];
		}
	}

	public byte[] ObjToByte(_ARP_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 28];

		System.arraycopy(Header.arp_hdType, 0, buf, 0, 2); // 2byte
		System.arraycopy(Header.arp_prototype, 0, buf, 2, 2); // 2byte
		buf[4] = Header.arp_hdLength; // 1byte
		buf[5] = Header.arp_protoLength; // 1byte
		System.arraycopy(Header.arp_op, 0, buf, 6, 2); // 2byte
		System.arraycopy(Header.arp_srcHdAddr, 0, buf, 8, 6); // 6byte
		System.arraycopy(Header.arp_srcProtoAddr, 0, buf, 14, 4); // 4byte
		System.arraycopy(Header.arp_destHdAddr, 0, buf, 18, 6); // 6byte
		System.arraycopy(Header.arp_destProtoAddr, 0, buf, 24, 4); // 4byte

		for (int i = 0; i < length; i++)
			buf[28 + i] = input[i];

		return buf;
	}

	// ARPLayer에서 만든 cache_Table을 다른 Layer에서도 사용할 수 있게끔 해주는 기능.
	public Map<String, _Cache_Entry> getCacheList() {
		return cache_Table;
	}

	public synchronized boolean Send(byte[] input, int length) {
		byte[] bytes = new byte[4];

		// IP HEADER의 src_addr을 byte[] -> String으로 변환 ex) -124,112,113,-35 ->
		// 132.112.113.221
		String ar = "";
		System.arraycopy(input, 12, bytes, 0, 4);
		for (byte b : bytes) {
			ar += Integer.toString(b & 0xFF) + ".";
		}
		ar = ar.substring(0, ar.length() - 1);

		// src_addr를 KEY로 갖고 cache_Entry를 VALUE로 갖는 hashMap 생성
		_Cache_Entry cache_Entry = new _Cache_Entry(new byte[6], "Incomplete", 10);
		cache_Table.put(ar, cache_Entry);

		System.out.println("MAP == " + cache_Table);
		System.out.println("SIZE == " + cache_Table.size());
		return false;
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
			while (true) {
				try {
					for (String ipAddr : my_cache_Itr) {
						_Cache_Entry cacheEntry = my_cache_Table.get(ipAddr);
						if (cacheEntry.cache_ttl < 1) {
							my_cache_Table.remove(ipAddr);
						}
						cacheEntry.cache_ttl--;
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public boolean run_Clean_Thread(Map<String, _Cache_Entry> table, Set<String> cacheIterator) {
		tableCleanThread cleanThread = new tableCleanThread(table, cacheIterator);
		Thread thread = new Thread(cleanThread, "Cleaner");
		thread.start();
		return true;
	}

	public boolean setRequest() {
		return false;
	}

	public boolean setReply() {
		return false;
	}

	private boolean isRequest() {
		return false;
	}

	private boolean isReply() {
		return false;
	}

	private boolean isBasicARP() {
		return false;
	}

	private boolean isProxyARP() {
		return false;
	}

	private boolean isGratuitousARP() {
		return false;
	}

	private boolean IsItMine(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (m_aHeader.arp_destHdAddr.addr[i] == input[i])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	public boolean Receive(byte[] input) {
		byte[] data;
		boolean Mine, Broadcast;
		
		if (Broadcast) {


		} else if () {
			
		} else() {
			
		}

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
