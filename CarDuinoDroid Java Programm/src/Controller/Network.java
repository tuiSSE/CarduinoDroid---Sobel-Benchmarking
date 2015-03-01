/*
 * NOCH NICHT FERTIG BZW EPRÜFT MUSS NOCH ÜBERARBEITET WERDEN
 */

/*
 *ip wo hin?
 * 
 */
package Controller;
import java.net.*;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;


/**
 * This Class is responsible for the communication with the android-application
 * @author Robin
 * @version 18.06.2012
 */

public class Network {
	
	Controller_Computer controller_computer;
	Socket_Picture socket_picture;
	Socket_Package socket_package;
	Socket_Controller socket_controller;
	Socket_Performance_Analyzer socket_performance_Analyzer;
	String mobilephone_ip;
	Camera_Picture camera_picture;
	Packagedata packagedata;
	Thread t1;
	Thread t2;
	Thread t3;
	Performance_Analyzer performance_Analyzer;
	private long _offTime;
	
	public Network(Packagedata n_packagedata, Camera_Picture n_camera_picture, Performance_Analyzer n_performance_Analyzer, Controller_Computer ControllerComputer)
	{
		controller_computer = ControllerComputer;
		socket_picture = new Socket_Picture(this);
		socket_package = new Socket_Package(this);
		socket_controller = new Socket_Controller();
		socket_performance_Analyzer = new Socket_Performance_Analyzer();
		camera_picture = n_camera_picture;
		packagedata = n_packagedata;
		performance_Analyzer = n_performance_Analyzer;
	}
	
//	public Network(String ip)
//	{
//		socket_picture = new Socket_Picture(this);
//		socket_package = new Socket_Package(this);
//		socket_controller = new Socket_Controller();
//		mobilephone_ip = ip;
//	}
	/*
	 * ports = ???????
	 */
	
	/**
	 * This method connects with the Android-Application
	 * @param ipstring
	 */
	public void connect(String ipstring)
	{
		InetAddress ip;
		mobilephone_ip = ipstring;
		boolean infosocket_picture = false, infosocket_controller = false, infosocket_package=false, infosocket_performance_Analyzer=false;
		
		_offTime = 0;
		//_offTime = receive_network_Statistics(); //***
		
		try {
			ip = InetAddress.getByName(ipstring);
			InetSocketAddress port_controll = new InetSocketAddress(ip, 12345);
			InetSocketAddress port_package = new InetSocketAddress(ip, 12346);
			InetSocketAddress port_picture = new InetSocketAddress(ip, 12347);
			InetSocketAddress port_performance_Analyzer = new InetSocketAddress(ip, 12348);
			infosocket_picture = socket_picture.connect(port_picture);
			infosocket_controller = socket_controller.connect(port_controll);
			infosocket_package = socket_package.connect(port_package);
			infosocket_performance_Analyzer = socket_performance_Analyzer.connect(port_performance_Analyzer); //GB
			t1 = new Thread(socket_picture);
			t2 = new Thread(socket_package);
			t3 = new Thread(socket_performance_Analyzer); //GB
			t1.start();
			t2.start();
			t3.start(); //GB
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(infosocket_picture!=true){controller_computer.log.writelogfile("Socket_Picture - Fehler beim Verbinden");}
		if(infosocket_controller!=true){controller_computer.log.writelogfile("Socket_Controller - Fehler beim Verbinden");}
		if(infosocket_package!=true){controller_computer.log.writelogfile("Socket_Package - Fehler beim Verbinden");}
		if(infosocket_performance_Analyzer!=true){controller_computer.log.writelogfile("Socket_Performance_Analyzer - Fehler beim Verbinden");} //GB
	}
	/*
	 * 1 = controllsignal
	 * 2 = settings
	 * 3 = sound
	 */
	
	
	/**
	 * @param direction
	 * @return
	 * @see Socket_Controller#send_controllsignal(String)
	 */
	public boolean send_controllsignal(String direction)
	{
		return socket_controller.send_controllsignal(direction);
	}
	
	
	/**
	 * @param settings
	 * @return
	 * @see Socket_Controller#send_camera_settings(String)
	 */
	public boolean send_camera_settings(String settings)
	{
		return socket_controller.send_camera_settings(settings);
	}
	
	/**
	 * @param sound_id
	 * @return
	 * @see Socket_Controller#send_sound(String)
	 */
	public boolean send_sound(String sound_id)
	{
		return socket_controller.send_sound(sound_id);
	}
	
	/**
	 * Transfer the message to the Packagedata
	 * @param message
	 * @see Packagedata#receive_package(String)
	 */
	public void receive_package(String message) {
		// TODO Auto-generated method stub
		packagedata.receive_package(message);
	}
	
	/**
	 * Transfer the image to the Camera_Picture
	 * @param bufferedImage
	 * @see Camera_Picture#receive_picture(ImageIcon)
	 */
	public void receive_picture(BufferedImage bufferedImage) {
		camera_picture.receive_picture(bufferedImage);
	}
	
	public void close() {
		socket_picture.close();
		socket_package.close();
		socket_controller.close();
	}
	
	//GB
	public void receive_frameRate(float frameRate) {
		camera_picture.receive_frameRate(frameRate);
	}
	
	//GB	
	public long receive_network_Statistics(){
		_offTime = performance_Analyzer.syncGlobalTime();
		return _offTime;
	}
		
	//GB
	public boolean send_requestTimeStamp(String request) {
		return socket_performance_Analyzer.send_requestTimeStamp(request);
	}
	
	//GB
	public long receive_requestedTimeStamp(){
		return socket_performance_Analyzer.receive_requestedTimeStamp();
	};
	
	//GB
	public void receive_absoluteDelay(){
		long absoluteDelay = socket_picture.getDelay() - _offTime;
		camera_picture.receive_Absolute_Delay(absoluteDelay);
	};
}
