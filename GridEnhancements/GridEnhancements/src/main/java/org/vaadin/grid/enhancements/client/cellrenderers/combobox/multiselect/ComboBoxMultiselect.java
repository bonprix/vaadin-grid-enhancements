package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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
public class ComboBoxMultiselect extends Composite
		implements KeyDownHandler, FocusHandler, BlurHandler, HasChangeHandlers {

	private static final String PROMPT_STYLE = "v-filterselect-prompt";

	private ComboBoxMultiselectPopup popup = null;

	private Set<OptionElement> selected = new HashSet<OptionElement>();

	private TextBox textBox;
	private Button dropDownButton;

	private EventHandler<OptionElement> eventHandler;

	private Timer t = null;
	// Page starts as page 0
	private int currentPage = 0;
	private String inputPrompt;
	private String selectAllText;
	private String deselectAllText;

	private int pages = 1;
	private boolean skipFocus = false;
	private boolean skipBlur = false;

	private boolean prevPage = false;

	public ComboBoxMultiselect() {

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

	public void setSelectAllText(String selectAllText) {
		this.selectAllText = selectAllText;
	}

	public void setDeselectAllText(String deselectAllText) {
		this.deselectAllText = deselectAllText;
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

	public void setSelection(Set<OptionElement> selection, boolean refreshPage) {
		this.selected = selection;
		if (refreshPage) {
			this.eventHandler.getPage(0);
			return;
		}

		if (this.popup == null || !this.popup.isAttached()) {
			this.textBox.setValue(getTextFieldValue(selection));
		} else if (this.popup != null) {
			this.popup.focusSelectionCurrent(true);
		}
	}

	public boolean isEnabled() {
		return this.textBox.isEnabled();
	}

	public void setEnabled(boolean enabled) {
		this.textBox.setEnabled(enabled);
	}

	private void updateAndShowDropdown(List<OptionElement> items) {
		this.skipBlur = false;

		boolean focus = false;
		if (this.popup != null) {
			focus = this.popup.isJustClosed();
			if (this.popup.isVisible())
				this.popup.hide(true);
		}
		this.popup = new ComboBoxMultiselectPopup(items, this.selectAllText, this.deselectAllText);
		this.popup.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> closeEvent) {
				ComboBoxMultiselect.this.popup.removePopupCallback(ComboBoxMultiselect.this.eventListener);
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
				int top = ComboBoxMultiselect.this.getAbsoluteTop() + ComboBoxMultiselect.this.getOffsetHeight();
				if (top + ComboBoxMultiselect.this.getOffsetHeight() > Window.getClientHeight()) {
					top = ComboBoxMultiselect.this.getAbsoluteTop() - ComboBoxMultiselect.this.getOffsetHeight();
				}
				ComboBoxMultiselect.this.popup.setPopupPosition(ComboBoxMultiselect.this.getAbsoluteLeft(), top);
			}
		});
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
		case KeyCodes.KEY_TAB:
			this.skipBlur = false;
			break;
		case KeyCodes.KEY_ESCAPE:
			event.preventDefault();
			event.stopPropagation();
			if (this.popup != null && this.popup.isVisible()) {
				this.popup.hide(true);
				this.popup = null;
			}
			break;
		case KeyCodes.KEY_ENTER:
			event.preventDefault();
			event.stopPropagation();
			if (this.popup != null && this.popup.isVisible()) {
				this.popup.toggleSelectionOfCurrentFocus();
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
		}

		RegExp regex = RegExp.compile("^[a-zA-Z0-9]+$");
		if (!regex.test("" + (char) event.getNativeKeyCode()) && event.getNativeKeyCode() != KeyCodes.KEY_BACKSPACE) {
			return;
		}

		if (this.t == null)
			this.t = new Timer() {
				@Override
				public void run() {
					ComboBoxMultiselect.this.currentPage = 0;
					ComboBoxMultiselect.this.eventHandler.filter(	ComboBoxMultiselect.this.textBox.getValue(),
																	ComboBoxMultiselect.this.currentPage);
					ComboBoxMultiselect.this.t = null;
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

		if (ComboBoxMultiselect.this.popup == null || !ComboBoxMultiselect.this.popup.isAttached()
				|| !ComboBoxMultiselect.this.popup.isJustClosed()) {
			this.textBox.removeStyleDependentName(PROMPT_STYLE);
			ComboBoxMultiselect.this.textBox.setValue("");
		}
	}

	@Override
	public void onBlur(BlurEvent event) {
		if (this.skipBlur) {
			return;
		}

		if (this.popup != null && this.popup.isAttached()) {
			this.popup.hide();
		}
		this.textBox.setValue(getTextFieldValue(this.selected));
		this.eventHandler.clearFilter();
	}

	private ClickHandler dropDownClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			if (ComboBoxMultiselect.this.popup != null && ComboBoxMultiselect.this.popup.isAttached()) {
				ComboBoxMultiselect.this.popup.hide();
				ComboBoxMultiselect.this.popup = null;
			} else if (ComboBoxMultiselect.this.popup == null || !ComboBoxMultiselect.this.popup.isJustClosed()) {
				// Start from page where selection is when opening.
				ComboBoxMultiselect.this.currentPage = -1;
				ComboBoxMultiselect.this.eventHandler.getPage(ComboBoxMultiselect.this.currentPage);
			}

			ComboBoxMultiselect.this.textBox.setFocus(true);
		}
	};

	PopupCallback<OptionElement> eventListener = new PopupCallback<OptionElement>() {
		@Override
		public void itemSelected(OptionElement item) {
			// NOOP
		}

		@Override
		public void nextPage() {
			ComboBoxMultiselect.this.eventHandler.getPage(++ComboBoxMultiselect.this.currentPage);
		}

		@Override
		public void prevPage() {
			ComboBoxMultiselect.this.eventHandler.getPage(--ComboBoxMultiselect.this.currentPage);
		}

		@Override
		public void itemsSelected(Set<OptionElement> selectedObjects) {
			ComboBoxMultiselect.this.selected.clear();
			ComboBoxMultiselect.this.selected.addAll(selectedObjects);
			ComboBoxMultiselect.this.eventHandler.change(selectedObjects);
		}

		@Override
		public void focus() {
			ComboBoxMultiselect.this.skipFocus = true;
			ComboBoxMultiselect.this.textBox.setFocus(true);
		}

		@Override
		public void selectAll() {
			ComboBoxMultiselect.this.eventHandler.selectAll();
		}

		@Override
		public void deselectAll() {
			ComboBoxMultiselect.this.eventHandler.deselectAll();
		}

		@Override
		public void setSkipBlur(boolean skipBlur) {
			ComboBoxMultiselect.this.skipBlur = skipBlur;
		}
	};

	private String getTextFieldValue(Set<OptionElement> selection) {
		if (selection.size() == 0) {
			this.textBox.addStyleName(PROMPT_STYLE);
			return this.inputPrompt;
		}

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
							.compareTo(o2.getName());
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

}
