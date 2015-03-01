package swp.tuilmenau.carduinodroid.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import swp.tuilmenau.carduinodroid.model.LOG;

public class Socket_Performance_Analyzer implements Runnable{

	BufferedWriter performance_Analyzer_Writer;
	ServerSocket socket_performance_Analyzer;
	Socket client;
	
	Controller_Android controller_Android;
	BufferedReader performance_Analyzer_Reader;
	private Network network;

	public Socket_Performance_Analyzer(Controller_Android n_controller_Android,
			Network nnetwork) {
		controller_Android = n_controller_Android;
		network = nnetwork;
	}

	/**
	 * transmits Timestamp through socket
	 */
	public boolean sendTimeStamp() {		
		String timeStamp = String.valueOf(System.currentTimeMillis());
		
		return sendContent("1;" + timeStamp);
	}
	
	/**
	 * transmits message through socket
	 */
	public boolean sendContent(String content){
		if (client.isConnected()) {
			try {
				performance_Analyzer_Writer.write(content);
				performance_Analyzer_Writer.newLine();
				performance_Analyzer_Writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			return true;
		}

		else {
			return false;
		}
	}
	
	/**
	 * waits for a TimeStampRequest to appear
	 * @return true if TimeStamp requested
	 */
	private boolean listen(){
		try {
			if (performance_Analyzer_Reader.ready()) {

				String message = (String) performance_Analyzer_Reader.readLine();
				controller_Android.log.write(LOG.INFO, message);

				if (message.charAt(0) == '1') {
					return true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void close() {
		try {
			socket_performance_Analyzer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		ServerSocket socket_performance_Analyzer = null;
		while (true) {
			client = null;
			performance_Analyzer_Writer = null;

			try {
				if (socket_performance_Analyzer == null)
					socket_performance_Analyzer = new ServerSocket(12348);
				client = socket_performance_Analyzer.accept();
				performance_Analyzer_Writer = new BufferedWriter(new OutputStreamWriter(
						client.getOutputStream()));
				performance_Analyzer_Reader = new BufferedReader(new InputStreamReader(
						client.getInputStream()));

			} catch (IOException e) {
				e.printStackTrace();
			}


			while (!client.isClosed()) {

				if(client != null){
					boolean requestReceived = listen();
					
					if(requestReceived){
						sendTimeStamp();
					}
				}
			}			
		} 
		
	}

}
