package org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect;

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
import com.vaadin.client.VConsole;

import java.util.List;
import java.util.Set;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.EventHandler;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.PopupCallback;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBox extends Composite implements KeyDownHandler, BlurHandler, HasChangeHandlers {

	private ComboBoxPopup popup = null;

	private OptionElement selected;

	private TextBox selector;
	private Button drop;

	private EventHandler<OptionElement> eventHandler;

	private Timer t = null;
	// Page starts as page 0
	private int currentPage = 0;

	private int pages = 1;
	private boolean skipBlur = false;

	public ComboBox() {
		this.selector = new TextBox();
		this.selector.addKeyDownHandler(this);

		this.selector	.getElement()
						.getStyle()
						.setProperty("padding", "0 16px");
		this.selector.setStyleName("c-combobox-input");
		this.selector.addBlurHandler(this);

		this.drop = new Button();
		this.drop.setStyleName("c-combobox-button");
		this.drop.addClickHandler(this.dropDownClickHandler);

		FlowPanel content = new FlowPanel();
		content.setStyleName("v-widget v-has-width v-filterselect v-filterselect-prompt");
		content.setWidth("100%");

		content.add(this.selector);
		content.add(this.drop);

		initWidget(content);

	}

	public void updateSelection(List<OptionElement> selection) {
		openDropdown(selection);
	}

	public void updatePageAmount(int pages) {
		this.pages = pages;
	}

	public OptionElement getValue() {
		return this.selected;
	}

	public void setSelected(OptionElement selected) {
		OptionElement old = this.selected;
		this.selector.setValue(selected.getName());
		this.selected = selected;

		if (!old.equals(selected)) {
			this.eventHandler.change(selected);
		}
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public void setEventHandler(EventHandler<OptionElement> eventHandler) {
		this.eventHandler = eventHandler;
	}

	public HandlerRegistration addChangeHandler(ChangeHandler handler) {
		return addDomHandler(handler, ChangeEvent.getType());
	}

	public void setSelection(OptionElement selection) {
		this.selected = selection;
		this.selector.setValue(selection.getName());
	}

	public boolean isEnabled() {
		return this.selector.isEnabled();
	}

	public void setEnabled(boolean enabled) {
		this.selector.setEnabled(enabled);
	}

	private void openDropdown(List<OptionElement> items) {
		boolean focus = false;
		if (this.popup != null) {
			focus = this.popup.isJustClosed();
			if (this.popup.isVisible())
				this.popup.hide(true);
		}
		this.popup = new ComboBoxPopup(items);
		this.popup.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> closeEvent) {
				ComboBox.this.popup.removePopupCallback(ComboBox.this.eventListener);
			}
		});
		this.popup.addPopupCallback(this.eventListener);

		this.popup.setPreviousPageEnabled(this.currentPage > 0);
		this.popup.setNextPageEnabled(this.currentPage < this.pages - 1);

		this.popup.setWidth(getOffsetWidth() + "px");
		this.popup	.getElement()
					.getStyle()
					.setZIndex(1000);
		this.popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int top = ComboBox.this.getAbsoluteTop() + ComboBox.this.getOffsetHeight();
				if (top + ComboBox.this.getOffsetHeight() > Window.getClientHeight()) {
					top = ComboBox.this.getAbsoluteTop() - ComboBox.this.getOffsetHeight();
				}
				ComboBox.this.popup.setPopupPosition(ComboBox.this.getAbsoluteLeft(), top);
			}
		});
		this.skipBlur = true;
		this.popup.focusSelection(this.selected, focus);
	}

	// -- Handlers --

	@Override
	public void onKeyDown(KeyDownEvent event) {

		switch (event.getNativeKeyCode()) {
		case KeyCodes.KEY_ESCAPE:
			event.preventDefault();
			event.stopPropagation();
			if (this.popup != null && this.popup.isVisible()) {
				this.popup.hide(true);
				this.popup = null;
			}
			this.eventHandler.clearFilter();

			this.selector.setValue(this.selected.getName());
			break;
		case KeyCodes.KEY_DOWN:
			event.preventDefault();
			event.stopPropagation();

			// Focus popup if open else open popup with first page
			if (this.popup != null && this.popup.isAttached()) {
				this.popup.focusSelection(this.selected, true);
			} else {
				// Start from page with selection when opening.
				this.currentPage = -1;
				this.eventHandler.getPage(this.currentPage);
			}
			break;
		case KeyCodes.KEY_TAB:
			if (this.popup != null && this.popup.isAttached()) {
				this.popup.hide(true);
			}
			this.selector.setValue(this.selected.getName());
			break;
		}

		RegExp regex = RegExp.compile("^[a-zA-Z0-9]+$");
		if (!regex.test("" + (char) event.getNativeKeyCode()) && event.getNativeKeyCode() != KeyCodes.KEY_BACKSPACE) {
			return;
		}

		if (this.t == null)
			this.t = new Timer() {
				@Override
				public void run() {
					ComboBox.this.currentPage = 0;
					ComboBox.this.eventHandler.filter(ComboBox.this.selector.getValue(), ComboBox.this.currentPage);
					ComboBox.this.t = null;
				}
			};
		this.t.schedule(300);
	}

	@Override
	public void onBlur(BlurEvent event) {
		if (!this.skipBlur) {
			if (this.popup != null && this.popup.isAttached()) {
				this.popup.hide();
			}
			this.selector.setValue(this.selected.getName());
			this.skipBlur = false;
		}
	}

	private ClickHandler dropDownClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			if (ComboBox.this.popup != null && ComboBox.this.popup.isAttached()) {
				ComboBox.this.popup.hide();
				ComboBox.this.popup = null;
			} else if (ComboBox.this.popup == null || !ComboBox.this.popup.isJustClosed()) {
				// Start from page where selection is when opening.
				ComboBox.this.currentPage = -1;
				ComboBox.this.eventHandler.getPage(ComboBox.this.currentPage);
			}
			ComboBox.this.selector.setFocus(true);
		}
	};

	PopupCallback<OptionElement> eventListener = new PopupCallback<OptionElement>() {
		@Override
		public void itemSelected(OptionElement item) {
			setSelected(item);
			ComboBox.this.selector.setFocus(true);
			ComboBox.this.eventHandler.clearFilter();
		}

		@Override
		public void nextPage() {
			ComboBox.this.eventHandler.getPage(++ComboBox.this.currentPage);
		}

		@Override
		public void prevPage() {
			ComboBox.this.eventHandler.getPage(--ComboBox.this.currentPage);
		}

		@Override
		public void itemsSelected(Set<OptionElement> selectedObjects) {
			// NOOP
		}

		@Override
		public void focus() {
			// TODO
		}

		@Override
		public void selectAll() {
			// NOOP
		}

		@Override
		public void deselectAll() {
			// NOOP
		}

		@Override
		public void setSkipBlur(boolean skipBlur) {
			VConsole.error("setSkipBlur: " + skipBlur);
			ComboBox.this.skipBlur = skipBlur;
		}
	};
}
