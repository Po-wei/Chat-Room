
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileSend
{
	private File fileout = null;
	private FileInputStream fileInputStream = null;
	private BufferedInputStream bufferedInputStreamFile = null;
	private long fileLength;
	private String fileName = null;

	private DataInputStream inputStream = null;
	private DataOutputStream outputStream = null;
	private BufferedOutputStream bufferedOutputStreamNet = null;

	private JFrame frameMain = null;
	private JTextField textFieldSelect;
	private JButton btnSelect;
	private JProgressBar progressBar;
	private JButton btnCancel;
	private JButton btnSend;

	private int port;
	private String ipString = null;
	private Socket socket = null;
	private boolean sendOk = false;

	private File[] files = null;

	public FileSend(String ipString, int port)
	{
		this.ipString = ipString;
		this.port = port;
		frameMain = new JFrame("File Send");
		frameMain.setResizable(true);
		frameMain.getContentPane().setLayout(null);
		frameMain.setBounds(120, 120, 530, 260);
		frameMain.setVisible(true);
		frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JLabel lblSelectFile = new JLabel("Choose the file：");
		lblSelectFile.setBounds(10, 34, 178, 15);
		frameMain.getContentPane().add(lblSelectFile);

		textFieldSelect = new JTextField();
		textFieldSelect.setBounds(177, 31, 213, 21);
		frameMain.getContentPane().add(textFieldSelect);
		textFieldSelect.setColumns(10);
		
		btnSelect = new JButton("Browse");
		btnSelect.setBounds(409, 30, 93, 23);
		frameMain.getContentPane().add(btnSelect);
		
		btnSend = new JButton("Send");
		btnSend.setBounds(409, 81, 93, 23);
		frameMain.getContentPane().add(btnSend);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setBounds(409, 138, 93, 23);
		frameMain.getContentPane().add(btnCancel);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setBounds(26, 112, 364, 19);
		frameMain.getContentPane().add(progressBar);
		addListener();
		frameMain.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public long getFileLength()
	{
		return fileLength;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setIP(String ip)
	{
		this.ipString = ip;
	}

	private void closeStream() throws IOException
	{
		outputStream.flush();
		outputStream.close();
		bufferedOutputStreamNet.flush();
		bufferedOutputStreamNet.close();
		bufferedInputStreamFile.close();
		fileInputStream.close();
	}

	private void addListener()
	{
		btnSelect.addActionListener(new BtnListener());
		btnSend.addActionListener(new BtnListener());
		btnCancel.addActionListener(new BtnListener());
	}

	/*
	 * 监听点击按钮后的事件
	 */
	class BtnListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (arg0.getSource() == btnSelect)
			{
				selectFile();
			}
			else if (arg0.getSource() == btnSend)
			{
				sendFile();
				btnSelect.setEnabled(false);
				btnSend.setEnabled(false);
			}
			else if (arg0.getSource() == btnCancel)
			{
				int n = JOptionPane.showConfirmDialog(frameMain, "Are you sure to abort？", "File transmission abort!", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION)
				{
					try
					{
						socket.close();
						JOptionPane.showMessageDialog(frameMain, "Transmission abort！" + "File has transmitted" + progressBar.getValue() + "%");
						frameMain.setVisible(false);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					frameMain.dispose();
				}
			}
		}

		private void selectFile()
		{

			JFileChooser fcDlg = new JFileChooser();
			fcDlg.setDialogTitle("请选择文件...");
			FileNameExtensionFilter filter = new FileNameExtensionFilter("JPEG file", "jpg", "jpeg");
			// ("文本文件(*.txt;*.kcd)", "txt", "kcd");
			// fcDlg.setFileFilter(filter);
			// int returnVal = fcDlg.showOpenDialog(null);
			// if (returnVal == JFileChooser.APPROVE_OPTION) {
			// String filepath = fcDlg.getSelectedFile().getPath();
			// textFieldSelect.setText(filepath);
			// }
			fcDlg.setMultiSelectionEnabled(true);
			fcDlg.showOpenDialog(frameMain);
			files = fcDlg.getSelectedFiles();
			textFieldSelect.setText(fcDlg.getCurrentDirectory() + files[0].getName());
			for (int i = 0; i < files.length; i++)
			{
				System.out.println(files[i].getName());
			}
			// fcDlg.setVisible(true);
			// FileDialog fileDialog = new FileDialog(frameMain, "选择文件",
			// FileDialog.LOAD);
			// fileDialog.setVisible(true);
			//
			// if (fileDialog.getFile() != null) {
			// fileDialog.setMultipleMode(true);
			// String fileName = fileDialog.getDirectory()
			// + fileDialog.getFile();
			// textFieldSelect.setText(fileName);
			// try {
			// fileout = new File(fileName);
			// fileInputStream = new FileInputStream(fileout);
			// bufferedInputStreamFile = new BufferedInputStream(
			// fileInputStream);
			// } catch (FileNotFoundException e) {
			// e.printStackTrace();
			// }
			// }
		}

		private void sendFile()
		{

			new FileSendThread().start();

		}

		class FileSendThread extends Thread
		{

			@Override
			public void run()
			{

				for (int k = 0; k < files.length; k++)
				{
					System.out.println("file " + ipString);
					try
					{
						socket = new Socket(ipString, port);
					}
					catch (UnknownHostException e2)
					{
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					catch (IOException e2)
					{
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					System.out.println("In:" + files[k].getName() + " " + files[k].getPath());
					fileout = files[k];
					try
					{
						fileInputStream = new FileInputStream(fileout);
					}
					catch (FileNotFoundException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					bufferedInputStreamFile = new BufferedInputStream(fileInputStream);
					if (fileout != null)
					{
						try
						{
							// TODO 获取文件长度和名称 接收时使用
							fileLength = fileout.length();
							fileName = fileout.getName();
							inputStream = new DataInputStream(socket.getInputStream());
							bufferedOutputStreamNet = new BufferedOutputStream(socket.getOutputStream());
							outputStream = new DataOutputStream(bufferedOutputStreamNet);
							int n = 0;
							int i = 0;
							int progress = 0;

							outputStream.writeUTF(fileName);
							outputStream.writeLong(fileLength);

							while ((n = bufferedInputStreamFile.read()) != -1)
							{
								System.out.println("filepath2" + k);
								i++;
								progress = (int) (100 * (i * 1.0 / fileLength));
								progressBar.setValue(progress);
								outputStream.write(n);
								if (i == fileLength)
								{
									sendOk = true;
									JOptionPane.showMessageDialog(frameMain, "发送完成");
									// System.out.println("filepath"+k);
									break;
								}
							}

						}
						catch (IOException e)
						{
							if (sendOk)
							{
							}
							else
							{
								JOptionPane.showMessageDialog(frameMain, "文件传输终止" + "文件已传输" + progressBar.getValue()
										+ "%" + fileLength * (progressBar.getValue() / 100.0) + "B");
								frameMain.dispose();
							}
							e.printStackTrace();
						}
					}
					try
					{
						// closeStream();
						socket.close();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				frameMain.dispose();
			}

		}
	}
}
