package ARP;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
		int snaplen = 64 * 1024; 			 // Capture all packets, no trucation
		int flags = Pcap.MODE_PROMISCUOUS; 	 // capture all packets
		int timeout = 10 * 1000;			 // 10 seconds in millis
		m_AdapterObject = Pcap.openLive(m_pAdapterList.get(m_iNumAdapter).getName(), snaplen, flags, timeout, errbuf);
	}

	public PcapIf GetAdapterObject(int iIndex) {
		return m_pAdapterList.get(iIndex);
	}

	public void SetAdapterNumber(List<byte[]> bytesMAC,int selectedNum) throws IOException {
		int deviceLen = 0;
		int cnt =0;
		
		System.out.println("\n----------------------------" + pLayerName + " : SetAdapterNumber(" +selectedNum+ ") ------------------------------------");
		for (PcapIf device : m_pAdapterList) { // m_pAdapterList 수 만큼 반복 -> deviceLen
			for (int z = 0; z < 6; z++) {
				// argu 1: device의 hardwareAddr이 null인데 device.getHardwareAddr()을 호출하면 error가 뜬다. 이를 방지하는 조건식
				// argu 2: bytesMAC에 하나밖에 안 들어있는데 SelecteNum으로 2가 들어온 경우, bytesMAC.get(2)를 실행하게 되어 outOfIndex error가 뜬다. 이를 방지하는 조건식.
				if (device.getHardwareAddress() != null && bytesMAC.size() >= (selectedNum+1)) {
					if (bytesMAC.get(selectedNum)[z] == device.getHardwareAddress()[z]) {
						cnt++;
						if (cnt == 6) {	//byte[]가 모두 일치함
							System.out.println("\n["+ deviceLen +"번]과 일치!");
							System.out.println(m_pAdapterList.get(deviceLen).getDescription());
							System.out.println(m_pAdapterList.get(deviceLen).getAddresses().get(0));
							System.out.printf(" %d %d %d %d %d %d\n", device.getHardwareAddress()[0],
									device.getHardwareAddress()[1], device.getHardwareAddress()[2],
									device.getHardwareAddress()[3], device.getHardwareAddress()[4],
									device.getHardwareAddress()[5]);
							System.out.println("");

							m_iNumAdapter = deviceLen;
						}
					} else 		// 바이트간 비교시 != 이면,
						break;  // 반복문을 계속하지 않고 탈출
				}
			}
			cnt = 0;

			// print for debug
			String description = (device.getDescription() != null) ? device.getDescription() : "장비에 대한 설명이 없습니다.";
			System.out.printf("[%d번]: %s %s \n", deviceLen, description,device.getAddresses());
			if (device.getHardwareAddress() != null) {
				try {
					System.out.printf(" %d %d %d %d %d %d\n", device.getHardwareAddress()[0],
							device.getHardwareAddress()[1], device.getHardwareAddress()[2],
							device.getHardwareAddress()[3], device.getHardwareAddress()[4],
							device.getHardwareAddress()[5]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			deviceLen++;
		}
		System.out.println("-----------------------------------------------------------------------------");

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
