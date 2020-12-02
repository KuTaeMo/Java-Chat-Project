package test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClientTest extends JFrame{
	
	private ChatClientTest chatClient=this;
	private static final String TAG="ChatClient : ";
	
	private static final int PORT=10000;
	
	private JButton btnConnect, btnSend;
	private JTextField tfHost, tfChat;
	private JTextArea taChatList;
	private ScrollPane scrollPane;
	private JPanel topPanel, bottomPanel;
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	
	private InputStreamReader in;
	private FileWriter fout;
	private Vector<String> allMsg;
	private String[] allMsgArray;
	
	public ChatClientTest() {
		init();
		setting();
		batch();
		listener();
		setVisible(true);
	}
	private void init() {
		btnConnect=new JButton("connect");
		btnSend=new JButton("send");
		tfHost=new JTextField("127.0.0.1",20);
		tfChat=new JTextField(20);
		taChatList=new JTextArea(10,30); // row, column
		scrollPane=new ScrollPane();
		topPanel=new JPanel();
		bottomPanel=new JPanel();
		allMsg=new Vector<String>();
		allMsgArray=new String[30];
	}
	private void setting() {
		setTitle("채팅 다대다 클라이언트");
		setSize(350, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); //창 가운데로 오게 하기
		taChatList.setBackground(Color.orange);
		taChatList.setForeground(Color.blue);
	}
	private void batch() {
		topPanel.add(tfHost);
		topPanel.add(btnConnect);
		bottomPanel.add(tfChat);
		bottomPanel.add(btnSend);
		scrollPane.add(taChatList);
		
		add(topPanel,BorderLayout.NORTH);
		add(scrollPane,BorderLayout.CENTER);
		add(bottomPanel,BorderLayout.SOUTH);
	}
	private void listener() {
		btnConnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				 connect();
			}
		});
		btnSend.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) { 
            	save();
            	System.out.println("끝나요!");
            	System.exit(0);
            }
    });
	}
	
	class ReaderThread extends Thread{
		//while을 돌면서 서버로부터 메시지를 받아서 taChatList에 뿌리기
		@Override
		public void run() {
			String chat=null;
			try {
				while((chat=reader.readLine())!=null) {
					taChatList.append("[상대 메시지] "+chat+"\n");
					allMsg.add(chat);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void connect() {
		String host=tfHost.getText();
		try {
			socket=new Socket(host,PORT);
			reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer=new PrintWriter(socket.getOutputStream());
			ReaderThread rt=new ReaderThread();
			rt.start();
		} catch (Exception e1) {
			System.out.println(TAG+"서버 연결 에러 :"+e1.getMessage());
		}
	}
	private void send() {
		String chat=tfChat.getText();
		//1번 taChatList 뿌리기
		taChatList.append("[내 메시지] "+chat+"\n");
		allMsg.add(chat);
		//2번 서버로 전송
		writer.write(chat+"\n");
		writer.flush();
		//3번 tfChat비우기
		tfChat.setText("");
	}
	private void save() {
		
    		try {
    			fout=new FileWriter("c:\\Temp\\chatTest.txt");
    			for(int i=0;i<allMsg.size();i++) {
    				allMsgArray[i]=allMsg.get(i);
    				System.out.println(allMsgArray[i]);
    				fout.write(allMsgArray[i]);
    				fout.write("\r\n");
    			}
    			fout.close();
			} catch (IOException e1) {
				System.out.println("파일 저장 실패");
			}
    	
	}
	public static void main(String[] args) {
		new ChatClientTest();
	}
}