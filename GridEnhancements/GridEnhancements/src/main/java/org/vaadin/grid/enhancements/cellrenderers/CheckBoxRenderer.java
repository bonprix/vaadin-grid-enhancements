package org.vaadin.grid.enhancements.cellrenderers;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.renderers.ClickableRenderer;
import org.vaadin.grid.cellrenderers.EditableRenderer;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class CheckBoxRenderer extends EditableRenderer<Boolean> implements ClickableRenderer.RendererClickListener {

    public CheckBoxRenderer() {
        super(Boolean.class);
        addClickListener(this);
    }

    @Override
    public void click(RendererClickEvent event) {
        Item row = getParentGrid().getContainerDataSource().getItem(event.getItemId());
        Object columnId = event.getPropertyId();

        @SuppressWarnings("unchecked")
        Property<Boolean> cell = (Property<Boolean>) row.getItemProperty(columnId);

        cell.setValue(!cell.getValue());

        fireItemEditEvent(event.getItemId(), row, columnId, cell.getValue());
    }

}
