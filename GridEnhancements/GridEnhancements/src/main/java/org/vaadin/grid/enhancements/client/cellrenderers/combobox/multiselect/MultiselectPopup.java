package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionElement;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.PopupCallback;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.shared.impl.StringCase;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.vaadin.client.VConsole;
import com.vaadin.client.ui.VOverlay;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
@SuppressWarnings("deprecation")
public class MultiselectPopup extends VOverlay
		implements MouseMoveHandler, SelectionChangeEvent.Handler, CellPreviewEvent.Handler<OptionElement> {

	private static final String SELECTED_ROW_CLASS = "gwt-MenuItem-selected";

	private CellList<OptionElement> optionList;
	private SelectionModel<OptionElement> selectionModel;
	private List<OptionElement> options;

	private Button up, down;
	private Set<HandlerRegistration> handlers = new HashSet<HandlerRegistration>();

	private PopupCallback<OptionElement> callback;
	// Not as items on page
	private Set<OptionElement> currentSelection = new HashSet<OptionElement>();
	private boolean updatingSelection = false;

	private long lastAutoClosed;

	int focusedPosition = -1;

	/**
	 * CellList item key provider
	 */
	private ProvidesKey<OptionElement> keyProvider;

	public MultiselectPopup(List<OptionElement> options) {
		super(true);

		this.options = options;

		setStyleName("v-filterselect-suggestpopup");

		initGwtParts();

		initHandlers();

		add(layout());
	}

	private void initGwtParts() {
		this.keyProvider = new ProvidesKey<OptionElement>() {
			public Object getKey(OptionElement item) {
				// Always do a null check.
				return (item == null) ? null : item.hashCode();
			}
		};

		this.selectionModel = new MultiSelectionModel<OptionElement>(this.keyProvider);
		final CellPreviewEvent.Handler<OptionElement> selectionEventManager = DefaultSelectionEventManager.createCheckboxManager();

		CellList.Resources resources = GWT.create(CellList.Resources.class);

		this.optionList = new CellList<OptionElement>(new OptionCell(), resources, this.keyProvider);
		this.optionList.setStyleName("v-filterselect-suggestmenu");
		this.optionList.setPageSize(this.options.size());
		this.optionList.setRowCount(this.options.size(), true);
		this.optionList.setRowData(0, this.options);
		this.optionList.setVisibleRange(0, this.options.size());
		this.optionList.setWidth("100%");
		this.optionList.setKeyboardPagingPolicy(HasKeyboardPagingPolicy.KeyboardPagingPolicy.INCREASE_RANGE);
		this.optionList.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

		this.optionList.setSelectionModel(this.selectionModel, selectionEventManager);
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

		// Add selection change handler
		this.handlers.add(this.selectionModel.addSelectionChangeHandler(this));
	}

	/**
	 * Get the set of selected objects when in multiselection mode
	 *
	 * @return Set of selected objects
	 */
	public Set<OptionElement> getSelectedOptions() {
		return ((MultiSelectionModel<OptionElement>) this.selectionModel).getSelectedSet();
	}

	/**
	 * Hide popup
	 */
	private void closePopup() {
		MultiselectPopup.this.hide();
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
		for (int i = 0; i < this.options.size(); i++) {
			if (!this.selectionModel.isSelected(this.options.get(i))) {
				focusSelectionViaPosition(i, focus);
				return;
			}
		}

		focusSelectionViaPosition(0, focus);
	}

	public void focusSelectionLast(boolean focus) {
		focusSelectionViaPosition(this.optionList	.getVisibleItems()
													.size()
				- 1, focus);
	}

	public void focusSelectionCurrent(boolean focus) {
		focusSelectionViaPosition(this.focusedPosition - 1, focus);
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
	 * @param currentSelection
	 */
	public void setCurrentSelection(Set<OptionElement> currentSelection) {
		// Lock selection event so we don't send change events
		this.updatingSelection = true;
		this.currentSelection.clear();

		for (OptionElement value : currentSelection) {
			// If current view doesn't contain item then add item to current
			// selections that are selected but not visible!
			//
			// Note! currentSelection is always added to selection event
			// selected items.
			if (!this.options.contains(value)) {
				this.currentSelection.add(value);
			} else {
				this.selectionModel.setSelected(value, true);
			}
		}
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
			final CheckBox checkBox = new CheckBox();
			checkBox.setValue(((MultiSelectionModel<OptionElement>) MultiselectPopup.this.selectionModel).isSelected(option));
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
				// focusSelection(, stealFocus);
				focusSelectionViaPosition(i, true);
				break;
			}
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
			final OptionElement value = event.getValue();
			final Boolean state = !event.getDisplay()
										.getSelectionModel()
										.isSelected(value);
			event	.getDisplay()
					.getSelectionModel()
					.setSelected(value, state);
			event.setCanceled(true);
		}
	}

	@Override
	public void onSelectionChange(SelectionChangeEvent event) {
		if (!this.updatingSelection && this.callback != null) {
			Set<OptionElement> selection = new HashSet<OptionElement>();
			selection.addAll(this.currentSelection);
			selection.addAll(getSelectedOptions());
			this.callback.itemsSelected(selection);
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

	public void selectCurrentFocus() {
		OptionElement focusedOption = this.options.get(this.focusedPosition);
		final Boolean state = !this.selectionModel.isSelected(focusedOption);
		this.selectionModel.setSelected(focusedOption, state);
	}

}
