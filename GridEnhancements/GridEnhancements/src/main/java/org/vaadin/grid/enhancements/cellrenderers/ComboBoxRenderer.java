package org.vaadin.grid.enhancements.cellrenderers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractInMemoryContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
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
	private Consumer<BEANTYPE> itemCaptionGenerator;
	private FilteringMode filteringMode = FilteringMode.CONTAINS;
	private final boolean nullSelectionAllowed;

	private EditableRendererEnabled editableRendererEnabled;

	private BEANTYPE nullSelectionElement;

	public ComboBoxRenderer(final Class<BEANTYPE> clazz, final List<BEANTYPE> selections, String itemIdPropertyId,
			String itemCaptionPropertyId, int pageSize, String inputPrompt, boolean nullSelectionAllowed,
			boolean showOnlyNotUsed, EditableRendererEnabled editableRendererEnabled
			,final Consumer<BEANTYPE> itemCaptionGenerator
			) {
		super(clazz);

		registerRpc(this.rpc);

		getState().showOnlyNotUsed = showOnlyNotUsed;

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
        this.itemCaptionGenerator = itemCaptionGenerator;
		this.editableRendererEnabled = editableRendererEnabled;

	}

	@Override
	protected ComboBoxState getState() {
		return (ComboBoxState) super.getState();
	}

	private ComboBoxServerRpc rpc = new ComboBoxServerRpc() {

		@Override
		public void getPage(int page, CellId id) {
			List<BEANTYPE> elements = ComboBoxRenderer.this.container.getItemIds();

			if (getState().showOnlyNotUsed) {
				elements = getShowOnlyNotUsed(id);
			}

			OptionsInfo info = new OptionsInfo(roundUp(elements.size(), ComboBoxRenderer.this.pageSize),
					ComboBoxRenderer.this.inputPrompt, ComboBoxRenderer.this.nullSelectionAllowed);
			if (page == -1) {
				page = elements.indexOf(getCellProperty(id).getValue()) / ComboBoxRenderer.this.pageSize;
			}
			info.setCurrentPage(page);

			// Get start id for page
			int fromIndex = ComboBoxRenderer.this.pageSize * page;
			int toIndex = fromIndex + ComboBoxRenderer.this.pageSize > elements.size() ? elements.size()
					: fromIndex + ComboBoxRenderer.this.pageSize;

			elements = elements.subList(fromIndex, toIndex);

			ArrayList<OptionElement> options = convertBeansToOptionElements(false, elements);
			getRpcProxy(ComboBoxClientRpc.class).updateOptions(info, options, id);
		}

		private int roundUp(int num, int divisor) {
			return (num + divisor - 1) / divisor;
		}

		private List<BEANTYPE> getShowOnlyNotUsed(CellId id) {
			List<BEANTYPE> onlyNotUsed = new ArrayList<BEANTYPE>(ComboBoxRenderer.this.container.getItemIds());

			Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

			Object itemId = getItemId(id.getRowId());

			Indexed indexed = getParentGrid().getContainerDataSource();

			Item item = indexed.getItem(itemId);
			item.getItemProperty(columnPropertyId)
				.getValue();

			Collection<?> allItemIds = indexed.getItemIds();
			if (getState().showOnlyNotUsed) {
				if (indexed instanceof GeneratedPropertyContainer) {
					indexed = ((GeneratedPropertyContainer) indexed).getWrappedContainer();
				}

				if (indexed instanceof AbstractInMemoryContainer) {
					try {
						Method m = AbstractInMemoryContainer.class.getDeclaredMethod("getAllItemIds");
						m.setAccessible(true);
						allItemIds = (List<Object>) m.invoke(indexed);
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				} else {
					System.err
						.println("Can't filter in combobox correctly with show only not used with not AbstractInMemoryContainer in grid");
				}
			}

			for (Object otherItemId : allItemIds) {
				Item otherItem = getParentGrid().getContainerDataSource()
					.getItem(otherItemId);

				if (!otherItem.equals(item)) {
					Property<BEANTYPE> itemProperty = otherItem.getItemProperty(columnPropertyId);
					BEANTYPE value = itemProperty.getValue();
					onlyNotUsed.remove(value);
				}
			}

			return onlyNotUsed;
		}

		private ArrayList<OptionElement> convertBeansToOptionElements(boolean filterExists, List<BEANTYPE> elements) {
			ArrayList<OptionElement> options = new ArrayList<OptionElement>();

			for (BEANTYPE bean : elements) {
			    if(ComboBoxRenderer.this.itemCaptionGenerator != null)
			        itemCaptionGenerator.accept(bean);
			    
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

			List<BEANTYPE> elements = ComboBoxRenderer.this.container.getItemIds();

			if (getState().showOnlyNotUsed) {
				elements = getShowOnlyNotUsed(id);
			}

			List<OptionElement> filteredResult = convertBeansToOptionElements(filterExists, elements);

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

			List<BEANTYPE> elements = ComboBoxRenderer.this.container.getItemIds();

			if (getState().showOnlyNotUsed) {
				elements = getShowOnlyNotUsed(id);
			}

			List<OptionElement> filteredResult = convertBeansToOptionElements(filterExists, elements);

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

	public void replaceBeans(List<BEANTYPE> beans) {
		this.container.removeAllItems();
		this.container.addAll(beans);
	}

}
