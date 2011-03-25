package soundcloud.nguyentuanviet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.urbanstew.soundcloudapi.SoundCloudAPI;
import org.urbanstew.soundcloudapi.SoundCloudAPI.OAuthVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soundcloud.nguyentuanviet.adapter.MyArrayAdapter;
import soundcloud.nguyentuanviet.entities.Track;
import soundcloud.nguyentuanviet.quickaction.ActionItem;
import soundcloud.nguyentuanviet.quickaction.QuickAction;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import android.widget.AdapterView.OnItemClickListener;

public class SearchTrackActivity extends ListActivity {
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
		setContentView(R.layout.searchtrack);
		
		//obtain saved authentication
		//this.obtainSavedAuthentication();
		getFreshAuthentication();
		setupViews();
		setListView();
		new InitActivity().execute();
		setUpQuickActionItems();		
	}
	//fresh authentication and saved authentication
	private void getFreshAuthentication(){
		api = new SoundCloudAPI(getResources().getString(R.string.CONSUMER_KEY), getResources().getString(R.string.CONSUMER_SECRET), SoundCloudAPI.USE_PRODUCTION.with(OAuthVersion.V2_0));
		try {
			api.obtainAccessToken("darkoftime@gmail.com","`123qwer");
			/*api = new SoundCloudAPI(getResources().getString(R.string.CONSUMER_KEY), 
					getResources().getString(R.string.CONSUMER_SECRET), api.getToken(), 
					api.getTokenSecret());*/
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void obtainSavedAuthentication(){
		SharedPreferences pref = this.getSharedPreferences(AuthenticationActivity.PREFERENCES,MODE_WORLD_READABLE);
		String token = pref.getString(AuthenticationActivity.TOKEN, "");
		String tokenSecret = pref.getString(AuthenticationActivity.TOKENSECRET, null);
		api = new SoundCloudAPI(getResources().getString(R.string.CONSUMER_KEY), 
				getResources().getString(R.string.CONSUMER_SECRET), token, tokenSecret);

	}
	//set listivew
	int selectedTrackPos = 0;
	QuickAction qa;
	private void setListView(){
		lv = getListView();
		myAdapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_1, tracklist);
		lv.setAdapter(myAdapter);
        registerForContextMenu(lv);
        lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView adapterview, View view,
                                           int position, long id) {
				myAdapter.setSelectedPosition(position);
				qa = new QuickAction(view);
				if(tracklist.get(position).getStreamURL()==""){
					inform("Sorry this sound is unstreamable.");
					selectedTrackPos = position;
					qa.addActionItem(favItem);
					qa.addActionItem(commentItem);
					qa.addActionItem(downItem);
					qa.setAnimStyle(QuickAction.ANIM_AUTO);
					qa.show();
				}
				else{
					selectedTrackPos = position;
					qa.addActionItem(tryItem);
					qa.addActionItem(playItem);
					qa.addActionItem(favItem);
					qa.addActionItem(commentItem);
					qa.addActionItem(downItem);
					qa.setAnimStyle(QuickAction.ANIM_AUTO);
					qa.show();
				}
			}
        });
	}
	private void showCommentDialog(){
		final Dialog dialog = new Dialog(this);
    	dialog.setContentView(R.layout.comment_dialog);
    	dialog.setTitle("Quick comment box");
        Button postCommentBtn = (Button)dialog.findViewById(R.id.postCommentBtn);
        Button cancelCommentBtn = (Button)dialog.findViewById(R.id.cancelCommentBtn);
        final EditText commentBox = (EditText)dialog.findViewById(R.id.commentET);
        postCommentBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("comment[body]", commentBox.getText().toString()));				                                
				try {
					HttpResponse response = api.post("tracks/" + tracklist.get(selectedTrackPos).getTrack_id() + "/comments", params);
					if(response.getStatusLine().getStatusCode()==201){
						inform("Comment successfully created.");
					}
					else{
						inform("Failed to post comment. Something goes wrong!");
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
				}
				dialog.dismiss();
			}
        });
        cancelCommentBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
        	
        });
    	dialog.show();
	}
	/** set up quickation menu **/
	ActionItem commentItem = new ActionItem();
	ActionItem favItem = new ActionItem();
	ActionItem downItem = new ActionItem();
	ActionItem tryItem = new ActionItem();
	ActionItem playItem = new ActionItem();
	
	MediaPlayer mp = new MediaPlayer();
	URL redirectURL;
	private void setUpQuickActionItems(){
		//try the track
		tryItem.setTitle("Quick Play");
		tryItem.setIcon(getResources().getDrawable(R.drawable.music));
		tryItem.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			URL url;
    		try {
    			url = new URL(tracklist.get(selectedTrackPos).getStreamURL()+"?consumer_key=AIBMBzom4aIwS64tzA3uvg");
    			HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
    			ucon.setInstanceFollowRedirects(false);
    			redirectURL = new URL(ucon.getHeaderField("Location"));
    			if(mp!=null){
    				mp.reset();
    				mp.setDataSource("http://"+redirectURL.getHost()+"/"+redirectURL.getPath()+"?"+redirectURL.getQuery());
    				mp.prepareAsync();
    				mp.setOnPreparedListener(new OnPreparedListener(){

    					@Override
    					public void onPrepared(MediaPlayer mp) {
    						mp.start();
    					}
    					
    				});
    			}
    		} catch (MalformedURLException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} 
		}
		});
		//play track
		playItem.setTitle("Play");
		playItem.setIcon(getResources().getDrawable(R.drawable.music));
		playItem.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			Intent myIntent = new Intent(SearchTrackActivity.this,MediaPlayerActivity.class);
			myIntent.putExtra(TRACK_ID, tracklist.get(selectedTrackPos).getTrack_id());
			myIntent.putExtra(STREAM_URL, tracklist.get(selectedTrackPos).getStreamURL());
			startActivity(myIntent);
			
		}
		});
		//favorite track
		favItem.setTitle("Favorite");
		favItem.setIcon(getResources().getDrawable(R.drawable.love));
		favItem.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				HttpResponse response = api.put("me/favorites/" + tracklist.get(selectedTrackPos).getTrack_id());
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
			}

		}
		});
		//comment on track
		commentItem.setTitle("Quick Comment");
		commentItem.setIcon(getResources().getDrawable(R.drawable.music));
		commentItem.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			showCommentDialog();
		}
		});
		//download track
		downItem.setTitle("Download");
		downItem.setIcon(getResources().getDrawable(R.drawable.music));
		downItem.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(checkMediaState()){
				if(tracklist.get(selectedTrackPos).getDownloadURL().equals("")
						&&tracklist.get(selectedTrackPos).getStreamURL().equals("")){
					inform("Download not available!");
				}
				else{
					String downloadString="";
					if(tracklist.get(selectedTrackPos).getDownloadURL().equals("")){
						downloadString = tracklist.get(selectedTrackPos).getStreamURL()+
						"?consumer_key=AIBMBzom4aIwS64tzA3uvg";
					}
					else{
						downloadString = tracklist.get(selectedTrackPos).getDownloadURL()+
						"?consumer_key=AIBMBzom4aIwS64tzA3uvg";
					}
					new DownloadTrackTasks().execute(downloadString);
				}
			}
			else{
				inform("SD Card not found!");
			}
		}
		});
	}

	/** get artwork**/
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
	NodeList nodelist;
	//TODO:Change number of items wanna show up here
	int numOfItemPerPage = 3;
	int currentTrack = 0;
	boolean isMorePage = true;
	private void loadTrackInfo(String search){
		try {
			HttpResponse response = api.get("tracks/?q="+search);
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    Document dom = db.parse(response.getEntity().getContent());
		    Element rootNode = dom.getDocumentElement();
		    nodelist = rootNode.getElementsByTagName("track");
		    
		    if(nodelist!=null&&nodelist.getLength()>0){
	    		for(int i=0;i<numOfItemPerPage;i++){
	    			if(i>=nodelist.getLength()){
	    				isMorePage=false;
	    				break;
	    			}
	    			else{
	    				tracklist.add(getSingleTrack(i));	
						currentTrack = i;
	    			}
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
;
	//load more track in case user want that
	int chunkItem = numOfItemPerPage;
	private void loadMoreTrackInfo(){
		chunkItem+=numOfItemPerPage;
		for(int i = currentTrack;i<chunkItem;i++){
			currentTrack+=1;
			if(currentTrack>=nodelist.getLength()){
				break;
			}
			else{
				tracklist.add(getSingleTrack(currentTrack));
			}
		}
	}
	//get single track for lazy thread
	private Track getSingleTrack(int num){
		Element entry = (Element)nodelist.item(num);
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
		String downURL="";
		if((Element)entry.getElementsByTagName("download-url").item(0)!=null){
			Element downloadNode = (Element)entry.getElementsByTagName("download-url").item(0);
			downURL = downloadNode.getFirstChild().getNodeValue();
		}
		Track newTrack = new Track();
		newTrack.setArtwork(this.getArtwork(avatarNode.getFirstChild().getNodeValue()));
		newTrack.setStreamURL(streamURL);
		newTrack.setTitle(title);
		newTrack.setTrack_id(trackId);
		newTrack.setUser_id(userId);
		newTrack.setUsername(username);
		newTrack.setDownloadURL(downURL);
		return newTrack;
	}

	private void inform(String message){
	    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	/** setup UI elements**/
	//views
	private EditText searchET;
	private Button buttonSearch;
	private void setupViews(){
		//set views
		buttonSearch = (Button)findViewById(R.id.searchTrackBtn);
		searchET = (EditText)findViewById(R.id.searchTrackET);
		//set event listener for button search
		buttonSearch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new InitActivity().execute();
			}		
		});		
	}
	private ViewSwitcher switcher;
	private void setupUI(){
	  //create the ViewSwitcher in the current context
	  switcher = new ViewSwitcher(this);
	  ImageButton footer = (ImageButton)View.inflate(this, R.layout.switcher_button, null);
	  View progress = View.inflate(this, R.layout.switcher_progress_bar, null);
	  
	  //add the views (first added will show first)
	  switcher.addView(footer);
	  switcher.addView(progress);
	  
	  //add the ViewSwitcher to the footer
	  getListView().addFooterView(switcher);
	  footer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				switcher.showNext();
				new LoadMoreItems().execute();
			}
		
		});

	}
	private void downloadTrack(URL url,String fileName) throws IOException{
		if(checkMediaState()){
			File dir = new File(Environment.getExternalStorageDirectory()+"/SoundCloudKid");
			if(!dir.exists()){
				dir.mkdir();
			}
			URLConnection con = url.openConnection();
			con.connect();
			int fileLength = con.getContentLength();
			InputStream is = con.getInputStream();
			if (is == null) {
	        	Log.e(getClass().getName(), "Unable to create InputStream for url:" + url);
	        }
			FileOutputStream fos = new FileOutputStream(dir+"/"+fileName);
			byte buffer[] = new byte[16384];
			int totalBytesRead = 0;
			int numread = 0;
			while((numread=is.read(buffer))!=-1){
				totalBytesRead += numread;
				fos.write(buffer,0,numread);
			}
			is.close();
			fos.close();
		}
	}
	private Boolean checkMediaState(){
		boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = false;
		}
		return mExternalStorageAvailable;
	}
	/** Background Task to initialize the activity**/
	private class LoadMoreItems extends AsyncTask<Object, Object, Object> {
		@Override 
		/* Background Task is Done */
		protected void onPostExecute(Object result) {
			//go back to the first view
			switcher.showPrevious();
			myAdapter.notifyDataSetChanged();
		}

		@Override
		protected Object doInBackground(Object... params) {
			loadMoreTrackInfo();
			return null;
		}
	}
	/** Background Task to Get First Items**/
	private class InitActivity extends AsyncTask<Object, Object, Object> {
		@Override 
		/* Background Task is Done */
		protected void onPostExecute(Object result) {
			setupUI();
		}

		@Override
		protected Object doInBackground(Object... params) {
			/*if(searchET.getText().toString().equals("")){
				
			}
			else{
				String searchterm = searchET.getText().toString().replaceAll(" ", "+");
				tracklist.clear();
				loadTrackInfo(searchterm);	
			}*/
			loadTrackInfo("SNSD");
			return null;
		}
	}
	/** Background Task to download track**/
	ProgressDialog mProgressDialog;
	private class DownloadTrackTasks extends AsyncTask<String, String, Object> {
		@Override 
		/* Background Task is Done */
		protected void onPostExecute(Object result) {
			mProgressDialog.dismiss();
		}

		@Override
		protected Object doInBackground(String... urls) {
			int count = urls.length;
			for(int i=0;i<count;i++){
				URL url = null;
				URL redirectURL=null;
				try {
					url = new URL(urls[i]);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
				try {
					HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
					ucon.setInstanceFollowRedirects(false);
					redirectURL = new URL(ucon.getHeaderField("Location"));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				File dir = new File(Environment.getExternalStorageDirectory()+"/SoundCloudKid");
				if(!dir.exists()){
					dir.mkdir();
				}
				URLConnection con;
				try {
					con = redirectURL.openConnection();
					con.connect();
					int fileLength = con.getContentLength();
					InputStream is = con.getInputStream();
					if (is == null) {
			        	Log.e(getClass().getName(), "Unable to create InputStream for url:" + redirectURL);
			        }
					FileOutputStream fos = new FileOutputStream(dir+"/"+
							tracklist.get(selectedTrackPos).getTitle()+
							String.valueOf(tracklist.get(selectedTrackPos).getTrack_id())+
							".mp3");
					byte buffer[] = new byte[16384];
					long totalBytesRead = 0;
					int numread = 0;
					while((numread=is.read(buffer))!=-1){
						totalBytesRead += numread;
						publishProgress(""+(int)((totalBytesRead*100)/fileLength));
						fos.write(buffer,0,numread);
					}
					is.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = new ProgressDialog(SearchTrackActivity.this);
			mProgressDialog.setMessage("Downloading...");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.show();
		}
		@Override
		protected void onProgressUpdate(String... progress) {
			mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
		
	}
}
