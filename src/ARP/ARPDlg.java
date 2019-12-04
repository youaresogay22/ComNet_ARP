package ARP;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import ARP.ARPLayer._Cache_Entry;
import ARP.ARPLayer._Proxy_Entry;
import ARP.RoutingTable._Routing_Entry;

// ARP cache Table을 쓰레드 없이 갱신할 수 있나? (TTL때문)
// 자료구조가 두 갈래 레이어에 적절하게 분배되고 있는지?
// 언제, 왜 GUI창이 종료되는 건지 모르겠다.

@SuppressWarnings("serial")
public class ARPDlg extends JFrame implements BaseLayer {

	public int nUnderLayerCount =0;
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<BaseLayer>();
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	private static LayerManager m_LayerMgr = new LayerManager();
	
	static Set<String> routing_Table_Itr;
	static Set<String> cache_Itr;
	static Map<String, _Cache_Entry> cache_Table;
	static Map<String, _Proxy_Entry> proxy_Table;
	static Map<String, _Routing_Entry> routing_Table; 

	JTextField TextWrite3; // 팝업 창 "IP주소"
	JTextField TextWrite4; // 팝업 창 "Ethernet주소"
	JTextField TextWrite5; // routing pop, "Destination"
	JTextField TextWrite6; // routing pop, "Netmask"
	JTextField TextWrite7; // routing pop, "Gateway"

	JComboBox<String> cBoxDevice; 	 // proxy popup, "Device" 콤보박스
	JComboBox<String> cBoxInterface; // routing popup, "Interface" 콤보박스
	
	JCheckBox checkUp;
	JCheckBox checkGate;
	JCheckBox checkHost;
	
	JFrame routingPopupFrame;
	JFrame proxyPopupFrame; // 프록시 add버튼 클릭 시 나타나는 팝업 창
	
	static JTable routingTable;
	static JTable ARPTable;
	static JTable proxyTable;

	JButton routingAddBtn;
	JButton routingDeleteBtn;
	JButton routingPopAddBtn;
	JButton routingPopCancelBtn;
	JButton ARPDeleteBtn;
	JButton proxyAddBtn; 		
	JButton proxyDeleteBtn; 	
	JButton proxyPopOkBtn; 		
	JButton proxyPopCancelBtn;		

	// JTable에 쓰이는 변수들
	String[] routingColumn = {"Destination", "NetMask", "Gateway","Flag","Interface","Metric"};
	String[] ARPColumn = {"IP Address", "Ethernet Address", "Status"};
	String[] proxyColumn = {"IP Address", "Ethernet Address", "Interface"};
	Object[][] routingData = {};
	Object[][] ARPData = {};
	Object[][] proxyData = {};
	
	static RoutingTable rtClass = new RoutingTable();
	
	public static void main(String[] args) throws IOException {
		m_LayerMgr.AddLayer(new NILayer("NI"));
		m_LayerMgr.AddLayer(new EthernetLayer("ETHERNET"));
		m_LayerMgr.AddLayer(new ARPLayer("ARP"));
		m_LayerMgr.AddLayer(new IPLayer("IP"));
		m_LayerMgr.AddLayer(new TCPLayer("TCP"));
		m_LayerMgr.AddLayer(new ARPDlg("GUI"));
		
		m_LayerMgr.AddLayer(new NILayer("NI2"));
		m_LayerMgr.AddLayer(new EthernetLayer("ETHERNET2"));
		m_LayerMgr.AddLayer(new ARPLayer("ARP2"));
		m_LayerMgr.AddLayer(new IPLayer("IP2"));
		m_LayerMgr.AddLayer(new TCPLayer("TCP2"));
				
		// old
		//m_LayerMgr.ConnectLayers("NI ( *ETHERNET ( *ARP +IP ( -ARP *TCP ) ) ) (*GUI) NI2 ( *ETHERNET2 ( *ARP2 +IP2 ( -ARP2 *TCP2 ) ) ) (*GUI) ");

		// new
		m_LayerMgr.ConnectLayers("NI ( *ETHERNET ( *ARP +IP ( -ARP *TCP ( *GUI ) ) ) ) (null) NI2 ( *ETHERNET2 ( *ARP2 +IP2 ( -ARP2 *TCP2 ( *GUI ) ) ) )");
		
		// IP 레이어에 IP2 레이어 정보를 set한다. 그 반대도 마찬가지.
		((IPLayer) m_LayerMgr.GetLayer("IP")).setOtherIPLayer((IPLayer) m_LayerMgr.GetLayer("IP2"));
		((IPLayer) m_LayerMgr.GetLayer("IP2")).setOtherIPLayer((IPLayer) m_LayerMgr.GetLayer("IP"));

		//print for debug
		//GUI에서 두 개의 TCP로 갈라져 나오는 구조. GUI.getUnderLayer(0) = TCP, GUI.getUnderLayer(1) = TCP2
		//원래는 ARP에서 Ethernet으로 일방향 연결이어야 한다. 하지만 여기서는 ARP와 Ethernet이 양방향으로 연결되게끔 구현했다.
		/*
		 * System.out.println("---------------------------------");
		 * System.out.println(m_LayerMgr.GetLayer("ETHERNET").GetUnderLayer(0).
		 * GetLayerName());
		 * System.out.println(m_LayerMgr.GetLayer("ARP").GetUnderLayer(0).GetLayerName()
		 * );
		 * System.out.println(m_LayerMgr.GetLayer("IP").GetUnderLayer(0).GetLayerName())
		 * ;
		 * System.out.println(m_LayerMgr.GetLayer("TCP").GetUnderLayer(0).GetLayerName()
		 * );
		 * System.out.println(m_LayerMgr.GetLayer("GUI").GetUnderLayer(0).GetLayerName()
		 * );
		 * System.out.println(m_LayerMgr.GetLayer("TCP").GetUpperLayer(0).GetLayerName()
		 * );
		 * 
		 * System.out.println("\n" +
		 * m_LayerMgr.GetLayer("ETHERNET2").GetUnderLayer(0).GetLayerName());
		 * System.out.println(m_LayerMgr.GetLayer("ARP2").GetUnderLayer(0).GetLayerName(
		 * ));
		 * System.out.println(m_LayerMgr.GetLayer("IP2").GetUnderLayer(0).GetLayerName()
		 * );
		 * System.out.println(m_LayerMgr.GetLayer("TCP2").GetUnderLayer(0).GetLayerName(
		 * ));
		 * System.out.println(m_LayerMgr.GetLayer("GUI").GetUnderLayer(1).GetLayerName()
		 * );
		 * System.out.println(m_LayerMgr.GetLayer("TCP2").GetUpperLayer(0).GetLayerName(
		 * )); System.out.println("---------------------------------");
		 */
		List<byte[]> list = new ArrayList<>();	//getMyisUpAdapterList()에서 isUp중인 네트워크 인터페이스 카드의 MAC주소 리스트를 담기 위한것
		list = getMyisUpAdapterList(list);		// 자료구조 list에 isUp중인 NIC의 MAC주소를 담는다
		
		((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterNumber(list,0); 	// NIC의 MAC 주소 list에서 0번째에 있는 것으로 set하겠다
		((NILayer) m_LayerMgr.GetLayer("NI2")).SetAdapterNumber(list,1);
		
		// 2초 마다 printCash()를 호출하여 캐시 테이블과 GUI를 갱신하는 쓰레드
		Runnable task = () -> {
			while (true) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				printCache(); // GUI에 cache_Table을 print
			}
		};
		//위 기능을 수행하는 쓰레드 생성 및 시작.
		Thread cacheUpdate = new Thread(task,"cacheUpdataThread");
		cacheUpdate.start();
		
		// 2초마다 gratSend()를 호출하는 함수
		Runnable GARPTask = () -> {
			while (true) {
				try {
					String threadName = Thread.currentThread().getName();
					System.out.println("Hello " + threadName);
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				runGratSend();
			}
		};

		Thread runGARP = new Thread(GARPTask, "runningGARP");
		runGARP.start();
		
		//프로그램 구동 초기에 null을 참조해서 에러나는 경우를 방지하기 위해 초기화
		proxy_Table = ((ARPLayer) m_LayerMgr.GetLayer("ARP")).getProxyList();
		routing_Table = rtClass.getRoutingList();
	}
	
	public static void runGratSend() {
		byte[] myMAC = new byte[6];
		// ARPLayer가 생성되고 난 뒤에(== getMyPCAddr()함수가 호출된 다음에) 쓰레드가 start..
		myMAC = ((ARPLayer) m_LayerMgr.GetLayer("ARP")).getMY_MAC_ADDRESS();
		((ARPLayer) m_LayerMgr.GetLayer("ARP")).setSrcMAC(myMAC);
		((TCPLayer) m_LayerMgr.GetLayer("TCP")).GratSend("".getBytes(), 0);
		
		byte[] myMAC2 = new byte[6];
		myMAC2 = ((ARPLayer) m_LayerMgr.GetLayer("ARP2")).getMY_MAC_ADDRESS();
		((ARPLayer) m_LayerMgr.GetLayer("ARP2")).setSrcMAC(myMAC2);
		((TCPLayer) m_LayerMgr.GetLayer("TCP2")).GratSend("".getBytes(), 0);
	}
	
	//1.TTL로 제거되는 ARP Cache entry를 GUI에 실시간으로 반영해야 한다. 
	//2.이를 위해 Sleep초 마다 ARPLayer에서 자료구조(new)를 가져온다.
	//3.GUI에 출력되어 있는 entry(old)를 삭제한 뒤, 새로 가져온 자료구조를 GUI에 출력한다.
	public static void printCache() {
		//old -> new
		cache_Table = ((ARPLayer) m_LayerMgr.GetLayer("ARP")).getCacheList();
		cache_Itr = cache_Table.keySet();
		
		//GUI에서 삭제 (ARP)
		DefaultTableModel arpModel = (DefaultTableModel) ARPTable.getModel();
		int rowCount = arpModel.getRowCount();
		for(int i=rowCount - 1 ; i >= 0; i--)
			arpModel.removeRow(i);
		
		//GUI에 출력 (ARP)
		for(String key : cache_Itr) {
			Object[] data = {
					key,																//IP Address
					byteArrayToQuestionOrEth(cache_Table.get(key).cache_ethaddr),		//Ethernet Address
					cache_Table.get(key).cache_status									//Status
			};
			arpModel.addRow(data);				
		}
	}
	
	public ARPDlg(String pName) {
		pLayerName = pName;

		setTitle("TestARP");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(180, 180, 850, 420);
		Container contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		////////////////////// 좌측 Routing Table ***********************************************
		JPanel routingPanel = new JPanel();
		//routingPanel.setBorder(new LineBorder(Color.RED));
		routingPanel.setBounds(5,5,450,380);
		contentPane.add(routingPanel);
		
		JTextArea routingTitle = new JTextArea("Static Routing Table");
		routingTitle.setBackground(null);
		routingTitle.setFont(new Font("Serif", Font.BOLD, 20));
		routingTitle.setBounds(100, 4, 160, 30);
		routingTitle.setEditable(false);												// 수정 불가
		routingTitle.setHighlighter(null);												// 드래그 불가
		routingPanel.add(routingTitle);
		
		routingTable = new JTable(makeDefaultTableModel(routingData,routingColumn));
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();		// JTable 가운데 정렬
		centerRenderer.setHorizontalAlignment( SwingConstants.CENTER );					// JTable 가운데 정렬
		routingTable.setDefaultRenderer(Object.class, centerRenderer);					// JTable 가운데 정렬
		routingTable.setShowGrid(false);												// JTable 내 경계선 제거
		routingTable.setFillsViewportHeight(true);										// JTable 내 data가 비어있더라도 size 만큼 가득 채워서 보여줌
		
		routingTable.getColumnModel().getColumn(0).setPreferredWidth(95);				// Destination size
		routingTable.getColumnModel().getColumn(1).setPreferredWidth(95);				// NetMask
		routingTable.getColumnModel().getColumn(2).setPreferredWidth(95);				// Gateway
		routingTable.getColumnModel().getColumn(3).setPreferredWidth(40);				// Flag
		routingTable.getColumnModel().getColumn(4).setPreferredWidth(70);				// Interface
		routingTable.getColumnModel().getColumn(5).setPreferredWidth(40);				// Metric
		
		JScrollPane routingScrollPane = new JScrollPane(routingTable);
		routingScrollPane.setPreferredSize(new Dimension(445,285));						// JScrollPane 크기 지정
		routingPanel.add(routingScrollPane);
		
		routingAddBtn = new JButton("Add");
		routingAddBtn.setBounds(50, 130, 110, 25);
		routingAddBtn.addActionListener(new setAddressListener());
		routingPanel.add(routingAddBtn);

		routingDeleteBtn = new JButton("Delete");
		routingDeleteBtn.setBounds(190, 130, 110, 25);
		routingDeleteBtn.addActionListener(new setAddressListener());
		routingPanel.add(routingDeleteBtn);
		
		////////////////////// 우측 ARP ********************************************
		JPanel ARPPanel = new JPanel();
		//ARPPanel.setBorder(new LineBorder(Color.RED));
		ARPPanel.setBounds(470, 5, 355, 190);
		contentPane.add(ARPPanel);
		
		JTextArea ARPTitle = new JTextArea("ARP Cache Table");
		ARPTitle.setBackground(null);
		ARPTitle.setFont(new Font("Serif",Font.BOLD, 20));
		ARPTitle.setBounds(100, 4, 160, 30);
		ARPTitle.setEditable(false);												// 수정 불가
		ARPTitle.setHighlighter(null);												// 드래그 불가
		ARPPanel.add(ARPTitle);

		ARPTable = new JTable(makeDefaultTableModel(ARPData,ARPColumn));
		ARPTable.setDefaultRenderer(Object.class, centerRenderer);					// JTable 가운데 정렬
		ARPTable.setShowGrid(false);												// JTable 내 경계선 제거
		ARPTable.setFillsViewportHeight(true);										// JTable 내 data가 비어있더라도 size 만큼 가득 채워서 보여줌
		
		ARPTable.getColumnModel().getColumn(0).setPreferredWidth(100);				// IP Address size
		ARPTable.getColumnModel().getColumn(1).setPreferredWidth(140);				// Ethernet Address
		ARPTable.getColumnModel().getColumn(2).setPreferredWidth(105);				// Status

		JScrollPane ARPScrollPane = new JScrollPane(ARPTable);
		ARPScrollPane.setPreferredSize(new Dimension(345,100));						// JScrollPane 크기 지정
		ARPPanel.add(ARPScrollPane);

		ARPDeleteBtn = new JButton("Delete");
		ARPDeleteBtn.setBounds(115, 130, 110, 25);
		ARPDeleteBtn.addActionListener(new setAddressListener());
		ARPPanel.add(ARPDeleteBtn);

		/////////////////// 우측  PROXY ************************************************************************************
		JPanel ProxyPanel = new JPanel();
		//ProxyPanel.setBorder(new LineBorder(Color.RED));
		ProxyPanel.setBounds(470, 190, 355, 190);
		contentPane.add(ProxyPanel);
		
		JTextArea proxyTitle = new JTextArea("Proxy ARP Table");
		proxyTitle.setBackground(null);
		proxyTitle.setFont(new Font("Serif",Font.BOLD, 20));
		proxyTitle.setEditable(false);												// 수정 불가
		proxyTitle.setHighlighter(null);											// 드래그 불가
		proxyTitle.setBounds(100, 4, 160, 30);
		ProxyPanel.add(proxyTitle);
		
		proxyTable = new JTable(makeDefaultTableModel(proxyData,proxyColumn));
		proxyTable.setDefaultRenderer(Object.class, centerRenderer);				// JTable 가운데 정렬			
		proxyTable.setShowGrid(false);												// JTable 내 경계선 제거
		proxyTable.setFillsViewportHeight(true); 									// JTable 내 data가 비어있더라도 size 만큼 가득 채워서 보여줌
		
		proxyTable.getColumnModel().getColumn(0).setPreferredWidth(105);			// IP Address size
		proxyTable.getColumnModel().getColumn(1).setPreferredWidth(160);			// Ethernet Address
		proxyTable.getColumnModel().getColumn(2).setPreferredWidth(80);				// Interface
		
		JScrollPane proxyScrollPane = new JScrollPane(proxyTable);
		proxyScrollPane.setPreferredSize(new Dimension(345,100));					// JScrollPane 크기 지정
		ProxyPanel.add(proxyScrollPane);

		proxyAddBtn = new JButton("Add");
		proxyAddBtn.setBounds(50, 130, 110, 25);
		proxyAddBtn.addActionListener(new setAddressListener());
		ProxyPanel.add(proxyAddBtn);

		proxyDeleteBtn= new JButton("Delete");
		proxyDeleteBtn.setBounds(190, 130, 110, 25);
		proxyDeleteBtn.addActionListener(new setAddressListener());
		ProxyPanel.add(proxyDeleteBtn);

		setVisible(true);
	}

	// proxy add 팝업창 GUI
	public void proxyPopup() {
		proxyPopupFrame = new JFrame("Proxy ARP Entry 추가");
		proxyPopupFrame.setBounds(200, 200, 300, 220);
		proxyPopupFrame.setLayout(null);
		proxyPopupFrame.setVisible(true);

		// "Device" 라벨과 콤보박스
		JLabel lblDevice = new JLabel("Device");
		lblDevice.setBounds(47, 10, 60, 25);
		proxyPopupFrame.add(lblDevice);

		String[] str = { "Host B", "Host C" };

		cBoxDevice = new JComboBox<>(str);
		cBoxDevice.setBounds(90, 10, 150, 25);
		cBoxDevice.addActionListener(new setAddressListener());
		((JLabel) cBoxDevice.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER); // 텍스트를 가운데로 정렬하는 코드
		proxyPopupFrame.add(cBoxDevice);

		// "IP주소" 라벨과 텍스트 입력 창
		JLabel lblPopUpIP = new JLabel("IP주소");
		lblPopUpIP.setBounds(47, 50, 60, 25);
		proxyPopupFrame.add(lblPopUpIP);

		TextWrite3 = new JTextField();
		TextWrite3.setBounds(90, 50, 150, 25);// 249
		TextWrite3.setHorizontalAlignment(SwingConstants.CENTER);
		TextWrite3.setColumns(10);
		proxyPopupFrame.add(TextWrite3);
		TextWrite3.setText("168.188.129.4"); 	//디버깅
		
		// "Ethernet주소" 라벨과 텍스트 입력 창
		JLabel lblEthernet = new JLabel("Ethernet주소");
		lblEthernet.setBounds(10, 90, 80, 25);
		proxyPopupFrame.add(lblEthernet);

		TextWrite4 = new JTextField();
		TextWrite4.setBounds(90, 90, 150, 25);// 249
		TextWrite4.setHorizontalAlignment(SwingConstants.CENTER);
		TextWrite4.setColumns(10);
		proxyPopupFrame.add(TextWrite4);
		TextWrite4.setText("06:05:04:03:02:01"); 	//디버깅

		// 하단 버튼 두 개
		proxyPopOkBtn = new JButton("OK");
		proxyPopOkBtn.setBounds(30, 130, 100, 25);
		proxyPopOkBtn.addActionListener(new setAddressListener());
		proxyPopupFrame.add(proxyPopOkBtn);

		proxyPopCancelBtn = new JButton("Cancel");
		proxyPopCancelBtn.setBounds(150, 130, 100, 25);
		proxyPopCancelBtn.addActionListener(new setAddressListener());
		proxyPopupFrame.add(proxyPopCancelBtn);
	}
	
	// routing add 팝업창 GUI
	public void routingPopup() {
		routingPopupFrame = new JFrame("Routing Table Entry 추가");
		routingPopupFrame.setBounds(400, 300, 330, 240);
		routingPopupFrame.setLayout(null);
		routingPopupFrame.setVisible(true);

		// "Destination" 라벨과 텍스트 입력 창
		JLabel lblDest = new JLabel("Destination");
		lblDest.setBounds(30, 10, 70, 25);
		routingPopupFrame.add(lblDest);

		TextWrite5 = new JTextField();
		TextWrite5.setBounds(110, 10, 170, 25);
		TextWrite5.setHorizontalAlignment(SwingConstants.CENTER);
		TextWrite5.setColumns(10);
		routingPopupFrame.add(TextWrite5);
		TextWrite5.setText("192.168.10.0"); // 디버깅

		// "Netmask" 라벨과 텍스트 입력 창
		JLabel lblNetMask = new JLabel("Netmask");
		lblNetMask.setBounds(30, 40, 70, 25);
		routingPopupFrame.add(lblNetMask);

		TextWrite6 = new JTextField();
		TextWrite6.setBounds(110, 40, 170, 25);
		TextWrite6.setHorizontalAlignment(SwingConstants.CENTER);
		TextWrite6.setColumns(10);
		routingPopupFrame.add(TextWrite6);
		TextWrite6.setText("255.255.255.0"); // 디버깅

		// "Gateway" 라벨과 텍스트 입력 창
		JLabel lblGate = new JLabel("Gateway");
		lblGate.setBounds(30, 70, 150, 25);
		routingPopupFrame.add(lblGate);

		TextWrite7 = new JTextField();
		TextWrite7.setBounds(110, 70, 170, 25);
		TextWrite7.setHorizontalAlignment(SwingConstants.CENTER);
		TextWrite7.setColumns(10);
		routingPopupFrame.add(TextWrite7);
		TextWrite7.setText("192.168.20.0"); // 디버깅

		// "Flag" 라벨과 체크박스
		JLabel lblFlag = new JLabel("Flag");
		lblFlag.setBounds(30, 100, 150, 25);
		routingPopupFrame.add(lblFlag);

		checkUp = new JCheckBox("UP");
		checkUp.setBounds(110, 100, 45, 25);
		routingPopupFrame.add(checkUp);

		checkGate = new JCheckBox("Gateway");
		checkGate.setBounds(160, 100, 80, 25);
		routingPopupFrame.add(checkGate);

		checkHost = new JCheckBox("Host");
		checkHost.setBounds(240, 100, 60, 25);
		routingPopupFrame.add(checkHost);

		// "Interface" 라벨과 콤보박스
		JLabel lblInterface = new JLabel("Interface");
		lblInterface.setBounds(30, 130, 150, 25);
		routingPopupFrame.add(lblInterface);

		String[] str = { "1", "2" };

		cBoxInterface = new JComboBox<>(str);
		cBoxInterface.setBounds(110, 130, 100, 25);
		cBoxInterface.addActionListener(new setAddressListener());
		cBoxInterface.setBackground(Color.WHITE);
		((JLabel) cBoxInterface.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER); // 텍스트를 가운데로 정렬하는 코드
		routingPopupFrame.add(cBoxInterface);

		// 하단 버튼 두 개
		routingPopAddBtn = new JButton("Add");
		routingPopAddBtn.setBounds(80, 165, 80, 25);
		routingPopAddBtn.addActionListener(new setAddressListener());
		routingPopupFrame.add(routingPopAddBtn);

		routingPopCancelBtn = new JButton("Cancel");
		routingPopCancelBtn.setBounds(180, 165, 80, 25);
		routingPopCancelBtn.addActionListener(new setAddressListener());
		routingPopupFrame.add(routingPopCancelBtn);
	}

	///////////////// 버튼 클릭 이벤트 *******************************************
	class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Routing Table 이벤트
			if (e.getSource() == routingAddBtn)
				routingPopup();
			if (e.getSource() == routingDeleteBtn) 				// 선택한 routing entry를 삭제
				deleteSelectedEntry(routingTable, routing_Table);
			if (e.getSource() == routingPopAddBtn)
				addNewEntry(routingTable);						// 입력한 값을 토대로 add new entry
			if (e.getSource() == routingPopCancelBtn)
				routingPopupFrame.dispose(); 					// 팝업창을 끈다		
			// ARP 이벤트
			if (e.getSource() == ARPDeleteBtn) 					// 선택한 ARP entry를 삭제
				deleteSelectedEntry(ARPTable, cache_Table);		// JTable과 <T extends Map<?, ?>>를 인자로 갖는 함수.
			// Proxy 이벤트
			if (e.getSource() == proxyAddBtn)
				proxyPopup();
			if (e.getSource() == proxyDeleteBtn)  				// 선택한 proxy entry를 삭제
				deleteSelectedEntry(proxyTable, proxy_Table);	
			if (e.getSource() == proxyPopOkBtn) 				// proxy ARP Table에 Item 생성
				addNewEntry(proxyTable);						// 입력한 값을 토대로 add new entry	
			if (e.getSource() == proxyPopCancelBtn) 
				proxyPopupFrame.dispose(); 						// 팝업창을 끈다
		}
	}
	
	// popup에서 값을 입력후 Add버튼을 눌렀을 때 자료구조와 GUI에 entry를 추가하는 함수
	public void addNewEntry(JTable table) {
		if(table == routingTable) {
			if(!routing_Table.containsKey(TextWrite5.getText())) { // 라우팅 테이블에 중복되는 값이 없을 때만 entry를 추가한다
				//자료구조에 추가
				rtClass.addRoutingEntry(
						TextWrite5.getText(), 										// Dest IP (Key, String)
						strIPToByteArray(TextWrite6.getText()),						// NetMask (byte[])
						TextWrite7.getText(),										// Gateway (String)
						isCheckUpTrue(),											// up Flag (boolean)
						isCheckGateTrue(),											// gateway Flag (boolean)
						Integer.parseInt((String) cBoxInterface.getSelectedItem())	// Interface (int)
						);
				// 자료구조에 추가할 때 (addRoutingEntry) IPLayer에서 sorting한다. 따라서 Sorting된 routingTable 자료구조를 다시 get해야 함
				routing_Table = rtClass.getRoutingList();
				routing_Table_Itr = routing_Table.keySet();
				// GUI에 추가
				DefaultTableModel routingModel = (DefaultTableModel) routingTable.getModel();
				int rowCount = routingModel.getRowCount();
				for(int i=rowCount - 1 ; i >= 0; i--)
					routingModel.removeRow(i);											// GUI에서 모든 entry 삭제
				for(String key : routing_Table_Itr) {
					Object[] data = {
							key, 														 // IP Address
							byteArrayToStringIP(routing_Table.get(key).getSubnetMask()), // NetMask
							routing_Table.get(key).getGateway(), 						 // Gateway
							getFlag2(routing_Table.get(key).isFlag_Up(),
									routing_Table.get(key).isFlag_Gateway()), 			 // Flag
							routing_Table.get(key).getRoute_Interface(),				 // Interface
							"0" 														 // Metric
					};
					routingModel.addRow(data); 											 // GUI에 재출력
				}
				routingPopupFrame.dispose();
			}
		}else if(table == proxyTable) {
			 if(!proxy_Table.containsKey(TextWrite3.getText())) {			// 프록시 테이블에 중복되는 값이 없을 때만 entry를 추가한다
					//자료구조에 추가
				 	((ARPLayer) m_LayerMgr.GetLayer("ARP")).setProxyTable( 	
							TextWrite3.getText(), 							// argu1 : proxy_Table의 key로 지정할, IP 주소칸에 입력한 텍스트
							strMACToByteArray(TextWrite4.getText()), 		// argu2 : proxy_Table의 value로 지정할, Ethernet 주소칸에 입력한 텍스트
							cBoxDevice.getSelectedItem().toString()			// argu3 : proxy_Table의 value로 지정할, Combobox 에 선택된 값
							); 	
				 	//GUI에 추가
					Object[] data = { 
							TextWrite3.getText(), 
							TextWrite4.getText(),
							cBoxDevice.getSelectedItem().toString() 
							};
					DefaultTableModel model = (DefaultTableModel) table.getModel(); 	// proxy JTable의 model을 가져온다
					model.addRow(data); 												// model에 추가 (GUI에 출력)
					proxyPopupFrame.dispose();
			 }
		}
	}
	
	static public List<byte[]> getMyisUpAdapterList(List<byte[]> list) throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); // 현재 PC의 모든 NIC를 열거형으로 받는다.

		// isUP 중에 MAC과 IP 주소를 출력
		System.out.println("----------- GUI : getMyisUpAdapterList() --------------");
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();
			if (networkInterface.isUp()) {									// isUp중인 NIC
				if (networkInterface.getHardwareAddress() != null) {		// loop back Interface가 null이므로, 걸러준다.
					list.add(networkInterface.getHardwareAddress());		// list에 MAC 주소 추가
					
					System.out.printf("%s -> %s \n ", networkInterface.getName(), networkInterface.getDisplayName());
					System.out.printf("%d %d %d %d %d %d\n", networkInterface.getHardwareAddress()[0],
							networkInterface.getHardwareAddress()[1], networkInterface.getHardwareAddress()[2],
							networkInterface.getHardwareAddress()[3], networkInterface.getHardwareAddress()[4],
							networkInterface.getHardwareAddress()[5]);

					System.out.println(networkInterface.getInetAddresses().nextElement());
					System.out.println();
				}
			}
		}
		return list;
	}
	
	// 1.아래 함수는 GUI에서 entry를 선택하고 Delete버튼을 눌렀을 시 작동한다. 선택한 entry를 자료구조와 GUI에서 삭제하는 기능의 함수다.
	// 2.자료구조에서 삭제하려면 HashMap이 필요하고, GUI에서 삭제하려면 JTable이 필요하다. 그래서 이 함수는 HashMap과 JTable을 인자로 받는다.
	// 3.그런데 routing,ARP,proxy의 HashMap 구조가 각기 다르다는 문제가 있다. 
	// cache_Table은 Map<String, _Cache_Entry>이고, proxy_Table은 Map<String, _proxy_Entry>이다.
	// 즉, Map의 두번째 인자가 다르다.
	// 4.이를 해결하기 위해 제네릭을 사용. T는 상황에 따라서 Map<String, _Cache_Entry>가 될 수 있고 Map<String _Proxy_Entry>가 될 수도 있다.
	public <T extends Map<String, ?>> void deleteSelectedEntry(JTable table, T cache) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		String key = (String) model.getValueAt(table.getSelectedRow(), 0);
		if(cache.containsKey(key)) {
			cache.remove(key);								// 자료구조에서 삭제
			model.removeRow(table.getSelectedRow());		// GUI에서 삭제
		}
	}
	public boolean isCheckUpTrue() {
		if(checkUp.isSelected())
			return true;
		else
			return false;
	}
	
	public boolean isCheckGateTrue() {
		if(checkGate.isSelected())
			return true;
		else
			return false;
	}
	
	// routing popup에서 선택된 Flag 값에 알맞은 값을 반환 (unused?)
	public String getFlag() {
		if(checkUp.isSelected()) {
			if(checkGate.isSelected()) 
				return "UG";
			if(checkHost.isSelected()) 
				return "invalid";
			return "U";
		}
		return "invalid";
	}
	//자료구조의 flag값을 참조
	public String getFlag2(boolean up, boolean gate) {
		if(up) {				// 자료구조 내부의 flag_up 변수가 True
			if(gate)			// 자료구조 내부의 flag_gateway 변수가 True
				return "UG";
			else
				return "U";
		}
		return "invalid";
	}
	// defaultTableModel을 만들어서 반환하는 함수
	public static DefaultTableModel makeDefaultTableModel(Object[][] data, String[] column) {
		DefaultTableModel model = new DefaultTableModel(data, column) {
			@Override
			public boolean isCellEditable(int row, int column) { // JTable 내 Cell을 수정 불가능하게 함
				return false; 									 // all cells false
			}
		};
		return model;
	}
	// String IP -> byte[] IP
	// xxx.xxx.xxx.xxx -> byte[]
	public byte[] strIPToByteArray(String str) {
		byte[] bytes = new byte[4];
		StringTokenizer st = new StringTokenizer(str, ".");

		for (int i = 0; i < 4; i++)
			bytes[i] = (byte) Integer.parseInt(st.nextToken());

		return bytes;
	}

	// String[] MAC -> byte[] MAC
	// XX:XX:XX:XX:XX:XX -> byte[]
	public byte[] strMACToByteArray(String str) {
		byte[] byteMACAddr = new byte[6];
		String[] byte_dst = str.split(":");

		for (int i = 0; i < 6; i++) 
			byteMACAddr[i] = (byte) Integer.parseInt(byte_dst[i], 16);

		return byteMACAddr;
	}

	// byte[] 를 경우에 따라서 "???" 또는 "AA:AA:AA:AA:AA:AA"로 GUI에 출력한다
	public static String byteArrayToQuestionOrEth(byte[] input) {
		if (isThisQuestion(input))  		// byte[]가 0000..이다 -> Question mark로 출력
			return "???????????";
		 else  								// byte[]가 정상적인 ethAddr 형식이다 -> String으로 바꿔서 출력.
			return byteArrayToStringMAC(input);
	}

	public static boolean isThisQuestion(byte[] input) {
		for (byte a : input) { 			// byte[]를 순회하는데
			if (a != 0)  				// 0 이 아닌 게 있다면,
				return false; 			// ???가 아니다.
		}
		return true;
	}

	public static String byteArrayToStringMAC(byte[] b) { // byte[0] = 1, byte[1] = 2 일 때... 01:02 String으로 변경
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; ++i) {
			sb.append(String.format("%02X", b[i])); // sb에 바이트를 str으로 바꿔서 저장한다. 이 때 1은 01, 2는 02 등 두 글자로 formatting
			sb.append(":");
		}
		sb.deleteCharAt(sb.length() - 1); // 마지막에 추가된 :를 지운다
		return sb.toString();
	}
	
	public static String byteArrayToStringIP(byte[] bytes) {
		String ipAddr = "";
		for (byte b : bytes) {
			ipAddr += Integer.toString(b & 0xFF) + ".";
		}
		return ipAddr.substring(0, ipAddr.length() - 1);
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
