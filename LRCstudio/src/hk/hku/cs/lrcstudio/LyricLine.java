package hk.hku.cs.lrcstudio;

public class LyricLine {
	// TODO(kent): Encapsulate and remove public access
	public Integer startPosition;
	public Integer endPosition;
	public final String text;

	public LyricLine(String text) {
		this(text, null, null);
	}

	public LyricLine(String text, Integer startPosition) {
		this(text, startPosition, null);
	}

	public LyricLine(String text, Integer startPosition, Integer endPosition) {
		this.text = text;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}
}
