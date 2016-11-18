package org.vaadin.grid.enhancements.client.cellrenderers.combobox.singleselect;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.vaadin.client.ui.VOverlay;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.PopupCallback;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option.OptionElement;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxPopup extends VOverlay
		implements FocusHandler, MouseMoveHandler, CellPreviewEvent.Handler<OptionElement> {

	private static final String SELECTED_ROW_CLASS = "gwt-MenuItem-selected";

	private CellList<OptionElement> optionsList;
	private List<OptionElement> options;

	private Button up, down;
	private Set<HandlerRegistration> handlers = new HashSet<HandlerRegistration>();

	private PopupCallback<OptionElement> callback;

	private long lastAutoClosed;

	private int focusedPosition = -1;

	public ComboBoxPopup() {
		super(true, false, true);

		setStyleName("v-filterselect-suggestpopup");

		this.optionsList = new CellList<OptionElement>(new OptionCell());
		this.optionsList.setStyleName("v-filterselect-suggestmenu");
		this.optionsList.setWidth("100%");
		this.optionsList.setKeyboardPagingPolicy(HasKeyboardPagingPolicy.KeyboardPagingPolicy.INCREASE_RANGE);
		this.optionsList.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

		initHandlers();

		add(layout());
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
				ComboBoxPopup.this.callback.prevPage();
			}
		});

		this.down = new Button("");
		this.down	.getElement()
					.removeAttribute("type");
		this.down.setStyleName("c-combo-popup-nextpage");
		this.down.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ComboBoxPopup.this.callback.nextPage();
			}
		});

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
		this.handlers.add(this.optionsList.addCellPreviewHandler(this));
	}

	public void setOptions(List<OptionElement> options) {
		this.options = options;

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
		focusSelectionViaPosition(0, focus);
	}

	public void focusSelectionLast(boolean focus) {
		focusSelectionViaPosition(this.optionsList	.getVisibleItems()
													.size()
				- 1, focus);
	}

	public void focusSelectionCurrent(boolean focus) {
		focusSelectionViaPosition(this.focusedPosition, focus);
	}

	public void focusSelectionSelected(OptionElement selected, boolean focus) {
		for (int i = 0; i < this.options.size(); i++) {
			OptionElement option = this.options.get(i);
			if (option.equals(selected)) {
				focusSelectionViaPosition(i, true);
				return;
			}
		}
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
		public void render(Context context, final OptionElement value, SafeHtmlBuilder sb) {
			sb.appendHtmlConstant("<span>");
			sb.appendEscaped(value.getName());
			sb.appendHtmlConstant("</span>");
		}
	}

	// --- Event handler implementations ---

	@Override
	public void onFocus(FocusEvent event) {
		if (ComboBoxPopup.this.callback != null) {
			ComboBoxPopup.this.callback.focus();
		}
	}

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
				break;
			}
		}
	}

	@Override
	public void onCellPreview(CellPreviewEvent<OptionElement> event) {

		if (BrowserEvents.CLICK.equals(event.getNativeEvent()
											.getType())) {
			select(event.getValue());
			event.setCanceled(true);
			event	.getNativeEvent()
					.preventDefault();
			event	.getNativeEvent()
					.stopPropagation();
		}
	}

	public void selectCurrent() {
		select(this.options.get(this.focusedPosition));
	}

	private void select(OptionElement option) {
		this.callback.itemSelected(option);
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

}
