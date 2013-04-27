package hk.hku.cs.lrcstudio;

import android.annotation.TargetApi;
import android.os.Build;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Stores a list of subtitles and their respective start and end position.
 * @author lamifong
 */
public class Lyrics {
	private List<Integer> startPositions;
	private List<Integer> endPositions;
	private List<String> subtitles;
	
	/**
	 * Default constructor with an empty list of subtitles.
	 */
	public Lyrics() {
		startPositions = new ArrayList<Integer>();
		endPositions = new ArrayList<Integer>();
		subtitles = new ArrayList<String>();
	}
	
	/**
	 * Add a subtitle.
	 * @param startPosition MediaPlayer playback position for the beginning of the subtitle.
	 * @param endPosition MediaPlayer playback position for the end of the subtitle.
	 * @param subtitle Text of the subtitle.
	 */
	public void addSubtitle(Integer startPosition, Integer endPosition, String subtitle) {
		startPositions.add(startPosition);
		endPositions.add(endPosition);
		subtitles.add(subtitle);
	}
	
	/**
	 * Converts lyrics to the SubRip text file format.
	 * @return SubRip file text. <code>null</code> if there are no subtitles.
	 */
	public String toSubRip() {
		if (subtitles.size() == 0) {
			return null;
		}
		
		String subRip = "";
		for (int i = 1; i <= subtitles.size(); ++i) {
			String subtitleNumber = String.format(Locale.US, "%d", i);
			String startTime = toSubRipTimeFormat(startPositions.get(i));
			String endTime = toSubRipTimeFormat(endPositions.get(i));
			String subtitle = subtitles.get(i);
			
			subRip += String.format(Locale.US, "%s%n%s --> %s%n%s%n%n",
			                        subtitleNumber,
			                        startTime,
			                        endTime,
			                        subtitle);
		}
		
		return subRip;
	}
	
	/**
	 * Helper to convert a MediaPlayer playback position into SubRip's time format.
	 * @param duration MediaPlayer playback position.
	 * @return SubRip time.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private String toSubRipTimeFormat(long duration) {
		return String.format(Locale.US, "%02d:%02d:%02d,%03d", 
		                     TimeUnit.MILLISECONDS.toHours(duration),
		                     TimeUnit.MILLISECONDS.toMinutes(duration),
		                     TimeUnit.MILLISECONDS.toSeconds(duration),
		                     TimeUnit.MILLISECONDS.toMillis(duration));
	}
}
