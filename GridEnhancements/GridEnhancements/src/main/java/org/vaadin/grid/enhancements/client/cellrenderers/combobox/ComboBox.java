package org.vaadin.grid.enhancements.client.cellrenderers.combobox;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

import java.util.List;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBox extends Composite implements KeyDownHandler, HasChangeHandlers {

    private ComboBoxPopup popup = null;

    private String selected;
    private TextBox selector;

    private EventHandler eventHandler;

    Timer t = null;

    // Page starts as page 0
    private int currentPage = 0;
    private int pages = 1;

    public ComboBox() {
        selector = new TextBox();
        selector.addBitlessDomHandler(this, KeyDownEvent.getType());

        selector.getElement().getStyle().setProperty("padding", "0 16px");
        selector.setStyleName("c-combobox-input");

        selector.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                RegExp regex = RegExp.compile("^[a-zA-Z0-9]+$");
                if (!regex.test("" + (char) event.getNativeKeyCode())) {
                    return;
                }
                if (t == null)
                    t = new Timer() {
                        @Override
                        public void run() {
                            currentPage = 0;
                            eventHandler.filter(selector.getValue(), currentPage);
                        }
                    };
                t.schedule(300);
            }
        });

        Button drop = new Button();
        drop.setStyleName("c-combobox-button");
        drop.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (popup != null && popup.isVisible()) {
                    popup.hide();
                    popup = null;
                } else {
                    // Start from first page when opening.
                    currentPage = -1;
                    eventHandler.getPage(currentPage);
                }
            }
        });

        FlowPanel content = new FlowPanel();
        content.setStyleName("v-widget v-has-width v-filterselect v-filterselect-prompt");
        content.setWidth("100%");

        content.add(selector);
        content.add(drop);

        initWidget(content);

    }

    public void updateSelection(List<String> selection) {
        openDropdown(selection);
    }

    public String getValue() {
        return selected;
    }

    public void setSelected(String selected) {
        String old = this.selected;
        selector.setValue(selected);
        this.selected = selected;

        if (!old.equals(selected)) {
            eventHandler.change();
        }
    }

    public void updatePageAmount(int pages) {
        this.pages = pages;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    protected interface PopupEvent {
        void itemSelected(String item);

        void nextPage();

        void prevPage();
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return addDomHandler(handler, ChangeEvent.getType());
    }

    public void setSelection(String selection) {
        selector.setValue(selection);
    }

    public boolean isEnabled() {
        return selector.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        selector.setEnabled(enabled);
    }

    // -- Handlers --

    @Override
    public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_DOWN) {
            event.stopPropagation();

            // Focus popup if open else open popup with first page
            if (popup != null && popup.isVisible()) {
                popup.focusSelection(selected);
            } else {
                // Start from page with selection when opening.
                currentPage = -1;
                eventHandler.getPage(currentPage);
            }
        }
    }

    private void openDropdown(List<String> items) {
        if (popup != null) {
            popup.hide();
        }
        popup = new ComboBoxPopup(items);
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> closeEvent) {
                popup = null;
            }
        });
        popup.addListener(new PopupEvent() {
            @Override
            public void itemSelected(String item) {
                setSelected(item);
                selector.setFocus(true);
                eventHandler.clearFilter();
            }

            @Override
            public void nextPage() {
                eventHandler.getPage(++currentPage);
            }

            @Override
            public void prevPage() {
                eventHandler.getPage(--currentPage);
            }
        });

        popup.setPreviousPageEnabled(currentPage > 0);
        popup.setNextPageEnabled(currentPage < pages - 1);

        popup.setWidth(getOffsetWidth() + "px");
        popup.getElement().getStyle().setZIndex(1000);
        popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            public void setPosition(int offsetWidth, int offsetHeight) {
                int top = ComboBox.this.getAbsoluteTop() + ComboBox.this.getOffsetHeight();
                if (top + ComboBox.this.getOffsetHeight() > Window.getClientHeight()) {
                    top = ComboBox.this.getAbsoluteTop() - ComboBox.this.getOffsetHeight();
                }
                popup.setPopupPosition(ComboBox.this.getAbsoluteLeft(), top);
            }
        });
        popup.focusSelection(selected);
    }
}
