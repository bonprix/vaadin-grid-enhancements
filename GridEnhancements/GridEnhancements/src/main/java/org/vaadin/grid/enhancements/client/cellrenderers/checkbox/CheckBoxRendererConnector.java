package org.vaadin.grid.enhancements.client.cellrenderers.checkbox;

import org.vaadin.grid.cellrenderers.client.editable.common.CellId;
import org.vaadin.grid.cellrenderers.client.editable.common.EditableRendererClientUtil;
import org.vaadin.grid.enhancements.cellrenderers.CheckBoxRenderer;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.connectors.ClickableRendererConnector;
import com.vaadin.client.renderers.ClickableRenderer;
import com.vaadin.client.renderers.Renderer;
import com.vaadin.client.widget.grid.RendererCellReference;
import com.vaadin.shared.ui.Connect;
import elemental.json.JsonObject;

/**
 * @author Vaadin Ltd
 */
@Connect(CheckBoxRenderer.class)
public class CheckBoxRendererConnector extends ClickableRendererConnector<Boolean> {

	CheckBoxRendererServerRpc rpc = RpcProxy.create(CheckBoxRendererServerRpc.class, this);

	@Override
	protected HandlerRegistration addClickHandler(
			ClickableRenderer.RendererClickHandler<JsonObject> jsonObjectRendererClickHandler) {
		return getRenderer().addClickHandler(jsonObjectRendererClickHandler);
	}

	@Override
	public CheckBoxClientRenderer getRenderer() {
		return (CheckBoxClientRenderer) super.getRenderer();
	}

	@Override
	protected Renderer<Boolean> createRenderer() {
		return new CheckBoxClientRenderer();
	}

	public class CheckBoxClientRenderer extends ClickableRenderer<Boolean, CheckBox> {

		@Override
		public void render(RendererCellReference cell, Boolean aBoolean, CheckBox checkBox) {
			checkBox.setValue(aBoolean);

			cell.getElement()
				.addClassName("unselectable");

			Element e = checkBox.getElement();

			EditableRendererClientUtil
				.setElementProperties(	e, getRowKey((JsonObject) cell.getRow()),
										EditableRendererClientUtil.getGridFromParent(getParent()),
										getColumnId(EditableRendererClientUtil.getGridFromParent(getParent())
											.getColumn(cell.getColumnIndex())));

			if (!cell.getColumn()
				.isEditable()
					|| !cell.getGrid()
						.isEnabled()) {
				checkBox.setEnabled(false);
				return;
			}

			CheckBoxRendererConnector.this.rpc
				.onRender(new CellId(e.getPropertyString(EditableRendererClientUtil.ROW_KEY_PROPERTY),
						e.getPropertyString(EditableRendererClientUtil.COLUMN_ID_PROPERTY)));
		}

		@Override
		public CheckBox createWidget() {
			final CheckBox checkBox = new CheckBox();
			checkBox.addClickHandler(this);
			checkBox.setStyleName("v-checkbox");
			checkBox.getElement()
				.removeAttribute("tabindex");

			registerRpc(CheckBoxRendererClientRpc.class, new CheckBoxRendererClientRpc() {

				@Override
				public void setEnabled(boolean enabled, CellId id) {
					if (id.equals(EditableRendererClientUtil.getCellId(checkBox))) {
						checkBox.setEnabled(enabled);
					}
				}

			});

			return checkBox;
		}

	}

}
