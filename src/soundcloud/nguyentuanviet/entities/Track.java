package soundcloud.nguyentuanviet.entities;

import android.graphics.drawable.Drawable;

public class Track {
	int track_id;
	int user_id;
	String title;
	String username;
	Drawable artwork;
	String streamURL;
	String downloadURL;
	public Track(int track_id,int user_id,String title,
			String username,String streamURL,String downloadURL,Drawable artwork) {
		this.track_id = track_id;
		this.user_id = user_id;
		this.title = title;
		this.username = username;
		this.artwork = artwork;
		this.streamURL = streamURL;
		this.downloadURL = downloadURL;
	}
	public Track(){
		
	}
	public String getDownloadURL() {
		return downloadURL;
	}
	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}
	public String getStreamURL() {
		return streamURL;
	}

	public void setStreamURL(String streamURL) {
		this.streamURL = streamURL;
	}

	public int getTrack_id() {
		return track_id;
	}
	public void setTrack_id(int trackId) {
		track_id = trackId;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int userId) {
		user_id = userId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Drawable getArtwork() {
		return artwork;
	}
	public void setArtwork(Drawable artwork) {
		this.artwork = artwork;
	}
	
	
}
