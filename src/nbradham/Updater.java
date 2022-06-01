package nbradham;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
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
final class Updater extends JFrame implements DocumentListener, WindowFocusListener {

	private static final long serialVersionUID = 1L;

	private final JTextArea pasteArea = new JTextArea("Paste Wishlist source code here.", 45, 30);
	private final TableModel model = new TableModel();

	/**
	 * Constructs the GUI.
	 */
	private Updater() {
		super("AliExpress Sales Updater");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		addWindowFocusListener(this);

		pasteArea.getDocument().addDocumentListener(this);
		add(new JScrollPane(pasteArea));

		JTable table = new JTable(model);
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(900);
		tcm.getColumn(1).setPreferredWidth(1);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(true);

		JScrollPane tablePane = new JScrollPane(table);
		tablePane.setPreferredSize(new Dimension(990, pasteArea.getPreferredSize().height));
		add(tablePane);

		pack();
	}

	/**
	 * Shows the GUI and open TSV dialogue.
	 */
	private final void start() {
		setVisible(true);

		JFileChooser jfc = new JFileChooser(
				FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath() + "/../Downloads");
		jfc.setDialogTitle("Open Sales TSV");
		jfc.setFileFilter(new FileNameExtensionFilter("Tab Seperated Value (tsv) file", "tsv"));

		if (jfc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			dispose();
			return;
		}

		try {
			Scanner scan = new Scanner(jfc.getSelectedFile()).useDelimiter("\t");
			scan.nextLine();

			ArrayList<Object[]> data = new ArrayList<>();
			while (scan.hasNext()) {
				Object[] prod = new Object[3];
				String str = scan.next();

				prod[0] = Long.parseLong(str.substring(str.lastIndexOf('/') + 1, str.lastIndexOf('.')));
				prod[1] = scan.next();
				for (byte n = 0; n < 3; n++)
					scan.next();

				str = scan.next();
				prod[2] = Byte.parseByte(str.substring(0, str.length() - 1));

				scan.nextLine();
				data.add(prod);
			}

			scan.close();
			model.setData(data.toArray(new Object[data.size()][]));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		System.out.println("Parsing...");
		Document doc = e.getDocument();
		try {
			Elements els = Jsoup.parse(doc.getText(0, doc.getLength())).getElementsByAttribute("data-product-id");
			els.forEach(el -> {
				Elements disEls = el.getElementsByClass("product-discount");
				long tarId = Long.parseLong(el.attr("data-product-id"));
				for (short r = 0; r < model.data.length; r++)
					if ((long) model.data[r][0] == tarId) {
						model.data[r][1] = "█ " + (String) model.data[r][1];
						model.data[r][2] = disEls.size() > 0 ? Byte.parseByte(disEls.get(0).text()) : 0;
						model.fireTableRowsUpdated(r, r);
						System.out.print('.');
						return;
					}
			});
			System.out.println();
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
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
	 * Constructs and starts a new {@link Updater} instance.
	 * 
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Updater().start());
	}

	/**
	 * Manages table data.
	 * 
	 * @author Nickolas S. Bradham
	 *
	 */
	private final class TableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private static final String[] COL_NAMES = { "Name", "% Off" };
		private Object[][] data = {};

		/**
		 * Stores incoming data and calls {@link #fireTableDataChanged()}.
		 * 
		 * @param newData The new data to store.
		 */
		private final void setData(Object[][] newData) {
			data = newData;
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public int getColumnCount() {
			return COL_NAMES.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex + 1];
		}

		@Override
		public String getColumnName(int c) {
			return COL_NAMES[c];
		}
	}
}