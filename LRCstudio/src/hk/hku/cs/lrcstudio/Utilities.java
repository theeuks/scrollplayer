package hk.hku.cs.lrcstudio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;


public class Utilities {
	
	/**
	 * Function to convert milliseconds time to
	 * Timer Format
	 * Hours:Minutes:Seconds
	 * */
	public String milliSecondsToTimer(long milliseconds){
		String finalTimerString = "";
		String secondsString = "";
		
		// Convert total duration into time
		   int hours = (int)( milliseconds / (1000*60*60));
		   int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
		   int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
		   // Add hours if there
		   if(hours > 0){
			   finalTimerString = hours + ":";
		   }
		   
		   // Prepending 0 to seconds if it is one digit
		   if(seconds < 10){ 
			   secondsString = "0" + seconds;
		   }else{
			   secondsString = "" + seconds;}
		   
		   finalTimerString = finalTimerString + minutes + ":" + secondsString;
		
		// return timer string
		return finalTimerString;
	}
	
	/**
	 * Function to get Progress percentage
	 * @param currentDuration
	 * @param totalDuration
	 * */
	public int getProgressPercentage(long currentDuration, long totalDuration){
		Double percentage = (double) 0;
		
		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);
		
		// calculating percentage
		percentage =(((double)currentSeconds)/totalSeconds)*100;
		
		// return percentage
		return percentage.intValue();
	}

	/**
	 * Function to change progress to timer
	 * @param progress - 
	 * @param totalDuration
	 * returns current duration in milliseconds
	 * */
	public int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = (int) (totalDuration / 1000);
		currentDuration = (int) ((((double)progress) / 100) * totalDuration);
		
		// return current duration in milliseconds
		return currentDuration * 1000;
	}
	
	public ArrayList<String> SendHttpRequest(String url){
        
		String  text = null;
        try {
              HttpClient httpclient = new DefaultHttpClient();  
              HttpPost httppost = new HttpPost(url);  
              
              
              List nameValuePairs = new ArrayList();  
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
                

               text = new String(baf.toByteArray());

             

           } catch (ClientProtocolException e) {  

               
          } catch (IOException e) {  

             
          }  
		
		
			return ResultFilter(text);
	}
	
	private ArrayList<String> ResultFilter(String data){
	   
		ArrayList<String> lyrics = new ArrayList<String>();
		boolean gracenote = false; 
		if(data.contains("uneditable Gracenote version")){
			gracenote=true;
		}
		data=data.substring(data.indexOf("<div class='lyricbox'>"));

		data=data.substring(data.indexOf("</div>")+6);
		data=data.substring(0,data.indexOf("<div class=\'rt")-1);
		data=data.replaceAll("&#","");
		data=data.replaceAll("<b>","");
		data=data.replaceAll("</b>","");
		data=data.replaceAll("&#10;","");
		data=data.replaceAll("<p>","");
		data=data.replaceAll("<br /><br />","<br />");
		while(data.indexOf("<br />")>0){
			String currentline=data.substring(0,data.indexOf("<br />"));
			
			String lyric="";
			if(gracenote){
				currentline=currentline.substring(1);
				gracenote=false;
			}
			String [] Temp_array=currentline.split(";");

			for (int i = 0; i < Temp_array.length; i++) { 
				lyric+=(char) Integer.parseInt(Temp_array[i]);
			}
			lyrics.add(lyric);
			data=data.substring(data.indexOf("<br />")+6,data.length());

		}
	
		return lyrics;

	}
	
	
	
}
