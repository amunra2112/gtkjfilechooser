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
package eu.kostia.gtkjfilechooser;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Costantino Cerbo
 * 
 */
public class GtkVersionTest {
	@Test
	public void testGetVersion() throws Exception {
		System.out.println("gtk 2.14.7: " + GtkVersion.check(2, 14, 7));
		System.out.println("gtk 2.18.0: " + GtkVersion.check(2, 18, 0));
		System.out.println("gtk 2.19.0: " + GtkVersion.check(2, 19, 0));
		Assert.assertNotNull(GtkVersion.check(2, 18, 0));
	}
}
