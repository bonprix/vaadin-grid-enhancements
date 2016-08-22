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
            if (node.getNodeName().equals("INPUT")) {
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

    protected static boolean isFirstCell(CellReference cellReference) {
        return cellReference.getRowIndex() == 0 && cellReference.getColumnIndex() == 0;
    }

    protected static boolean isLastCell(CellReference cellReference, Grid grid) {
        return cellReference.getRowIndex() + 1 == grid.getDataSource().size() && cellReference.getColumnIndex() + 1 == grid.getColumnCount();
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
