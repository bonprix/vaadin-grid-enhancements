package org.vaadin.grid.enhancements.events;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class CellFocusEvent extends Component.Event {

    private final int row;
    private final int col;
    private boolean rowChanged;
    private boolean colChanged;
    private final Object itemId;

    public CellFocusEvent(final Component source, final int row, final int col, final boolean rowChanged, final boolean colChanged, final Object itemId) {
        super(source);
        this.row = row;
        this.col = col;
        this.itemId = itemId;
    }

    /**
     * Get currently focused row index
     *
     * @return The row index, -1 if the focus is in Header/Footer
     */
    public int getRow() {
        return this.row;
    }

    /**
     * Return true if the row was changed from the previously known value
     *
     * @return true if the row was changed
     */
    public boolean wasRowChanged() {
        return this.rowChanged;
    }

    /**
     * Get currently focused column index
     *
     * @return The column index
     */
    public int getColumn() {
        return this.col;
    }

    /**
     * Return true if the column was changed from the previously known value
     *
     * @return true if the column was changed
     */
    public boolean wasColumnChanged() {
        return this.colChanged;
    }

    /**
     * Get itemId which wherew focus is from underlying datasource
     *
     * @return itemId where focus is, null if focus in Header/Footer
     */
    public Object getItemId() {
        return this.itemId;
    }
}