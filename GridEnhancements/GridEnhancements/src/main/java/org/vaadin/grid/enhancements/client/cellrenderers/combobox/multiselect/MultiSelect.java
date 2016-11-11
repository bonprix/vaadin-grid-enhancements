package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.EventHandler;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.PopupCallback;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class MultiSelect extends Composite implements KeyDownHandler, FocusHandler, BlurHandler, HasChangeHandlers {

	private MultiselectPopup popup = null;

	private Set<OptionElement> selected = new HashSet<OptionElement>();

	private TextBox selector;
	private Button drop;

	private EventHandler<OptionElement> eventHandler;

	private Timer t = null;
	// Page starts as page 0
	private int currentPage = 0;

	private int pages = 1;
	private boolean skipBlur = false;

	private boolean prevPage = false;

	public MultiSelect() {
		this.selector = new TextBox();
		this.selector.addKeyDownHandler(this);

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
		updateAndShowDropdown(selection);
	}

	public void updatePageAmount(int pages) {
		this.pages = pages;
	}

	public Set<OptionElement> getValue() {
		return this.selected;
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

	public void setSelection(Set<OptionElement> selection) {
		this.selected = selection;
		if (this.popup == null || !this.popup.isAttached()) {
			this.selector.setValue(getTextFieldValue(selection));
		} else if (this.popup != null) {
			this.popup.focusSelectionCurrent(true);
		}
	}

	public boolean isEnabled() {
		return this.selector.isEnabled();
	}

	public void setEnabled(boolean enabled) {
		this.selector.setEnabled(enabled);
	}

	private void updateAndShowDropdown(List<OptionElement> items) {
		boolean focus = false;
		if (this.popup != null) {
			focus = this.popup.isJustClosed();
			if (this.popup.isVisible())
				this.popup.hide(true);
		}
		this.popup = new MultiselectPopup(items);
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
		this.popup.setCurrentSelection(this.selected);

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
		this.popup.updateElementCss();

		if (this.prevPage) {
			this.popup.focusSelectionLast(focus);
		} else {
			this.popup.focusSelectionFirst(focus);
		}

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
			this.selector.setValue(getTextFieldValue(this.selected));
			break;
		case KeyCodes.KEY_ENTER:
			event.preventDefault();
			event.stopPropagation();
			if (this.popup != null && this.popup.isVisible()) {
				this.popup.selectCurrentFocus();
			}
			break;
		case KeyCodes.KEY_UP:
			// check if popup is open
			if (this.popup != null && this.popup.isAttached()) {
				event.preventDefault();
				event.stopPropagation();

				// if first element in visible list is already selected
				if (this.popup.isFirstElementFocused()) {
					// and previous page is available
					// open previous page
					if (this.popup.isPreviousPageAvailable()) {
						this.prevPage = true;
						this.popup.prevPage();
					}
					break;
				}

				// Focus popup if open else open popup with first page
				this.popup.focusSelectionViaNativeKey(KeyCodes.KEY_UP, true);
			}
			break;
		case KeyCodes.KEY_DOWN:
			event.preventDefault();
			event.stopPropagation();

			// check if popup is open
			if (this.popup != null && this.popup.isAttached()) {

				// if last element in visible list is already selected

				if (this.popup.isLastElementFocused()) {
					// and next page is available
					// open next page
					if (this.popup.isNextPageAvailable()) {
						this.prevPage = false;
						this.popup.nextPage();
					}
					break;
				}

				// Focus popup next element in popup
				this.popup.focusSelectionViaNativeKey(KeyCodes.KEY_DOWN, true);
				break;
			}

			// Start from page with selection when opening.
			this.currentPage = -1;
			this.prevPage = false;
			this.eventHandler.getPage(this.currentPage);

			break;
		case KeyCodes.KEY_TAB:
			if (this.popup != null && this.popup.isAttached()) {
				this.popup.hide(true);
			}
			this.selector.setValue(getTextFieldValue(this.selected));
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
			this.selector.setValue(getTextFieldValue(this.selected));
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

	PopupCallback<OptionElement> eventListener = new PopupCallback<OptionElement>() {
		@Override
		public void itemSelected(OptionElement item) {
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
		public void itemsSelected(Set<OptionElement> selectedObjects) {
			MultiSelect.this.selected.clear();
			MultiSelect.this.selected.addAll(selectedObjects);
			MultiSelect.this.eventHandler.change(selectedObjects);
		}
	};

	private String getTextFieldValue(Set<OptionElement> selection) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("(" + selection.size() + ") ");

		List<OptionElement> orderedList = new ArrayList<OptionElement>(selection);
		Collections.sort(orderedList, new Comparator<OptionElement>() {

			@Override
			public int compare(OptionElement o1, OptionElement o2) {
				if (o1.getName() == null) {
					return -1;
				}
				return o1	.getName()
							.compareToIgnoreCase(o2.getName());
			}
		});

		boolean first = true;
		for (OptionElement comboBoxMultiselectOption : orderedList) {
			if (first) {
				first = false;
			} else {
				stringBuffer.append(", ");
			}
			stringBuffer.append(comboBoxMultiselectOption.getName());
		}
		return stringBuffer.toString();
	}

	@Override
	public void onFocus(FocusEvent event) {
		if (MultiSelect.this.popup == null || !MultiSelect.this.popup.isAttached()
				|| !MultiSelect.this.popup.isJustClosed()) {
			MultiSelect.this.selector.setValue("");
		}
	}
}
