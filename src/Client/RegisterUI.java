
/**
注册界面的UI包含了一个SocketClient端,他的工作方式将和ChatRoomUI里获取列表相同，但此处他的工作是发送数据和接收数据，
它将根据服务端反馈回来的信息判断是否注册成功以及注册的状态，是否已经被注册等等
 * 
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Label;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JProgressBar;

public class RegisterUI extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1331039277600772730L;
	private final JPanel contentPanel = new JPanel();
	private JTextField tfUsername;
	private JPasswordField tfPassword;
	private JPasswordField stength;
	private Socket client;
	private PrintWriter send;
	private BufferedReader receive;
	private JLabel lblUsername;
	private JLabel lblPassword;
	private JPanel buttonPane;
	private JButton btnSignup;

	/**
	 * Launch the application.
	 */
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
		try {
			RegisterUI dialog = new RegisterUI();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public RegisterUI() {
		setResizable(false);
		
		setBounds(100, 100, 335, 300);
		setTitle("SignUp Account");
		getContentPane().setLayout(null);
		contentPanel.setBounds(0, 10, 329, 41);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		stength = new JPasswordField();
		stength.setToolTipText("strength");
		stength.setHorizontalAlignment(SwingConstants.RIGHT);
		getContentPane().add(contentPanel, BorderLayout.NORTH);
		{
			buttonPane = new JPanel();
			buttonPane.setBounds(6, 238, 323, 33);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane);
			{
				btnSignup = new JButton("Signup");
				btnSignup.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (tfUsername.getText().length() > 0 && tfPassword.getPassword().length > 0) {
							register();
						} else {
							final Dialog exError = new Dialog(RegisterUI.this, "Error", true);
							exError.add(new Label("Please input correct usr and psw"));
							exError.setBounds(600, 200, 180, 100);
							exError.setLayout(new FlowLayout());
							JButton okButton = new JButton("OK");
							exError.add(okButton);
							okButton.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									exError.dispose();
								}
							});
							exError.setVisible(true);
						}

					}
				});
				
				JButton btnReset = new JButton("Reset");
				btnReset.addActionListener(new ActionListener(){
					public void actionPerformed( ActionEvent e) {
						tfPassword.setText("");
						tfUsername.setText("");
					}
				});
				btnReset.setHorizontalAlignment(SwingConstants.LEFT);
				buttonPane.add(btnReset);
				//btnSignup.setActionCommand("OK");
				buttonPane.add(btnSignup);
				//getRootPane().setDefaultButton(btnSignup);
			}
		}
		tfUsername = new JTextField();
		tfUsername.setBounds(112, 53, 130, 26);
		getContentPane().add(tfUsername);
		tfUsername.setColumns(10);
		lblUsername = new JLabel("Username:");
		lblUsername.setBounds(36, 59, 75, 15);
		getContentPane().add(lblUsername);
		lblPassword = new JLabel("Password:");
		lblPassword.setBounds(36, 109, 75, 15);
		getContentPane().add(lblPassword);
		tfPassword = new JPasswordField();
		tfPassword.setBounds(112, 103, 130, 26);
		getContentPane().add(tfPassword);
		
		JTextField tfWeak= new JTextField("");
		tfWeak.setHorizontalAlignment(SwingConstants.CENTER);
		tfWeak.setBounds(112, 140, 45, 10);
		getContentPane().add(tfWeak);
		tfWeak.setBackground(Color.RED);
		tfWeak.setEditable(false);
		
		JTextField tfStrong = new JTextField("");
		tfStrong.setBounds(194, 140, 45, 10);
		getContentPane().add(tfStrong);
		tfStrong.setEditable(false);
		
		JTextField tfMedium = new JTextField("");
		tfMedium.setHorizontalAlignment(SwingConstants.CENTER);
		tfMedium.setBounds(153, 140, 45, 10);
		getContentPane().add(tfMedium);
		tfMedium.setEditable(false);
		tfPassword.addKeyListener(new KeyListener() {
			@Override
            public void keyReleased(KeyEvent e) {
				String psw = new String(tfPassword.getPassword());
				String result = checkPassword(psw);
                if(result == "weak"){
                	tfWeak.setBackground(Color.RED);
                	tfMedium.setBackground(Color.WHITE);
                	tfStrong.setBackground(Color.WHITE);
                }
                else if(result == "medium"){
                	tfWeak.setBackground(Color.WHITE);
                	tfMedium.setBackground(Color.yellow);
                	tfStrong.setBackground(Color.WHITE);
                }
                else {
                	tfWeak.setBackground(Color.WHITE);
                	tfMedium.setBackground(Color.WHITE);
                	tfStrong.setBackground(Color.GREEN);
                }
            }

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		

	}
	
    public String checkPassword(String passwordStr) {  
        String regexZ = "\\d*";  
        String regexS = "[a-zA-Z]+";  
        String regexT = "\\W+$";  
        String regexZT = "\\D*";  
        String regexST = "[\\d\\W]*";  
        String regexZS = "\\w*";  
        String regexZST = "[\\w\\W]*";  
  
        if (passwordStr.matches(regexZ)) {  
            return "weak";  
        }  
        if (passwordStr.matches(regexS)) {  
            return "weak";  
        }  
        if (passwordStr.matches(regexT)) {  
            return "weak";  
        }  
        if (passwordStr.matches(regexZT)) {  
            return "medium";  
        }  
        if (passwordStr.matches(regexST)) {  
            return "medium";  
        }  
        if (passwordStr.matches(regexZS)) {  
            return "medium";  
        }  
        if (passwordStr.matches(regexZST)) {  
            return "strong";  
        }  
        return passwordStr;  
  
    }  

	public RegisterUI(Socket client) {// 根据登录界面的返回，确定服务器地址
		this();
		this.client = client;
	}

	public void register() {
		try {
			send = new PrintWriter(client.getOutputStream(), true);// 发送信息，且刷新
			receive = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String name = tfUsername.getText();
			String password = new String(tfPassword.getPassword());
			send.println("REG");
			send.println(name);
			send.println(password);
			receive.readLine();
			//String header = receive.readLine();
			String data = receive.readLine();
			switch (data) {
			case "Exist":
				final Dialog exError = new Dialog(this, "Username has existed", true);
				exError.add(new Label("Username has existed, please try others"));
				exError.setBounds(600, 200, 180, 100);
				exError.setLayout(new FlowLayout());
				JButton exButton = new JButton("OK");
				exError.add(exButton);
				exButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						exError.dispose();
					}
				});
				exError.setVisible(true);
				break;
			case "Success":
				final Dialog reS = new Dialog(this, "Register Success", true);
				reS.add(new Label("Congratulations! Register Success"));
				reS.setBounds(600, 200, 180, 100);
				reS.setLayout(new FlowLayout());
				JButton reSButton = new JButton("OK");
				reS.add(reSButton);
				reSButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						reS.dispose();
						RegisterUI.this.dispose();
					}
				});
				reS.setVisible(true);
				break;
			default:
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
