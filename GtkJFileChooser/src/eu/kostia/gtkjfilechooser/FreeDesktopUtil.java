package eu.kostia.gtkjfilechooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import eu.kostia.gtkjfilechooser.RemovableDevice.RemovableDeviceType;

/**
 * Gnome specific utilities
 * 
 * @author c.cerbo
 * 
 */
public class FreeDesktopUtil {
	/**
	 * Kappabyte: 1024 Bytes
	 */
	private static final int KB = 1024;

	/**
	 * Megabyte: 1024^2 Bytes
	 */
	private static final int MB = 1048576;

	/**
	 * Gigabyte: 1024^3 Bytes
	 */
	private static final int GB = 1073741824;

	public enum WellKnownDir {
		DESKTOP, DOWNLOAD, TEMPLATES, PUBLICSHARE, DOCUMENTS, MUSIC, PICTURES, VIDEOS
	}

	static private final String HUMAN_READABLE_FMT = "%.1f %s";

	/**
	 * Retrieve the path of "well known" user directories like the desktop
	 * folder and the music folder.
	 * 
	 * @param type
	 * @return
	 * 
	 * @see http://freedesktop.org/wiki/Software/xdg-user-dirs
	 */
	static public File getWellKnownDirPath(WellKnownDir type) {
		File userDirs = new File(System.getProperty("user.home")
				+ "/.config/user-dirs.dirs");
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(userDirs));
		} catch (IOException e) {
			throw new IOError(e);
		}

		String property = expandEnv(props.getProperty("XDG_" + type + "_DIR"));
		return new File(property);
	}

	/**
	 * Expand the environment variables contained in a string
	 * 
	 * @param str
	 *            The string to expand
	 * @return The expanded string
	 */
	static public String expandEnv(String str) {
		StringBuilder sb = new StringBuilder();

		int i = 0;
		while (i < str.length()) {
			char ch = str.charAt(i);
			if ('$' == ch) {
				// Expand variable
				ch = str.charAt(++i);
				int start = i;
				while (Character.isLetter(ch)) {
					ch = str.charAt(i);
					i++;
				}
				int end = i - 1;
				sb.append(System.getenv(str.substring(start, end)));
				i = end;
			} else if ('\\' == ch) {
				// handle escape chars
				ch = str.charAt(++i);
				sb.append(ch);
			} else if ('"' == ch) {
				// ignore quotes
				i++;
			} else {
				sb.append(ch);
				i++;
			}
		}

		return sb.toString();
	}

	/**
	 * Returns the list of the removable devices current mounted.
	 * 
	 * @return The list of the removable devices current mounted.
	 * 
	 * @see <a
	 *      href="http://www.pathname.com/fhs/pub/fhs-2.3.html#MEDIAMOUNTPOINT">Filesystem
	 *      Hierarchy Standard: /media : Mount point for removeable media</a>
	 */
	static public List<RemovableDevice> getRemovableDevices() {
		List<RemovableDevice> devices = new ArrayList<RemovableDevice>();
		String[] diskUUIDs = new File("/dev/disk/by-uuid/").list();
		Arrays.sort(diskUUIDs);

		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File("/proc/mounts"));
			while (fileScanner.hasNextLine()) {
				String line = fileScanner.nextLine();
				Scanner lineScanner = null;
				try {
					lineScanner = new Scanner(line);
					String dev = lineScanner.next();
					String location = lineScanner.next();
					if (location.startsWith("/media/")) {
						RemovableDevice device = new RemovableDevice();
						device.setLocation(location);
						device.setType(RemovableDeviceType.getType(dev));
						String name = location.substring("/media/".length());
						if (Arrays.binarySearch(diskUUIDs, name) >= 0) {
							// Removable device without name. 
							//Set a generic name with size
							name = humanreadble(new File(location).getTotalSpace())	+ " Filesystem";
							// TODO I18N ?
						}

						device.setName(name);

						// add to the results list
						devices.add(device);
					}
				} finally {
					if (lineScanner != null) {
						lineScanner.close();
					}
				}
			}
		} catch (IOException e) {
			throw new IOError(e);
		} finally {
			if (fileScanner != null) {
				fileScanner.close();
			}
		}

		return devices;
	}

	/**
	 * Make byte human readable
	 */
	private static String humanreadble(long bytes) {
		long roundedBytes = bytes;

		// round to 1/2 GB
		long mod = bytes % (GB / 2);
		if (mod != 0) {
			roundedBytes += (GB / 2 - mod);
		}

		if (roundedBytes >= GB) {
			return String.format(HUMAN_READABLE_FMT, (roundedBytes / (double) GB), "GB");
		} else if (roundedBytes >= MB) { // 1024^2
			return String.format(HUMAN_READABLE_FMT, (roundedBytes / (double) MB), "MB");
		} else if (roundedBytes >= KB) {
			return String.format(HUMAN_READABLE_FMT, (roundedBytes / (double) KB), "KB");
		} else {
			return roundedBytes + " Bytes";
		}
	}

	static public List<BasicPath> getBasicLocations() {
		List<BasicPath> basicLocations = new ArrayList<BasicPath>();

		basicLocations.add(BasicPath.HOME);
		basicLocations.add(BasicPath.DESKTOP);
		basicLocations.add(BasicPath.ROOT);

		return basicLocations;
	}
}