//1. GUI에서 proxy Add버튼을 통해 Device,IP,MAC을 설정하면 이것들을 cache_Table에 추가해야 한다.
//2. ARP & Proxy remove
//3.

package ARP;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ARP.ARPLayer._Cache_Entry;

@SuppressWarnings("serial")
public class ARPDlg extends JFrame implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;

	private static LayerManager m_LayerMgr = new LayerManager();
	static Map<String, _Cache_Entry> cache_Table;
	static Set<String> cache_Itr;
	static ArrayList<byte[]> byteArray = new ArrayList<byte[]>();

	private JTextField TextWrite; // 좌측 "IP주소"
	private JTextField TextWrite2; // 우측 "H/W주소"
	private JTextField TextWrite3; // 팝업 창 "IP주소"
	private JTextField TextWrite4; // 팝업 창 "Ethernet주소"

	JPanel TextInputPanel; // 좌측 "IP주소"
	JPanel TextInputPanel2; // 우측 "H/W주소"
	JPanel TextInputPanel3; // 팝업 창 "IP주소"
	JPanel TextInputPanel4; // 팝업 창 "Ethernet주소"
	JComboBox DeviceComboBox; // 팝업 창 "Device" 콤보박스

	Container contentPane; // 메인 콘테이너
	Container contentPane2; // 팝업 창 콘테이너

	static DefaultListModel ARPModel; // JList의 Item들을 관리하는 모델
	static DefaultListModel ProxyModel;
	JFrame PopUpFrame; // 프록시 add버튼 클릭 시 나타나는 팝업 창

	JList ArpArea; // 좌측 ARP 텍스트 출력란
	JList ProxyArea; // 우측 Proxy 텍스트 출력란
	JTextArea srcAddress; // 근원지 주소
	JTextArea dstAddress; // 도착지 주소

	JLabel lblIP; // 좌측 3층 "IP주소"
	JLabel lblHW; // 우측 3층 "H/W주소"
	JLabel lblDevice; // 팝업 창 "Device" 라벨
	JLabel lblPopUpIP; // 팝업 창 "IP주소" 라벨
	JLabel lblEthernet; // 팝업 창 "Ethernet 주소" 라벨

	JButton Item_Delete_Button; // 좌측 "Item Delete" 버튼
	JButton All_Delete_Button; // 좌측 "All Delete" 버튼
	JButton Add_Button; // 우측 "Add" 버튼
	JButton Delete_Button; // 우측 "Delete" 버튼
	JButton ARP_Send_Button; // 좌측 "Send" 버튼
	JButton Grat_Send_Button; // 우측 "Send" 버튼
	JButton Exit_Button; // 하단 "종료"버튼
	JButton Cancle_Button; // 하단 "취소"버튼
	JButton OK_Button; // 팝업 다이얼로그 "OK"버튼
	JButton Proxy_Cancle_Button;// 팝업 다이얼로그 "Cancel"버튼

	public static void main(String[] args) {
		m_LayerMgr.AddLayer(new NILayer("NI"));
		m_LayerMgr.AddLayer(new EthernetLayer("ETHERNET"));
		m_LayerMgr.AddLayer(new ARPLayer("ARP"));
		m_LayerMgr.AddLayer(new IPLayer("IP"));
		m_LayerMgr.AddLayer(new TCPLayer("TCP"));
		m_LayerMgr.AddLayer(new ARPDlg("GUI"));

		m_LayerMgr.ConnectLayers(" NI ( *ETHERNET ( *ARP +IP ( -ARP *TCP ( *GUI ) ) ) )");

		// 2초 마다 printCash()를 호출하여 캐시 테이블과 GUI를 갱신하는 쓰레드
		Runnable task = () -> {
			while (true) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				///디버깅
				//for (String ipAddr : cache_Itr) {
				//	_Cache_Entry cacheEntry = cache_Table.get(ipAddr);
				//	System.out.println("UI레이어 테이블:"+cacheEntry.cache_ttl);
				//}
				///디버깅
				printCash(); // GUI에 cache_Table을 print
			}
		};
		// 위 기능을 수행하는 쓰레드 생성 및 시작.
		Thread cacheUpdate = new Thread(task);
		cacheUpdate.start();
	}
	public ARPDlg(String pName) {
		pLayerName = pName;

		setTitle("TestARP");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 748, 380);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		////////////////////// 좌측 1층
		JPanel ARPPanel = new JPanel();
		ARPPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		ARPPanel.setBounds(10, 5, 355, 276);
		contentPane.add(ARPPanel);
		ARPPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();
		chattingEditorPanel.setBounds(10, 15, 340, 180);
		ARPPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		ARPModel = new DefaultListModel();
		ArpArea = new JList(ARPModel);
		ArpArea.setBounds(0, 0, 340, 180);
		chattingEditorPanel.add(ArpArea);

		////////////////////// 좌측 2층
		Item_Delete_Button = new JButton("Item Delete");
		Item_Delete_Button.setBounds(50, 205, 120, 25);
		Item_Delete_Button.addActionListener(new setAddressListener());
		ARPPanel.add(Item_Delete_Button);

		All_Delete_Button = new JButton("All Delete");
		All_Delete_Button.setBounds(190, 205, 120, 25);
		All_Delete_Button.addActionListener(new setAddressListener());
		ARPPanel.add(All_Delete_Button);

		////////////////////// 좌측 3층
		lblIP = new JLabel("IP주소");
		lblIP.setBounds(20, 240, 80, 25);
		ARPPanel.add(lblIP);

		TextInputPanel = new JPanel();
		TextInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		TextInputPanel.setBounds(70, 240, 180, 25);
		ARPPanel.add(TextInputPanel);
		TextInputPanel.setLayout(null);

		TextWrite = new JTextField();
		TextWrite.setBounds(2, 2, 180, 25);// 249
		TextInputPanel.add(TextWrite);
		TextWrite.setHorizontalAlignment(SwingConstants.CENTER);
		TextWrite.setColumns(10);

		ARP_Send_Button = new JButton("Send");
		ARP_Send_Button.setBounds(260, 240, 80, 25);
		ARP_Send_Button.addActionListener(new setAddressListener());
		ARPPanel.add(ARP_Send_Button);

		/////////////////// 우측 1층
		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy ARP Entry",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(370, 5, 355, 215);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		JPanel proxyEditorPanel = new JPanel();
		proxyEditorPanel.setBounds(5, 20, 345, 150);
		settingPanel.add(proxyEditorPanel);
		proxyEditorPanel.setLayout(null);

		ProxyModel = new DefaultListModel();
		ProxyArea = new JList(ProxyModel);
		ProxyArea.setBounds(0, 0, 345, 150);
		proxyEditorPanel.add(ProxyArea);

		///////////////////// 우측 2층
		Add_Button = new JButton("Add");
		Add_Button.setBounds(50, 180, 120, 25);
		Add_Button.addActionListener(new setAddressListener());
		settingPanel.add(Add_Button);

		Delete_Button = new JButton("Delete");
		Delete_Button.setBounds(190, 180, 120, 25);
		Delete_Button.addActionListener(new setAddressListener());
		settingPanel.add(Delete_Button);

		////////////////////// 우측 3층
		JPanel GratuitousPanel = new JPanel();
		GratuitousPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gratuitous ARP",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GratuitousPanel.setBounds(370, 220, 355, 60);
		contentPane.add(GratuitousPanel);
		GratuitousPanel.setLayout(null);

		lblHW = new JLabel("H/W주소");
		lblHW.setBounds(15, 23, 60, 25);
		GratuitousPanel.add(lblHW);

		TextInputPanel2 = new JPanel();
		TextInputPanel2.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		TextInputPanel2.setBounds(70, 23, 180, 25);
		GratuitousPanel.add(TextInputPanel2);
		TextInputPanel2.setLayout(null);

		TextWrite2 = new JTextField();
		TextWrite2.setBounds(0, 0, 180, 25);// 249
		TextInputPanel2.add(TextWrite2);
		TextWrite2.setHorizontalAlignment(SwingConstants.CENTER);
		TextWrite2.setColumns(10);

		Grat_Send_Button = new JButton("Send");
		Grat_Send_Button.setBounds(260, 23, 80, 25);
		Grat_Send_Button.addActionListener(new setAddressListener());
		GratuitousPanel.add(Grat_Send_Button);

		///////////////////////// 하단
		Exit_Button = new JButton("종료");
		Exit_Button.setBounds(263, 290, 100, 25);
		Exit_Button.addActionListener(new setAddressListener());
		contentPane.add(Exit_Button);

		Cancle_Button = new JButton("취소");
		Cancle_Button.setBounds(370, 290, 100, 25);
		Cancle_Button.addActionListener(new setAddressListener());
		contentPane.add(Cancle_Button);

		setVisible(true);
	}

	///////////////// 버튼 클릭 이벤트
	class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// ARP 이벤트
			if (e.getSource() == Item_Delete_Button) {
				if (ArpArea.getSelectedValue() != null) {
					// 선택한 Item의 문자열을 받아와 앞 뒤 공백을 제거한 후(trim) 가운데 공백을 구분자 삼아 토큰화 한다.
					StringTokenizer st = new StringTokenizer(ArpArea.getSelectedValue().toString().trim(), " ");

					// cache_Table 에서 제거한 뒤 GUI에서도 제거
					cache_Table.remove(st.nextToken());
					ARPModel.remove(ArpArea.getSelectedIndex());
				}
			}
			if (e.getSource() == All_Delete_Button) {
				// cache_Table 에서 모두 제거한 뒤 GUI에서도 제거
				cache_Table.clear();
				ARPModel.removeAllElements();
			}
			if (e.getSource() == ARP_Send_Button) {
				if (isValidIPv4Addr(TextWrite.getText())) { // 올바른 IP주소 형식이 입력되었다면,
					((IPLayer) m_LayerMgr.GetLayer("IP")).setDstAddr(TextWrite.getText()); // IPLayer의 dst 주소 설정
																							// IPLayer의 src 주소 설정
					try {
						byteArray = getAddr();
						((ARPLayer) m_LayerMgr.GetLayer("ARP")).setSrcIPAddr(byteArray.get(1)); // ARPLayer에 SrcAddr Set
						((ARPLayer) m_LayerMgr.GetLayer("ARP")).setSrcMAC(byteArray.get(0)); // ARpLayer에 MACAddr Set
						((ARPLayer) m_LayerMgr.GetLayer("ARP")).setDstIPAddr(strToByteArray(TextWrite.getText())); // ARpLayer에
																													// dstAddr
																													// Set
						((IPLayer) m_LayerMgr.GetLayer("IP")).setDstAddr(TextWrite.getText());
						((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterNumber(0);
					} catch (SocketException e1) {
						e1.printStackTrace();
					}
					((TCPLayer) m_LayerMgr.GetLayer("TCP")).Send("".getBytes(), 0); // Send 시작
				} else {
					System.out.println("올바른 IP주소를 입력하시오");
				}
			}
			// Proxy 이벤트
			if (e.getSource() == Add_Button) { // 우측의 Add 버튼을 클릭 시, 팝업 다이얼로그
				PopupDialog();
			}
			if (e.getSource() == Delete_Button) {
				if (ProxyArea.getSelectedValue() != null) {
					//CacheTable에서 지우는 코드도 구현되야 함.
					ProxyModel.remove(ProxyArea.getSelectedIndex());	//GUI에서 제거함
				}
			}

			if (e.getSource() == OK_Button) { // 프록시 리스트에 Item 생성
				if (TextWrite3.getText().trim().isEmpty() || TextWrite4.getText().trim().isEmpty())
					System.out.println("입력되지 않은 항목이 있습니다");
				else {
					ProxyModel.addElement(String.format("%15s%20s%23s", DeviceComboBox.getSelectedItem(),
							TextWrite3.getText(), TextWrite4.getText()));
					
					System.out.println(DeviceComboBox.getSelectedItem());	//Host 칸에 입력한 텍스트를 가져오기
					System.out.println(TextWrite3.getText());	//IP 주소칸에 입력한 텍스트를 가져오기
					System.out.println(TextWrite4.getText());	//Ethernet 주소칸에 입력한 텍스트를 가져오기
					// ARPLayer에서 만든 cache_Table을 가져온다. 2초마다 갱신하는 셈.
					cache_Table = ((ARPLayer) m_LayerMgr.GetLayer("ARP")).getCacheList();
					cache_Itr = cache_Table.keySet();
					
					PopUpFrame.dispose();
				}
			}
			if (e.getSource() == Proxy_Cancle_Button) {
				PopUpFrame.dispose(); // 팝업창을 끈다
			}
			// Grat 이벤트
			if (e.getSource() == Grat_Send_Button) {
				((ARPLayer) m_LayerMgr.GetLayer("ARP")).setSrcMAC(strToByteArray2(TextWrite2.getText()));
			}
			// 하단 버튼 이벤트
			if (e.getSource() == Exit_Button) {
				dispose();
			}
			if (e.getSource() == Cancle_Button) {
				dispose();
			}
		}
	}

	// IP
	public byte[] strToByteArray(String str) {
		byte[] bytes = new byte[4];
		StringTokenizer st = new StringTokenizer(str, ".");

		for (int i = 0; i < 4; i++)
			bytes[i] = (byte) Integer.parseInt(st.nextToken());

		return bytes;
	}

	// MAC
	public byte[] strToByteArray2(String str) {
		byte[] bytes = new byte[6];
		StringTokenizer st = new StringTokenizer(str, ":");

		for (int i = 0; i < 6; i++)
			bytes[i] = (byte) Integer.parseInt(st.nextToken());

		return bytes;
	}

	// ARPLayer의 cache_Table을 가져와서 GUI에 Print하는 함수
	@SuppressWarnings("unchecked")
	public synchronized static void printCash() {
		// ARPLayer에서 만든 cache_Table을 가져온다. Sleep초 마다 갱신하는 셈.
		cache_Table = ((ARPLayer) m_LayerMgr.GetLayer("ARP")).getCacheList();
		cache_Itr = cache_Table.keySet();
		
		//디버깅
		System.out.println("CacheTable = " + cache_Table);
		System.out.println("ARPModel = " + ARPModel);
		//디버깅
		for(String key2 : cache_Itr) {
			System.out.println(key2 + "'s ttl = " + cache_Table.get(key2).cache_ttl);
		}
		
		System.out.println(cache_Table.size() + " is cache's size ///// " + ARPModel.size() + " is ARPModel's size");
		if (cache_Table.size() != ARPModel.size()) { // 캐시테이블과 ARPModel의 사이즈가 다르면, 갱신한다.
			System.out.println("different Sizes. I'm gonna refresh.");
			ARPModel.removeAllElements(); // ARPModel의 값을 모두 지우고,
			for (String key : cache_Itr) { // 캐시테이블의 모든 값을 ARPModel에 저장하기 위해서 캐시테이블을 순회.
				ARPModel.addElement(String.format("%20s%20s%15s", // 캐시테이블의 값 중 dstIPAddr(key), dstMACaddr, status를 아래 형식으로 ARPModel에 저장
						key, 														// key는 String이기 때문에 그대로 저장.
						ethAddrToQuestionOrEth(cache_Table.get(key).cache_ethaddr), // ethAddr은 byte[]이기 때문에 ??? 혹은 xx:xx 형태의 String으로 변경해서 저장
						cache_Table.get(key).cache_status));						// status는 String이기 때문에 그대로 저장
			}
		}
	}
	
	//byte[]가 0.0.0 이면 ??? String으로 바꿔서 리턴, 0.0.0.이 아니면 정상적인 ethAddr이다. 이 ethAddr을 xx:xx:xx의 String으로 바꿔서 리턴. (GUI에 출력하기 위함)
	public static String ethAddrToQuestionOrEth(byte[] input) {
		if(isThisQuestion(input)) { // byte[]가 0000..이다 -> Question mark로 출력
			return "??????????????";
		}
		else {						// byte[]가 정상적인 ethAddr 형식이다 -> String으로 바꿔서 출력.
			return byteArrayToString(input);
		}
	}
	
	public static boolean isThisQuestion(byte[] input) {
		for(byte a : input ) {	//byte[]를 순회하는데
			if(a != 0) {		// 0 이 아닌 게 있다면,
				return false;	//  ???가 아니다.
			}
		}
		return true;
	}
	public static String byteArrayToString(byte[] b) {	// byte[0] = 1, byte[1] = 2 일 때... 01:02 String으로 변경
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; ++i) {	
        	sb.append(String.format("%02d", b[i] ));	// sb에 바이트를 str으로 바꿔서 저장한다. 이 때  1은 01, 2는 02 등 두 글자로 formatting
            sb.append(":");								
        }
        sb.deleteCharAt(sb.length()-1);	// 마지막에 추가된 :를 지운다
        return sb.toString();
    }

	public ArrayList<byte[]> getAddr() throws SocketException {
		Enumeration<NetworkInterface> interfaces = null;
		interfaces = NetworkInterface.getNetworkInterfaces(); // 현재 PC의 모든 NIC를 열거형으로 받는다.

		// isUP 중에 MAC과 IP 주소를 출력
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			if (networkInterface.isUp()) {
				byte[] mac = new byte[6];
				byte[] src_ip = new byte[4];
				if (networkInterface.getHardwareAddress() != null) { // loop back Interface가 null이므로, 걸러준다.
					mac = networkInterface.getHardwareAddress(); // MAC주소 받기
					src_ip = networkInterface.getInetAddresses().nextElement().getAddress(); // IP주소 받기

					byteArray.add(mac);
					byteArray.add(src_ip);
					return byteArray; // 현재 사용중인 NIC 이외에는 필요 없다. 탈출
				}
			}
		}
		return null;
	}

	// 팝업창 GUI
	public void PopupDialog() {
		PopUpFrame = new JFrame("Proxy ARP Entry 추가");
		PopUpFrame.setBounds(200, 200, 300, 220);
		contentPane2 = PopUpFrame.getContentPane();
		PopUpFrame.setLayout(null);
		PopUpFrame.setVisible(true);

		// "Device" 라벨과 콤보박스
		lblDevice = new JLabel("Device");
		lblDevice.setBounds(47, 10, 60, 25);
		contentPane2.add(lblDevice);

		String[] str = { "Host B", "Host C" };

		DeviceComboBox = new JComboBox<>(str);
		DeviceComboBox.setBounds(90, 10, 150, 25);
		DeviceComboBox.addActionListener(new setAddressListener());
		((JLabel) DeviceComboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER); // 텍스트를 가운데로 정렬하는 코드
		contentPane2.add(DeviceComboBox);

		// "IP주소" 라벨과 텍스트 입력 창
		lblPopUpIP = new JLabel("IP주소");
		lblPopUpIP.setBounds(47, 50, 60, 25);
		contentPane2.add(lblPopUpIP);

		TextInputPanel3 = new JPanel();// chatting write panel
		TextInputPanel3.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		TextInputPanel3.setBounds(90, 50, 150, 25);
		contentPane2.add(TextInputPanel3);
		TextInputPanel3.setLayout(null);

		TextWrite3 = new JTextField();
		TextWrite3.setBounds(2, 2, 150, 25);// 249
		TextInputPanel3.add(TextWrite3);
		TextWrite3.setHorizontalAlignment(SwingConstants.CENTER);
		TextWrite3.setColumns(10);// writing area

		// "Ethernet주소" 라벨과 텍스트 입력 창
		lblEthernet = new JLabel("Ethernet주소");
		lblEthernet.setBounds(10, 90, 80, 25);
		contentPane2.add(lblEthernet);

		TextInputPanel4 = new JPanel();// chatting write panel
		TextInputPanel4.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		TextInputPanel4.setBounds(90, 90, 150, 25);
		contentPane2.add(TextInputPanel4);
		TextInputPanel4.setLayout(null);

		TextWrite4 = new JTextField();
		TextWrite4.setBounds(2, 2, 150, 25);// 249
		TextInputPanel4.add(TextWrite4);
		TextWrite4.setHorizontalAlignment(SwingConstants.CENTER);
		TextWrite4.setColumns(10);// writing area

		// 하단 버튼 두 개
		OK_Button = new JButton("OK");
		OK_Button.setBounds(30, 130, 100, 25);
		OK_Button.addActionListener(new setAddressListener());
		contentPane2.add(OK_Button);// chatting send button

		Proxy_Cancle_Button = new JButton("Cancel");
		Proxy_Cancle_Button.setBounds(150, 130, 100, 25);
		Proxy_Cancle_Button.addActionListener(new setAddressListener());
		contentPane2.add(Proxy_Cancle_Button);// chatting send button
	}

	// 유효한 IP 형식인지 체크. 속도 저하의 주범
	public boolean isValidIPv4Addr(String ip) {
		try {
			return InetAddress.getByName(ip).getHostAddress().equals(ip);
		} catch (UnknownHostException ex) {
			return false;
		}

	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) { // 하위 레이어 설정
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) { // 상위 레이어 설정
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() { // 레이어 이름 반환
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() { // 하위 레이어 반환
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) { // 상위 레이어 반환
		// TODO Auto-generated method stub
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
