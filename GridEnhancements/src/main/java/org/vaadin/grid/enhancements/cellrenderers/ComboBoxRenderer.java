package org.vaadin.grid.enhancements.cellrenderers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.vaadin.grid.enhancements.filter.ItemCaptionGeneratorFilter;

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

import elemental.json.JsonValue;

/**
 * Grid renderer that renders a ComboBox element
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxRenderer<BEANTYPE> extends EditableRenderer<BEANTYPE> {

    private final BeanItemContainer<BEANTYPE> container;

    private final int pageSize;
    private final int pages;
    private final String inputPrompt;

    private final String itemIdPropertyId;
    private final String itemCaptionPropertyId;
    private final Function<BEANTYPE, String> itemCaptionGenerator;
    private final FilteringMode filteringMode = FilteringMode.CONTAINS;
    private final boolean nullSelectionAllowed;

    private final EditableRendererEnabled editableRendererEnabled;

    private BEANTYPE nullSelectionElement;

    public ComboBoxRenderer(final Class<BEANTYPE> clazz, final List<BEANTYPE> selections, final String itemIdPropertyId, final String itemCaptionPropertyId,
            final int pageSize, final String inputPrompt, final boolean nullSelectionAllowed, final boolean showOnlyNotUsed,
            final EditableRendererEnabled editableRendererEnabled, final Function<BEANTYPE, String> itemCaptionGenerator) {
        super(clazz);

        registerRpc(this.rpc);

        getState().showOnlyNotUsed = showOnlyNotUsed;

        // Add items to internal list so we don't expose ourselves to changes in
        // the given list
        this.container = new BeanItemContainer<BEANTYPE>(clazz);

        this.nullSelectionAllowed = nullSelectionAllowed;
        if (this.nullSelectionAllowed) {
            try {
                final Constructor<BEANTYPE> constructorStr = clazz.getConstructor();
                this.nullSelectionElement = constructorStr.newInstance();
                this.container.addBean(this.nullSelectionElement);
            }
            catch (final Exception e) {
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

    private final ComboBoxServerRpc rpc = new ComboBoxServerRpc() {

        @Override
        public void getPage(int page, final CellId id) {
            List<BEANTYPE> elements = ComboBoxRenderer.this.container.getItemIds();

            if (getState().showOnlyNotUsed) {
                elements = getShowOnlyNotUsed(id);
            }

            final OptionsInfo info = new OptionsInfo(roundUp(elements.size(), ComboBoxRenderer.this.pageSize), ComboBoxRenderer.this.inputPrompt,
                    ComboBoxRenderer.this.nullSelectionAllowed);
            if (page == -1) {
                page = elements.indexOf(getCellProperty(id).getValue()) / ComboBoxRenderer.this.pageSize;
            }
            info.setCurrentPage(page);

            // Get start id for page
            final int fromIndex = ComboBoxRenderer.this.pageSize * page;
            final int toIndex = fromIndex + ComboBoxRenderer.this.pageSize > elements.size() ? elements.size() : fromIndex + ComboBoxRenderer.this.pageSize;

            elements = elements.subList(fromIndex, toIndex);

            final ArrayList<OptionElement> options = convertBeansToOptionElements(elements);
            getRpcProxy(ComboBoxClientRpc.class).updateOptions(info, options, id);
        }

        private int roundUp(final int num, final int divisor) {
            return (num + divisor - 1) / divisor;
        }

        private List<BEANTYPE> getShowOnlyNotUsed(final CellId id) {
            final List<BEANTYPE> onlyNotUsed = new ArrayList<BEANTYPE>(ComboBoxRenderer.this.container.getItemIds());

            final Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            final Object itemId = getItemId(id.getRowId());

            Indexed indexed = getParentGrid().getContainerDataSource();

            final Item item = indexed.getItem(itemId);
            item.getItemProperty(columnPropertyId)
                .getValue();

            Collection<?> allItemIds = indexed.getItemIds();
            if (getState().showOnlyNotUsed) {
                if (indexed instanceof GeneratedPropertyContainer) {
                    indexed = ((GeneratedPropertyContainer) indexed).getWrappedContainer();
                }

                if (indexed instanceof AbstractInMemoryContainer) {
                    try {
                        final Method m = AbstractInMemoryContainer.class.getDeclaredMethod("getAllItemIds");
                        m.setAccessible(true);
                        allItemIds = (List<Object>) m.invoke(indexed);
                    }
                    catch (final NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    catch (final SecurityException e) {
                        e.printStackTrace();
                    }
                    catch (final IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    catch (final IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                    catch (final InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.err.println("Can't filter in combobox correctly with show only not used with not AbstractInMemoryContainer in grid");
                }
            }

            for (final Object otherItemId : allItemIds) {
                final Item otherItem = getParentGrid().getContainerDataSource()
                    .getItem(otherItemId);

                if (!otherItem.equals(item)) {
                    final Property<BEANTYPE> itemProperty = otherItem.getItemProperty(columnPropertyId);
                    final BEANTYPE value = itemProperty.getValue();
                    onlyNotUsed.remove(value);
                }
            }

            return onlyNotUsed;
        }

        private ArrayList<OptionElement> convertBeansToOptionElements(final List<BEANTYPE> elements) {
            final ArrayList<OptionElement> options = new ArrayList<OptionElement>();

            for (final BEANTYPE bean : elements) {
                options.add(getOptionElement(bean));
            }
            return options;
        }

        @Override
        public void getFilterPage(final String filterString, int page, final CellId id) {
            if (filterString.isEmpty()) {
                getPage(-1, id);
                return;
            }

            final Filterable filterable = ComboBoxRenderer.this.container;

            final Filter filter = buildFilter(filterString, ComboBoxRenderer.this.filteringMode);

            final boolean filterExists = filter != null;
            if (filterExists) {
                filterable.addContainerFilter(filter);
            }

            List<BEANTYPE> elements = ComboBoxRenderer.this.container.getItemIds();

            if (getState().showOnlyNotUsed) {
                elements = getShowOnlyNotUsed(id);
            }

            final List<OptionElement> filteredResult = convertBeansToOptionElements(elements);

            final int filteredPages = (int) Math.ceil((double) filteredResult.size() / ComboBoxRenderer.this.pageSize);

            final OptionsInfo info = new OptionsInfo(filteredPages, ComboBoxRenderer.this.inputPrompt, ComboBoxRenderer.this.nullSelectionAllowed);
            if (page == -1) {
                page = filteredResult.indexOf(getCellProperty(id).getValue()) / ComboBoxRenderer.this.pageSize;
                // Inform which page we are sending.
                info.setCurrentPage(page);
                // getRpcProxy(ComboBoxClientRpc.class).setCurrentPage(page,
                // id);
            }

            final int fromIndex = ComboBoxRenderer.this.pageSize * page;
            final int toIndex = fromIndex + ComboBoxRenderer.this.pageSize > filteredResult.size() ? filteredResult.size()
                    : fromIndex + ComboBoxRenderer.this.pageSize;

            if (filter != null) {
                filterable.removeContainerFilter(filter);
            }

            getRpcProxy(ComboBoxClientRpc.class).updateOptions(info, filteredResult.subList(fromIndex, toIndex), id);
        }

        /**
         * Constructs a filter instance to use when using a Filterable container in the <code>ITEM_CAPTION_MODE_PROPERTY</code> mode.
         *
         * Note that the client side implementation expects the filter string to apply to the item caption string it sees, so changing the behavior of this
         * method can cause problems.
         *
         * @param filterString
         * @param filteringMode
         * @return
         */
        protected Filter buildFilter(final String filterString, final FilteringMode filteringMode) {
            Filter filter = null;

            if (null != filterString && !"".equals(filterString)) {
                switch (filteringMode) {
                    case OFF:
                        break;
                    case STARTSWITH:
                        if (ComboBoxRenderer.this.itemCaptionGenerator == null) {
                            filter = new SimpleStringFilter(ComboBoxRenderer.this.itemCaptionPropertyId, filterString, true, true);
                        }
                        else {
                            filter = new ItemCaptionGeneratorFilter<BEANTYPE>(ComboBoxRenderer.this.itemCaptionGenerator, filterString, true, true,
                                    ComboBoxRenderer.this.container);
                        }
                        break;
                    case CONTAINS:
                        if (ComboBoxRenderer.this.itemCaptionGenerator == null) {
                            filter = new SimpleStringFilter(ComboBoxRenderer.this.itemCaptionPropertyId, filterString, true, false);
                        }
                        else {
                            filter = new ItemCaptionGeneratorFilter<BEANTYPE>(ComboBoxRenderer.this.itemCaptionGenerator, filterString, true, false,
                                    ComboBoxRenderer.this.container);
                        }
                        break;
                }
            }

            return filter;
        }

        @Override
        public void filter(final CellId id, final String filterString) {
            final Filterable filterable = ComboBoxRenderer.this.container;

            final Filter filter = buildFilter(filterString, ComboBoxRenderer.this.filteringMode);

            final boolean filterExists = filter != null;
            if (filterExists) {
                filterable.addContainerFilter(filter);
            }

            List<BEANTYPE> elements = ComboBoxRenderer.this.container.getItemIds();

            if (getState().showOnlyNotUsed) {
                elements = getShowOnlyNotUsed(id);
            }

            final List<OptionElement> filteredResult = convertBeansToOptionElements(elements);

            final int filteredPages = (int) Math.ceil((double) filteredResult.size() / ComboBoxRenderer.this.pageSize);
            final OptionsInfo info = new OptionsInfo(filteredPages, ComboBoxRenderer.this.inputPrompt, ComboBoxRenderer.this.nullSelectionAllowed);

            if (filter != null) {
                filterable.removeContainerFilter(filter);
            }

            getRpcProxy(ComboBoxClientRpc.class).updateOptions(info, filteredResult, id);
        }

        @Override
        public void onValueChange(final CellId id, final OptionElement newValue) {
            final Object itemId = getItemId(id.getRowId());
            final Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            final Item row = getParentGrid().getContainerDataSource()
                .getItem(itemId);

            final Property<BEANTYPE> cell = getCellProperty(id);

            BEANTYPE selectedBean = null;

            if (newValue != null && newValue.getId() != null) {

                for (final BEANTYPE bean : ComboBoxRenderer.this.container.getItemIds()) {
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
        public void onRender(final CellId id) {
            if (ComboBoxRenderer.this.editableRendererEnabled != null) {
                final boolean enable = EditableRendererUtil.isColumnComponentEnabled(getItemId(id.getRowId()), getParentGrid(),
                                                                                     ComboBoxRenderer.this.editableRendererEnabled);
                getRpcProxy(ComboBoxClientRpc.class).setEnabled(enable, id);
            }
        };

        private Property<BEANTYPE> getCellProperty(final CellId id) {
            final Object itemId = getItemId(id.getRowId());
            final Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            final Item row = getParentGrid().getContainerDataSource()
                .getItem(itemId);

            return row.getItemProperty(columnPropertyId);
        }

    };

    public void replaceBeans(final List<BEANTYPE> beans) {
        this.container.removeAllItems();
        this.container.addAll(beans);
    }

    @Override
    public JsonValue encode(final BEANTYPE bean) {
        return encode(getOptionElement(bean), OptionElement.class);
    }

    private OptionElement getOptionElement(final BEANTYPE bean) {
        if (bean == null) {
            return new OptionElement();
        }

        if (ComboBoxRenderer.this.nullSelectionAllowed && bean == ComboBoxRenderer.this.nullSelectionElement) {
            return new OptionElement(null, "");
        }

        final Item item = ComboBoxRenderer.this.container.getItem(bean);
        final Property<?> idProperty = item.getItemProperty(ComboBoxRenderer.this.itemIdPropertyId);

        final OptionElement optionElement = new OptionElement((Long) idProperty.getValue());

        if (ComboBoxRenderer.this.itemCaptionGenerator != null) {
            optionElement.setName(ComboBoxRenderer.this.itemCaptionGenerator.apply(bean));
        }
        else {
            final Property<?> captionProperty = item.getItemProperty(ComboBoxRenderer.this.itemCaptionPropertyId);
            optionElement.setName((String) captionProperty.getValue());
        }
        return optionElement;
    }

}
