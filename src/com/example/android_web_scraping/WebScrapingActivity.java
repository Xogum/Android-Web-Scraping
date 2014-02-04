package com.example.android_web_scraping;

import java.util.regex.Pattern;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class WebScrapingActivity extends Activity {
	//private String stockPattern2 = "Last\:<span>\&nbsp\;(\d+\.\d+)";
	private String stockPattern = "(Last\\:\\<span\\>\\&nbsp\\;)(\\d+\\.\\d+)";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_scraping);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_scraping, menu);
		return true;
	}
	
	public void updateStocks(View v) {
		
		Pattern p = Pattern.compile(stockPattern);
		
	}

}
