package ARP;

import java.util.ArrayList;


public class TCPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _TCP_HEADER {
		byte[] tcp_sport;	//source port 			(O, 2byte)
		byte[] tcp_dport;	//destination port		(O, 2byte)
		byte[] tcp_seq;		//sequence number		(X, 4byte)
		byte[] tcp_ack;		//acknowledged sequence	(X, 4byte)
		byte tcp_offset;	//fragment -> no use	(X, 1byte)
		byte tcp_flag;		//control flag			(X, 1byte)
		byte[] tcp_window;	//no use				(X, 2byte)
		byte[] tcp_cksum;	//check sum				(X, 2byte)
		byte[] tcp_urgptr;	//no use				(X, 2byte)
		byte[] Padding;		//no use				(X, 4byte)
		byte[] tcp_data;	//						(O, TCP_DATA_SIZE)

		public _TCP_HEADER() {
			this.tcp_sport = new byte[2];
			this.tcp_dport = new byte[2];
			this.tcp_seq = new byte[4];
			this.tcp_ack = new byte[4];
			this.tcp_offset = (byte) 0x00;
			this.tcp_flag = (byte) 0x00;
			this.tcp_window = new byte[2];
			this.tcp_cksum = new byte[2];
			this.tcp_urgptr = new byte[2];
			this.Padding = new byte[4];
			this.tcp_data = null;	// new
		}
	}

	_TCP_HEADER m_tHeader = new _TCP_HEADER();
	
	public TCPLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		//ResetHeader();
	}
	
	public byte[] getPortNumber() {//포트 넘버를 아래 계층에서 가져오는 메소드
		return null;//TCP계층 내의 메소드를 제거할 방법이 없는지 확인
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
