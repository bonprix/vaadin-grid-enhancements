package org.vaadin.grid.enhancements.cellrenderers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.vaadin.grid.cellrenderers.EditableRenderer;
import org.vaadin.grid.cellrenderers.client.editable.common.CellId;
import org.vaadin.grid.cellrenderers.editable.common.EditableRendererEnabled;
import org.vaadin.grid.cellrenderers.editable.common.EditableRendererUtil;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionsInfo;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiselectClientRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiselectServerRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect.ComboBoxMultiselectState;
import org.vaadin.grid.enhancements.filter.ItemCaptionGeneratorFilter;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.shared.ui.combobox.FilteringMode;

import elemental.json.JsonValue;

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
    private final int pages;
    private final String inputPrompt;
    private final String selectAllText;
    private final String deselectAllText;

    private final String originalItemProperty;
    private final String itemIdPropertyId;
    private final String itemCaptionPropertyId;
    private final Function<BEANTYPE, String> itemCaptionGenerator;
    private final FilteringMode filteringMode = FilteringMode.CONTAINS;
    private String lastFilterString;

    private final EditableRendererEnabled editableRendererEnabled;

    private final Comparator<? super OptionElement> comparator = new Comparator<OptionElement>() {

        @Override
        public int compare(final OptionElement o1, final OptionElement o2) {
            final boolean o1Selected = ComboBoxMultiselectRenderer.this.selectedOptions.contains(o1);
            final boolean o2Selected = ComboBoxMultiselectRenderer.this.selectedOptions.contains(o2);

            if (o1Selected && o2Selected) {
                return o1.getName()
                    .compareTo(o2.getName());
            }

            if (o1Selected) {
                return -1;
            }

            if (o2Selected) {
                return 1;
            }

            return o1.getName()
                .compareTo(o2.getName());
        }
    };

    protected boolean sortingNeeded = true;
    protected ArrayList<OptionElement> sortedOptions;

    public ComboBoxMultiselectRenderer(final Class<BEANTYPE> clazz, final List<BEANTYPE> selections, final String itemIdPropertyId,
            final String itemCaptionPropertyId, final int pageSize, final String inputPrompt, final String selectAllText, final String deselectAllText,
            final String originalItemProperty, final EditableRendererEnabled editableRendererEnabled, final Function<BEANTYPE, String> itemCaptionGenerator) {
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
        this.itemCaptionGenerator = itemCaptionGenerator;
        this.originalItemProperty = originalItemProperty;

        this.editableRendererEnabled = editableRendererEnabled;
    }

    @Override
    protected ComboBoxMultiselectState getState() {
        return (ComboBoxMultiselectState) super.getState();
    }

    private final ComboBoxMultiselectServerRpc rpc = new ComboBoxMultiselectServerRpc() {

        @Override
        public void getPage(int page, final boolean skipBlur, final CellId id) {
            ComboBoxMultiselectRenderer.this.lastFilterString = null;

            if (ComboBoxMultiselectRenderer.this.selectedOptions == null) {
                return;
            }

            final OptionsInfo info = new OptionsInfo(ComboBoxMultiselectRenderer.this.pages, ComboBoxMultiselectRenderer.this.inputPrompt,
                    ComboBoxMultiselectRenderer.this.selectAllText, ComboBoxMultiselectRenderer.this.deselectAllText);
            if (page == -1) {
                page = ComboBoxMultiselectRenderer.this.container.indexOfId(getCellProperty(id).getValue()) / ComboBoxMultiselectRenderer.this.pageSize;
                // Inform which page we are sending.
                info.setCurrentPage(page);
                // getRpcProxy(MultiSelectClientRpc.class).setCurrentPage(page,
                // id);
            }

            // Get start id for page
            final int fromIndex = ComboBoxMultiselectRenderer.this.pageSize * page;
            final int toIndex = fromIndex + ComboBoxMultiselectRenderer.this.pageSize > ComboBoxMultiselectRenderer.this.container.size()
                    ? ComboBoxMultiselectRenderer.this.container.size()
                    : fromIndex + ComboBoxMultiselectRenderer.this.pageSize;

            final List<BEANTYPE> elements = ComboBoxMultiselectRenderer.this.container.getItemIds();

            if (ComboBoxMultiselectRenderer.this.sortedOptions == null || ComboBoxMultiselectRenderer.this.sortingNeeded) {
                ComboBoxMultiselectRenderer.this.sortedOptions = convertBeansToComboBoxMultiselectOptions(elements);
                Collections.sort(ComboBoxMultiselectRenderer.this.sortedOptions, ComboBoxMultiselectRenderer.this.comparator);

                ComboBoxMultiselectRenderer.this.sortingNeeded = false;
            }
            final List<OptionElement> options = ComboBoxMultiselectRenderer.this.sortedOptions.subList(fromIndex, toIndex);
            getRpcProxy(ComboBoxMultiselectClientRpc.class).updateOptions(info, options, skipBlur, id);
        }

        private ArrayList<OptionElement> convertBeansToComboBoxMultiselectOptions(final Collection<BEANTYPE> elements) {
            final ArrayList<OptionElement> options = new ArrayList<OptionElement>();
            for (final BEANTYPE bean : elements) {
                options.add(getOptionElement(bean));
            }
            return options;
        }

        @Override
        public void getFilterPage(final String filterString, int page, final boolean skipBlur, final CellId id) {
            ComboBoxMultiselectRenderer.this.lastFilterString = filterString;

            if (ComboBoxMultiselectRenderer.this.selectedOptions == null) {
                return;
            }

            if (filterString.isEmpty()) {
                getPage(-1, skipBlur, id);
                return;
            }

            final Filterable filterable = ComboBoxMultiselectRenderer.this.container;

            final Filter filter = buildFilter(filterString, ComboBoxMultiselectRenderer.this.filteringMode);

            if (filter != null) {
                filterable.addContainerFilter(filter);
            }

            final List<OptionElement> filteredResult = convertBeansToComboBoxMultiselectOptions(ComboBoxMultiselectRenderer.this.container.getItemIds());

            Collections.sort(filteredResult, ComboBoxMultiselectRenderer.this.comparator);

            final int filteredPages = (int) Math.ceil((double) filteredResult.size() / ComboBoxMultiselectRenderer.this.pageSize);
            final OptionsInfo info = new OptionsInfo(filteredPages, ComboBoxMultiselectRenderer.this.inputPrompt,
                    ComboBoxMultiselectRenderer.this.selectAllText, ComboBoxMultiselectRenderer.this.deselectAllText);

            if (page == -1) {
                page = filteredResult.indexOf(getCellProperty(id).getValue()) / ComboBoxMultiselectRenderer.this.pageSize;
                // Inform which page we are sending.
                info.setCurrentPage(page);
                // getRpcProxy(MultiSelectClientRpc.class).setCurrentPage(page,
                // id);
            }

            final int fromIndex = ComboBoxMultiselectRenderer.this.pageSize * page;
            final int toIndex = fromIndex + ComboBoxMultiselectRenderer.this.pageSize > filteredResult.size() ? filteredResult.size()
                    : fromIndex + ComboBoxMultiselectRenderer.this.pageSize;

            if (filter != null) {
                filterable.removeContainerFilter(filter);
            }

            getRpcProxy(ComboBoxMultiselectClientRpc.class).updateOptions(info, filteredResult.subList(fromIndex, toIndex), skipBlur, id);
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
                        if (ComboBoxMultiselectRenderer.this.itemCaptionGenerator == null) {
                            filter = new SimpleStringFilter(ComboBoxMultiselectRenderer.this.itemCaptionPropertyId, filterString, true, true);
                        }
                        else {
                            filter = new ItemCaptionGeneratorFilter<BEANTYPE>(ComboBoxMultiselectRenderer.this.itemCaptionGenerator, filterString, true, true,
                                    ComboBoxMultiselectRenderer.this.container);
                        }

                        break;
                    case CONTAINS:
                        if (ComboBoxMultiselectRenderer.this.itemCaptionGenerator == null) {
                            filter = new SimpleStringFilter(ComboBoxMultiselectRenderer.this.itemCaptionPropertyId, filterString, true, false);
                        }
                        else {
                            filter = new ItemCaptionGeneratorFilter<BEANTYPE>(ComboBoxMultiselectRenderer.this.itemCaptionGenerator, filterString, true, false,
                                    ComboBoxMultiselectRenderer.this.container);
                        }

                        break;
                }
            }
            return filter;
        }

        @Override
        public void filter(final CellId id, final String filterString, final boolean skipBlur) {
            final Filterable filterable = ComboBoxMultiselectRenderer.this.container;
            final Filter filter = buildFilter(filterString, ComboBoxMultiselectRenderer.this.filteringMode);

            if (filter != null) {
                filterable.addContainerFilter(filter);
            }

            final List<OptionElement> filteredResult = convertBeansToComboBoxMultiselectOptions(ComboBoxMultiselectRenderer.this.container.getItemIds());

            final int filteredPages = (int) Math.ceil((double) filteredResult.size() / ComboBoxMultiselectRenderer.this.pageSize);
            final OptionsInfo info = new OptionsInfo(filteredPages, ComboBoxMultiselectRenderer.this.inputPrompt,
                    ComboBoxMultiselectRenderer.this.selectAllText, ComboBoxMultiselectRenderer.this.deselectAllText);

            if (filter != null) {
                filterable.removeContainerFilter(filter);
            }

            getRpcProxy(ComboBoxMultiselectClientRpc.class).updateOptions(info, filteredResult, skipBlur, id);
        }

        @Override
        public void onValueSetChange(final CellId id, final Set<OptionElement> newValues) {
            final Object itemId = getItemId(id.getRowId());
            final Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            final Item row = getParentGrid().getContainerDataSource()
                .getItem(itemId);

            final Property<Set<BEANTYPE>> cell = getCellProperty(id);

            final Set<BEANTYPE> selectedBeans = new HashSet<BEANTYPE>();

            for (final BEANTYPE bean : ComboBoxMultiselectRenderer.this.container.getItemIds()) {
                final Property<?> idProperty = ComboBoxMultiselectRenderer.this.container.getItem(bean)
                    .getItemProperty(ComboBoxMultiselectRenderer.this.itemIdPropertyId);
                for (final OptionElement newValue : newValues) {
                    if (newValue.getId() != null && newValue.getId()
                        .equals(idProperty.getValue())) {
                        selectedBeans.add(bean);
                    }
                }

            }

            cell.setValue(selectedBeans);

            fireItemEditEvent(itemId, row, columnPropertyId, selectedBeans);
        }

        private Property<Set<BEANTYPE>> getCellProperty(final CellId id) {
            final Object itemId = getItemId(id.getRowId());
            final Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            final Item row = getParentGrid().getContainerDataSource()
                .getItem(itemId);

            return row.getItemProperty(ComboBoxMultiselectRenderer.this.originalItemProperty);
        }

        @Override
        public void onRender(final CellId id) {
            final Set<BEANTYPE> value = getCellProperty(id).getValue();

            final Set<OptionElement> selected = new HashSet<OptionElement>();

            if (value != null) {
                for (final BEANTYPE bean : ComboBoxMultiselectRenderer.this.container.getItemIds()) {
                    if (value.contains(bean)) {
                        selected.add(getOptionElement(bean));
                    }
                }
            }

            ComboBoxMultiselectRenderer.this.selectedOptions = selected;

            getRpcProxy(ComboBoxMultiselectClientRpc.class)
                .updateSelectedOptions(ComboBoxMultiselectRenderer.this.selectedOptions, id, false, EditableRendererUtil
                    .isColumnComponentEnabled(getItemId(id.getRowId()), getParentGrid(), ComboBoxMultiselectRenderer.this.editableRendererEnabled));
        }

        @Override
        public void setSortingNeeded(final boolean sortingNeeded) {
            ComboBoxMultiselectRenderer.this.sortingNeeded = sortingNeeded;
        }

        @Override
        public void selectAll(final CellId id) {
            final Object itemId = getItemId(id.getRowId());
            final Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            final Item row = getParentGrid().getContainerDataSource()
                .getItem(itemId);

            final Property<Set<BEANTYPE>> cell = getCellProperty(id);

            ComboBoxMultiselectRenderer.this.sortingNeeded = true;

            final Filterable filterable = ComboBoxMultiselectRenderer.this.container;
            final Filter filter = buildFilter(ComboBoxMultiselectRenderer.this.lastFilterString, ComboBoxMultiselectRenderer.this.filteringMode);

            if (filter != null) {
                filterable.addContainerFilter(filter);
            }

            final Set<OptionElement> selected = new HashSet<OptionElement>();
            for (final BEANTYPE bean : ComboBoxMultiselectRenderer.this.container.getItemIds()) {
                selected.add(getOptionElement(bean));
            }
            ComboBoxMultiselectRenderer.this.selectedOptions.addAll(selected);

            final Set<BEANTYPE> selectedBeans = cell.getValue();
            selectedBeans.addAll(ComboBoxMultiselectRenderer.this.container.getItemIds());

            if (filter != null) {
                filterable.removeContainerFilter(filter);
            }

            cell.setValue(selectedBeans);
            fireItemEditEvent(itemId, row, columnPropertyId, selectedBeans);

            getRpcProxy(ComboBoxMultiselectClientRpc.class)
                .updateSelectedOptions(ComboBoxMultiselectRenderer.this.selectedOptions, id, true, EditableRendererUtil
                    .isColumnComponentEnabled(getItemId(id.getRowId()), getParentGrid(), ComboBoxMultiselectRenderer.this.editableRendererEnabled));
        }

        @Override
        public void deselectAll(final CellId id) {
            // TODO make this for fireItem simpler and own function
            final Object itemId = getItemId(id.getRowId());
            final Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            final Item row = getParentGrid().getContainerDataSource()
                .getItem(itemId);

            final Property<Set<BEANTYPE>> cell = getCellProperty(id);

            ComboBoxMultiselectRenderer.this.sortingNeeded = true;

            final Filterable filterable = ComboBoxMultiselectRenderer.this.container;
            final Filter filter = buildFilter(ComboBoxMultiselectRenderer.this.lastFilterString, ComboBoxMultiselectRenderer.this.filteringMode);

            if (filter != null) {
                filterable.addContainerFilter(filter);
            }

            final Set<OptionElement> selected = new HashSet<OptionElement>();
            for (final BEANTYPE bean : ComboBoxMultiselectRenderer.this.container.getItemIds()) {
                selected.add(getOptionElement(bean));
            }
            ComboBoxMultiselectRenderer.this.selectedOptions.removeAll(selected);

            final Set<BEANTYPE> selectedBeans = cell.getValue();
            selectedBeans.removeAll(ComboBoxMultiselectRenderer.this.container.getItemIds());

            if (filter != null) {
                filterable.removeContainerFilter(filter);
            }

            cell.setValue(selectedBeans);
            fireItemEditEvent(itemId, row, columnPropertyId, cell.getValue());

            getRpcProxy(ComboBoxMultiselectClientRpc.class)
                .updateSelectedOptions(ComboBoxMultiselectRenderer.this.selectedOptions, id, true, EditableRendererUtil
                    .isColumnComponentEnabled(getItemId(id.getRowId()), getParentGrid(), ComboBoxMultiselectRenderer.this.editableRendererEnabled));
        }
    };

    @Override
    public JsonValue encode(final BEANTYPE bean) {
        return encode(getOptionElement(bean), OptionElement.class);
    }

    private OptionElement getOptionElement(final BEANTYPE bean) {
        if (bean == null) {
            return new OptionElement();
        }

        final Item item = ComboBoxMultiselectRenderer.this.container.getItem(bean);

        if (item == null) {
            return new OptionElement();
        }

        final Property<?> idProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemIdPropertyId);

        final OptionElement optionElement = new OptionElement((Long) idProperty.getValue());

        if (ComboBoxMultiselectRenderer.this.itemCaptionGenerator != null) {
            optionElement.setName(ComboBoxMultiselectRenderer.this.itemCaptionGenerator.apply(bean));
        }
        else {
            final Property<?> captionProperty = item.getItemProperty(ComboBoxMultiselectRenderer.this.itemCaptionPropertyId);
            optionElement.setName((String) captionProperty.getValue());
        }
        return optionElement;
    }
}
