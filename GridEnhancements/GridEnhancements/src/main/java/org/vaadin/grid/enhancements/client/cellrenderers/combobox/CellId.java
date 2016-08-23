package org.vaadin.grid.enhancements.client.cellrenderers.combobox;

/**
 * Grid cell identification class
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class CellId {
    String rowId;
    String columnId;

    public CellId() {
    }

    public CellId(String rowId, String columnId) {
        this.rowId = rowId;
        this.columnId = columnId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getRowId() {
        return rowId;
    }

    public String getColumnId() {
        return columnId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CellId) {
            CellId target = (CellId) obj;
            return target.rowId.equals(rowId) && target.columnId.equals(columnId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (rowId + columnId).hashCode();
    }

    @Override
    public String toString() {
        return "Row: " + rowId + " Column: " + columnId;
    }
}
