package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.DeselectAllOptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.PopupCallback;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.SelectAllOptionElement;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.impl.StringCase;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.vaadin.client.VConsole;
import com.vaadin.client.ui.VOverlay;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
@SuppressWarnings("deprecation")
public class MultiselectPopup extends VOverlay
		implements FocusHandler, MouseMoveHandler, CellPreviewEvent.Handler<OptionElement> {

	private static final String SELECTED_ROW_CLASS = "gwt-MenuItem-selected";

	private CellList<OptionElement> optionList;
	private List<OptionElement> options;

	private Button up, down;
	private Set<HandlerRegistration> handlers = new HashSet<HandlerRegistration>();

	private PopupCallback<OptionElement> callback;
	// Not as items on page
	private HashSet<OptionElement> currentSelections = new HashSet<OptionElement>();
	private boolean updatingSelection = false;

	private long lastAutoClosed;

	int focusedPosition = -1;

	public MultiselectPopup(List<OptionElement> options) {
		super(true);

		this.options = options;

		setStyleName("v-filterselect-suggestpopup");

		initOptionList();

		initHandlers();

		add(layout());
	}

	private void initOptionList() {
		DeselectAllOptionElement deselectAllOption = new DeselectAllOptionElement("clear");
		if (!this.options.contains(deselectAllOption)) {
			this.options.add(0, deselectAllOption);
		}

		SelectAllOptionElement selectAllOption = new SelectAllOptionElement("select all");
		if (!this.options.contains(selectAllOption)) {
			this.options.add(0, selectAllOption);
		}

		this.optionList = new CellList<OptionElement>(new OptionCell());
		this.optionList.setStyleName("v-filterselect-suggestmenu");
		this.optionList.setPageSize(this.options.size());
		this.optionList.setRowCount(this.options.size(), true);
		this.optionList.setRowData(0, this.options);
		this.optionList.setVisibleRange(0, this.options.size());
		this.optionList.setWidth("100%");
		this.optionList.setKeyboardPagingPolicy(HasKeyboardPagingPolicy.KeyboardPagingPolicy.INCREASE_RANGE);
		this.optionList.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
		this.optionList.addHandler(this, FocusEvent.getType());
	}

	private Widget layout() {
		VerticalPanel layout = new VerticalPanel();
		layout.setWidth("100%");

		this.up = new Button("");
		this.up	.getElement()
				.removeAttribute("type");
		this.up.setStyleName("c-combo-popup-prevpage");
		this.up.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				MultiselectPopup.this.callback.prevPage();
			}
		});

		this.down = new Button("");
		this.down	.getElement()
					.removeAttribute("type");
		this.down.setStyleName("c-combo-popup-nextpage");
		this.down.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				MultiselectPopup.this.callback.nextPage();
			}
		});

		// Add widgets to content panel
		layout.add(this.up);
		layout.add(this.optionList);
		layout.add(this.down);

		return layout;
	}

	private void initHandlers() {
		addCloseHandler(this);

		// Remove all handlers if any exist for any reason
		if (!this.handlers.isEmpty()) {
			for (HandlerRegistration handler : this.handlers) {
				handler.removeHandler();
			}
		}

		// Add CellList listeners
		this.handlers.add(this.optionList.addBitlessDomHandler(this, MouseMoveEvent.getType()));
		this.handlers.add(this.optionList.addCellPreviewHandler(this));
	}

	public void setNextPageEnabled(boolean nextPageEnabled) {
		this.down.setVisible(nextPageEnabled);
	}

	public void setPreviousPageEnabled(boolean prevPageEnabled) {
		this.up.setVisible(prevPageEnabled);
	}

	/**
	 * Move keyboard focus to the selected item if found in current options
	 * 
	 * @param nativeKeyCode
	 *
	 * @param stealFocus
	 *            true to focus new row
	 */
	public void focusSelectionViaNativeKey(int nativeKeyCode, boolean stealFocus) {
		int newPosition = this.focusedPosition;

		switch (nativeKeyCode) {
		case KeyCodes.KEY_UP:
			newPosition--;
			break;
		case KeyCodes.KEY_DOWN:
			newPosition++;
			break;
		}

		focusSelectionViaPosition(newPosition, stealFocus);
	}

	public void focusSelectionFirst(boolean focus) {
		int cntOptionalButtons = 0;
		for (int i = 0; i < this.options.size(); i++) {
			OptionElement option = this.options.get(i);
			if (option instanceof SelectAllOptionElement || option instanceof DeselectAllOptionElement) {
				cntOptionalButtons++;
				continue;
			}
			if (!this.currentSelections.contains(option)) {
				focusSelectionViaPosition(i, focus);
				return;
			}
		}

		focusSelectionViaPosition(cntOptionalButtons, focus);
	}

	public void focusSelectionLast(boolean focus) {
		focusSelectionViaPosition(this.optionList	.getVisibleItems()
													.size()
				- 1, focus);
	}

	public void focusSelectionCurrent(boolean focus) {
		focusSelectionViaPosition(this.focusedPosition, focus);
	}

	public void focusSelectionViaPosition(int newPosition, boolean stealFocus) {
		if (stealFocus && this.focusedPosition > -1 && this.focusedPosition < this.optionList	.getVisibleItems()
																								.size()) {
			this.optionList	.getRowElement(this.focusedPosition)
							.removeClassName(SELECTED_ROW_CLASS);
		}

		this.focusedPosition = newPosition;

		VConsole.error("focusedPosition: " + this.focusedPosition);

		this.optionList	.getRowElement(this.focusedPosition)
						.addClassName(SELECTED_ROW_CLASS);

	}

	/**
	 * Set the current selection set for multiselection mode
	 *
	 * @param currentSelections
	 */
	public void setCurrentSelection(Set<OptionElement> currentSelections) {
		// Lock selection event so we don't send change events
		this.updatingSelection = true;
		this.currentSelections.clear();
		this.currentSelections.addAll(currentSelections);
		this.updatingSelection = false;
	}

	/**
	 * Add a popup callback
	 *
	 * @param callback
	 *            ComboBox callback
	 */
	public void addPopupCallback(PopupCallback<OptionElement> callback) {
		this.callback = callback;
	}

	/**
	 * Remove popup callback
	 *
	 * @param callback
	 */
	public void removePopupCallback(PopupCallback<OptionElement> callback) {
		this.callback = null;
	}

	public boolean isJustClosed() {
		final long now = (new Date()).getTime();
		return (this.lastAutoClosed > 0 && (now - this.lastAutoClosed) < 200);
	}

	@Override
	public void onClose(CloseEvent<PopupPanel> event) {
		if (event.isAutoClosed()) {
			this.lastAutoClosed = (new Date()).getTime();
		}
		if (!this.handlers.isEmpty()) {
			for (HandlerRegistration handler : this.handlers) {
				handler.removeHandler();
			}
		}
	}

	/**
	 * CellList cell content renderer implementation
	 */
	private class OptionCell extends AbstractCell<OptionElement> {

		@Override
		public void render(Context context, final OptionElement option, SafeHtmlBuilder sb) {
			if (option instanceof SelectAllOptionElement || option instanceof DeselectAllOptionElement) {
				sb.appendHtmlConstant("<span class='align-center'>");
				sb.appendEscaped(option.getName());
				sb.appendHtmlConstant("</span>");
				return;
			}

			final CheckBox checkBox = new CheckBox();
			checkBox.setValue(MultiselectPopup.this.currentSelections.contains(option));
			sb.appendHtmlConstant(checkBox	.getElement()
											.getString());
			sb.appendHtmlConstant("<span>");
			sb.appendEscaped(option.getName());
			sb.appendHtmlConstant("</span>");
		}
	}

	// --- Event handler implementations ---
	@Override
	public void onMouseMove(MouseMoveEvent event) {
		Element target = event	.getNativeEvent()
								.getEventTarget()
								.cast();
		for (int i = 0; i < this.optionList	.getVisibleItems()
											.size(); i++) {
			Element e = this.optionList.getRowElement(i);
			if (e.equals(target)) {
				focusSelectionViaPosition(i, true);
				this.callback.setSkipBlur(true);
				return;
			}
		}

		this.callback.setSkipBlur(false);
	}

	@Override
	public void onFocus(FocusEvent event) {
		if (MultiselectPopup.this.callback != null) {
			MultiselectPopup.this.callback.focus();
		}
	}

	@Override
	public void onCellPreview(CellPreviewEvent<OptionElement> event) {

		if (BrowserEvents.CLICK.equals(event.getNativeEvent()
											.getType())) {
			// Do not handle selection for clicks into CheckBoxes
			// as the selectionEventManager already does this.
			Element target = event	.getNativeEvent()
									.getEventTarget()
									.cast();
			if ("input".equals(StringCase.toLower(target.getTagName()))) {
				final InputElement input = target.cast();
				if ("checkbox".equals(StringCase.toLower(input.getType()))) {
					return;
				}
			}
			final OptionElement option = event.getValue();
			toggleSelection(option);
			event.setCanceled(true);
			event	.getNativeEvent()
					.preventDefault();
			event	.getNativeEvent()
					.stopPropagation();
		}
	}

	public void updateElementCss() {
		for (int i = 0; i < this.optionList.getRowCount(); i++) {
			this.optionList	.getRowElement(i)
							.addClassName("gwt-MenuItem");
		}
	}

	public boolean isLastElementFocused() {
		return this.focusedPosition == this.optionList	.getVisibleItems()
														.size()
				- 1;
	}

	public boolean isNextPageAvailable() {
		return this.down.isVisible();
	}

	public void nextPage() {
		this.callback.nextPage();
	}

	public boolean isFirstElementFocused() {
		return this.focusedPosition == 0;
	}

	public boolean isPreviousPageAvailable() {
		return this.up.isVisible();
	}

	public void prevPage() {
		this.callback.prevPage();
	}

	public void toggleSelectionOfCurrentFocus() {
		OptionElement focusedOption = this.options.get(this.focusedPosition);
		toggleSelection(focusedOption);
	}

	private void toggleSelection(OptionElement option) {
		if (option instanceof SelectAllOptionElement) {
			this.callback.selectAll();
			return;
		}
		if (option instanceof DeselectAllOptionElement) {
			this.callback.deselectAll();
			return;
		}

		if (this.currentSelections.contains(option)) {
			this.currentSelections.remove(option);
		} else {
			this.currentSelections.add(option);
		}

		if (!this.updatingSelection && this.callback != null) {
			this.callback.itemsSelected(this.currentSelections);
		}

		for (int i = 0; i < this.options.size(); i++) {
			this.optionList.redrawRow(i);
			focusSelectionViaNativeKey(i, true);
		}

	}

}
