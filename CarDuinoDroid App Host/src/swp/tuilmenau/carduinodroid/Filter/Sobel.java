package swp.tuilmenau.carduinodroid.Filter;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import android.graphics.Bitmap;
import android.graphics.YuvImage;

public class Sobel extends ImageFilter {
	private IntBuffer _frameDiff;
	
	Sobel(){
		//include native code
		System.loadLibrary("CarduinoDroid");
	}
	
	private native void doNativeSobel(byte[] frame, int width, int height, IntBuffer diff);
	private native int doNativeSobelYUV(byte[] frame, int width, int height, ByteBuffer diff);
	//private native void doNativeSobel2(byte[] frame, int width, int height, byte[] diff);
	
	/*
	
	@Override
	public void filterImage(byte[] f){
		
		int width = 640;
		int height = 480;
		
		byte[] frame = new byte [width*height];
		
		System.arraycopy(f, 0, frame, 0, frame.length);
		
		_frameDiff = ByteBuffer.allocateDirect((width*height)<<2).asIntBuffer();
		this.doNativeSobel(f, width, height, _frameDiff);
		_frameDiff.position(0);
		
		Bitmap _content = Bitmap.createBitmap(width>>0, height>>0, Bitmap.Config.ARGB_8888);
		_content.copyPixelsFromBuffer(_frameDiff);
		
		//beim praktikum kommt intbuffer raus (_framediff)
		//return _frameDiff;
	}; */
	
	@Override
	public Bitmap filterImage(byte[] f){
		return null;
	}
	
	@Override
	public Bitmap filterImage(byte[] f, int width, int height){
		
		byte[] frame = new byte [width*height];          //******* unnštig
		
		System.arraycopy(f, 0, frame, 0, frame.length);  //******* unnštig
		
		//4 bytes per int
		_frameDiff = ByteBuffer.allocateDirect((width*height)<<2).asIntBuffer();
		this.doNativeSobel(f, width, height, _frameDiff);
		_frameDiff.position(0);
			
		Bitmap content = Bitmap.createBitmap(width>>0, height>>0, Bitmap.Config.ARGB_8888);

		content.copyPixelsFromBuffer(_frameDiff);
		
		return content;
	};
	
	public YuvImage filterImageYUV(byte[] f, int width, int height){
		
		//byte[] frame = new byte [width*height];          //******* unnštig
		
		//System.arraycopy(f, 0, frame, 0, frame.length);  //******* unnštig
		
		//width*height bytes for the y values; 1 byte u, 1 byte v for every 4 bytes of y. that is width*height+(width*height)/2.
		int bufferSize = (int)(width*height*1.5);
		
		ByteBuffer incredibleByteBuffer = ByteBuffer.allocateDirect(bufferSize);
		
		int counter = this.doNativeSobelYUV(f, width, height, incredibleByteBuffer);
		incredibleByteBuffer.position(0);
			
		//**** 17
		YuvImage temp = new YuvImage(incredibleByteBuffer.array(), 17, width,
				height, null);
		
		System.gc();

		return temp;
	};
		
	@Override
	protected <T> void doFilter(byte[] data, Class<T> clazz) {
		//Class blubb;
		if(Bitmap.class.isAssignableFrom(clazz)){
        //if (clazz instanceof Bitmap) {
            this.filterImage(data);
        } else if (YuvImage.class.isAssignableFrom(clazz)) {
            this.filterImageYUV(data,10,10);
        }	
		
	}
	
	
	
}
