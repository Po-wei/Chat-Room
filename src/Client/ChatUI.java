
import java.awt.Button;
/**
 * 聊天窗口的主界面将实现聊天系统的重要功能，再这个界面，同时创建了两个SocketClient，
 * 一个是从登录界面获取的Socket，它主要用来发送和接收消息，聊天窗口的message接收与发送，都转移到ClientThread中
 * 另一个Socket主要被用来接收当前的在线人数，这是我迫不得已的做法，因为目前看来，引用一个新的Socket连接是获取列表最为方便的做法
 * 
 * 还将实现的功能是，与某人特定聊天。。。。
 */
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Font;

public class ChatUI extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private Thread client;
	private JTextArea outArea;
	private JTextArea inArea;
	private DefaultListModel<String> model;
	private JList<String> userList;
	private Socket listSocket;
	private String ip;
	private JLabel onlineNum;
	private String user;
	private JScrollPane inPane;
	private JScrollPane outPane;
	private JButton btnSend;
	private JButton btnLeave;
	private JScrollPane userPane;
	private JLabel lblOnlinePersons;
	private JButton btnHelp;
	private JButton btnLog;
	private String receieverID = "Robot";
	private PrintWriter sendMsg;
	private JLabel lblChatroom;
	private JTextField tfAll;
	private JLabel lblSendTo;
	private JLabel lblMessge;
	private FileSend fileSend;
	private FileReceiveWait fileReceiveWait;
	private JButton btnFind;
	private JTextField tfFind;
	private JButton btnGroup;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					ChatUI frame = new ChatUI();
					frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 */
	public ChatUI(Socket clientSocket, String name, String ip) throws IOException
	{
		this();
		this.user = name;
		setTitle(user + " ChatRoom");
		try
		{
			sendMsg = new PrintWriter(clientSocket.getOutputStream(), true);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		this.client = new Thread(new clientThread(clientSocket, inArea, this));
		this.client.start();// 开启线程
		this.ip = ip;
		try
		{
			this.listSocket = new Socket(ip, 34344);// 在线用户列表线程
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// File reader = new File("/Users/jackieliu/Desktop/" + user + ".txt");
		// if( reader.exists()) {
		// BufferedReader br = new BufferedReader(new FileReader(reader)); //
		// 建立一个对象，它把文件内容转成计算机能读懂的语言
		// String line = br.readLine();
		// inArea.append(line+"\n");
		// while ((line = br.readLine()) != null) {
		// inArea.append(line+"\n");
		// }
		// br.close();
		// }

		// file
		fileReceiveWait = new FileReceiveWait("Unkown");
	}

	public ChatUI() throws IOException
	{

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		setBounds(100, 100, 580, 450);
		setResizable(false);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		inPane = new JScrollPane();
		inPane.setLocation(60, 35);
		inPane.setSize(330, 264);

		outPane = new JScrollPane();
		outPane.setBounds(60, 350, 330, 63);

		btnSend = new JButton("Send");
		btnSend.setFont(new Font("Verdana", Font.PLAIN, 12));
		btnSend.setBounds(406, 309, 134, 30);
		btnSend.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (tfAll.getText().equals("Robot"))
				{
					String APIKEY = "df3d1850219843c3844355a095374351";

					try
					{
						// String INFO = outArea.getText();
						String INFO = URLEncoder.encode(outArea.getText(), "utf-8");
						System.out.println("INFO:" + INFO);
						String getURL = "http://www.tuling123.com/openapi/api?key=" + APIKEY + "&info=" + INFO;
						System.out.println("URL:" + getURL);
						URL getUrl = new URL(getURL);
						HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
						connection.connect();

						// 取得输入流，并使用Reader读取
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(connection.getInputStream(), "utf-8"));
						StringBuffer sb = new StringBuffer();
						String line = "";
						while ((line = reader.readLine()) != null)
						{
							sb.append(line);
						}
						reader.close();
						// 断开连接
						connection.disconnect();
						String[] ss = new String[10];
						String s = sb.toString();
						String answer;
						ss = s.split(":");
						answer = ss[ss.length - 1];
						answer = answer.substring(1, answer.length() - 2);
						System.out.println("Result" + answer);
						outArea.setText("");
						inArea.append("Robot say to you : " + answer + "\n");
					}
					catch (IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} // 这里可以输入问题

					return;
				}
				String message = outArea.getText();
				sendMsg.println("MSG");
				sendMsg.println(user);
				sendMsg.println(receieverID);
				try
				{
					sendMsg.println(Encryption.encrypt(outArea.getText()));
				}
				catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				inArea.append("you send to " + receieverID + " : " + outArea.getText() + "\n");
				// client.send(message);// 客户端发送信息
				// tfAll.setText("All");
				outArea.setText("");
				inArea.setCaretPosition(inArea.getText().length());// 控制光标下移
			}
		});

		btnLeave = new JButton("Leave");
		btnLeave.setFont(new Font("Verdana", Font.PLAIN, 12));
		btnLeave.setBounds(406, 385, 134, 30);
		btnLeave.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (client != null)
				{
					sendMsg.println("CMG");
					sendMsg.println("offline");
					// client.exit = true;
					// client.Exit();
					// System.exit(0);
				}
			}
		});

		userPane = new JScrollPane();
		userPane.setBounds(409, 69, 155, 227);

		lblOnlinePersons = new JLabel("Online Persons");
		lblOnlinePersons.setFont(new Font("Verdana", Font.PLAIN, 12));
		lblOnlinePersons.setHorizontalAlignment(SwingConstants.LEFT);
		lblOnlinePersons.setBounds(406, 35, 105, 35);

		btnHelp = new JButton("Help");
		btnHelp.setFont(new Font("Arial", Font.BOLD, 12));
		btnHelp.setBounds(406, 10, 68, 23);
		btnHelp.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JDialog help = new JDialog(ChatUI.this, "Help", true);
				help.getContentPane().setLayout(new FlowLayout());
				JLabel helpContent = new JLabel("通过下方的聊天窗口，");
				JLabel helpContent5 = new JLabel("与聊天室的好友交流。");
				JLabel helpContent2 = new JLabel("左侧可以查看当前在线人数。");
				JLabel helpContent3 = new JLabel("双击列表中的姓名，");
				JLabel helpContent4 = new JLabel("可以单独向其发送信息。");
				JLabel helpContent6 = new JLabel("Ctrl+Enter快捷键可以快速发送信息。");
				help.setBounds(550, 250, 240, 180);
				help.setResizable(false);
				help.getContentPane().add(helpContent);
				help.getContentPane().add(helpContent5);
				help.getContentPane().add(helpContent2);
				help.getContentPane().add(helpContent3);
				help.getContentPane().add(helpContent4);
				help.getContentPane().add(helpContent6);
				help.setVisible(true);
			}
		});

		btnLog = new JButton("Log");
		btnLog.setFont(new Font("Arial", Font.PLAIN, 11));
		btnLog.setBounds(484, 10, 68, 23);
		btnLog.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				File reader = new File("/Users/jackieliu/Desktop/" + user + ".txt");
				if (reader.exists())
				{
					BufferedReader br;
					try
					{
						br = new BufferedReader(new FileReader(reader));
						String line = br.readLine();
						inArea.append(line + "\n");
						while ((line = br.readLine()) != null)
						{
							inArea.append(line + "\n");
						}
						br.close();
					}
					catch (IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} // 建立一个对象，它把文件内容转成计算机能读懂的语言

				}
			}
		});

		addWindowListener(new WindowAdapter()
		{// 防止直接点关闭后台线程一直异常
			public void windowClosing(WindowEvent e)
			{
				if (client != null)
				{
					sendMsg.println("CMG");
					sendMsg.println("offline");
					// dispose();
				}
			}
		});

		outArea = new JTextArea();
		outArea.setLineWrap(true);
		outArea.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_ENTER))
				{
					// 快捷键发送
					sendMsg.println("MSG");
					sendMsg.println(user);
					sendMsg.println(receieverID);
					try
					{
						sendMsg.println(Encryption.encrypt(outArea.getText()));
					}
					catch (Exception e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					inArea.append("you send to " + receieverID + " : " + outArea.getText() + "\n");
					outArea.setText("");
					inArea.setCaretPosition(inArea.getText().length());// 控制光标下移
				}
			}
		});

		outPane.setViewportView(outArea);

		model = new DefaultListModel<>();
		userList = new JList<>(model);
		userPane.setViewportView(userList);

		userList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int index = userList.locationToIndex(e.getPoint());
					String obj = model.getElementAt(index);
					if (!obj.equals(user))
					{
						// userList bianshe
						if (obj.charAt(0) == '*')
							receieverID = obj.substring(1, obj.length());
						else
							receieverID = obj;
						tfAll.setText(receieverID);
						// outArea.append("%s:" + obj + "%s:");
					}
				}
			}
		});
		contentPane.setLayout(null);

		contentPane.add(btnHelp);
		contentPane.add(btnLog);

		onlineNum = new JLabel("0");
		onlineNum.setFont(new Font("Verdana", Font.PLAIN, 12));
		onlineNum.setHorizontalAlignment(SwingConstants.CENTER);
		onlineNum.setBounds(495, 43, 43, 19);
		contentPane.add(onlineNum);

		lblSendTo = new JLabel("Send To");
		lblSendTo.setFont(new Font("Verdana", Font.PLAIN, 12));
		lblSendTo.setBounds(6, 309, 61, 29);
		contentPane.add(lblSendTo);

		lblMessge = new JLabel("Messge");
		lblMessge.setFont(new Font("Verdana", Font.PLAIN, 12));
		lblMessge.setBounds(6, 352, 61, 20);
		contentPane.add(lblMessge);

		lblChatroom = new JLabel("ChatRoom");
		lblChatroom.setFont(new Font("Verdana", Font.PLAIN, 12));
		lblChatroom.setHorizontalAlignment(SwingConstants.CENTER);
		lblChatroom.setBounds(10, 0, 90, 33);
		contentPane.add(lblChatroom);
		contentPane.add(lblOnlinePersons);

		tfAll = new JTextField();
		tfAll.setFont(new Font("Verdana", Font.PLAIN, 12));
		tfAll.setHorizontalAlignment(SwingConstants.CENTER);
		tfAll.setText("Robot");
		tfAll.setEditable(false);
		tfAll.setBounds(60, 311, 68, 26);
		contentPane.add(tfAll);
		tfAll.setColumns(10);

		JButton btnFile = new JButton("File");
		btnFile.setFont(new Font("Verdana", Font.PLAIN, 12));
		btnFile.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				sendMsg.println("FILE");
				sendMsg.println("0");
				sendMsg.println(tfAll.getText());
				fileSend = new FileSend(tfFind.getText(), 6666);
				// fileSend = new FileSend("10.5.5.101", 6666);
			}
		});
		btnFile.setBounds(329, 309, 61, 29);
		contentPane.add(btnFile);
		contentPane.add(userPane);
		contentPane.add(btnSend);
		contentPane.add(btnLeave);
		contentPane.add(outPane);
		contentPane.add(inPane);

		inArea = new JTextArea();
		inPane.setViewportView(inArea);
		inArea.setLineWrap(true);
		inArea.setEditable(false);
		setResizable(false);
		setContentPane(contentPane);

		btnFind = new JButton("Find");
		btnFind.setFont(new Font("Verdana", Font.PLAIN, 12));
		btnFind.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				sendMsg.println("FILE");
				sendMsg.println("0");
				sendMsg.println(tfAll.getText());
			}

		});
		btnFind.setBounds(134, 310, 61, 26);
		contentPane.add(btnFind);

		tfFind = new JTextField();
		tfFind.setBounds(205, 312, 114, 26);
		contentPane.add(tfFind);
		tfFind.setColumns(10);

		btnGroup = new JButton("Group");
		btnGroup.setFont(new Font("Verdana", Font.PLAIN, 12));
		btnGroup.setBounds(406, 347, 134, 30);
		btnGroup.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				sendMsg.println("GRP");
				sendMsg.println(outArea.getText());
				outArea.setText("");
			}
		});
		contentPane.add(btnGroup);

		new Thread(new innerRoomThread()).start();

	}

	private class innerRoomThread implements Runnable
	{
		// 获取在线列表，原理和RegDialog，使用新的线程，
		// 但设置为1S接收一次在线数据，这样看来不断的使用新线程将是一件危险低效的事情
		// private final Socket listSocket;//这样将出现空指针BUG！！！
		// public innerRoomThread(Socket listSocket){
		// this.listSocket=listSocket;
		// }
		private BufferedReader receive;

		public void run()
		{
			try
			{
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}

				System.out.println(listSocket.getInetAddress() + "正在获取此处的在线列表......");
				receive = new BufferedReader(new InputStreamReader(listSocket.getInputStream()));
				String info = null;
				String list[] = null;
				while ((info = receive.readLine()) != null)
				{
					if (info.indexOf(user) == -1)
						break;
					list = info.split(":");

					DefaultListModel<String> newModel = new DefaultListModel<>();
					int i = 0;
					for (String name : list)
					{
						newModel.add(i++, name);
					}
					newModel.add(i++, "Robot");
					model.clear();
					model = newModel;
					userList.setModel(model);
					onlineNum.setText(list.length + "");
				}
				listSocket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private class clientThread implements Runnable
	{
		private Socket client = null;
		private BufferedReader receive = null;
		private PrintWriter send = null;
		public boolean exit = false;
		private JTextArea reText;
		private ChatUI room;
		private String header;

		public clientThread(Socket clientSocket, JTextArea inArea, ChatUI room)
		{
			this.client = clientSocket;
			this.reText = inArea;
			this.room = room;
		}

		public void run()
		{
			try
			{
				receive = new BufferedReader(new InputStreamReader(client.getInputStream()));// 获取Socket的两个流
				send = new PrintWriter(client.getOutputStream(), true);// 设置自动刷新

			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			great_loop: while (true)
			{
				try
				{
					header = receive.readLine();
					System.out.println("header : " + header);
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				switch (header)
				{
				case "MSG":
					try
					{
						String sendID = receive.readLine();
						String rcvID = receive.readLine();
						String msg = receive.readLine();
						try
						{
							msg = Encryption.decrypt(msg);
						}
						catch (Exception e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						reText.append(sendID + " say to you : " + msg + "\n");
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case "FILE":
					try
					{
						String type = receive.readLine();
						System.out.println("type : " + type);
						if (type.equals("1"))
						{
							String ip = receive.readLine();
							System.out.println("ip : " + ip.substring(1, ip.length()));
							tfFind.setText(ip.substring(1, ip.length()));
							// fileSend.setIP(ip);
							// new FileSend(ip.substring(1, ip.length()),
							// 6666);//10.5.5.101
						}
						else
						{
							String sender = receive.readLine();
							System.out.println("sender : " + sender);
							// new FileReceiveWait(sender);
							fileReceiveWait.setSender(sender);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					break;
				case "CMG":
					try
					{
						String info = receive.readLine();
						if (info.equals("offline"))
						{
							new LoginUI().setVisible(true);
							room.dispose();
							// listSocket.close();
							this.client.close();

							File file = new File("/Users/jackieliu/Desktop/" + user + ".txt");
							synchronized (file)
							{
								FileOutputStream fos = new FileOutputStream(
										"/Users/jackieliu/Desktop/" + user + ".txt");
								fos.write(inArea.getText().getBytes("utf-8"));
								fos.close();
							}
						}
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break great_loop;

				}
			}
		}
	}

	// public void save() throws IOException {
	// FileDialog fileS = new FileDialog(this, "Chat Log", FileDialog.SAVE);
	// fileS.setFile("chat.log");
	//
	// fileS.setVisible(true);
	// String path = fileS.getDirectory();
	// String filename = fileS.getFile();
	// FileWriter filewriter = new FileWriter(new File(path, filename), true);
	// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd
	// HH-mm-ss");
	// filewriter.write(dateFormat.format(System.currentTimeMillis()) + "\r\n");
	// filewriter.write(inArea.getText());
	// filewriter.close();
	// }
	private static void addPopup(Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
