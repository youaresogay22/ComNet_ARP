package ARP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import ARP.IPLayer._IP_ADDR;
import ARP.EthernetLayer;

public class ARPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	private static LayerManager m_LayerMgr = new LayerManager();
	public Map<String, _Cache_Entry> cache_Table = Collections.synchronizedMap(new HashMap<String, _Cache_Entry>());
	public Set<String> cache_Itr = cache_Table.keySet(); 
	
	public class _Cache_Entry {
		byte[] cache_ethaddr;
		String cache_status;
		int cache_ttl;			// time to live
		
		public _Cache_Entry(byte[] ethaddr, String status, int ttl) {
			cache_ethaddr = ethaddr;
			cache_status = status;
			cache_ttl = ttl;
		}
	}

	private class _ARP_HEADER {
		byte[] arp_hdType;		// Ethernet = 1
		byte[] arp_prototype;	// Ipv4 = 0x0800
		byte arp_hdLength;		// MAC address = 6
		byte arp_protoLength;	// IP address = 4
		byte[] arp_op;			// request = 1, reply = 2
		byte[] arp_srcHdAddr;	// Sender's Ethernet Address
		byte[] arp_srcProtoAddr;// Sender's IP Address
		byte[] arp_destHdAddr;;	// Receiver's
		byte[] arp_destProtoAddr;
 
		public _ARP_HEADER() { //28byte
			this.arp_hdType = new byte[2];
			this.arp_prototype = new byte[2];
			this.arp_hdLength = (byte) 0x00;
			this.arp_protoLength = (byte) 0x00;
			this.arp_op = new byte[2];
			this.arp_srcHdAddr = new byte[4];
			this.arp_srcProtoAddr = new byte[6];
			this.arp_destHdAddr = new byte[4];
			this.arp_destProtoAddr = new byte[6];
		}
	}

	_ARP_HEADER m_aHeader = new _ARP_HEADER();

	public ARPLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
		
		// 캐시테이블 자동 제거 스레드
	}

	public void ResetHeader() {
		for (int i = 0; i < 5; i++) {
			m_aHeader.enet_dstaddr.addr[i] = (byte) 0x00;
			m_aHeader.enet_srcaddr.addr[i] = (byte) 0x00;
		}
		m_aHeader.enet_type[0] = (byte) 0x00;
		m_aHeader.enet_type[1] = (byte) 0x00;
		m_aHeader.enet_data = null;
	}
	
	// ARPLayer에서 만든 cache_Table을 다른 Layer에서도 사용할 수 있게끔 해주는 기능.
	public Map<String, _Cache_Entry> getCacheList(){
		return cache_Table;
	}
	
	public synchronized boolean Send(byte[] input, int length) {
		byte[] bytes= new byte[4];
		
		//IP HEADER의 src_addr을 byte[] -> String으로 변환 ex) -124,112,113,-35 -> 132.112.113.221
		String ar="";
		System.arraycopy(input, 12, bytes, 0, 4);
		for (byte b : bytes) {
		    ar += Integer.toString(b & 0xFF)+".";
		}
		ar = ar.substring(0, ar.length() - 1);
		
		//src_addr를 KEY로 갖고 cache_Entry를 VALUE로 갖는 hashMap 생성
		_Cache_Entry cache_Entry = new _Cache_Entry(new byte[6], "Incomplete", 10);
		cache_Table.put(ar,cache_Entry);
		
		System.out.println("MAP == " + cache_Table);
		System.out.println("SIZE == " + cache_Table.size());
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
