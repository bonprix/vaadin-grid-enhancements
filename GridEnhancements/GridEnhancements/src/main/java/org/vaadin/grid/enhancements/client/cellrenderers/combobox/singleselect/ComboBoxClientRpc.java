package org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect;

import com.vaadin.shared.communication.ClientRpc;

import java.util.List;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.CellId;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionsInfo;

/**
 * Server to client rpc
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public interface ComboBoxClientRpc extends ClientRpc {

	/**
	 * Send to the client which page of options we are showing now
	 *
	 * @param page
	 *            Page of options
	 * @param id
	 *            Cell identification
	 */
	void setCurrentPage(int page, CellId id);

	/**
	 * Send updated options to client to be shown
	 *
	 * @param optionsInfo
	 *            Total amount of pages
	 * @param options
	 *            Options for current page
	 * @param id
	 *            Cell identification
	 */
	void updateOptions(OptionsInfo optionsInfo, List<OptionElement> options, CellId id);
}
