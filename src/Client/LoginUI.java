
/**
 * 在这个界面将实现登录功能与连接服务器功能，
 * 首先要先于服务器去得连接，为了确保程序的可用性，每一个按钮处都设置了这个前提，
 * 通过点击登录将用户信息发送到服务端进行核对，然后根据服务端反馈的信息来判断当前用户能否进入，
 * 如果反馈信息提示不能进入，就会根据信息来判断是哪一步错了，这一点使用switch来提高效率，因为选择题要比if else判断题更高效
 * 同时这个界面还实现了向RegDialogUI注册界面的跳转，具体的操作都交给了RegDialogUI
 * 
 */
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class LoginUI extends JFrame {


	private static final long serialVersionUID = -8542876911761120960L;
	private JPanel contentPane;
	private JTextField tfUsr;
	private JPasswordField tfPsw;
	private Socket client;
	private JLabel lblUsername;
	private JButton btnLogin;
	private JLabel lblPassword;
	private JTextField tfServer;
	private RegisterUI regDialog;
	private JLabel lblServer;
	private String ip = null;
	private JButton btnRegister;
	private JButton btnConnect;
	
	public static void main(String[] args) {
		try 
		{
		    for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
		    {
		        if("Nimbus".equals(info.getName()))
		        {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		}
		catch(Exception e)
		{
			// If Nimbus is not available, you can set the GUI to another look and feel.
			System.out.println("theme initialization failed");
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginUI frame = new LoginUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public LoginUI() {
		

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(120, 120, 411, 389);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setTitle("Client User Interface");
		
		lblServer = new JLabel("Server\uFF1A");
		lblServer.setLocation(21, 8);
		lblServer.setSize(81, 43);
		
		tfServer = new JTextField();
		tfServer.setLocation(91, 16);
		tfServer.setSize(134, 27);
		tfServer.setText("10.5.5.101");
		tfServer.setColumns(10);

		btnConnect = new JButton("Connect");
		btnConnect.setLocation(244, 16);
		btnConnect.setSize(126, 27);
		//Button of Connect
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String ip = tfServer.getText();//  "10.103.221.231"
				int port = 8787;
				try {
					client = new Socket(ip, port);
					LoginUI.this.ip = ip;
					setTitle(getTitle() + "...connect successfully");
					btnConnect.setEnabled(false);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					JDialog error = new JDialog(LoginUI.this, "Connection error", true);
					error.getContentPane().add(new Label("Server doesn't response！\r\n Please reconnect"));
					error.setBounds(400, 200, 200, 100);
					error.setVisible(true);
					e1.printStackTrace();
				}
			}
		});
		
		lblUsername = new JLabel("Username");
		lblUsername.setHorizontalAlignment(SwingConstants.CENTER);
		lblUsername.setSize(71, 27);
		lblUsername.setLocation(54, 74);
		
		lblPassword = new JLabel("Password");
		lblPassword.setLocation(54, 133);
		lblPassword.setSize(71, 27);

		tfUsr = new JTextField();
		tfUsr.setLocation(191, 66);
		tfUsr.setSize(109, 43);
		tfUsr.setColumns(10);

		tfPsw = new JPasswordField();
		tfPsw.setLocation(174, 125);
		tfPsw.setSize(95, 43);

		btnLogin = new JButton("Login");
		btnLogin.setSize(71, 56);
		btnLogin.setLocation(86, 239);
		
		//Button of Login
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tfUsr.getText().length() == 0 || tfPsw.getPassword().length == 0) {
					final Dialog errorLink = new Dialog(LoginUI.this);
					errorLink.setLayout(new FlowLayout());
					errorLink.setTitle("Input Error");
					errorLink.add(new Label("Please input username and password"));
					errorLink.setBounds(600, 200, 200, 100);
					JButton liButton = new JButton("OK");
					errorLink.add(liButton);
					liButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							errorLink.dispose();
						}
					});
					errorLink.setVisible(true);
				} else {
					if (client != null) {
						login(tfUsr.getText(), new String(tfPsw.getPassword()));
					} else {
						final Dialog errorLink = new Dialog(LoginUI.this);
						errorLink.setLayout(new FlowLayout());
						errorLink.setTitle("Connect Error");
						errorLink.add(new Label("Please connect to server first!"));
						errorLink.setBounds(600, 200, 200, 100);
						JButton okButton = new JButton("OK");
						errorLink.add(okButton);
						okButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								errorLink.dispose();
							}
						});
						errorLink.setVisible(true);
					}
				}
			}
		});


		btnRegister = new JButton("Register");
		btnRegister.setLocation(219, 246);
		btnRegister.setSize(81, 43);
		//Button of register
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (client != null)
					register(client);
				else {
					final Dialog errorLink = new Dialog(LoginUI.this);
					errorLink.setLayout(new FlowLayout());
					errorLink.setTitle("Connect Error");
					errorLink.add(new Label("Please connect to server first!"));
					errorLink.setBounds(600, 200, 200, 100);
					JButton okButton = new JButton("OK");
					errorLink.add(okButton);
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							errorLink.dispose();
						}
					});
					errorLink.setVisible(true);
				}
			}
		});
		
		contentPane.add(tfUsr);
		contentPane.add(tfPsw);
		contentPane.add(tfServer);
		contentPane.add(lblPassword);
		contentPane.add(lblServer);
		contentPane.add(lblUsername);
		contentPane.add(btnLogin);
		contentPane.add(btnConnect);
		contentPane.add(btnRegister);
		setResizable(false);
		setContentPane(contentPane);

	}

	public void login(String name, String password) {
		try {
			PrintWriter send = new PrintWriter(client.getOutputStream(), true);
			BufferedReader receive = new BufferedReader(new InputStreamReader(client.getInputStream()));
			send.println("LOG");
			send.println(name);
			send.println(password);
			String header = receive.readLine();
			//String header = receive.readLine();//header = CSM
			String data = receive.readLine();
			switch (data) {
			case "Success":
				System.out.println(header + " " + data);
				new ChatUI(client, name ,ip).setVisible(true);
				dispose();//close LoginUI
				break;
			case "Failed":
				final Dialog faError = new Dialog(this, "Login Error", true);// 使用Jdialog将出现解析问题
				faError.add(new Label("    Username or Password is wrong. Please log in again!"));
				faError.setBounds(600, 200, 190, 100);
				faError.setLayout(new FlowLayout());
				JButton faButton = new JButton("OK");
				faError.add(faButton);
				faButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						faError.dispose();
					}
				});
				faError.setVisible(true);
				break;
			case "Repetition":
				final Dialog reError = new Dialog(this, "Login Error", true);
				reError.add(new Label("   The acount you have typed is online, please try other account!"));
				reError.setBounds(600, 200, 270, 100);
				reError.setLayout(new FlowLayout());
				JButton reButton = new JButton("OK");
				reError.add(reButton);
				reButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						reError.dispose();
					}
				});
				reError.setVisible(true);
				break;
			case "Blank":
				final Dialog blankError = new Dialog(this, "Login Error", true);
				blankError.add(new Label("Please type the username and password"));
				blankError.setBounds(600, 200, 180, 100);
				blankError.setLayout(new FlowLayout());
				JButton blButton = new JButton("OK");
				blankError.add(blButton);
				blButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						blankError.dispose();
					}
				});
				blankError.setVisible(true);
				break;
			default:
				break;
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void register(Socket client) {//Dialog注册服务窗体
		regDialog = new RegisterUI(client);
		regDialog.setModal(true);
		//“有模式”意味着该窗口打开时其他窗口都被屏蔽了，你可以试试，在此情况下，点击程序的其他窗口是不允许的。
		regDialog.setVisible(true);
	}
}
