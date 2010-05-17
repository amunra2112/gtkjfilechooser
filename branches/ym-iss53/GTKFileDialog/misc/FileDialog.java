/*
 * Copyright 1995-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package java.awt;

import java.awt.peer.FileDialogPeer;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * The <code>FileDialog</code> class displays a dialog window
 * from which the user can select a file.
 * <p>
 * Since it is a modal dialog, when the application calls
 * its <code>show</code> method to display the dialog,
 * it blocks the rest of the application until the user has
 * chosen a file.
 *
 * @see Window#show
 *
 * @author      Sami Shaio
 * @author      Arthur van Hoff
 * @since       JDK1.0
 */
public class FileDialog extends Dialog {

    /**
     * This constant value indicates that the purpose of the file
     * dialog window is to locate a file from which to read.
     */
    public static final int LOAD = 0;

    /**
     * This constant value indicates that the purpose of the file
     * dialog window is to locate a file to which to write.
     */
    public static final int SAVE = 1;

    /*
     * There are two <code>FileDialog</code> modes: <code>LOAD</code> and
     * <code>SAVE</code>.
     * This integer will represent one or the other.
     * If the mode is not specified it will default to <code>LOAD</code>.
     *
     * @serial
     * @see getMode()
     * @see setMode()
     * @see java.awt.FileDialog#LOAD
     * @see java.awt.FileDialog#SAVE
     */
    int mode;

    /*
     * The string specifying the directory to display
     * in the file dialog.  This variable may be <code>null</code>.
     *
     * @serial
     * @see getDirectory()
     * @see setDirectory()
     */
    String dir;

    /*
     * The string specifying the initial value of the
     * filename text field in the file dialog.
     * This variable may be <code>null</code>.
     *
     * @serial
     * @see getFile()
     * @see setFile()
     */
    String file;

    /*
     * The filter used as the file dialog's filename filter.
     * The file dialog will only be displaying files whose
     * names are accepted by this filter.
     * This variable may be <code>null</code>.
     *
     * @serial
     * @see #getFilenameFilter()
     * @see #setFilenameFilter()
     * @see FileNameFilter
     */
    FilenameFilter filter;

    private static final String base = "filedlg";
    private static int nameCounter = 0;

    /*
     * JDK 1.1 serialVersionUID
     */
     private static final long serialVersionUID = 5035145889651310422L;


    static {
        /* ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * Initialize JNI field and method IDs for fields that may be
       accessed from C.
     */
    private static native void initIDs();

    /**
     * Creates a file dialog for loading a file.  The title of the
     * file dialog is initially empty.  This is a convenience method for
     * <code>FileDialog(parent, "", LOAD)</code>.
     *
     * @param parent the owner of the dialog
     * @since JDK1.1
     */
    public FileDialog(Frame parent) {
        this(parent, "", LOAD);
    }

    /**
     * Creates a file dialog window with the specified title for loading
     * a file. The files shown are those in the current directory.
     * This is a convenience method for
     * <code>FileDialog(parent, title, LOAD)</code>.
     *
     * @param     parent   the owner of the dialog
     * @param     title    the title of the dialog
     */
    public FileDialog(Frame parent, String title) {
        this(parent, title, LOAD);
    }

    /**
     * Creates a file dialog window with the specified title for loading
     * or saving a file.
     * <p>
     * If the value of <code>mode</code> is <code>LOAD</code>, then the
     * file dialog is finding a file to read, and the files shown are those
     * in the current directory.   If the value of
     * <code>mode</code> is <code>SAVE</code>, the file dialog is finding
     * a place to write a file.
     *
     * @param     parent   the owner of the dialog
     * @param     title   the title of the dialog
     * @param     mode   the mode of the dialog; either
     *          <code>FileDialog.LOAD</code> or <code>FileDialog.SAVE</code>
     * @exception  IllegalArgumentException if an illegal file
     *                 dialog mode is supplied
     * @see       java.awt.FileDialog#LOAD
     * @see       java.awt.FileDialog#SAVE
     */
    public FileDialog(Frame parent, String title, int mode) {
        super(parent, title, true);
        this.setMode(mode);
        setLayout(null);
    }

    /**
     * Creates a file dialog for loading a file.  The title of the
     * file dialog is initially empty.  This is a convenience method for
     * <code>FileDialog(parent, "", LOAD)</code>.
     *
     * @param     parent   the owner of the dialog
     * @exception java.lang.IllegalArgumentException if the <code>parent</code>'s
     *            <code>GraphicsConfiguration</code>
     *            is not from a screen device;
     * @exception java.lang.IllegalArgumentException if <code>parent</code>
     *            is <code>null</code>; this exception is always thrown when
     *            <code>GraphicsEnvironment.isHeadless</code>
     *            returns <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since 1.5
     */
    public FileDialog(Dialog parent) {
        this(parent, "", LOAD);
    }

    /**
     * Creates a file dialog window with the specified title for loading
     * a file. The files shown are those in the current directory.
     * This is a convenience method for
     * <code>FileDialog(parent, title, LOAD)</code>.
     *
     * @param     parent   the owner of the dialog
     * @param     title    the title of the dialog; a <code>null</code> value
     *                     will be accepted without causing a
     *                     <code>NullPointerException</code> to be thrown
     * @exception java.lang.IllegalArgumentException if the <code>parent</code>'s
     *            <code>GraphicsConfiguration</code>
     *            is not from a screen device;
     * @exception java.lang.IllegalArgumentException if <code>parent</code>
     *            is <code>null</code>; this exception is always thrown when
     *            <code>GraphicsEnvironment.isHeadless</code>
     *            returns <code>true</code>
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since     1.5
     */
    public FileDialog(Dialog parent, String title) {
        this(parent, title, LOAD);
    }

    /**
     * Creates a file dialog window with the specified title for loading
     * or saving a file.
     * <p>
     * If the value of <code>mode</code> is <code>LOAD</code>, then the
     * file dialog is finding a file to read, and the files shown are those
     * in the current directory.   If the value of
     * <code>mode</code> is <code>SAVE</code>, the file dialog is finding
     * a place to write a file.
     *
     * @param     parent   the owner of the dialog
     * @param     title    the title of the dialog; a <code>null</code> value
     *                     will be accepted without causing a
     *                     <code>NullPointerException</code> to be thrown
     * @param     mode     the mode of the dialog; either
     *                     <code>FileDialog.LOAD</code> or <code>FileDialog.SAVE</code>
     * @exception java.lang.IllegalArgumentException if an illegal
     *            file dialog mode is supplied;
     * @exception java.lang.IllegalArgumentException if the <code>parent</code>'s
     *            <code>GraphicsConfiguration</code>
     *            is not from a screen device;
     * @exception java.lang.IllegalArgumentException if <code>parent</code>
     *            is <code>null</code>; this exception is always thrown when
     *            <code>GraphicsEnvironment.isHeadless</code>
     *            returns <code>true</code>
     * @see       java.awt.GraphicsEnvironment#isHeadless
     * @see       java.awt.FileDialog#LOAD
     * @see       java.awt.FileDialog#SAVE
     * @since     1.5
     */
    public FileDialog(Dialog parent, String title, int mode) {
        super(parent, title, true);
        this.setMode(mode);
        setLayout(null);
    }

    /**
     * Constructs a name for this component. Called by <code>getName()</code>
     * when the name is <code>null</code>.
     */
    String constructComponentName() {
        synchronized (FileDialog.class) {
            return base + nameCounter++;
        }
    }

    /**
     * Creates the file dialog's peer.  The peer allows us to change the look
     * of the file dialog without changing its functionality.
     */
    public void addNotify() {
        synchronized(getTreeLock()) {
            if (parent != null && parent.getPeer() == null) {
                parent.addNotify();
            }
            if (peer == null)
                peer = getToolkit().createFileDialog(this);
            super.addNotify();
        }
    }

    /**
     * Indicates whether this file dialog box is for loading from a file
     * or for saving to a file.
     *
     * @return   the mode of this file dialog window, either
     *               <code>FileDialog.LOAD</code> or
     *               <code>FileDialog.SAVE</code>
     * @see      java.awt.FileDialog#LOAD
     * @see      java.awt.FileDialog#SAVE
     * @see      java.awt.FileDialog#setMode
     */
    public int getMode() {
        return mode;
    }

    /**
     * Sets the mode of the file dialog.  If <code>mode</code> is not
     * a legal value, an exception will be thrown and <code>mode</code>
     * will not be set.
     *
     * @param      mode  the mode for this file dialog, either
     *                 <code>FileDialog.LOAD</code> or
     *                 <code>FileDialog.SAVE</code>
     * @see        java.awt.FileDialog#LOAD
     * @see        java.awt.FileDialog#SAVE
     * @see        java.awt.FileDialog#getMode
     * @exception  IllegalArgumentException if an illegal file
     *                 dialog mode is supplied
     * @since      JDK1.1
     */
    public void setMode(int mode) {
        switch (mode) {
          case LOAD:
          case SAVE:
            this.mode = mode;
            break;
          default:
            throw new IllegalArgumentException("illegal file dialog mode");
        }
    }

    /**
     * Gets the directory of this file dialog.
     *
     * @return  the (potentially <code>null</code> or invalid)
     *          directory of this <code>FileDialog</code>
     * @see       java.awt.FileDialog#setDirectory
     */
    public String getDirectory() {
        return dir;
    }

    /**
     * Sets the directory of this file dialog window to be the
     * specified directory. Specifying a <code>null</code> or an
     * invalid directory implies an implementation-defined default.
     * This default will not be realized, however, until the user
     * has selected a file. Until this point, <code>getDirectory()</code>
     * will return the value passed into this method.
     * <p>
     * Specifying "" as the directory is exactly equivalent to
     * specifying <code>null</code> as the directory.
     *
     * @param     dir   the specified directory
     * @see       java.awt.FileDialog#getDirectory
     */
    public void setDirectory(String dir) {
        this.dir = (dir != null && dir.equals("")) ? null : dir;
        FileDialogPeer peer = (FileDialogPeer)this.peer;
        if (peer != null) {
            peer.setDirectory(this.dir);
        }
    }

    /**
     * Gets the selected file of this file dialog.  If the user
     * selected <code>CANCEL</code>, the returned file is <code>null</code>.
     *
     * @return    the currently selected file of this file dialog window,
     *                or <code>null</code> if none is selected
     * @see       java.awt.FileDialog#setFile
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the selected file for this file dialog window to be the
     * specified file. This file becomes the default file if it is set
     * before the file dialog window is first shown.
     * <p>
     * Specifying "" as the file is exactly equivalent to specifying
     * <code>null</code>
     * as the file.
     *
     * @param    file   the file being set
     * @see      java.awt.FileDialog#getFile
     */
    public void setFile(String file) {
        this.file = (file != null && file.equals("")) ? null : file;
        FileDialogPeer peer = (FileDialogPeer)this.peer;
        if (peer != null) {
            peer.setFile(this.file);
        }
    }

    /**
     * Determines this file dialog's filename filter. A filename filter
     * allows the user to specify which files appear in the file dialog
     * window.  Filename filters do not function in Sun's reference
     * implementation for Microsoft Windows.
     *
     * @return    this file dialog's filename filter
     * @see       java.io.FilenameFilter
     * @see       java.awt.FileDialog#setFilenameFilter
     */
    public FilenameFilter getFilenameFilter() {
        return filter;
    }

    /**
     * Sets the filename filter for this file dialog window to the
     * specified filter.
     * Filename filters do not function in Sun's reference
     * implementation for Microsoft Windows.
     *
     * @param   filter   the specified filter
     * @see     java.io.FilenameFilter
     * @see     java.awt.FileDialog#getFilenameFilter
     */
    public synchronized void setFilenameFilter(FilenameFilter filter) {
        this.filter = filter;
        FileDialogPeer peer = (FileDialogPeer)this.peer;
        if (peer != null) {
            peer.setFilenameFilter(filter);
        }
    }

    /**
     * Reads the <code>ObjectInputStream</code> and performs
     * a backwards compatibility check by converting
     * either a <code>dir</code> or a <code>file</code>
     * equal to an empty string to <code>null</code>.
     *
     * @param s the <code>ObjectInputStream</code> to read
     */
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        s.defaultReadObject();

        // 1.1 Compatibility: "" is not converted to null in 1.1
        if (dir != null && dir.equals("")) {
            dir = null;
        }
        if (file != null && file.equals("")) {
            file = null;
        }
    }

    /**
     * Returns a string representing the state of this <code>FileDialog</code>
     * window. This method is intended to be used only for debugging purposes,
     * and the content and format of the returned string may vary between
     * implementations. The returned string may be empty but may not be
     * <code>null</code>.
     *
     * @return  the parameter string of this file dialog window
     */
    protected String paramString() {
        String str = super.paramString();
        str += ",dir= " + dir;
        str += ",file= " + file;
        return str + ((mode == LOAD) ? ",load" : ",save");
    }

    boolean postsOldMouseEvents() {
        return false;
    }

    @Override
    public void setVisible(boolean b) {        
       if (peer == null) {
           peer = getToolkit().createFileDialog(this);
       }

       peer.setVisible(b);
        
       super.setVisible(b);
    }



}