package ARP;


import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class NILayer implements BaseLayer {

	public int nUnderLayerCount =0;
	public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<BaseLayer>();
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	int m_iNumAdapter;
	public Pcap m_AdapterObject;
	public PcapIf device;
	public ArrayList<PcapIf> m_pAdapterList;
	StringBuilder errbuf = new StringBuilder();

	public NILayer(String pName) {
		// super(pName);
		pLayerName = pName;
		m_pAdapterList = new ArrayList<PcapIf>();
		m_iNumAdapter = 0;
		SetAdapterList();
	}

	public void PacketStartDriver() {
		int snaplen = 64 * 1024; // Capture all packets, no trucation
		int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
		int timeout = 10 * 1000; // 10 seconds in millis
		m_AdapterObject = Pcap.openLive(m_pAdapterList.get(m_iNumAdapter).getName(), snaplen, flags, timeout, errbuf);
	}

	public PcapIf GetAdapterObject(int iIndex) {
		return m_pAdapterList.get(iIndex);
	}

	public void SetAdapterNumber(int iNum) {
		m_iNumAdapter = iNum;
		PacketStartDriver();
		Receive();
	}

	public void SetAdapterList() {
		// 현재 컴퓨터에 존재하는 모든 네트워크 어뎁터 목록 가져오기
		int r = Pcap.findAllDevs(m_pAdapterList, errbuf);

		// 네트워크 어뎁터가 하나도 존재하지 않을 경우 에러 처리
		if (r == Pcap.NOT_OK || m_pAdapterList.isEmpty())
			System.out.println("[Error] 네트워크 어댑터를 읽지 못하였습니다. Error : " + errbuf.toString());
	}

	public ArrayList<PcapIf> getAdapterList() {
		return m_pAdapterList;
	}

	public boolean Send(byte[] input, int length) {
		ByteBuffer buf = ByteBuffer.wrap(input);
		if (m_AdapterObject.sendPacket(buf) != Pcap.OK) {
			System.err.println(m_AdapterObject.getErr());
			return false;
		}
		return true;
	}

	public boolean Receive() {
		Receive_Thread thread = new Receive_Thread(m_AdapterObject, this.GetUpperLayer(0));
		Thread obj = new Thread(thread,"Receive Thread");
		obj.start();

		return false;
	}

	// old
		//@Override
		//public void SetUnderLayer(BaseLayer pUnderLayer) { // 하위 레이어 설정
		//	if (pUnderLayer == null)
		//		return;
		//	this.p_UnderLayer = pUnderLayer;
		//}
		
		// new
		@Override
		public void SetUnderLayer(BaseLayer pUnderLayer) { // 하위 레이어 설정
			if (pUnderLayer == null)
				return;
			this.p_aUnderLayer.add(nUnderLayerCount++, pUnderLayer);
		}

		@Override
		public void SetUpperLayer(BaseLayer pUpperLayer) { // 상위 레이어 설정
			if (pUpperLayer == null)
				return;
			this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
			// nUpperLayerCount++;
		}

		@Override
		public String GetLayerName() { // 레이어 이름 반환
			return pLayerName;
		}

		// old
		//@Override
		//public BaseLayer GetUnderLayer() { // 하위 레이어 반환
		//	if (p_UnderLayer == null)
		//		return null;
		//	return p_UnderLayer;
		//}
		
		// new
		@Override
		public BaseLayer GetUnderLayer(int nindex) { // 상위 레이어 반환
			if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
				return null;
			return p_aUnderLayer.get(nindex);
		}

		@Override
		public BaseLayer GetUpperLayer(int nindex) { // 상위 레이어 반환
			if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
				return null;
			return p_aUpperLayer.get(nindex);
		}

		@Override
		public void SetUpperUnderLayer(BaseLayer pUULayer) { // 매개변수로 받은 레이어를 상위레이어로 세팅하고, 그 레이어의 하위를 함수를 호출한 객체로 저장
			this.SetUpperLayer(pUULayer);
			pUULayer.SetUnderLayer(this);
		}
}

class Receive_Thread implements Runnable {
	byte[] data;
	Pcap AdapterObject;
	BaseLayer UpperLayer;

	public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {
		// TODO Auto-generated constructor stub
		AdapterObject = m_AdapterObject;
		UpperLayer = m_UpperLayer;
	}

	@Override
	public void run() {
		while (true) {
			PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
				public void nextPacket(PcapPacket packet, String user) {
					data = packet.getByteArray(0, packet.size());
					UpperLayer.Receive(data);
				}
			};
			AdapterObject.loop(100000, jpacketHandler, "");
		}
	}
}
