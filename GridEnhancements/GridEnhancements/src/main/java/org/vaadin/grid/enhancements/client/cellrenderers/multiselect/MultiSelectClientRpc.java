package org.vaadin.grid.enhancements.client.cellrenderers.multiselect;

import com.vaadin.shared.communication.ClientRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.CellId;

import java.util.List;

/**
 * Server to client rpc
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public interface MultiSelectClientRpc extends ClientRpc {

    /**
     * Send to the client which page of options we are showing now
     *
     * @param page Page of options
     * @param id   Cell identification
     */
    void setCurrentPage(int page, CellId id);

    /**
     * Send updated options to client to be shown
     *
     * @param pages   Total amount of pages
     * @param options Options for current page
     * @param id      Cell identification
     */
    void updateOptions(int pages, List<String> options, CellId id);
}
