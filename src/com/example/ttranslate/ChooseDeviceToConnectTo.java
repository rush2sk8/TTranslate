package com.example.ttranslate;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 *
 * A class that is called initially that allows the user to select 
 * a device to connect to based on devices that are paired to that android device
 * 
 * */
public class ChooseDeviceToConnectTo extends Activity {

	//disables the back button
	public void onBackPressed() {}

	//does stuff
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_device_to_connect_to);



		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (mBluetoothAdapter.isEnabled()) {

			doBTStuff();

		} else {

			//callls native os intent (activity) that will ask user to turn on the bluetooth radio
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

			//calls a background activity that will ask the user to turn on the bluetooth radio
			startActivityForResult(enableBtIntent, 1);
		}


	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 
		super.onActivityResult(requestCode, resultCode, data);
		doBTStuff();
	}
	
	public void doBTStuff() {

		ListView list = (ListView)findViewById(R.id.list);
		ArrayList<String> s = new ArrayList<String>();
		final ArrayList<String> ads = new ArrayList<String>();

		for(BluetoothDevice d:BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
			ads.add(d.getAddress());
			s.add(d.getName() + "\n" + d.getAddress());
		}

		list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,s));
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				MainActivity.btHandler = new BluetoothHandler(view.getContext(), ads.get((int) id));
				finish();
			}
		});
	}
}
