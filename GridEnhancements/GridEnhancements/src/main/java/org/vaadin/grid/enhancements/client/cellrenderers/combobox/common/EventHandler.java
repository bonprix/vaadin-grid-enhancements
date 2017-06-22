package org.vaadin.grid.enhancements.client.cellrenderers.combobox.common;

import java.util.Set;

/**
 * Event handler for events between Widget and Connector
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public interface EventHandler<BEANTYPE> {
	/**
	 * Send value change event to server
	 * 
	 * @param item
	 *            changing item
	 */
	void change(BEANTYPE item);

	/**
	 * Send value change event to server
	 * 
	 * @param items
	 *            changing items
	 */
	void change(Set<BEANTYPE> items);

	/**
	 * Request options for page
	 * 
	 * @param pageNumber
	 *            Number of page to request options for
	 * @param skipBlur
	 *            if skipBlur should be skipped and popup shouldnt be hidden
	 */
	void getPage(int pageNumber, boolean skipBlur);

	/**
	 * Filter options with given filter String and show page number
	 * 
	 * @param filterValue
	 *            Filter String
	 * @param pageNumber
	 *            Options page number
	 * @param skipBlur
	 *            if skipBlur should be skipped and popup shouldnt be hidden
	 */
	void filter(String filterValue, int pageNumber, boolean skipBlur);

	/**
	 * Clear any existing filter value
	 */
	void clearFilter();

	/**
	 * selects all elements in the combobox
	 */
	void selectAll();

	/**
	 * deselects all elements in the combobox
	 */
	void deselectAll();
}
