package hk.hku.cs.lrcstudio;

import hk.hku.cs.lrcstudio.R.drawable;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LyricsAdapter  extends ArrayAdapter<String>{
	private ArrayList<String> objects;
	public ArrayList<Boolean> clickStatus;
	
	public LyricsAdapter(Context context, int textViewResourceId,
			ArrayList<String> objects) {
		super(context, textViewResourceId, objects);
		this.objects=objects;
		// TODO Auto-generated constructor stub
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		// assign the view we are converting to a local variable
		View v = convertView;
		// first check to see if the view is null. if so, we have to inflate it.
				// to inflate it basically means to render, or show, the view.
				if (v == null) {
					LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					v = inflater.inflate(R.layout.lyricsline, null);
				}
				String i = objects.get(position);
				TextView tt = (TextView) v.findViewById(R.id.tvLyricitem);
				if (tt != null){
					tt.setText(i);
				}
				if ((v != null)&&(convertView!=null)){
					
					// This is how you obtain a reference to the TextViews.
					// These TextViews are created in the XML files we defined.
					
					if(!clickStatus.get(position)) {    
						
						convertView.setBackgroundColor(Color.BLACK);
						
						
					}
					else{
						
							convertView.setBackgroundColor(Color.CYAN);
						
						
						
					}
				}
				

				// the view must be returned to our activity
				return v;

			}
}

	


