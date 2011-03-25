package soundcloud.nguyentuanviet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

public class MediaPlayerService extends Service {
	public MediaPlayer mp = new MediaPlayer();
	private List<String> tracks = new ArrayList<String>();
	private int currentPosition = 0;
	private NotificationManager nm;
	private static final int NOTIFY_ID = 1;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	/** Binder object*/
	private final MPInterface.Stub mBinder = new MPInterface.Stub() {
		
		@Override
		public void stop() throws RemoteException {
			nm.cancel(NOTIFY_ID);
			mp.stop();
		}
		
		@Override
		public void skipForward() throws RemoteException {
			nextSong();
		}
		
		@Override
		public void skipBack() throws RemoteException {
			prevSong();
		}
		
		@Override
		public void playFile(int position) throws RemoteException {
			try{
				currentPosition = position;
				playSong(tracks.get(position));
			}
			catch(IndexOutOfBoundsException ex){
				Log.e(getString(R.string.app_name), ex.getMessage());
			}
		}
		@Override
		public void pause() throws RemoteException {
			//Notification notification = new Notification(R.drawable.arrow_down,null,0);
			//nm.notify(NOTIFY_ID,notification);
			mp.pause();
		}
		
		@Override
		public void clearPlaylist() throws RemoteException {
			tracks.clear();
		}
		
		@Override
		public void addSongPlaylist(String song) throws RemoteException {
			tracks.add(song);
		}
		private void playSong(String path){
			try{
				Notification notification = new Notification(R.drawable.arrow_up,path,System.currentTimeMillis());
				Intent notificationIntent = new Intent(getApplicationContext(), MediaPlayerActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

				notification.setLatestEventInfo(getApplicationContext(), "Playing", "Hello World", contentIntent);
				nm.notify(NOTIFY_ID, notification);
				mp.reset();
				mp.setDataSource(path);
				mp.prepare();
				mp.start();
				mp.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						nextSong();
					}
				});
			}
			catch(IOException ex){
				Log.e(getString(R.string.app_name),ex.getMessage());
			}
		}
		private void nextSong(){
			if(++currentPosition>=tracks.size()){
				currentPosition = 0;
				nm.cancel(NOTIFY_ID);
			}else{
				playSong(tracks.get(currentPosition));
			}
		}
		private void prevSong(){
			
		}

		@Override
		public void test(String track) throws RemoteException {
			playSong(track);
		}

		@Override
		public int getCurrentDuration() throws RemoteException {
			// TODO Auto-generated method stub
			return mp.getCurrentPosition();
		}

		@Override
		public int getDuration() throws RemoteException {
			// TODO Auto-generated method stub
			return mp.getDuration();
		}
	};
	
	 	
	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public void onDestroy() {
		mp.stop();
		mp.release();
		nm.cancel(NOTIFY_ID);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	

}
