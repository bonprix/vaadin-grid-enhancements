package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.PopupCallback;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.DeselectAllOptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.SelectAllOptionElement;

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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
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
import com.vaadin.client.ui.VOverlay;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxMultiselectPopup extends VOverlay implements FocusHandler, MouseMoveHandler, MouseOverHandler,
		MouseOutHandler, CellPreviewEvent.Handler<OptionElement> {

	private static final String SELECTED_ROW_CLASS = "gwt-MenuItem-selected";

	private String selectAllText;
	private String deselectAllText;

	private CellList<OptionElement> optionsList;
	private List<OptionElement> options;

	private Button up, down;
	private Set<HandlerRegistration> handlers = new HashSet<HandlerRegistration>();

	private PopupCallback<OptionElement> callback;
	// Not as items on page
	private HashSet<OptionElement> currentSelections = new HashSet<OptionElement>();
	private boolean updatingSelection = false;

	private long lastAutoClosed;

	private int focusedPosition = -1;

	public ComboBoxMultiselectPopup() {
		super(true);

		setStyleName("v-filterselect-suggestpopup v-filterselect-suggestpopup-column");

		initOptionsList();

		initHandlers();

		add(layout());
	}

	private void initOptionsList() {
		this.optionsList = new CellList<OptionElement>(new OptionCell());
		this.optionsList.setStyleName("v-filterselect-suggestmenu");
		this.optionsList.setWidth("100%");
		this.optionsList.setKeyboardPagingPolicy(HasKeyboardPagingPolicy.KeyboardPagingPolicy.INCREASE_RANGE);
		this.optionsList.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
	}

	private boolean withDeselectAllButton() {
		return this.deselectAllText != null;
	}

	private boolean withSelectAllButton() {
		return this.selectAllText != null;
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
				ComboBoxMultiselectPopup.this.callback.prevPage();
			}
		});
		this.up.addMouseOverHandler(this);
		this.up.addMouseOutHandler(this);

		this.down = new Button("");
		this.down	.getElement()
					.removeAttribute("type");
		this.down.setStyleName("c-combo-popup-nextpage");
		this.down.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ComboBoxMultiselectPopup.this.callback.nextPage();
			}
		});
		this.down.addMouseOverHandler(this);
		this.down.addMouseOutHandler(this);

		// Add widgets to content panel
		layout.add(this.up);
		layout.add(this.optionsList);
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
		this.handlers.add(this.optionsList.addBitlessDomHandler(this, MouseMoveEvent.getType()));
		this.handlers.add(this.optionsList.addBitlessDomHandler(this, MouseOverEvent.getType()));
		this.handlers.add(this.optionsList.addBitlessDomHandler(this, MouseOutEvent.getType()));
		this.handlers.add(this.optionsList.addCellPreviewHandler(this));
		this.handlers.add(this.optionsList.addHandler(this, FocusEvent.getType()));
	}

	public void setOptions(List<OptionElement> options, String selectAllText, String deselectAllText) {
		this.options = options;
		this.selectAllText = selectAllText;
		this.deselectAllText = deselectAllText;

		if (withDeselectAllButton()) {
			DeselectAllOptionElement deselectAllOption = new DeselectAllOptionElement(this.deselectAllText);
			if (!this.options.contains(deselectAllOption)) {
				this.options.add(0, deselectAllOption);
			}
		}

		if (withSelectAllButton()) {
			SelectAllOptionElement selectAllOption = new SelectAllOptionElement(this.selectAllText);
			if (!this.options.contains(selectAllOption)) {
				this.options.add(0, selectAllOption);
			}
		}

		this.optionsList.setPageSize(this.options.size());
		this.optionsList.setRowCount(this.options.size(), true);
		this.optionsList.setRowData(0, this.options);
		this.optionsList.setVisibleRange(0, this.options.size());
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
		focusSelectionViaPosition(this.optionsList	.getVisibleItems()
													.size()
				- 1, focus);
	}

	public void focusSelectionCurrent(boolean focus) {
		focusSelectionViaPosition(this.focusedPosition, focus);
	}

	public void focusSelectionViaPosition(int newPosition, boolean stealFocus) {
		if (stealFocus && this.focusedPosition > -1 && this.focusedPosition < this.optionsList	.getVisibleItems()
																								.size()) {
			this.optionsList.getRowElement(this.focusedPosition)
							.removeClassName(SELECTED_ROW_CLASS);
		}

		this.focusedPosition = newPosition;

		this.optionsList.getRowElement(this.focusedPosition)
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
			checkBox.setValue(ComboBoxMultiselectPopup.this.currentSelections.contains(option));
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
		for (int i = 0; i < this.optionsList.getVisibleItems()
											.size(); i++) {
			Element e = this.optionsList.getRowElement(i);
			if (e.equals(target)) {
				focusSelectionViaPosition(i, true);
				return;
			}
		}
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		this.callback.setSkipBlur(false);
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		this.callback.setSkipBlur(true);
	}

	@Override
	public void onFocus(FocusEvent event) {
		if (ComboBoxMultiselectPopup.this.callback != null) {
			ComboBoxMultiselectPopup.this.callback.focus();
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
		for (int i = 0; i < this.optionsList.getRowCount(); i++) {
			this.optionsList.getRowElement(i)
							.addClassName("gwt-MenuItem");
		}
	}

	public boolean isLastElementFocused() {
		return this.focusedPosition == this.optionsList	.getVisibleItems()
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
		updateElementCss();
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
			this.optionsList.redrawRow(i);
			focusSelectionViaNativeKey(i, true);
		}

	}

}
