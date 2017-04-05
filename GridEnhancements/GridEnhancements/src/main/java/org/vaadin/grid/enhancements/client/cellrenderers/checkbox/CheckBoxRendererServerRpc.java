package org.vaadin.grid.enhancements.client.cellrenderers.checkbox;

import org.vaadin.grid.cellrenderers.client.editable.common.CellId;

import com.vaadin.shared.communication.ServerRpc;

/**
 * Client to server rpc
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public interface CheckBoxRendererServerRpc extends ServerRpc {

	/**
	 * workaround to set enabled of component correctly
	 * 
	 * @param id
	 *            Cell identification
	 */
	void onRender(CellId id);

}
