package org.vaadin.grid.enhancements.client.cellrenderers.combobox;


/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class BoxItem {
    public String id;
    public String key;
    public String caption;

    public BoxItem() { }

    public BoxItem(String id, String caption) {
        this.id = id;
        this.caption = caption;
    }

    @Override
    public String toString() {
        return caption;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoxItem)
            return obj != null && id.equals(((BoxItem) obj).id);
        return false;
    }
}
