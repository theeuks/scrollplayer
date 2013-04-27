package hk.hku.cs.lrcstudio;

import android.annotation.TargetApi;
import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
	
	public enum Format {
		SUBRIP
	}
	
	/**
	 * Default constructor with an empty list of subtitles.
	 */
	public Lyrics() {
		startPositions = new ArrayList<Integer>();
		endPositions = new ArrayList<Integer>();
		subtitles = new ArrayList<String>();
	}
	
	/**
	 * Constructs the object using an external lyrics file.
	 * @param stream InputStream of the file.
	 * @param format A file format under <code>Lyrics.Format</code>.
	 * @throws IOException
	 */
	public Lyrics(InputStream stream, Lyrics.Format format) throws IOException {
		switch (format) {
		case SUBRIP:
			loadSubRip(stream);
			break;
		default:
			throw new IOException("File format not supported.");
		}
	}
	
	/**
	 * Appends a subtitle.
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
	 * Loads a SubRip file from an InputStream. Caller is responsible for closing the InputStream afterwards.
	 * @param stream InputStream to read from.
	 * @throws IOException
	 */
	private void loadSubRip(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		while (reader.readLine() != null) {
			// Ignore subtitle number.
			parseSubRipTime(reader.readLine());
			subtitles.add(reader.readLine());
			reader.readLine();	// Ignore blank line.
		}
		
		reader.close();
		
		if ((startPositions.size() != endPositions.size()) &&
		    (endPositions.size() != subtitles.size())) {
			throw new IOException("Invalid SubRip file.");
		}
	}
	
	/**
	 * Helper to convert a MediaPlayer playback position into SubRip's time format.
	 * @param milliseconds MediaPlayer playback position.
	 * @return SubRip time.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private String millisecondsToSubRipTimeFormat(long milliseconds) {
		return String.format(Locale.US, "%02d:%02d:%02d,%03d", 
		                     TimeUnit.MILLISECONDS.toHours(milliseconds),
		                     TimeUnit.MILLISECONDS.toMinutes(milliseconds),
		                     TimeUnit.MILLISECONDS.toSeconds(milliseconds),
		                     TimeUnit.MILLISECONDS.toMillis(milliseconds));
	}
	
	/**
	 * Parses SubRip's start time and end time.
	 * @param time The line which includes the start time and end time.
	 * @throws IOException
	 */
	private void parseSubRipTime(String time) throws IOException {
		String[] times = time.split(" --> ");
		if (times.length != 2) {
			throw new IOException("Start time or end time is missing.");
		}
		
		for (int i = 0; i < 1; ++i) {
			String[] timeComponents = times[i].split("[:,]");
			if (timeComponents.length != 4) {
				throw new IOException(String.format(Locale.US, "%s time does not have 4 fields.", i == 0 ? "Start" : "End"));
			}
			
			int milliseconds = subRipTimeToMilliseconds(timeComponents);
			if (i == 0) {
				startPositions.add(milliseconds);
			} else {
				endPositions.add(milliseconds);
			}
		}
	}
	
	/**
	 * Converts lyrics to the SubRip file format and writes it to an OutputStream.
	 * Caller is responsible for flushing and closing the OutputStream.
	 * @param stream OutputStream to write to.
	 * @throws IOException
	 */
	public void saveSubRip(OutputStream stream) throws IOException {
		stream.write(toSubRip().getBytes());
	}
	
	/**
	 * Converts SubRip's time to milliseconds.
	 * @param timeComponents An array of String in the format of: hours, minutes, seconds, milliseconds
	 * @return Milliseconds.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static int subRipTimeToMilliseconds(String[] timeComponents) {
		int milliseconds = 0;
		
		milliseconds += TimeUnit.MILLISECONDS.convert(Long.parseLong(timeComponents[0]), TimeUnit.HOURS);
		milliseconds += TimeUnit.MILLISECONDS.convert(Long.parseLong(timeComponents[1]), TimeUnit.MINUTES);
		milliseconds += TimeUnit.MILLISECONDS.convert(Long.parseLong(timeComponents[2]), TimeUnit.SECONDS);
		milliseconds += Long.parseLong(timeComponents[3]);
		
		return milliseconds;
	}
	
	/**
	 * Converts lyrics to the SubRip text file format.
	 * @return SubRip file text. <code>null</code> if there are no subtitles.
	 */
	private String toSubRip() {
		if (subtitles.size() == 0) {
			return null;
		}
		
		String subRip = "";
		for (int i = 1; i <= subtitles.size(); ++i) {
			String subtitleNumber = String.format(Locale.US, "%d", i);
			String startTime = millisecondsToSubRipTimeFormat(startPositions.get(i));
			String endTime = millisecondsToSubRipTimeFormat(endPositions.get(i));
			String subtitle = subtitles.get(i);
			
			subRip += String.format(Locale.US, "%s%n%s --> %s%n%s%n%n",
			                        subtitleNumber,
			                        startTime,
			                        endTime,
			                        subtitle);
		}
		
		return subRip;
	}
}
