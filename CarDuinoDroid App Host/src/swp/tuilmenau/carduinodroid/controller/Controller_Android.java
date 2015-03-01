package swp.tuilmenau.carduinodroid.controller;


import android.app.Activity;
import android.os.BatteryManager;
import android.util.Log;
import swp.tuilmenau.carduinodroid.model.LOG;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;

/**
 * Wraps all other classes into this class to be used by the activity.
 * 
 * @author Paul Thorwirth
 * @author Lars
 * @version 1.0
 */
public class Controller_Android 
{
	public LOG log;
	
	public Cam cam;
	public Connection connection;
	public GPS gps;
	public Network network;
	public Record_Sound record_sound;
	public Sound sound;
	
	public Activity ACTIVITY;
	private static UsbController sUsbController;
	private static final int VID = 0x2341;
	private static final int PID = 0x0043;//I believe it is 0x0000 for the Arduino Megas
	public boolean sent;
	public boolean sentfree;
	
	/**
	 * Calls the Constructor of all other sub-classes.
	 * 
	 * @param activity the current Activity
	 */
	public Controller_Android(Activity activity) 
	{
		log = new LOG();
		//test
		ACTIVITY = activity;
		cam = new Cam(this, activity);
		connection = new Connection(activity, log);
		gps = new GPS(this, activity, log);
		record_sound = new Record_Sound(log);
		sound = new Sound(activity, log);
		sent=false;

		final Controller_Android temp = this;
		
		new Thread(new Runnable()
		{
			public void run() 
			{
				@SuppressWarnings("unused")
				Network network = new Network(temp);
			}
		}).start();
	}

	/**
	 * Collects all Info and prepares the Info-Package to be sent to the PC-Client.
	 *
	 * @return A {@link String} containing the compressed Data.
	 */
	public String packData() 
	{
		String data;
		data = "1;";
		if (connection.getMobileAvailable()) data = data + 1 + ";"; 
										else data = data + 0 + ";";
		if (connection.getWLANAvailable())   data = data + 1 + ";"; 
										else data = data + 0 + ";";	
		if (connection.getMobile())			 data = data + 1 + ";"; 
										else data = data + 0 + ";";
		if (connection.getWLAN()) 			 data = data + 1 + ";"; 
										else data = data + 0 + ";";	
		
		data = data + gps.getGPS() + ";";

		return data;
	}

	/**
	 * Used to decode a packed command String received from the PC. 
	 * After the decode the commands are executed.
	 *
	 * @param data A String containing the compressed Data as follows:
	 * <ol>
	 * 	<li>Control Signals with settings</li>
	 * 	<li>Camera Settings</li>
	 *	<ol>
	 *		<li>Front or Back Camera</li>
	 * 		<li>Camera Resolution</li>
	 * 		<li>Camera Light</li>
	 * 		<li>Quality</>
	 * 	</ol>
	 * 	<li>Sound Signals</li>
	 * 	<ol>
	 * 		<li>Play a Sound by Android phone</li>
	 * 		<li>Start or Stop a Record</li>
	 * 	</ol>
	 * </ol>
	 */
	public void receiveData(String data)
	{
		final String TRUE_STRING = "true";
		
		boolean front, right;
		String[] parts = data.split(";",-1);

		switch (Integer.parseInt(parts[0]))
		{
			case 1: // Everything for control signals
			{
					sent=false;
					if(sUsbController == null){
						sUsbController = new UsbController(ACTIVITY, mConnectionHandler, VID, PID, log, this);
						log.write(LOG.INFO, "New USBController");}
					/*else{
						sUsbController.stop();
						sUsbController = new UsbController(ACTIVITY, mConnectionHandler, VID, PID, log, this);
						log.write(LOG.INFO, "USBController.stop und New USB Controller");
					}*/ //Just to have the power for closing the USB-Controller an enumerate again
					byte[] commando = new byte[4];
					front = (parts[2].equals(TRUE_STRING));
					right = (parts[4].equals(TRUE_STRING));
					commando[0] = Byte.parseByte(parts[1]);
					commando[1] = (byte)(front?1:0);
					commando[2] = Byte.parseByte(parts[3]);
					commando[3] = (byte)(right?1:0);
					
					if(sUsbController != null){
						sUsbController.send(commando);
					}
	
			} break;
			
			case 2: // Everything for camera settings
			{
				switch (Integer.parseInt(parts[1]))
				{
					case 1:
					{
						cam.switchCam(Integer.parseInt(parts[2])); //Anpassen mit Robin
						log.write(LOG.INFO, "Switched Camera");
					} break;
					case 2:
					{
						Log.wtf("cam", "reschange" + parts[2]);
						cam.changeRes(Integer.parseInt(parts[2])); //Anpassen mit Robin
					} break;
					
					case 3:
					{
						if (Integer.parseInt(parts[2]) == 1) cam.enableFlash();
						if (Integer.parseInt(parts[2]) == 0) cam.disableFlash();
					} break;
					
					case 4:
					{
						cam.setQuality(Integer.parseInt(parts[2]));
					} break;
					
					case 5:  //GB
					{
						if (Integer.parseInt(parts[2]) == 1) cam.setFilterImage(true);
						if (Integer.parseInt(parts[2]) == 0) cam.setFilterImage(false);
					} break;
			
					default: log.write(LOG.WARNING, "Unknown camera command from PC"); break;
				}
			} break;
			
			case 3: // Everything with sounds
			{
				switch (Integer.parseInt(parts[1]))
				{
					case 1: 
					{	
						sound.horn();
					} break;
					
					case 2:
					{
						if (Integer.parseInt(parts[2]) == 0) 
						{
							record_sound.stop();
						}
						else 
						{
							record_sound.start();
						}
					} break;
				
					default: log.write(LOG.WARNING, "Unknown Sound command from PC"); break;
				}
			} break;

			default: log.write(LOG.WARNING, "unknown command from PC"); break;
		}
	}

	/**
	 * Sets the Network
	 * 
	 * @param network the Network to set.
	 */
	public void setNetwork(Network network) 
	{
		this.network = network;
	}
	
	public void SetSent(boolean Sent){
		sent = Sent;
	}
	
	public boolean GetSent(){
		return sent;
	}
	
	private final IUsbConnectionHandler mConnectionHandler = new IUsbConnectionHandler() {
		public void onUsbStopped() {
			log.write(LOG.INFO, "USB Connection gestoppt");
		}
		
		public void onErrorLooperRunningAlready() {
			log.write(LOG.INFO, "Looper läuft schon");
		}
		
		public void onDeviceNotFound() {
			if(sUsbController != null){
				sUsbController.stop();
				sUsbController = null;
			}
			log.write(LOG.INFO, "Device nicht gefunden");
		}
	};
}