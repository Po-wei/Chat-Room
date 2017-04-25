
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ListThread extends Thread
{
	private HashMap<String, Socket> m_userMap;
//	private HashMap<String, Boolean> m_userSate;
	private Set<String> userList;
	private ServerSocket server;
	private File userFile;
	private Lock fileLock = new ReentrantLock();
	

	public ListThread(HashMap<String, Socket> userMap)
	{

		try
		{
			server = new ServerSocket(34344);
			m_userMap = userMap;
//			m_userSate = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		try
		{
			while (true)
			{
				Socket clientSocket = server.accept();
				userList = new HashSet<String>();
				new Thread(new innerListThread(clientSocket,m_userMap,userList)).start();
			}
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}
	}

	private class innerListThread implements Runnable
	{
		private Socket m_clientSocket;
		private HashMap<String, Socket> m_userMap;
		private Set<String> m_userList;
		
		private PrintWriter sender;
		private StringBuilder info;

		public innerListThread(Socket client,HashMap<String, Socket> userMap, Set<String> userList)
		{
			m_clientSocket = client;
			m_userMap = userMap;
			m_userList = userList;
		}

		public void run()
		{
			try
			{
			    sender = new PrintWriter(m_clientSocket.getOutputStream(), true);
				
				while (true)
				{
					try
					{
						Thread.sleep(300);
						
						if(fileLock.tryLock()) // trylock  won't block if don't get lock.
						{
							userFile = new File("E:\\list.txt"); // all 
							BufferedReader userListReader = new BufferedReader(new FileReader(userFile));
							String temp;
							while ((temp = userListReader.readLine()) != null)
							{
								String str[] = temp.split("::");
								m_userList.add(str[0]);
							}
							fileLock.unlock();
						}
						
						info = new StringBuilder();
						Set<String> keySet = m_userMap.keySet();
						
						for(String s : m_userList)
						{
							if(keySet.contains(s))
							{
								info.append("*" + s + ":");
							}
							else
							{
								info.append(s + ":");
							}
						}
						sender.println(info);
						
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void exit()
	{
		try
		{
			if (server != null)
				server.close();
			server = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}

