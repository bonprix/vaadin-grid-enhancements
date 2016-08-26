package org.vaadin.grid.enhancements.cellrenderers;

import com.vaadin.server.FontIcon;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ServerBoxItem {

    private final String id;

    private String caption;

    private Resource icon = null;

    public ServerBoxItem(String id, String caption) {
        this.id = id;
        this.caption = caption;
    }

    public ServerBoxItem(String id, String iconPath, String caption) {
        this.id = id;
        icon = new ThemeResource(iconPath);
        this.caption = caption;
    }

    public ServerBoxItem(String id, FontIcon icon, String caption) {
        this.id = id;
        this.icon = icon;
        this.caption = caption;
    }

    public String getId() {
        return id;
    }

    public ServerBoxItem(String id, String caption, Resource icon) {
        this.id = id;
        this.caption = caption;
        this.icon = icon;
    }

    public String getCaption() {
        return caption;
    }

    public Resource getIcon() {
        return icon;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setIcon(Resource icon) {
        this.icon = icon;
    }

}
