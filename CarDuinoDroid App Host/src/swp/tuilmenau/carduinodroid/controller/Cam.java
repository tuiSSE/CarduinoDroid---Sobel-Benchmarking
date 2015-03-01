package swp.tuilmenau.carduinodroid.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import swp.tuilmenau.carduinodroid.R;
import swp.tuilmenau.carduinodroid.Filter.FilterFactory;
import swp.tuilmenau.carduinodroid.Filter.ImageFilter;
import swp.tuilmenau.carduinodroid.model.LOG;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.LayoutInflater.Filter;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
/**
 * The Cam Class is used to set the settings, initialized the cam, the socket and the preview.
 * @author Robin
 * @see android.hardware.Camera
 */
public class Cam implements CameraCallback, Runnable
{
	Camera camera;
	Parameters parameters;

	LOG log;
	private Controller_Android controller;
	private Activity activity;
	private CameraSurface camerasurface;
	private ViewGroup cameraholder;
	private Socket client;
	private OutputStream os;
	private ServerSocket ss;
	private int quality;
	private boolean inPreviewFrame = false;
	public int height;
	public int width;
	public MyPreviewCallback previewcallback;
	public int previewFormat;
	private byte[] data;
	private boolean newFrame;
	private boolean wantToChangeRes = false;
	private int iheight;
	private int iwidth;
	private int camId;
	private int ipreviewFormat;
	private boolean onPreviewFrameInProgress;
	
	//GB
	private boolean _filterImage = false;	
	private int counter1 = 0;
	private int counter2 = 0;
	private int loopCounter = 0;
	private int dummy;
	private float[] _compressionTimeArray = new float[200];
	private long _imageShotTimeStamp = 0;
	
	/**
	 * This is the constructor of the Cam-Class. In this Method the Camera object and the Serversocket are created
	 * @param controller
	 * @param activity
	 */
	public Cam(Controller_Android controller, Activity activity)
	{	
		previewcallback = new MyPreviewCallback(this);
		onPreviewFrameInProgress = false;
		Log.e("cam", "cam erstellung gestartet");
		quality = 30;
		this.activity = activity;
		camera = Camera.open();
		camId = 0;
		cameraholder = (ViewGroup) activity.findViewById(R.id.preview);
		parameters = camera.getParameters();
		width = parameters.getPreviewSize().width;
		height = parameters.getPreviewSize().height;
		previewFormat = parameters.getPreviewFormat();
		this.controller = controller;
		parameters.setRotation(270);
		camera.setParameters(parameters);
		Log.e("cam", "cam erstellung fertig");
		camera.startPreview();
		Log.e("cam", "preview gestartet");
		setupPictureMode();
		new Thread(this).start();
		Thread t = new Thread(new Runnable(){
			public void run() {
				ss = null;
				client = null;
				Log.e("thread camera","thread camera gestartet");
				try {
					ss = new ServerSocket(12347);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					Log.e("thread camera","serversocket fehlgeschlagen");
				}
				try {
					client = ss.accept();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					Log.e("thread camera","accept fehlgeschlagen");
				}
				try {
					os = client.getOutputStream();
					Log.e("thread camera","outputstream gesetzt");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					Log.e("thread camera","output bekommen fehlgeschlagen");
				}

				if(client != null)
					Log.e("thread camera","javaprog gefunden" + client.getInetAddress().toString());
			}
		});
		t.start();
	}
	/**
	 * This Method enables the flashlight of the camera
	 */
	public void enableFlash()
	{
		parameters = camera.getParameters();
		parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		camera.setParameters(parameters);
	}
	/**
	 * This Method disables the flashlight of the camera
	 */
	public void disableFlash()
	{
		parameters = camera.getParameters();
		parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		camera.setParameters(parameters);
	}
	/**
	 * This Method release the current Camera and starts the camera with the ID
	 * 
	 * @param id of the camera to access
	 * 
	 * @see android.hardware.Camera#open(int)
	 */
	public void switchCam(int cameraId) {
		camId = cameraId;
	    if (camera != null) {
	        stopCamera();
	    }
	    
	    camera = Camera.open(cameraId);
        try {
            camera.setPreviewDisplay(camerasurface.holder);
            camera.setPreviewCallback(previewcallback);
        } catch (IOException e) { e.printStackTrace(); }
	    camerasurface.setCamera(camera);
		camerasurface.setCallback(this);
	    parameters = camera.getParameters();
	    width = parameters.getPreviewSize().width;
	    height = parameters.getPreviewSize().height;
	    previewFormat = parameters.getPreviewFormat();
	    controller.network.socket_package.newPreviewSizes();
	    camera.startPreview();
	}
	
	/**
	 * Change the Resolution of the preview pictures
	 * @param width the width of the pictures, in pixels
	 */
	public void changeRes(int index)
	{
		while(onPreviewFrameInProgress)
		{
			Log.e("bla", "bla");
		}
		Log.e("cam", "preview stop changeres");
		parameters = camera.getParameters();
		List<Size> temp = parameters.getSupportedPreviewSizes();
		//camera.stopPreview();
		if(index < temp.size() && index >= 0){
			if (camera != null) {
		        stopCamera();
		    }
			camera = Camera.open(camId);
			parameters = camera.getParameters();
	        try {
	            camera.setPreviewDisplay(camerasurface.holder);
	            camera.setPreviewCallback(previewcallback);
	        } catch (IOException e) { e.printStackTrace(); }
		    camerasurface.setCamera(camera);
			camerasurface.setCallback(this);
		    parameters = camera.getParameters();
		    width = temp.get(temp.size()-1-index).width;
		    height = temp.get(temp.size()-1-index).height;
		    previewFormat = parameters.getPreviewFormat();
		    //controller.network.socket_package.newPreviewSizes();
		    parameters.setPreviewSize(width, height);
			camera.setParameters(parameters);
		    camera.startPreview();
			
			/*
			//camera.stopPreview();
			controller.log.write(Log.INFO, "2.Teil");
			int newwidth = temp.get(temp.size()-1-index).width;
			int newheight = temp.get(temp.size()-1-index).height;
			width = newwidth;
			height = newheight;
			controller.log.write(Log.INFO, "3.Teil - Breite: "+width+" Hoehe: "+height);
			parameters = camera.getParameters();
			controller.log.write(Log.INFO, "4.Teil");
			//parameters.set
			//parameters.setPictureSize(newwidth, newheight);
			parameters.setPreviewSize(newwidth, newheight);
			controller.log.write(Log.INFO, "5.Teil");
			camera.setParameters(parameters);
			controller.log.write(Log.INFO, "6.Teil");
			//camera.startPreview();
			Log.e("cam", "preview restarted changeres");
			controller.log.write(Log.INFO, "7.Teil");*/
		}
	}
	/**
	 * Set the preview frame rate
	 * @param fps the frame rate
	 */
	public void changeFPS(int fps)
	{
		List<Integer> temp = parameters.getSupportedPreviewFrameRates();
		if(temp.contains(Integer.valueOf(fps))){
			parameters.setPreviewFrameRate(fps);
			camera.setParameters(parameters);
		}
		else
		{
			controller.log.write(LOG.WARNING, fps + " fps not supported");
		}
	}
	/**
	 * Releases the camera
	 */
	public void disableCamera()
	{
		disableFlash();
		camera.stopPreview();
		camera.release();
	}



	/**
	 * Sets the Surface and the Callback
	 */
	private void setupPictureMode(){
		if(camerasurface  == null)
		camerasurface = new CameraSurface(activity, camera, this);
		else
			camerasurface.setCamera(camera);
		cameraholder.removeAllViews();
		cameraholder.addView(camerasurface, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		camerasurface.setCallback(this);
	}
	/**
	 * not used
	 */
	public void onJpegPictureTaken(byte[] data, Camera camera) {
	}

	/**
	 *   Called as preview frames are displayed. Compress the data too a jpeg-file and send it to the java-program
	 */
	public void onPreviewFrame(byte[] data, Camera camera) {
		if(os != null && !client.isClosed() && inPreviewFrame == false && !wantToChangeRes){
			onPreviewFrameInProgress = true;

			iwidth = width;
			iheight = height;
			ipreviewFormat = previewFormat;
			this.data = data.clone();
			newFrame = true;
			onPreviewFrameInProgress = false;
			
			_imageShotTimeStamp = System.currentTimeMillis(); //GB
			}
		}
	
		
		
		public String getSupportedSize(){
			List<Size> supsize = parameters.getSupportedPreviewSizes();
			String result = supsize.get(0).width+"x"+supsize.get(0).height;
			for(int i = 1; i< supsize.size(); i++)
			{
				result = result + ";" +supsize.get(i).width+"x"+supsize.get(i).height;
			}
			return result;
			}
		
		public String[] getSupportedFPS(){
			List<Integer> supfps = parameters.getSupportedPreviewFrameRates();
			String[] result = new String[supfps.size()];
			for(int i = 0; i< supfps.size(); i++)
			{
				result[i] = supfps.get(i).intValue() + "";
			}
			return result;
			}

	/**
	 *   	not used      
	 */
	public void onRawPictureTaken(byte[] data, Camera camera) {
	}

	/**
	 *    	not used    
	 */
	public void onShutter() {
	}
	/**
	 *    	not used
	 */
	public String onGetVideoFilename() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setQuality(int quality){
		if(quality > 100){
			this.quality = 100;
			controller.log.write(LOG.WARNING, "Quality changed to 100");
		}
		else{
			this.quality = quality;
			controller.log.write(LOG.WARNING, "Quality changed to " + quality);
			}
	}
	
	public void setFilterImage(boolean filterImage){  //GB
		_filterImage = filterImage;
	}
	
	public void close() {
		try {
		if(client != null){
				os = null;
				client.close();
				}
		Thread t = new Thread(new Runnable(){
			public void run() {
				client = null;
					try {
						if(ss != null)
						{
							Log.e("cam","serversocket da");
						}
						client = ss.accept();
						os = client.getOutputStream();
					} catch (IOException e) {
						// TODO Auto-generated catch block
							e.printStackTrace();
					}
			}
		});
		t.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	

	private void stopCamera(){
		if (camera != null){
	        camera.stopPreview();
	        camera.release();
	        camera = null;
	        camerasurface.holder.removeCallback(camerasurface);
	        camerasurface.setCallback(null);
	    }
	}
	public void run() {
		iwidth = width;
		iheight = height;
		ipreviewFormat = previewFormat;
		FilterFactory filterFactory = FilterFactory.getFactory(); //GB
		Set<String> blubb = filterFactory.getAvailableImageFilters();
		ImageFilter sobel = filterFactory.getInstance("sobel"); //GB
		int counter = 0; //GB
		while(true)
		{
			if(newFrame && !wantToChangeRes){
				newFrame = false;
				inPreviewFrame = true;
				Log.e("bla", newFrame + " " + inPreviewFrame);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				//GB
				byte[] tsBytes = ByteBuffer.allocate(8).putLong(_imageShotTimeStamp).array(); //attach timeStamp at the front of the buffer
				try {
					baos.write(tsBytes);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
							
				//performanceTest(sobel);
				
				if(_filterImage){
					//ggf Bitmap aus den includes entfernen
					//Bitmap blubb = sobel.filterImage(data,iwidth,iheight);
					
					YuvImage temp = sobel.filterImageYUV(data,iwidth,iheight);
					
					Rect rect = new Rect(0, 0, iwidth, iheight);
					//
					
					long compressStartTime = System.nanoTime();
					//blubb.compress(Bitmap.CompressFormat.JPEG, quality, baos);
					temp.compressToJpeg(rect, quality, baos);
					long compressEndTime = System.nanoTime();
					float diff = ((float) (compressEndTime - compressStartTime))/1000000.f;										
					Log.e("Compression Time",String.valueOf(diff));
					_compressionTimeArray[counter] = diff;
				}else{
					YuvImage temp = new YuvImage(data, ipreviewFormat, iwidth,
							iheight, null);
					Rect rect = new Rect(0, 0, iwidth, iheight);
					//temp.compressToJpeg(rect, quality, baos);
					
					long compressStartTime = System.nanoTime();
					temp.compressToJpeg(rect, quality, baos);
					long compressEndTime = System.nanoTime();
					float diff = ((float) (compressEndTime - compressStartTime))/1000000.f;										
					Log.e("Compression Time",String.valueOf(diff));
					_compressionTimeArray[counter] = diff;
				}
								
				try {					
					//os.write(image); //******
					os.write(baos.toByteArray()); //*****
					baos.close();  //*****
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("cam", "fehler beim schreiben des previewimage");
				}
				
				if(counter == 199){
					float avg = 0;
					for(int i = 0; i<counter; ++i){
						avg += _compressionTimeArray[counter];
					}
					avg/=counter;
					Log.e("AVG", String.valueOf(avg));
					counter = -1;
				}
				
				counter++;
				
				inPreviewFrame = false;
				Log.e("bla", newFrame + " " + inPreviewFrame);
	}}
	}
	
	//GB
	private void performanceTest(ImageFilter filter) {
		// TODO Auto-generated method stub				
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		
		byte[] data2 = data.clone();
		
		//Bitmap blubb = filter.filterImage(data,iwidth,iheight);
		YuvImage blubb = filter.filterImageYUV(data,iwidth,iheight);
		
		Rect rect = new Rect(0, 0, iwidth, iheight);
		//blubb.compress(Bitmap.CompressFormat.PNG, quality, baos);
		blubb.compressToJpeg(rect, quality, baos);
		
		byte[] image = baos.toByteArray();				
		counter1 += image.length;
		try {
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		YuvImage temp = new YuvImage(data2, ipreviewFormat, iwidth,
				iheight, null);
		Rect rect2 = new Rect(0, 0, iwidth, iheight);
		temp.compressToJpeg(rect2, quality, baos2);
		byte[] image2 = baos2.toByteArray();
		counter2 += image2.length;
		try {
			baos2.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		loopCounter++;
		
		if(loopCounter == 100){
			//counter1 == sobel
			//counter2 == native
			
			int diff = counter1 - counter2;
			float avg1 = counter1/loopCounter;
			float avg2 = counter2/loopCounter;
			dummy = diff + (int) avg1 + (int) avg2;
		}
		//YuvImage bla = filter.doBlubb(10, YuvImage.class);
		//rotate
	}
}