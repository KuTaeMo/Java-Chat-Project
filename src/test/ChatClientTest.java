package test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.google.gson.Gson;

import ChatProtocol.ChatInter;
import ChatProtocol.ChatTestDto;

public class ChatClientTest extends JFrame{
	
	private ChatClientTest chatClient=this;
	private static final String TAG="ChatClient : ";
	
	private static final int PORT=10000;
	
	private JButton btnConnect, btnSend, btnIDSet;
	private JTextField tfHost, tfChat, tfID;
	private JTextArea taChatList;
	private ScrollPane scrollPane;
	private JPanel topPanel, bottomPanel, inTopPanel1, inTopPanel2;
	private JRadioButton rbMsgAll, rbMsgMsg;
	private ButtonGroup group;
	
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
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
		btnIDSet=new JButton("set");
		tfHost=new JTextField("127.0.0.1",20);
		tfChat=new JTextField("ID:",20);
		tfID=new JTextField(6);
		rbMsgAll=new JRadioButton("전체 메시지",true);
		rbMsgMsg=new JRadioButton("개인 메시지");
		group=new ButtonGroup();
		taChatList=new JTextArea(10,30); // row, column
		scrollPane=new ScrollPane();
		inTopPanel1=new JPanel();
		inTopPanel2=new JPanel();
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
		topPanel.setLayout(new BorderLayout());
		tfID.setEnabled(false);
	}
	private void batch() {
		topPanel.add(inTopPanel1);
		topPanel.add(inTopPanel2);
		inTopPanel1.add(tfHost);
		inTopPanel1.add(btnConnect);
		inTopPanel2.add(rbMsgAll);
		inTopPanel2.add(rbMsgMsg);
		inTopPanel2.add(tfID);
		inTopPanel2.add(btnIDSet);
		bottomPanel.add(tfChat);
		bottomPanel.add(btnSend);
		scrollPane.add(taChatList);
		group.add(rbMsgAll);
		group.add(rbMsgMsg);
		
		add(topPanel,BorderLayout.NORTH);
		topPanel.add(inTopPanel1, BorderLayout.NORTH);
		topPanel.add(inTopPanel2, BorderLayout.SOUTH);
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
		btnIDSet.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tfChat.setText("MSG:"+tfID.getText()+":");
				tfID.setText("");
			}
		});
		rbMsgMsg.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==ItemEvent.DESELECTED) {
					return;
				}
				if(rbMsgMsg.isSelected()) {
					tfChat.setText("MSG:");
					tfID.setEnabled(true);
				}
			}
		});
		rbMsgAll.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange()==ItemEvent.DESELECTED) {
					return;
				}
				if(rbMsgAll.isSelected()) {
					tfChat.setText("ALL:");
					tfID.setEnabled(false);
				}
			}
		});
		this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) { 
            	//save();
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
		String chatview=null;
		ChatTestDto dto=new ChatTestDto();
		
		String gubun[]=chat.split(":");
		if(gubun[0].equals(ChatInter.ALL)) {
			dto.setGubun(gubun[0]);
			dto.setMsg(gubun[1]);
			chatview=gubun[1];
		}else if(gubun[0].equals(ChatInter.ID)) {
			dto.setGubun(gubun[0]);
			dto.setId(gubun[1]);
			chatview=gubun[1];
		}else if(gubun[0].equals(ChatInter.MSG)) {
			dto.setGubun(gubun[0]);
			dto.setId(gubun[1]);
			dto.setMsg(gubun[2]);
			chatview=gubun[2];
		}
		Gson gson=new Gson();
		String jsonData=gson.toJson(dto);
		
		writer.println(jsonData);
		writer.flush();
		
		//1번 taChatList 뿌리기
		taChatList.append("[내 메시지] "+chatview+"\n");
		allMsg.add(chat);
		//2번 서버로 전송

		//3번 tfChat비우기
		if(rbMsgAll.isSelected()==true) {
			tfChat.setText("ALL:");
		}else if(rbMsgMsg.isSelected()==true) {
			tfChat.setText("MSG:");
		}
		
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