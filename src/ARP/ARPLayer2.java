package ARP;

import java.util.ArrayList;

public class ARPLayer2 implements BaseLayer {
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private class _ARP_HEADER {
		byte[] arp_hdType; // Ethernet = 1
		byte[] arp_prototype; // IP = 0x0800 ARP = 0x0806. 본 과제에서는 IP만 쓴다? 02강 05:40
		byte arp_hdLength; // MAC address = 6
		byte arp_protoLength; // IP address = 4
		byte[] arp_op; // request = 1, reply = 2
		byte[] arp_srcHdAddr; // Sender's Ethernet Address
		byte[] arp_srcProtoAddr;// Sender's IP Address
		byte[] arp_destHdAddr; // 수신자 MAC 주소
		byte[] arp_destProtoAddr; // 수신자 IP주소

		public _ARP_HEADER() {
			this.arp_hdType = new byte[2];
			this.arp_prototype = new byte[2];
			this.arp_hdLength = (byte) 0x00;
			this.arp_protoLength = (byte) 0x00;
			this.arp_op = new byte[2];
			this.arp_srcHdAddr = new byte[4];
			this.arp_srcProtoAddr = new byte[6];
			this.arp_destHdAddr = new byte[4];
			this.arp_destProtoAddr = new byte[6];
		}
	}

	_ARP_HEADER m_aHeader = new _ARP_HEADER();

	public ARPLayer2(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
<<<<<<< HEAD
		//ResetHeader();
		
=======
		ResetHeader();

>>>>>>> branch 'master' of https://github.com/youaresogay22/ComNet_ARP.git
		// 캐시테이블 자동 제거 스레드
	}

	public void close() {
		// TTL 스레드 종료
	}

	public void ResetHeader() {
		for (int i = 0; i < 4; i++) {
			m_aHeader.ip_src.addr[i] = (byte) 0x00;
			m_aHeader.ip_dst.addr[i] = (byte) 0x00;
		}
		for (int i = 0; i < 2; i++) {
			m_aHeader.ip_len[i] = (byte) 0x00;
			m_aHeader.ip_id[i] = (byte) 0x00;
			m_aHeader.ip_fragoff[i] = (byte) 0x00;
			m_aHeader.ip_cksum[i] = (byte) 0x00;
		}
		m_aHeader.ip_verlen = (byte) 0x00;
		m_aHeader.ip_tos = (byte) 0x00;
		m_aHeader.ip_ttl = (byte) 0x00;
		m_aHeader.ip_proto = (byte) 0x00;

		m_aHeader.ip_data = null;
	}

	// 출발주소 복사(MAC)주소
	public void SetSourceHdAddress(byte[] src_addr) {
		// System.arraycopy();
	}

	// 5. 출발 주소의 IP주소 복사
	public void SetSourceProtoAddress(byte[] src_addr) {
		// System.arraycopy();
	}

	// 도착 주소 복사(Mac주소)
	public void SetDestinHdAddress(byte[] src_addr) {
		// System.arraycopy();
	}

	// 도착 주소 복사(ip주소)
	public void SetDestinProtoAddress(long src_addr) {
		// System.arraycopy();
	}

	// 출발 주소 반환(Mac)주소
//	public byte GetSourceHdAddress() {
//		// return;// m_sHeader.arp_srchdaddr;
//	}
//
//	// 출발 주소 반환(IP주소)
//	public byte GetSourceProtoAddress()
//	{
//		return // m_sHeader.arp_srcprotoaddr;
//	}
//
//	// 목적지주소 반환(Mac주소)
//	public byte GetDestinHdAddress()
//	{
//		return //m_sHeader.arp_desthdaddr;
//	}
//
//	// 목적지 주소 반환(IP주소)
//	public byte GetDestinProtoAddress()
//	{
//		return //m_sHeader.arp_destprotoaddr;
//	}
//
//	// Cachelist반환
//	public LPCACHE_LIST GetCacheList() {
//		return cache_header;
//	}
//
//	// Proxylist 반환
//	public LPPROXY_LIST GetProxyList() {
//		return proxy_header;
//	}
//
//	// 캐시, 프록시 리스트에 대한 Add, Delete관련 함수
//
//	// Cache목록에 추가
//	public void AddCache(LPCACHE_LIST cache_element) {
//		// 캐시 엔트리를 추가할때는 헤드 뒤에 붙인다.
//	}
//
//	// Proxy목록에 추가
//	public void AddProxy(LPPROXY_LIST proxy_element) {
//		// 프록시 엔트리를 추가할때는 헤드 뒤에 붙인다.
//	}
//
//	// 캐시목록에서 제거
//	public boolean DeleteCache(long ipaddr) {
//	//캐시 헤더를 before로, 헤더의 다음을 current로 설정
//				
//		while(current != null) {//현재 리스트가 없을때까지 계속 반복
//			if () {//현재 제거할 주소와 일치한다면
//					// 제거할 주소 발견시 이전 노드의 next에 제거할 주소의 다음 노드 연결, 그리고 제거
//			}
//					
//		}
//				//포인터를 다음으로 옮겨준다
//				if (current == null)//현재 포인터가 NULL이라면 지울것이 없으므로 false 반환
//				{
//					return false;
//				}
//			}
//
//	// 프록시 목록에서 제거
//	public boolean DeleteProxy(long ipaddr) {
//				//프록시 목록의 시작으로 current를 설정
//				while(current != null) {//현재 리스트가 없을때까지 계속 반복
//					if () {//현재 제거할 주소와 일치한다면
//						// 제거할 주소 발견시 이전 노드의 next에 제거할 주소의 다음 노드 연결, 그리고 제거
//					}
//					//포인터를 다음으로 옮겨준다
//				}
//				if (current == null)//현재 포인터가 NULL이라면 지울것이 없으므로 false 반환
//				{
//				return false;
//				}
//			}
//
//	// 모든 캐시 제거
//	public void AllDeleteCache() {
//		// 지울 포인터를 임시로 저장해놓을 것을 설정하고, 캐시헤더의 다음을 temp_list로 설정
//		// 캐시 헤더의 다음을 NULL로 설정
//
//		while (temp_list != null)// 리스트의 마지막까지 반복해서
//		{
//			// 리스트를 돌며 모든 entry 제거
//		}
//	}
//
//	// 모든 프록시 제거
//	public void AllDeleteProxy() {
//		// 지울 포인터를 임시로 저장해놓을 것을 설정하고, 프록시헤더의 다음을 temp_list로 설정
//		// 프록시 헤더의 다음을 NULL로 설정
//
//		while (temp_list != null) {
//			// 리스트를 돌며 모든 entry 제거
//		}
//	}
//
//	// ARP메세지 보내기
//	public boolean Send(byte[] input, int length, int type) {
//			
//		boolean bSuccess; // 성공 여부
//		
//		for (int i=0; i<4; i++) { // 브로드캐스트 검사 루틴, IP주소로 판단
//			// 브로드캐스트 아닐 경우 break, 255가 아닐 경우 바로 break
//			if () { 
//				break;
//			}
//			// 브로드캐스트일 경우, 전부 255일 경우
//			if (i==4) { 
//				byte[] broadcast = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};	// 주소 ff-ff-ff-ff-ff-ff로 세팅
//				// 이더넷 레이어에 목적지 주소 세팅
//				// 데이터 전송
//				return bSuccess;
//			}
//		}
//		
//		// 브로드캐스트가 아닐 경우 일치하는 주소 탐색
//		
//		// 리스트 끝까지 찾음, seekList:캐시리스트
//		while (seekList.hasNext()) {		
//			// 캐시리스트에 ARP목적지 주소가 있고
//			if () {
//				// 그 캐시 타입이 incomplete면
//				if () {
//					return false;	// false를 반환한다.
//				}
//				// incomplete가 아니면
//				else {
//					// 주소 세팅하고 Ethernet으로 데이터 전송
//					return bSuccess;
//				}
//			}
//			seekList = seekList.next();
//		}
//			
//		// List내에 없을 경우, SeekAddress메소드를 통해 목적 IP주소를 기준으로 탐색
//		if (SeekAddress()==true) {
//			// 검색 성공 시 데이터 전송
//			return bSuccess;
//		}	
//		
//		return false; // SeekAddress 실패
//	}
//
//	// 리스트에 주소가 없을 시, IP주소를 가지고 MAC주소를 탐색하는 메소드
//	public boolean SeekAddr() {
//		// 시작 IP주소와 목적지 IP주소가 같지 않은 경우 실행
//		if () {
//			while (seekList.hasNext()) {
//				// seekList 캐시의 IP주소와 ARP의 목적지 주소가 일치할 경우, 일단 incomplete로 넣음
//				if () {
//					// type을 incomplete로 바꾼다.
//					break;
//				}
//				
//				seekList = seekList.next();
//			}
//			// seekList가 NULL이라면
//			if(seekList == NULL) {
//				// 즉 찾고자하는 정보가 캐시에 없다면 추가한다.
//				// List에 incomplete 임시 생성
//			}
//		}
//
//		int try_cnt;	
//		// 몇 번 정도 반복적으로 검색 실시
//		for (try_cnt = 0; try_cnt < 3; try_cnt++) {
//			
//			int seekAddrMsg = 0; // 메세지 도착 여부
//
//			// ARP 요청 메세지를 한번 보내고서 그에 대한 응답 메시지를 기다리는 몇 초 정도 응답을 기다림
//			// ARP Timer 작동
//			// ARP 요청 메시지
//			
//			while (seekAddrMsg == 0);		// ARP 응답 메시지가 도착하거나 Timeout 될때까지 대기
//
//			if (seekAddrMsg == 1)
//				break;						// ARP 응답 메시지 도착
//			else if (seekAddrMsg == 2)
//				continue;					// Timeout
//		}
//
//		if (try_cnt == 3)
//			return false;					// 검색시도 3회 초과시 false 반환
//
//		return true;
//	}
//
//	public boolean Receive(byte[] input) {
//
//		// ARP의 opCode 값이 1이면 request
//
//		// arp 도착지 주소와 자신의 ip주소가 동일 한 경우
//		// cachelist가 비어있지 않은 경우
//		// 받아온 패킷의 ip 주소와 캐시 리스트 내에 일치하는 ip주소가 있으면
//		// cache table에 저장한다
//		// 캐시 리스트를 생성하고 각 mac주소와 ip주소를 저장한다.
//
//		// ARP의 opCode 값이 2이면 reply
//		return false;
//	}
//
//	// 캐시테이블의 데이터 삭제에 관여하는 스레드
//	public int TTLThread() {
//		// 지정된 시간마다 테이블을 갱신하면서
//		// static은 영구보관하고,dynamic은 ttl이 0이되면 저장목록에서 제거한다
//		return 0;
//	}
//
//	// 두 개의 주소를 받아 주소의 값이 값으면 true다르면 false를 반환한다.
//	public boolean isEqualAddr() {
//		return false;
//	}
//
//	// AddrMsg를 세팅한다.
//	public void SetSeekAddrMsg(int msg) {
//		seekAddrMsg = msg;
//	}
//
//	// seekAddrMsg를 얻어온다.
//	public void GetSeekAddrMsg() {
//		return seekAddrMsg;
//	}

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
