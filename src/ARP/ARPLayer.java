package ARP;

import java.util.ArrayList;

public class ARPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _ARP_HEADER {
		byte[] arp_hdType;		// Ethernet = 1
		byte[] arp_prototype;	//
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

	}
	
	public boolean Send(byte[] input, int length) {
		
		System.out.println("ARP Send");
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
