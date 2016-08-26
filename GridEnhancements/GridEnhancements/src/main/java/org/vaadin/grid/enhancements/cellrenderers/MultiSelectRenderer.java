package org.vaadin.grid.enhancements.cellrenderers;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import org.vaadin.grid.cellrenderers.EditableRenderer;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.CellId;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.OptionsInfo;
import org.vaadin.grid.enhancements.client.cellrenderers.multiselect.MultiSelectClientRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.multiselect.MultiSelectServerRpc;
import org.vaadin.grid.enhancements.client.cellrenderers.multiselect.MultiSelectState;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Grid renderer that renders a multiselect ComboBox element
 *
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class MultiSelectRenderer extends EditableRenderer<String> {

    List<String> fullList = new LinkedList<String>();

    int pageSize = 5;
    int pages;

    public MultiSelectRenderer(List<String> selections) {
        super(String.class);

        registerRpc(rpc);
        // Add items to internal list so we don't expose ourselves to changes in the given list
        fullList.addAll(selections);

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

    @Override
    protected MultiSelectState getState() {
        return (MultiSelectState) super.getState();
    }

    private MultiSelectServerRpc rpc = new MultiSelectServerRpc() {

        @Override
        public void getPage(int page, CellId id) {
            OptionsInfo info = new OptionsInfo(pages);
            if (page == -1) {
                page = fullList.indexOf(getCellProperty(id).getValue()) / pageSize;
                // Inform which page we are sending.
                info.setCurrentPage(page);
//                getRpcProxy(MultiSelectClientRpc.class).setCurrentPage(page, id);
            }

            // Get start id for page
            int fromIndex = pageSize * page;
            int toIndex = fromIndex + pageSize > fullList.size() ? fullList.size() : fromIndex + pageSize;
            getRpcProxy(MultiSelectClientRpc.class).updateOptions(info, fullList.subList(fromIndex, toIndex), id);
        }

        @Override
        public void getFilterPage(String filter, int page, CellId id) {
            if (filter.isEmpty()) {
                getPage(-1, id);
                return;
            }

            List<String> filteredResult;
            filteredResult = new LinkedList<String>();
            for (String s : fullList) {
                if (s.contains(filter)) filteredResult.add(s);
            }

            int filteredPages = (int) Math.ceil((double) filteredResult.size() / pageSize);
            OptionsInfo info = new OptionsInfo(filteredPages);

            if (page == -1) {
                page = filteredResult.indexOf(getCellProperty(id).getValue()) / pageSize;
                // Inform which page we are sending.
                info.setCurrentPage(page);
//                getRpcProxy(MultiSelectClientRpc.class).setCurrentPage(page, id);
            }

            int fromIndex = pageSize * page;
            int toIndex = fromIndex + pageSize > filteredResult.size() ? filteredResult.size() : fromIndex + pageSize;
            getRpcProxy(MultiSelectClientRpc.class).updateOptions(info, filteredResult.subList(fromIndex, toIndex), id);
        }

        @Override
        public void filter(CellId id, String filter) {
            List<String> filteredResult = new LinkedList<String>();
            for (String s : fullList) {
                if (s.contains(filter)) filteredResult.add(s);
            }
            int filteredPages = (int) Math.ceil((double) filteredResult.size() / pageSize);
            OptionsInfo info = new OptionsInfo(filteredPages);
            getRpcProxy(MultiSelectClientRpc.class).updateOptions(info, filteredResult, id);
        }


        @Override
        public void onValueSetChange(CellId id, Set<String> newValue) {
            Object itemId = getItemId(id.getRowId());
            Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            Item row = getParentGrid().getContainerDataSource().getItem(itemId);

            Property<String> cell = getCellProperty(id);

            cell.setValue(newValue.toString());

            fireItemEditEvent(itemId, row, columnPropertyId, newValue.toString());
        }

        private Property<String> getCellProperty(CellId id) {
            Object itemId = getItemId(id.getRowId());
            Object columnPropertyId = getColumn(id.getColumnId()).getPropertyId();

            Item row = getParentGrid().getContainerDataSource().getItem(itemId);

            return (Property<String>) row.getItemProperty(columnPropertyId);
        }
    };

}
