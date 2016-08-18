package org.vaadin.grid.enhancements.client.cellrenderers;

import com.google.web.bindery.event.shared.HandlerRegistration;
import com.vaadin.client.connectors.ClickableRendererConnector;
import com.vaadin.client.renderers.ClickableRenderer;
import com.vaadin.client.ui.VCheckBox;
import com.vaadin.client.widget.grid.RendererCellReference;
import com.vaadin.shared.ui.Connect;
import elemental.json.JsonObject;
import org.vaadin.grid.enhancements.cellrenderers.CheckBoxRenderer;

/**
 * @author Vaadin Ltd
 */
@Connect(CheckBoxRenderer.class)
public class CheckBoxRendererConnector extends ClickableRendererConnector<Boolean> {

    @Override
    protected HandlerRegistration addClickHandler(ClickableRenderer.RendererClickHandler<JsonObject> jsonObjectRendererClickHandler) {
        return getRenderer().addClickHandler(jsonObjectRendererClickHandler);
    }

    public CheckBoxClientRenderer getRenderer() {
        return (CheckBoxClientRenderer) super.getRenderer();
    }

    public static class CheckBoxClientRenderer extends ClickableRenderer<Boolean, VCheckBox> {

        @Override
        public void render(RendererCellReference rendererCellReference, Boolean aBoolean, VCheckBox checkBox) {
            checkBox.setEnabled(rendererCellReference.getColumn().isEditable() && rendererCellReference.getGrid().isEnabled());

            checkBox.setValue(aBoolean);

            rendererCellReference.getElement().addClassName("unselectable");
        }

        @Override
        public VCheckBox createWidget() {
            VCheckBox checkBox = new VCheckBox();
            checkBox.addClickHandler(this);
            checkBox.getElement().removeAttribute("tabindex");
            return checkBox;
        }
    }
}
