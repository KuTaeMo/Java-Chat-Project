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
	private Vector<ClientInfo> vc;	//����� Ŭ���̾�Ʈ Ŭ����(����)�� ��� �÷���
	
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

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class ClientInfo extends Thread{
		
		Socket socket; //��������
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
