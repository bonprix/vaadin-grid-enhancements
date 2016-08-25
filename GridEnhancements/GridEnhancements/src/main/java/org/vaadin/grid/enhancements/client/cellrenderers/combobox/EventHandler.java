package org.vaadin.grid.enhancements.client.cellrenderers.combobox;

import java.util.Set;

/**
 * Event handler for events between Widget and Connector
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public interface EventHandler {
    /**
     * Send value change event to server
     */
    void change(String item);

    void change(Set<String> items);

    /**
     * Request options for page
     * @param pageNumber Number of page to request options for
     */
    void getPage(int pageNumber);

    /**
     * Filter options with given filter String and show page number
     * @param filterValue Filter String
     * @param pageNumber Options page number
     */
    void filter(String filterValue, int pageNumber);

    /**
     * Clear any existing filter value
     */
    void clearFilter();
}
