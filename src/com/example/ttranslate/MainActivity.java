package com.example.ttranslate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;


public class MainActivity extends Activity {

    protected static BluetoothHandler btHandler;
    private NumberPicker langFrom, langTo;
    private TextView trans;
    private Button voice;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    private String[] languages = new String[] {"English", "Spanish","German","Italian","Latin"};  
    private String[] languageCodes = new String[] {"en","es","de","it","ja","la"};

    private int currFrom = 0, currTo = 1;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	startActivity(new Intent(getApplicationContext(), ChooseDeviceToConnectTo.class));
	//XXX ^^ do when we add bt


	init();

    }

    private void init() {

	input = (EditText)findViewById(R.id.transInput);
	input.setOnEditorActionListener(new OnEditorActionListener() {

	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
		    doTranslation(input.getText().toString());
		    return true;
		}
		return false;
	    }
	});

	trans = (TextView)findViewById(R.id.translated);

	((RelativeLayout)findViewById(R.id.background)).setOnTouchListener(new OnTouchListener() {

	    @Override 
	    public boolean onTouch(View v, MotionEvent event) {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
		return false;
	    }
	});

	langFrom = (NumberPicker)findViewById(R.id.langFrom);
	langFrom.setMinValue(0);
	langFrom.setValue(currFrom);
	langFrom.setMaxValue(languages.length-1);
	langFrom.setDisplayedValues(languages);
	langFrom.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
	langFrom.setFormatter(new NumberPicker.Formatter() {

	    @Override
	    public String format(int value) {
		return languages[value];
	    }
	});
	langFrom.setOnValueChangedListener(new OnValueChangeListener() {

	    @Override
	    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

		currFrom = newVal;

	    }
	});


	langTo = (NumberPicker)findViewById(R.id.langTo);
	langTo.setMinValue(0);
	langTo.setValue(currTo);
	langTo.setMaxValue(languages.length-1);
	langTo.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
	langTo.setFormatter(new NumberPicker.Formatter() {

	    @Override
	    public String format(int value) {
		return languages[value];
	    }
	});

	langTo.setDisplayedValues(languages);
	langTo.setOnValueChangedListener(new OnValueChangeListener() {

	    @Override
	    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

		currTo = newVal;

	    }
	});

	voice = (Button)findViewById(R.id.voice);
	voice.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {

		getSpeechInput();


	    }
	});

    }

    private void getSpeechInput() {
	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
	intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say something in "+ languages[currFrom]);
	try {
	    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
	} catch (ActivityNotFoundException a) {
	    a.printStackTrace();
	}

    }

    private void doTranslation(String text) {

	String url = "http://api.mymemory.translated.net/get?q="+text.replaceAll(" ", "%20")+"&langpair="+languageCodes[currFrom]+"|"+languageCodes[currTo];
	System.out.println(url);
	new TranslateTask(url).execute(new Void[] {});
    }

    class TranslateTask extends AsyncTask<Void, Void, Void>{

	private String url;
	private String data;

	public TranslateTask(String url) {
	    this.url = url;
	}

	protected Void doInBackground(Void... params) {

	    try {
		URL url = new URL(this.url);

		URLConnection connection = url.openConnection();
		if(connection == null) {
		    Toast.makeText(getApplicationContext(), "Yo get on the web!", Toast.LENGTH_SHORT).show();
		    return null;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		data = "";

		StringBuilder sBuilder = new StringBuilder();

		String line = null;

		while ((line = reader.readLine()) != null) 
		    sBuilder.append(line + "\n");

		reader.close();
		data = sBuilder.toString();
		System.out.println(data);

		data = data.substring(data.indexOf("Text\":\"")+7,data.indexOf("\",\"match"));
		runOnUiThread(new Runnable() {

		    @Override
		    public void run() {

		/**	String converted = "";
			if(data.equals("\u00ed"))
			    data =  data.replaceAll("\u00ed", Html.fromHtml("\u00ED").toString());

			if(data.equals("\u00cf"))
			    data =    data.replaceAll("\u00c4", Html.fromHtml("\u00C4").toString());

			if(data.equals("\u00e1"))
			    data =    data.replaceAll("\u00e1", Html.fromHtml("\u00E1").toString());

			if(data.equals("\u00c9"))
			    data =    data.replaceAll("\u00c9", Html.fromHtml("\u00C9").toString());

			if(data.equals("\u00e9"))
			    data =    data.replaceAll("\u00e9", Html.fromHtml("\u00E9").toString());

			if(data.equals("\u00d1"))
			    data =    data.replaceAll("\u00d1", Html.fromHtml("\u00D1").toString());

			if(data.equals("\u00f1"))
			    data =    data.replaceAll("\u00f1", Html.fromHtml("\u00F1").toString());

			if(data.equals("\u00d3"))
			    data =    data.replaceAll("\u00d3", Html.fromHtml("\u00D3").toString());

			if(data.equals("\u00f3"))
			    data =    data.replaceAll("\u00f3", Html.fromHtml("\u00F3").toString());

			if(data.equals("\u00da"))
			    data =    data.replaceAll("\u00da", Html.fromHtml("\u00DA").toString());

			if(data.equals("\u00fa"))
			    data =    data.replaceAll("\u00fa", Html.fromHtml("\u00FA").toString());

			if(data.equals("\u00bf"))
			    data =    data.replaceAll("\u00bf", Html.fromHtml("\u00BF").toString());

			if(data.equals("\u00a1"))
			    data =    data.replaceAll("\u00a1", Html.fromHtml("\u00A1").toString());
*/

			if(currFrom!=currTo) {
			    trans.setText(data);
			    btHandler.write(data);
			}
			else
			    Toast.makeText(getBaseContext(), "Please Choose 2 distinct languages", Toast.LENGTH_SHORT).show();
		    }
		});

	    } catch (IOException e) {
		e.printStackTrace();
	    }


	    return null;
	}


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	super.onActivityResult(requestCode, resultCode, data);

	switch (requestCode) {
	case REQ_CODE_SPEECH_INPUT: {
	    if (resultCode == RESULT_OK && null != data) {

		ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		String bestMatch = result.get(0);
		System.out.println(bestMatch);

		doTranslation(bestMatch);

	    }
	    break;
	}

	}

    }
}