package soundcloud.nguyentuanviet;

import java.io.IOException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.client.ClientProtocolException;
import org.urbanstew.soundcloudapi.SoundCloudAPI;
import org.urbanstew.soundcloudapi.SoundCloudAPI.OAuthVersion;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

public class MediaPlayerActivity extends Activity {
	private SoundCloudAPI api;
	private ImageButton buttonPlayPause;
	private SeekBar seekBarProgress;
	
	//private MediaPlayer mediaPlayer;
	private int mediaFileLengthInMilliseconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class
	
	private final Handler handler = new Handler();
	private MPInterface mpInterface;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mediaplayer);
        getFreshAuthentication();
        getRedirectURL();
        //initView();
        seekBarProgress = (SeekBar)findViewById(R.id.SeekBarTestPlay);	
        this.bindService(new Intent(MediaPlayerActivity.this,MediaPlayerService.class), mConnection, Context.BIND_AUTO_CREATE);
        this.startService(new Intent(MediaPlayerActivity.this,MediaPlayerService.class));
    }
    private void inform(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	private ServiceConnection mConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mpInterface = MPInterface.Stub.asInterface((IBinder)service);
			try {
				mpInterface.clearPlaylist();
				mpInterface.addSongPlaylist(redirectURL);
				mpInterface.test(redirectURL);
				Log.i("SERVICE", "SERVICE CONNECTED");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mpInterface = null;
		}
    	
    };
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
	String redirectURL;
	private void getRedirectURL(){
		try {
			redirectURL = api.getRedirectedStreamUrl("https://api.soundcloud.com/tracks/11856032/stream?consumer_key=AIBMBzom4aIwS64tzA3uvg");
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
    private void initView() {
		buttonPlayPause = (ImageButton)findViewById(R.id.ButtonTestPlayPause);
		buttonPlayPause.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
			buttonPlayPauseClick();}
		});
		
		seekBarProgress = (SeekBar)findViewById(R.id.SeekBarTestPlay);	
		seekBarProgress.setMax(99); // It means 100% .0-99
		seekBarProgress.setOnTouchListener(new OnTouchListener() {
			@Override public boolean onTouch(View v, MotionEvent event) {
				seekMediaPlayerToSeekBarTouch(v);
				return false;}
		});
		
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			@Override public void onBufferingUpdate(MediaPlayer mp, int percent) {
				secondarySeekBarProgressUpdater(percent);}
		});
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() { 
			@Override public void onCompletion(MediaPlayer mp) {
				songPlayingComplete();}
		});
	}

	private void buttonPlayPauseClick(){
		try {
			mediaPlayer.setDataSource(redirectURL); 
			mediaPlayer.prepare(); // you must call this method after setup the datasource in setDataSource method. After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer. 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL
		
		if(!mediaPlayer.isPlaying()){
			mediaPlayer.start();
			buttonPlayPause.setImageResource(R.drawable.arrow_down);
		}else {
			mediaPlayer.pause();
			buttonPlayPause.setImageResource(R.drawable.arrow_up);
		}
		
		primarySeekBarProgressUpdater();
	}

    private void primarySeekBarProgressUpdater() {
    	seekBarProgress.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaFileLengthInMilliseconds)*100)); // This math construction give a percentage of "was playing"/"song length"
		if (mediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
		        public void run() {
		        	primarySeekBarProgressUpdater();
				}
		    };
		    handler.postDelayed(notification,1000);
    	}
    }
    
    private void secondarySeekBarProgressUpdater(int percent){
    	seekBarProgress.setSecondaryProgress(percent);
    }
    
    private void seekMediaPlayerToSeekBarTouch(View v){
    	if(mediaPlayer.isPlaying()){
	    	SeekBar sb = (SeekBar)v;
			int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
			mediaPlayer.seekTo(playPositionInMillisecconds);
		}
    }
    private void songPlayingComplete(){
    	buttonPlayPause.setImageResource(R.drawable.arrow_up);
    }*/
}