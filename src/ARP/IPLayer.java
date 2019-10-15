package ARP;

import java.util.ArrayList;


public class IPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	private class _IP_ADDR {
		private byte[] addr = new byte[4];

		public _IP_ADDR() {
			this.addr[0] = (byte) 0x00;
			this.addr[1] = (byte) 0x00;
			this.addr[2] = (byte) 0x00;
			this.addr[3] = (byte) 0x00;
		}
	}

	private class _IP_HEADER {
		byte ip_verlen;		//ip version -> IPv4 : 4	(O, 1byte)
		byte ip_tos;		//type of service			(X, 1byte)
		byte[] ip_len;		//total packet length		(△,  2byte)
		byte[] ip_id;		//datagram id				(X, 2byte)
		byte[] ip_fragoff;	//fragment offset			(X, 2byte)
		byte ip_ttl;		//time to live in gatewaye hopes(X, 1byte)
		byte ip_proto;		//IP protocol				(X, 1byte)
		byte[] ip_cksum;	//header checksum			(X, 2byte)
		_IP_ADDR ip_src;	//IP address of source		(O, 4byte)
		_IP_ADDR ip_dst;	//IP address of destination (O, 4byte)
		byte[] ip_data;		// new						(O, IP_DATA_SIZE)

		public _IP_HEADER() {
			this.ip_verlen = (byte) 0x00;
			this.ip_tos = (byte) 0x00;
			this.ip_len = new byte[2];
			this.ip_id = new byte[2];
			this.ip_fragoff = new byte[2];
			this.ip_ttl = (byte) 0x00;
			this.ip_proto = (byte) 0x00;
			this.ip_cksum = new byte[2];
			this.ip_src = new _IP_ADDR();
			this.ip_dst = new _IP_ADDR();
			this.ip_data = null;	// new
		}
	}

	_IP_HEADER m_iHeader = new _IP_HEADER(); 
	
	public IPLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		//ResetHeader();
	}

	public boolean Send(byte[] input, int length) {
		return false;
	}

	public boolean Send(String filename) {
		return false;
	}

	public boolean Receive(byte[] input) {
		return false;
	}

	public boolean Receive() {
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
