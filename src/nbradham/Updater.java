package nbradham;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.JFrame;
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

	private final JTextArea pasteArea = new JTextArea("Paste Wishlist source code here.", 45, 30);
	private final TableModel model = new TableModel();

	/**
	 * Constructs the GUI.
	 */
	private Updater() {
		super("AliExpress Sales Util");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		addWindowFocusListener(this);

		pasteArea.getDocument().addDocumentListener(this);
		add(new JScrollPane(pasteArea));

		JTable table = new JTable(model);
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(1);
		tcm.getColumn(1).setPreferredWidth(800);
		tcm.getColumn(2).setPreferredWidth(30);
		tcm.getColumn(3).setPreferredWidth(20);

		JScrollPane tablePane = new JScrollPane(table);
		tablePane.setPreferredSize(new Dimension(990, pasteArea.getPreferredSize().height));
		add(tablePane);

		pack();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		Document doc = e.getDocument();
		try {
			Elements els = Jsoup.parse(doc.getText(0, doc.getLength())).getElementsByAttribute("data-product-id");
			els.forEach(el -> {
				Elements diss = el.getElementsByClass("product-discount");
				String price, discount;
				if (diss.size() > 0) {
					price = el.getElementsByClass("old-price").get(0).text();
					discount = el.getElementsByClass("product-discount").get(0).text();
				} else {
					price = el.getElementsByClass("price").get(0).text();
					discount = "0";
				}
				int dol = price.lastIndexOf('$'), end = price.indexOf(' ', dol);
				model.data.add(new Object[] { "aliexpress.com/item/" + el.attr("data-product-id") + ".html",
						el.getElementsByTag("h3").get(0).getAllElements().get(0).text(),
						price.substring(dol, end > dol ? end : price.length()), discount + '%' });
			});
			model.fireTableRowsInserted(model.getRowCount() - els.size(), model.getRowCount());
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

		private static final String[] COL_NAMES = { "URL", "Name", "~$", "% Off" };
		private ArrayList<Object[]> data = new ArrayList<>();

//		private TableModel() {
//			data.add(new Object[] { "aliexpress.com/item/3256803421363559.html",
//					"Dakimakura Sexy Pillow Case Anime Fate Series Double-sided Print Cute Naked Girl Otaku Body Pillowcase Body Decor Cushion Cover",
//					"$35.90", "32%" });
//			data.add(new Object[] { "aliexpress.com/item/3256803429694590.html",
//					"Dakimakura Sexy Pillow Cover Anime FATE series Double-sided Print Covers Cute Naked Girl Body Pillowcase Otaku Body Pillow Case",
//					"$35.90", "32%" });
//		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return COL_NAMES.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data.get(rowIndex)[columnIndex];
		}

		@Override
		public String getColumnName(int c) {
			return COL_NAMES[c];
		}
	}
}