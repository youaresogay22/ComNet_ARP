package ARP;

import java.util.ArrayList;
import java.util.StringTokenizer;

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

		buf[0] = m_iHeader.ip_verlen; // buf에 1byte verlen 초기화
		buf[1] = m_iHeader.ip_tos; // 1byte tos
		System.arraycopy(m_iHeader.ip_len, 0, buf, 2, 2); // 2byte len
		System.arraycopy(m_iHeader.ip_id, 0, buf, 4, 2); // 2byte id
		System.arraycopy(m_iHeader.ip_fragoff, 0, buf, 6, 2); // 2byte fragoff
		buf[8] = m_iHeader.ip_ttl; // 1byte ttl
		buf[9] = m_iHeader.ip_proto; // 1byte proto
		System.arraycopy(m_iHeader.ip_cksum, 0, buf, 10, 2); // 2byte cksum
		System.arraycopy(m_iHeader.ip_src.addr, 0, buf, 12, 4); // 4byte src
		System.arraycopy(m_iHeader.ip_dst.addr, 0, buf, 16, 4); // 4byte dst
		System.arraycopy(input, 0, buf, 20, length); // IP header에 TCP header 붙이기

		for (int i = 0; i < length; i++)
			buf[20 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) {
		//send 내에 구현사항을 많이 넣으면 가독성이 떨어져서 ObjToByte로 분리했습니다.
		byte[] IP_header_added_bytes = ObjToByte(m_iHeader, input, length);

		this.GetUnderLayer().Send(IP_header_added_bytes, IP_header_added_bytes.length);
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
