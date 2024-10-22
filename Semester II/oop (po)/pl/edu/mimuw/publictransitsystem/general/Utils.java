package pl.edu.mimuw.publictransitsystem.general;

public class Utils {

    public static String capitalizeFirst(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String timestampToString(int timestamp) {
        int hour = timestamp / 60;
        int minute = timestamp % 60;
        StringBuilder sb = new StringBuilder();
        if(hour < 10) sb.append(0);
        sb.append(hour).append(":");
        if(minute < 10) sb.append(0);
        sb.append(minute);
        return sb.toString();
    }

    public static String timeToString(int day, int timestamp) {
        return day + ", " + timestampToString(timestamp);
    }
}
