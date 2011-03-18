package soundcloud.nguyentuanviet;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.urbanstew.soundcloudapi.SoundCloudAPI;
import org.urbanstew.soundcloudapi.SoundCloudAPI.OAuthVersion;
import org.urbanstew.soundcloudapi.SoundCloudAPI.State;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class AuthenticationActivity extends Activity {
	private static String consumerKey="AIBMBzom4aIwS64tzA3uvg";
	private static String consumerSecret="FwvM54R1RaYSOatSuRJA8lvqnycVLeH4OqIDGf4zI";
	//private String callback="http://stormy-stream-176.heroku.com";
	private static final SoundCloudAPI api = new SoundCloudAPI(consumerKey, consumerSecret, SoundCloudAPI.USE_PRODUCTION.with(OAuthVersion.V2_0));
	private Context context = AuthenticationActivity.this;
	private String status="";
	private final static String FAILED_TO_AUTHENTICATE = "0";
	private final static String SUCCESS = "1";
	private final static String UNKNOWN_ERROR = "2";
	private String username ="";
	Handler handler;
	//set views
	private ImageButton connectBtn;
	private EditText usernameET;
	private EditText passwordET;
	
	private ProgressDialog dialog;
	//set sharedreferences for later use of api
	public final static String PREFERENCES = "tokenpref";
	public final static String TOKEN = "token";
	public final static String TOKENSECRET = "tokenSecret";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authentication);
		connectBtn = (ImageButton)findViewById(R.id.connectBtn);
		usernameET = (EditText)findViewById(R.id.usernameET);
		passwordET = (EditText)findViewById(R.id.passwordET);
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				dialog.dismiss();
				//inform(status);
				/*if(status.equals(SUCCESS)){
					saveAuthenticationInfos();
				    inform("Good day! You logged in as "+username);
					//Intent intent = new Intent(context,TrackActivity.class);
					//startActivity(intent);
				}
				else if(status.equals(FAILED_TO_AUTHENTICATE)){
					inform("Invalid Credentials");
				}
				else if(status.equals(UNKNOWN_ERROR)){
					inform("Unknown error or network problem");
				}*/
				if(api.getState()==State.AUTHORIZED){
					inform(username);
				}
				else{
					inform("invalid credentials");
				}
			}
		};
		connectBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				authenticate("darkoftime@gmail.com","`123qwer");
				if(api.getState()==State.AUTHORIZED){
					saveAuthenticationInfos();
					Intent myIntent = new Intent(getBaseContext(),TrackActivity.class);
					startActivity(myIntent);
				}
				else{
				}
			}
		});
	}
	String authorizationURL="";
    private void authenticate(String username,String password){ 	
    	try {
			api.obtainAccessToken(username, password);
			this.saveAuthenticationInfos();
			HttpResponse response = api.get("me");
			if(response.getStatusLine().getStatusCode() == 200)
			{
			    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			    Document dom = db.parse(response.getEntity().getContent());
			    Element rootNode = dom.getDocumentElement();
			    NodeList nodelist = rootNode.getElementsByTagName("user");
			    Element entry = (Element)nodelist.item(0);
			    Element userNode = (Element)entry.getElementsByTagName("username").item(0);
			    Log.i("XML", userNode.getFirstChild().getNodeValue());
			    status = this.SUCCESS;
			    inform(userNode.getFirstChild().getNodeValue());
			}
			else if(response.getStatusLine().getStatusCode() == 401){
				status = this.FAILED_TO_AUTHENTICATE;
				inform("failed");
			}
			else{
				status = this.UNKNOWN_ERROR;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
    }
    private void inform(String message){
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    public void saveAuthenticationInfos(){
    	SharedPreferences pref = this.getSharedPreferences(PREFERENCES, MODE_WORLD_READABLE);
    	SharedPreferences.Editor editor = pref.edit();
    	editor.putString(TOKEN, api.getToken());
    	editor.putString(TOKENSECRET, api.getTokenSecret());
    	editor.commit();
    }
    private void processAuthentication(){
    	dialog = ProgressDialog.show(context, "", 
                "Authenticating user. Please wait...", true);
		new Thread(new Runnable(){

			@Override
			public void run() {
				authenticate(usernameET.getText().toString(),passwordET.getText().toString());	
				handler.sendEmptyMessage(0);
			}
			
		}).start();
    }
}
