package org.vaadin.grid.enhancements.cellrenderers;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.grid.cellrenderers.EditableRenderer;
import org.vaadin.grid.cellrenderers.client.editable.common.CellId;
import org.vaadin.grid.cellrenderers.editable.common.EditableRendererEnabled;
import org.vaadin.grid.cellrenderers.editable.common.EditableRendererUtil;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionsInfo;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect.ComboBoxClientRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect.ComboBoxServerRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect.ComboBoxState;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.shared.ui.combobox.FilteringMode;

/**
 * Grid renderer that renders a ComboBox element
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxRenderer<BEANTYPE> extends EditableRenderer<BEANTYPE> {

	private final BeanItemContainer<BEANTYPE> container;

	private final int pageSize;
	private int pages;
	private final String inputPrompt;

	private String itemIdPropertyId;
	private String itemCaptionPropertyId;
	private FilteringMode filteringMode = FilteringMode.CONTAINS;
	private final boolean nullSelectionAllowed;

	private EditableRendererEnabled editableRendererEnabled;

	private BEANTYPE nullSelectionElement;

	public ComboBoxRenderer(final Class<BEANTYPE> clazz, List<BEANTYPE> selections, String itemIdPropertyId,
			String itemCaptionPropertyId, int pageSize, String inputPrompt, boolean nullSelectionAllowed,
			EditableRendererEnabled editableRendererEnabled) {
		super(clazz);

		registerRpc(this.rpc);
		// Add items to internal list so we don't expose ourselves to changes in
		// the given list
		this.container = new BeanItemContainer<BEANTYPE>(clazz);

		this.nullSelectionAllowed = nullSelectionAllowed;
		if (this.nullSelectionAllowed) {
			try {
				Constructor<BEANTYPE> constructorStr = clazz.getConstructor();
				this.nullSelectionElement = constructorStr.newInstance();
				this.container.addBean(this.nullSelectionElement);
			} catch (Exception e) {
				e.printStackTrace();
				// throw new UnsupportedOperationException(
				// "BEANTYPE needs to have a default (no arguments) constructor
				// if you want to use nullSelectionAllowed");
			}
		}

		this.container.addAll(selections);

		this.pageSize = pageSize;
		this.pages = (int) Math.ceil((double) this.container.size() / this.pageSize);
		this.inputPrompt = inputPrompt;

		this.itemIdPropertyId = itemIdPropertyId;
		this.itemCaptionPropertyId = itemCaptionPropertyId;

		this.editableRendererEnabled = editableRendererEnabled;

	}

	@Override
	protected ComboBoxState getState() {
		return (ComboBoxState) super.getState();
	}

	private ComboBoxServerRpc rpc = new ComboBoxServerRpc() {

		@Override
		public void getPage(int page, CellId id) {
			OptionsInfo info = new OptionsInfo(ComboBoxRenderer.this.pages, ComboBoxRenderer.this.inputPrompt,
					ComboBoxRenderer.this.nullSelectionAllowed);
			if (page == -1) {
				page = ComboBoxRenderer.this.container.indexOfId(getCellProperty(id).getValue())
						/ ComboBoxRenderer.this.pageSize;
			}
			info.setCurrentPage(page);

			// Get start id for page
			int fromIndex = ComboBoxRenderer.this.pageSize * page;
			int toIndex = fromIndex + ComboBoxRenderer.this.pageSize > ComboBoxRenderer.this.container.size()
					? ComboBoxRenderer.this.container.size() : fromIndex + ComboBoxRenderer.this.pageSize;

			List<BEANTYPE> elements = ComboBoxRenderer.this.container.getItemIds()
				.subList(fromIndex, toIndex);

			ArrayList<OptionElement> options = convertBeansToOptionElements(false, elements);
			getRpcProxy(ComboBoxClientRpc.class).updateOptions(info, options, id);
		}

		private ArrayList<OptionElement> convertBeansToOptionElements(boolean filterExists, List<BEANTYPE> elements) {
			ArrayList<OptionElement> options = new ArrayList<OptionElement>();

			for (BEANTYPE bean : elements) {
				if (ComboBoxRenderer.this.nullSelectionAllowed && bean == ComboBoxRenderer.this.nullSelectionElement) {
					options.add(new OptionElement(null, ""));
					continue;
				}
				Item item = ComboBoxRenderer.this.container.getItem(bean);
				final Property<?> idProperty = item.getItemProperty(ComboBoxRenderer.this.itemIdPropertyId);
				final Property<?> captionProperty = item.getItemProperty(ComboBoxRenderer.this.itemCaptionPropertyId);
				options.add(new OptionElement((Long) idProperty.getValue(), (String) captionProperty.getValue()));
			}
			return options;
		}

		@Override
		public void getFilterPage(String filterString, int page, CellId id) {
			if (filterString.isEmpty()) {
				getPage(-1, id);
				return;
			}

			Filterable filterable = (Filterable) ComboBoxRenderer.this.container;

			Filter filter = buildFilter(filterString, ComboBoxRenderer.this.filteringMode);

			boolean filterExists = filter != null;
			if (filterExists) {
				filterable.addContainerFilter(filter);
			}

			List<OptionElement> filteredResult = convertBeansToOptionElements(	filterExists,
																				ComboBoxRenderer.this.container
																					.getItemIds());

			int filteredPages = (int) Math.ceil((double) filteredResult.size() / ComboBoxRenderer.this.pageSize);

			OptionsInfo info = new OptionsInfo(filteredPages, ComboBoxRenderer.this.inputPrompt,
					ComboBoxRenderer.this.nullSelectionAllowed);
			if (page == -1) {
				page = filteredResult.indexOf(getCellProperty(id).getValue()) / ComboBoxRenderer.this.pageSize;
				// Inform which page we are sending.
				info.setCurrentPage(page);
				// getRpcProxy(ComboBoxClientRpc.class).setCurrentPage(page,
				// id);
			}

			int fromIndex = ComboBoxRenderer.this.pageSize * page;
			int toIndex = fromIndex + ComboBoxRenderer.this.pageSize > filteredResult.size() ? filteredResult.size()
					: fromIndex + ComboBoxRenderer.this.pageSize;

			if (filter != null) {
				filterable.removeContainerFilter(filter);
			}

			getRpcProxy(ComboBoxClientRpc.class).updateOptions(info, filteredResult.subList(fromIndex, toIndex), id);
		}

		/**
		 * Constructs a filter instance to use when using a Filterable container
		 * in the <code>ITEM_CAPTION_MODE_PROPERTY</code> mode.
		 * 
		 * Note that the client side implementation expects the filter string to
		 * apply to the item caption string it sees, so changing the behavior of
		 * this method can cause problems.
		 * 
		 * @param filterString
		 * @param filteringMode
		 * @return
		 */
		protected Filter buildFilter(String filterString, FilteringMode filteringMode) {
			Filter filter = null;

			if (null != filterString && !"".equals(filterString)) {
				switch (filteringMode) {
				case OFF:
					break;
				case STARTSWITH:
					filter = new SimpleStringFilter(ComboBoxRenderer.this.itemCaptionPropertyId, filterString, true,
							true);
					break;
				case CONTAINS:
					filter = new SimpleStringFilter(ComboBoxRenderer.this.itemCaptionPropertyId, filterString, true,
							false);
					break;
				}
			}

			return filter;
		}

		@Override
		public void filter(CellId id, String filterString) {
			Filterable filterable = (Filterable) ComboBoxRenderer.this.container;
			Filter filter = buildFilter(filterString, ComboBoxRenderer.this.filteringMode);

			boolean filterExists = filter != null;
			if (filterExists) {
				filterable.addContainerFilter(filter);
			}

			List<OptionElement> filteredResult = convertBeansToOptionElements(	filterExists,
																				ComboBoxRenderer.this.container
																					.getItemIds());

			int filteredPages = (int) Math.ceil((double) filteredResult.size() / ComboBoxRenderer.this.pageSize);
			OptionsInfo info = new OptionsInfo(filteredPages, ComboBoxRenderer.this.inputPrompt,
					ComboBoxRenderer.this.nullSelectionAllowed);

			if (filter != null) {
				filterable.removeContainerFilter(filter);
			}

			getRpcProxy(ComboBoxClientRpc.class).updateOptions(info, filteredResult, id);
		}

		@Override
		public void onValueChange(CellId id, OptionElement newValue) {
			Object itemId = getItemId(id.getRowId());
			Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

			Item row = getParentGrid().getContainerDataSource()
				.getItem(itemId);

			Property<BEANTYPE> cell = getCellProperty(id);

			BEANTYPE selectedBean = null;

			if (newValue != null && newValue.getId() != null) {

				for (BEANTYPE bean : ComboBoxRenderer.this.container.getItemIds()) {
					final Property<?> idProperty = ComboBoxRenderer.this.container.getItem(bean)
						.getItemProperty(ComboBoxRenderer.this.itemIdPropertyId);
					if (newValue.getId()
						.equals(idProperty.getValue())) {
						selectedBean = bean;
					}
				}

			}

			cell.setValue(selectedBean);

			fireItemEditEvent(itemId, row, columnPropertyId, selectedBean);
		}

		@Override
		public void onRender(CellId id) {
			if (ComboBoxRenderer.this.editableRendererEnabled != null) {
				boolean enable = EditableRendererUtil
					.isColumnComponentEnabled(	getItemId(id.getRowId()), getParentGrid(),
												ComboBoxRenderer.this.editableRendererEnabled);
				getRpcProxy(ComboBoxClientRpc.class).setEnabled(enable, id);
			}
		};

		private Property<BEANTYPE> getCellProperty(CellId id) {
			Object itemId = getItemId(id.getRowId());
			Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

			Item row = getParentGrid().getContainerDataSource()
				.getItem(itemId);

			return (Property<BEANTYPE>) row.getItemProperty(columnPropertyId);
		}
	};

}
