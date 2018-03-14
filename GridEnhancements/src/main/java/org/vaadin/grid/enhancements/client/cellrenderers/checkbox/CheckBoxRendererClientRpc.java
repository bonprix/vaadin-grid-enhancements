package org.vaadin.grid.enhancements.client.cellrenderers.checkbox;

import org.vaadin.grid.cellrenderers.client.editable.common.CellId;

import com.vaadin.shared.communication.ClientRpc;

/**
 * Server to client rpc
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public interface CheckBoxRendererClientRpc extends ClientRpc {

	/**
	 * Send to the client if the checkbox should be enabled
	 * 
	 * @param enabled
	 *            checkbox is enabled
	 * @param id
	 *            Cell identification
	 */
	void setEnabled(boolean enabled, CellId id);
}
