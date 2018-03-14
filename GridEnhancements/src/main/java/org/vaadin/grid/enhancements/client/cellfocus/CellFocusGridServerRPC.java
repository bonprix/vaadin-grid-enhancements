package org.vaadin.grid.enhancements.client.cellfocus;

import com.vaadin.shared.communication.ServerRpc;

public interface CellFocusGridServerRPC extends ServerRpc {

    void focusUpdated(int rowIndex, int colIndex);

    void ping();
}
