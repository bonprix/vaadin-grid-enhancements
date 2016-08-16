package org.vaadin.grid.enhancements.client.navigation;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.vaadin.client.VConsole;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.widget.grid.CellReference;
import com.vaadin.client.widget.grid.events.BodyKeyDownHandler;
import com.vaadin.client.widget.grid.events.GridKeyDownEvent;
import com.vaadin.client.widgets.Grid;

public class BodyNaviagtionHandler implements BodyKeyDownHandler {

    @Override
    public void onKeyDown(GridKeyDownEvent event) {
        VConsole.log("Handle key event! " + WidgetUtil.getFocusedElement());
        switch (event.getNativeKeyCode()) {
            case KeyCodes.KEY_ENTER:
                if (isCellContainingComponent(event.getFocusedCell())) {
                    // Don't propagate enter to component
                    event.preventDefault();
                    event.stopPropagation();

                    final Element componentElement = extractComponentElement(event.getFocusedCell());

                    // Run focus as deferred command so the Navigation handler doesn't catch the event.
                    Scheduler.get().scheduleDeferred(new Command() {

                        @Override
                        public void execute() {
                            WidgetUtil.focus(componentElement);
                            NavigationUtil.focusInputField(componentElement);
                        }
                    });

                }
                break;
        }
    }

    private Element extractComponentElement(CellReference cell) {

        for (int i = 0; i < cell.getElement().getChildNodes().getLength(); i++) {
            Node node = cell.getElement().getChildNodes().getItem(i);
            if (node.getNodeName().equals("INPUT")) {
                return cell.getElement();
            }
        }

        return ((AbstractComponentConnector) cell.getValue()).getWidget().getElement();
    }

    private boolean isCellContainingComponent(CellReference cell) {
        VConsole.log(cell.getValue().toString() + " : " + cell.toString() + " : " + cell.getElement().getNodeName());

        for (int i = 0; i < cell.getElement().getChildNodes().getLength(); i++) {
            Node node = cell.getElement().getChildNodes().getItem(i);
            if (node.getNodeName().equals("INPUT")) {
                return true;
            }
        }
        return cell.getValue() instanceof AbstractComponentConnector;
    }
}