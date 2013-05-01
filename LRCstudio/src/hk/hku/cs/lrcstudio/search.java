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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;





public class search extends Activity {
	
	 
  
	 List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
    @TargetApi(9)
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	 StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	    StrictMode.setThreadPolicy(policy);
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.results);
        ListView lv= (ListView)findViewById(R.id.listview);
        
        String[] from = new String[] {"artist", "URL"};
        int[] to = new int[] { R.id.item1, R.id.item2, };
        
        
    	Intent intent = getIntent();
    	String query=intent.getStringExtra("query");
    	
    	query=query.replaceAll("\\s","+");
    	String base="http://lyrics.wikia.com/index.php?search=";
    	String end="&fulltext=Search";
    	String url=base+query+end;
    	SendHttpRequest(url);
    	SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.griditem, from, to);
    	lv.setAdapter(adapter);
  	  lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      	  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
      		Intent intent = new Intent();
  		  	
  		  	//intent.putExtra("Song Name", fillMaps.get(position).get("artist").toString());
  		  	Bundle bundle = new Bundle();
  		  	bundle.putString("URL", fillMaps.get(position).get("URL").toString());
  		  	intent.putExtras(bundle);
  		  	
  		  	
  		  	//intent.setData(Uri.parse(fillMaps.get(position).get("URL").toString()));
  	    	setResult(RESULT_OK, intent);
  	    	finish();
      	  }
      	});  
    
    
    }
    

    
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private void ResultFilter(String data){

    							
    	String artist="";
    	String url="";
    	String findStr="<li class=\"result\">";
    	int secondIndex,lastIndex = 0;
    	while(data.indexOf(findStr)>0){
    		HashMap<String, String> map = new HashMap<String, String>();
    		lastIndex=data.indexOf(findStr);
    		lastIndex+=findStr.length();

    		data=data.substring(lastIndex);
    		url=data.substring(data.indexOf("http"),data.indexOf("class=")-2);
 		
    		secondIndex=data.indexOf("\" >");
    		artist=data.substring(secondIndex+3,data.indexOf("</a>"));

    		map.put("artist",artist);
    		map.put("URL",url);
    		fillMaps.add(map);
   		
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
    
    