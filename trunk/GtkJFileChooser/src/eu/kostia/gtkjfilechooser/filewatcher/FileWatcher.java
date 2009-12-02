/*
 * Copyright 2009 Costantino Cerbo.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact me at c.cerbo@gmail.com if you need additional information or
 * have any questions.
 */
package eu.kostia.gtkjfilechooser.filewatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import eu.kostia.gtkjfilechooser.filewatcher.FileEvent.FileEventType;

/**
 * <p>
 * FileWatcher
 * </p>
 * 
 * 
 * 
 * @author $Author:$
 * @version $Revision:$
 */
public class FileWatcher {

	private Map<File, Long> timeStamps;
	private List<File> filesToWatch;
	private List<FileListener> listeners;

	private TimerTask timerTask;
	private Timer timer;

	private FileWatcher() {
		init();
	}

	private static class SingletonHolder {
		private static final FileWatcher INSTANCE = new FileWatcher();
	}

	public static FileWatcher theFileWatcher() {
		return SingletonHolder.INSTANCE;
	}

	private void init() {
		this.filesToWatch = new ArrayList<File>();
		this.timeStamps = new HashMap<File, Long>();
		this.listeners = new ArrayList<FileListener>();
		this.timerTask = new TimerTask() {
			@Override
			public void run() {
				FileWatcher.this.watch();
			}
		};
	}

	/**
	 * Register the files to watch.
	 * 
	 * @param file
	 *            The file to watch
	 * @throws FileNotFoundException
	 */
	public void register(File file) throws FileNotFoundException {
		filesToWatch.add(file);
		timeStamps.put(file, file.lastModified());
	}

	/**
	 * Unregister files that we don't want to watch anymore.
	 * 
	 * @param file
	 *            The file to unregister
	 */
	public void unregister(File file) {
		filesToWatch.remove(file);
		timeStamps.remove(file);
	}

	private void notifyEvent(FileEvent evt) {
		for (FileListener l : listeners) {
			l.fileChanged(evt);
		}
	}

	public void addFileListener(FileListener l) {
		listeners.add(l);
	}

	public void removeFileListener(FileListener l) {
		listeners.remove(l);
	}

	public List<FileListener> getAllFileListeners() {
		return listeners;
	}

	private void watch() {
		for (File file : filesToWatch) {
			long currentTimeStamp = file.lastModified();
			long previousTimeStamp = timeStamps.get(file);

			if (file.exists()) {
				if (previousTimeStamp != currentTimeStamp) {
					timeStamps.put(file, currentTimeStamp);
					FileEvent evt = new FileEvent(this, file,
							previousTimeStamp == 0 ? FileEventType.CREATED
									: FileEventType.MODIFIED);
					notifyEvent(evt);
				}
			} else {
				if (previousTimeStamp != 0) {
					// File was deleted
					FileEvent evt = new FileEvent(this, file,
							FileEventType.DELETED);
					notifyEvent(evt);
					timeStamps.put(file, 0L);
				}
			}
		}
	}

	public void start() {
		if (timer != null) {
			stop();
		}

		timer = new Timer();
		// repeat the check every second
		timer.schedule(timerTask, new Date(), 1000);
	}

	/**
	 * This method may be called repeatedly; the second and subsequent calls
	 * have no effect.
	 */
	public void stop() {
		if (timer != null) {
			timer.cancel();
		}

	}
}
