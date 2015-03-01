package swp.tuilmenau.carduinodroid.controller;

import swp.tuilmenau.carduinodroid.model.LOG;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.*;
import android.os.Bundle;

/**
 * Provides an API for handling GPS data.
 * 
 * @author Paul Thorwirth
 * @version 1.0
 * @see LocationManager
 */
public class GPS
{
	public LOG log;
	private Controller_Android controller_Android;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private double longitude, latitude, altitude;
	private SensorManager mSensorManager;
	private float mAccel; // acceleration apart from gravity
	private float mAccelCurrent; // current acceleration including gravity
	private float mAccelLast; // last acceleration including gravity
	private float delta;
	private int batterylevel=100;

	/**
	 * Gets an Instance of the LocationManager and creates and registers a LocationListener
	 * 
	 * @param activity the current Activity
	 * @param nlog 	The Log
	 * 
	 * @see LocationManager
	 * @see LocationListener
	 */
	public GPS(Controller_Android Controller_android,Activity activity, LOG nlog) 
	{
		controller_Android = Controller_android;
		log = nlog;
		reset();
		mAccel = 0.00f;
	    mAccelCurrent = SensorManager.GRAVITY_EARTH;
	    mAccelLast = SensorManager.GRAVITY_EARTH;
		mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
	    
		locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() 
		{
			public void onLocationChanged(Location location) 
			{
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				altitude = location.getAltitude();
			}

			public void onStatusChanged(String provider, int status, Bundle extras)
			{
				int satellites;
				if ((status == LocationProvider.OUT_OF_SERVICE) | (status == LocationProvider.TEMPORARILY_UNAVAILABLE))
				{	
					reset();
				}
				satellites = (Integer) extras.get("satellites");
				if (satellites == 0) reset();
			}

			public void onProviderEnabled(String provider) 
			{
				log.write(LOG.INFO, "GPS reciever activated");
			}

			public void onProviderDisabled(String provider) 
			{
				log.write(LOG.WARNING, "GPS reciever disabled");
				reset();
			}

		};	

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
		
		SensorEventListener mSensorListener = new SensorEventListener() {
		    public void onSensorChanged(SensorEvent se) {
		      float x = se.values[0];
		      float y = se.values[1];
		      float z = se.values[2];
		      mAccelLast = mAccelCurrent;
		      mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
		      delta = mAccelCurrent - mAccelLast;
		      mAccel = mAccel * 0.9f + delta; // perform low-cut filter
		      //log.write(LOG.INFO, mAccel+"");
		    }

		    public void onAccuracyChanged(Sensor sensor, int accuracy) {
		    }
		  };
		  
		mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	/**
	 * Sets the Fields to zero.
	 */
	private void reset()
	{
		latitude = 0;
		longitude = 0;
		altitude = 0;
	}

	/**
	 * Collects the GPS-Data and prepares in to by sent by the Controller.
	 *
	 * @return {@link String} containing the GPS-Data.
	 * @see Controller_Android
	 */
	public String getGPS()
	{	
		return longitude + ";" + latitude + ";" + altitude;
	}
	
	//Its a Vibration Value created by the Accelerometer
	public String Vibration()
	{
		return (mAccel+";"+batterylevel);
	}
	
	public void SetBatteryLevel(int level)
	{
		batterylevel = level;
	}
}
