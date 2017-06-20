import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JTextArea;

public class ClientThread implements Runnable
{
	
	private HashMap<String, Socket> userList = null;
	private HashMap<String, String[]> m_group;
	private JTextArea area = null;
	private boolean login = false;// 
	
	//member start with m_...
	private Socket m_client = null;
	private String m_user;
	private String m_password;
	private BufferedReader m_receiver = null;
	private PrintWriter m_sender = null;
	private String message = null;
	
	
	private File userFile; //global reference

	public ClientThread(Socket client, HashMap<String, Socket> userList,HashMap<String, String[]> group, JTextArea area) throws IOException
	{
		this.m_client = client;  //pass reference 
		this.userList = userList;
		this.area = area;
		this.m_group = group;
		
		userFile = new File(".\\list.txt");
		if (!userFile.exists())
		{
			userFile.createNewFile();
		}
	}

	public void stop()
	{
		try
		{
			m_sender.println("server closed");
			userList.remove(m_user);
			m_client.close();

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
	//Main function
	public void run()
	{
		try
		{
			String type;
			InetAddress ip = m_client.getInetAddress();
			m_receiver = new BufferedReader(new InputStreamReader(m_client.getInputStream()));
			m_sender = new PrintWriter(m_client.getOutputStream(), true);
			
			great_loop:
			while(true)
			{
				//READ HEADER
				type = m_receiver.readLine();
				System.out.println("type:" + type);
				switch(type)
				{
				case "REG":
					register();
					break;
				case "LOG":
					login();
					break;
				case "MSG":
					getMessage();
					break;
				case "FILE":
					getRequest();
					break;
				case "GRP":
					System.out.println("GRP");
					createGroup();
					break;
				case "CMG":
					String cmg = m_receiver.readLine();
					if(cmg.equals("offline"))
					{
						exit();
						break great_loop;
					}
					break;
				default:
					break;
				}
			}
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("CLient thread stop!");
		
	}
	
	private void register() throws IOException
	{
		//lock
		synchronized (userFile)
		{
			//critical section
			userFile = new  File(".\\list.txt");
			BufferedReader regList = new BufferedReader(new FileReader(userFile));
			area.append("Getting information from disk...");
			
			String name = m_receiver.readLine();
			String password = m_receiver.readLine();
			String info = null;
			boolean exist = false;
			while ((info = regList.readLine()) != null)
			{
				String str[] = info.split("::");
				if (str[0].equals(name))
				{
					m_sender.println("CMG");
					m_sender.println("Exist");
					exist = true;
					area.append(name + "Username already exist !\r\n");
					break;
				}
			}
			area.append(name + "::" + password + "\r\n");
			if (!exist)
			{
				area.append("Storing new username...\r\n");
				PrintWriter writerList = new PrintWriter(new FileOutputStream(".\\list.txt", true), true);//////////////////////////////////
				writerList.println(name + "::" + password);
				m_sender.println("CMG");
				m_sender.println("Success");
				area.append("Done !\n");
			}
		}
	}

	public void login() throws IOException, InterruptedException
	{
		String info = null;
		synchronized (userFile) //lock
		{
			//critical section
			userFile = new  File(".\\list.txt");
			BufferedReader logList = new BufferedReader(new FileReader(userFile));
			m_user = m_receiver.readLine();
			m_password = m_receiver.readLine();
			
			while ((info = logList.readLine()) != null)
			{
				String str[] = info.split("::");
				if (m_user.equals(str[0]) && m_password.equals(str[1]) && !alreadyLogin(m_user))
				{
					area.append("user:" + m_user + "  login successfully\n");
					login = true;
					break;
				}
			}
			if (login)
			{
				//POROCOL SEND
				m_sender.println("CMG");
				m_sender.println("Success");
				userList.put(m_user,m_client);
				Thread.sleep(100);
				checkOffline();
				return;
			}
			else
			{
				if (alreadyLogin(m_user))
				{
					m_sender.println("CMG");
					m_sender.println("Repetition");
				}
				else
				{
					m_sender.println("CMG");
					m_sender.println("Failed");
				}
					
			} 
			area.append(m_user + "try to login!\n");
		}
	}

	public void exit()
	{
		try
		{
			//TODO notify other thread 
			userList.remove(m_user);
			m_sender.println("CMG");
			m_sender.println("offline");
			m_client.close();
			area.append(m_user + "  logout!\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public boolean alreadyLogin(String user)
	{
		if (userList.containsKey(user))
		{
			return true;
		}
		else
			return false;
	}

	private void getMessage() throws IOException
	{
		//SETTING OUR PROTOCOL !!!!!!!!!!!!!!
		String src;
		String dest;
		src = m_receiver.readLine();
		dest = m_receiver.readLine();
		message = m_receiver.readLine();
		sendMsg(dest,message);
	}
	
	private void sendMsg(String dest,String message) throws IOException
	{
		Set<String> userSet = userList.keySet();

		// TODO Check user is online
		for(String s : userSet)  // C++11 range-base for
		{
			if(s.equals(dest))
			{
				//TODO LOCK
				PrintWriter destSock = new PrintWriter(userList.get(s).getOutputStream(), true);
				destSock.println("MSG");
				destSock.println(m_user);
				destSock.println(dest);
				destSock.println(message);
				return;
			}
		}
		
		if(dest.equals("All"))
		{
			sendToAll(message);
			return;
		}
		
		//GROUP
		if( m_group.containsKey(dest))
		{
			String[] receivers = m_group.get(dest);
			for(String str : receivers)
			{
				if(str.equals(m_user))
				{
					for(String s : receivers)
					{
						if(!userList.containsKey(s)) //online
						{
							continue;
						}
						PrintWriter groupSock = new PrintWriter(userList.get(s).getOutputStream(), true);
						groupSock.println("MSG");
						groupSock.println("Group: " + dest + "'s " + m_user);
						groupSock.println(s);
						groupSock.println(message);
					}
					break;
				}
			}
			
		}
		
				
		//OFFLINE
		String path = ".\\offline\\";
		String filename = dest +".log";
		File offlineData = new File(path,filename);
		if (!offlineData.exists())
		{
			offlineData.createNewFile();
		}
		PrintWriter filewriter = new PrintWriter(new FileOutputStream(path + filename, true), true);
		filewriter.println("MSG");
		filewriter.println(m_user);
		filewriter.println(dest);
		filewriter.println(message);
		filewriter.close();
	}

	public void sendToAll(String message) throws IOException
	{
		Set<String> keySet = userList.keySet();
		
		for(String s : keySet)  // C++11 range-base for
		{
			PrintWriter destSock = new PrintWriter(userList.get(s).getOutputStream(), true);
			//TODO LOCK
			destSock.println("MSG");
			destSock.println(m_user);
			destSock.println(s);
			destSock.println(message);
			
		}

	}
	
	private void checkOffline() throws IOException
	{
		String path = ".\\offline\\";
		String filename = m_user +".log";
		String tempLine;
		File offlineData = new File(path + filename);
		
		if (!offlineData.exists())
		{
			return;
		}
		BufferedReader offlineReader = new BufferedReader(new FileReader(offlineData));
		while ((tempLine = offlineReader.readLine()) != null)
		{
			m_sender.println(tempLine);
			System.out.println(tempLine);
		}
		offlineReader.close();
		if(!offlineData.delete())
		{
			System.out.println("WRONG");
		}
		else
		{
			System.out.println(m_user + "send offline message success!");
		}
	}
	
	private void createGroup() throws IOException
	{
		synchronized (userFile)
		{
			System.out.println("INFILE");
			String lineBuffer = m_receiver.readLine();
			String []groupData = lineBuffer.split(":");
			String groupName = groupData[0];
			
			System.out.println(groupName);
			String []members = groupData[1].split(",");
			for(int i = 0 ; i < members.length ; i++)
			{
				System.out.println(members[i]);
			}
			m_group.put(groupName,members);
			PrintWriter writerList = new PrintWriter(new FileOutputStream(".\\list.txt", true), true);
			writerList.println(groupName + "::" +"null");
			
		}
	}
	
	private void getRequest() throws IOException, InterruptedException
	{
		switch(m_receiver.readLine())
		{
		case "0" :
			//for send
			String dest = m_receiver.readLine();
			if(!userList.containsKey(dest))
				return;
			Socket destSocket = userList.get(dest);
			m_sender.println("FILE");
			m_sender.println("1");
			m_sender.println(destSocket.getInetAddress());
			System.out.println(destSocket.getInetAddress());
			
			//for recv
			Thread.sleep(100);
			PrintWriter destPrinter = new PrintWriter(destSocket.getOutputStream(), true);
			destPrinter.println("FILE");
			destPrinter.println("2");
			destPrinter.println(m_user);
			break;
		default:
			break;
		}
	}
	
	
	
	

}
