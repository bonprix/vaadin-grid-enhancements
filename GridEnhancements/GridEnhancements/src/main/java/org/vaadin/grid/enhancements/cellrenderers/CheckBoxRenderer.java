package org.vaadin.grid.enhancements.cellrenderers;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.renderers.ClickableRenderer;
import org.vaadin.grid.cellrenderers.EditableRenderer;
import org.vaadin.grid.cellrenderers.client.editable.common.CellId;
import org.vaadin.grid.cellrenderers.editable.common.EditableRendererEnabled;
import org.vaadin.grid.cellrenderers.editable.common.EditableRendererUtil;
import org.vaadin.grid.enhancements.client.cellrenderers.checkbox.CheckBoxRendererClientRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.checkbox.CheckBoxRendererServerRpc;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class CheckBoxRenderer extends EditableRenderer<Boolean> implements ClickableRenderer.RendererClickListener {

	private final EditableRendererEnabled editableRendererEnabled;

	public CheckBoxRenderer() {
		this(null);
	}

	public CheckBoxRenderer(EditableRendererEnabled editableRendererEnabled) {
		super(Boolean.class);
		addClickListener(this);

		registerRpc(this.rpc);

		this.editableRendererEnabled = editableRendererEnabled;
	}

	@Override
	public void click(RendererClickEvent event) {
		Item row = getParentGrid().getContainerDataSource()
			.getItem(event.getItemId());
		Object columnId = event.getPropertyId();

		@SuppressWarnings("unchecked")
		Property<Boolean> cell = (Property<Boolean>) row.getItemProperty(columnId);

		cell.setValue(!cell.getValue());

		fireItemEditEvent(event.getItemId(), row, columnId, cell.getValue());
	}

	private CheckBoxRendererServerRpc rpc = new CheckBoxRendererServerRpc() {

		@Override
		public void onRender(CellId id) {
			getRpcProxy(CheckBoxRendererClientRpc.class).setEnabled(
																	EditableRendererUtil.isColumnComponentEnabled(
																													getItemId(id
																														.getRowId()),
																													getParentGrid(),
																													CheckBoxRenderer.this.editableRendererEnabled),
																	id);
		}

	};
}
