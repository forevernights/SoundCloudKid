package soundcloud.nguyentuanviet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import soundcloud.nguyentuanviet.helpers.StreamingMediaPlayer;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MediaPlayerActivity extends Activity {
	//set views
	private Button streamButton;
	private ImageButton playButton;
	private TextView textStreamed;
	private boolean isPlaying;
	private StreamingMediaPlayer audioStreamer;
	
	String streamURL="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mediaplayer);
		streamURL = getIntent().getStringExtra(TrackActivity.STREAM_URL);
		String id = getIntent().getStringExtra(TrackActivity.TRACK_ID);
		initControls();		
	}	
	private void inform(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	 private void initControls() {
	    	textStreamed = (TextView) findViewById(R.id.text_kb_streamed);
			streamButton = (Button) findViewById(R.id.button_stream);
			streamButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					startStreamingAudio();
	        }});

			playButton = (ImageButton) findViewById(R.id.button_play);
			playButton.setEnabled(false);
			playButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					if (audioStreamer.getMediaPlayer().isPlaying()) {
						audioStreamer.getMediaPlayer().pause();
						playButton.setImageResource(R.drawable.icon);
					} else {
						audioStreamer.getMediaPlayer().start();
						audioStreamer.startPlayProgressUpdater();
						playButton.setImageResource(R.drawable.scconnect);
					}
					isPlaying = !isPlaying;
	        }});
	    }
	    
	 	URL redirectURL;
	    private void startStreamingAudio() {
	    	//get redirected stream url
    		URL url;
    		try {
    			url = new URL(streamURL);
    			HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
    			ucon.setInstanceFollowRedirects(false);
    			redirectURL = new URL(ucon.getHeaderField("Location"));
    		} catch (MalformedURLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
	    	try { 
	    		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
	    		if ( audioStreamer != null) {
	    			audioStreamer.interrupt();
	    		}
	    		audioStreamer = new StreamingMediaPlayer(this,textStreamed,playButton,streamButton,progressBar);
	    		audioStreamer.startStreaming(redirectURL,3730, 216);
	    		streamButton.setEnabled(false);
	    	} catch (IOException e) {
		    	Log.e(getClass().getName(), "Error starting to stream audio.", e);            		
	    	}
	    	    	
	    }
}
