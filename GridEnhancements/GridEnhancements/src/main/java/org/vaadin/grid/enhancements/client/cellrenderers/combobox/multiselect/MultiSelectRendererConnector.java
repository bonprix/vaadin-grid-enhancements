package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.connectors.AbstractRendererConnector;
import com.vaadin.client.connectors.GridConnector;
import com.vaadin.client.renderers.ClickableRenderer;
import com.vaadin.client.renderers.Renderer;
import com.vaadin.client.widget.grid.RendererCellReference;
import com.vaadin.client.widgets.Grid;
import com.vaadin.shared.ui.Connect;
import elemental.json.JsonObject;
import org.vaadin.grid.enhancements.cellrenderers.ComboBoxMultiselectRenderer;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.CellId;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.EventHandler;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionsInfo;

import java.util.List;
import java.util.Set;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
@Connect(ComboBoxMultiselectRenderer.class)
public class MultiSelectRendererConnector extends AbstractRendererConnector<OptionElement> {
	private static final long serialVersionUID = 1L;

	ComboBoxMultiselectServerRpc rpc = RpcProxy.create(ComboBoxMultiselectServerRpc.class, this);

	public class MultiSelectRenderer extends ClickableRenderer<OptionElement, MultiSelect> {

		private static final String ROW_KEY_PROPERTY = "rowKey";
		private static final String COLUMN_ID_PROPERTY = "columnId";

		private String filter = "";

		@Override
		public MultiSelect createWidget() {
			final MultiSelect comboBox = GWT.create(MultiSelect.class);

			comboBox.addDomHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					event.stopPropagation();
				}
			}, ClickEvent.getType());

			comboBox.addDomHandler(new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					event.stopPropagation();
				}
			}, MouseDownEvent.getType());

			comboBox.setEventHandler(new EventHandler<OptionElement>() {
				@Override
				public void change(OptionElement item) {
					// NOOP
				}

				@Override
				public void change(Set<OptionElement> item) {
					MultiSelectRendererConnector.this.rpc.onValueSetChange(getCellId(comboBox), item);
				}

				@Override
				public void getPage(int pageNumber) {
					if (!org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.MultiSelectRendererConnector.MultiSelectRenderer.this.filter.isEmpty()) {
						MultiSelectRendererConnector.this.rpc.getFilterPage(org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.MultiSelectRendererConnector.MultiSelectRenderer.this.filter,
																			pageNumber, getCellId(comboBox));
					} else {
						MultiSelectRendererConnector.this.rpc.getPage(pageNumber, getCellId(comboBox));
					}
				}

				@Override
				public void filter(String filterValue, int pageNumber) {
					org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.MultiSelectRendererConnector.MultiSelectRenderer.this.filter = filterValue;
					MultiSelectRendererConnector.this.rpc.getFilterPage(filterValue, pageNumber, getCellId(comboBox));
				}

				@Override
				public void clearFilter() {
					org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.MultiSelectRendererConnector.MultiSelectRenderer.this.filter = "";
				}
			});

			return comboBox;
		}

		@Override
		public void render(final RendererCellReference cell, final OptionElement selectedValue,
				final MultiSelect multiSelect) {
			this.filter = "";

			registerRpc(ComboBoxMultiselectClientRpc.class, new ComboBoxMultiselectClientRpc() {
				@Override
				public void setCurrentPage(int page, CellId id) {
					if (id.equals(getCellId(multiSelect))) {
						multiSelect.setCurrentPage(page);
					}
				}

				@Override
				public void updateOptions(OptionsInfo optionsInfo, List<OptionElement> options, CellId id) {
					if (id.equals(getCellId(multiSelect))) {
						if (optionsInfo.getCurrentPage() != -1) {
							multiSelect.setCurrentPage(optionsInfo.getCurrentPage());
						}
						multiSelect.updatePageAmount(optionsInfo.getPageAmount());
						multiSelect.updateSelection(options);
					}
				}

				@Override
				public void updateSelectedOptions(Set<OptionElement> selectedOptions, CellId id) {
					if (id.equals(getCellId(multiSelect))) {
						multiSelect.setSelection(selectedOptions);
					}
				}
			});

			Element e = multiSelect.getElement();

			// TODO
			// getState().value = selectedValue;

			if (e.getPropertyString(ROW_KEY_PROPERTY) != getRowKey((JsonObject) cell.getRow())) {
				e.setPropertyString(ROW_KEY_PROPERTY, getRowKey((JsonObject) cell.getRow()));
			}
			// Generics issue, need a correctly typed column.

			if (e.getPropertyString(COLUMN_ID_PROPERTY) != getColumnId(getGrid().getColumn(cell.getColumnIndex()))) {
				e.setPropertyString(COLUMN_ID_PROPERTY, getColumnId(getGrid().getColumn(cell.getColumnIndex())));
			}

			MultiSelectRendererConnector.this.rpc.onRender(new CellId(e.getPropertyString(ROW_KEY_PROPERTY),
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
		private CellId getCellId(MultiSelect comboBox) {
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
