package ARP;

import java.util.ArrayList;

public class TCPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _TCP_HEADER {
		byte[] tcp_sport; // source port (O, 2byte)
		byte[] tcp_dport; // destination port (O, 2byte)
		byte[] tcp_seq; // sequence number (X, 4byte)
		byte[] tcp_ack; // acknowledged sequence (X, 4byte)
		byte tcp_offset; // fragment -> no use (X, 1byte)
		byte tcp_flag; // control flag (X, 1byte)
		byte[] tcp_window; // no use (X, 2byte)
		byte[] tcp_cksum; // check sum (X, 2byte)
		byte[] tcp_urgptr; // no use (X, 2byte)
		byte[] padding; // no use (X, 4byte)
		byte[] tcp_data; // (O, TCP_DATA_SIZE)

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
			this.padding = new byte[4];
			this.tcp_data = null; // new
		}
	}

	_TCP_HEADER m_tHeader = new _TCP_HEADER();

	public TCPLayer(String pName) {//생성자:레이어 생성+헤더 생성+헤더 초기화
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	// public byte[] getPortNumber() {
	// return null; // 추가 구현 사항: 포트 넘버를 아래 계층에서 가져오는 메소드
	// }

	public void ResetHeader() {// 헤더 초기화(기본 구현=모든 필드 0x00)
		for (int i = 0; i < 4; i++) {
			m_tHeader.tcp_seq[i] = (byte) 0x00;
			m_tHeader.tcp_ack[i] = (byte) 0x00;
			m_tHeader.padding[i] = (byte) 0x00;
		}
		for (int i = 0; i < 2; i++) {
			m_tHeader.tcp_sport[i] = (byte) 0x00;
			m_tHeader.tcp_dport[i] = (byte) 0x00;
			m_tHeader.tcp_window[i] = (byte) 0x00;
			m_tHeader.tcp_cksum[i] = (byte) 0x00;
			m_tHeader.tcp_urgptr[i] = (byte) 0x00;
		}
		m_tHeader.tcp_offset = (byte) 0x00;
		m_tHeader.tcp_flag = (byte) 0x00;
		m_tHeader.tcp_data = null;
	}

	public byte[] ObjToByte(_TCP_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 24];//헤더길이+input 데이터 길이+length

		buf[0] = Header.tcp_sport[0];
		buf[1] = Header.tcp_sport[1];

		buf[2] = Header.tcp_dport[0];
		buf[3] = Header.tcp_dport[1];

		buf[4] = Header.tcp_seq[0];
		buf[5] = Header.tcp_seq[1];
		buf[6] = Header.tcp_seq[2];
		buf[7] = Header.tcp_seq[3];

		buf[8] = Header.tcp_ack[0];
		buf[9] = Header.tcp_ack[1];
		buf[10] = Header.tcp_ack[2];
		buf[11] = Header.tcp_ack[3];

		buf[12] = Header.tcp_offset;
		buf[13] = Header.tcp_flag;
		
		buf[14] = Header.tcp_window[0];
		buf[15] = Header.tcp_window[1];
		
		buf[16] = Header.tcp_cksum[0];
		buf[17] = Header.tcp_cksum[1];
		
		buf[18] = Header.tcp_urgptr[0];
		buf[19] = Header.tcp_urgptr[1];
		
		buf[20] = Header.padding[0];
		buf[21] = Header.padding[1];
		buf[22] = Header.padding[2];
		buf[23] = Header.padding[3];

		for (int i = 0; i < length; i++)
			buf[24 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) {
		byte[] tHeader_added_bytes = ObjToByte(m_tHeader, input, length);
		this.GetUnderLayer().Send(tHeader_added_bytes, tHeader_added_bytes.length);
		return false;
	}

	public boolean Receive(byte[] input) {
		//추가 구현 사항:기본 구현에서 tcp layer는 receive 하지 않음
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
