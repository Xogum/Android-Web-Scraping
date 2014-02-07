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
	private String stockStringPattern = "(Last\\:\\<span\\>\\&nbsp\\;)(\\d{1,3}(\\,\\d{1,3})*\\.\\d{2})";
	private Pattern stockPattern = null;
	private EditText stockTickers = null;
	private EditText stockNames = null;
	private String[] stockPrices;

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
	
	/*
	 * after update button is pressed, network connectivity is confirmed and threads are started
	 * to scrape stock tickers for stock names.
	 */
	public void updateStocks(View v) {
		// check connectivity
	    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        if (networkInfo != null && networkInfo.isConnected()) {
        	Log.d(this.getClass().toString(), "Connected to the internet.");
        	//For each line in stock textview, start a thread to scrape stock ticker
        	String stocks = stockNames.getText().toString();
        	//Count how many lines to create array to ensure thread order
        	int lines = stocks.split(System.getProperty("line.separator")).length;
        	Log.d(this.getClass().toString(), "There are " + lines + " stocks.");
        	stockPrices = new String[lines];
        	BufferedReader in;
        	try {
        		in = new BufferedReader(new StringReader(stocks));
            	String line;
            	int arrayIndex = 0;
				while ((line = in.readLine()) != null) {
					if (line.equals("")) {
						continue;
					}
		        	Log.d(this.getClass().toString(), "Checking stock for: " + line);
		        	startThread("http://quotes.esignal.com/esignalprod/quote.action?s=", line, arrayIndex, stockTickers);
		        	arrayIndex++;
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
	
	/*
	 * starts a new thread that requests a site to be scraped for a stock ticker and updates EditText view
	 * with this stock ticker
	 * @param site a url to a stock ticker site. For example, http://quotes.esignal.com/esignalprod/quote.action?s=
	 * @param stock a stock ticker that will be appended to the site. For example, "AAPL"
	 * @param editText an EditText view that will display the stock price
	 */
	private void startThread(final String site, final String stock, final int arrayIndex, final EditText editText) {
		stockPrices[arrayIndex] = getString(R.string.updating);
		updateEditText(editText);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String stockPrice = scrapeStockPrice(site + stock);
				editText.post(new Runnable() {
					public void run() {
						stockPrices[arrayIndex] = stockPrice;
						updateEditText(editText);
					}
				});
			}		
		}).start();		
	}
	
	private void updateEditText(final EditText editText) {
		editText.setText("");
		for (int i = 0; i < stockPrices.length; i++) {
			editText.append(stockPrices[i] + "\n");
			editText.invalidate();
		}	
	}
	
	/*
	 * scrapes a stock price from the given site url.
	 * @param site a url to a specific ticker on a stock web site. For example, http://quotes.esignal.com/esignalprod/quote.action?s=AAPL
	 * @return the stock price as a string. For example, "334.23"
	 */
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
