package soundcloud.nguyentuanviet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class LatestTrackActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("Latest Track");
        setContentView(textview);
    }

}
