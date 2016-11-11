package org.vaadin.grid.enhancements.client.cellrenderers.combobox.common;

import java.util.Set;

/**
 * CallBack interface for communication from ComboBoxPopup to ComboBox
 * 
 * @param <T>
 *            beantype
 */
public interface PopupCallback<T> {
	/**
	 * Item selected (SingleSelectionMode)
	 * 
	 * @param item
	 *            selected item
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
	 * Items selected (MultiSelectionMode)
	 * 
	 * @param selectedObjects
	 *            Set of selected items
	 */
	void itemsSelected(Set<T> selectedObjects);

	/**
	 * focuses the textfield of the combobox
	 */
	void focus();

	/**
	 * selects all elements in the combobox
	 */
	void selectAll();

	/**
	 * deselects all elements in the combobox
	 */
	void deselectAll();

	/**
	 * set if the blur event of textfield should be skipped
	 */
	void setSkipBlur(boolean skipBlur);

}