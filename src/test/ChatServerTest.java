package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServerTest {
	
	private static final String TAG="Chat Server : ";
	private ServerSocket serverSocket;
	private Vector<ClientInfo> vc;	//연결된 클라이언트 클래스(소켓)을 담는 컬렉션
	
	public ChatServerTest() {
		try {
			vc=new Vector<>();
			serverSocket=new ServerSocket(10000);
			System.out.println(TAG+"클라이언트 연결 대기 중...");
			
			//메인 스레드의 역할 : 벡터에 정보 담기
			while(true) {
				Socket socket=serverSocket.accept(); //클라이언트 연결 대기
				System.out.println(TAG+"클라이언트 연결 완료");
				
				ClientInfo clientInfo=new ClientInfo(socket);
				clientInfo.start();
				vc.add(clientInfo);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class ClientInfo extends Thread{
		
		Socket socket; //콤포지션
		BufferedReader reader;
		PrintWriter writer;	//BufferedWriter와 다른 점은 내려쓰기 함수를 지원
		
		public ClientInfo(Socket socket) {
			this.socket=socket;
			try {
				reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer=new PrintWriter(socket.getOutputStream());
				
			} catch (Exception e) {
				System.out.println("서버 연결 실패 : "+e.getMessage());
			}
		}
		
		//역할 : 클라이언트로부터 받은 메시지를 모든 클라이언트에게 재전송
		@Override
		public void run() {
			String input=null;
			
				try {
					while((input=reader.readLine())!=null) {
						System.out.println(input);
						for(int i=0;i<vc.size();i++) {
							if(vc.get(i)!=this) {
								vc.get(i).writer.println(input);
								vc.get(i).writer.flush();
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			
		}
	}
	
	public static void main(String[] args) {
		new ChatServerTest();
	}
}
