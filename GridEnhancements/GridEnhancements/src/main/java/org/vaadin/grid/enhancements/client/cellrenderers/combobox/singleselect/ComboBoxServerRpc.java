package org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.CellId;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;

import com.vaadin.shared.communication.ServerRpc;

/**
 * Client to server rpc
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public interface ComboBoxServerRpc extends ServerRpc {

	/**
	 * Get options for requested page offset
	 *
	 * @param page
	 *            Items for page (-1 will send page with current selection)
	 * @param id
	 *            Cell identification
	 */
	void getPage(int page, CellId id);

	/**
	 * Get filtered results.
	 *
	 * @param filter
	 *            Filter string for items
	 * @param page
	 *            Page items to show
	 * @param id
	 *            Cell identification
	 */
	void getFilterPage(String filter, int page, CellId id);

	/**
	 * Selection event for combobox
	 *
	 * @param id
	 *            Cell identification
	 * @param newValue
	 *            Selected new value
	 */
	void onValueChange(CellId id, OptionElement newValue);

	void filter(CellId id, String filter);
}
