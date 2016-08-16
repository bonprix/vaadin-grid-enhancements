package org.vaadin.grid.enhancements.client.navigation;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.connectors.GridConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.widgets.Grid;
import com.vaadin.shared.ui.Connect;
import org.vaadin.grid.enhancements.navigation.GridNavigationExtension;

/**
 * @author Mikael Grankvist - Vaadin
 */
@Connect(GridNavigationExtension.class)
public class GridNavigationExtensionConnector extends AbstractExtensionConnector {

    @Override
    protected void extend(ServerConnector target) {

        Grid grid = ((GridConnector) target).getWidget();

        grid.addBodyKeyDownHandler(new BodyNaviagtionHandler());
        grid.addDomHandler(new NavigationHandler(grid), KeyDownEvent.getType());
    }
}
