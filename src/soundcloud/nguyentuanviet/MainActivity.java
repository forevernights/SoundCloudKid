package soundcloud.nguyentuanviet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends Activity {
	private Button connectBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        connectBtn = (Button)findViewById(R.id.btnConnect);
        /*connectBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				 Uri uri = Uri.parse(authorizationURL);
				 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				 startActivity(intent);
			}
        	
        });*/
    }
}