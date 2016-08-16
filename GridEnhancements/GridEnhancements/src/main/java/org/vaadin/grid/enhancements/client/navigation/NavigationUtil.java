package org.vaadin.grid.enhancements.client.navigation;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.widget.escalator.Cell;
import com.vaadin.client.widgets.Grid;

/**
 * Utilities for Grid keyboard navigation
 *
 * @author Mikael Grankvist - Vaadin
 */
public final class NavigationUtil {

    protected static void focusInputField(final Element componentElement) {
        if (componentElement == null) {
            return;
        }
        for (int i = 0; i < componentElement.getChildNodes().getLength(); i++) {
            Node node = componentElement.getChildNodes().getItem(i);
            if (node.getNodeName().equals("INPUT") || node.getNodeName().equals("SELECT")) {
                WidgetUtil.focus((Element) node);
                return;
            }
        }
    }


    protected native static void focusCell(Grid<?> grid, int rowIndex, int columnIndex) /*-{
        grid.@com.vaadin.client.widgets.Grid::focusCell(II)(rowIndex, columnIndex);
    }-*/;

    protected native static Cell getFocusedCell(Grid<?> grid) /*-{
        var cfh = grid.@com.vaadin.client.widgets.Grid::cellFocusHandler;
        return cfh.@com.vaadin.client.widgets.Grid.CellFocusHandler::getFocusedCell()();
    }-*/;
}
