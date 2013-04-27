package hk.hku.cs.lrcstudio;



import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;



import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;






public class LyricsParser extends Activity {
	
	private ArrayList<String> lyrics;
  
	 List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
    @TargetApi(9)
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	 StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	    StrictMode.setThreadPolicy(policy);
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.lyricsview);
        ListView lv= (ListView)findViewById(R.id.lyricsview);
        
        String[] from = new String[] {"lyric"};
        int[] to = new int[] { R.id.item1 };
        
        lyrics = new ArrayList();
    	Intent intent = getIntent();
    	String url=intent.getStringExtra("URL");
    
 
    	
    	SendHttpRequest(url);

       	ArrayAdapter adapter = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_list_item_1,lyrics);
    	lv.setAdapter(adapter);
    
    }
    

    
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private void ResultFilter(String data){
   
    							
    	String artist="";
    	String url="";

    	data=data.substring(data.indexOf("<div class='lyricbox'>"));
    	data=data.substring(data.indexOf("</div>")+6);
    	data=data.substring(0,data.indexOf("<div class=\'rt")-1);
    	data=data.replaceAll("&#","");
    	while(data.indexOf("<br />")>0){
    		String currentline=data.substring(0,data.indexOf("<br />")-1);
    		Log.v("currentline",currentline);
    		String lyric="";
    		String [] Temp_array=currentline.split(";");
    		for (int i = 0; i < Temp_array.length; i++) { 
    			lyric+=(char) Integer.parseInt(Temp_array[i]);
    		}
    		lyrics.add(lyric);
    		data=data.substring(data.indexOf("<br />")+6,data.length());
    	}
    	for (int i = 0; i < lyrics.size(); i++) {
    	    if(!lyrics.get(i).equals(null)){
    	        Log.v("data",lyrics.get(i));
    	    }
    	}
    	
    	
    	
    }
    
    
    private void SendHttpRequest(String url){
        
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
               String  text = new String(baf.toByteArray());
               ResultFilter(text);
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
           }
   
}
    
    