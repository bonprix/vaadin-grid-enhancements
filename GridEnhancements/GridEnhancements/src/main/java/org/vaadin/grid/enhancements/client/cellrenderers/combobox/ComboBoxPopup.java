package org.vaadin.grid.enhancements.client.cellrenderers.combobox;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.vaadin.client.ui.VOverlay;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxPopup<T> extends VOverlay implements MouseMoveHandler, KeyDownHandler, SelectionChangeEvent.Handler {

    private final CellList<T> list;
    private final SelectionModel<T> selectionModel;
    private List<T> values;

    private Button up, down;
    private Set<HandlerRegistration> handlers = new HashSet<HandlerRegistration>();

    private PopupCallback callback;

    private long lastAutoClosed;

    public ComboBoxPopup(List<T> values) {
        super(true);

        this.values = values;
        addCloseHandler(this);

        CellList.Resources resources = GWT.create(CellListResources.class);

        list = new CellList<T>(new Cell(), resources, keyProvider);
        list.setPageSize(values.size());
        list.setRowCount(values.size(), true);
        list.setRowData(0, values);
        list.setVisibleRange(0, values.size());
        list.setWidth("100%");
        list.setKeyboardPagingPolicy(HasKeyboardPagingPolicy.KeyboardPagingPolicy.INCREASE_RANGE);
        list.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

        list.setStyleName("c-combobox-options");


        selectionModel = new SingleSelectionModel<T>(keyProvider);
        list.setSelectionModel(selectionModel);

        // Remove all handlers if any exist for any reason
        if (!handlers.isEmpty()) {
            for (HandlerRegistration handler : handlers) {
                handler.removeHandler();
            }
        }

        // Add CellList listeners
        handlers.add(list.addBitlessDomHandler(this, MouseMoveEvent.getType()));
        handlers.add(list.addHandler(this, KeyDownEvent.getType()));

        // Add selection change handler
        handlers.add(selectionModel.addSelectionChangeHandler(this));


        setStyleName("c-combo-popup");

        VerticalPanel content = new VerticalPanel();
        content.setWidth("100%");

        up = new Button("");
        up.getElement().removeAttribute("type");
        up.setStyleName("c-combo-popup-prevpage");
        up.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                callback.prevPage();
            }
        });

        down = new Button("");
        down.getElement().removeAttribute("type");
        down.setStyleName("c-combo-popup-nextpage");
        down.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                callback.nextPage();
            }
        });

        // Add widgets to content panel
        content.add(up);
        content.add(list);
        content.add(down);

        // Init content widget
        add(content);
    }

    /**
     * Get the selected object when in singleSelection mode
     *
     * @return Single selected object
     */
    private T getSelectedObject() {
        return ((SingleSelectionModel<T>) selectionModel).getSelectedObject();
    }

    /**
     * Hide popup
     */
    private void closePopup() {
        ComboBoxPopup.this.hide();
    }

    public void setNextPageEnabled(boolean nextPageEnabled) {
        down.setVisible(nextPageEnabled);
    }

    public void setPreviousPageEnabled(boolean prevPageEnabled) {
        up.setVisible(prevPageEnabled);
    }

    /**
     * Move keyboard focus to the selected item if found in current options
     *
     * @param selected Selected item to focus if available
     */
    public void focusSelection(T selected, boolean stealFocus) {
        if (values.contains(selected)) {
            // Focus selected item
            list.setKeyboardSelectedRow(values.indexOf(selected), stealFocus);
        } else if (!values.isEmpty()) {
            // Else focus first item if values exist
            list.setKeyboardSelectedRow(0, stealFocus);
        } else {
            // Else move focus to list
            list.setFocus(stealFocus);
        }
    }

    /**
     * Add a popup callback
     *
     * @param callback ComboBox callback
     */
    public void addPopupCallback(PopupCallback<T> callback) {
        this.callback = callback;
    }

    /**
     * Remove popup callback
     *
     * @param callback
     */
    public void removePopupCallback(PopupCallback<T> callback) {
        this.callback = null;
    }

    public boolean isJustClosed() {
        final long now = (new Date()).getTime();
        return (lastAutoClosed > 0 && (now - lastAutoClosed) < 200);
    }

    @Override
    public void onClose(CloseEvent<PopupPanel> event) {
        if (event.isAutoClosed()) {
            lastAutoClosed = (new Date()).getTime();
        }
        if (!handlers.isEmpty()) {
            for (HandlerRegistration handler : handlers) {
                handler.removeHandler();
            }
        }
    }

    /**
     * CellList cell content renderer implementation
     */
    private class Cell extends AbstractCell<T> {

        @Override
        public void render(Context context, final T value, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<span>"); // TODO: add something for icons?
            sb.appendEscaped(value.toString());
            sb.appendHtmlConstant("</span>");

        }
    }

    /**
     * CellList item key provider
     */
    private ProvidesKey<T> keyProvider = new ProvidesKey<T>() {
        public Object getKey(T item) {
            // Always do a null check.
            return (item == null) ? null : item.hashCode();
        }
    };


    // --- Event handler implementations ---

    @Override
    public void onKeyDown(KeyDownEvent event) {
        switch (event.getNativeEvent().getKeyCode()) {
            case KeyCodes.KEY_ESCAPE:
                callback.clear();
                closePopup();
                break;
            case KeyCodes.KEY_DOWN:
                if (list.getKeyboardSelectedRow() == list.getVisibleItems().size() - 1 && down.isEnabled()) {
                    callback.nextPage();
                }
                break;
            case KeyCodes.KEY_UP:
                if (list.getKeyboardSelectedRow() == 0 && up.isEnabled()) {
                    callback.prevPage();
                }
                break;
            case KeyCodes.KEY_TAB:
                if (callback != null) {
                    callback.itemSelected(list.getVisibleItem(list.getKeyboardSelectedRow()));
                }
                closePopup();
                break;
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        Element target = event.getNativeEvent().getEventTarget().cast();
        for (int i = 0; i < list.getVisibleItems().size(); i++) {
            Element e = list.getRowElement(i);
            if (e.equals(target)) {
                list.setKeyboardSelectedRow(i, true);
                break;
            }
        }
    }

    public void onSelectionChange(SelectionChangeEvent event) {
        if (callback != null) {
            callback.itemSelected(getSelectedObject());
        }
        closePopup();
    }
}
