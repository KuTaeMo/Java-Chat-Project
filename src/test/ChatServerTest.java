package test;

import java.io.BufferedReader;
import java.io.File;
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
	private Vector<ClientInfo> vc;	//����� Ŭ���̾�Ʈ Ŭ����(����)�� ��� �÷���
	private Vector <String> vcdata;
	private File file;
	
	public ChatServerTest() {
		try {
			vc=new Vector<>();
			serverSocket=new ServerSocket(10000);
			System.out.println(TAG+"Ŭ���̾�Ʈ ���� ��� ��...");
			
			//���� �������� ���� : ���Ϳ� ���� ���
			while(true) {
				Socket socket=serverSocket.accept(); //Ŭ���̾�Ʈ ���� ���
				System.out.println(TAG+"Ŭ���̾�Ʈ ���� �Ϸ�");
				ClientInfo clientInfo=new ClientInfo(socket);
				clientInfo.start();
				vc.add(clientInfo);
				clientInfo.meeting();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		file=new File("c:\\Temp\\chatTest.txt");
		
	}
	
	class ClientInfo extends Thread{
		
		Socket socket; //��������
		String id;
		BufferedReader reader;
		PrintWriter writer;	//BufferedWriter�� �ٸ� ���� �������� �Լ��� ����
		
		public ClientInfo(Socket socket) {
			this.socket=socket;
			try {
				reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer=new PrintWriter(socket.getOutputStream());
				
			} catch (Exception e) {
				System.out.println("���� ���� ���� : "+e.getMessage());
			}
		}
		
		//���� : Ŭ���̾�Ʈ�κ��� ���� �޽����� ��� Ŭ���̾�Ʈ���� ������
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
				System.out.println("����");
				writer.println("���� ����! �ݰ����ϴ�.");
				writer.flush();
		}
		private void routing(ChatTestDto dto) {
			 if(id==null) {
					if(dto.getGubun().equals("ID")) {
						id=dto.getId();
						writer.println("����� ���̵�� "+id+"�Դϴ�.");
						writer.flush();

					}else {
						System.out.println("���̵� �����ϴ�.");
						writer.flush();
					} 
				 }
			if(dto.getGubun().equals(ChatInter.ALL)) {
				for (int i = 0; i < vc.size(); i++) {
					if(vc.get(i).id!=this.id) {
					vc.get(i).writer.println(id+" : "+dto.getMsg());
					vc.get(i).writer.flush();
					}
				}
			}else if(dto.getGubun().equals(ChatInter.MSG)) {	
				String tempId = dto.getId();
				String tempMsg = dto.getMsg();
				
				for (int i = 0; i < vc.size(); i++) {
					if(vc.get(i).id != null && vc.get(i).id.equals(tempId)) {
						vc.get(i).writer.println(id+" : "+tempMsg);
						vc.get(i).writer.flush();
					}
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		new ChatServerTest();
	}
}
