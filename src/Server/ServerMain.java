
import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class ServerMain
{
	public static void main(String[] args)
	{
		try
		{
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			{
				if ("Nimbus".equals(info.getName()))
				{
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("theme initialization failed");
		}
		/**
		 * Launch the application.
		 */
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					ServerUI frame = new ServerUI();
					frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
