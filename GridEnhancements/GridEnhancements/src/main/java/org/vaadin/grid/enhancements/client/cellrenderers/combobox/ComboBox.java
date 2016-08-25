package org.vaadin.grid.enhancements.client.cellrenderers.combobox;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBox extends Composite implements KeyDownHandler, BlurHandler, HasChangeHandlers {

    private ComboBoxPopup popup = null;

    private String selected;
    private Set<String> selectedSet = new HashSet<String>();

    private TextBox selector;
    private Button drop;

    private EventHandler eventHandler;

    private Timer t = null;
    // Page starts as page 0
    private int currentPage = 0;

    private int pages = 1;
    private boolean skipBlur = false;
    private boolean multiSelect;

    public ComboBox() {
        selector = new TextBox();
        selector.addKeyDownHandler(this);

        selector.getElement().getStyle().setProperty("padding", "0 16px");
        selector.setStyleName("c-combobox-input");
        selector.addBlurHandler(this);

        drop = new Button();
        drop.setStyleName("c-combobox-button");
        drop.addClickHandler(dropDownClickHandler);

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

    public void updatePageAmount(int pages) {
        this.pages = pages;
    }

    public String getValue() {
        return selected;
    }

    public void setSelected(String selected) {
        String old = this.selected;
        selector.setValue(selected);
        this.selected = selected;

        if (!old.equals(selected)) {
            eventHandler.change(selected);
        }
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return addDomHandler(handler, ChangeEvent.getType());
    }

    public void setSelection(String selection) {
        selected = selection;
        selector.setValue(selection);
    }

    public boolean isEnabled() {
        return selector.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        selector.setEnabled(enabled);
    }

    private void openDropdown(List<String> items) {
        boolean focus = false;
        if (popup != null) {
            focus = popup.isJustClosed();
            if (popup.isVisible()) popup.hide(true);
        }
        popup = new ComboBoxPopup(items, multiSelect);
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> closeEvent) {
                popup.removePopupCallback(eventListener);
            }
        });
        popup.addPopupCallback(eventListener);

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
        skipBlur = true;
        if (multiSelect) {
            popup.setCurrentSelection(selectedSet);
        }
        popup.focusSelection(selected, focus);
    }

    // -- Handlers --

    @Override
    public void onKeyDown(KeyDownEvent event) {

        switch (event.getNativeKeyCode()) {
            case KeyCodes.KEY_ESCAPE:
                event.preventDefault();
                event.stopPropagation();
                if (popup != null && popup.isVisible()) {
                    popup.hide(true);
                    popup = null;
                }
                eventHandler.clearFilter();
                selector.setValue(selected);
                break;
            case KeyCodes.KEY_DOWN:
                event.preventDefault();
                event.stopPropagation();

                // Focus popup if open else open popup with first page
                if (popup != null && popup.isAttached()) {
                    popup.focusSelection(selected, true);
                } else {
                    // Start from page with selection when opening.
                    currentPage = -1;
                    eventHandler.getPage(currentPage);
                }
                break;
            case KeyCodes.KEY_TAB:
                if (popup != null && popup.isAttached()) {
                    popup.hide(true);
                }
                selector.setValue(selected);
                break;
        }

        RegExp regex = RegExp.compile("^[a-zA-Z0-9]+$");
        if (!regex.test("" + (char) event.getNativeKeyCode()) && event.getNativeKeyCode() != KeyCodes.KEY_BACKSPACE) {
            return;
        }

        if (t == null)
            t = new Timer() {
                @Override
                public void run() {
                    currentPage = 0;
                    eventHandler.filter(selector.getValue(), currentPage);
                    t = null;
                }
            };
        t.schedule(300);
    }

    @Override
    public void onBlur(BlurEvent event) {
        if (!skipBlur) {
            if (popup != null && popup.isAttached()) {
                popup.hide();
            }
            selector.setValue(selected);
            skipBlur = false;
        }
    }

    private ClickHandler dropDownClickHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            if (popup != null && popup.isAttached()) {
                popup.hide();
                popup = null;
            } else if (popup == null || !popup.isJustClosed()) {
                // Start from page where selection is when opening.
                currentPage = -1;
                eventHandler.getPage(currentPage);
            }
            selector.setFocus(true);
        }
    };

    PopupCallback<String> eventListener = new PopupCallback<String>() {
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

        @Override
        public void clear() {
            selector.setFocus(true);
            eventHandler.clearFilter();
        }

        @Override
        public void itemsSelected(Set<String> selectedObjects) {
            selectedSet.clear();
            selectedSet.addAll(selectedObjects);
            eventHandler.change(selectedObjects);
            // TODO: update selected items
        }
    };
}
