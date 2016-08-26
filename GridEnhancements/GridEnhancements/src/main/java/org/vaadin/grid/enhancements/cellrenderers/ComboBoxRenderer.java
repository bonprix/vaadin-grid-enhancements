package org.vaadin.grid.enhancements.cellrenderers;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import org.vaadin.grid.cellrenderers.EditableRenderer;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.BoxItem;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.CellId;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.ComboBoxClientRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.ComboBoxServerRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.ComboBoxState;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Grid renderer that renders a ComboBox element
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxRenderer extends EditableRenderer<BoxItem> {

    List<BoxItem> fullList = new LinkedList<BoxItem>();

    int pageSize = 5;
    int pages;

    public ComboBoxRenderer(List<ServerBoxItem> selections) {
        super(BoxItem.class);

        registerRpc(rpc);
        for(ServerBoxItem item : selections) {

            String key = item.getCaption();
            setResource(key, item.getIcon());

            BoxItem i = new BoxItem();
            i.caption = item.getCaption();
            i.key= key;
            // Add items to internal list so we don't expose ourselves to changes in the given list
            fullList.add(i);
        }

        pages = (int) Math.ceil((double) fullList.size() / pageSize);
    }

    /**
     * Set the amount of items to be shown in the dropdown.
     *
     * @param pageSize Amount of items to show on page
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setMultiselect(boolean multiselect) {
        getState().isMultiSelect = multiselect;
    }

    @Override
    protected ComboBoxState getState() {
        return (ComboBoxState) super.getState();
    }

    private ComboBoxServerRpc rpc = new ComboBoxServerRpc() {

        @Override
        public void getPage(int page, CellId id) {
            if (page == -1) {
                page = fullList.indexOf(getCellProperty(id).getValue()) / pageSize;
                // Inform which page we are sending.
                getRpcProxy(ComboBoxClientRpc.class).setCurrentPage(page, id);
            }

            // Get start id for page
            int fromIndex = pageSize * page;
            int toIndex = fromIndex + pageSize > fullList.size() ? fullList.size() : fromIndex + pageSize;
            getRpcProxy(ComboBoxClientRpc.class).updateOptions(pages, fullList.subList(fromIndex, toIndex), id);
        }

        @Override
        public void getFilterPage(String filter, int page, CellId id) {
            if (filter.isEmpty()) {
                getPage(-1, id);
                return;
            }

            List<BoxItem> filteredResult;
            filteredResult = new LinkedList<BoxItem>();
            for (BoxItem s : fullList) {
                if (s.caption.contains(filter)) filteredResult.add(s);
            }

            int filteredPages = (int) Math.ceil((double) filteredResult.size() / pageSize);

            if (page == -1) {
                page = filteredResult.indexOf(getCellProperty(id).getValue()) / pageSize;
                // Inform which page we are sending.
                getRpcProxy(ComboBoxClientRpc.class).setCurrentPage(page, id);
            }

            int fromIndex = pageSize * page;
            int toIndex = fromIndex + pageSize > filteredResult.size() ? filteredResult.size() : fromIndex + pageSize;
            getRpcProxy(ComboBoxClientRpc.class).updateOptions(filteredPages, filteredResult.subList(fromIndex, toIndex), id);
        }

        @Override
        public void filter(CellId id, String filter) {
            List<BoxItem> filteredResult = new LinkedList<BoxItem>();
            for (BoxItem s : fullList) {
                if (s.caption.contains(filter)) filteredResult.add(s);
            }
            int filteredPages = (int) Math.ceil((double) filteredResult.size() / pageSize);
            getRpcProxy(ComboBoxClientRpc.class).updateOptions(filteredPages, filteredResult, id);
        }

        @Override
        public void onValueChange(CellId id, BoxItem newValue) {
            Object itemId = getItemId(id.getRowId());
            Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            Item row = getParentGrid().getContainerDataSource().getItem(itemId);

            Property<BoxItem> cell = getCellProperty(id);

            cell.setValue(newValue);

            fireItemEditEvent(itemId, row, columnPropertyId, newValue);
        }

        @Override
        public void onValueSetChange(CellId id, Set<BoxItem> newValue) {
             Object itemId = getItemId(id.getRowId());
            Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            Item row = getParentGrid().getContainerDataSource().getItem(itemId);

            Property<BoxItem> cell = getCellProperty(id);

            cell.setValue(newValue.iterator().next());

            fireItemEditEvent(itemId, row, columnPropertyId, newValue.iterator().next());
        }

        private Property<BoxItem> getCellProperty(CellId id) {
            Object itemId = getItemId(id.getRowId());
            Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            Item row = getParentGrid().getContainerDataSource().getItem(itemId);

            return (Property<BoxItem>) row.getItemProperty(columnPropertyId);
        }
    };

}
