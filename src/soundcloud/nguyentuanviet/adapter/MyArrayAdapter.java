package soundcloud.nguyentuanviet.adapter;

import java.util.ArrayList;

import soundcloud.nguyentuanviet.R;
import soundcloud.nguyentuanviet.entities.Track;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MyArrayAdapter extends ArrayAdapter<Track> {
	private ArrayList<Track> items;
	private Context context;
	private int selectedPos = -1;
	public MyArrayAdapter(Context context,int textViewResourceId,ArrayList<Track> items) 
	{
		super(context, textViewResourceId, items);
		this.items = items;	
		this.context = context;
	}	
	public void replaceDataSet(ArrayList<Track> newItem){
		this.items = newItem;
		notifyDataSetChanged();
	}
	public void setSelectedPosition(int position){
		selectedPos = position;
		// inform the view of this change
		notifyDataSetChanged();
	}
	
	public int getSelectedPosition(){
		return selectedPos;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		final int location = position;
        if(v == null)
        {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.row, null);
        }                
        
        //get views
        TextView title = (TextView)v.findViewById(R.id.titleTV);
        TextView artist = (TextView)v.findViewById(R.id.artistTV);
        ImageView art = (ImageView)v.findViewById(R.id.avatarIV);  
        LinearLayout container = (LinearLayout)v.findViewById(R.id.selector);
        if(selectedPos == position){
        	container.setBackgroundResource(R.drawable.selection);
         }else{
        	container.setBackgroundColor(Color.WHITE);
        }
        
        title.setText(items.get(position).getTitle());
        artist.setText(items.get(position).getUsername());
        if(items.get(position).getArtwork()!=null){
            art.setBackgroundDrawable(items.get(position).getArtwork());
        }                     
     	return v;
	}	
	
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();		
	}
	@Override
	public void setNotifyOnChange(boolean notifyOnChange) {
		// TODO Auto-generated method stub
		super.setNotifyOnChange(notifyOnChange);
	}
	@Override 
	public int getCount() { 
	        return items.size(); 
	} 
	
}
