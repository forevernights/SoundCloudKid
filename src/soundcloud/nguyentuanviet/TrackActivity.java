package soundcloud.nguyentuanviet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soundcloud.nguyentuanviet.adapter.MyArrayAdapter;
import soundcloud.nguyentuanviet.entities.Track;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class TrackActivity extends ListActivity {
	ArrayList<Track> tracklist = new ArrayList<Track>();
	ListView lv;
	MyArrayAdapter myAdapter;
	SoundCloudAPI api;
	Handler handler;
	//static values
	public final static String TRACK_ID = "";
	public final static String STREAM_URL = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.track);
		
		//obtain saved authentication
		//this.obtainSavedAuthentication();
		
		api = new SoundCloudAPI(getResources().getString(R.string.CONSUMER_KEY), getResources().getString(R.string.CONSUMER_SECRET), SoundCloudAPI.USE_PRODUCTION.with(OAuthVersion.V2_0));
		try {
			api.obtainAccessToken("darkoftime@gmail.com","`123qwer");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		//set listview
		loadTrackInfo();
		lv = getListView();
		myAdapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_1, tracklist);
		lv.setAdapter(myAdapter);
        registerForContextMenu(lv);
        lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView arg0, View view,
                                           int position, long id) {
				myAdapter.setSelectedPosition(position);
				if(tracklist.get(position).getStreamURL()==""){
					inform("Sorry this sound is unstreamable.");
				}
				else{
					Intent myIntent = new Intent(getBaseContext(),MediaPlayerActivity.class);
					myIntent.putExtra(TRACK_ID,tracklist.get(position).getTrack_id());
					myIntent.putExtra(STREAM_URL, tracklist.get(position).getStreamURL()+"?consumer_key="+getResources().getString(R.string.CONSUMER_KEY));
					startActivity(myIntent);
				}
			}
        });
     	
        handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				myAdapter.notifyDataSetChanged();
			}
        	
        };
	}
	Drawable artwork;
	private Drawable getArtwork(String link){
		try {
			URL url = new URL(link);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setDoOutput(true);
			connection.connect();
			InputStream is = connection.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			artwork = new BitmapDrawable(bitmap);
			return artwork;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private Bitmap getDefaultAvatar(Context context) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		return BitmapFactory.decodeStream(context.getResources()
				.openRawResource(R.drawable.icon), null, opts);
	}
	private class MyHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			myAdapter.notifyDataSetChanged();
		}
		
	}
	NodeList nodelist;
	private void loadTrackInfo(){
		try {
			HttpResponse response = api.get("tracks");
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    Document dom = db.parse(response.getEntity().getContent());
		    Element rootNode = dom.getDocumentElement();
		    nodelist = rootNode.getElementsByTagName("track");
		    if(nodelist!=null&&nodelist.getLength()>0){
				for(int i=0;i<10;i++){
					/*final int num = i;
					new Thread(new Runnable(){
						@Override
						public void run() {
							tracklist.add(getSingleTrack(num));
							handler.sendEmptyMessage(0);
						}
					}).start();*/
					Element entry = (Element)nodelist.item(i);
					Element trackIDNode = (Element)entry.getElementsByTagName("id").item(0);
					Element userIDNode = (Element)entry.getElementsByTagName("user-id").item(0);
					Element titleNode = (Element)entry.getElementsByTagName("title").item(0);
					//Element artworkNode = (Element)entry.getElementsByTagName("artwork-url").item(0);
					
					//username and avatar
					Element userNode = (Element)entry.getElementsByTagName("user").item(0);
					Element usernameNode = (Element)userNode.getElementsByTagName("username").item(0);
					Element avatarNode = (Element)userNode.getElementsByTagName("avatar-url").item(0);	
					
					//new instance of track
					int trackId = Integer.parseInt(trackIDNode.getFirstChild().getNodeValue());
					int userId = Integer.parseInt(userIDNode.getFirstChild().getNodeValue());
					String title = titleNode.getFirstChild().getNodeValue();
					String username = usernameNode.getFirstChild().getNodeValue();
					//String artwork = artworkNode.getFirstChild().getNodeValue();
					//Drawable defaultAvatar = new BitmapDrawable(getDefaultAvatar(this));
					
					//xml element need to be validated before fetching to prevent crashes
					String streamURL="";
					if((Element)entry.getElementsByTagName("stream-url").item(0)!=null){
						Element streamURLNode = (Element)entry.getElementsByTagName("stream-url").item(0);
						streamURL = streamURLNode.getFirstChild().getNodeValue();
					}
					Track newTrack = new Track();
					newTrack.setArtwork(this.getArtwork(avatarNode.getFirstChild().getNodeValue()));
					newTrack.setStreamURL(streamURL);
					newTrack.setTitle(title);
					newTrack.setTrack_id(trackId);
					newTrack.setUser_id(userId);
					newTrack.setUsername(username);
					tracklist.add(newTrack);
				}
		    }
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
	//get single track for lazy thread
	private Track getSingleTrack(int num){
		Element entry = (Element)nodelist.item(num);
		Element trackIDNode = (Element)entry.getElementsByTagName("id").item(0);
		Element userIDNode = (Element)entry.getElementsByTagName("user-id").item(0);
		Element titleNode = (Element)entry.getElementsByTagName("title").item(0);
		Element streamURLNode = (Element)entry.getElementsByTagName("stream_url").item(0);
		//Element artworkNode = (Element)entry.getElementsByTagName("artwork-url").item(0);
		
		//username and avatar
		Element userNode = (Element)entry.getElementsByTagName("user").item(0);
		Element usernameNode = (Element)userNode.getElementsByTagName("username").item(0);
		Element avatarNode = (Element)userNode.getElementsByTagName("avatar-url").item(0);	
		
		//new instance of track
		int trackId = Integer.parseInt(trackIDNode.getFirstChild().getNodeValue());
		int userId = Integer.parseInt(userIDNode.getFirstChild().getNodeValue());
		String title = titleNode.getFirstChild().getNodeValue();
		String username = usernameNode.getFirstChild().getNodeValue();
		String streamURL = streamURLNode.getFirstChild().getNodeValue();
		//String artwork = artworkNode.getFirstChild().getNodeValue();
		//Drawable defaultAvatar = new BitmapDrawable(getDefaultAvatar(this));
		Track newTrack = new Track();
		newTrack.setArtwork(this.getArtwork(avatarNode.getFirstChild().getNodeValue()));
		newTrack.setStreamURL(streamURL);
		newTrack.setTitle(title);
		newTrack.setTrack_id(trackId);
		newTrack.setUser_id(userId);
		newTrack.setUsername(username);
		return newTrack;
	}
	private void obtainSavedAuthentication(){
		SharedPreferences pref = this.getSharedPreferences(AuthenticationActivity.PREFERENCES,MODE_WORLD_READABLE);
		String token = pref.getString(AuthenticationActivity.TOKEN, "");
		String tokenSecret = pref.getString(AuthenticationActivity.TOKENSECRET, null);
		api = new SoundCloudAPI(getResources().getString(R.string.CONSUMER_KEY), 
				getResources().getString(R.string.CONSUMER_SECRET), token, tokenSecret);

	}
	 private void inform(String message){
	    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	    }
}
