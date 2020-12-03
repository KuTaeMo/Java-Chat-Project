package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import com.google.gson.Gson;

import ChatProtocol.ChatInter;
import ChatProtocol.ChatTestDto;

public class ChatServerTest {
	
	private static final String TAG="Chat Server : ";
	private ServerSocket serverSocket;
	private Vector<ClientInfo> vc;	//연결된 클라이언트 클래스(소켓)을 담는 컬렉션
	private Vector <String> vcdata=new Vector<String>();
	private File file;
	private FileWriter fout;
	
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
				clientInfo.meeting();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	public void saveLog() {
		System.out.println("실행");
		try {
			fout=new FileWriter("c:\\Temp\\chatServerTest.txt");
			String[] arr=new String[30];
			for(int i=0;i<vcdata.size();i++) {
				arr[i]=vcdata.get(i);
				fout.write(arr[i]);
				fout.write("\r\n");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	class ClientInfo extends Thread{
		
		Socket socket; //콤포지션
		String id;
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
						Gson gson=new Gson();
						ChatTestDto dto=gson.fromJson(input, ChatTestDto.class);
						
						routing(dto);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		public void meeting() {
				System.out.println("연결");
				writer.println("접속 성공! 반갑습니다.");
				writer.flush();
		}
		private void routing(ChatTestDto dto) {
			 if(id==null) {
					if(dto.getGubun().equals("ID")) {
						id=dto.getId();
						writer.println("당신의 아이디는 "+id+"입니다.");
						writer.flush();

					}else {
						System.out.println("아이디가 없습니다.");
						writer.flush();
					} 
				 }
			if(dto.getGubun().equals(ChatInter.ALL)) {
				for (int i = 0; i < vc.size(); i++) {
					if(vc.get(i).id!=this.id) {
					vc.get(i).writer.println(id+" : "+dto.getMsg());
					vc.get(i).writer.flush();
					}
				}vcdata.add(dto.getMsg());
				saveLog();
			}else if(dto.getGubun().equals(ChatInter.MSG)) {	
				String tempId = dto.getId();
				String tempMsg = dto.getMsg();
				
				for (int i = 0; i < vc.size(); i++) {
					if(vc.get(i).id != null && vc.get(i).id.equals(tempId)) {
						vc.get(i).writer.println(id+" : "+tempMsg);
						vc.get(i).writer.flush();
					}
				}vcdata.add(dto.getMsg());
				saveLog();
			}
		}
	}
	
	public static void main(String[] args) {
		new ChatServerTest();
	}
}
