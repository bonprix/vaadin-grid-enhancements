package org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect;

import java.util.List;
import java.util.Set;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.EventHandler;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.PopupCallback;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;

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

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBox extends Composite implements KeyDownHandler, FocusHandler, BlurHandler, HasChangeHandlers {

	private static final String PROMPT_STYLE = "v-filterselect-prompt";

	private enum SelectionType {
		FIRST_ELEMENT, LAST_ELEMENT, SELECTED_ELEMENT
	}

	private ComboBoxPopup popup = null;

	private OptionElement selected;

	private TextBox textBox;
	private Button dropDownButton;

	private EventHandler<OptionElement> eventHandler;

	private Timer t = null;
	// Page starts as page 0
	private int currentPage = 0;
	private String inputPrompt;

	private int pages = 1;
	private boolean skipFocus = false;
	private boolean skipBlur = false;

	private SelectionType selectionType = SelectionType.FIRST_ELEMENT;

	public ComboBox() {
		this.textBox = new TextBox();
		this.textBox.setStyleName("c-combobox-input");
		this.textBox.addKeyDownHandler(this);
		this.textBox.addFocusHandler(this);
		this.textBox.addBlurHandler(this);

		this.dropDownButton = new Button();
		this.dropDownButton.setStyleName("c-combobox-button");
		this.dropDownButton.addClickHandler(this.dropDownClickHandler);

		FlowPanel content = new FlowPanel();
		content.setStyleName("v-widget v-has-width v-filterselect v-filterselect-prompt");
		content.setWidth("100%");

		content.add(this.textBox);
		content.add(this.dropDownButton);

		this.popup = new ComboBoxPopup();

		initWidget(content);

	}

	@Override
	protected void onAttach() {
		super.onAttach();
		this.dropDownButton.setTabIndex(-1);
	}

	public void setInputPrompt(String inputPrompt) {
		this.inputPrompt = inputPrompt;
	}

	public void updateSelection(List<OptionElement> selection) {
		updateAndShowDropdown(selection);
	}

	public void updatePageAmount(int pages) {
		this.pages = pages;
	}

	public OptionElement getValue() {
		return this.selected;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public void setSelected(OptionElement selected) {
		OptionElement old = this.selected;
		updateTextFieldValue(selected);
		this.selected = selected;

		if (!old.equals(selected)) {
			this.eventHandler.change(selected);
		}
	}

	public void setEventHandler(EventHandler<OptionElement> eventHandler) {
		this.eventHandler = eventHandler;
	}

	@Override
	public HandlerRegistration addChangeHandler(ChangeHandler handler) {
		return addDomHandler(handler, ChangeEvent.getType());
	}

	public boolean isEnabled() {
		return this.textBox.isEnabled();
	}

	public void setEnabled(boolean enabled) {
		this.textBox.setEnabled(enabled);
	}

	private void updateAndShowDropdown(List<OptionElement> options) {
		this.skipBlur = true;

		this.popup.setOptions(options);

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
			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {
				int top = ComboBox.this.getAbsoluteTop() + ComboBox.this.getOffsetHeight();
				if (top + ComboBox.this.getOffsetHeight() > Window.getClientHeight()) {
					top = ComboBox.this.getAbsoluteTop() - ComboBox.this.getOffsetHeight();
				}
				ComboBox.this.popup.setPopupPosition(ComboBox.this.getAbsoluteLeft(), top);
			}
		});

		this.popup.updateElementCss();

		switch (this.selectionType) {
		case FIRST_ELEMENT:
			this.popup.focusSelectionFirst(true);
			break;
		case LAST_ELEMENT:
			this.popup.focusSelectionLast(true);
			break;
		case SELECTED_ELEMENT:
			this.popup.focusSelectionSelected(this.selected, true);
			break;
		default:
			break;
		}
	}

	// -- Handlers --

	@Override
	public void onKeyDown(KeyDownEvent event) {

		switch (event.getNativeKeyCode()) {
		case KeyCodes.KEY_TAB:
			this.skipBlur = false;
			break;
		case KeyCodes.KEY_ESCAPE:
			event.preventDefault();
			event.stopPropagation();
			if (this.popup.isShowing()) {
				this.popup.hide(true);
			}
			this.eventHandler.clearFilter();

			updateTextFieldValue(this.selected);
			break;
		case KeyCodes.KEY_ENTER:
			event.preventDefault();
			event.stopPropagation();
			if (this.popup.isShowing()) {
				this.popup.selectCurrent();
			}
			break;
		case KeyCodes.KEY_UP:
			// check if popup is open
			if (this.popup.isShowing()) {
				event.preventDefault();
				event.stopPropagation();

				// if first element in visible list is already selected
				if (this.popup.isFirstElementFocused()) {
					// and previous page is available
					// open previous page
					if (this.popup.isPreviousPageAvailable()) {
						this.selectionType = SelectionType.LAST_ELEMENT;
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
			if (this.popup.isAttached() && this.popup.isShowing()) {

				// if last element in visible list is already selected

				if (this.popup.isLastElementFocused()) {
					// and next page is available
					// open next page
					if (this.popup.isNextPageAvailable()) {
						this.selectionType = SelectionType.FIRST_ELEMENT;
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
			this.selectionType = SelectionType.SELECTED_ELEMENT;
			this.eventHandler.getPage(this.currentPage, false);
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
					ComboBox.this.selectionType = SelectionType.FIRST_ELEMENT;
					ComboBox.this.eventHandler.filter(	ComboBox.this.textBox.getValue(), ComboBox.this.currentPage,
														false);
					ComboBox.this.t = null;
				}
			};
		this.t.schedule(300);
	}

	@Override
	public void onFocus(FocusEvent event) {
		if (this.skipFocus) {
			this.skipFocus = false;
			return;
		}

		if (!ComboBox.this.popup.isShowing() || !ComboBox.this.popup.isJustClosed()) {
			removeStyleDependentName(PROMPT_STYLE);
		}
	}

	@Override
	public void onBlur(BlurEvent event) {
		if (!this.skipBlur) {
			if (this.popup.isShowing()) {
				this.popup.hide();
			}
			updateTextFieldValue(this.selected);
			this.skipBlur = false;
		}
	}

	private ClickHandler dropDownClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			if (ComboBox.this.popup.isShowing()) {
				ComboBox.this.popup.hide();
			} else if (!ComboBox.this.popup.isJustClosed()) {
				// Start from page where selection is when opening.
				ComboBox.this.currentPage = -1;
				ComboBox.this.eventHandler.getPage(ComboBox.this.currentPage, false);
			}
			ComboBox.this.textBox.setFocus(true);
		}
	};

	PopupCallback<OptionElement> eventListener = new PopupCallback<OptionElement>() {
		@Override
		public void itemSelected(OptionElement item) {
			setSelected(item);
			ComboBox.this.textBox.setFocus(true);
			ComboBox.this.eventHandler.clearFilter();
			ComboBox.this.popup.hide();
		}

		@Override
		public void nextPage() {
			ComboBox.this.eventHandler.getPage(++ComboBox.this.currentPage, false);
		}

		@Override
		public void prevPage() {
			ComboBox.this.eventHandler.getPage(--ComboBox.this.currentPage, false);
		}

		@Override
		public void itemsSelected(Set<OptionElement> selectedObjects) {
			// NOOP
		}

		@Override
		public void focus() {
			ComboBox.this.skipFocus = true;
			ComboBox.this.textBox.setFocus(true);
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
			ComboBox.this.skipBlur = skipBlur;
		}
	};

	private void updateTextFieldValue(OptionElement selection) {
		if (selection == null) {
			addStyleName(PROMPT_STYLE);
			this.textBox.setValue(this.inputPrompt);
			return;
		}

		removeStyleName(PROMPT_STYLE);
		this.textBox.setValue(selection.getName());
	}

	public ComboBoxPopup getPopup() {
		return this.popup;
	}
}
