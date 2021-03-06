package ARP;

import java.lang.reflect.Array;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

public class EthernetLayer implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private final static byte[] enetType_CHAT = byte4To2(intToByte(0x2080));// 안 바뀔 필드라서 final로 넣었습니다.
	private final static byte[] enetType_FILE = byte4To2(intToByte(0x2090));
	private final static byte[] enetType_ARP = byte4To2(intToByte(0x0806));
	private final static byte[] enetType_IP = byte4To2(intToByte(0x0800));
	private final static byte[] broadcastAddr = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF };
	byte[] GratOriginMac = new byte[6];//GratARP할때 사용할 원래 맥 주소(변경전 맥주소)
	
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

	private class _ETHERNET_HEADER {
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data;

		public _ETHERNET_HEADER() {
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type = new byte[2];
			this.enet_data = null;
		}
	}

	_ETHERNET_HEADER m_sHeader = new _ETHERNET_HEADER();

	public EthernetLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}

	public void ResetHeader() {
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = (byte) 0x00;
			m_sHeader.enet_srcaddr.addr[i] = (byte) 0x00;
		}
		m_sHeader.enet_type[0] = (byte) 0x00;
		m_sHeader.enet_type[1] = (byte) 0x00;
		m_sHeader.enet_data = null;
	}

	public _ETHERNET_ADDR GetEnetDstAddress() {
		return m_sHeader.enet_dstaddr;
	}

	public _ETHERNET_ADDR GetEnetSrcAddress() {
		return m_sHeader.enet_srcaddr;
	}

	public void SetEnetType(byte[] input) {
		for (int i = 0; i < 2; i++) {
			m_sHeader.enet_type[i] = input[i];
		}
	}

	public void SetEnetDstAddress(byte[] input) {
		// 디버깅 System.out.println("header len = " + m_sHeader.enet_dstaddr.addr.length);
		// 디버깅 System.out.println("input len = " + input.length);
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_dstaddr.addr[i] = input[i];
		}
	}

	public void SetEnetSrcAddress(byte[] input) {
		for (int i = 0; i < 6; i++) {
			m_sHeader.enet_srcaddr.addr[i] = input[i];
		}
	}

	public byte[] ObjToByte(_ETHERNET_HEADER Header, byte[] input, int length) {
		byte[] buf = new byte[length + 14];

		buf[0] = Header.enet_dstaddr.addr[0];
		buf[1] = Header.enet_dstaddr.addr[1];
		buf[2] = Header.enet_dstaddr.addr[2];
		buf[3] = Header.enet_dstaddr.addr[3];
		buf[4] = Header.enet_dstaddr.addr[4];
		buf[5] = Header.enet_dstaddr.addr[5];

		buf[6] = Header.enet_srcaddr.addr[0];
		buf[7] = Header.enet_srcaddr.addr[1];
		buf[8] = Header.enet_srcaddr.addr[2];
		buf[9] = Header.enet_srcaddr.addr[3];
		buf[10] = Header.enet_srcaddr.addr[4];
		buf[11] = Header.enet_srcaddr.addr[5];

		buf[12] = Header.enet_type[0];
		buf[13] = Header.enet_type[1];

		for (int i = 0; i < length; i++)
			buf[14 + i] = input[i];

		return buf;
	}

	public byte[] RemoveEtherHeader(byte[] input, int length) {
		byte[] data = new byte[length - 14];
		for (int i = 0; i < length - 14; i++)
			data[i] = input[14 + i];
		return data;
	}

	public boolean Send(byte[] input, int length) {
	      try {
	         setEtherHeader(input);
	         byte[] bytes = ObjToByte(m_sHeader, input, length);

	         this.GetUnderLayer().Send(bytes, length + 14);
	         
	      } catch (SocketException e) {
	         // TODO 자동 생성된 catch 블록
	         e.printStackTrace();
	      }
	      
	      return false;
	   }

	public void setEtherHeader(byte[] input) throws SocketException {
	      byte[] my_dstAddress = new byte[6];
	      byte[] my_srcAddress = new byte[6];
	      byte[] my_enetType = new byte[2];

	      // my_dstAddress 세팅
	      if (needToBroadCast(input)) {
	         System.arraycopy(broadcastAddr, 0, my_dstAddress, 0, 6);
	      } else {
	         System.arraycopy(input, 18, my_dstAddress, 0, 6);
	      }
	      // my_srcAddress 세팅
	      // Gratuitous ARP일 경우, 변경 전 MAC주소 세팅
	      if (isGratuitousARP(input)) {
	         this.getMyPCAddr();
	         System.arraycopy(GratOriginMac, 0, my_srcAddress, 0, 6);
	      } else {
	         System.arraycopy(input, 8, my_srcAddress, 0, 6);
	      }
	      // my_enetType 세팅
	      System.arraycopy(enetType_ARP, 0, my_enetType, 0, 2);

	      SetEnetDstAddress(my_dstAddress);
	      SetEnetSrcAddress(my_srcAddress);
	      SetEnetType(my_enetType);
	   }
	//PC의 맥주소 가져오기
	public void getMyPCAddr() throws SocketException {
	      Enumeration<NetworkInterface> interfaces = null;
	      interfaces = NetworkInterface.getNetworkInterfaces(); // 현재 PC의 모든 NIC를 열거형으로 받는다.

	      // isUP 중에 MAC과 IP 주소를 출력
	      while (interfaces.hasMoreElements()) {
	         NetworkInterface networkInterface = interfaces.nextElement();
	         if (networkInterface.isUp()) {
	            if (networkInterface.getHardwareAddress() != null) {// loop back Interface가 null이므로, 걸러준다.
	               GratOriginMac = networkInterface.getHardwareAddress(); // MAC주소 받기
	               break; // 현재 사용중인 NIC 이외에는 필요 없다. 반복문 탈출
	            }
	         }
	      }
	   }
	

	// ARP 과제 추가 메서드 sendARP는 필요 없을 것 같아서 삭제했습니다.
	// ARP 헤더는 ARP layer에서 만드는 거니까 broadcasting 만 해 주면 될 것 같습니다.

	public byte[] getPortNumber() {// ARP 과제 추가 메서
		return null;// 생성될 ARP 테이블을 검색하여 포트 넘버를 제공함
	}

	public boolean needToBroadCast(byte[] input) {// when target MAC address = ???
		for (int i = 0; i < 6; i++) {
			if (input[i + 18] == (byte) 0x00)
				continue;
			else
				return false;
		}
		return true;
	}

	public boolean IsItMyPacket(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (m_sHeader.enet_srcaddr.addr[i] == input[6 + i])
				continue;
			else
				return false;
		}
		return true;
	}

	public boolean IsItMine(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (m_sHeader.enet_srcaddr.addr[i] == input[i])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	public boolean IsItBroadcast(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (input[i] == (byte) -1) {
				continue;
			} else
				return false;
		}
		return true;
	}

	public boolean IsItARP(byte[] input) {// ARP protocol 판별
		byte[] enet_type = { input[12], input[13] };

		if (Arrays.equals(enet_type, enetType_ARP)) {
			return true;
		} else
			return false;
	}

	public boolean IsItIP(byte[] input) {// IP Protocol 판별
		byte[] enet_type = { input[12], input[13] };

		if (Arrays.equals(enet_type, enetType_IP)) {
			return true;
		} else
			return false;
	}

	// Gratuitous ARP인지 확인합니다.
	public boolean isGratuitousARP(byte[] input) {
		for (int i = 0; i < 4; i++) {
			if (input[i + 14] == input[i + 24])
				continue;
			else {
				return false;
			}
		}
		return true;
	}

	public boolean Receive(byte[] input) {
		int len = input.length;
		byte[] data;
		boolean MyPacket, Mine, Broadcast;
		MyPacket = IsItMyPacket(input);

		if (MyPacket == true) {
			// 내가 만든 패킷이면 수신하지 않음.
			return false;
		} else {
			Broadcast = IsItBroadcast(input);
			if (Broadcast == false) {
				// 브로드 캐스팅도 아니면서,
				Mine = IsItMine(input);
				if (Mine == false) {
					// 목적지가 자신이 아니면 수신하지 않음.
					return false;
				}
			}
		}

		// ARP 과제 추가 사항:
		if (IsItARP(input)) {// ARP이고
			Mine = IsItMine(input);
			Broadcast = IsItBroadcast(input);
			if (Mine == true || Broadcast == true) {// 수신자가 나이거나 broadcast이면
				data = RemoveEtherHeader(input, input.length);
				this.GetUpperLayer(0).Receive(data);// receive하고 ARP layer로 올려보냄 . upperlayer(0)이 맞나요?
				return true;
			} else
				return false;
		} else
			return false;
	}

	public static byte[] intToByte(int value) {
		byte[] byteArray = new byte[4];
		byteArray[0] = (byte) (value >> 24);
		byteArray[1] = (byte) (value >> 16);
		byteArray[2] = (byte) (value >> 8);
		byteArray[3] = (byte) (value);
		return byteArray;
	}

	public static byte[] byte4To2(byte[] fourByte) {
		byte[] byteArray = new byte[2];
		byteArray[0] = fourByte[2];
		byteArray[1] = fourByte[3];
		return byteArray;
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
