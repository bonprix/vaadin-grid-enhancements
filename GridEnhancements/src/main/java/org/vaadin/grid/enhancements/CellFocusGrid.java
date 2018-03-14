package org.vaadin.grid.enhancements;

import java.util.logging.Logger;

import org.vaadin.grid.enhancements.client.cellfocus.CellFocusGridServerRPC;
import org.vaadin.grid.enhancements.client.cellfocus.CellFocusGridState;
import org.vaadin.grid.enhancements.events.CellFocusEvent;
import org.vaadin.grid.enhancements.events.EventListenerList;
import org.vaadin.grid.enhancements.events.Listener;
import org.vaadin.grid.enhancements.events.RowFocusEvent;

import com.vaadin.data.Container.Indexed;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Grid;

public class CellFocusGrid extends AbstractExtension {

    private static Logger _logger = Logger.getLogger("CellFocusGrid");

    private static Logger getLogger() {
        return CellFocusGrid._logger;
    }

    //
    // Event interfaces
    //

    public interface CellFocusListener extends Listener<CellFocusEvent> {
    }

    private final EventListenerList<CellFocusListener, CellFocusEvent> cellFocusListeners = new EventListenerList<CellFocusListener, CellFocusEvent>();

    public interface RowFocusListener extends Listener<RowFocusEvent> {
    }

    private final EventListenerList<RowFocusListener, RowFocusEvent> rowFocusListeners = new EventListenerList<RowFocusListener, RowFocusEvent>();


    //
    // Actual class stuff
    //

    // Mirror state value here to avoid unnecessary comms
    private boolean hasRowFocusListener = false;

    // Mirror state value here to avoid unnecessary comms
    private boolean hasCellFocusListener = false;

    // Information about previously seen focused row
    private int lastFocusedRow = 0;
    private int lastFocusedCol = 0;

    /**
     * Default constructor. Enter key changes the row.
     *
     * @param grid Grid to extend
     */
    public CellFocusGrid(final Grid grid) {

        this.addRowFocusListener(new RowFocusListener() {
            @Override
            public void onEvent(final RowFocusEvent event) {

            }
        });
        registerRpc(new CellFocusGridServerRPC() {

            private Object getItemIdByRowIndex(final Grid g, final int rowIndex) {
                final Indexed ds = g.getContainerDataSource();
                Object itemId = null;
                if (rowIndex >= 0) {
                    itemId = ds.getIdByIndex(rowIndex);
                }
                return itemId;
            }

            @Override
            public void focusUpdated(final int rowIndex, final int colIndex) {
                final Object itemId = getItemIdByRowIndex(grid, rowIndex);
                if (CellFocusGrid.this.hasRowFocusListener && rowIndex != CellFocusGrid.this.lastFocusedRow) {
                    CellFocusGrid.this.rowFocusListeners.dispatch(new RowFocusEvent(grid, rowIndex, itemId));
                }

                if (CellFocusGrid.this.hasCellFocusListener && (rowIndex != CellFocusGrid.this.lastFocusedRow || colIndex != CellFocusGrid.this.lastFocusedCol)) {
                    CellFocusGrid.this.cellFocusListeners.dispatch(new CellFocusEvent(grid, rowIndex, colIndex,
                                                                                      CellFocusGrid.this.lastFocusedRow == rowIndex,
                                                                                      CellFocusGrid.this.lastFocusedCol == colIndex, itemId));
                }

                CellFocusGrid.this.lastFocusedRow = rowIndex;
                CellFocusGrid.this.lastFocusedCol = colIndex;
            }


            @Override
            public void ping() {
                CellFocusGrid.getLogger().info("Received ping");
            }

        }, CellFocusGridServerRPC.class);

        super.extend(grid);
    }

    public static CellFocusGrid extendGrid(final Grid grid) {
        return new CellFocusGrid(grid);
    }

    @Override
    public CellFocusGridState getState() {
        return (CellFocusGridState) super.getState();
    }


    //
    // Event listeners
    //

    /**
     * Register cell focus listener, which is triggered when focus has
     * changed.
     *
     * @param listener
     *            an CellFocusListener instance
     */
    public void addCellFocusListener(final CellFocusListener listener) {
        this.cellFocusListeners.addListener(listener);

        getState().hasCellFocusListener = true;
        this.hasCellFocusListener = true;
    }

    /**
     * Register row focus listener, which is triggered when row has
     * changed.
     *
     * @param listener
     *            an RowFocusListener instance
     */
    public void addRowFocusListener(final RowFocusListener listener) {
        this.rowFocusListeners.addListener(listener);

        getState().hasFocusListener = true;
        getState().hasRowFocusListener = true;
        this.hasRowFocusListener = true;
    }

    public void removeCellFocusListener(final CellFocusListener listener) {
        this.cellFocusListeners.removeListener(listener);
        getState().hasFocusListener = false;
        getState().hasRowFocusListener = false;
        this.hasRowFocusListener = false;
    }
}
