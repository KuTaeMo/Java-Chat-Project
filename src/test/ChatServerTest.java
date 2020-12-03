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
	private Vector<ClientInfo> vc;	//����� Ŭ���̾�Ʈ Ŭ����(����)�� ��� �÷���
	private Vector <String> vcdata=new Vector<String>();
	private File file=new File("c:\\Temp\\chatServerTest.txt");
	private FileWriter fout;
	
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
				}vcdata.add(dto.getMsg());
				System.out.println(dto.getMsg());
			}else if(dto.getGubun().equals(ChatInter.MSG)) {	
				String tempId = dto.getId();
				String tempMsg = dto.getMsg();
				
				for (int i = 0; i < vc.size(); i++) {
					if(vc.get(i).id != null && vc.get(i).id.equals(tempId)) {
						vc.get(i).writer.println(id+" : "+tempMsg);
						vc.get(i).writer.flush();
					}
				}vcdata.add(dto.getMsg());
				System.out.println(dto.getMsg());
			}
		}
	}
	public void saveLog() {
		try {
			fout=new FileWriter(file);
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
	
	public static void main(String[] args) {
		ChatServerTest t1=new ChatServerTest();
		t1.saveLog();
	}
}
