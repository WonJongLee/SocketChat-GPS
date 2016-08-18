// Java Chatting Server

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame {
	static int tCnt = 0; // 총 인원수
	static int mCnt = 0; // 남자 총 인원수
	static int fCnt = 0; // 여자 총 인원수
	static int sumCnt = 0; // 총 인원수
	static int bigger = 0; //검색중인 남자, 여자들 중 큰수
	static int match = 0; 
	static int roomCnt = 0; //방번호
	int myIndex = 0; // 보낸 사람의 인덱스
	int yourIndex = 0; // 받는 사람의 인덱스
	
	int mRandom[] = new int[100]; 
	int fRandom[] = new int[100]; 
	Random random; 
	
	String sender;
	String receiver;

	Vector<String> totalId = new Vector<String>(); // 전체 id 저장

	Vector<String> mId = new Vector<String>(); // 남자 id 저장
	Vector<String> fId = new Vector<String>(); // 여자 id 저장

	ArrayList<String> mIdSearch = new ArrayList<String>();
	ArrayList<String> fIdSearch = new ArrayList<String>();
	
	int[] mState = new int[100];  //현재 상태 0-기본 1-서칭중 2-대화중
	int[] fState = new int[100];
	
	private JPanel contentPane;
	private JTextField textField; // 사용할 PORT번호 입력
	private JButton Start; // 서버를 실행시킨 버튼
	JTextArea textArea; // 클라이언트 및 서버 메시지 출력

	private ServerSocket socket; // 서버소켓
	private Socket soc; // 연결소켓
	private int Port; // 포트번호

	private Vector vc = new Vector(); // 연결된 사용자를 저장할 벡터

	public static void main(String[] args) {
		
		Server frame = new Server();
		frame.setVisible(true);
	}

	public Server() {

		init();

	}

	private void init() { // GUI를 구성하는 메소드

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 280, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane js = new JScrollPane();

		textArea = new JTextArea();
		textArea.setColumns(20);
		textArea.setRows(5);
		js.setBounds(0, 0, 264, 254);
		contentPane.add(js);
		js.setViewportView(textArea);

		textField = new JTextField();
		textField.setBounds(98, 264, 154, 37);
		contentPane.add(textField);
		textField.setColumns(10);
		textField.setText("44444");
	
		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(12, 264, 98, 37);
		contentPane.add(lblNewLabel);
		Start = new JButton("서버 실행");
		Myaction action = new Myaction();
		Start.addActionListener(action); // 내부클래스로 액션 리스너를 상속받은 클래스로
		textField.addActionListener(action);
		Start.setBounds(0, 325, 264, 37);
		contentPane.add(Start);
		textArea.setEditable(false); // textArea를 사용자가 수정 못하게끔 막는다.
	}

	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {

			// 액션 이벤트가 sendBtn일때 또는 textField 에세 Enter key 치면
			if (e.getSource() == Start || e.getSource() == textField) {
				if (textField.getText().equals("") || textField.getText().length() == 0)// textField에
																						// 값이
																						// 들어있지
																						// 않을때
				{
					textField.setText("포트번호를 입력해주세요");
					textField.requestFocus(); // 포커스를 다시 textField에 넣어준다
				} else {
					try {
						Port = Integer.parseInt(textField.getText()); // 숫자로
																		// 입력하지
																		// 않으면
																		// 에러 발생
																		// 포트를
																		// 열수
																		// 없다.
						server_start(); // 사용자가 제대로된 포트번호를 넣었을때 서버 실행을위헤 메소드 호출
					} catch (Exception er) {
						// 사용자가 숫자로 입력하지 않았을시에는 재입력을 요구한다
						textField.setText("숫자로 입력해주세요");
						textField.requestFocus(); // 포커스를 다시 textField에 넣어준다
					}
				} // else 문 끝
			}

		}

	}

	private void server_start() {
		try {
			socket = new ServerSocket(Port); // 서버가 포트 여는부분
			Start.setText("서버실행중");
			Start.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
			textField.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다

			if (socket != null) // socket 이 정상적으로 열렸을때
			{
				Connection();
			}

		} catch (IOException e) {
			textArea.append("소켓이 이미 사용중입니다...\n");

		}
	}

	private void Connection() {
		Thread th = new Thread(new Runnable() { // 사용자 접속을 받을 스레드
			@Override
			public void run() {
				while (true) { // 사용자 접속을 계속해서 받기 위해 while문
					try {
						textArea.append("사용자 접속 대기중...\n");
						soc = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
						textArea.append("사용자 접속!!\n");
						UserInfo user = new UserInfo(soc, vc); // 연결된 소켓 정보는 금방
																// 사라지므로, user
																// 클래스 형태로 객체 생성
						// 매개변수로 현재 연결된 소켓과, 벡터를 담아둔다
						vc.add(user); // 해당 벡터에 사용자 객체를 추가
						user.start(); // 만든 객체의 스레드 실행
					} catch (IOException e) {
						
						textArea.append("!!!! accept 에러 발생... !!!!\n");
					}
				}
			}
		});
		th.start();
	}

	class UserInfo extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Socket user_socket;
		private Vector user_vc;
		private String Nickname = "";

		public UserInfo(Socket soc, Vector vc) // 생성자메소드
		{
			// 매개변수로 넘어온 자료 저장
			this.user_socket = soc;
			this.user_vc = vc;

			User_network();
		}

		public void User_network() {
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);

				// Nickname = dis.readUTF(); // 사용자의 닉네임 받는부분
				byte[] b = new byte[1024];
				dis.read(b);

				String sMessage;
				String gender = new String(b);
				gender = gender.trim();
				System.out.println("처음" + gender);

	
				if (gender.equals("male")) {
					System.out.println("남자 들옴");
					mId.addElement("m" + mCnt); // add는 boolean 리턴값, addElement는
												// 리턴값없음
					mState[mCnt] = 0;
					textArea.append("ID " + mId.elementAt(mCnt) + " 접속\n");
					textArea.setCaretPosition(textArea.getText().length()); // 커서
																			// 위치
																			// 처음으로?
					sMessage = "DEFINE-" + mId.elementAt(mCnt);
					send_Message(sMessage); // 연결된 사용자에게 정상접속을 알림
					System.out.println(mId.elementAt(mCnt) + "현재상태" + mCnt + "번째 등록완료");
					
					totalId.addElement("m" + mCnt);
					mCnt++;
					tCnt++;
				}

				else if (gender.equals("female")) {
					System.out.println("여자 들옴");
					fId.addElement("f" + fCnt); // add는 boolean 리턴값, addElement는
												// 리턴값없음
					fState[fCnt] = 0;
					textArea.append("ID " + fId.elementAt(fCnt) + " 접속\n");
					textArea.setCaretPosition(textArea.getText().length()); // 커서
																			// 위치
																			// 처음으로?
					sMessage = "DEFINE-" + fId.elementAt(fCnt);
					send_Message(sMessage); // 연결된 사용자에게 정상접속을 알림
					System.out.println(fId.elementAt(fCnt) + "현재상태" + fCnt + "번째 등록완료");
					totalId.addElement("f" + fCnt);
					fCnt++;
					tCnt++;
				}

			} catch (Exception e) {
				textArea.append("스트림 셋팅 에러\n");
				textArea.setCaretPosition(textArea.getText().length());
			}
				
		}
			
		public synchronized void InMessage(String str) {
			/*str = str.trim();*/

			String sMessage;
			String[] receive;

			receive = str.split("-");
			

			// 사용자 메세지 처리
			if (receive[0].equals("start")) {
				sender = receive[1];

				System.out.println("테스트2-0 start 한 사람" + sender);
				
				if(sender.contains("m")) {
					System.out.println("테스트2-1 sender는 " +sender);  
					mIdSearch.add(sender);
					System.out.println("++++++++++++++++++" + mIdSearch.size());
					for(int i=0; i<mCnt; i++)
						if(mId.contains(sender)) {
							mState[i] = 1;
							System.out.println("테스트3-1 mId"+mState[i]);
							/*System.out.println("테스트3-2 mId "+i+"번째요소"+mIdSearch.get(i));*/
						}
				}
				
				else if(sender.contains("f")) {
					System.out.println("테스트2-1 sender는 " +sender);  
					fIdSearch.add(sender);
					System.out.println("-------------------" + fIdSearch.size());
					for(int i=0; i<fCnt; i++)
						if(fId.contains(sender)) {
							fState[i] = 1;
							System.out.println("테스트3-1 상태 "+fState[i]);
							/*System.out.println("테스트3-2 fId"+i+"번째요소"+fIdSearch.get(i));*/
						}
				}
				System.out.println("테스트4-1 검색중인 남자" + mIdSearch.size());
				System.out.println("테스트4-2 검색중인 여자" + fIdSearch.size());
				//match 방의 개수
				//여기가 맞나
				if(mIdSearch.size() < fIdSearch.size())
					match = mIdSearch.size();
					
				else
					match = fIdSearch.size();
					
				System.out.println("match 값은 =" +match);
				random = new Random();
				
				for(int i=roomCnt; i<match; i++) {
					mRandom[i] = random.nextInt(match-roomCnt)+roomCnt;
					System.out.println("테스트5-0" + mRandom[i]);
					for(int j=roomCnt; j<i; j++) {
						if(mRandom[i] == mRandom[j]) {
							i = i-1;
							break;
						}
					}
					System.out.println("테스트5-1 " + "mrandom " + i +"인덱스 " + mRandom[i]);
				}
				
				for(int i=roomCnt; i<match; i++) {
					fRandom[i] = random.nextInt(match-roomCnt)+roomCnt;
					System.out.println("테스트5-0" + fRandom[i]);
					for(int j=roomCnt; j<i; j++) {
						if(fRandom[i] == fRandom[j]) {
							i = i-1;
							break;
						}
					}
					System.out.println("테스트5-2 " + "frandom " + i +"인덱스 " + fRandom[i]);
				}
				
				System.out.println("테스트6 match 값은" +match + "roomCnt 값은" + roomCnt);
					 
				//검색중에서 sender가 남자일때 
				for(int i=roomCnt; i<match; i++) {
					sender = mIdSearch.get(mRandom[i]);
					receiver = fIdSearch.get(fRandom[i]);
					System.out.println("테스트7-1" + "검색중인 sender남자 " + mRandom[i] + "==" + sender);
					System.out.println("테스트7-2" + "검색중인 receiver여자 " +fRandom[i] + "==" + receiver);
					str = "IN-" + sender + "-" + receiver;
					System.out.println("테스트7-3 "+ str);
					System.out.println("테스트7-4 "+ mCnt);
					for(int j=0; j<tCnt; j++) {
						if(totalId.elementAt(j).equals(sender)) { 
							System.out.println("테스트7-5" + str + "인덱스" +j);
							broad_cast(str, j);
						}
					}
				}
				
				//검색중에서 sender가 여자일때 
				for(int i=roomCnt; i<match; i++) {
					sender = fIdSearch.get(fRandom[i]);
					receiver = mIdSearch.get(mRandom[i]);
					System.out.println("테스트8-1 " + "검색중인 sender여자" + fRandom[i] + "==" + sender);
					System.out.println("테스트8-2 " +"검색중인 receiver남자" +mRandom[i] + "==" + receiver);
					str = "IN-" + sender + "-" + receiver;
					System.out.println("테스트8-3 "+ str);
					System.out.println("테스트8-4 "+ fCnt);
					for(int j=0; j<tCnt; j++) { //인덱스 난수 중복 안됨
						if(totalId.elementAt(j).equals(sender)) { 
							System.out.println("테스트8-5" + str + "인덱스" +j);
							broad_cast(str, j);
						}
					}
				}
				
				roomCnt = match;
				
				str = "IN-" + sender + "-" + receiver; //받는 사람 
				receive = str.split("-");
			}
			
			//나가면 상대방에게 알림
			if(receive[0].equals("end")) {
				String endId = receive[1];
				String endMatchId = null;
				
				System.out.println("테스트9-0" + str);
				
				for(int i=match-1; i>=0; i--) {
					if(mIdSearch.get(i).equals(endId)) {
						 System.out.println("테스트 9-1-1 " + mIdSearch.get(i));
						 System.out.println("테스트777 이때의 i값은" + i);
						 endMatchId = fIdSearch.get(i);
						 fIdSearch.set(i, "");
						 mIdSearch.set(i, "");
						 break;
					 }
					 
					else if(fIdSearch.get(i).equals(endId)) {
						 System.out.println("테스트 9-1-2 " + fIdSearch.get(i));
						 System.out.println("테스트888 이때의 i값은" + i);
						 endMatchId = mIdSearch.get(i);
						 mIdSearch.set(i, "");
						 fIdSearch.set(i, ""); 
						 break;
					 }
				}
				System.out.println("테스트9-2" + "나간사람 = "+ endId + "나간상대방 사람" + endMatchId);
				
				str = "OUT-" + endMatchId;
				for(int j=0; j<tCnt; j++) {
					if(totalId.elementAt(j).equals(endMatchId)) { 
						System.out.println("테스트9-3 " + str + "인덱스" +j);
						broad_cast(str, j);
					}
				}
			}
			
			//메시지 보내는곳
			if (receive[0].equals("to")) {
				str = receive[3];
				
				for(int i=0; i<tCnt; i++) {
					System.out.println("테스트10" + "보내는사람 " + receive[1] + "받는사람 " + receive[2] + receive[3] );
					for(int j=0; j<tCnt; j++) {
						if (totalId.elementAt(i).equals(receive[1])){
							if (totalId.elementAt(j).equals(receive[2])) {
								myIndex = i;  //myIndex는 totalId에 저장된 나의 아이디 인덱스
								yourIndex = j; //yourIndex는 totalId에 저장된 상대방의 아이디 인덱스
							} 
						}
					}
				}
			
				broad_cast("I-"+str, myIndex);
				broad_cast("YOU-"+str, yourIndex);
			}
		}
		
		public void broad_cast(String str, int index) {
			/*for (int i = 0; i < user_vc.size(); i++) {*/

				/* UserInfo imsi = (UserInfo) user_vc.elementAt(i); */
				UserInfo imsi = (UserInfo) user_vc.elementAt(index);
				imsi.send_Message(str);
			/*}*/
		}

		public void send_Message(String str) {
			try {
				// dos.writeUTF(str);
				byte[] bb;
				bb = str.getBytes();
				dos.write(bb); // .writeUTF(str);
			} catch (IOException e) {
				textArea.append("메시지 송신 에러 발생\n");
				textArea.setCaretPosition(textArea.getText().length());
			}
		}

		public void run() // 스레드 정의
		{
			while (true) {
				try {

					// 사용자에게 받는 메세지
					byte[] b = new byte[1024];
					dis.read(b);
					String msg = new String(b);
					msg = msg.trim();
					// String msg = dis.readUTF();
					InMessage(msg);

				} catch (IOException e) {

					try {
						dos.close();
						dis.close();
						user_socket.close();
						vc.removeElement(this); // 에러가난 현재 객체를 벡터에서 지운다
						textArea.append(vc.size() + " : 현재 벡터에 담겨진 사용자 수\n");
						textArea.append("사용자 접속 끊어짐 자원 반납\n");
						textArea.setCaretPosition(textArea.getText().length());

						break;

					} catch (Exception ee) {

					} // catch문 끝
				} // 바깥 catch문끝
					
			}

		}// run메소드 끝

	} // 내부 userinfo클래스끝

}