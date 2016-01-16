package com.example.ttranslate;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {

    private NumberPicker langFrom, langTo;
    private String[] languages = new String[] {"English", "Spanish"};    
    private int currFrom = 0, currTo = 1;
    private EditText input;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


	setupPickers();

	input = (EditText)findViewById(R.id.transInput);
	input.setOnEditorActionListener(new OnEditorActionListener() {

	    @Override
	    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
		    doTranslation();
		    return true;
		}
		return false;
	    }
	});

	((RelativeLayout)findViewById(R.id.background)).setOnTouchListener(new OnTouchListener() {

	    @Override 
	    public boolean onTouch(View v, MotionEvent event) {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
		return false;
	    }
	});
    }

    private void setupPickers() {
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

    }

    private void doTranslation() {

	
	
	
    }
}
