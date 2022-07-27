package nbradham;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
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
public final class Updater extends JFrame implements DocumentListener, WindowFocusListener, ActionListener {

	private static final long serialVersionUID = 1L;

	private final JTextArea pasteArea = new JTextArea("Paste Wishlist source code here.", 40, 21);
	private final TableModel model = new TableModel();

	private float priceShift = 1;

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
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder("Price Selection for Range"));
		ButtonGroup priceGroup = new ButtonGroup();
		panel.add(createRadioButton("Minimum", "0", priceGroup));
		panel.add(createRadioButton("1/2", ".5", priceGroup));
		JRadioButton button = createRadioButton("Maximum", "1", priceGroup);
		button.setSelected(true);
		panel.add(button);
		add(panel, gbc);

		gbc.gridy = 1;
		pasteArea.getDocument().addDocumentListener(this);
		add(new JScrollPane(pasteArea), gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		JTable table = new JTable(model);
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setPreferredWidth(1);
		tcm.getColumn(1).setPreferredWidth(800);
		tcm.getColumn(2).setPreferredWidth(30);
		tcm.getColumn(3).setPreferredWidth(20);

		JScrollPane tablePane = new JScrollPane(table);
		tablePane.setPreferredSize(
				new Dimension(990, pasteArea.getPreferredSize().height + panel.getPreferredSize().height));
		add(tablePane, gbc);

		pack();
	}

	/**
	 * Creates a new JRadioButton, sets the label and action command, adds a action
	 * listener, and adds it to the button group.
	 * 
	 * @param label   The label for the button.
	 * @param command The action command of the button.
	 * @param group   The ButtonGroup to add the button to.
	 * @return The new JRadioButton instance.
	 */
	private JRadioButton createRadioButton(String label, String command, ButtonGroup group) {
		JRadioButton button = new JRadioButton(label);
		button.setActionCommand(command);
		button.addActionListener(this);
		group.add(button);
		return button;
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
				int firstDol = price.indexOf('$') + 1, firstEnd = price.indexOf(' ', firstDol),
						dol = price.lastIndexOf('$') + 1, end = price.indexOf(' ', dol);
				float min = Float
						.parseFloat(price.substring(firstDol, firstEnd > firstDol ? firstEnd : price.length())),
						max = Float.parseFloat(price.substring(dol, end > dol ? end : price.length()));
				model.data.add(new Object[] { "aliexpress.com/item/" + el.attr("data-product-id") + ".html",
						el.getElementsByTag("h3").get(0).getAllElements().get(0).text(),
						String.format("$%.2f", min + (max - min) * priceShift), discount + '%' });
			});
			model.fireTableRowsInserted(model.getRowCount() - els.size(), model.getRowCount());
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

	@Override
	public void actionPerformed(ActionEvent e) {
		priceShift = Float.parseFloat(e.getActionCommand());
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