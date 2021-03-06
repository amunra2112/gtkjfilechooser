/*******************************************************************************
 * Copyright (c) 2010 Costantino Cerbo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Costantino Cerbo - initial API and implementation
 ******************************************************************************/
/*
 * Copyright 2010 Costantino Cerbo.  All Rights Reserved.
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
package com.google.code.gtkjfilechooser.filewatcher;

import java.io.File;

import com.google.code.gtkjfilechooser.filewatcher.FileEvent;
import com.google.code.gtkjfilechooser.filewatcher.FileListener;
import com.google.code.gtkjfilechooser.filewatcher.FileWatcher;

/**
 * @author Costantino Cerbo
 * 
 */
public class FileWatcherTest {
	public static void main(String[] args) throws Exception {
		FileWatcher.theFileWatcher().register(new File("/home/c.cerbo/temp/hello"));
		FileWatcher.theFileWatcher().register(new File("/home/c.cerbo/temp"));
		FileWatcher.theFileWatcher().register(new File("/media"));
		
		FileWatcher.theFileWatcher().addFileListener(new FileListener() {
			@Override
			public void fileChanged(FileEvent event) {
				System.out.println(event.getType() + ": " + event.getFile());
			}
		});
		
		FileWatcher.theFileWatcher().start();
		FileWatcher.theFileWatcher().stop();
		FileWatcher.theFileWatcher().start();
	}
}
