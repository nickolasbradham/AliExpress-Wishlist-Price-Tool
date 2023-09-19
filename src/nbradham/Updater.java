package nbradham;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

/**
 * Handles entire program execution.
 * 
 * @author Nickolas S. Bradham
 *
 */
public final class Updater extends JFrame implements DocumentListener, WindowFocusListener {

	private static final long serialVersionUID = 1L;

	private final JTextArea pasteArea = new JTextArea("Paste Wishlist source code here.", 45, 21);
	private final TableModel model = new TableModel();

	/**
	 * Constructs the GUI.
	 */
	private Updater() {
		super("AliExpress Sales Util");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new GridBagLayout());
		addWindowFocusListener(this);

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		pasteArea.getDocument().addDocumentListener(this);
		add(new JScrollPane(pasteArea), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		JTable table = new JTable(model);
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(900);
		tcm.getColumn(1).setPreferredWidth(1);

		JScrollPane tablePane = new JScrollPane(table);
		tablePane.setPreferredSize(new Dimension(990, pasteArea.getPreferredSize().height));
		add(tablePane, gbc);

		pack();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		Document doc = e.getDocument();
		try {
			Elements els = Jsoup.parse(doc.getText(0, doc.getLength()))
					.getElementsByClass("AllListItem--rightContainer--2AiihCN");
			els.forEach(el -> {
				String price = el.getElementsByClass("AllListItem--priceNowText--24hulSy").get(0).text(),
						name = el.getElementsByClass("AllListItem--productNameText--3aZEYzK ellipse").get(0).text();
				if (price.isBlank())
					JOptionPane.showMessageDialog(this, "Could not retrieve price for: " + name, "Data Not Found Error",
							JOptionPane.ERROR_MESSAGE);
				else {
					price = price.substring(price.indexOf('$'));
					boolean isNew = true;
					for (String[] r : model.data)
						if (r[0].equals(name) && r[1].equals(price)) {
							isNew = false;
							break;
						}
					if (isNew)
						model.data.add(new String[] { name, price });
				}
			});
			int rows = model.getRowCount();
			model.fireTableRowsInserted(rows - Math.min(rows, els.size()), rows);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
		pasteArea.requestFocus();
		pasteArea.setSelectionStart(0);
		pasteArea.setSelectionEnd(Integer.MAX_VALUE);
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	/**
	 * Constructs and shows a new {@link Updater} instance.
	 * 
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Updater().setVisible(true));
	}

	/**
	 * Manages table data.
	 * 
	 * @author Nickolas S. Bradham
	 *
	 */
	private final class TableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private static final String[] COL_NAMES = { "Name", "~$" };
		private ArrayList<String[]> data = new ArrayList<>();

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return COL_NAMES.length;
		}

		@Override
		public String getValueAt(int rowIndex, int columnIndex) {
			return data.get(rowIndex)[columnIndex];
		}

		@Override
		public String getColumnName(int c) {
			return COL_NAMES[c];
		}
	}
}