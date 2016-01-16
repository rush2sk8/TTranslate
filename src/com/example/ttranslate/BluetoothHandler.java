package com.example.ttranslate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Rushad Antia
 * 
 * This class reduces the baggage of setting up a bluetooth 
 * connection.
 * */
public class BluetoothHandler extends Activity {

	//used for debugging
	private static final String TAG = "bluetooth2";

	//id necessary for making a bluetooth connection
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private StringBuilder sb;

	//an android tool that can properly recieve messages
	private Handler h;

	public static final int SENSOR_DATA_ERROR = -1;

	//bluetooth adapter
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;

	private ConnectedThread mConnectedThread;
	private final int RECIEVE_MESSAGE =1;

	private BluetoothHandler context;

	protected static String recievedMessage = "-1";

	//MAC address to connect to 
	private static String address;

	/**
	 * called by the android OS that will safely dispose of the activity
	 * */
	public void dispose() {

		//killz the adapter
		btAdapter = null;
		try {

			//closes the socket
			btSocket.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

		//disposes the closed socket
		btSocket = null;

		try {

			//joins the other thread to the main thread
			mConnectedThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//disposes the thread
		mConnectedThread = null;
	}

	/**
	 * Sends data to the end device
	 * */
	public synchronized void write(String data) {

		if(mConnectedThread.isAlive()) //error checking
			mConnectedThread.write(data);

	}

	/**
	 * Self Explanatory
	 * @returns true if socket is available 
	 * */
	public boolean isSocketAvailable() {
		return btSocket.isConnected();
	}

	/**
	 * Creates an instance oF the bluetooth handler
	 * 
	 * */
	public BluetoothHandler(Context c, String ad) {

		//makes the ivs = to the parameters
		address = ad;
		btAdapter = BluetoothAdapter.getDefaultAdapter();	
		// get Bluetooth adapter

		sb = new StringBuilder();

		//checks bluetooth radio
		checkBTState();

		//creates handler to recieve the data
		createHandler();

		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		try {

			//creates a bluetooth socket (connection) between this device and the other device
			btSocket = createBluetoothSocket(device);

		} catch (IOException e) {
			errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
		}


		// Discovery is resource intensive.  Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection.  This will block until it connects.
		Log.d(TAG, "...Connecting...");

		try {

			//waits here until it connects
			btSocket.connect();
			Log.d(TAG, "....Connection ok...");

		} catch (IOException e) {

			try {

				//if the exception is thrown then safely dispose of the socket if not done can cause memory leak
				btSocket.close();

			} catch (IOException e2) {
				errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		Log.d(TAG, "...Create Socket...");

		//creates a new thread that will handle all of the data transmission
		mConnectedThread = new ConnectedThread(btSocket);

		//starts the thread
		mConnectedThread.start();
		context = this;
	
	}

	/**
	 * Creates an android handler that will send recieved messages to a target
	 * 
	 * */
	private void createHandler(){
		h = new Handler() {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case RECIEVE_MESSAGE:													// if receive massage

					byte[] readBuf = (byte[]) msg.obj;
					String strIncom = new String(readBuf, 0, msg.arg1);					// create string from bytes array
					sb.append(strIncom);												// append string

					int endOfLineIndex = sb.indexOf("\r\n");							// determine the end-of-line
					if (endOfLineIndex > 0) { 											// if end-of-line,

						String sbprint = sb.substring(0, endOfLineIndex);				// extract string
						sb.delete(0, sb.length());										// and clear

						System.out.println("Data: "+ sbprint);

						recievedMessage = sbprint;

					}
					//Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
					break;
				}
			};
		};
	}

	/**
	 * Called by the android system when user swtiches into a multitaking mode.
	 * 
	 * During the system pause we close the socket
	 * 
	 * */
	public void onPause() {
		super.onPause();

		Log.d(TAG, "...In onPause()...");

		try  {

			//closes a socket
			btSocket.close();


		} catch (IOException e2) {
			errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
		}
	}

	/**
	 * Checks the state of the bluetooth radio
	 * */
	private void checkBTState() {

		if(btAdapter==null) { 
			errorExit("Fatal Error", "Bluetooth not support");
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG, "...Bluetooth ON...");
			} else {

				//callls native os intent (activity) that will ask user to turn on the bluetooth radio
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

				//calls a background activity that will ask the user to turn on the bluetooth radio
				startActivityForResult(enableBtIntent, 1);
			}
		}
	}

	//makes a toast error message and closes app
	private void errorExit(String title, String message){
		Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
		finish();
	}

	/**
	 * Creates a connection between 2 devices
	 * */
	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
		if(Build.VERSION.SDK_INT >= 10){
			try {

				//creates a native method to create an unencrypted socket 
				final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });

				//wiill return the socket
				return (BluetoothSocket) m.invoke(device, MY_UUID);

			} catch (Exception e) {
				Log.e(TAG, "Could not create Insecure RFComm Connection",e);
			}
		}

		return  device.createRfcommSocketToServiceRecord(MY_UUID);
	}

	/**
	 * @author Rushad Antia
	 * 
	 * an inner class that will control low level in and output
	 * */
	private class ConnectedThread extends Thread {

		//2 ivs that hold elements for every connection in a socket
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		/**
		 * Creates an instance of the class and will break apart the socket into 
		 * and out in input stream
		 * */
		public ConnectedThread(BluetoothSocket socket) {

			//the reason we are using temporary steams is because if there is some exception throew whcih is not likely then we dont want the
			//iv's directly referenceing somehting that is broken.
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {

				//gets the streams
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();


			} catch (IOException e) { }

			//makes iv's equal to the streams
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		/**
		 * Method that is inherited from the thread class
		 * basically whatever code is in here will be excecuted in a background thread
		 * */
		public void run() {
			byte[] buffer = new byte[256];  // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);	

					//sends data from other device to the handler
					h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();		
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		/**
		 * Will write the string to the other device
		 * 
		 * */
		public void write(String message) {
			//Log.d(TAG, "...Data to send: " + message + "...");
			byte[] msgBuffer = message.getBytes();

			try {

				if(mmOutStream!=null)
					mmOutStream.write(msgBuffer);

			} catch (IOException e) {
				Log.d(TAG, "...Error data send: " + e.getMessage() + "...");     
				Toast.makeText(getApplicationContext(), "Caught an exception while sending data", Toast.LENGTH_LONG).show();

			}
		}


	}

	//disables the background press
	public void onBackPressed() {
		//do nothing
	}

}
