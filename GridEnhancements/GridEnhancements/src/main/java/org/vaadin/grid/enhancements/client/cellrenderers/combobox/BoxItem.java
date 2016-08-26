package org.vaadin.grid.enhancements.client.cellrenderers.combobox;


/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class BoxItem {
    public String key;
    public String caption;

    @Override
    public String toString() {
        return caption;
    }
}
