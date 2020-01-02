package ch.tyratox.infcom.share.dropshare;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JTextArea;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class MiniChat extends JFrame implements KeyListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3300794883253367135L;
	private static JPanel contentPane;
	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	private static String OS = System.getProperty("os.name").toLowerCase();
	@SuppressWarnings("unused")
	private static String USER = System.getProperty("user.name").toLowerCase();
	ArrayList<String> file_list = new ArrayList<String>();
	static MulticastSocket getSocket;
	static MulticastSocket sendSocket;
	static InetAddress group;
	@SuppressWarnings("unused")
	private static DropTarget dt;
	@SuppressWarnings("unused")
	private static int files_saved = 0;
	private static int port_udp = 22222;
	static String ls = System.getProperty("line.separator");
	private static JTextField textField;
	private static JTextArea textArea;
	
	public static String nickname = "";
	
	public static ArrayList<String> messageHistory = new ArrayList<String>();
	public static int historyCounter = 1;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					try{
						System.out.println("Starting Getting Multicast Socket....");
						group = InetAddress.getByName(JOptionPane.showInputDialog("Enter the multicast address"));
						getSocket = new MulticastSocket(port_udp);
						getSocket.joinGroup(group);
						System.out.println("Starting Sending Multicast Socket....");
						sendSocket = new MulticastSocket();
						
					}catch(BindException e){
						handleException(e);
						System.exit(0);
					}
					System.out.println("New Thread: Get Messages over UDP");
					new Thread(){
						public void run(){
							getMessages();
						}
					}.start();
				     System.out.println("Creating Frame");
					MiniChat frame = new MiniChat();
					frame.setResizable(false);
					System.out.println("Set Frame Visible");
					frame.setVisible(true);
					textField.requestFocus();
				} catch (Exception e) {
					handleException(e);
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MiniChat() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 250, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		setAlwaysOnTop(true);
		
		textField = new JTextField();
		textField.setFont(new Font("Helvetica", Font.PLAIN, 22));
		textField.setBounds(6, 294, 238, 28);
//		textField.setColumns(5);
		textField.addKeyListener(this);
		contentPane.add(textField);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("Helvetica", Font.PLAIN, 12));
		textArea.setBounds(6, 6, 238, 288);
		textArea.setLineWrap(true);
		textArea.setForeground(Color.black);
		textArea.setOpaque(false);
		textArea.setBackground(new Color(255, 255, 255, 0));
//		textArea.setContentType("text/html");
		final MiniChatMenu mcm = new MiniChatMenu(this);
		textArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3)
				mcm.show(textArea, e.getX(), e.getY());
			}
		});
//		contentPane.add(textArea);
		  
		JScrollPane scrollPanePlain = new JScrollPane(textArea);  
		scrollPanePlain.setBounds(6, 6, 238, 284);  
		scrollPanePlain.setVisible(true);
		contentPane.add(scrollPanePlain);
		
		setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		// Set undecorated
//		setUndecorated(true);
//		setBackground(new Color(255, 255, 255, 0));
	}
	public static boolean isWindows() {
		 
		return (OS.indexOf("win") >= 0);
 
	}
 
	public static boolean isMac() {
 
		return (OS.indexOf("mac") >= 0);
 
	}
 
	public static boolean isUnix() {
 
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
 
	}
	  public static void getMessages(){
		  while(true){
			try {
				  byte buf[] = new byte[1024 * 64];
				  DatagramPacket pack = new DatagramPacket(buf, buf.length);
				  System.out.println("Get Pack....");
				  getSocket.receive(pack);
				  System.out.println("Got Message: " + new String(pack.getData()));
				  String ips = pack.getAddress().toString().replace("/", "");
				  String[] ip_ = ips.split(":");
				  String ip = ip_[0];
				  String msg = new String(pack.getData());
				  
					  String niname = "";
					  if(msg.contains("n:") && msg.contains(";")){
						  niname = msg.replace("n:", "").split(";")[0];
						  msg = msg.replace("n:", "").split(";")[1];
						  ip = niname;
					  }
					  if(textArea.getText().equalsIgnoreCase("") && new String(pack.getData()) != ""){
						  textArea.setText(textArea.getText() + "[" + ip + "]" + ": " + msg);
					  }else if(new String(pack.getData()) != ""){
						  textArea.setText(textArea.getText() + ls + "[" + ip + "]" + ": " + msg);
					  }
					  textArea.setCaretPosition(textArea.getText().trim().length());  
					  
			} catch (IOException e) {
				handleException(e);
			}
		  }
	  }
	  public static void sendMessage(String message){
			  byte[] y = message.getBytes();
				  DatagramPacket pack = new DatagramPacket(y, y.length, group, port_udp);
		          try {
					sendSocket.send(pack);
					System.out.println("Sent: " + new String(y));
				} catch (IOException e) {
					handleException(e);
				}
	  }

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		String msg = textField.getText();
		if(e.getKeyCode() == 10 && msg != "" && msg != null && msg.contains(";") == false && msg.length() < 150){
			if(msg.contains("/setname") && msg.substring(0, "/setname".length()).equalsIgnoreCase("/setname")){
				  try{
					  if(msg.split(" ")[1].length() < 16){
						  nickname = msg.split(" ")[1];
						  System.out.println("New Nickname: " + nickname);
					  }else{
						  showMessage("Nickname is too long!");
					  }
				  }catch(Exception e1){
					  
				  }
			  }else{
				  if(nickname != ""){
					sendMessage("n:" + nickname + ";" + msg);
					System.out.println("Sent Message with Nickname");
				  }else{
					sendMessage(msg);
					System.out.println("Sent message");
				  }
				textField.setText("");
				historyCounter = 1;
			  }
			  messageHistory.add(msg);
			  textField.setText("");
		}else if(e.getKeyCode() == 38){
			try{
			textField.setText(messageHistory.get(messageHistory.size() - historyCounter));
			
			}catch(Exception e1){
				
			}
			historyCounter++;
		}else{
			historyCounter = 1;
			if(msg.length() > 150){
				showMessage("Message is too long!");
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
	public static void handleException(Exception e){
		  e.printStackTrace();
		  showMessage(e.toString());
	  }
	public static void showMessage(String message){
		  JOptionPane.showMessageDialog(textArea, message);
	}
	public void enableMulticastOSX(){
		System.out.println("Requesting Admin Password on Mac beacuse of Multicasting");
		String[] command = {
		        "osascript",
		        "-e",
		        "do shell script \"route -nv add -net 224.2.2.3 -interface en0\" with administrator privileges" };
		Runtime runtime = Runtime.getRuntime();
		try {
		    runtime.exec(command);
		} catch (IOException e) {
		    handleException(e);
		}
  }
}
