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
     * Focus into input field below given element.
     * Note! Element needs to be a table TD element so we don't mistakenly recurse too much.
     *
     * @param componentElement TD element to be searched for input and focused
     */
    protected static void focusInputField(final Element componentElement) {
        if (componentElement == null || !componentElement.getNodeName().equals("TD")) {
            return;
        }
        Element input = getInputElement(componentElement.getChildNodes());
        if (input != null) {
            WidgetUtil.focus(input);
        }
    }

    /**
     * Recursively find an input element for given Child node list
     *
     * @param nodes NodeList of child nodes for element to find input under
     * @return Input node if found else null.
     */
    protected static Element getInputElement(NodeList<Node> nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.getItem(i);
            // Focus on <input> and <button> but only if they are visible.
            if ((node.getNodeName().equals("INPUT") || node.getNodeName().equals("BUTTON")) && !((Element) node).getStyle().getDisplay().equals("none")) {
                return (Element) node;
            } else if (node.getChildNodes().getLength() > 0) {
                Element inputNode = getInputElement(node.getChildNodes());
                if (inputNode != null) {
                    return inputNode;
                }
            }
        }

        return null;
    }

    /**
     * Check if cell is the first cell in the whole grid
     *
     * @param cellReference Cell
     * @return true if first cell in the first row
     */
    protected static boolean isFirstCell(CellReference cellReference) {
        return cellReference.getRowIndex() == 0 && cellReference.getColumnIndex() == 0;
    }

    /**
     * Check if the cell is the last cell in the whole grid
     *
     * @param cellReference Cell
     * @param grid          Grid to check
     * @return true if the last column in the last row
     */
    protected static boolean isLastCell(CellReference cellReference, Grid grid) {
        return cellReference.getRowIndex() + 1 == grid.getDataSource().size() && cellReference.getColumnIndex() + 1 == grid.getColumnCount();
    }

    /**
     * Check if given cell is in the first column of the row
     *
     * @param cellReference cell
     * @return true if in first column
     */
    protected static boolean isfirstColumn(CellReference cellReference) {
        return cellReference.getColumnIndex() == 0;
    }

    /**
     * Check if given cell is on the last column of the row
     *
     * @param cellReference Cell
     * @param grid          Grid for cell
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
}
