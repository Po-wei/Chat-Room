
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JTextArea;

public class Server extends Thread
{
	private int port = 8787;
	private JTextArea area;
	private ServerSocket server;
	private boolean stop = false;
	private HashMap<String, Socket> userMap = new HashMap<>();
	private HashMap<String, String[]> group = new HashMap<>();
	private ListThread listUpdateThread;

	public Server(int port, JTextArea textArea)
	{
		this.port = port;
		this.area = textArea;
	}
	//MAIN THREAD!!!!!!!!!!!
	public void run()
	{
		InetAddress ip = null;
		try
		{
			ip = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e1)
		{
			e1.printStackTrace();
		}
		try
		{
			server = new ServerSocket(port, 100);
//			register(area);
			area.append("Server address :" + ip + "\nPort number : " + port + "\r\n");
			area.append("Server start !\r\n");
			
			listUpdateThread = new ListThread(userMap);
			listUpdateThread.start();
			
			
			
			
			int i = 0;
			while (!stop)
			{
				Socket client = server.accept();// Blocking thread 
				System.out.println(client.getInetAddress());
				System.out.println(client.getPort());
				Thread test = new Thread(new ClientThread(client, userMap,group, area), "ClientThread" + (i++));
				test.start();
				
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	
//	public void exit()
//	{
//		try
//		{
//	//TODO USE VECTOR TO MANGE
//			if (server != null)
//			{
//				this.stop = true;
//				this.interrupt();
//				listUpdateThread.interrupt();
//				server.close();
//				Set<String> keySet = userMap.keySet();
//
//				for(String s : keySet)
//				{
//					userMap.get(s).stop();
//				}
//				area.append("Close Server\r\n");
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
}
