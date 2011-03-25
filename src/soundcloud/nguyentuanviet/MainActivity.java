package soundcloud.nguyentuanviet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class MainActivity extends Activity {
	private ImageButton downloadtrackactivityBtn;
	private ImageButton searchtrackactivityBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        downloadtrackactivityBtn = (ImageButton)findViewById(R.id.downloadtrack);
        searchtrackactivityBtn = (ImageButton)findViewById(R.id.searchtrack);
        downloadtrackactivityBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,DownloadTrackActivity.class);
				startActivity(intent);
			}
        	
        });
        searchtrackactivityBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,TrackTabWidget.class);
				startActivity(intent);
			}
        	
        });
    }
}