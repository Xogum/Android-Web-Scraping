package com.example.android_web_scraping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class WebScrapingActivity extends Activity {
	private String stockStringPattern = "(Last\\:\\<span\\>\\&nbsp\\;)(\\d+\\.\\d+)";
	private Pattern stockPattern = null;
	private EditText stockTickers = null;
	private EditText stockNames = null;
	private boolean isUpdating = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_scraping);
		
		stockTickers = (EditText)findViewById(R.id.stockTickers);
    	stockNames = (EditText)findViewById(R.id.stockNames);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_scraping, menu);
		return true;
	}
	
	public void updateStocks(View v) {
		stockTickers.setText("");
		isUpdating = true;
		// check connectivity
	    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        if (networkInfo != null && networkInfo.isConnected()) {
        	Log.d(this.getClass().toString(), "Connected to the internet.");
        	//For each line in stock textview, start a thread to scrape stock ticker
        	BufferedReader in;
        	try {
        		in = new BufferedReader(new StringReader(stockNames.getText().toString()));
            	String line;
				while ((line = in.readLine()) != null) {
		        	Log.d(this.getClass().toString(), "Checking stock for: " + line);
		        	startThread("http://quotes.esignal.com/esignalprod/quote.action?s=", line, stockTickers);	
				}
			} catch (IOException e) {
	        	Log.e(this.getClass().toString(), "Could not read from stockNames EditText.");
				e.printStackTrace();
			}
        } else {
        	Log.e(this.getClass().toString(), "Could not get network connection.");
        	stockTickers.setText(R.string.noNetworkLabel);
        }
				
		
	}

	private void startThread(final String site, final String stock, final EditText editText) {
        editText.setText(getString(R.string.updating));
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String stockPrice = scrapeStockPrice(site + stock);
				//TODO set textfield with stock price
	        	Log.d(this.getClass().toString(), "Trying to change EditText field.");
				editText.post(new Runnable() {
					public void run() {
						if (isUpdating) {
							editText.setText("");
							isUpdating = false;
						}
			        	editText.append(stockPrice + "\n");	
					}
				});
			}		
		}).start();		
	}
		
	private String scrapeStockPrice(String site) {
		BufferedReader in = null;
		if (stockPattern == null)
			stockPattern = Pattern.compile(stockStringPattern); // slow, only do once, and not on UI thread*/
		try {
			URL url = new URL(site);
			in = new BufferedReader(
			            new InputStreamReader(
			            url.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				Matcher m = stockPattern.matcher(inputLine);
				if (m.find()) {
					String s = m.group(2);
					Log.d(this.getClass().toString(), "Group 2 at " + site + " is: " + s);
					return s;
				}
			}
			Log.d(this.getClass().toString(), "Done searching");

		} catch (IOException e) {
			Log.e(this.getClass().toString(), "Unable to open url: " /* + site */);
		} catch (Exception e) {
			Log.e(this.getClass().toString(), e.toString());
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					// ignore, we tried and failed to close, limp along anyway
				}
		}
		return "Unable to find stock";
		
	}
	
  

}
