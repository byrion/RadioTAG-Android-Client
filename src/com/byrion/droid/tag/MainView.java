package com.byrion.droid.tag;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainView extends Activity implements View.OnClickListener {
  
	static final String TAG = "MainView";
	
	static final String RADIOTAG_BASE_PATH = "http://radiotag.prototyping.bbc.co.uk";
	static final String RADIOTAG_TAG_PATH = "/tag";
	static final String RADIOTAG_TOKEN_PATH = "/token";
	static final String RADIOTAG_REGISTRATION_PATH = "/registration_key";
	static final String RADIOTAG_REGISTER_PATH = "/register";
	
	static final int UPDATE_OUTPUT_VIEW = 0x203;
	static final int UPDATE_KEY_VIEW = 0x204;
	
	EditText mETxtStation;
	EditText mETxtKey;
	EditText mETxtPin;
	
	TextView mTxtOutput;
	
	Button mBtnTag;
	Button mBtnRegister;
	Button mBtnSubmit;
	
	String mAuthToken = null;
	String mGrantToken = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mETxtStation = (EditText) findViewById(R.id.etxt_default_station);
        mETxtKey = (EditText) findViewById(R.id.etxt_key);
        mETxtPin = (EditText) findViewById(R.id.etxt_pin);
        mTxtOutput = (TextView) findViewById(R.id.txt_output);
        
        mBtnTag = (Button) findViewById(R.id.btn_tag);
        mBtnTag.setOnClickListener(this);
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mBtnRegister.setOnClickListener(this);
        mBtnRegister.setEnabled(false);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);
        mBtnSubmit.setOnClickListener(this);
        mBtnSubmit.setEnabled(false);
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_tag:
			Log.d(TAG, "TAG");
			
			try {
				sendTag();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		break;
		case R.id.btn_register:
			Log.d(TAG, "REGISTER");
			
			try {
				register();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		break;
		case R.id.btn_submit:
			Log.d(TAG, "SUBMIT");
			
			try {
				submitRegistration();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		break;
		}
	}
	
	Handler mRadioTagHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
            switch (msg.what) {
            
            }
		}
	};
	
	Handler mViewHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
            switch (msg.what) {
            case UPDATE_OUTPUT_VIEW:
            	mTxtOutput.setText(mTxtOutput.getText() + "\n" + (String) msg.obj);
            	break;
            case UPDATE_KEY_VIEW:
            	mETxtKey.setText((String) msg.obj);
            	break;
            }
		}
	};
	
	private void sendTag() throws UnsupportedEncodingException, IOException {		
		
		mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n>>> SEND TAG (/tag)"));
		
		Map<String, String> headers = null;
		if (mAuthToken != null) {
			mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    Header: X-RadioTAG-Auth-Token:" + mAuthToken));
			headers = new HashMap<String, String>();
			headers.put("X-RadioTAG-Auth-Token", mAuthToken);
		}
		
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("station", mETxtStation.getEditableText().toString()));
		postData.add(new BasicNameValuePair("time", String.valueOf(System.currentTimeMillis()/1000)));
		
		mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    Body: " + streamToString(new UrlEncodedFormEntity(postData).getContent())));
		
		HTTPPost httpPost = new HTTPPost(mRadioTagHandler, RADIOTAG_BASE_PATH + RADIOTAG_TAG_PATH, headers, postData);
		new Thread(httpPost).start();
	}
	
	private void sendTokenRequest() throws UnsupportedEncodingException, IOException {
		
		mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n>>> Token request (/token)"));
		
		Map<String, String> headers = null;
		
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("grant_scope", "unpaired"));
		postData.add(new BasicNameValuePair("grant_token", mGrantToken));
		
		mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    body:" + streamToString(new UrlEncodedFormEntity(postData).getContent())));

		HTTPPost httpPost = new HTTPPost(mRadioTagHandler, RADIOTAG_BASE_PATH + RADIOTAG_TOKEN_PATH, headers, postData);
		new Thread(httpPost).start();
	}
	
	private void register() throws UnsupportedEncodingException, IOException {
		
		mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n>>> Registration request (/registration_key)"));
		
		Map<String, String> headers = null;
		
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("grant_scope", "can_register"));
		postData.add(new BasicNameValuePair("grant_token", mGrantToken));
		
		mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    body:" + streamToString(new UrlEncodedFormEntity(postData).getContent())));

		HTTPPost httpPost = new HTTPPost(mRadioTagHandler, RADIOTAG_BASE_PATH + RADIOTAG_REGISTRATION_PATH, headers, postData);
		new Thread(httpPost).start();
	}
	
	private void submitRegistration() throws UnsupportedEncodingException, IOException {
		
		mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n>>> PIN Submission (/register)"));
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("X-RadioTAG-Auth-Token", mAuthToken);
		
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("registration_key", mETxtKey.getEditableText().toString()));
		postData.add(new BasicNameValuePair("pin", mETxtPin.getEditableText().toString()));
		
		mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    Header: X-RadioTAG-Auth-Token:" + mAuthToken));
		mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    body:" + streamToString(new UrlEncodedFormEntity(postData).getContent())));

		HTTPPost httpPost = new HTTPPost(mRadioTagHandler, RADIOTAG_BASE_PATH + RADIOTAG_REGISTER_PATH, headers, postData);
		new Thread(httpPost).start();
	}
	
	private String streamToString(InputStream is) throws IOException {
		
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(is, "UTF-8");
		int read;
		do {
		  read = in.read(buffer, 0, buffer.length);
		  if (read>0) {
		    out.append(buffer, 0, read);
		  }
		} while (read>=0);
		return out.toString();
	}
    
	private class HTTPPost implements Runnable {
		
		Handler _returnHandler;
		String _url;
		Map<String, String> _headers;
		List<NameValuePair> _postData;
		
		public HTTPPost(Handler returnHandler, String url, Map<String, String> headers, List<NameValuePair> postData) {
			_returnHandler = returnHandler;
			_url = url;
			_headers = headers;
			_postData = postData;
		}
		
		public void run() {
			try {
				httpPostRequestSync(_url, _headers, _postData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void httpPostRequestSync(String url, Map<String, String> headers, List<NameValuePair> postData) throws UnsupportedEncodingException, IOException {
			
			HttpPost httpPost = new HttpPost(url);
			try {
				if (postData != null) {
					httpPost.setEntity(new UrlEncodedFormEntity(postData));
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());					
				}
			}
			
			try {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = httpClient.execute(httpPost);
				
				switch(response.getStatusLine().getStatusCode()) {
				case HttpStatus.SC_CREATED:
				case HttpStatus.SC_OK:
					
					Header[] grantTokenHeaders = response.getHeaders("X-Radiotag-Grant-Token");
					if (grantTokenHeaders.length > 0) {
						mGrantToken = grantTokenHeaders[0].getValue();

						mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n<<< Tag response"));
						mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    Header: X-Radiotag-Grant-Token: " + mGrantToken));
						
						mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n" + streamToString(response.getEntity().getContent())));
						
						String grantScope = response.getHeaders("X-RadioTAG-Grant-Scope")[0].getValue();
						if (grantScope.equals("can_register")) {
							mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n== CAN REGISTER ==\n"));
							mBtnRegister.setEnabled(true);
						}
						break;
					}
					
					{
					Header[] authTokenHeaders = response.getHeaders("X-RadioTAG-Auth-Token");
						if (authTokenHeaders.length > 0) {
							mAuthToken = authTokenHeaders[0].getValue();
							String accountName = response.getHeaders("X-RadioTAG-Account-Name")[0].getValue();
							
							mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n<<< Tag response"));
							mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    Header: X-RadioTAG-Auth-Token: " + mAuthToken));
							mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    Header: X-RadioTAG-Account-Name: " + accountName));
							
							mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n\nHello " + accountName + "\n"));
							
							break;
						}
					}
					
					break;
				case HttpStatus.SC_UNAUTHORIZED:
					// must request token
					Log.d(TAG, "Must request token");
					
					mGrantToken = response.getHeaders("X-Radiotag-Grant-Token")[0].getValue();
					
					mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n<<< Must Request Token"));
					mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    Header: X-Radiotag-Grant-Token: " + mGrantToken));

					sendTokenRequest();
					
					break;
				case HttpStatus.SC_NO_CONTENT:
					// auth token returned
					
					Header[] authTokenHeaders = response.getHeaders("X-RadioTAG-Auth-Token");
					if (authTokenHeaders.length > 0) {
						Log.d(TAG, "Auth token returned");
						
						mAuthToken = authTokenHeaders[0].getValue();
						
						mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n<<< Auth Token Returned"));
						mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    Header: X-RadioTAG-Auth-Token: " + mAuthToken));
						mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n\n== RESEND TAG ==\n"));
						
						Log.d(TAG, "Trying tag again...");
						sendTag();
						
						break;
					}
					
					Header[] registrationKeyHeaders = response.getHeaders("X-RadioTAG-Registration-Key");
					if (registrationKeyHeaders.length > 0) {
						Log.d(TAG, "Registration key returned");
						
						String registrationKey = registrationKeyHeaders[0].getValue();
						String registrationUrl = response.getHeaders("X-RadioTAG-Registration-Url")[0].getValue();
						
						mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_KEY_VIEW, registrationKey));
						
						mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "\n<<< Registration key returned"));
						mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    Header: X-RadioTAG-Registration-Key: " + registrationKey));
						mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, "    Header: X-RadioTAG-Registration-Url: " + registrationUrl));
						
						mBtnSubmit.setEnabled(true);
						
						break;
					}
					
					break;
				default:
					Log.d(TAG, "HTTP NOT OK: " + response.getStatusLine().getStatusCode());
					Header[] failResponseHeaders = response.getAllHeaders();
					for (Header h : failResponseHeaders) {
						Log.d(TAG, h.getName() + ": " + h.getValue());
					}
					
					mViewHandler.sendMessage(Message.obtain(mViewHandler, UPDATE_OUTPUT_VIEW, streamToString(response.getEntity().getContent())));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    
}