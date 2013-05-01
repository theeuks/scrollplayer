package hk.hku.cs.lrcstudio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidBuildingMusicPlayerActivity extends Activity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {

	private static final int EDITOR = 0;
	
	
	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private ImageButton btnPlaylist;
	private ImageButton btnRepeat;
	private ImageButton btnShuffle;
	private Button btnLyrics;
	private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	private TextView lyricsLabel;
	
	public ListView lv;
	// Media Player
	private  MediaPlayer mp;
	// Handler to update UI timer, progress bar etc,.
	private final Handler mHandler = new Handler();;
	private SongsManager songManager;
	private Utilities utils;
	private String songTitle;
	private final int seekForwardTime = 5000; // 5000 milliseconds
	private final int seekBackwardTime = 5000; // 5000 milliseconds
	private int currentSongIndex = 0;
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	private boolean EditorMode = false;

	// Lyrics for the current song.
	private Lyrics lyrics;
	
	private Lyrics editing_lyrics;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);

		// All player buttons
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnForward = (ImageButton) findViewById(R.id.btnForward);
		btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
		btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
		btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
		btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
		lyricsLabel = (TextView) findViewById(R.id.lyricsLabel);
		btnLyrics = (Button) findViewById(R.id.btnLyrics);

		lv= (ListView)findViewById(R.id.lyricsview);
		
		// Mediaplayer
		mp = new MediaPlayer();
		songManager = new SongsManager();
		utils = new Utilities();
		lyrics = new Lyrics();

		// Listeners
		songProgressBar.setOnSeekBarChangeListener(this); // Important
		mp.setOnCompletionListener(this); // Important

		// Getting all songs list
		songsList = songManager.getPlayList();

		// By default play first song
		if(songsList.size() >0){
			// By default play first song
			playSong(0);
		}

		/**
		 * Play button click event
		 * plays a song and changes button to pause image
		 * pauses a song and changes button to play image
		 * */
		btnPlay.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				// check for already playing
				if(mp.isPlaying()){
					if(mp!=null){
						mp.pause();
						// Changing button image to play button
						btnPlay.setImageResource(R.drawable.btn_play);
					}
				}else{
					// Resume song
					if(mp!=null){
						mp.start();
						// Changing button image to pause button
						btnPlay.setImageResource(R.drawable.btn_pause);
					}
				}

			}
		});

		/**
		 * Forward button click event
		 * Forwards song specified seconds
		 * */
		btnForward.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				// get current song position
				int currentPosition = mp.getCurrentPosition();
				// check if seekForward time is lesser than song duration
				if(currentPosition + seekForwardTime <= mp.getDuration()){
					// forward song
					mp.seekTo(currentPosition + seekForwardTime);
				}else{
					// forward to end position
					mp.seekTo(mp.getDuration());
				}
			}
		});

		/**
		 * Backward button click event
		 * Backward song to specified seconds
		 * */
		btnBackward.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				// get current song position
				int currentPosition = mp.getCurrentPosition();
				// check if seekBackward time is greater than 0 sec
				if(currentPosition - seekBackwardTime >= 0){
					// forward song
					mp.seekTo(currentPosition - seekBackwardTime);
				}else{
					// backward to starting position
					mp.seekTo(0);
				}

			}
		});

		/**
		 * Next button click event
		 * Plays next song by taking currentSongIndex + 1
		 * */
		btnNext.setOnClickListener(new View.OnClickListener() {


			public void onClick(View arg0) {
				// check if next song is there or not
				if(currentSongIndex < (songsList.size() - 1)){
					playSong(currentSongIndex + 1);
					currentSongIndex = currentSongIndex + 1;
				}else{
					// play first song
					playSong(0);
					currentSongIndex = 0;
				}

			}
		});

		/**
		 * Back button click event
		 * Plays previous song by currentSongIndex - 1
		 * */
		btnPrevious.setOnClickListener(new View.OnClickListener() {


			public void onClick(View arg0) {
				if(currentSongIndex > 0){
					playSong(currentSongIndex - 1);
					currentSongIndex = currentSongIndex - 1;
				}else{
					// play last song
					playSong(songsList.size() - 1);
					currentSongIndex = songsList.size() - 1;
				}

			}
		});

		/**
		 * Button Click event for Repeat button
		 * Enables repeat flag to true
		 * */
		btnRepeat.setOnClickListener(new View.OnClickListener() {


			public void onClick(View arg0) {
				if(isRepeat){
					isRepeat = false;
					Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				}else{
					// make repeat to true
					isRepeat = true;
					Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isShuffle = false;
					btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}
			}
		});

		/**
		 * Button Click event for Shuffle button
		 * Enables shuffle flag to true
		 * */
		btnShuffle.setOnClickListener(new View.OnClickListener() {


			public void onClick(View arg0) {
				if(isShuffle){
					isShuffle = false;
					Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
					btnShuffle.setImageResource(R.drawable.btn_shuffle);
				}else{
					// make repeat to true
					isShuffle= true;
					Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isRepeat = false;
					btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
					btnRepeat.setImageResource(R.drawable.btn_repeat);
				}
			}
		});

		/**
		 * Button Click event for Play list click event
		 * Launches list activity which displays list of songs
		 * */
		btnPlaylist.setOnClickListener(new View.OnClickListener() {


			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
				startActivityForResult(i, 100);
			}
		});

		/**
		 * Button Click event for Lyrics click event
		 * Shows lyrics.. duh
		 * */
		btnLyrics.setOnClickListener(new View.OnClickListener() {


			public void onClick(View arg0) {

				RecorderUI();
				EditorMode = true;
				
			}
		});




	}

	/**
	 * Receiving song index from playlist view
	 * and play the song
	 * */

	@Override
	protected void onActivityResult(int requestCode,
			int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 100){
			currentSongIndex = data.getExtras().getInt("songIndex");
			// play selected song
			playSong(currentSongIndex);
		}
		if (requestCode == EDITOR){
        	
        	if (resultCode == RESULT_OK){
        		Log.i("url", "previous");
            	
            	String url=data.getExtras().getString("URL");
            	InitialRecorder(url);
        	}
        	
        }

	}

	/**
	 * Function to play a song
	 * @param songIndex - index of song
	 * */
	public void  playSong(int songIndex){
		// Play song
		try {
			mp.reset();

			String songPath = songsList.get(songIndex).get("songPath");
			mp.setDataSource(songPath);
			mp.prepare();
			mp.start();
			// Displaying Song title
			songTitle = songsList.get(songIndex).get("songTitle");
			songTitleLabel.setText(songTitle);

			// Changing Button Image to pause image
			btnPlay.setImageResource(R.drawable.btn_pause);

			// set Progress bar values
			songProgressBar.setProgress(0);
			songProgressBar.setMax(100);

			// Updating progress bar
			updateProgressBar();

			// Load lyrics for the current song.
			String lyricsPath = songPath.replaceFirst("\\.[mM][pP]3$", ".srt");
			File lyricsFile = new File(lyricsPath);
			if (lyricsFile.exists()) {
				lyrics.load(new FileInputStream(lyricsFile), Lyrics.Format.SUBRIP);
			} else {
				// Clear existing lyrics which may be loaded from other songs.
				lyrics.clear();
			}

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update timer on seekbar
	 * */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	/**
	 * Background Runnable thread
	 * */
	private final Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = mp.getDuration();
			int currentDuration = mp.getCurrentPosition();

			// Displaying Total Duration time
			songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

			// Updating progress bar
			int progress = (utils.getProgressPercentage(currentDuration, totalDuration));
			//Log.d("Progress", ""+progress);
			songProgressBar.setProgress(progress);

			// Update lyrics text.
			LyricLine currentLine = lyrics.getSubtitle(currentDuration);
			if (currentLine != null) {
				lyricsLabel.setText(currentLine.text);
			} else {
				lyricsLabel.setText("");
			}

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};

	/**
	 * 
	 * */

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

	}

	/**
	 * When user starts moving the progress handler
	 * */
	public void onStartTrackingTouch(SeekBar seekBar) {
		// remove message Handler from updating progress bar
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * When user stops moving the progress hanlder
	 * */

	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = mp.getDuration();
		int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

		// forward or backward to certain seconds
		mp.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

	/**
	 * On Song Playing completed
	 * if repeat is ON play same song again
	 * if shuffle is ON play random song
	 * */

	public void onCompletion(MediaPlayer arg0) {

		if (!EditorMode){
		// check for repeat is ON or OFF
			if(isRepeat){
				// repeat is on play same song again
				playSong(currentSongIndex);
			} else if(isShuffle){
				// shuffle is on - play a random song
				Random rand = new Random();
				currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
				playSong(currentSongIndex);
			} else{
				// no repeat or shuffle ON - play next song
				if(currentSongIndex < (songsList.size() - 1)){
					playSong(currentSongIndex + 1);
					currentSongIndex = currentSongIndex + 1;
				}else{
					// play first song
					playSong(0);
					currentSongIndex = 0;
				}
			}
		}
		else{
			mp.pause();
			btnPlay.setImageResource(R.drawable.btn_play);
		}
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		mp.release();
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	public void RecorderUI(){
		
		btnRepeat.setVisibility(View.GONE);
		btnShuffle.setVisibility(View.GONE);
		btnLyrics.setVisibility(View.GONE);
		
		Intent i = new Intent(this, search.class);
		i.putExtra("query", songTitle);
		startActivityForResult(i, EDITOR);

	}
	
	private ArrayList<String> SendHttpRequest(String url){
        
		String  text = null;
        try {
              HttpClient httpclient = new DefaultHttpClient();  
              HttpPost httppost = new HttpPost(url);  
              
              
              List nameValuePairs = new ArrayList();  
              //nameValuePairs.add(new BasicNameValuePair("data", "Atul Yadav"));   
              httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,HTTP.UTF_8));  

              // Execute HTTP Post Request  
              HttpResponse response = httpclient.execute(httppost);
              
              InputStream is = response.getEntity().getContent();
              BufferedInputStream bis = new BufferedInputStream(is);
              ByteArrayBuffer baf = new ByteArrayBuffer(20);

               int current = 0;  
               while((current = bis.read()) != -1){  
                   baf.append((byte)current);  
               }  
                
              /* Convert the Bytes read to a String. */
               //TextView lbl = (TextView) findViewById(R.id.tv1);
               text = new String(baf.toByteArray());
               
               //lbl.setText(text);
             

           } catch (ClientProtocolException e) {  
               // TODO Auto-generated catch block 
                //TextView lbl = (TextView) findViewById(R.id.tv1);
                //lbl.setText(e.getMessage());
               
          } catch (IOException e) {  
               // TODO Auto-generated catch block 
                //TextView lbl = (TextView) findViewById(R.id.tv1);
                //lbl.setText(e.getMessage());
             
          }  
		
		
			return ResultFilter(text);
	}
	
	private ArrayList<String> ResultFilter(String data){
	   
		ArrayList<String> lyrics = new ArrayList<String>();
		
		String artist="";
		String url="";
	
		data=data.substring(data.indexOf("<div class='lyricbox'>"));
		data=data.substring(data.indexOf("</div>")+6);
		data=data.substring(0,data.indexOf("<div class=\'rt")-1);
		data=data.replaceAll("&#","");
		data=data.replaceAll("<b>","");
		data=data.replaceAll("</b>","");
		data=data.replaceAll("<br /><br />","<br />");
		while(data.indexOf("<br />")>0){
			String currentline=data.substring(0,data.indexOf("<br />"));
			
			String lyric="";
			String [] Temp_array=currentline.split(";");
			for (int i = 0; i < Temp_array.length; i++) { 
				lyric+=(char) Integer.parseInt(Temp_array[i]);
			}
			lyrics.add(lyric);
			data=data.substring(data.indexOf("<br />")+6,data.length());
			Log.v("currentline",data);
		}
		for (int i = 0; i < lyrics.size(); i++) {
		    if(!lyrics.get(i).equals(null)){
		        Log.v("data",lyrics.get(i));
		    }
		}
	
		return lyrics;

	}
	
	public void InitialRecorder(String url){
		
		ArrayList<String> tmplyrics = new ArrayList<String>();

    	tmplyrics = SendHttpRequest(url);

       	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 ,tmplyrics);
    	lv.setAdapter(adapter);
    	
    	editing_lyrics = new Lyrics();
    	
    	for (String tmpline : tmplyrics){
    		editing_lyrics.addSubtitle(tmpline);
    	}
    	
    	
    	lv.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
				
				
				
			}});
		
	}
	
	
}