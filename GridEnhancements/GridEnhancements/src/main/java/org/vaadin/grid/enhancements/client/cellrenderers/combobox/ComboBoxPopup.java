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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxPopup<T> extends DecoratedPopupPanel {

    private final CellList<T> list;
    private final SelectionModel<T> selectionModel;
    private List<T> values;

    private Button up, down;
    private Set<HandlerRegistration> handlers = new HashSet<HandlerRegistration>();

    private ComboBox.PopupEvent selectionListener;
    // Not as items on page
    private Set<T> currentSelection = new HashSet<T>();
    private boolean updatingSelection = false;

    public ComboBoxPopup(List<T> values, boolean multiSelect) {
        this.values = values;
        if (multiSelect) {
            selectionModel = new MultiSelectionModel<T>(keyProvider);
        } else {
            selectionModel = new SingleSelectionModel<T>(keyProvider);
        }

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
        list.setSelectionModel(selectionModel);

        if (!handlers.isEmpty()) {
            for (HandlerRegistration handler : handlers) {
                handler.removeHandler();
            }
        }

        HandlerRegistration mouseHandler = list.addBitlessDomHandler(new MouseMoveHandler() {
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
        }, MouseMoveEvent.getType());
        handlers.add(mouseHandler);

        HandlerRegistration keyPressHandler = list.addHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                switch (event.getNativeEvent().getKeyCode()) {
                    case KeyCodes.KEY_ESCAPE:
                        closePopup();
                        selectionListener.clear();
                        break;
                    case KeyCodes.KEY_DOWN:
                        if (list.getKeyboardSelectedRow() == list.getVisibleItems().size() - 1 && down.isEnabled()) {
                            selectionListener.nextPage();
                        }
                        break;
                    case KeyCodes.KEY_UP:
                        if (list.getKeyboardSelectedRow() == 0 && up.isEnabled()) {
                            selectionListener.prevPage();
                        }
                        break;
                    case KeyCodes.KEY_TAB:
                        closePopup();
                        if (selectionListener != null) {
                            selectionListener.itemSelected(list.getVisibleItem(list.getKeyboardSelectedRow()));
                        }
                        break;
                }
            }
        }, KeyDownEvent.getType());
        handlers.add(keyPressHandler);

        HandlerRegistration selectionHandler = selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                if (selectionModel instanceof SingleSelectionModel) {
                    if (selectionListener != null) {
                        selectionListener.itemSelected(getSelectedObject());
                    }
                    closePopup();
                } else {
                    if (!updatingSelection && selectionListener != null) {
                        Set<T> selection = new HashSet<T>();
                        selection.addAll(currentSelection);
                        selection.addAll(getSelectedObjects());
                        selectionListener.itemsSelected(selection);
                    }
                }
            }
        });
        handlers.add(selectionHandler);

        setStyleName("c-combo-popup");

        VerticalPanel content = new VerticalPanel();
        content.setWidth("100%");

        up = new Button("Prev");
        up.getElement().removeAttribute("type");
        up.setWidth("100%");
        up.setStyleName("c-combo-popup-prevpage");
        up.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionListener.prevPage();
            }
        });

        down = new Button("Next");
        down.getElement().removeAttribute("type");
        down.setWidth("100%");
        down.setStyleName("c-combo-popup-nextpage");
        down.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                selectionListener.nextPage();
            }
        });

        content.add(up);
        content.add(list);
        content.add(down);

        add(content);
    }

    private T getSelectedObject() {
        return ((SingleSelectionModel<T>) selectionModel).getSelectedObject();
    }

    public Set<T> getSelectedObjects() {
        return ((MultiSelectionModel<T>) selectionModel).getSelectedSet();
    }


    private void closePopup() {
        ComboBoxPopup.this.hide();
        if (!handlers.isEmpty()) {
            for (HandlerRegistration handler : handlers) {
                handler.removeHandler();
            }
        }
    }

    public void addEventListener(ComboBox.PopupEvent event) {
        this.selectionListener = event;
    }

    public void focusSelection(String selected) {
        if (values.contains(selected)) {
            list.setKeyboardSelectedRow(values.indexOf(selected), true);
        } else if (!values.isEmpty()) {
            list.setKeyboardSelectedRow(0, true);
        } else {
            list.setFocus(true);
        }
    }

    public void setNextPageEnabled(boolean nextPageEnabled) {
        down.setEnabled(nextPageEnabled);
    }

    public void setPreviousPageEnabled(boolean prevPageEnabled) {
        up.setEnabled(prevPageEnabled);
    }

    public void setCurrentSelection(Set<T> currentSelection) {
        updatingSelection = true;
        this.currentSelection.clear();
        for (T value : currentSelection) {
            if (!values.contains(value)) {
                this.currentSelection.add(value);
            } else {
                selectionModel.setSelected(value, true);
            }
        }
        updatingSelection = false;
    }

    public void removeEventListener(ComboBox.PopupEvent<T> eventListener) {
        selectionListener = null;
    }

    private class Cell extends AbstractCell<T> {

        @Override
        public void render(Context context, final T value, SafeHtmlBuilder sb) {
            if (selectionModel instanceof MultiSelectionModel) {
                sb.appendHtmlConstant("<input type=\"checkbox\" " + (((MultiSelectionModel) selectionModel).isSelected(value) ? "checked" : "") + ">");
            }
            sb.appendHtmlConstant("<span>"); // TODO: add something for icons?
            sb.appendEscaped(value.toString());
            sb.appendHtmlConstant("</span>");

        }
    }

    ProvidesKey<T> keyProvider = new ProvidesKey<T>() {
        public Object getKey(T item) {
            // Always do a null check.
            return (item == null) ? null : item.hashCode();
        }
    };
}
