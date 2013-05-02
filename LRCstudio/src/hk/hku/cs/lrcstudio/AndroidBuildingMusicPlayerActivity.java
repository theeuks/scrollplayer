package hk.hku.cs.lrcstudio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
	private ImageView albumArt;
	
	public ListView lv;
	
	private MediaMetadataRetriever Mdr;
	
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
	private LyricsAdapter adapter;

	// Lyrics for the current song.
	private Lyrics lyrics;
	private ArrayList<Boolean> clickStatus;
	private Lyrics editing_lyrics;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
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
		lv.setVisibility(View.GONE);
		
		albumArt = (ImageView) findViewById(R.id.imageView1);
		
		// Mediaplayer
		mp = new MediaPlayer();
		songManager = new SongsManager();
		utils = new Utilities();
		lyrics = new Lyrics();
		
		Mdr = new MediaMetadataRetriever();

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
				if (!EditorMode){
					if(currentSongIndex < (songsList.size() - 1)){
						playSong(currentSongIndex + 1);
						currentSongIndex = currentSongIndex + 1;
					}else{
						// play first song
						playSong(0);
						currentSongIndex = 0;
					}
				}
				else{
					mp.seekTo(mp.getDuration());
				}
			}
		});

		/**
		 * Back button click event
		 * Plays previous song by currentSongIndex - 1
		 * */
		btnPrevious.setOnClickListener(new View.OnClickListener() {


			public void onClick(View arg0) {
				
				if (!EditorMode){
				
					if(currentSongIndex > 0){
						playSong(currentSongIndex - 1);
						currentSongIndex = currentSongIndex - 1;
					}else{
						// play last song
						playSong(songsList.size() - 1);
						currentSongIndex = songsList.size() - 1;
					}
				}
				else {
					
					mp.seekTo(0);
					
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

				mp.pause();
				mp.seekTo(0);
				RecorderUI();
				
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

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (!EditorMode)super.onBackPressed();
		else {
			
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	PlayerUI();
			            //Yes button clicked
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //No button clicked
			            break;
			        }
			    }
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Discard edited lyrics?").setPositiveButton("Yes", dialogClickListener)
			    .setNegativeButton("No", dialogClickListener).show();
			
		}
		
		
		
	}

	/**
	 * Function to play a song
	 * @param songIndex - index of song
	 * */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	public void  playSong(int songIndex){
		// Play song
		try {
			mp.reset();

			String songPath = songsList.get(songIndex).get("songPath");
			mp.setDataSource(songPath);
			mp.prepare();
			mp.start();
			
			Mdr.setDataSource(songPath);
			byte[] art = Mdr.getEmbeddedPicture();
			if (art != null) {
				albumArt.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.length));
			}
			
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
			String lyricsPath = songPath.replaceFirst("\\.[mM][pP]3$", ".lrc");
			File lyricsFile = new File(lyricsPath);
			if (lyricsFile.exists()) {
				lyrics.load(new FileInputStream(lyricsFile), Lyrics.Format.LRC);
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
			editorEnd();
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
		lv.setVisibility(View.VISIBLE);
		
		
		EditorMode = true;
		
		Intent i = new Intent(this, search.class);
		i.putExtra("query", songTitle);
		startActivityForResult(i, EDITOR);

	}
	
	private void PlayerUI(){
		
		btnRepeat.setVisibility(View.VISIBLE);
		btnShuffle.setVisibility(View.VISIBLE);
		btnLyrics.setVisibility(View.VISIBLE);
		lv.setVisibility(View.GONE);
		editing_lyrics = null;
		clickStatus = null;
		EditorMode = false;
		
	}
	
	
	
	public void InitialRecorder(String url){
		

		ArrayList<String> tmplyrics = new ArrayList<String>();

    	tmplyrics = utils.SendHttpRequest(url);

       	//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 ,tmplyrics);
    	adapter = new LyricsAdapter(this,R.layout.lyricsline,tmplyrics);
    	lv.setAdapter(adapter);
    	lv.setSelector(R.drawable.list_selector);
    	
    	editing_lyrics = new Lyrics();
    	
    	for (String tmpline : tmplyrics){
    		editing_lyrics.addSubtitle(tmpline);
    	}
    	
    	clickStatus = new ArrayList<Boolean>();
    	
    	for (int i = 0; i < tmplyrics.size(); i++) clickStatus.add(false);
    	adapter.clickStatus=clickStatus;
    	Log.v("size of clickstatus",""+adapter.clickStatus.size());
    	lv.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
				if (!clickStatus.get(arg2)){
					arg1.setBackgroundColor(Color.CYAN);
					clickStatus.set(arg2, true);
					adapter.clickStatus.set(arg2,true);
					LyricLine tmpLine = editing_lyrics.getLyricLineDirect(arg2);
					tmpLine.endPosition = mp.getCurrentPosition();
					tmpLine.startPosition = mp.getCurrentPosition();
					
				}
				else{
					arg1.setBackgroundColor(Color.BLACK);
					clickStatus.set(arg2, false);
					adapter.clickStatus.set(arg2,false);
					LyricLine tmpLine = editing_lyrics.getLyricLineDirect(arg2);
					tmpLine.endPosition = null;
					tmpLine.startPosition = null;
					
				}
				lv.smoothScrollToPosition(arg2+4);
				
				
			}});
		
    	mp.start();
    	
	}
	
	private void editorEnd(){
		
		mp.pause();
		mp.seekTo(0);
		btnPlay.setImageResource(R.drawable.btn_play);
		editing_lyrics.removeNullTimeLine();
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            //Yes button clicked
		        	OutputStream fs = null;
		        	try {
		    			fs = new FileOutputStream("/sdcard/Music/" + songTitle + ".lrc");
		    		} catch (FileNotFoundException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}
		    		
		    		try {
		    			editing_lyrics.save(fs, Lyrics.Format.LRC);
		    		} catch (IOException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}
		    		
		    		PlayerUI();
		    		
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Save lyrics?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		
		
		
		
	}
	
}