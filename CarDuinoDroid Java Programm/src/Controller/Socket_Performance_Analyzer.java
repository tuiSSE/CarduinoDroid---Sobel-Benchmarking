package Controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

//import swp.tuilmenau.carduinodroid.model.LOG;

public class Socket_Performance_Analyzer implements Runnable {

	BufferedWriter performance_Analyzer_Writer;
	Socket socket_performance_Analyzer;
	String mobilephone_ip;
	
	BufferedReader performance_Analyzer_Reader;
	
	Socket_Performance_Analyzer(){
	}

	/**
	 * Connect the Socket to the Android-Application
	 * @param nport_picture The Socketaddress
	 */
	public boolean connect(InetSocketAddress port_Performance_Analyzer)
	{
		try {
			socket_performance_Analyzer = new Socket();
			socket_performance_Analyzer.connect(port_Performance_Analyzer,5000);
		} catch (IOException e) {	
			System.out.println("Socket_Picture - Fehler beim Connecten");
		}
		
		try {
			performance_Analyzer_Writer = new BufferedWriter(new OutputStreamWriter(socket_performance_Analyzer.getOutputStream()));
			performance_Analyzer_Reader = new BufferedReader(new InputStreamReader(socket_performance_Analyzer.getInputStream()));
		} catch (IOException e) {
			System.out.println("Socket_Performance_Analyzer - Fehler beim Inputstream");
		}
		if(socket_performance_Analyzer.isConnected()) {
			System.out.println("Socket_Performance_Analyzer - Connected");
			return true;
		}else return false;
	}
		
	
	public void close() {
		try {
			socket_performance_Analyzer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Socket_Performance_Analyzer - Fehler beim Schlie§en des Streams");
		}
	}

	public boolean sendContent(String content){
		if(socket_performance_Analyzer!=null){
			if(!socket_performance_Analyzer.isClosed())
			{
				try {
					performance_Analyzer_Writer.write(content);
					performance_Analyzer_Writer.newLine();
					performance_Analyzer_Writer.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("fail");
				}
				return true;
			}
			else{
				return false;
			}
		} else return false;
	}
	
	public boolean send_requestTimeStamp(String request) {
		return sendContent("1;" + request);
	}
	
	private String[] receive(){
		String[] tokens = {};
		try {
			if (performance_Analyzer_Reader.ready()) {

				String message = (String) performance_Analyzer_Reader.readLine();
				System.out.println(message);
				
				tokens = tokenize(message);
			} else {
				System.out.println("Performance_Analyzer - Stream not ready");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block trim
			e.printStackTrace();
		}
		return tokens;
	}

	private String[] tokenize(String message) {
		String[] tokens = message.split(";",-1);
		for (int i = 0; i < tokens.length; i++) 
			tokens[i] = tokens[i].trim(); 
		return tokens;
	}
	
	private long receiveTimeStamp(){
		while(true){
			try {
				if(socket_performance_Analyzer.getInputStream().available() > 0){
					String[] tokens = receive();
					if (tokens.length > 0) {
						if (tokens[0].equals("1")) {
							return Long.valueOf(tokens[1]);
						} else {
							//throw new RuntimeException("Socket_Performance_Analyzer - Decoding package failed");
							return -1;
						}
					} else {
						return -1;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
//	private long receiveTimeStamp(){
////			if(picturestream!=null){
////				i =picturestream.available();
////				if(i > 0){
////					network.receive_picture(readpictureWithTimeStamp());
////					network.receive_frameRate(_frameRate);
////					network.receive_Absolute_Delay();
////				//System.out.println("was da zum lesen" + i + "      " + System.currentTimeMillis());
////				}
//			String[] tokens = receive();
//			if (tokens.length > 0) {
//				if (tokens[0].equals("1")) {
//					return Long.valueOf(tokens[1]);
//				} else {
//					//throw new RuntimeException("Socket_Performance_Analyzer - Decoding package failed");
//					return -1;
//				}
//			} else {
//				return -1;
//			}
//	}


	@Override
	public void run() {
//		while(true){
//			receive_statistics_package();
//		}		
	}

	public long receive_requestedTimeStamp() {		
		return receiveTimeStamp();
	}

}

