package swp.tuilmenau.carduinodroid.controller;

import java.util.HashMap;
import java.util.Iterator;

import swp.tuilmenau.carduinodroid.model.LOG;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

public class UsbController {

	LOG log;
	private final Context mApplicationContext;
	private final UsbManager mUsbManager;
	private final IUsbConnectionHandler mConnectionHandler;
	private final int VID;
	private final int PID;
	protected static final String ACTION_USB_PERMISSION = "swp.tuilmenau.carduinodroid.USB";
	public boolean sent;
	Controller_Android controller_Android;

	/**
	 * Activity is needed for onResult
	 * 
	 * @param parentActivity
	 */
	public UsbController(Activity parentActivity,
			IUsbConnectionHandler connectionHandler, int vid, int pid, LOG Log,Controller_Android controller_android) {
		log = Log;
		mApplicationContext = parentActivity.getApplicationContext();
		mConnectionHandler = connectionHandler;
		mUsbManager = (UsbManager) mApplicationContext
				.getSystemService(Context.USB_SERVICE);
		VID = vid;
		PID = pid;
		controller_Android = controller_android;
		sent = true;
		
		init();
	}

	private void init() {
		enumerate(new IPermissionListener() {
			public void onPermissionDenied(UsbDevice d) {
				UsbManager usbman = (UsbManager) mApplicationContext
						.getSystemService(Context.USB_SERVICE);
				PendingIntent pi = PendingIntent.getBroadcast(
						mApplicationContext, 0, new Intent(
								ACTION_USB_PERMISSION), 0);
				mApplicationContext.registerReceiver(mPermissionReceiver,
						new IntentFilter(ACTION_USB_PERMISSION));
				usbman.requestPermission(d, pi);
			}
		});
	}

	public void stop() {
		mStop = true;
		synchronized (sSendLock) {
			sSendLock.notify();
		}
		try {
			if(mUsbThread != null)
				mUsbThread.join();
		} catch (InterruptedException e) {
			e(e);
		}
		mStop = false;
		mLoop = null;
		mUsbThread = null;
		
		try{
			mApplicationContext.unregisterReceiver(mPermissionReceiver);
		}catch(IllegalArgumentException e){};//bravo
	}

	private UsbRunnable mLoop;
	private Thread mUsbThread;

	private void startHandler(UsbDevice d) {
		if (mLoop != null) {
			mConnectionHandler.onErrorLooperRunningAlready();
			return;
		}
		mLoop = new UsbRunnable(d);
		mUsbThread = new Thread(mLoop);
		mUsbThread.start();
	}

	public void send(byte[] commando) {
		mData = commando;
		synchronized (sSendLock) {
			sSendLock.notify();
		}
	}

	private void enumerate(IPermissionListener listener) {
		l("enumerating");
		HashMap<String, UsbDevice> devlist = mUsbManager.getDeviceList();
		Iterator<UsbDevice> deviter = devlist.values().iterator();
		l(mUsbManager.getDeviceList());
		while (deviter.hasNext()) {
			l("Device-Suche!!!");
			UsbDevice d = deviter.next();
			l("Found device: "
					+ String.format("%04X:%04X", d.getVendorId(),
							d.getProductId()));
			if (d.getVendorId() == VID && d.getProductId() == PID) {
				l("Device under: " + d.getDeviceName());
				if (!mUsbManager.hasPermission(d))
					listener.onPermissionDenied(d);
				else{
					startHandler(d);
					return;
				}
				break;
			}
		}
		l("no more devices found");
		mConnectionHandler.onDeviceNotFound();
	}

	private class PermissionReceiver extends BroadcastReceiver {
		private final IPermissionListener mPermissionListener;

		public PermissionReceiver(IPermissionListener permissionListener) {
			mPermissionListener = permissionListener;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			mApplicationContext.unregisterReceiver(this);
			if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
				if (!intent.getBooleanExtra(
						UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					mPermissionListener.onPermissionDenied((UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE));
				} else {
					l("Permission granted");
					UsbDevice dev = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (dev != null) {
						if (dev.getVendorId() == VID
								&& dev.getProductId() == PID) {
							startHandler(dev);// has new thread
						}
					} else {
						e("device not present!");
					}
				}
			}
		}

	}

	// MAIN LOOP
	private static final Object[] sSendLock = new Object[]{};//learned this trick from some google example :)
	//basically an empty array is lighter than an  actual new Object()...
	private boolean mStop = false;
	private byte mData[];

	private class UsbRunnable implements Runnable {
		private final UsbDevice mDevice;
	
		UsbRunnable(UsbDevice dev) {
			mDevice = dev;
		}
	
		public void run() {//here the main USB functionality is implemented
			UsbDeviceConnection conn = mUsbManager.openDevice(mDevice);
			if (!conn.claimInterface(mDevice.getInterface(1), true)) {
				return;
			}
			// Arduino Serial usb Conv
			conn.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
			conn.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,
					0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);
			log.write(LOG.INFO, "controlTransfer");
			UsbEndpoint epIN = null;
			UsbEndpoint epOUT = null;
	
			UsbInterface usbIf = mDevice.getInterface(1);
			for (int i = 0; i < usbIf.getEndpointCount(); i++) {
				if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
					if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
						epIN = usbIf.getEndpoint(i);
					else
						epOUT = usbIf.getEndpoint(i);
				}
			}
			
			for (;;) {// this is the main loop for transferring
				
				synchronized (sSendLock) {//ok there should be a OUT queue, no guarantee that the byte is sent actually
					try {
						sSendLock.wait();
					} catch (InterruptedException e) {
						if (mStop) {
							mConnectionHandler.onUsbStopped();
							return;
						}
						e.printStackTrace();
					}
				} 
				
				if(!controller_Android.GetSent()){
					controller_Android.SetSent(true);
					conn.bulkTransfer(epOUT,mData,mData.length, 0);
				}
				
				
	
				if (mStop) {
					mConnectionHandler.onUsbStopped();
					return;
				}
			}
		}
	}

	// END MAIN LOOP
	private BroadcastReceiver mPermissionReceiver = new PermissionReceiver(
			new IPermissionListener() {
				public void onPermissionDenied(UsbDevice d) {
					l("Permission denied on " + d.getDeviceId());
				}
			});

	private static interface IPermissionListener {
		void onPermissionDenied(UsbDevice d);
	}

	public final static String TAG = "USBController";

	private void l(Object msg) {
		//method to give you the opportunity for writing in the Log
	}

	private void e(Object msg) {
		//method to give you the opportunity for writing in the Log
	}
	
}
