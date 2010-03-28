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
package eu.kostia.gtkjfilechooser.ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;

public class GtkFileChooserUITestGui {

	public void showOpenDialog() throws Exception {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		int option = fileChooser.showOpenDialog(null);
		if (JFileChooser.APPROVE_OPTION == option){			
			for (File selected : fileChooser.getSelectedFiles()) {
				System.out.println("Selected file: " + selected);	
			}

		}
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(GTKLookAndFeel.class.getName());

		if ("GTK look and feel".equals(UIManager.getLookAndFeel().getName())){
			UIManager.put("FileChooserUI", eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI.class.getName());
		}
	
		GtkFileChooserUITestGui test = new GtkFileChooserUITestGui();
		test.showOpenDialog();	
	}
}

