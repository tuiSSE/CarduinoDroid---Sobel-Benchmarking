package Controller;


import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;


/**
 * This class provides methods for receiving images from the Android-Application
 * @author Robin
 * @version 18.06.2012
 */
public class Socket_Picture implements Runnable{

	Socket socket_picture;
	Network network;
	InetSocketAddress port_picture;
	private InputStream picturestream;

	//GB
	private float _frameRate = 0; 
	private long _startTime = System.currentTimeMillis();
	private long _senderTimeStamp = 0;
	private long _receiverTimeStamp = 0;

	Socket_Picture(Network n_network)
	{
		socket_picture = new Socket();
		network = n_network;
		//log = new Log();
	}

	/**
	 * Connect the Socket to the Android-Application
	 * @param nport_picture The Socketaddress
	 */
	public boolean connect(InetSocketAddress nport_picture)
	{
		port_picture = nport_picture;
		try {
			socket_picture = new Socket();
			socket_picture.connect(port_picture,5000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			/*
			 * noch überlegen
			 */			
			System.out.println("Socket_Picture - Fehler beim Connecten");
		}

		try {
			picturestream = socket_picture.getInputStream();
		} catch (IOException e) {
			System.out.println("Socket_Picture - Fehler beim Inputstream");
		}
		if(socket_picture.isConnected()) {
			System.out.println("Socket_Picture - Connected");
			return true;
		}else return false;
	}



	/**
	 * Connect the Socket to the Android-Application
	 */
	public void connect(){
		try {
			socket_picture.connect(port_picture,5000);
			picturestream = socket_picture.getInputStream();
			run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		BufferedImage buffer;
		int i = 0;
		while(!socket_picture.isClosed())
		{
			try {
				if(picturestream!=null){
					i =picturestream.available();
					if(i > 0){
						network.receive_picture(readpictureWithTimeStamp()); //GB
						network.receive_frameRate(_frameRate); //GB
						network.receive_absoluteDelay();  //GB
						//System.out.println("was da zum lesen" + i + "      " + System.currentTimeMillis());
					}

					else
					{
						try {
							Thread.sleep(25);
						} catch (InterruptedException e) {
							System.out.println("Socket_Picture - Fehler beim Schlafen");
						}
					}
				}
			} catch (IOException e) {
				System.out.println("Socket_Picture - Fehler beim Lesen");
			}
		}
	}


	/**
	 * reads the imagedata from the Socket
	 * @return returns the Image
	 */
	private BufferedImage readpicture() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		boolean completeImage = false;
		byte[] lastBytes = new byte[2];

		try {
			int i = picturestream.available();	
			while(!completeImage){
				byte[] buffer = new byte[i];
				picturestream.read(buffer);
				if(i >= 4){
					lastBytes[0] = buffer[i - 2];
					lastBytes[1] = buffer[i - 1];
					//			System.out.println("daten holen" + new String(lastBytes));
				}else if(i == 1){
					lastBytes[0] = lastBytes[1];
					lastBytes[1] = buffer[0];
				}

				if(lastBytes[0] == (byte)255 && lastBytes[1] ==  (byte)217){
					//		System.out.println("komplettes bild");
					completeImage = true;
				}
				baos.write(buffer);
				i = picturestream.available();
			}

			byte[] image = baos.toByteArray();
			BufferedImage picture = ImageIO.read(new ByteArrayInputStream(image));

			return picture;
		} catch (IOException e) {
		}
		return null;
	}

	/**
	 * reads imagedata and timestamp from Socket
	 * @return Image as BufferedImage
	 */
	//GB
	private BufferedImage readpictureWithTimeStamp() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		boolean completeImage = false;
		byte[] lastBytes = new byte[2];

		try {
			int i = picturestream.available();	
			while(!completeImage){
				byte[] buffer = new byte[i];
				picturestream.read(buffer);
				if(i >= 4){
					lastBytes[0] = buffer[i - 2];
					lastBytes[1] = buffer[i - 1];
					//			System.out.println("daten holen" + new String(lastBytes));
				}else if(i == 1){
					lastBytes[0] = lastBytes[1];
					lastBytes[1] = buffer[0];
				}

				if(lastBytes[0] == (byte)255 && lastBytes[1] ==  (byte)217){
					//		System.out.println("komplettes bild");
					completeImage = true;
				}
				baos.write(buffer);
				i = picturestream.available();
			}

			byte[] image = baos.toByteArray();

			//strip first 8 bytes (long) from Bytestream
			byte[] ts = Arrays.copyOf(image, 8);
			_senderTimeStamp = ByteBuffer.wrap(ts).getLong();

			BufferedImage picture = ImageIO.read(new ByteArrayInputStream(Arrays.copyOfRange(image, 8, image.length)));

			_receiverTimeStamp = System.currentTimeMillis();
			float diff = (float) (_receiverTimeStamp - _startTime);			
			_frameRate = 1000.f/diff; // == 1.f/(diff/1000.f)

			_startTime = _receiverTimeStamp;	

			return picture;
		} catch (IOException e) {
		}
		return null;
	}


	public void close() {
		try {
			socket_picture.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//GB
	public float getImageFrameRate() {
		return _frameRate;
	}

	//GB
	public long getSenderTimeStamp() {
		return _senderTimeStamp;
	}

	//GB
	public long getReceiverTimeStamp() {
		return _receiverTimeStamp;
	}

	//GB
	public long getDelay() {
		return _receiverTimeStamp - _senderTimeStamp;
	}
}

