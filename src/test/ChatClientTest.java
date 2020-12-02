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
		setTitle("ä�� �ٴ�� Ŭ���̾�Ʈ");
		setSize(350, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null); //â ����� ���� �ϱ�
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
            	System.out.println("������!");
            	System.exit(0);
            }
    });
	}
	
	class ReaderThread extends Thread{
		//while�� ���鼭 �����κ��� �޽����� �޾Ƽ� taChatList�� �Ѹ���
		@Override
		public void run() {
			String chat=null;
			try {
				while((chat=reader.readLine())!=null) {
					taChatList.append("[��� �޽���] "+chat+"\n");
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
			System.out.println(TAG+"���� ���� ���� :"+e1.getMessage());
		}
	}
	private void send() {
		String chat=tfChat.getText();
		//1�� taChatList �Ѹ���
		taChatList.append("[�� �޽���] "+chat+"\n");
		allMsg.add(chat);
		//2�� ������ ����
		writer.write(chat+"\n");
		writer.flush();
		//3�� tfChat����
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
				System.out.println("���� ���� ����");
			}
    	
	}
	public static void main(String[] args) {
		new ChatClientTest();
	}
}