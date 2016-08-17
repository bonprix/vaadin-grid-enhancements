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
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.grid.cellrenderers.EditableRenderer;
import org.vaadin.grid.cellrenderers.editable.DateFieldRenderer;
import org.vaadin.grid.cellrenderers.editable.TextFieldRenderer;
import org.vaadin.grid.enhancements.navigation.GridNavigationExtension;

import javax.servlet.annotation.WebServlet;
import java.text.SimpleDateFormat;
import java.util.Date;

@Theme("demo")
@Title("MyComponent Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI {


    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class, widgetset = "org.vaadin.grid.enhancements.demo.DemoWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    private Label latestChangeLabel;

    @Override
    protected void init(VaadinRequest request) {

        latestChangeLabel = new Label("Latest change: -none-");

        final Grid grid = new Grid(getDataSource());
        GridNavigationExtension.extend(grid);

        grid.setHeightByRows(10.0);
        grid.setWidth("700px");

        TextFieldRenderer<String> stringRenderer = new TextFieldRenderer<String>();
        grid.getColumn("foo").setRenderer(stringRenderer);
        grid.getColumn("foo").setExpandRatio(1);
        stringRenderer.addItemEditListener(new EditableRenderer.ItemEditListener() {
            @Override
            public void itemEdited(EditableRenderer.ItemEditEvent event) {
                latestChangeLabel.setValue("Latest change: '" + event.getColumnPropertyId() + "' " + event.getNewValue());
            }
        });
        TextFieldRenderer<Integer> integerRenderer = new TextFieldRenderer<Integer>();
        grid.getColumn("bar").setRenderer(integerRenderer);
        grid.getColumn("bar").setWidth(100);
        integerRenderer.addItemEditListener(new EditableRenderer.ItemEditListener() {
            @Override
            public void itemEdited(EditableRenderer.ItemEditEvent event) {
                latestChangeLabel.setValue("Latest change: '" + event.getColumnPropertyId() + "' " + event.getNewValue());
            }
        });
        TextFieldRenderer<Double> doubleRenderer = new TextFieldRenderer<Double>();
        grid.getColumn("km").setRenderer(doubleRenderer);
        grid.getColumn("km").setWidth(100);
        doubleRenderer.addItemEditListener(new EditableRenderer.ItemEditListener() {
            @Override
            public void itemEdited(EditableRenderer.ItemEditEvent event) {
                latestChangeLabel.setValue("Latest change: '" + event.getColumnPropertyId() + "' " + event.getNewValue());
            }
        });
        DateFieldRenderer dateRenderer = new DateFieldRenderer();
        grid.getColumn("today").setRenderer(dateRenderer);
        grid.getColumn("today").setWidth(200);
        dateRenderer.addItemEditListener(new EditableRenderer.ItemEditListener() {
            @Override
            public void itemEdited(EditableRenderer.ItemEditEvent event) {
                latestChangeLabel.setValue("Latest change: '" + event.getColumnPropertyId() + "' " + new SimpleDateFormat("dd-MM-yyyy").format(event.getNewValue()));
            }
        });

        // Show it in the middle of the screen
        VerticalLayout content = new VerticalLayout();
        content.setStyleName("demoContentLayout");
        content.setSizeFull();
        setContent(content);

        final VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setSizeUndefined();
        layout.addComponent(grid);
        layout.addComponent(latestChangeLabel);

        content.addComponent(layout);
        content.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);

    }

    public IndexedContainer getDataSource() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty("foo", String.class, "");
        container.addContainerProperty("bar", Integer.class, 0);
        // km contains double values from 0.0 to 2.0
        container.addContainerProperty("km", Double.class, 0);
        container.addContainerProperty("today", Date.class, new Date());

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
