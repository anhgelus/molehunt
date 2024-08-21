package world.anhgelus.molehunt.utils;

public class TimeUtils {

    private record Time(long hours, long minutes, long seconds) {}

    public static String printTime(long time) {
        final var pt = generateTime(time);

        StringBuilder sb = new StringBuilder();
        if (pt.hours != 0) {
            sb.append(pt.hours).append(" hours ");
        }
        if (pt.minutes != 0 || pt.hours != 0) {
            sb.append(pt.minutes).append(" minutes ");
        }
        sb.append(pt.seconds).append(" seconds");

        return sb.toString();
    }

    public static String printShortTime(long time) {
        final var pt = generateTime(time);

        return padLeft(pt.hours) + ":" +
                padLeft(pt.minutes) + ":" +
                padLeft(pt.seconds);
    }

    private static Time generateTime(long time) {
        long hours = 0;
        if (time > 3600) {
            hours = Math.floorDiv(time, 3600);
        }
        long minutes = 0;
        if (hours != 0 || time > 60) {
            minutes = Math.floorDiv(time - hours * 3600, 60);
        }
        long seconds = (long) Math.floor(time - hours * 3600 - minutes * 60);
        return new Time(hours, minutes, seconds);
    }

    private static String padLeft(long n) {
        if (n < 10 && n != 0) {
            return "0" + Math.round(n);
        } else if (n == 0) {
            return "00";
        }
        return Long.toString(Math.round(n));
    }
}
