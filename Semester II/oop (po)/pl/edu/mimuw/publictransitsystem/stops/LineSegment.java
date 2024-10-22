package pl.edu.mimuw.publictransitsystem.stops;

public class LineSegment {
    public enum TYPE {
        FIRST,
        LAST,
        NORMAL,
    }

    private final Stop stop;
    private final int length; // measured in minutes. If last, means time for which vehicle waits
    private final TYPE t;

    public LineSegment(Stop beginningStop, int length, TYPE t) {
        this.stop = beginningStop;
        this.length = length;
        this.t = t;
    }

    public Stop getStop() {
        return stop;
    }

    public int getLength() {
        return length;
    }

    public TYPE getType() {
        return t;
    }
}
