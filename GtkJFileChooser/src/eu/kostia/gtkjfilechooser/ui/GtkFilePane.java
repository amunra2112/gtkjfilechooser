package eu.kostia.gtkjfilechooser.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.basic.BasicDirectoryModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Position;

import sun.awt.shell.ShellFolder;
import sun.awt.shell.ShellFolderColumnInfo;
import eu.kostia.gtkjfilechooser.DateUtil;
import eu.kostia.gtkjfilechooser.FreeDesktopUtil;
import eu.kostia.gtkjfilechooser.GtkFileChooserSettings;
import eu.kostia.gtkjfilechooser.GtkStockIcon;
import eu.kostia.gtkjfilechooser.GtkFileChooserSettings.Column;
import eu.kostia.gtkjfilechooser.GtkStockIcon.Size;
import eu.kostia.gtkjfilechooser.ui.GtkFileChooserUI.MyGTKFileChooserUIAccessor;

@SuppressWarnings("unchecked")
public class GtkFilePane extends JPanel implements PropertyChangeListener {
	private static final long serialVersionUID = 10L;

	// Constants for actions. These are used for the actions' ACTION_COMMAND_KEY
	// and as keys in the action maps for FilePane and the corresponding UI
	// classes
	public final static String ACTION_APPROVE_SELECTION = "approveSelection";
	public final static String ACTION_CANCEL = "cancelSelection";
	public final static String ACTION_EDIT_FILE_NAME = "editFileName";
	public final static String ACTION_REFRESH = "refresh";
	public final static String ACTION_CHANGE_TO_PARENT_DIRECTORY = "Go Up";
	public final static String ACTION_NEW_FOLDER = "New Folder";
	public final static String ACTION_VIEW_LIST = "viewTypeList";
	public final static String ACTION_VIEW_DETAILS = "viewTypeDetails";

	public static final String FILE_NAME_HEADER = "FileChooser.fileNameHeaderText";
	public static final String FILE_SIZE_HEADER = "FileChooser.fileSizeHeaderText";
	private static final int FILE_SIZE_COLUMN_WIDTH = 100;
	public static final String FILE_DATE_HEADER = "FileChooser.fileDateHeaderText";
	private static final int FILE_DATE_COLUMN_WIDTH = 125;

	private Action[] actions;

	// "enums" for setViewType()
	public static final int VIEWTYPE_LIST = 0;
	public static final int VIEWTYPE_DETAILS = 1;
	private static final int VIEWTYPE_COUNT = 2;

	private int viewType = -1;
	private JPanel[] viewPanels = new JPanel[VIEWTYPE_COUNT];
	private JPanel currentViewPanel;
	private String[] viewTypeActionNames;

	private JMenu viewMenu;

	private String viewMenuLabelText;
	private String refreshActionLabelText;
	private String newFolderActionLabelText;

	private String renameErrorTitleText;
	private String renameErrorText;
	private String renameErrorFileExistsText;

	private static final Cursor waitCursor = Cursor
	.getPredefinedCursor(Cursor.WAIT_CURSOR);

	private final KeyListener detailsKeyListener = new KeyAdapter() {
		private final long timeFactor;

		private final StringBuilder typedString = new StringBuilder();

		private long lastTime = 1000L;

		{
			Long l = (Long) UIManager.get("Table.timeFactor");
			timeFactor = (l != null) ? l : 1000L;
		}

		/**
		 * Moves the keyboard focus to the first element whose prefix matches
		 * the sequence of alphanumeric keys pressed by the user with delay less
		 * than value of <code>timeFactor</code>. Subsequent same key presses
		 * move the keyboard focus to the next object that starts with the same
		 * letter until another key is pressed, then it is treated as the prefix
		 * with appropriate number of the same letters followed by first typed
		 * another letter.
		 */
		@Override
		public void keyTyped(KeyEvent e) {
			BasicDirectoryModel model = getModel();
			int rowCount = model.getSize();

			if (detailsTable == null || rowCount == 0 || e.isAltDown()
					|| e.isControlDown() || e.isMetaDown()) {
				return;
			}

			InputMap inputMap = detailsTable
			.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);

			if (inputMap != null && inputMap.get(key) != null) {
				return;
			}

			int startIndex = detailsTable.getSelectionModel().getLeadSelectionIndex();

			if (startIndex < 0) {
				startIndex = 0;
			}

			if (startIndex >= rowCount) {
				startIndex = rowCount - 1;
			}

			char c = e.getKeyChar();

			long time = e.getWhen();

			if (time - lastTime < timeFactor) {
				if (typedString.length() == 1 && typedString.charAt(0) == c) {
					// Subsequent same key presses move the keyboard focus to
					// the next
					// object that starts with the same letter.
					startIndex++;
				} else {
					typedString.append(c);
				}
			} else {
				startIndex++;

				typedString.setLength(0);
				typedString.append(c);
			}

			lastTime = time;

			if (startIndex >= rowCount) {
				startIndex = 0;
			}

			// Find next file
			int index = getNextMatch(startIndex, rowCount - 1);

			if (index < 0 && startIndex > 0) { // wrap
				index = getNextMatch(0, startIndex - 1);
			}

			if (index >= 0) {
				detailsTable.getSelectionModel().setSelectionInterval(index, index);

				Rectangle cellRect = detailsTable.getCellRect(index, detailsTable
						.convertColumnIndexToView(COLUMN_FILENAME), false);
				detailsTable.scrollRectToVisible(cellRect);
			}
		}

		private int getNextMatch(int startIndex, int finishIndex) {
			BasicDirectoryModel model = getModel();
			JFileChooser fileChooser = getFileChooser();
			GtkFilePaneRowSorter rowSorter = getRowSorter();

			String prefix = typedString.toString().toLowerCase();

			// Search element
			for (int index = startIndex; index <= finishIndex; index++) {
				File file = (File) model.getElementAt(rowSorter
						.convertRowIndexToModel(index));

				String fileName = fileChooser.getName(file).toLowerCase();

				if (fileName.startsWith(prefix)) {
					return index;
				}
			}

			return -1;
		}
	};

	private FocusListener editorFocusListener = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent e) {
			if (!e.isTemporary()) {
				applyEdit();
			}
		}
	};

	private static FocusListener repaintListener = new FocusListener() {
		public void focusGained(FocusEvent fe) {
			repaintSelection(fe.getSource());
		}

		public void focusLost(FocusEvent fe) {
			repaintSelection(fe.getSource());
		}

		private void repaintSelection(Object source) {
			if (source instanceof JList) {
				repaintListSelection((JList) source);
			} else if (source instanceof JTable) {
				repaintTableSelection((JTable) source);
			}
		}

		private void repaintListSelection(JList list) {
			int[] indices = list.getSelectedIndices();
			for (int i : indices) {
				Rectangle bounds = list.getCellBounds(i, i);
				list.repaint(bounds);
			}
		}

		private void repaintTableSelection(JTable table) {
			int minRow = table.getSelectionModel().getMinSelectionIndex();
			int maxRow = table.getSelectionModel().getMaxSelectionIndex();
			if (minRow == -1 || maxRow == -1) {
				return;
			}

			int col0 = table.convertColumnIndexToView(COLUMN_FILENAME);

			Rectangle first = table.getCellRect(minRow, col0, false);
			Rectangle last = table.getCellRect(maxRow, col0, false);
			Rectangle dirty = first.union(last);
			table.repaint(dirty);
		}
	};

	private boolean smallIconsView = false;
	private Border listViewBorder;
	private Color listViewBackground;
	private boolean listViewWindowsStyle;
	private boolean readOnly;

	private ListSelectionModel listSelectionModel;
	private JList filesList;
	private JTable detailsTable;

	private static final int COLUMN_FILENAME = 0;

	// Provides a way to recognize a newly created folder, so it can
	// be selected when it appears in the model.
	private File newFolderFile;

	// Used for accessing methods in the corresponding UI class
	private FileChooserUIAccessor fileChooserUIAccessor;
	private DetailsTableModel detailsTableModel;
	private GtkFilePaneRowSorter rowSorter;

	public GtkFilePane(FileChooserUIAccessor fileChooserUIAccessor) {
		super(new BorderLayout());

		this.fileChooserUIAccessor = fileChooserUIAccessor;

		installDefaults();
		createActionMap();
	}

	public void uninstallUI() {
		if (getModel() != null) {
			getModel().removePropertyChangeListener(this);
		}
	}

	public JTable getDetailsTable() {
		return detailsTable;
	}

	protected JFileChooser getFileChooser() {
		return getFileChooserUIAccessor().getFileChooser();
	}

	protected BasicDirectoryModel getModel() {
		return getFileChooserUIAccessor().getModel();
	}

	public int getViewType() {
		return viewType;
	}

	public void setViewType(int viewType) {
		int oldValue = this.viewType;
		if (viewType == oldValue) {
			return;
		}
		this.viewType = viewType;

		switch (viewType) {
		case VIEWTYPE_LIST:
			if (viewPanels[viewType] == null) {
				JPanel p = getFileChooserUIAccessor().createList();
				if (p == null) {
					p = createList();
				}
				setViewPanel(viewType, p);
			}
			getFilesList().setLayoutOrientation(JList.VERTICAL_WRAP);
			break;

		case VIEWTYPE_DETAILS:
			if (viewPanels[viewType] == null) {
				JPanel p = getFileChooserUIAccessor().createDetailsView();
				if (p == null) {
					p = createDetailsView();
				}
				setViewPanel(viewType, p);
			}
			break;
		}
		JPanel oldViewPanel = currentViewPanel;
		currentViewPanel = viewPanels[viewType];
		if (currentViewPanel != oldViewPanel) {
			if (oldViewPanel != null) {
				remove(oldViewPanel);
			}
			add(currentViewPanel, BorderLayout.CENTER);
			revalidate();
			repaint();
		}
		updateViewMenu();
		firePropertyChange("viewType", oldValue, viewType);
	}

	class ViewTypeAction extends AbstractAction {

		private static final long serialVersionUID = GtkFilePane.serialVersionUID;

		private int viewType;

		ViewTypeAction(int viewType) {
			super(viewTypeActionNames[viewType]);
			this.viewType = viewType;

			String cmd;
			switch (viewType) {
			case VIEWTYPE_LIST:
				cmd = ACTION_VIEW_LIST;
				break;
			case VIEWTYPE_DETAILS:
				cmd = ACTION_VIEW_DETAILS;
				break;
			default:
				cmd = (String) getValue(Action.NAME);
			}
			putValue(Action.ACTION_COMMAND_KEY, cmd);
		}

		public void actionPerformed(ActionEvent e) {
			setViewType(viewType);
		}
	}

	public Action getViewTypeAction(int viewType) {
		return new ViewTypeAction(viewType);
	}

	private static void recursivelySetInheritsPopupMenu(Container container, boolean b) {
		if (container instanceof JComponent) {
			((JComponent) container).setInheritsPopupMenu(b);
		}
		int n = container.getComponentCount();
		for (int i = 0; i < n; i++) {
			recursivelySetInheritsPopupMenu((Container) container.getComponent(i), b);
		}
	}

	public void setViewPanel(int viewType, JPanel viewPanel) {
		viewPanels[viewType] = viewPanel;
		recursivelySetInheritsPopupMenu(viewPanel, true);

		switch (viewType) {
		case VIEWTYPE_LIST:
			setFilesList((JList) findChildComponent(viewPanels[viewType], JList.class));
			if (getListSelectionModel() == null) {
				setListSelectionModel(getFilesList().getSelectionModel());
				if (detailsTable != null) {
					detailsTable.setSelectionModel(getListSelectionModel());
				}
			} else {
				getFilesList().setSelectionModel(getListSelectionModel());
			}
			break;

		case VIEWTYPE_DETAILS:
			detailsTable = (JTable) findChildComponent(viewPanels[viewType], JTable.class);
			detailsTable.setRowHeight(Math.max(detailsTable.getFont().getSize() + 4,
					16 + 1));
			if (getListSelectionModel() != null) {
				detailsTable.setSelectionModel(getListSelectionModel());
			}
			break;
		}
		if (this.viewType == viewType) {
			if (currentViewPanel != null) {
				remove(currentViewPanel);
			}
			currentViewPanel = viewPanel;
			add(currentViewPanel, BorderLayout.CENTER);
			revalidate();
			repaint();
		}
	}

	protected void installDefaults() {
		Locale l = getFileChooser().getLocale();

		listViewBorder = UIManager.getBorder("FileChooser.listViewBorder");
		listViewBackground = UIManager.getColor("FileChooser.listViewBackground");
		listViewWindowsStyle = UIManager.getBoolean("FileChooser.listViewWindowsStyle");
		readOnly = UIManager.getBoolean("FileChooser.readOnly");

		// TODO: On windows, get the following localized strings from the OS

		viewMenuLabelText = UIManager.getString("FileChooser.viewMenuLabelText", l);
		refreshActionLabelText = UIManager.getString(
				"FileChooser.refreshActionLabelText", l);
		newFolderActionLabelText = UIManager.getString(
				"FileChooser.newFolderActionLabelText", l);

		viewTypeActionNames = new String[VIEWTYPE_COUNT];
		viewTypeActionNames[VIEWTYPE_LIST] = UIManager.getString(
				"FileChooser.listViewActionLabelText", l);
		viewTypeActionNames[VIEWTYPE_DETAILS] = UIManager.getString(
				"FileChooser.detailsViewActionLabelText", l);

		renameErrorTitleText = UIManager.getString("FileChooser.renameErrorTitleText", l);
		renameErrorText = UIManager.getString("FileChooser.renameErrorText", l);
		renameErrorFileExistsText = UIManager.getString(
				"FileChooser.renameErrorFileExistsText", l);
	}

	/**
	 * Fetches the command filesList for the FilePane. These commands are useful
	 * for binding to events, such as in a keymap.
	 * 
	 * @return the command filesList
	 */
	public Action[] getActions() {
		if (actions == null) {
			class FilePaneAction extends AbstractAction {

				private static final long serialVersionUID = GtkFilePane.serialVersionUID;

				FilePaneAction(String name) {
					this(name, name);
				}

				FilePaneAction(String name, String cmd) {
					super(name);
					putValue(Action.ACTION_COMMAND_KEY, cmd);
				}

				public void actionPerformed(ActionEvent e) {
					String cmd = (String) getValue(Action.ACTION_COMMAND_KEY);

					if (cmd == ACTION_CANCEL) {
						if (getEditFile() != null) {
							cancelEdit();
						} else {
							getFileChooser().cancelSelection();
						}
					} else if (cmd == ACTION_EDIT_FILE_NAME) {
						JFileChooser fc = getFileChooser();
						int index = getListSelectionModel().getMinSelectionIndex();
						if (index >= 0
								&& getEditFile() == null
								&& (!fc.isMultiSelectionEnabled() || fc
										.getSelectedFiles().length <= 1)) {

							editFileName(index);
						}
					} else if (cmd == ACTION_REFRESH) {
						getFileChooser().rescanCurrentDirectory();
					}
				}

				@Override
				public boolean isEnabled() {
					String cmd = (String) getValue(Action.ACTION_COMMAND_KEY);
					if (cmd == ACTION_CANCEL) {
						return getFileChooser().isEnabled();
					} else if (cmd == ACTION_EDIT_FILE_NAME) {
						return !readOnly && getFileChooser().isEnabled();
					} else {
						return true;
					}
				}
			}

			ArrayList<Action> actionList = new ArrayList<Action>(8);
			Action action;

			actionList.add(new FilePaneAction(ACTION_CANCEL));
			actionList.add(new FilePaneAction(ACTION_EDIT_FILE_NAME));
			actionList.add(new FilePaneAction(refreshActionLabelText, ACTION_REFRESH));

			action = getFileChooserUIAccessor().getApproveSelectionAction();
			if (action != null) {
				actionList.add(action);
			}
			action = getFileChooserUIAccessor().getChangeToParentDirectoryAction();
			if (action != null) {
				actionList.add(action);
			}
			action = getNewFolderAction();
			if (action != null) {
				actionList.add(action);
			}
			action = getViewTypeAction(VIEWTYPE_LIST);
			if (action != null) {
				actionList.add(action);
			}
			action = getViewTypeAction(VIEWTYPE_DETAILS);
			if (action != null) {
				actionList.add(action);
			}
			actions = actionList.toArray(new Action[actionList.size()]);
		}

		return actions;
	}

	protected void createActionMap() {
		addActionsToMap(super.getActionMap(), getActions());
	}

	public static void addActionsToMap(ActionMap map, Action[] actions) {
		if (map != null && actions != null) {
			for (int i = 0; i < actions.length; i++) {
				Action a = actions[i];
				String cmd = (String) a.getValue(Action.ACTION_COMMAND_KEY);
				if (cmd == null) {
					cmd = (String) a.getValue(Action.NAME);
				}
				map.put(cmd, a);
			}
		}
	}

	private void updateListRowCount(JList list) {
		if (smallIconsView) {
			list.setVisibleRowCount(getModel().getSize() / 3);
		} else {
			list.setVisibleRowCount(-1);
		}
	}

	public JList createList(final JFileChooser fileChooser) {
		// --
		final JList list = new JList() {
			private static final long serialVersionUID = GtkFilePane.serialVersionUID;

			@Override
			public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
				ListModel model = getModel();
				int max = model.getSize();
				if (prefix == null || startIndex < 0 || startIndex >= max) {
					throw new IllegalArgumentException();
				}
				// start search from the next element before/after the selected
				// element
				boolean backwards = (bias == Position.Bias.Backward);
				for (int i = startIndex; backwards ? i >= 0 : i < max; i += (backwards ? -1
						: 1)) {
					String filename = fileChooser.getName((File) model.getElementAt(i));
					if (filename.regionMatches(true, 0, prefix, 0, prefix.length())) {
						return i;
					}
				}
				return -1;
			}
		};
		list.setCellRenderer(new FileRenderer());
		list.setLayoutOrientation(JList.VERTICAL_WRAP);

		// 4835633 : tell BasicListUI that this is a file filesList
		list.putClientProperty("List.isFileList", Boolean.TRUE);

		if (listViewWindowsStyle) {
			list.addFocusListener(repaintListener);
		}

		updateListRowCount(list);

		getModel().addListDataListener(new ListDataListener() {
			public void intervalAdded(ListDataEvent e) {
				updateListRowCount(list);
			}

			public void intervalRemoved(ListDataEvent e) {
				updateListRowCount(list);
			}

			public void contentsChanged(ListDataEvent e) {
				if (isShowing()) {
					clearSelection();
				}
				updateListRowCount(list);
			}
		});

		getModel().addPropertyChangeListener(this);

		if (fileChooser.isMultiSelectionEnabled()) {
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		list.setModel(new SortableListModel());

		list.addListSelectionListener(createListSelectionListener());
		list.addMouseListener(getMouseHandler());

		// --

		return list;
	}

	public JPanel createList() {
		JPanel p = new JPanel(new BorderLayout());
		final JFileChooser fileChooser = getFileChooser();

		JList list = createList(fileChooser);

		JScrollPane scrollpane = new JScrollPane(list);
		if (listViewBackground != null) {
			list.setBackground(listViewBackground);
		}
		if (listViewBorder != null) {
			scrollpane.setBorder(listViewBorder);
		}

		p.add(scrollpane, BorderLayout.CENTER);
		return p;
	}

	/**
	 * This model allows for sorting JList
	 */
	private class SortableListModel extends AbstractListModel implements
	TableModelListener, RowSorterListener {

		private static final long serialVersionUID = GtkFilePane.serialVersionUID;

		public SortableListModel() {
			getDetailsTableModel().addTableModelListener(this);
			getRowSorter().addRowSorterListener(this);
		}

		public int getSize() {
			return getModel().getSize();
		}

		public Object getElementAt(int index) {
			// JList doesn't support RowSorter so far,
			// so we put it into the filesList model
			return getModel().getElementAt(getRowSorter().convertRowIndexToModel(index));
		}

		public void tableChanged(TableModelEvent e) {
			fireContentsChanged(this, 0, getSize());
		}

		public void sorterChanged(RowSorterEvent e) {
			fireContentsChanged(this, 0, getSize());
		}
	}

	public DetailsTableModel getDetailsTableModel() {
		if (detailsTableModel == null) {
			detailsTableModel = new DetailsTableModel(getFileChooser());
		}
		return detailsTableModel;
	}

	class DetailsTableModel extends AbstractTableModel implements ListDataListener {



		private static final long serialVersionUID = GtkFilePane.serialVersionUID;

		private JFileChooser chooser;
		private BasicDirectoryModel directoryModel;

		private ShellFolderColumnInfo[] columns;
		private int[] columnMap;

		private DetailsTableModel(JFileChooser fc) {
			this.chooser = fc;
			directoryModel = getModel();
			directoryModel.addListDataListener(this);

			updateColumnInfo();
		}

		private void updateColumnInfo() {
			File dir = chooser.getCurrentDirectory();
			if (dir != null && getFileChooserUIAccessor().usesShellFolder()) {
				try {
					dir = ShellFolder.getShellFolder(dir);
				} catch (FileNotFoundException e) {
					// Leave dir without changing
				}
			}

			ShellFolderColumnInfo[] allColumns = ShellFolder.getFolderColumns(dir);

			ArrayList<ShellFolderColumnInfo> visibleColumns = new ArrayList<ShellFolderColumnInfo>();
			columnMap = new int[allColumns.length];
			for (int i = 0; i < allColumns.length; i++) {
				ShellFolderColumnInfo column = allColumns[i];

				if (FILE_SIZE_HEADER.equals(column.getTitle())) {
					column.setVisible(GtkFileChooserSettings.get().getShowSizeColumn());
				}

				if (FILE_SIZE_HEADER.equals(column.getTitle())) {
					column.setWidth(FILE_SIZE_COLUMN_WIDTH);
				}
				
				if (FILE_DATE_HEADER.equals(column.getTitle())) {
					column.setWidth(FILE_DATE_COLUMN_WIDTH);
				}

				if (column.isVisible()) {
					columnMap[visibleColumns.size()] = i;
					visibleColumns.add(column);
				}
			}

			columns = new ShellFolderColumnInfo[visibleColumns.size()];
			visibleColumns.toArray(columns);
			columnMap = Arrays.copyOf(columnMap, columns.length);

			List<RowSorter.SortKey> sortKeys = getSortKeys();
			fireTableStructureChanged();
			restoreSortKeys(sortKeys);
		}

		private List<RowSorter.SortKey> getSortKeys() {
			if (rowSorter == null) {
				return null;
			}

			if (rowSorter.getSortKeys().isEmpty()) {
				Column column = GtkFileChooserSettings.get().getSortColumn();
				if (column != null) {
					SortOrder sortOrder = GtkFileChooserSettings.get().getSortOrder();
					int columnIndex = column.ordinal();
					if (getColumnCount() == 2 && columnIndex == 2) {
						columnIndex = 1;
					}

					RowSorter.SortKey sortKey = new RowSorter.SortKey(columnIndex,
							sortOrder);
					List<RowSorter.SortKey> list = new ArrayList<RowSorter.SortKey>();
					list.add(sortKey);
					rowSorter.setSortKeys(list);
				}
			}

			return rowSorter.getSortKeys();
		}

		private void restoreSortKeys(List<RowSorter.SortKey> sortKeys) {
			if (sortKeys != null) {
				// check if preserved sortKeys are valid for this folder
				for (int i = 0; i < sortKeys.size(); i++) {
					RowSorter.SortKey sortKey = sortKeys.get(i);
					if (sortKey.getColumn() >= columns.length) {
						sortKeys = null;
						break;
					}
				}
				if (sortKeys != null) {
					rowSorter.setSortKeys(sortKeys);
				}
			}
		}

		public int getRowCount() {
			return directoryModel.getSize();
		}

		public int getColumnCount() {
			return columns.length;
		}

		public Object getValueAt(int row, int col) {
			// Note: It is very important to avoid getting info on drives, as
			// this will trigger "No disk in A:" and similar dialogs.
			//
			// Use (f.exists() &&
			// !chooser.getFileSystemView().isFileSystemRoot(f)) to
			// determine if it is safe to call methods directly on f.
			return getFileColumnValue((File) directoryModel.getElementAt(row), col);
		}

		public Object getFileColumnValue(File f, int col) {
			return (col == COLUMN_FILENAME) ? f // always return the file itself
					// for the 1st column
					: ShellFolder.getFolderColumnValue(f, columnMap[col]);
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (col == COLUMN_FILENAME) {
				JFileChooser chooser = getFileChooser();
				File f = (File) getValueAt(row, col);
				if (f != null) {
					String oldDisplayName = chooser.getName(f);
					String oldFileName = f.getName();
					String newDisplayName = ((String) value).trim();
					String newFileName;

					if (!newDisplayName.equals(oldDisplayName)) {
						newFileName = newDisplayName;
						// Check if extension is hidden from user
						int i1 = oldFileName.length();
						int i2 = oldDisplayName.length();
						if (i1 > i2 && oldFileName.charAt(i2) == '.') {
							newFileName = newDisplayName + oldFileName.substring(i2);
						}

						// rename
						FileSystemView fsv = chooser.getFileSystemView();
						File f2 = fsv.createFileObject(f.getParentFile(), newFileName);
						if (f2.exists()) {
							JOptionPane.showMessageDialog(chooser, MessageFormat.format(
									renameErrorFileExistsText, oldFileName),
									renameErrorTitleText, JOptionPane.ERROR_MESSAGE);
						} else {
							if (GtkFilePane.this.getModel().renameFile(f, f2)) {
								if (fsv.isParent(chooser.getCurrentDirectory(), f2)) {
									if (chooser.isMultiSelectionEnabled()) {
										chooser.setSelectedFiles(new File[] { f2 });
									} else {
										chooser.setSelectedFile(f2);
									}
								} else {
									// Could be because of delay in updating
									// Desktop folder
									// chooser.setSelectedFile(null);
								}
							} else {
								JOptionPane.showMessageDialog(chooser, MessageFormat
										.format(renameErrorText, oldFileName),
										renameErrorTitleText, JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			File currentDirectory = getFileChooser().getCurrentDirectory();
			return (!readOnly && column == COLUMN_FILENAME && canWrite(currentDirectory));
		}

		public void contentsChanged(ListDataEvent e) {
			// Update the selection after the model has been updated
			new DelayedSelectionUpdater();
			fireTableDataChanged();
		}

		public void intervalAdded(ListDataEvent e) {
			int i0 = e.getIndex0();
			int i1 = e.getIndex1();
			if (i0 == i1) {
				File file = (File) getModel().getElementAt(i0);
				if (file.equals(newFolderFile)) {
					new DelayedSelectionUpdater(file);
					newFolderFile = null;
				}
			}

			fireTableRowsInserted(e.getIndex0(), e.getIndex1());
		}

		public void intervalRemoved(ListDataEvent e) {
			fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
		}

		public ShellFolderColumnInfo[] getColumns() {
			return columns;
		}
	}

	private void updateDetailsColumnModel(JTable table) {
		if (table != null) {
			ShellFolderColumnInfo[] columns = detailsTableModel.getColumns();

			TableColumnModel columnModel = new DefaultTableColumnModel();
			for (int i = 0; i < columns.length; i++) {
				ShellFolderColumnInfo dataItem = columns[i];
				TableColumn column = new TableColumn(i);

				String title = dataItem.getTitle();
				if (title != null && title.startsWith("FileChooser.")
						&& title.endsWith("HeaderText")) {
					// the column must have a string resource that we try to get
					String uiTitle = UIManager.getString(title, table.getLocale());
					if (uiTitle != null) {
						title = uiTitle;
					}
				}
				column.setHeaderValue(title);

				Integer width = dataItem.getWidth();
				if (width != null) {
					column.setPreferredWidth(width);

					if (!FILE_NAME_HEADER.equals(dataItem.getTitle())) {
						// Size and Modified columns have a fix width
						column.setMinWidth(width);
						column.setMaxWidth(width);
					}
				}

				columnModel.addColumn(column);
			}

			// Install cell editor for editing file name
			if (!readOnly && columnModel.getColumnCount() > COLUMN_FILENAME) {
				columnModel.getColumn(COLUMN_FILENAME).setCellEditor(
						getDetailsTableCellEditor());
			}

			table.setColumnModel(columnModel);
		}
	}

	private GtkFilePaneRowSorter getRowSorter() {
		if (rowSorter == null) {
			rowSorter = new GtkFilePaneRowSorter(this);
		}
		return rowSorter;
	}

	private DetailsTableCellEditor tableCellEditor;

	private DetailsTableCellEditor getDetailsTableCellEditor() {
		if (tableCellEditor == null) {
			tableCellEditor = new DetailsTableCellEditor(new JTextField());
		}
		return tableCellEditor;
	}

	private class DetailsTableCellEditor extends DefaultCellEditor {

		private static final long serialVersionUID = GtkFilePane.serialVersionUID;

		private final JTextField tf;

		public DetailsTableCellEditor(JTextField tf) {
			super(tf);
			this.tf = tf;
			tf.addFocusListener(editorFocusListener);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			Component comp = super.getTableCellEditorComponent(table, value, isSelected,
					row, column);
			if (value instanceof File) {
				tf.setText(getFileChooser().getName((File) value));
				tf.selectAll();
			}
			return comp;
		}
	}

	class DetailsTableCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = GtkFilePane.serialVersionUID;

		private JFileChooser chooser;

		private DetailsTableCellRenderer(JFileChooser chooser) {
			this.chooser = chooser;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);

			setIcon(null);

			// formatting cell text
			// TODO: it's rather a temporary trick, to be revised
			String text;

			if (value == null) {
				text = "";
			} else if (value instanceof File) {
				File file = (File) value;
				text = chooser.getName(file);
				
				Icon thumb = GtkStockIcon.get(file, Size.GTK_ICON_SIZE_MENU);
				setIcon(thumb != null ? thumb : chooser.getIcon(file));

			} else if (value instanceof Date) {
				// modified date
				text = DateUtil.toPrettyFormat((Date) value);
			} else if (value instanceof Long) {
				// size
				text = FreeDesktopUtil.humanreadble((Long) value, 0);
			} else {
				text = value.toString();
			}

			setText(text);

			if (isSelected) {
				setForeground(UIManager.getColor("List.selectionForeground"));
				setBackground(UIManager.getColor("List.selectionBackground"));
			} else {
				setForeground(UIManager.getColor("List.foreground"));
				Color rowcolor = (row % 2 == 0) ? new Color(238, 238, 238) : UIManager
						.getColor("TextPane.background");
				setBackground(rowcolor);
			}

			return this;
		}
	}

	public JPanel createDetailsView() {
		final JFileChooser chooser = getFileChooser();

		JPanel p = new JPanel(new BorderLayout());

		detailsTable = new JTable(getDetailsTableModel()) {

			private static final long serialVersionUID = GtkFilePane.serialVersionUID;

			// Handle Escape key events here
			@Override
			protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition,
					boolean pressed) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE && getCellEditor() == null) {
					// We are not editing, forward to filechooser.
					chooser.dispatchEvent(e);
					return true;
				}
				return super.processKeyBinding(ks, e, condition, pressed);
			}

			@Override
			public void tableChanged(TableModelEvent e) {
				super.tableChanged(e);

				if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
					// update header with possibly changed column set
					updateDetailsColumnModel(this);
				}
			}

			@Override
			public int getRowHeight() {
				// gnome rows are a taller
				return 22;
			}
		};

		detailsTable.setRowSorter(getRowSorter());
		detailsTable.getTableHeader().setReorderingAllowed(false);
		detailsTable.getTableHeader().setResizingAllowed(false);
		detailsTable.setAutoCreateColumnsFromModel(false);
		detailsTable.setComponentOrientation(chooser.getComponentOrientation());
		detailsTable.setShowGrid(false);
		detailsTable.setRowSelectionAllowed(true);
		detailsTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
		detailsTable.addKeyListener(detailsKeyListener);

		if (getFilesList() == null) {
			// The Details view works only after that the filesList view was
			// initialized
			setViewType(VIEWTYPE_LIST);
			setFilesList((JList) findChildComponent(viewPanels[viewType], JList.class));
		}

		Font font = getFilesList().getFont();
		detailsTable.setFont(font);

		// TableCellRenderer headerRenderer = new
		// AlignableTableHeaderRenderer(detailsTable.getTableHeader().getDefaultRenderer());
		// detailsTable.getTableHeader().setDefaultRenderer(headerRenderer);
		TableCellRenderer cellRenderer = new DetailsTableCellRenderer(chooser);
		detailsTable.setDefaultRenderer(Object.class, cellRenderer);

		// So that drag can be started on a mouse press
		detailsTable.getColumnModel().getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);

		detailsTable.addMouseListener(getMouseHandler());
		// No need to addListSelectionListener because selections are forwarded
		// to our JList.

		// 4835633 : tell BasicTableUI that this is a file filesList
		detailsTable.putClientProperty("Table.isFileList", Boolean.TRUE);

		if (listViewWindowsStyle) {
			detailsTable.addFocusListener(repaintListener);
		}

		// TAB/SHIFT-TAB should transfer focus and ENTER should select an item.
		// We don't want them to navigate within the table
		ActionMap am = SwingUtilities.getUIActionMap(detailsTable);
		am.remove("selectNextRowCell");
		am.remove("selectPreviousRowCell");
		am.remove("selectNextColumnCell");
		am.remove("selectPreviousColumnCell");
		detailsTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				null);
		detailsTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
				null);

		final JScrollPane scrollpane = new JScrollPane(detailsTable);
		scrollpane.setComponentOrientation(chooser.getComponentOrientation());

		scrollpane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				fixNameColumnWidth(scrollpane.getViewport().getSize().width);
				scrollpane.removeComponentListener(this);
			}
		});

		// 4835633.
		// If the mouse is pressed in the area below the Details view table, the
		// event is not dispatched to the Table MouseListener but to the
		// scrollpane. Listen for that here so we can clear the selection.
		scrollpane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				JScrollPane jsp = ((JScrollPane) e.getComponent());
				JTable table = (JTable) jsp.getViewport().getView();

				if (!e.isShiftDown()
						|| table.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
					clearSelection();
					TableCellEditor tce = table.getCellEditor();
					if (tce != null) {
						tce.stopCellEditing();
					}
				}
			}
		});

		if (listViewBorder != null) {
			scrollpane.setBorder(listViewBorder);
		}
		p.add(scrollpane, BorderLayout.CENTER);

		detailsTableModel.fireTableStructureChanged();

		return p;
	} // createDetailsView

	/**
	 * Adjust width of first column so the table fills the viewport whenfirst
	 * displayed (temporary listener).
	 */
	private void fixNameColumnWidth(int viewWidth) {
		TableColumn nameCol = detailsTable.getColumnModel().getColumn(COLUMN_FILENAME);
		int tableWidth = detailsTable.getPreferredSize().width;

		if (tableWidth < viewWidth) {
			nameCol.setPreferredWidth(nameCol.getPreferredWidth() + viewWidth
					- tableWidth);
		}
	}

	private class DelayedSelectionUpdater implements Runnable {
		File editFile;

		DelayedSelectionUpdater() {
			this(null);
		}

		DelayedSelectionUpdater(File editFile) {
			this.editFile = editFile;
			if (isShowing()) {
				SwingUtilities.invokeLater(this);
			}
		}

		public void run() {
			setFileSelected();
			if (editFile != null) {
				editFileName(getRowSorter().convertRowIndexToView(
						getModel().indexOf(editFile)));
				editFile = null;
			}
		}
	}

	/**
	 * Creates a selection listener for the filesList of files and directories.
	 * 
	 * @return a <code>ListSelectionListener</code>
	 */
	public ListSelectionListener createListSelectionListener() {
		return getFileChooserUIAccessor().createListSelectionListener();
	}

	int lastIndex = -1;
	private File editFile = null;

	void resetEditIndex() {
		lastIndex = -1;
	}

	private void cancelEdit() {
		if (getEditFile() != null) {
			setEditFile(null);
			getFilesList().remove(editCell);
			repaint();
		} else if (detailsTable != null && detailsTable.isEditing()) {
			detailsTable.getCellEditor().cancelCellEditing();
		}
	}

	JTextField editCell = null;

	/**
	 * @param index
	 *            visual index of the file to be edited
	 */
	private void editFileName(int index) {
		JFileChooser chooser = getFileChooser();
		File currentDirectory = chooser.getCurrentDirectory();

		if (readOnly || !canWrite(currentDirectory)) {
			return;
		}

		ensureIndexIsVisible(index);
		switch (viewType) {
		case VIEWTYPE_LIST:
			setEditFile((File) getModel().getElementAt(
					getRowSorter().convertRowIndexToModel(index)));
			Rectangle r = getFilesList().getCellBounds(index, index);
			if (editCell == null) {
				editCell = new JTextField();
				editCell.addActionListener(new EditActionListener());
				editCell.addFocusListener(editorFocusListener);
			}
			getFilesList().add(editCell);
			editCell.setText(chooser.getName(getEditFile()));
			ComponentOrientation orientation = getFilesList().getComponentOrientation();
			editCell.setComponentOrientation(orientation);

			Icon icon = chooser.getIcon(getEditFile());

			// PENDING - grab padding (4) below from defaults table.
			int editX = icon == null ? 20 : icon.getIconWidth() + 4;

			if (orientation.isLeftToRight()) {
				editCell.setBounds(editX + r.x, r.y, r.width - editX, r.height);
			} else {
				editCell.setBounds(r.x, r.y, r.width - editX, r.height);
			}
			editCell.requestFocus();
			editCell.selectAll();
			break;

		case VIEWTYPE_DETAILS:
			detailsTable.editCellAt(index, COLUMN_FILENAME);
			break;
		}
	}

	class EditActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			applyEdit();
		}
	}

	private void applyEdit() {
		if (getEditFile() != null && getEditFile().exists()) {
			JFileChooser chooser = getFileChooser();
			String oldDisplayName = chooser.getName(getEditFile());
			String oldFileName = getEditFile().getName();
			String newDisplayName = editCell.getText().trim();
			String newFileName;

			if (!newDisplayName.equals(oldDisplayName)) {
				newFileName = newDisplayName;
				// Check if extension is hidden from user
				int i1 = oldFileName.length();
				int i2 = oldDisplayName.length();
				if (i1 > i2 && oldFileName.charAt(i2) == '.') {
					newFileName = newDisplayName + oldFileName.substring(i2);
				}

				// rename
				FileSystemView fsv = chooser.getFileSystemView();
				File f2 = fsv
				.createFileObject(getEditFile().getParentFile(), newFileName);
				if (f2.exists()) {
					JOptionPane.showMessageDialog(chooser, MessageFormat.format(
							renameErrorFileExistsText, oldFileName),
							renameErrorTitleText, JOptionPane.ERROR_MESSAGE);
				} else {
					if (getModel().renameFile(getEditFile(), f2)) {
						if (fsv.isParent(chooser.getCurrentDirectory(), f2)) {
							if (chooser.isMultiSelectionEnabled()) {
								chooser.setSelectedFiles(new File[] { f2 });
							} else {
								chooser.setSelectedFile(f2);
							}
						} else {
							// Could be because of delay in updating Desktop
							// folder
							// chooser.setSelectedFile(null);
						}
					} else {
						JOptionPane.showMessageDialog(chooser, MessageFormat.format(
								renameErrorText, oldFileName), renameErrorTitleText,
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		if (detailsTable != null && detailsTable.isEditing()) {
			detailsTable.getCellEditor().stopCellEditing();
		}
		cancelEdit();
	}

	protected Action newFolderAction;

	public Action getNewFolderAction() {
		if (!readOnly && newFolderAction == null) {
			newFolderAction = new AbstractAction(newFolderActionLabelText) {

				private static final long serialVersionUID = GtkFilePane.serialVersionUID;

				private Action basicNewFolderAction;

				// Initializer
				{
					putValue(Action.ACTION_COMMAND_KEY, GtkFilePane.ACTION_NEW_FOLDER);

					File currentDirectory = getFileChooser().getCurrentDirectory();
					if (currentDirectory != null) {
						setEnabled(canWrite(currentDirectory));
					}
				}

				public void actionPerformed(ActionEvent ev) {
					if (basicNewFolderAction == null) {
						basicNewFolderAction = getFileChooserUIAccessor()
						.getNewFolderAction();
					}
					JFileChooser fc = getFileChooser();
					File oldFile = fc.getSelectedFile();
					basicNewFolderAction.actionPerformed(ev);
					File newFile = fc.getSelectedFile();
					if (newFile != null && !newFile.equals(oldFile)
							&& newFile.isDirectory()) {
						newFolderFile = newFile;
					}
				}
			};
		}
		return newFolderAction;
	}

	protected class FileRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = GtkFilePane.serialVersionUID;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (listViewWindowsStyle && !list.isFocusOwner()) {
				isSelected = false;
			}

			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			File file = (File) value;
			String fileName = getFileChooser().getName(file);
			setText(fileName);
			setFont(list.getFont());

			Icon icon = getFileChooser().getIcon(file);
			if (icon != null) {
				setIcon(icon);
			} else {
				if (getFileChooser().getFileSystemView().isTraversable(file)) {
					setText(fileName + File.separator);
				}
			}

			return this;
		}
	}

	void setFileSelected() {
		if (getFileChooser().isMultiSelectionEnabled() && !isDirectorySelected()) {
			File[] files = getFileChooser().getSelectedFiles(); // Should be
			// selected
			Object[] selectedObjects = getFilesList().getSelectedValues(); // Are
			// actually
			// selected

			getListSelectionModel().setValueIsAdjusting(true);
			try {
				int lead = getListSelectionModel().getLeadSelectionIndex();
				int anchor = getListSelectionModel().getAnchorSelectionIndex();

				Arrays.sort(files);
				Arrays.sort(selectedObjects);

				int shouldIndex = 0;
				int actuallyIndex = 0;

				// Remove files that shouldn't be selected and add files which
				// should be selected
				// Note: Assume files are already sorted in compareTo order.
				while (shouldIndex < files.length
						&& actuallyIndex < selectedObjects.length) {
					int comparison = files[shouldIndex]
					                       .compareTo((File) selectedObjects[actuallyIndex]);
					if (comparison < 0) {
						doSelectFile(files[shouldIndex++]);
					} else if (comparison > 0) {
						doDeselectFile(selectedObjects[actuallyIndex++]);
					} else {
						// Do nothing
						shouldIndex++;
						actuallyIndex++;
					}

				}

				while (shouldIndex < files.length) {
					doSelectFile(files[shouldIndex++]);
				}

				while (actuallyIndex < selectedObjects.length) {
					doDeselectFile(selectedObjects[actuallyIndex++]);
				}

				// restore the anchor and lead
				if (getListSelectionModel() instanceof DefaultListSelectionModel) {
					((DefaultListSelectionModel) getListSelectionModel())
					.moveLeadSelectionIndex(lead);
					getListSelectionModel().setAnchorSelectionIndex(anchor);
				}
			} finally {
				getListSelectionModel().setValueIsAdjusting(false);
			}
		} else {
			JFileChooser chooser = getFileChooser();
			File f;
			if (isDirectorySelected()) {
				f = getDirectory();
			} else {
				f = chooser.getSelectedFile();
			}
			int i;
			if (f != null && (i = getModel().indexOf(f)) >= 0) {
				int viewIndex = getRowSorter().convertRowIndexToView(i);
				getListSelectionModel().setSelectionInterval(viewIndex, viewIndex);
				ensureIndexIsVisible(viewIndex);
			} else {
				clearSelection();
			}
		}
	}

	private void doSelectFile(File fileToSelect) {
		int index = getModel().indexOf(fileToSelect);
		// could be missed in the current directory if it changed
		if (index >= 0) {
			index = getRowSorter().convertRowIndexToView(index);
			getListSelectionModel().addSelectionInterval(index, index);
		}
	}

	private void doDeselectFile(Object fileToDeselect) {
		int index = getRowSorter().convertRowIndexToView(
				getModel().indexOf(fileToDeselect));
		getListSelectionModel().removeSelectionInterval(index, index);
	}

	/* The following methods are used by the PropertyChange Listener */

	private void doSelectedFileChanged(PropertyChangeEvent e) {
		applyEdit();
		File f = (File) e.getNewValue();
		JFileChooser fc = getFileChooser();
		if (f != null
				&& ((fc.isFileSelectionEnabled() && !f.isDirectory()) || (f.isDirectory() && fc
						.isDirectorySelectionEnabled()))) {

			setFileSelected();
		}
	}

	private void doSelectedFilesChanged(PropertyChangeEvent e) {
		applyEdit();
		File[] files = (File[]) e.getNewValue();
		JFileChooser fc = getFileChooser();
		if (files != null
				&& files.length > 0
				&& (files.length > 1 || fc.isDirectorySelectionEnabled() || !files[0]
				                                                                   .isDirectory())) {
			setFileSelected();
		}
	}

	private void doDirectoryChanged(PropertyChangeEvent e) {
		getDetailsTableModel().updateColumnInfo();

		JFileChooser fc = getFileChooser();
		FileSystemView fsv = fc.getFileSystemView();

		applyEdit();
		resetEditIndex();
		ensureIndexIsVisible(0);
		File currentDirectory = fc.getCurrentDirectory();
		if (currentDirectory != null) {
			if (!readOnly) {
				getNewFolderAction().setEnabled(canWrite(currentDirectory));
			}
			getFileChooserUIAccessor().getChangeToParentDirectoryAction().setEnabled(
					!fsv.isRoot(currentDirectory));
		}
		if (getFilesList() != null) {
			getFilesList().clearSelection();
		}
	}

	private void doFilterChanged(PropertyChangeEvent e) {
		applyEdit();
		resetEditIndex();
		clearSelection();
	}

	private void doFileSelectionModeChanged(PropertyChangeEvent e) {
		applyEdit();
		resetEditIndex();
		clearSelection();
	}

	private void doMultiSelectionChanged(PropertyChangeEvent e) {
		if (getFileChooser().isMultiSelectionEnabled()) {
			getListSelectionModel().setSelectionMode(
					ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			getListSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			clearSelection();
			getFileChooser().setSelectedFiles(null);
		}
	}

	/*
	 * Listen for filechooser property changes, such as the selected file
	 * changing, or the type of the dialog changing.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		if (viewType == -1) {
			setViewType(VIEWTYPE_LIST);
		}

		String s = e.getPropertyName();
		if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
			doSelectedFileChanged(e);
		} else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
			doSelectedFilesChanged(e);
		} else if (s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
			doDirectoryChanged(e);
		} else if (s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
			doFilterChanged(e);
		} else if (s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
			doFileSelectionModeChanged(e);
		} else if (s.equals(JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY)) {
			doMultiSelectionChanged(e);
		} else if (s.equals(JFileChooser.CANCEL_SELECTION)) {
			applyEdit();
		} else if (s.equals("busy")) {
			setCursor((Boolean) e.getNewValue() ? waitCursor : null);
		} else if (s.equals("componentOrientation")) {
			ComponentOrientation o = (ComponentOrientation) e.getNewValue();
			JFileChooser cc = (JFileChooser) e.getSource();
			if (o != e.getOldValue()) {
				cc.applyComponentOrientation(o);
			}
			if (detailsTable != null) {
				detailsTable.setComponentOrientation(o);
				detailsTable.getParent().getParent().setComponentOrientation(o);
			}
		}
	}

	private void ensureIndexIsVisible(int i) {
		if (i >= 0) {
			if (getFilesList() != null) {
				getFilesList().ensureIndexIsVisible(i);
			}
			if (detailsTable != null) {
				detailsTable.scrollRectToVisible(detailsTable.getCellRect(i,
						COLUMN_FILENAME, true));
			}
		}
	}

	public void ensureFileIsVisible(JFileChooser fc, File f) {
		int modelIndex = getModel().indexOf(f);
		if (modelIndex >= 0) {
			ensureIndexIsVisible(getRowSorter().convertRowIndexToView(modelIndex));
		}
	}

	public void rescanCurrentDirectory() {
		getModel().validateFileCache();
	}

	public void clearSelection() {
		if (getListSelectionModel() != null) {
			getListSelectionModel().clearSelection();
			if (getListSelectionModel() instanceof DefaultListSelectionModel) {
				((DefaultListSelectionModel) getListSelectionModel())
				.moveLeadSelectionIndex(0);
				getListSelectionModel().setAnchorSelectionIndex(0);
			}
		}
	}

	public JMenu getViewMenu() {
		if (viewMenu == null) {
			viewMenu = new JMenu(viewMenuLabelText);
			ButtonGroup viewButtonGroup = new ButtonGroup();

			for (int i = 0; i < VIEWTYPE_COUNT; i++) {
				JRadioButtonMenuItem mi = new JRadioButtonMenuItem(new ViewTypeAction(i));
				viewButtonGroup.add(mi);
				viewMenu.add(mi);
			}
			updateViewMenu();
		}
		return viewMenu;
	}

	private void updateViewMenu() {
		if (viewMenu != null) {
			Component[] comps = viewMenu.getMenuComponents();
			for (int i = 0; i < comps.length; i++) {
				if (comps[i] instanceof JRadioButtonMenuItem) {
					JRadioButtonMenuItem mi = (JRadioButtonMenuItem) comps[i];
					if (((ViewTypeAction) mi.getAction()).viewType == viewType) {
						mi.setSelected(true);
					}
				}
			}
		}
	}

	/**
	 * Create a popup menu on right click.
	 * Features: Refresh (?), Rename (?), Add to Bookmark, Show hidden files, Show size column
	 */
	public JPopupMenu createContextMenu() {
		JPopupMenu contextMenu = new JPopupMenu();

		ActionMap actionMap = getActionMap();
		Action refreshAction = actionMap.get(ACTION_REFRESH);
		if (refreshAction != null) {
			contextMenu.add(refreshAction);
		}

		// TODO leave new folder action?
		Action newFolderAction = actionMap.get(ACTION_NEW_FOLDER);
		if (newFolderAction != null) {
			contextMenu.add(newFolderAction);
		}

		JMenuItem addToBookmarkMenuItem = new JMenuItem();
		// TODO I18N
		addToBookmarkMenuItem.setText("Add to Bookmark");
		addToBookmarkMenuItem.setIcon(GtkStockIcon.get("gtk-add", Size.GTK_ICON_SIZE_MENU));
		final File path = getSelectedPath();		
		addToBookmarkMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				getFileChooserUIAccessor().getLocationsPane().addBookmark(path);
			}

		});
		addToBookmarkMenuItem.setEnabled(path != null && path.isDirectory());
		contextMenu.add(addToBookmarkMenuItem);

		contextMenu.addSeparator();

		// Add "show hidden files" CheckBoxMenuItem
		JCheckBoxMenuItem showHiddenCheckBoxItem = new JCheckBoxMenuItem();
		// TODO I18N
		showHiddenCheckBoxItem.setText("Show hidden files");
		showHiddenCheckBoxItem.setSelected(GtkFileChooserSettings.get().getShowHidden());
		showHiddenCheckBoxItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
				boolean showHidden = source.isSelected();
				getFileChooser().setFileHidingEnabled(!showHidden);
				GtkFileChooserSettings.get().setShowHidden(showHidden);
			}
		});
		contextMenu.add(showHiddenCheckBoxItem);

		// Add "show file size column" CheckBoxMenuItem
		JCheckBoxMenuItem showFileSizeCheckBoxItem = new JCheckBoxMenuItem();
		// TODO I18N
		showFileSizeCheckBoxItem.setText("Show size column");
		showFileSizeCheckBoxItem.setSelected(GtkFileChooserSettings.get()
				.getShowSizeColumn());
		showFileSizeCheckBoxItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
				boolean showSizeColumn = source.isSelected();
				GtkFileChooserSettings.get().setShowSizeColumn(showSizeColumn);

				List<SortKey> sortKeys = new ArrayList<SortKey>();
				SortKey sortKey = new RowSorter.SortKey(COLUMN_FILENAME,
						SortOrder.ASCENDING);
				sortKeys.add(sortKey);
				// Reset sorting settings
				rowSorter.setSortKeys(sortKeys);

				getDetailsTableModel().updateColumnInfo();
			}
		});
		contextMenu.add(showFileSizeCheckBoxItem);

		return contextMenu;
	}

	private MouseListener handler;

	protected MouseListener getMouseHandler() {
		if (handler == null) {
			handler = new GtkFilePaneMouseListener(this);
		}
		return handler;
	}

	/**
	 * Property to remember whether a directory is currently selected in the UI.
	 * 
	 * @return <code>true</code> iff a directory is currently selected.
	 */
	protected boolean isDirectorySelected() {
		return getFileChooserUIAccessor().isDirectorySelected();
	}

	/**
	 * Property to remember the directory that is currently selected in the UI.
	 * 
	 * @return the value of the <code>directory</code> property
	 * @see javax.swing.plaf.basic.BasicFileChooserUI#setDirectory
	 */
	protected File getDirectory() {
		return getFileChooserUIAccessor().getDirectory();
	}

	private Component findChildComponent(Container container, Class cls) {
		int n = container.getComponentCount();
		for (int i = 0; i < n; i++) {
			Component comp = container.getComponent(i);
			if (cls.isInstance(comp)) {
				return comp;
			} else if (comp instanceof Container) {
				Component c = findChildComponent((Container) comp, cls);
				if (c != null) {
					return c;
				}
			}
		}
		return null;
	}

	public boolean canWrite(File f) {
		// Return false for non FileSystem files or if file doesn't exist.
		if (!f.exists()) {
			return false;
		}

		if (f instanceof ShellFolder) {
			return ((ShellFolder) f).isFileSystem();
		} else {
			if (getFileChooserUIAccessor().usesShellFolder()) {
				try {
					return ShellFolder.getShellFolder(f).isFileSystem();
				} catch (FileNotFoundException ex) {
					// File doesn't exist
					return false;
				}
			} else {
				// Ordinary file
				return true;
			}
		}
	}

	private void setListSelectionModel(ListSelectionModel listSelectionModel) {
		this.listSelectionModel = listSelectionModel;
	}

	ListSelectionModel getListSelectionModel() {
		return listSelectionModel;
	}

	MyGTKFileChooserUIAccessor getFileChooserUIAccessor() {
		return (MyGTKFileChooserUIAccessor) fileChooserUIAccessor;
	}

	void setFilesList(JList filesList) {
		this.filesList = filesList;
	}

	JList getFilesList() {
		return filesList;
	}

	void setEditFile(File editFile) {
		this.editFile = editFile;
	}

	File getEditFile() {
		return editFile;
	}

	public File getSelectedPath() {
		int row = detailsTable.getSelectedRow();
		return row != -1 ? new File(detailsTableModel.getValueAt(row, 0).toString()) : null;
	}

	// This interface is used to access methods in the FileChooserUI
	// that are not public.
	public interface FileChooserUIAccessor {
		public JFileChooser getFileChooser();

		public BasicDirectoryModel getModel();

		public JPanel createList();

		public JPanel createDetailsView();

		public boolean isDirectorySelected();

		public File getDirectory();

		public Action getApproveSelectionAction();

		public Action getChangeToParentDirectoryAction();

		public Action getNewFolderAction();

		public MouseListener createDoubleClickListener(JList list);

		public ListSelectionListener createListSelectionListener();

		public boolean usesShellFolder();
	}
}
