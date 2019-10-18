package ARP;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TCPLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _TCP_HEADER { // 24byte
		// sport와 dport는 추가구현 때 ChatApp/FileApp을 분별하기 위한 것?
		// sport는 source port address로 자신의 ephemeral port number를,
		// dport는 destination port address로 받는 상대의 well-known port number를 씁니다
		// 추가 구현 시 setSportnum(), setDportnum() 같은 함수를 사용해야겠죠
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

	public TCPLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
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
		m_tHeader.tcp_data = null;
	}

	//public byte[] getPortNumber() {// 포트 넘버를 아래 계층에서 가져오는 메소드
		//return null;// TCP계층 내의 메소드를 제거할 방법이 없는지 확인
	//} 추가 구현 메소드이므로 주석처리함

	public byte[] ObjToByte(_TCP_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 24];

		System.arraycopy(Header.tcp_sport, 0, buf, 0, 2); // buf에 2byte sport 초기화
		System.arraycopy(Header.tcp_dport, 0, buf, 2, 2); // 2byte dport
		System.arraycopy(Header.tcp_seq, 0, buf, 4, 4); // 4byte sqe
		System.arraycopy(Header.tcp_ack, 0, buf, 8, 4); // 4byte ack
		buf[12] = Header.tcp_offset; // 1byte offset
		buf[13] = Header.tcp_flag; // 1byte flag
		System.arraycopy(Header.tcp_window, 0, buf, 14, 2); // 2byte window
		System.arraycopy(Header.tcp_cksum, 0, buf, 16, 2); // 2byte cksum
		System.arraycopy(Header.tcp_urgptr, 0, buf, 18, 2); // 2byte urgptr
		System.arraycopy(Header.padding, 0, buf, 20, 4); // 4byte padding

		for (int i = 0; i < length; i++)
			buf[24 + i] = input[i];

		return buf;
	}

	public boolean Send(byte[] input, int length) {
		//send 내에 구현사항을 많이 넣으면 가독성이 떨어져서 ObjToByte로 분리했습니다.
		byte[] TCP_header_added_bytes = ObjToByte(m_tHeader, input, length);

		this.GetUnderLayer().Send(TCP_header_added_bytes, TCP_header_added_bytes.length);
		return false;
	}

	public boolean Send(String filename) {
		return false;
	}

	public boolean Receive(byte[] input) {
		//기본 구현 사항에서는 TCP layer는 reecive하지 않습니다.
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
