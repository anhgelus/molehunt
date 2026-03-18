package world.anhgelus.molehunt.utils;

import org.jetbrains.annotations.NotNull;

public class TimeUtils {
	public static Time toTime(long time) {
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

	public record Time(long hours, long minutes, long seconds) {
		public @NotNull String toString() {
			return padLeft(hours) + ":" +
				padLeft(minutes) + ":" +
				padLeft(seconds);
		}
	}
}
