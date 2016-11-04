package org.vaadin.grid.enhancements.cellrenderers;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.shared.ui.combobox.FilteringMode;

import org.vaadin.grid.cellrenderers.EditableRenderer;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.CellId;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.OptionsInfo;
import org.vaadin.grid.enhancements.client.cellrenderers.multiselect.ComboBoxMultiselectOption;
import org.vaadin.grid.enhancements.client.cellrenderers.multiselect.MultiSelectClientRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.multiselect.MultiSelectServerRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.multiselect.MultiSelectState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Grid renderer that renders a multiselect ComboBox element
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxMultiselectRenderer<BEANTYPE> extends EditableRenderer<Set<BEANTYPE>> {

	private final BeanItemContainer<BEANTYPE> container;

	private int pageSize = 5;
	private int pages;

	private String itemIdPropertyId;
	private String itemCaptionPropertyId;
	private FilteringMode filteringMode = FilteringMode.CONTAINS;

	public ComboBoxMultiselectRenderer(final Class<BEANTYPE> clazz, List<BEANTYPE> selections, String itemIdPropertyId,
			String itemCaptionPropertyId) {
		super((Class<Set<BEANTYPE>>) new HashSet<BEANTYPE>().getClass());

		registerRpc(this.rpc);
		// Add items to internal list so we don't expose ourselves to changes in
		// the given list
		this.container = new BeanItemContainer<BEANTYPE>(clazz);
		this.container.addAll(selections);

		this.pages = (int) Math.ceil((double) this.container.size() / this.pageSize);

		this.itemIdPropertyId = itemIdPropertyId;
		this.itemCaptionPropertyId = itemCaptionPropertyId;
	}

	/**
	 * Set the amount of items to be shown in the dropdown.
	 *
	 * @param pageSize
	 *            Amount of items to show on page
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	protected MultiSelectState getState() {
		return (MultiSelectState) super.getState();
	}

	private MultiSelectServerRpc rpc = new MultiSelectServerRpc() {

		@Override
		public void getPage(int page, CellId id) {
			OptionsInfo info = new OptionsInfo(ComboBoxMultiselectRenderer.this.pages);
			if (page == -1) {
				page = ComboBoxMultiselectRenderer.this.container.indexOfId(getCellProperty(id).getValue())
						/ ComboBoxMultiselectRenderer.this.pageSize;
				// Inform which page we are sending.
				info.setCurrentPage(page);
				// getRpcProxy(MultiSelectClientRpc.class).setCurrentPage(page,
				// id);
			}

			// Get start id for page
			int fromIndex = ComboBoxMultiselectRenderer.this.pageSize * page;
			int toIndex = fromIndex
					+ ComboBoxMultiselectRenderer.this.pageSize > ComboBoxMultiselectRenderer.this.container.size()
							? ComboBoxMultiselectRenderer.this.container.size()
							: fromIndex + ComboBoxMultiselectRenderer.this.pageSize;

			List<BEANTYPE> elements = ComboBoxMultiselectRenderer.this.container.getItemIds()
																				.subList(fromIndex, toIndex);
			ArrayList<ComboBoxMultiselectOption> options = convertBeansToComboBoxMultiselectOptions(elements);
			getRpcProxy(MultiSelectClientRpc.class).updateOptions(info, options, id);
		}

		private ArrayList<ComboBoxMultiselectOption> convertBeansToComboBoxMultiselectOptions(List<BEANTYPE> elements) {
			ArrayList<ComboBoxMultiselectOption> options = new ArrayList<ComboBoxMultiselectOption>();
			for (BEANTYPE bean : elements) {
				Item item = ComboBoxMultiselectRenderer.this.container.getItem(bean);
				final Property<?> idProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemIdPropertyId);
				final Property<?> captionProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemCaptionPropertyId);
				options.add(new ComboBoxMultiselectOption((Long) idProperty.getValue(),
						(String) captionProperty.getValue()));
			}
			return options;
		}

		@Override
		public void getFilterPage(String filterString, int page, CellId id) {
			if (filterString.isEmpty()) {
				getPage(-1, id);
				return;
			}

			Filterable filterable = (Filterable) ComboBoxMultiselectRenderer.this.container;

			Filter filter = buildFilter(filterString, ComboBoxMultiselectRenderer.this.filteringMode);

			if (filter != null) {
				filterable.addContainerFilter(filter);
			}

			List<ComboBoxMultiselectOption> filteredResult = convertBeansToComboBoxMultiselectOptions(ComboBoxMultiselectRenderer.this.container.getItemIds());

			int filteredPages = (int) Math.ceil((double) filteredResult.size()
					/ ComboBoxMultiselectRenderer.this.pageSize);
			OptionsInfo info = new OptionsInfo(filteredPages);

			if (page == -1) {
				page = filteredResult.indexOf(getCellProperty(id).getValue())
						/ ComboBoxMultiselectRenderer.this.pageSize;
				// Inform which page we are sending.
				info.setCurrentPage(page);
				// getRpcProxy(MultiSelectClientRpc.class).setCurrentPage(page,
				// id);
			}

			int fromIndex = ComboBoxMultiselectRenderer.this.pageSize * page;
			int toIndex = fromIndex + ComboBoxMultiselectRenderer.this.pageSize > filteredResult.size()
					? filteredResult.size() : fromIndex + ComboBoxMultiselectRenderer.this.pageSize;

			if (filter != null) {
				filterable.removeContainerFilter(filter);
			}

			getRpcProxy(MultiSelectClientRpc.class).updateOptions(info, filteredResult.subList(fromIndex, toIndex), id);
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
					filter = new SimpleStringFilter(ComboBoxMultiselectRenderer.this.itemCaptionPropertyId,
							filterString, true, true);
					break;
				case CONTAINS:
					filter = new SimpleStringFilter(ComboBoxMultiselectRenderer.this.itemCaptionPropertyId,
							filterString, true, false);
					break;
				}
			}
			return filter;
		}

		@Override
		public void filter(CellId id, String filterString) {
			Filterable filterable = (Filterable) ComboBoxMultiselectRenderer.this.container;
			Filter filter = buildFilter(filterString, ComboBoxMultiselectRenderer.this.filteringMode);

			if (filter != null) {
				filterable.addContainerFilter(filter);
			}

			List<ComboBoxMultiselectOption> filteredResult = convertBeansToComboBoxMultiselectOptions(ComboBoxMultiselectRenderer.this.container.getItemIds());

			int filteredPages = (int) Math.ceil((double) filteredResult.size()
					/ ComboBoxMultiselectRenderer.this.pageSize);
			OptionsInfo info = new OptionsInfo(filteredPages);

			if (filter != null) {
				filterable.removeContainerFilter(filter);
			}

			getRpcProxy(MultiSelectClientRpc.class).updateOptions(info, filteredResult, id);
		}

		@Override
		public void onValueSetChange(CellId id, Set<ComboBoxMultiselectOption> newValues) {
			Object itemId = getItemId(id.getRowId());
			Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

			Item row = getParentGrid()	.getContainerDataSource()
										.getItem(itemId);

			Property<Set<BEANTYPE>> cell = getCellProperty(id);

			HashSet<BEANTYPE> selectedBeans = new HashSet<BEANTYPE>();

			for (BEANTYPE bean : ComboBoxMultiselectRenderer.this.container.getItemIds()) {
				final Property<?> idProperty = ComboBoxMultiselectRenderer.this.container	.getItem(bean)
																							.getItemProperty(ComboBoxMultiselectRenderer.this.itemIdPropertyId);
				for (ComboBoxMultiselectOption newValue : newValues) {
					if (newValue.getId()
								.equals(idProperty.getValue())) {
						selectedBeans.add(bean);
					}
				}

			}

			cell.setValue(selectedBeans);

			fireItemEditEvent(itemId, row, columnPropertyId, selectedBeans);
		}

		private Property<Set<BEANTYPE>> getCellProperty(CellId id) {
			Object itemId = getItemId(id.getRowId());
			Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

			Item row = getParentGrid()	.getContainerDataSource()
										.getItem(itemId);

			return (Property<Set<BEANTYPE>>) row.getItemProperty(columnPropertyId);
		}
	};

}
