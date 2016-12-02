package org.vaadin.grid.enhancements.client.navigation;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.widget.escalator.Cell;
import com.vaadin.client.widget.grid.CellReference;
import com.vaadin.client.widgets.Grid;

/**
 * Utilities for Grid keyboard navigation
 *
 * @author Mikael Grankvist - Vaadin
 */
public final class NavigationUtil {

	/**
	 * Focus into input field below given element. Note! Element needs to be a
	 * table TD element so we don't mistakenly recurse too much.
	 *
	 * @param componentElement
	 *            TD element to be searched for input and focused
	 */
	protected static void focusInputField(final Element componentElement) {
		if (componentElement == null || !componentElement	.getNodeName()
															.equals("TD")) {
			return;
		}
		Element input = getInputElement(componentElement.getChildNodes());
		if (input != null) {
			WidgetUtil.focus(input);
			input.scrollIntoView();
		}
	}

	/**
	 * Recursively find an input element for given Child node list
	 *
	 * @param nodes
	 *            NodeList of child nodes for element to find input under
	 * @return Input node if found else null.
	 */
	protected static Element getInputElement(NodeList<Node> nodes) {
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.getItem(i);
			// Focus on <input> and <button> but only if they are visible.
			if ((node	.getNodeName()
						.equals("INPUT")
					|| node	.getNodeName()
							.equals("BUTTON"))
					&& !((Element) node).getStyle()
										.getDisplay()
										.equals("none")) {
				return (Element) node;
			} else if (node	.getChildNodes()
							.getLength() > 0) {
				Element inputNode = getInputElement(node.getChildNodes());
				if (inputNode != null) {
					return inputNode;
				}
			}
		}

		return null;
	}

	protected static boolean hasInputElement(final Element componentElement) {
		if (componentElement == null || !componentElement	.getNodeName()
															.equals("TD")) {
			return false;
		}

		Element input = getInputElement(componentElement.getChildNodes());
		return input != null;
	}

	/**
	 * Check if cell is the first cell in the whole grid
	 *
	 * @param cellReference
	 *            Cell
	 * @param element
	 *            element
	 * @return true if first cell in the first row
	 */
	protected static boolean isFirstCell(CellReference cellReference, Element element) {
		return isFirstRow(cellReference) && !hasPreviousInputElement(element);
	}

	/**
	 * Check if given cell is in the first row of the grid
	 *
	 * @param cellReference
	 *            cell
	 * @return true if in first row of the grid
	 */
	protected static boolean isFirstRow(CellReference cellReference) {
		return cellReference.getRowIndex() == 0;
	}

	/**
	 * Check if given cell is in the first column of the row
	 *
	 * @param cellReference
	 *            cell
	 * @return true if in first column
	 */
	protected static boolean isFirstColumn(CellReference cellReference) {
		return cellReference.getColumnIndex() == 0;
	}

	/**
	 * Check if the cell is the last cell in the whole grid
	 *
	 * @param cellReference
	 *            Cell
	 * @param grid
	 *            Grid to check
	 * @param element
	 *            element
	 * @return true if the last column in the last row
	 */
	protected static boolean isLastCell(CellReference cellReference, Grid grid, Element element) {
		return isLastRow(cellReference, grid) && !hasNextInputElement(element);
	}

	/**
	 * Check if given cell is in the last row of the grid
	 *
	 * @param cellReference
	 *            cell
	 * @return true if in first row of the grid
	 */
	protected static boolean isLastRow(CellReference cellReference, Grid grid) {
		return cellReference.getRowIndex() + 1 == grid	.getDataSource()
														.size();
	}

	/**
	 * Check if given cell is on the last column of the row
	 *
	 * @param cellReference
	 *            Cell
	 * @param grid
	 *            Grid for cell
	 * @return true if in last column
	 */
	protected static boolean isLastColumn(CellReference cellReference, Grid grid) {
		return cellReference.getColumnIndex() + 1 == grid.getColumnCount();
	}

	/**
	 * Focus grid cell at rowIndex, columnIndex
	 */
	protected native static void focusCell(Grid<?> grid, int rowIndex, int columnIndex) /*-{
																						grid.@com.vaadin.client.widgets.Grid::focusCell(II)(rowIndex, columnIndex);
																						}-*/;

	/**
	 * Get the currently focused cell for Grid
	 *
	 * @return Currently focused cell
	 */
	protected native static Cell getFocusedCell(Grid<?> grid) /*-{
																var cfh = grid.@com.vaadin.client.widgets.Grid::cellFocusHandler;
																return cfh.@com.vaadin.client.widgets.Grid.CellFocusHandler::getFocusedCell()();
																}-*/;

	protected static boolean hasNextInputElement(Element element) {
		return hasNextInputElementRecursive(element.getNextSiblingElement());
	}

	private static boolean hasNextInputElementRecursive(Element element) {
		if (element == null) {
			return false;
		}

		if (hasInputElement(element)) {
			return true;
		}
		return hasNextInputElementRecursive(element.getNextSiblingElement());
	}

	protected static boolean hasPreviousInputElement(Element element) {
		return hasPreviousInputElementRecursive(element.getPreviousSiblingElement());
	}

	private static boolean hasPreviousInputElementRecursive(Element element) {
		if (element == null) {
			return false;
		}

		if (hasInputElement(element)) {
			return true;
		}

		return hasPreviousInputElementRecursive(element.getPreviousSiblingElement());
	}

	public static Element getFirstEditableElement(Element element) {
		return getFirstEditableElementRecursive(element);
	}

	private static Element getFirstEditableElementRecursive(Element element) {
		if (element == null) {
			return null;
		}

		Element editableElement = getFirstEditableElementRecursive(element.getPreviousSiblingElement());

		if (editableElement != null) {
			return editableElement;
		}

		if (hasInputElement(element)) {
			return element;
		}
		return null;
	}

	public static Element getLastEditableElement(Element element) {
		return getLastEditableElementRecursive(element);
	}

	private static Element getLastEditableElementRecursive(Element element) {
		if (element == null) {
			return null;
		}

		Element editableElement = getLastEditableElementRecursive(element.getNextSiblingElement());

		if (editableElement != null) {
			return editableElement;
		}

		if (hasInputElement(element)) {
			return element;
		}
		return null;
	}
}
