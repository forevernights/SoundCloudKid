package soundcloud.nguyentuanviet;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TrackTabWidget extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tracktab);

	    Resources res = getResources();
	    TabHost tabHost = getTabHost(); 
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, HottestTrackActivity.class);
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("hottest").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_hottesttrack))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    intent = new Intent().setClass(this, LatestTrackActivity.class);
	    spec = tabHost.newTabSpec("latest").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_latesttrack))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, SearchTrackActivity.class);
	    spec = tabHost.newTabSpec("search").setIndicator("",
	                      res.getDrawable(R.drawable.ic_tab_searchtrack))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(2);
	}
}
