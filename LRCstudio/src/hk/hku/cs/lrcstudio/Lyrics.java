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
		load(stream, format);
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
	 * Clear playback positions and subtitles.
	 */
	public void clear() {
		startPositions.clear();
		endPositions.clear();
		subtitles.clear();
	}
	
	/**
	 * Get subtitle for a playback position.
	 * @param position MediaPlayer playback position.
	 * @return Either a subtitle or <code>null</code> if no subtitles are available at that position.
	 */
	public String getSubtitle(Integer position) {
		for (int i = 0; i < subtitles.size(); ++i) {
			if ((position >= startPositions.get(i)) &&
			    (position <= endPositions.get(i))) {
				return subtitles.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * Overwrites current data with data from an external lyrics file.
	 * @param stream InputStream of the file.
	 * @param format A file format under <code>Lyrics.Format</code>.
	 * @throws IOException
	 */
	public void load(InputStream stream, Lyrics.Format format) throws IOException {
		clear();
		switch (format) {
		case SUBRIP:
			loadSubRip(stream);
			break;
		default:
			throw new IOException("File format not supported.");
		}
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
		
		if ((startPositions.size() != endPositions.size()) ||
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
		long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
		milliseconds -= TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
		milliseconds -= TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
		milliseconds -= TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS);
		
		return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds);
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
		
		for (int i = 0; i < 2; ++i) {
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
		
		StringBuilder subRip = new StringBuilder();
		for (int i = 0; i < subtitles.size(); ++i) {
			subRip.append(String.format(Locale.US, "%s%n%s --> %s%n%s%n%n",
			                            String.format(Locale.US, "%d", i + 1),
			                            millisecondsToSubRipTimeFormat(startPositions.get(i)),
			                            millisecondsToSubRipTimeFormat(endPositions.get(i)),
			                            subtitles.get(i)));
		}
		
		return subRip.toString();
	}
}
