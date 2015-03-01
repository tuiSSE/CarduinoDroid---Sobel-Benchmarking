package swp.tuilmenau.carduinodroid.Filter;

import android.graphics.Bitmap;
import android.graphics.YuvImage;

public abstract class ImageFilter {
	protected byte[] _filteredImage;
	
//	public abstract void filterImage(byte[] data);
	
	/**
	 * @param data
	 * @return
	 */
	public abstract Bitmap filterImage(byte[] data);
	
	public abstract Bitmap filterImage(byte[] data, int width, int height);
	
	public abstract YuvImage filterImageYUV(byte[] data, int width, int height);
	
	public<T> T doBlubb(byte[] data, Class<T> clazz){
		//prolly type selection should be done here
		
		doPreProcessing(data, clazz);
		doFilter(data, clazz);
		doPostProcessing(data, clazz);
		
		return (T) data;
	};
	
	//can be overwritten by sub
	protected <T> void doPreProcessing(byte[] data, Class<T> clazz) {
		// TODO Auto-generated method stub
		
	}
	
	//must be implemented by sub
	protected abstract <T> void doFilter(byte[] data, Class<T> clazz); 
	
	//can be overwritten by sub
	protected <T> void doPostProcessing(byte[] data, Class<T> clazz) {
		// TODO Auto-generated method stub
		
	}

	public byte[] getFilteredImage(){
		return _filteredImage;
	}
}
