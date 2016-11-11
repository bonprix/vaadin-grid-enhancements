package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import com.vaadin.shared.communication.ServerRpc;

import java.util.Set;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.CellId;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionElement;

/**
 * Client to server rpc
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public interface ComboBoxMultiselectServerRpc extends ServerRpc {

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
	 * @param newValues
	 *            Selected values
	 */
	void onValueSetChange(CellId id, Set<OptionElement> newValues);

	/**
	 * filtering the visible options in the dropdown
	 * 
	 * @param id
	 *            Cell identification
	 * @param filter
	 *            string to filter the elements
	 */
	void filter(CellId id, String filter);

	/**
	 * workaround to load the value of the correct property
	 * 
	 * @param id
	 *            Cell identification
	 */
	void onRender(CellId id);

}
