package soundcloud.nguyentuanviet;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DownloadTrackActivity extends ListActivity {
	private ArrayList<String>tracklist = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.downloadtrack);
		populateTrackList(new File(Environment.getExternalStorageDirectory()+"/SoundCloudKid").listFiles());
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}
	private void populateTrackList(File[] files){
		for(File file:files){
			tracklist.add(file.getName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,tracklist);
		setListAdapter(adapter);
	}
}
