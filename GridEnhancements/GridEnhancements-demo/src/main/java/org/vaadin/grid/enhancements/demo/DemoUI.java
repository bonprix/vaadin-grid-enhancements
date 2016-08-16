package org.vaadin.grid.enhancements.demo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.grid.cellrenderers.editable.TextFieldRenderer;
import org.vaadin.grid.enhancements.navigation.GridNavigationExtension;

import javax.servlet.annotation.WebServlet;

@Theme("demo")
@Title("MyComponent Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {


    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.vaadin.grid.enhancements.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {

        final Grid grid = new Grid(getDataSource());
        GridNavigationExtension.extend(grid);

        grid.setHeightByRows(10.0);

        grid.getColumn("foo").setRenderer(new TextFieldRenderer<String>());
        grid.getColumn("bar").setRenderer(new TextFieldRenderer<Integer>());
        grid.getColumn("km").setRenderer(new TextFieldRenderer<Double>());

        // Show it in the middle of the screen
        final VerticalLayout layout = new VerticalLayout();
        layout.setStyleName("demoContentLayout");
        layout.setSizeFull();
        layout.addComponent(grid);
        layout.setComponentAlignment(grid, Alignment.MIDDLE_CENTER);
        setContent(layout);

    }

    public IndexedContainer getDataSource() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty("foo", String.class, "");
        container.addContainerProperty("bar", Integer.class, 0);
        // km contains double values from 0.0 to 2.0
        container.addContainerProperty("km", Double.class, 0);

        // Populate data
        for (int i = 0; i <= 30; ++i) {
            Object itemId = container.addItem();
            Item item = container.getItem(itemId);
            item.getItemProperty("foo").setValue("foo");
            item.getItemProperty("bar").setValue(i);
            item.getItemProperty("km").setValue(i / 5.0d);
        }

        return container;
    }
}
