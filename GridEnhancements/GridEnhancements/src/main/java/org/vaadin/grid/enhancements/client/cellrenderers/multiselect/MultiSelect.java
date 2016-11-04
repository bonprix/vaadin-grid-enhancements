package org.vaadin.grid.enhancements.client.cellrenderers.multiselect;

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
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.EventHandler;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.PopupCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class MultiSelect extends Composite implements KeyDownHandler, BlurHandler, HasChangeHandlers {

	private MultiSelectPopup popup = null;

	private Set<ComboBoxMultiselectOption> selected = new HashSet<ComboBoxMultiselectOption>();

	private TextBox selector;
	private Button drop;

	private EventHandler<ComboBoxMultiselectOption> eventHandler;

	private Timer t = null;
	// Page starts as page 0
	private int currentPage = 0;

	private int pages = 1;
	private boolean skipBlur = false;

	public MultiSelect() {
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

	public void updateSelection(List<ComboBoxMultiselectOption> selection) {
		openDropdown(selection);
	}

	public void updatePageAmount(int pages) {
		this.pages = pages;
	}

	public Set<ComboBoxMultiselectOption> getValue() {
		return this.selected;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public void setEventHandler(EventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public HandlerRegistration addChangeHandler(ChangeHandler handler) {
		return addDomHandler(handler, ChangeEvent.getType());
	}

	public void setSelection(Set<ComboBoxMultiselectOption> selection) {
		this.selected = selection;
		// TODO
		this.selector.setValue(selection.toString());
	}

	public boolean isEnabled() {
		return this.selector.isEnabled();
	}

	public void setEnabled(boolean enabled) {
		this.selector.setEnabled(enabled);
	}

	private void openDropdown(List<ComboBoxMultiselectOption> items) {
		boolean focus = false;
		if (this.popup != null) {
			focus = this.popup.isJustClosed();
			if (this.popup.isVisible())
				this.popup.hide(true);
		}
		this.popup = new MultiSelectPopup(items);
		this.popup.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> closeEvent) {
				MultiSelect.this.popup.removePopupCallback(MultiSelect.this.eventListener);
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
				int top = MultiSelect.this.getAbsoluteTop() + MultiSelect.this.getOffsetHeight();
				if (top + MultiSelect.this.getOffsetHeight() > Window.getClientHeight()) {
					top = MultiSelect.this.getAbsoluteTop() - MultiSelect.this.getOffsetHeight();
				}
				MultiSelect.this.popup.setPopupPosition(MultiSelect.this.getAbsoluteLeft(), top);
			}
		});
		this.skipBlur = true;
		this.popup.setCurrentSelection(this.selected);
		// TODO?!
		// this.popup.focusSelection(this.selected, focus);
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
			// TODO
			this.selector.setValue(this.selected.toString());
			break;
		case KeyCodes.KEY_DOWN:
			event.preventDefault();
			event.stopPropagation();

			// Focus popup if open else open popup with first page
			if (this.popup != null && this.popup.isAttached()) {
				// TODO?!
				// this.popup.focusSelection(this.selected, true);
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
			// TODO
			this.selector.setValue(this.selected.toString());
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
					MultiSelect.this.currentPage = 0;
					MultiSelect.this.eventHandler.filter(	MultiSelect.this.selector.getValue(),
															MultiSelect.this.currentPage);
					MultiSelect.this.t = null;
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
			// TODO
			this.selector.setValue(this.selected.toString());
			this.skipBlur = false;
		}
	}

	private ClickHandler dropDownClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			if (MultiSelect.this.popup != null && MultiSelect.this.popup.isAttached()) {
				MultiSelect.this.popup.hide();
				MultiSelect.this.popup = null;
			} else if (MultiSelect.this.popup == null || !MultiSelect.this.popup.isJustClosed()) {
				// Start from page where selection is when opening.
				MultiSelect.this.currentPage = -1;
				MultiSelect.this.eventHandler.getPage(MultiSelect.this.currentPage);
			}
			MultiSelect.this.selector.setFocus(true);
		}
	};

	PopupCallback<ComboBoxMultiselectOption> eventListener = new PopupCallback<ComboBoxMultiselectOption>() {
		@Override
		public void itemSelected(ComboBoxMultiselectOption item) {
			// NOOP
		}

		@Override
		public void nextPage() {
			MultiSelect.this.eventHandler.getPage(++MultiSelect.this.currentPage);
		}

		@Override
		public void prevPage() {
			MultiSelect.this.eventHandler.getPage(--MultiSelect.this.currentPage);
		}

		@Override
		public void clear() {
			MultiSelect.this.selector.setFocus(true);
			MultiSelect.this.eventHandler.clearFilter();
		}

		@Override
		public void itemsSelected(Set<ComboBoxMultiselectOption> selectedObjects) {
			MultiSelect.this.selected.clear();
			MultiSelect.this.selected.addAll(selectedObjects);
			MultiSelect.this.eventHandler.change(selectedObjects);
			// TODO: update selected items
		}
	};
}
