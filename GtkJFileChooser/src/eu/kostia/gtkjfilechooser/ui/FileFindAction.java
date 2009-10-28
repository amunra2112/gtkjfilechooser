package eu.kostia.gtkjfilechooser.ui;

import java.io.File;

import javax.swing.JTable;
import javax.swing.text.Position;

public class FileFindAction extends TableFindAction {
	@Override
	public int getNextMatch(JTable table, String prefix, int startIndex,
			Position.Bias bias) {
		
		int max = table.getRowCount();
		if (prefix == null) {
			throw new IllegalArgumentException();
		}
		if (startIndex < 0 || startIndex >= max) {
			throw new IllegalArgumentException();
		}

		prefix = prefix.toUpperCase();

		// start search from the next element after the selected element
		int increment = (bias == null || bias == Position.Bias.Forward) ? 1 : -1;
		int index = startIndex;
		do {
			int column = 0;
			File item = (File) table.getValueAt(index, column);

			if (item != null) {
				String text = item.getName();

				text = text.toUpperCase();

				if (text != null && text.startsWith(prefix)) {
					return index;
				}
			}
			index = (index + increment + max) % max;
		} while (index != startIndex);
		return -1;
	}
}