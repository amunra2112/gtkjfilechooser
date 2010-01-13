/*******************************************************************************
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
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact me at c.cerbo@gmail.com if you need additional information or
 * have any questions.
 *******************************************************************************/
package eu.kostia.gtkjfilechooser.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

public class LowerBorder extends AbstractBorder {

	private static final long serialVersionUID = 1L;

	protected int thickness;
	protected Color lineColor;

	static final public Insets INSETS = new Insets(1,5,1,5);

	/**
	 * Creates a line border with the specified color and thickness.
	 * 
	 * @param color
	 *            the color of the border
	 * @param thickness
	 *            the thickness of the border
	 */
	public LowerBorder(Color color, int thickness) {
		this.lineColor = color;
		this.thickness = thickness;		
	}

	protected Insets getBorderInsets() {
		return INSETS;
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return getBorderInsets();
	}

	@Override
	public Insets getBorderInsets(Component c, Insets insets) {
		return getBorderInsets();
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Color oldColor = g.getColor();
		g.setColor(lineColor);
		for (int i = 0; i < thickness; i++) {
			g.drawLine(x + i - width, 
					height - y + i, 
					width - i - i - 1, 
					height - i - i - 1);

			g.setColor(oldColor);
		}
	}

}