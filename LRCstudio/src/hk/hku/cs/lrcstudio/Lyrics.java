package hk.hku.cs.lrcstudio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

/**
 * Stores a list of subtitles and their respective start and end position.
 * @author lamifong & kenthklui
 */
public class Lyrics {
	private List<LyricLine> lines;

	public enum Format {
		LRC,
		SUBRIP
	}

	/**
	 * Default constructor with an empty list of subtitles.
	 */
	public Lyrics() {
		lines = new ArrayList<LyricLine>();
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
	 * @param subtitle Text of the subtitle.
	 */
	public void addSubtitle(String text) {
		LyricLine newLine = new LyricLine(text);
		lines.add(newLine);
	}

	/**
	 * Appends a subtitle.
	 * @param startPosition MediaPlayer playback position for the beginning of the subtitle.
	 * @param endPosition MediaPlayer playback position for the end of the subtitle.
	 * @param subtitle Text of the subtitle.
	 */
	public void addSubtitle(String text, Integer startPosition) {
		LyricLine newLine = new LyricLine(text, startPosition);
		lines.add(newLine);
	}

	/**
	 * Appends a subtitle.
	 * @param startPosition MediaPlayer playback position for the beginning of the subtitle.
	 * @param endPosition MediaPlayer playback position for the end of the subtitle.
	 * @param subtitle Text of the subtitle.
	 */
	public void addSubtitle(String text, Integer startPosition, Integer endPosition) {
		LyricLine newLine = new LyricLine(text, startPosition, endPosition);
		lines.add(newLine);
	}
	
	/**
	 * Changes a subtitle at a given index to the passed line
	 * @param text the new lyric line
	 * @param Position the index of the list that needs to be changed
	 */
	public void changeSubtitle(LyricLine text, Integer Position){
		lines.set(Position,text);
	}

	/**
	 * Clear playback positions and subtitles.
	 */
	public void clear() {
		lines.clear();
	}

	/**
	 * Get subtitle for a playback position.
	 * @param position MediaPlayer playback position.
	 * @return Either a subtitle or <code>null</code> if no subtitles are available at that position.
	 */
	public LyricLine getSubtitle(Integer position) {
		return getSubtitle(position, 0);
	}

	public LyricLine getSubtitle(Integer position, int startLine) {
		// NOTE(kent): Lyric endPosition values are currently ignored due to LRC support

		LyricLine currentLine = null;
		for (int lineIndex = startLine; lineIndex < lines.size(); lineIndex++) {
			LyricLine line = lines.get(lineIndex);

			if (line.startPosition <= position) {
				currentLine = line;
			}
		}

		return currentLine;
	}
	
	public LyricLine getLyricLineDirect(int position){
		return lines.get(position);
	}
	
	public int getLyricsPositionInList(int time){
		
		for (int i = lines.size() - 1; i < lines.size(); i--){
			
			if (getLyricLineDirect(i).startPosition <= time) return i;
			
		}
		
		return -1;
		
	}
	
	public int getLyricsListSize (){
		
		return lines.size();
		
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
		case LRC:
			loadLRC(stream);
			break;
		case SUBRIP:
			loadSubRip(stream);
			break;
		default:
			throw new IOException("File format not supported.");
		}
	}

	/**
	 * Loads an LRC file from an InputStream. Caller is responsible for closing the InputStream afterwards.
	 * @param stream InputStream to read from.
	 * @throws IOException
	 */
	private void loadLRC(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		String line = null;
		while ((line = reader.readLine()) != null) {
			parseLRC(line.substring(0, 10), line.substring(10));
		}

		reader.close();
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
			Integer[] times = parseSubRipTime(reader.readLine());
			String text = reader.readLine();
			reader.readLine();	// Ignore blank line.

			addSubtitle(text, times[0], times[1]);
		}

		reader.close();
	}

	/**
	 * Converts LRC time to milliseconds.
	 * @param timeComponents An array of String in the format of: minutes, seconds, centiseconds
	 * @return Milliseconds.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private int lrcTimeToMilliseconds(String[] timeComponents) {
		int milliseconds = 0;
		milliseconds += TimeUnit.MILLISECONDS.convert(Long.parseLong(timeComponents[0]), TimeUnit.MINUTES);
		milliseconds += TimeUnit.MILLISECONDS.convert(Long.parseLong(timeComponents[1]), TimeUnit.SECONDS);
		milliseconds += Integer.parseInt(timeComponents[2]) * 10;

		return milliseconds;
	}

	/**
	 * Helper to convert a MediaPlayer playback position into LRC's time format.
	 * @param milliseconds MediaPlayer playback position.
	 * @return LRC time.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private String millisecondsToLRCTimeFormat(long milliseconds) {
		long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
		milliseconds -= TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
		milliseconds -= TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS);
		long centiseconds = milliseconds / 10;

		return String.format(Locale.US, "[%02d:%02d.%02d]", minutes, seconds, centiseconds);
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
	 * Parses and adds an LRC line.
	 * @param time Time in the format of [mm:ss.xx] where mm is minutes, ss is seconds and xx is centiseconds.
	 * @param subtitle The subtitle text.
	 * @throws IOException
	 */
	private void parseLRC(String timeString, String text) throws IOException {
		String[] timeComponents = timeString.replaceAll("[\\[\\]]", "").split("[:\\.]");
		if (timeComponents.length != 3) {
			throw new IOException(String.format(Locale.US, "Time does not have 3 fields."));
		}

		int startPosition = lrcTimeToMilliseconds(timeComponents);
		this.addSubtitle(text, startPosition);
	}

	/**
	 * Parses and returns SubRip start time and end time.
	 * @param time The line which includes the start time and end time.
	 * @throws IOException
	 */
	private Integer[] parseSubRipTime(String timeString) throws IOException {
		String[] times = timeString.split(" --> ");
		if (times.length != 2) {
			throw new IOException("Start time or end time is missing.");
		}

		Integer positions[] = new Integer[2];
		positions[0] = subRipTimeToMilliseconds(times[0]);
		positions[1] = subRipTimeToMilliseconds(times[1]);
		return positions;
	}

	/**
	 * Saves lyrics to a text file. Does nothing if there are no subtitles.
	 * @param stream Stream to write to.
	 * @param format A format under <code>Lyrics.Format</code>.
	 * @throws IOException
	 */
	public void save(OutputStream stream, Lyrics.Format format) throws IOException {
		if (lines.size() == 0) {
			return;
		}

		switch (format) {
		case LRC:
			saveLRC(stream);
			break;
		case SUBRIP:
			saveSubRip(stream);
			break;
		default:
			throw new IOException("File format not supported.");
		}
	}

	// TODO(kent): Consolidate saveLRC and saveSRT by migrating string
	//			   formatting to LyricLine class

	/**
	 * Converts lyrics to the LRC file format and writes it to an OutputStream.
	 * Caller is responsible for flushing and closing the OutputStream.
	 * @param stream OutputStream to write to.
	 * @throws IOException
	 */
	private void saveLRC(OutputStream stream) throws IOException {
		StringBuilder outputBuilder = new StringBuilder();

		for (int i = 0; i < lines.size(); ++i) {
			LyricLine line = lines.get(i);
			String lineString = String.format(Locale.US, "%s%s%n",
					millisecondsToLRCTimeFormat(line.startPosition),
					line.text);

			outputBuilder.append(lineString);
		}

		Log.i("output", outputBuilder.toString());
		stream.write(outputBuilder.toString().getBytes());
	}

	/**
	 * Converts lyrics to the SubRip file format and writes it to an OutputStream.
	 * Caller is responsible for flushing and closing the OutputStream.
	 * @param stream OutputStream to write to.
	 * @throws IOException
	 */
	private void saveSubRip(OutputStream stream) throws IOException {
		StringBuilder outputBuilder = new StringBuilder();
		for (int i = 0; i < lines.size(); ++i) {
			LyricLine line = lines.get(i);
			int lineNumber = i + 1;

			String lineString = String.format(Locale.US, "%d%n%s --> %s%n%s%n%n",
					lineNumber,
					millisecondsToSubRipTimeFormat(line.startPosition),
					millisecondsToSubRipTimeFormat(line.endPosition),
					line.text);

			outputBuilder.append(lineString);
		}

		stream.write(outputBuilder.toString().getBytes());
	}

	/**
	 * Converts SubRip time to milliseconds.
	 * @param timeComponents An array of String in the format of: hours, minutes, seconds, milliseconds
	 * @return Milliseconds.
	 * @throws IOException
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static int subRipTimeToMilliseconds(String subripTimeString) throws IOException {
		String[] timeStringComponents = subripTimeString.split("[:,]");
		if (timeStringComponents.length != 4) {
			throw new IOException(String.format(Locale.US, "SRT time string does not have 4 fields."));
		}

		int milliseconds = 0;
		milliseconds += TimeUnit.MILLISECONDS.convert(Long.parseLong(timeStringComponents[0]), TimeUnit.HOURS);
		milliseconds += TimeUnit.MILLISECONDS.convert(Long.parseLong(timeStringComponents[1]), TimeUnit.MINUTES);
		milliseconds += TimeUnit.MILLISECONDS.convert(Long.parseLong(timeStringComponents[2]), TimeUnit.SECONDS);
		milliseconds += Long.parseLong(timeStringComponents[3]);

		return milliseconds;
	}

	public void removeNullTimeLine(){
		
		int index = 0;
		while (index < lines.size() ){
			
			if (lines.get(index).startPosition == null) lines.remove(index);
			else index++;
			
		}
		
	}

}
