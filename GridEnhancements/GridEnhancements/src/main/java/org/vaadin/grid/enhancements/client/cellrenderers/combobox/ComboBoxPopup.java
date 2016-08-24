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
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.vaadin.client.VConsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxPopup extends DecoratedPopupPanel {

    private final CellList<String> list;
    private final SingleSelectionModel<String> selectionModel;
    private List<String> values;

    private Button up, down;
    private Set<HandlerRegistration> handlers = new HashSet<HandlerRegistration>();

    private ComboBox.PopupEvent selectionListener;

    public ComboBoxPopup(List<String> values) {
        this.values = values;
        selectionModel = new SingleSelectionModel<String>(keyProvider);

        CellList.Resources resources = GWT.create(CellListResources.class);

        list = new CellList<String>(new Cell(), resources, keyProvider);
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
            for(HandlerRegistration handler: handlers) {
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
                VConsole.log("Key down");
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

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                closePopup();
                if (selectionListener != null) {
                    selectionListener.itemSelected(selectionModel.getSelectedObject());
                }
            }
        });
        setStyleName("v-filterselect-suggestpopup c-combo-popup");

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

    private void closePopup() {
        ComboBoxPopup.this.hide();
        if (!handlers.isEmpty()) {
            for(HandlerRegistration handler: handlers) {
                handler.removeHandler();
            }
        }
    }

    public void addListener(ComboBox.PopupEvent event) {
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

    private class Cell extends AbstractCell<String> {

        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            sb.appendHtmlConstant("<span class=\"" + (context.getIndex() % 2 == 0 ? "even" : "odd") + "\">");
            sb.appendEscaped(value);
            sb.appendHtmlConstant("</span>");

        }
    }

    ProvidesKey<String> keyProvider = new ProvidesKey<String>() {
        public Object getKey(String item) {
            // Always do a null check.
            return (item == null) ? null : item.hashCode();
        }
    };
}
