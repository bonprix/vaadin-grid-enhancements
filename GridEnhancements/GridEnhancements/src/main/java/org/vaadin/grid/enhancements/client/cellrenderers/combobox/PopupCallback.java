package org.vaadin.grid.enhancements.client.cellrenderers.combobox;

import java.util.Set;

/**
 * CallBack interface for communication from ComboBoxPopup to ComboBox
 * @param <T>
 */
public interface PopupCallback<T> {
    /**
     * Item selected (SingleSelectionMode)
     * @param item selected item
     */
    void itemSelected(T item);

    /**
     * Request options for next page
     */
    void nextPage();

    /**
     * Request options for previous page
     */
    void prevPage();

    /**
     * Clear filters and changes
     */
    void clear();

    /**
     * Items selected (MultiSelectionMode)
     * @param selectedObjects Set of selected items
     */
    void itemsSelected(Set<T> selectedObjects);
}