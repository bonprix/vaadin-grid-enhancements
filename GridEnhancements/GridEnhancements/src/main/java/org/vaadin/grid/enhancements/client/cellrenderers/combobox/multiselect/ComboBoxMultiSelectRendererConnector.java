package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import java.util.List;
import java.util.Set;

import org.vaadin.grid.cellrenderers.client.editable.common.CellId;
import org.vaadin.grid.cellrenderers.client.editable.common.EditableRendererClientUtil;
import org.vaadin.grid.enhancements.cellrenderers.ComboBoxMultiselectRenderer;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.EventHandler;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionsInfo;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.vaadin.client.MouseEventDetailsBuilder;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.connectors.AbstractRendererConnector;
import com.vaadin.client.renderers.ClickableRenderer;
import com.vaadin.client.renderers.Renderer;
import com.vaadin.client.widget.grid.RendererCellReference;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.grid.renderers.RendererClickRpc;

import elemental.json.JsonObject;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
@Connect(ComboBoxMultiselectRenderer.class)
public class ComboBoxMultiSelectRendererConnector extends AbstractRendererConnector<OptionElement> {
	private static final long serialVersionUID = 1L;

	ComboBoxMultiselectServerRpc rpc = RpcProxy.create(ComboBoxMultiselectServerRpc.class, this);

	public class MultiSelectRenderer extends ClickableRenderer<OptionElement, ComboBoxMultiselect> {

		private String filter = "";

		@Override
		public ComboBoxMultiselect createWidget() {
			final ComboBoxMultiselect comboBoxMultiselect = GWT.create(ComboBoxMultiselect.class);

			comboBoxMultiselect.addDomHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					Element e = comboBoxMultiselect.getElement();
					getRpcProxy(RendererClickRpc.class)
						.click(	e.getPropertyString(EditableRendererClientUtil.ROW_KEY_PROPERTY),
								e.getPropertyString(EditableRendererClientUtil.COLUMN_ID_PROPERTY),
								MouseEventDetailsBuilder.buildMouseEventDetails(event.getNativeEvent()));
				}
			}, ClickEvent.getType());

			comboBoxMultiselect.addDomHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					event.stopPropagation();
				}
			}, MouseDownEvent.getType());

			comboBoxMultiselect.setEventHandler(new EventHandler<OptionElement>() {
				@Override
				public void change(OptionElement item) {
					// NOOP
				}

				@Override
				public void change(Set<OptionElement> item) {
					ComboBoxMultiSelectRendererConnector.this.rpc
						.onValueSetChange(EditableRendererClientUtil.getCellId(comboBoxMultiselect), item);
				}

				@Override
				public void getPage(int pageNumber, boolean skipBlur) {
					if (!org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiSelectRendererConnector.MultiSelectRenderer.this.filter
						.isEmpty()) {
						ComboBoxMultiSelectRendererConnector.this.rpc
							.getFilterPage(	org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiSelectRendererConnector.MultiSelectRenderer.this.filter,
											pageNumber, skipBlur,
											EditableRendererClientUtil.getCellId(comboBoxMultiselect));
					} else {
						ComboBoxMultiSelectRendererConnector.this.rpc
							.getPage(pageNumber, skipBlur, EditableRendererClientUtil.getCellId(comboBoxMultiselect));
					}
				}

				@Override
				public void filter(String filterValue, int pageNumber, boolean skipBlur) {
					org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiSelectRendererConnector.MultiSelectRenderer.this.filter = filterValue;
					ComboBoxMultiSelectRendererConnector.this.rpc
						.getFilterPage(	filterValue, pageNumber, skipBlur,
										EditableRendererClientUtil.getCellId(comboBoxMultiselect));
				}

				@Override
				public void clearFilter() {
					org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiSelectRendererConnector.MultiSelectRenderer.this.filter = "";
					ComboBoxMultiSelectRendererConnector.this.rpc.setSortingNeeded(true);
				}

				@Override
				public void selectAll() {
					ComboBoxMultiSelectRendererConnector.this.rpc
						.selectAll(EditableRendererClientUtil.getCellId(comboBoxMultiselect));
				}

				@Override
				public void deselectAll() {
					ComboBoxMultiSelectRendererConnector.this.rpc
						.deselectAll(EditableRendererClientUtil.getCellId(comboBoxMultiselect));
				}
			});

			comboBoxMultiselect.getPopup()
				.setOwner(EditableRendererClientUtil.getGridFromParent(getParent()));

			registerRpc(ComboBoxMultiselectClientRpc.class, new ComboBoxMultiselectClientRpc() {
				private static final long serialVersionUID = 1L;

				@Override
				public void setCurrentPage(int page, CellId id) {
					if (id.equals(EditableRendererClientUtil.getCellId(comboBoxMultiselect))) {
						comboBoxMultiselect.setCurrentPage(page);
					}
				}

				@Override
				public void updateOptions(OptionsInfo optionsInfo, List<OptionElement> options, boolean skipBlur,
						CellId id) {
					if (id.equals(EditableRendererClientUtil.getCellId(comboBoxMultiselect))) {
						if (optionsInfo.getCurrentPage() != -1) {
							comboBoxMultiselect.setCurrentPage(optionsInfo.getCurrentPage());
						}
						comboBoxMultiselect.setInputPrompt(optionsInfo.getInputPrompt());
						comboBoxMultiselect.setSelectAllText(optionsInfo.getSelectAllText());
						comboBoxMultiselect.setDeselectAllText(optionsInfo.getDeselectAllText());

						comboBoxMultiselect.updatePageAmount(optionsInfo.getPageAmount());
						comboBoxMultiselect.updateSelection(options, skipBlur);
					}
				}

				@Override
				public void updateSelectedOptions(Set<OptionElement> selectedOptions, CellId id, boolean refreshPage,
						boolean enabled) {
					if (id.equals(EditableRendererClientUtil.getCellId(comboBoxMultiselect))) {
						comboBoxMultiselect.setSelection(selectedOptions, refreshPage, enabled);
					}
				}
			});

			return comboBoxMultiselect;
		}

		@Override
		public void render(final RendererCellReference cell, final OptionElement selectedValue,
				final ComboBoxMultiselect multiSelect) {
			this.filter = "";

			Element e = multiSelect.getElement();

			EditableRendererClientUtil
				.setElementProperties(	e, getRowKey((JsonObject) cell.getRow()),
										EditableRendererClientUtil.getGridFromParent(getParent()),
										getColumnId(EditableRendererClientUtil.getGridFromParent(getParent())
											.getColumn(cell.getColumnIndex())));

			if (!cell.getColumn()
				.isEditable()
					|| !cell.getGrid()
						.isEnabled()) {
				multiSelect.setEnabled(false);
				return;
			}

			ComboBoxMultiSelectRendererConnector.this.rpc
				.onRender(new CellId(e.getPropertyString(EditableRendererClientUtil.ROW_KEY_PROPERTY),
						e.getPropertyString(EditableRendererClientUtil.COLUMN_ID_PROPERTY)));
		}

	}

	@Override
	public ComboBoxMultiselectState getState() {
		return (ComboBoxMultiselectState) super.getState();
	}

	@Override
	protected Renderer<OptionElement> createRenderer() {
		return new MultiSelectRenderer();
	}

	@Override
	public MultiSelectRenderer getRenderer() {
		return (MultiSelectRenderer) super.getRenderer();
	}

}
