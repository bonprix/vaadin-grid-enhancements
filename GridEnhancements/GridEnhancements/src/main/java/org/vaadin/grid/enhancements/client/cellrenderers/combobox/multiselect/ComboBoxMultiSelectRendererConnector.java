package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.vaadin.client.MouseEventDetailsBuilder;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.connectors.AbstractRendererConnector;
import com.vaadin.client.connectors.GridConnector;
import com.vaadin.client.renderers.ClickableRenderer;
import com.vaadin.client.renderers.Renderer;
import com.vaadin.client.widget.grid.RendererCellReference;
import com.vaadin.client.widgets.Grid;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.grid.renderers.RendererClickRpc;

import elemental.json.JsonObject;
import org.vaadin.grid.enhancements.cellrenderers.ComboBoxMultiselectRenderer;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.CellId;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.EventHandler;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionsInfo;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;

import java.util.List;
import java.util.Set;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
@Connect(ComboBoxMultiselectRenderer.class)
public class ComboBoxMultiSelectRendererConnector extends AbstractRendererConnector<OptionElement> {
	private static final long serialVersionUID = 1L;

	ComboBoxMultiselectServerRpc rpc = RpcProxy.create(ComboBoxMultiselectServerRpc.class, this);

	public class MultiSelectRenderer extends ClickableRenderer<OptionElement, ComboBoxMultiselect> {

		private static final String ROW_KEY_PROPERTY = "rowKey";
		private static final String COLUMN_ID_PROPERTY = "columnId";

		private String filter = "";

		@Override
		public ComboBoxMultiselect createWidget() {
			final ComboBoxMultiselect comboBoxMultiselect = GWT.create(ComboBoxMultiselect.class);

			comboBoxMultiselect.addDomHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
					Element e = comboBoxMultiselect.getElement();
					getRpcProxy(RendererClickRpc.class).click(	e.getPropertyString(ROW_KEY_PROPERTY),
																e.getPropertyString(COLUMN_ID_PROPERTY),
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
					ComboBoxMultiSelectRendererConnector.this.rpc.onValueSetChange(	getCellId(comboBoxMultiselect),
																					item);
				}

				@Override
				public void getPage(int pageNumber, boolean skipBlur) {
					if (!org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiSelectRendererConnector.MultiSelectRenderer.this.filter.isEmpty()) {
						ComboBoxMultiSelectRendererConnector.this.rpc.getFilterPage(org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiSelectRendererConnector.MultiSelectRenderer.this.filter,
																					pageNumber, skipBlur,
																					getCellId(comboBoxMultiselect));
					} else {
						ComboBoxMultiSelectRendererConnector.this.rpc.getPage(	pageNumber, skipBlur,
																				getCellId(comboBoxMultiselect));
					}
				}

				@Override
				public void filter(String filterValue, int pageNumber, boolean skipBlur) {
					org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiSelectRendererConnector.MultiSelectRenderer.this.filter = filterValue;
					ComboBoxMultiSelectRendererConnector.this.rpc.getFilterPage(filterValue, pageNumber, skipBlur,
																				getCellId(comboBoxMultiselect));
				}

				@Override
				public void clearFilter() {
					org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiSelectRendererConnector.MultiSelectRenderer.this.filter = "";
					ComboBoxMultiSelectRendererConnector.this.rpc.setSortingNeeded(true);
				}

				@Override
				public void selectAll() {
					ComboBoxMultiSelectRendererConnector.this.rpc.selectAll(getCellId(comboBoxMultiselect));
				}

				@Override
				public void deselectAll() {
					ComboBoxMultiSelectRendererConnector.this.rpc.deselectAll(getCellId(comboBoxMultiselect));
				}
			});

			comboBoxMultiselect	.getPopup()
								.setOwner(getGrid());

			return comboBoxMultiselect;
		}

		@Override
		public void render(final RendererCellReference cell, final OptionElement selectedValue,
				final ComboBoxMultiselect multiSelect) {
			this.filter = "";

			registerRpc(ComboBoxMultiselectClientRpc.class, new ComboBoxMultiselectClientRpc() {
				private static final long serialVersionUID = 1L;

				@Override
				public void setCurrentPage(int page, CellId id) {
					if (id.equals(getCellId(multiSelect))) {
						multiSelect.setCurrentPage(page);
					}
				}

				@Override
				public void updateOptions(OptionsInfo optionsInfo, List<OptionElement> options, boolean skipBlur,
						CellId id) {
					if (id.equals(getCellId(multiSelect))) {
						if (optionsInfo.getCurrentPage() != -1) {
							multiSelect.setCurrentPage(optionsInfo.getCurrentPage());
						}
						multiSelect.setInputPrompt(optionsInfo.getInputPrompt());
						multiSelect.setSelectAllText(optionsInfo.getSelectAllText());
						multiSelect.setDeselectAllText(optionsInfo.getDeselectAllText());

						multiSelect.updatePageAmount(optionsInfo.getPageAmount());
						multiSelect.updateSelection(options, skipBlur);
					}
				}

				@Override
				public void updateSelectedOptions(Set<OptionElement> selectedOptions, CellId id, boolean refreshPage) {
					if (id.equals(getCellId(multiSelect))) {
						multiSelect.setSelection(selectedOptions, refreshPage);
					}
				}
			});

			Element e = multiSelect.getElement();

			if (e.getPropertyString(ROW_KEY_PROPERTY) != getRowKey((JsonObject) cell.getRow())) {
				e.setPropertyString(ROW_KEY_PROPERTY, getRowKey((JsonObject) cell.getRow()));
			}
			// Generics issue, need a correctly typed column.

			if (e.getPropertyString(COLUMN_ID_PROPERTY) != getColumnId(getGrid().getColumn(cell.getColumnIndex()))) {
				e.setPropertyString(COLUMN_ID_PROPERTY, getColumnId(getGrid().getColumn(cell.getColumnIndex())));
			}

			ComboBoxMultiSelectRendererConnector.this.rpc.onRender(new CellId(e.getPropertyString(ROW_KEY_PROPERTY),
					e.getPropertyString(COLUMN_ID_PROPERTY)));

			if (multiSelect.isEnabled() != cell	.getColumn()
												.isEditable()) {
				multiSelect.setEnabled(cell	.getColumn()
											.isEditable());
			}
		}

		/**
		 * Create cell identification for current ComboBox on row and column.
		 *
		 * @param comboBox
		 *            ComboBox to get cell identification for
		 * @return CellId for ComboBox
		 */
		private CellId getCellId(ComboBoxMultiselect comboBox) {
			Element e = comboBox.getElement();
			return new CellId(e.getPropertyString(ROW_KEY_PROPERTY), e.getPropertyString(COLUMN_ID_PROPERTY));
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

	private Grid<JsonObject> getGrid() {
		return ((GridConnector) getParent()).getWidget();
	}

}
