package org.vaadin.grid.enhancements.cellrenderers;

import com.vaadin.server.FontIcon;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ServerBoxItem {

    private String caption;

    private Resource icon = null;

    public ServerBoxItem(String caption) {
        this.caption = caption;
    }

    public ServerBoxItem(String iconPath, String caption) {
        icon = new ThemeResource(iconPath);
        this.caption = caption;
    }

    public ServerBoxItem(FontIcon icon, String caption) {
        this.icon = icon;
        this.caption = caption;
    }

    public ServerBoxItem(String caption, Resource icon) {
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
