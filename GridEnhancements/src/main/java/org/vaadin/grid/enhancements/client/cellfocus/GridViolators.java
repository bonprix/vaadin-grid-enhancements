package org.vaadin.grid.enhancements.client.cellfocus;
import com.vaadin.client.widgets.Grid;

public class GridViolators {

    // ========================================================================
    // Violators - access Grid internals irrespective of visibility
    // ========================================================================

    public static native final int getFocusedRow(Grid<?> grid) /*-{
        var cfh = grid.@com.vaadin.client.widgets.Grid::cellFocusHandler;
        var row = cfh.@com.vaadin.client.widgets.Grid.CellFocusHandler::rowWithFocus;
        var contWithFocus= cfh.@com.vaadin.client.widgets.Grid.CellFocusHandler::containerWithFocus;
        var escallator = grid.@com.vaadin.client.widgets.Grid::getEscalator()();
        var conBody = escallator.@com.vaadin.client.widgets.Escalator::getBody()();
        if (contWithFocus == conBody)
            return row;
        else
            return -1;
    }-*/;

    public static native final int getFocusedCol(Grid<?> grid) /*-{
        var cfh = grid.@com.vaadin.client.widgets.Grid::cellFocusHandler;
        var cell = cfh.@com.vaadin.client.widgets.Grid.CellFocusHandler::getFocusedCell()();
        var col = cell.@com.vaadin.client.widget.escalator.Cell::getColumn()();
        return col;
    }-*/;

}