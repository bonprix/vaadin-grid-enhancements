package org.vaadin.grid.enhancements.cellrenderers;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.shared.ui.combobox.FilteringMode;

import org.vaadin.grid.cellrenderers.EditableRenderer;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.CellId;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionsInfo;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiselectClientRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiselectServerRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiselectState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Grid renderer that renders a multiselect ComboBox element
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxMultiselectRenderer<BEANTYPE> extends EditableRenderer<BEANTYPE> {

	private static final String SELECTED_PROPERTY = "selected";

	private final GeneratedPropertyContainer propertyContainer;
	private final BeanItemContainer<BEANTYPE> container;
	private Set<OptionElement> selectedOptions = null;

	private final int pageSize;
	private int pages;
	private final String inputPrompt;
	private final String selectAllText;
	private final String deselectAllText;

	private final String originalItemProperty;
	private final String itemIdPropertyId;
	private final String itemCaptionPropertyId;
	private FilteringMode filteringMode = FilteringMode.CONTAINS;
	private String lastFilterString;

	private Comparator<? super OptionElement> comparator = new Comparator<OptionElement>() {

		@Override
		public int compare(OptionElement o1, OptionElement o2) {
			boolean o1Selected = ComboBoxMultiselectRenderer.this.selectedOptions.contains(o1);
			boolean o2Selected = ComboBoxMultiselectRenderer.this.selectedOptions.contains(o2);

			if (o1Selected && o2Selected) {
				return o1	.getName()
							.compareTo(o2.getName());
			}

			if (o1Selected) {
				return -1;
			}

			if (o2Selected) {
				return 1;
			}

			return o1	.getName()
						.compareTo(o2.getName());
		}
	};
	protected boolean sortingNeeded = true;
	protected ArrayList<OptionElement> sortedOptions;

	public ComboBoxMultiselectRenderer(final Class<BEANTYPE> clazz, List<BEANTYPE> selections, String itemIdPropertyId,
			String itemCaptionPropertyId, int pageSize, String inputPrompt, String selectAllText,
			String deselectAllText, String originalItemProperty) {
		super(clazz);

		registerRpc(this.rpc);
		// Add items to internal list so we don't expose ourselves to changes in
		// the given list
		this.container = new BeanItemContainer<BEANTYPE>(clazz);
		this.container.addAll(selections);

		this.propertyContainer = new GeneratedPropertyContainer(this.container);

		this.pageSize = pageSize;
		this.pages = (int) Math.ceil((double) this.container.size() / this.pageSize);
		this.inputPrompt = inputPrompt;
		this.selectAllText = selectAllText;
		this.deselectAllText = deselectAllText;

		this.itemIdPropertyId = itemIdPropertyId;
		this.itemCaptionPropertyId = itemCaptionPropertyId;
		this.originalItemProperty = originalItemProperty;
	}

	@Override
	protected ComboBoxMultiselectState getState() {
		return (ComboBoxMultiselectState) super.getState();
	}

	private ComboBoxMultiselectServerRpc rpc = new ComboBoxMultiselectServerRpc() {

		@Override
		public void getPage(int page, boolean skipBlur, CellId id) {
			ComboBoxMultiselectRenderer.this.lastFilterString = null;

			if (ComboBoxMultiselectRenderer.this.selectedOptions == null) {
				return;
			}

			OptionsInfo info = new OptionsInfo(ComboBoxMultiselectRenderer.this.pages,
					ComboBoxMultiselectRenderer.this.inputPrompt, ComboBoxMultiselectRenderer.this.selectAllText,
					ComboBoxMultiselectRenderer.this.deselectAllText);
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

			List<BEANTYPE> elements = ComboBoxMultiselectRenderer.this.container.getItemIds();

			if (ComboBoxMultiselectRenderer.this.sortedOptions == null
					|| ComboBoxMultiselectRenderer.this.sortingNeeded) {
				ComboBoxMultiselectRenderer.this.sortedOptions = convertBeansToComboBoxMultiselectOptions(elements);
				Collections.sort(	ComboBoxMultiselectRenderer.this.sortedOptions,
									ComboBoxMultiselectRenderer.this.comparator);

				ComboBoxMultiselectRenderer.this.sortingNeeded = false;
			}
			List<OptionElement> options = ComboBoxMultiselectRenderer.this.sortedOptions.subList(fromIndex, toIndex);
			getRpcProxy(ComboBoxMultiselectClientRpc.class).updateOptions(info, options, skipBlur, id);
		}

		private ArrayList<OptionElement> convertBeansToComboBoxMultiselectOptions(Collection<BEANTYPE> elements) {
			ArrayList<OptionElement> options = new ArrayList<OptionElement>();
			for (BEANTYPE bean : elements) {
				Item item = ComboBoxMultiselectRenderer.this.container.getItem(bean);
				final Property<?> idProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemIdPropertyId);
				final Property<?> captionProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemCaptionPropertyId);
				options.add(new OptionElement((Long) idProperty.getValue(), (String) captionProperty.getValue()));
			}
			return options;
		}

		@Override
		public void getFilterPage(String filterString, int page, boolean skipBlur, CellId id) {
			ComboBoxMultiselectRenderer.this.lastFilterString = filterString;

			if (ComboBoxMultiselectRenderer.this.selectedOptions == null) {
				return;
			}

			if (filterString.isEmpty()) {
				getPage(-1, skipBlur, id);
				return;
			}

			Filterable filterable = (Filterable) ComboBoxMultiselectRenderer.this.container;

			Filter filter = buildFilter(filterString, ComboBoxMultiselectRenderer.this.filteringMode);

			if (filter != null) {
				filterable.addContainerFilter(filter);
			}

			List<OptionElement> filteredResult = convertBeansToComboBoxMultiselectOptions(ComboBoxMultiselectRenderer.this.container.getItemIds());

			Collections.sort(filteredResult, ComboBoxMultiselectRenderer.this.comparator);

			int filteredPages = (int) Math.ceil((double) filteredResult.size()
					/ ComboBoxMultiselectRenderer.this.pageSize);
			OptionsInfo info = new OptionsInfo(filteredPages, ComboBoxMultiselectRenderer.this.inputPrompt,
					ComboBoxMultiselectRenderer.this.selectAllText, ComboBoxMultiselectRenderer.this.deselectAllText);

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

			getRpcProxy(ComboBoxMultiselectClientRpc.class).updateOptions(	info,
																			filteredResult.subList(fromIndex, toIndex),
																			skipBlur, id);
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
		public void filter(CellId id, String filterString, boolean skipBlur) {
			Filterable filterable = (Filterable) ComboBoxMultiselectRenderer.this.container;
			Filter filter = buildFilter(filterString, ComboBoxMultiselectRenderer.this.filteringMode);

			if (filter != null) {
				filterable.addContainerFilter(filter);
			}

			List<OptionElement> filteredResult = convertBeansToComboBoxMultiselectOptions(ComboBoxMultiselectRenderer.this.container.getItemIds());

			int filteredPages = (int) Math.ceil((double) filteredResult.size()
					/ ComboBoxMultiselectRenderer.this.pageSize);
			OptionsInfo info = new OptionsInfo(filteredPages, ComboBoxMultiselectRenderer.this.inputPrompt,
					ComboBoxMultiselectRenderer.this.selectAllText, ComboBoxMultiselectRenderer.this.deselectAllText);

			if (filter != null) {
				filterable.removeContainerFilter(filter);
			}

			getRpcProxy(ComboBoxMultiselectClientRpc.class).updateOptions(info, filteredResult, skipBlur, id);
		}

		@Override
		public void onValueSetChange(CellId id, Set<OptionElement> newValues) {
			Object itemId = getItemId(id.getRowId());
			Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

			Item row = getParentGrid()	.getContainerDataSource()
										.getItem(itemId);

			Property<Set<BEANTYPE>> cell = getCellProperty(id);

			Set<BEANTYPE> selectedBeans = new HashSet<BEANTYPE>();

			for (BEANTYPE bean : ComboBoxMultiselectRenderer.this.container.getItemIds()) {
				final Property<?> idProperty = ComboBoxMultiselectRenderer.this.container	.getItem(bean)
																							.getItemProperty(ComboBoxMultiselectRenderer.this.itemIdPropertyId);
				for (OptionElement newValue : newValues) {
					if (newValue.getId() != null && newValue.getId()
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

			return (Property<Set<BEANTYPE>>) row.getItemProperty(ComboBoxMultiselectRenderer.this.originalItemProperty);
		}

		@Override
		public void onRender(CellId id) {
			Set<BEANTYPE> value = getCellProperty(id).getValue();

			Set<OptionElement> selected = new HashSet<OptionElement>();

			if (value != null) {
				for (BEANTYPE bean : ComboBoxMultiselectRenderer.this.container.getItemIds()) {
					if (value.contains(bean)) {
						Item item = ComboBoxMultiselectRenderer.this.container.getItem(bean);
						final Property<?> idProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemIdPropertyId);
						final Property<?> captionProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemCaptionPropertyId);
						selected.add(new OptionElement((Long) idProperty.getValue(),
								(String) captionProperty.getValue()));
					}
				}
			}

			ComboBoxMultiselectRenderer.this.selectedOptions = selected;

			getRpcProxy(ComboBoxMultiselectClientRpc.class).updateSelectedOptions(	ComboBoxMultiselectRenderer.this.selectedOptions,
																					id, false);
		}

		@Override
		public void setSortingNeeded(boolean sortingNeeded) {
			ComboBoxMultiselectRenderer.this.sortingNeeded = sortingNeeded;
		}

		@Override
		public void selectAll(CellId id) {
			Object itemId = getItemId(id.getRowId());
			Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

			Item row = getParentGrid()	.getContainerDataSource()
										.getItem(itemId);

			Property<Set<BEANTYPE>> cell = getCellProperty(id);

			ComboBoxMultiselectRenderer.this.sortingNeeded = true;

			Filterable filterable = (Filterable) ComboBoxMultiselectRenderer.this.container;
			Filter filter = buildFilter(ComboBoxMultiselectRenderer.this.lastFilterString,
										ComboBoxMultiselectRenderer.this.filteringMode);

			if (filter != null) {
				filterable.addContainerFilter(filter);
			}

			Set<OptionElement> selected = new HashSet<OptionElement>();
			for (BEANTYPE bean : ComboBoxMultiselectRenderer.this.container.getItemIds()) {
				Item item = ComboBoxMultiselectRenderer.this.container.getItem(bean);
				final Property<?> idProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemIdPropertyId);
				final Property<?> captionProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemCaptionPropertyId);
				selected.add(new OptionElement((Long) idProperty.getValue(), (String) captionProperty.getValue()));
			}
			ComboBoxMultiselectRenderer.this.selectedOptions.addAll(selected);

			Set<BEANTYPE> selectedBeans = cell.getValue();
			selectedBeans.addAll(ComboBoxMultiselectRenderer.this.container.getItemIds());

			if (filter != null) {
				filterable.removeContainerFilter(filter);
			}

			cell.setValue(selectedBeans);
			fireItemEditEvent(itemId, row, columnPropertyId, selectedBeans);

			getRpcProxy(ComboBoxMultiselectClientRpc.class).updateSelectedOptions(	ComboBoxMultiselectRenderer.this.selectedOptions,
																					id, true);
		}

		@Override
		public void deselectAll(CellId id) {
			// TODO make this for fireItem simpler and own function
			Object itemId = getItemId(id.getRowId());
			Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

			Item row = getParentGrid()	.getContainerDataSource()
										.getItem(itemId);

			Property<Set<BEANTYPE>> cell = getCellProperty(id);

			ComboBoxMultiselectRenderer.this.sortingNeeded = true;

			Filterable filterable = (Filterable) ComboBoxMultiselectRenderer.this.container;
			Filter filter = buildFilter(ComboBoxMultiselectRenderer.this.lastFilterString,
										ComboBoxMultiselectRenderer.this.filteringMode);

			if (filter != null) {
				filterable.addContainerFilter(filter);
			}

			Set<OptionElement> selected = new HashSet<OptionElement>();
			for (BEANTYPE bean : ComboBoxMultiselectRenderer.this.container.getItemIds()) {
				Item item = ComboBoxMultiselectRenderer.this.container.getItem(bean);
				final Property<?> idProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemIdPropertyId);
				final Property<?> captionProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemCaptionPropertyId);
				selected.add(new OptionElement((Long) idProperty.getValue(), (String) captionProperty.getValue()));
			}
			ComboBoxMultiselectRenderer.this.selectedOptions.removeAll(selected);

			Set<BEANTYPE> selectedBeans = cell.getValue();
			selectedBeans.removeAll(ComboBoxMultiselectRenderer.this.container.getItemIds());

			if (filter != null) {
				filterable.removeContainerFilter(filter);
			}

			cell.setValue(selectedBeans);
			fireItemEditEvent(itemId, row, columnPropertyId, cell.getValue());

			getRpcProxy(ComboBoxMultiselectClientRpc.class).updateSelectedOptions(	ComboBoxMultiselectRenderer.this.selectedOptions,
																					id, true);
		}
	};

}
