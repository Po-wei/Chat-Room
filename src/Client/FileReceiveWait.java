
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class FileReceiveWait
{
	private JFrame frame;
	private JTextField textFieldSpeed;
	private ServerSocket serverSocketFile = null;
	private Socket socketFile = null;
	// 与文件有关的
	private File fileReceive = null;
	private FileOutputStream fileOutputStream = null;
	private BufferedOutputStream bufferedOutputStream = null;
	// 与网络有关的
	private DataInputStream dataInputStream = null;
	private DataOutputStream dataOutputStream = null;
	private BufferedInputStream bufferedInputStream = null;
	private String fileName = null;
	private long fileLength = 0;
	private JProgressBar progressBar;
	private JButton btnTerminate;
	private boolean receiveOk = false;
	private String sender;

	public FileReceiveWait(String sender)
	{
		this.sender = sender;
		frame = new JFrame("接收文件");
		frame.setBounds(120, 120, 470, 100);
		frame.setResizable(false);
		frame.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		frame.getContentPane().add(progressBar);
		textFieldSpeed = new JTextField("理论上这里显示的是速度");
		textFieldSpeed.setEditable(false);
		frame.getContentPane().add(textFieldSpeed);
		textFieldSpeed.setColumns(10);
		btnTerminate = new JButton("终止");
		frame.getContentPane().add(btnTerminate);
		frame.setVisible(false);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addListener();
		new FileReceiveThreadWait().start();
	}

	private void closeStream() throws IOException
	{
		bufferedOutputStream.flush();
		dataOutputStream.flush();
		dataOutputStream.close();
		bufferedOutputStream.close();
		fileOutputStream.close();
	}

	private void addListener()
	{
		btnTerminate.addActionListener(new btnListener());
	}

	public void setSender(String sender)
	{
		this.sender = sender;
	}

	class btnListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (arg0.getSource() == btnTerminate)
			{
				int n = JOptionPane.showConfirmDialog(frame, "确定终止吗？", "终止文件传输", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION)
				{
					try
					{
						socketFile.close();
						JOptionPane.showMessageDialog(frame, "文件传输中止！" + "文件已传输" + progressBar.getValue() + "%");
						frame.setVisible(false);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	class FileReceiveThreadWait extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				serverSocketFile = new ServerSocket(6667);// 6666
				while (true)
				{
					socketFile = serverSocketFile.accept();
					bufferedInputStream = new BufferedInputStream(socketFile.getInputStream());
					dataInputStream = new DataInputStream(bufferedInputStream);
					fileName = dataInputStream.readUTF();
					fileLength = dataInputStream.readLong();
					double fileLengthShow = 0;
					int n = 0;
					// 如果大于1GB，1MB，1KB
					if (fileLength > 1024 * 1024 * 1024)
					{
						fileLengthShow = (fileLength / (1024.0 * 1024.0 * 1024.0));
						n = JOptionPane.showConfirmDialog(frame,
								sender + "发送文件" + fileName + "大小" + fileLengthShow + "GB" + "，是否接收？", "接收文件确认",
								JOptionPane.YES_NO_OPTION);
					}
					else if (fileLength > 1024 * 1024)
					{
						fileLengthShow = (fileLength / (1024.0 * 1024.0));
						n = JOptionPane.showConfirmDialog(frame,
								sender + "发送文件" + fileName + "大小" + fileLengthShow + "MB" + "，是否接收？", "接收文件确认",
								JOptionPane.YES_NO_OPTION);
					}
					else if (fileLength > 1024)
					{
						fileLengthShow = (fileLength / (1024.0));
						n = JOptionPane.showConfirmDialog(frame,
								sender + "发送文件" + fileName + "大小" + fileLengthShow + "KB" + "，是否接收？", "接收文件确认",
								JOptionPane.YES_NO_OPTION);
					}
					else if (fileLength < 1024)
					{
						fileLengthShow = fileLength;
						n = JOptionPane.showConfirmDialog(frame,
								sender + "发送文件" + fileName + "大小" + fileLengthShow + "B" + "，是否接收？", "接收文件确认",
								JOptionPane.YES_NO_OPTION);
					}
					if (n == JOptionPane.YES_OPTION)
					{
						selectFile();
					}
					else
					{
						socketFile.close();
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		private void selectFile()
		{
			FileDialog dialog = new FileDialog(frame, "保存位置", FileDialog.SAVE);
			dialog.setFile(fileName);
			dialog.setVisible(true);
			if (dialog.getFile() != null)
			{
				fileReceive = new File(dialog.getDirectory() + dialog.getFile());
				new FileReceiveThread().start();
				frame.setVisible(true);
			}
		}
	}

	class FileReceiveThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				fileOutputStream = new FileOutputStream(fileReceive);
				bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
				dataOutputStream = new DataOutputStream(bufferedOutputStream);
				int n = 0;
				int progress = 0;
				int i = 1;
				while ((n = dataInputStream.read()) != -1)
				{
					i++;
					progress = (int) (100 * (i * 1.0 / fileLength));
					progressBar.setValue(progress);
					dataOutputStream.write(n);
					if (i == fileLength)
					{
						JOptionPane.showMessageDialog(frame, "文件传输完成");
						receiveOk = true;
						closeStream();
						frame.dispose();
					}
				}
				closeStream();
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				if (receiveOk)
				{
				}
				else
				{
					JOptionPane.showMessageDialog(frame, "文件传输终止！");
				}
				e.printStackTrace();
			}
		}
	}
}
