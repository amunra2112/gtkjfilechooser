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
package eu.kostia.gtkjfilechooser.demo;

/*
 * @(#)FileChooserDemo.java	1.31 06/02/03
 * 
 * Copyright 2006, 2009006 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

/*
 * @(#)FileChooserDemo.java	1.31 06/02/03
 */

import static javax.swing.JFileChooser.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;

import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;

import eu.kostia.gtkjfilechooser.GtkFileChooserSettings;
import eu.kostia.gtkjfilechooser.ui.ImagePreviewer;

/**
 * 
 * A demo which makes extensive use of the file chooser.
 * 
 * 1.31 02/03/06
 * 
 * @author Jeff Dinkins
 */
public class FileChooserDemo extends JPanel implements ActionListener {
	static JFrame frame;

	JButton showButton;

	JCheckBox showAllFilesFilterCheckBox;
	JCheckBox showImageFilesFilterCheckBox;
	JCheckBox showFullDescriptionCheckBox;

	JCheckBox previewCheckBox;
	JCheckBox setHiddenCheckBox;
	JCheckBox useEmbedInWizardCheckBox;
	JCheckBox useControlsCheckBox;

	JRadioButton singleSelectionRadioButton;
	JRadioButton multiSelectionRadioButton;

	JRadioButton openRadioButton;
	JRadioButton saveRadioButton;
	JRadioButton customButton;

	JRadioButton justFilesRadioButton;
	JRadioButton justDirectoriesRadioButton;
	JRadioButton bothFilesAndDirectoriesRadioButton;

	JTextField customField;

	FileFilter jpgFilter, gifFilter, bothFilter;

	JPanel buttonPanel;

	public final static Dimension hpad10 = new Dimension(10, 1);
	public final static Dimension vpad20 = new Dimension(1, 20);
	public final static Dimension vpad7 = new Dimension(1, 7);
	public final static Dimension vpad4 = new Dimension(1, 4);
	public final static Insets insets = new Insets(5, 10, 0, 10);

	ImagePreviewer previewer;
	JFileChooser chooser;

	public FileChooserDemo() {

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		chooser = new JFileChooser();
		previewer = new ImagePreviewer(chooser);

		// create a radio listener to listen to option changes
		OptionListener optionListener = new OptionListener();

		// Create options
		openRadioButton = new JRadioButton("Open");
		openRadioButton.setSelected(true);
		openRadioButton.addActionListener(optionListener);

		saveRadioButton = new JRadioButton("Save");
		saveRadioButton.addActionListener(optionListener);

		customButton = new JRadioButton("Custom");
		customButton.addActionListener(optionListener);

		customField = new JTextField(8) {
			@Override
			public Dimension getMaximumSize() {
				return new Dimension(getPreferredSize().width, getPreferredSize().height);
			}
		};
		customField.setText("Doit");
		customField.setAlignmentY(JComponent.TOP_ALIGNMENT);
		customField.setEnabled(false);
		customField.addActionListener(optionListener);

		ButtonGroup group1 = new ButtonGroup();
		group1.add(openRadioButton);
		group1.add(saveRadioButton);
		group1.add(customButton);

		// filter buttons
		showAllFilesFilterCheckBox = new JCheckBox("Show \"All Files\" Filter");
		showAllFilesFilterCheckBox.addActionListener(optionListener);
		showAllFilesFilterCheckBox.setSelected(true);

		showImageFilesFilterCheckBox = new JCheckBox("Show JPG and GIF Filters");
		showImageFilesFilterCheckBox.addActionListener(optionListener);
		showImageFilesFilterCheckBox.setSelected(false);

		previewCheckBox = new JCheckBox("Show Preview");
		previewCheckBox.addActionListener(optionListener);
		previewCheckBox.setSelected(false);

		// more options
		setHiddenCheckBox = new JCheckBox("Show Hidden Files");
		setHiddenCheckBox.setSelected(GtkFileChooserSettings.get().getShowHidden());

		setHiddenCheckBox.addActionListener(optionListener);

		showFullDescriptionCheckBox = new JCheckBox("With File Extensions");
		showFullDescriptionCheckBox.addActionListener(optionListener);
		showFullDescriptionCheckBox.setSelected(true);
		showFullDescriptionCheckBox.setEnabled(false);

		useEmbedInWizardCheckBox = new JCheckBox("Embed in Wizard");
		useEmbedInWizardCheckBox.addActionListener(optionListener);
		useEmbedInWizardCheckBox.setSelected(false);

		useControlsCheckBox = new JCheckBox("Show Control Buttons");
		useControlsCheckBox.addActionListener(optionListener);
		useControlsCheckBox.setSelected(true);

		// File or Directory chooser options
		ButtonGroup group3 = new ButtonGroup();
		justFilesRadioButton = new JRadioButton("Just Select Files");
		justFilesRadioButton.setSelected(true);
		group3.add(justFilesRadioButton);
		justFilesRadioButton.addActionListener(optionListener);

		justDirectoriesRadioButton = new JRadioButton("Just Select Directories");
		group3.add(justDirectoriesRadioButton);
		justDirectoriesRadioButton.addActionListener(optionListener);

		bothFilesAndDirectoriesRadioButton = new JRadioButton(
		"Select Files or Directories");
		group3.add(bothFilesAndDirectoriesRadioButton);
		bothFilesAndDirectoriesRadioButton.addActionListener(optionListener);

		singleSelectionRadioButton = new JRadioButton("Single Selection", true);
		singleSelectionRadioButton.addActionListener(optionListener);

		multiSelectionRadioButton = new JRadioButton("Multi Selection");
		multiSelectionRadioButton.addActionListener(optionListener);

		ButtonGroup group4 = new ButtonGroup();
		group4.add(singleSelectionRadioButton);
		group4.add(multiSelectionRadioButton);

		// Create show button
		showButton = new JButton("Show FileChooser");
		showButton.addActionListener(this);
		showButton.setMnemonic('s');

		// ********************************************************
		// ******************** Dialog Type ***********************
		// ********************************************************
		JPanel control1 = new InsetPanel(insets);
		control1.setBorder(BorderFactory.createTitledBorder("Dialog Type"));

		control1.setLayout(new BoxLayout(control1, BoxLayout.Y_AXIS));
		control1.add(Box.createRigidArea(vpad20));
		control1.add(openRadioButton);
		control1.add(Box.createRigidArea(vpad7));
		control1.add(saveRadioButton);
		control1.add(Box.createRigidArea(vpad7));
		control1.add(customButton);
		control1.add(Box.createRigidArea(vpad4));
		JPanel fieldWrapper = new JPanel();
		fieldWrapper.setLayout(new BoxLayout(fieldWrapper, BoxLayout.X_AXIS));
		fieldWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldWrapper.add(Box.createRigidArea(hpad10));
		fieldWrapper.add(Box.createRigidArea(hpad10));
		fieldWrapper.add(customField);
		control1.add(fieldWrapper);
		control1.add(Box.createRigidArea(vpad20));
		control1.add(Box.createGlue());

		// ********************************************************
		// ***************** Filter Controls **********************
		// ********************************************************
		JPanel control2 = new InsetPanel(insets);
		control2.setBorder(BorderFactory.createTitledBorder("Filter Controls"));
		control2.setLayout(new BoxLayout(control2, BoxLayout.Y_AXIS));
		control2.add(Box.createRigidArea(vpad20));
		control2.add(showAllFilesFilterCheckBox);
		control2.add(Box.createRigidArea(vpad7));
		control2.add(showImageFilesFilterCheckBox);
		control2.add(Box.createRigidArea(vpad4));
		JPanel checkWrapper = new JPanel();
		checkWrapper.setLayout(new BoxLayout(checkWrapper, BoxLayout.X_AXIS));
		checkWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
		checkWrapper.add(Box.createRigidArea(hpad10));
		checkWrapper.add(Box.createRigidArea(hpad10));
		checkWrapper.add(showFullDescriptionCheckBox);
		control2.add(checkWrapper);
		control2.add(Box.createRigidArea(vpad20));
		control2.add(Box.createGlue());

		// ********************************************************
		// ****************** Display Options *********************
		// ********************************************************
		JPanel control3 = new InsetPanel(insets);
		control3.setBorder(BorderFactory.createTitledBorder("Display Options"));
		control3.setLayout(new BoxLayout(control3, BoxLayout.Y_AXIS));
		control3.add(Box.createRigidArea(vpad20));
		control3.add(setHiddenCheckBox);
		control3.add(Box.createRigidArea(vpad7));
		control3.add(previewCheckBox);
		control3.add(Box.createRigidArea(vpad7));
		control3.add(useEmbedInWizardCheckBox);
		control3.add(Box.createRigidArea(vpad7));
		control3.add(useControlsCheckBox);
		control3.add(Box.createGlue());

		// ********************************************************
		// ************* File & Directory Options *****************
		// ********************************************************
		JPanel control4 = new InsetPanel(insets);
		control4
		.setBorder(BorderFactory.createTitledBorder("File and Directory Options"));
		control4.setLayout(new BoxLayout(control4, BoxLayout.Y_AXIS));
		control4.add(Box.createRigidArea(vpad20));
		control4.add(justFilesRadioButton);
		control4.add(Box.createRigidArea(vpad7));
		control4.add(justDirectoriesRadioButton);
		control4.add(Box.createRigidArea(vpad7));
		control4.add(bothFilesAndDirectoriesRadioButton);
		control4.add(Box.createRigidArea(vpad20));
		control4.add(singleSelectionRadioButton);
		control4.add(Box.createRigidArea(vpad7));
		control4.add(multiSelectionRadioButton);
		control4.add(Box.createRigidArea(vpad20));
		control4.add(Box.createGlue());

		JPanel panel = new JPanel();
		panel.add(showButton);

		// ********************************************************
		// ****************** Wrap 'em all up *********************
		// ********************************************************
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));

		add(Box.createRigidArea(vpad20));

		wrapper.add(Box.createRigidArea(hpad10));
		wrapper.add(Box.createRigidArea(hpad10));
		wrapper.add(control1);
		wrapper.add(Box.createRigidArea(hpad10));
		wrapper.add(control2);
		wrapper.add(Box.createRigidArea(hpad10));
		wrapper.add(control3);
		wrapper.add(Box.createRigidArea(hpad10));
		wrapper.add(control4);
		wrapper.add(Box.createRigidArea(hpad10));
		wrapper.add(Box.createRigidArea(hpad10));

		add(wrapper);
		add(Box.createRigidArea(vpad20));
		add(panel);
		add(Box.createRigidArea(vpad20));
	}

	public void actionPerformed(ActionEvent e) {
		if (customButton.isSelected()) {
			chooser.setApproveButtonText(customField.getText());
		}
		if (chooser.isMultiSelectionEnabled()) {
			chooser.setSelectedFiles(null);
		} else {
			chooser.setSelectedFile(null);
		}
		// clear the preview from the previous display of the chooser
		JComponent accessory = chooser.getAccessory();
		if (accessory != null) {
			((ImagePreviewer) accessory).loadImage(null);
		}

		if (useEmbedInWizardCheckBox.isSelected()) {
			WizardDialog wizard = new WizardDialog(frame, true);
			wizard.setVisible(true);
			wizard.dispose();
			return;
		}

		int retval = chooser.showDialog(frame, null);
		if (retval == APPROVE_OPTION) {
			JOptionPane.showMessageDialog(frame, getResultString());
		} else if (retval == CANCEL_OPTION) {
			JOptionPane.showMessageDialog(frame,
			"User cancelled operation. No file was chosen.");
		} else if (retval == ERROR_OPTION) {
			JOptionPane.showMessageDialog(frame, "An error occured. No file was chosen.");
		} else {
			JOptionPane.showMessageDialog(frame, "Unknown operation occured.");
		}
	}

	private void resetFileFilters(boolean enableFilters,
			boolean showExtensionInDescription) {
		chooser.resetChoosableFileFilters();
		if (enableFilters) {
			jpgFilter = createFileFilter("JPEG Compressed Image Files",
					showExtensionInDescription, "jpg");
			gifFilter = createFileFilter("GIF Image Files", showExtensionInDescription,
			"gif");
			bothFilter = createFileFilter("JPEG and GIF Image Files",
					showExtensionInDescription, "jpg", "gif");
			chooser.addChoosableFileFilter(bothFilter);
			chooser.addChoosableFileFilter(jpgFilter);
			chooser.addChoosableFileFilter(gifFilter);
		}
	}

	private FileFilter createFileFilter(String description,
			boolean showExtensionInDescription, String... extensions) {
		if (showExtensionInDescription) {
			description = createFileNameFilterDescriptionFromExtensions(description,
					extensions);
		}
		return new FileNameExtensionFilter(description, extensions);
	}

	private String createFileNameFilterDescriptionFromExtensions(String description,
			String[] extensions) {
		String fullDescription = (description == null) ? "(" : description + " (";
		// build the description from the extension list
		fullDescription += "." + extensions[0];
		for (int i = 1; i < extensions.length; i++) {
			fullDescription += ", .";
			fullDescription += extensions[i];
		}
		fullDescription += ")";
		return fullDescription;
	}

	class WizardDialog extends JDialog implements ActionListener {
		CardLayout cardLayout;
		JPanel cardPanel;
		JLabel messageLabel;
		JButton backButton, nextButton, closeButton;

		WizardDialog(JFrame frame, boolean modal) {
			super(frame, "Embedded JFileChooser Demo", modal);

			cardLayout = new CardLayout();
			cardPanel = new JPanel(cardLayout);
			getContentPane().add(cardPanel, BorderLayout.CENTER);

			messageLabel = new JLabel("", JLabel.CENTER);
			cardPanel.add(chooser, "fileChooser");
			cardPanel.add(messageLabel, "label");
			cardLayout.show(cardPanel, "fileChooser");
			chooser.addActionListener(this);

			JPanel buttonPanel = new JPanel();
			backButton = new JButton("< Back");
			nextButton = new JButton("Next >");
			closeButton = new JButton("Close");

			buttonPanel.add(backButton);
			buttonPanel.add(nextButton);
			buttonPanel.add(closeButton);

			getContentPane().add(buttonPanel, BorderLayout.SOUTH);

			backButton.setEnabled(false);
			getRootPane().setDefaultButton(nextButton);

			backButton.addActionListener(this);
			nextButton.addActionListener(this);
			closeButton.addActionListener(this);

			pack();
			setLocationRelativeTo(frame);
		}

		public void actionPerformed(ActionEvent evt) {
			Object src = evt.getSource();
			String cmd = evt.getActionCommand();

			if (src == backButton) {
				back();
			} else if (src == nextButton) {
				FileChooserUI ui = chooser.getUI();
				if (ui instanceof BasicFileChooserUI) {
					// Workaround for bug 4528663. This is necessary to
					// pick up the contents of the file chooser text field.
					// This will trigger an APPROVE_SELECTION action.
					((BasicFileChooserUI) ui).getApproveSelectionAction()
					.actionPerformed(null);
				} else {
					next();
				}
			} else if (src == closeButton) {
				close();
			} else if (cmd == APPROVE_SELECTION) {
				next();
			} else if (cmd == CANCEL_SELECTION) {
				close();
			}
		}

		private void back() {
			backButton.setEnabled(false);
			nextButton.setEnabled(true);
			cardLayout.show(cardPanel, "fileChooser");
			getRootPane().setDefaultButton(nextButton);
			chooser.requestFocus();
		}

		private void next() {
			backButton.setEnabled(true);
			nextButton.setEnabled(false);
			messageLabel.setText(getResultString());
			cardLayout.show(cardPanel, "label");
			getRootPane().setDefaultButton(closeButton);
			closeButton.requestFocus();
		}

		private void close() {
			setVisible(false);
		}

		@Override
		public void dispose() {
			chooser.removeActionListener(this);

			// The chooser is hidden by CardLayout on remove
			// so fix it here
			cardPanel.remove(chooser);
			chooser.setVisible(true);

			super.dispose();
		}
	}

	private String getResultString() {
		String resultString = null;
		String filter = chooser.getFileFilter().getDescription();
		String path = null;
		boolean isDirMode = (chooser.getFileSelectionMode() == DIRECTORIES_ONLY);
		boolean isMulti = chooser.isMultiSelectionEnabled();

		if (isMulti) {
			File[] files = chooser.getSelectedFiles();
			if (files != null && files.length > 0) {
				path = "";
				for (int i = 0; i < files.length; i++) {
					path = path + "<br>" + files[i].getAbsolutePath();
				}
			}
		} else {
			File file = chooser.getSelectedFile();
			if (file != null) {
				path = "<br>" + file.getAbsolutePath();
			}
		}
		if (path != null) {
			path = path.replace(" ", "&nbsp;");
			filter = filter.replace(" ", "&nbsp;");
			resultString = "<html>You chose "
				+ (isMulti ? "these" : "this")
				+ " "
				+ (isDirMode ? (isMulti ? "directories" : "directory")
						: (isMulti ? "files" : "file")) + ": <code>" + path
						+ "</code><br><br>with filter: <br><code>" + filter;
		} else {
			resultString = "Nothing was chosen";
		}
		return resultString;
	}

	/** An ActionListener that listens to the radio buttons. */
	class OptionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComponent c = (JComponent) e.getSource();
			boolean selected = false;
			if (c instanceof JToggleButton) {
				selected = ((JToggleButton) c).isSelected();
			}

			if (c == openRadioButton) {
				chooser.setDialogType(OPEN_DIALOG);
				customField.setEnabled(false);
				repaint();
			} else if (c == useEmbedInWizardCheckBox) {
				useControlsCheckBox.setEnabled(!selected);
				useControlsCheckBox.setSelected(!selected);
				chooser.setControlButtonsAreShown(!selected);
			} else if (c == useControlsCheckBox) {
				chooser.setControlButtonsAreShown(selected);
			} else if (c == saveRadioButton) {
				chooser.setDialogType(SAVE_DIALOG);
				customField.setEnabled(false);
				repaint();
			} else if (c == customButton || c == customField) {
				customField.setEnabled(true);
				chooser.setDialogType(CUSTOM_DIALOG);
				repaint();
			} else if (c == showAllFilesFilterCheckBox) {
				chooser.setAcceptAllFileFilterUsed(selected);
			} else if (c == showImageFilesFilterCheckBox) {
				resetFileFilters(selected, showFullDescriptionCheckBox.isSelected());
				showFullDescriptionCheckBox.setEnabled(selected);
			} else if (c == setHiddenCheckBox) {
				chooser.setFileHidingEnabled(!selected);
			} else if (c == previewCheckBox) {
				if (selected) {
					chooser.setAccessory(previewer);
				} else {
					chooser.setAccessory(null);
				}
			} else if (c == showFullDescriptionCheckBox) {
				resetFileFilters(showImageFilesFilterCheckBox.isSelected(), selected);
			} else if (c == justFilesRadioButton) {
				chooser.setFileSelectionMode(FILES_ONLY);
			} else if (c == justDirectoriesRadioButton) {
				chooser.setFileSelectionMode(DIRECTORIES_ONLY);
			} else if (c == bothFilesAndDirectoriesRadioButton) {
				chooser.setFileSelectionMode(FILES_AND_DIRECTORIES);
			} else if (c == singleSelectionRadioButton) {
				if (selected) {
					chooser.setMultiSelectionEnabled(false);
				}
			} else if (c == multiSelectionRadioButton) {
				if (selected) {
					chooser.setMultiSelectionEnabled(true);
				}
			}
		}
	}

	class InsetPanel extends JPanel {
		Insets i;

		InsetPanel(Insets i) {
			this.i = i;
		}

		@Override
		public Insets getInsets() {
			return i;
		}
	}

	public static void main(String s[]) {
		try {
			UIManager.setLookAndFeel(GTKLookAndFeel.class.getName());
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(null, exc.getLocalizedMessage(),
					"Look and Feel error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if ("GTK look and feel".equals(UIManager.getLookAndFeel().getName())) {
			UIManager.put("FileChooserUI",
					eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI.class.getName());
		}

		FileChooserDemo panel = new FileChooserDemo();

		frame = new JFrame("FileChooserDemo");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.getContentPane().add("Center", panel);
		frame.pack();
		frame.setVisible(true);
	}
}
