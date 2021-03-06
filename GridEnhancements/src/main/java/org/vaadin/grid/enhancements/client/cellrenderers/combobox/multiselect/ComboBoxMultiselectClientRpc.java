package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import com.vaadin.shared.communication.ClientRpc;

import java.util.List;
import java.util.Set;

import org.vaadin.grid.cellrenderers.client.editable.common.CellId;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionsInfo;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;

/**
 * Server to client rpc
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public interface ComboBoxMultiselectClientRpc extends ClientRpc {

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
	 * @param skipBlur
	 *            if bluring and hiding popup should be skipped
	 * @param id
	 *            Cell identification
	 */
	void updateOptions(OptionsInfo optionsInfo, List<OptionElement> options, boolean skipBlur, CellId id);

	/**
	 * workaround to send the selected options to client
	 * 
	 * @param selectedOptions
	 *            selected options from server
	 * @param id
	 *            Cell identification
	 * @param refreshPage
	 *            if page should be refreshed after setting selection
	 * @param enabled
	 *            if comboBoxMultiselect should be enabled
	 */
	void updateSelectedOptions(Set<OptionElement> selectedOptions, CellId id, boolean refreshPage, boolean enabled);
}
