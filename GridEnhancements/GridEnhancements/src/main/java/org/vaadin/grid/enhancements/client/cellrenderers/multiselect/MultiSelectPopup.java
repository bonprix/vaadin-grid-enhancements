package org.vaadin.grid.enhancements.client.cellrenderers.multiselect;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.shared.impl.StringCase;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
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
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.vaadin.client.ui.VOverlay;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.CellListResources;
import org.vaadin.grid.enhancements.client.cellrenderers.combobox.PopupCallback;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class MultiSelectPopup extends VOverlay implements MouseMoveHandler, KeyDownHandler,
		SelectionChangeEvent.Handler, CellPreviewEvent.Handler<ComboBoxMultiselectOption> {

	private final CellList<ComboBoxMultiselectOption> list;
	private final SelectionModel<ComboBoxMultiselectOption> selectionModel;
	private List<ComboBoxMultiselectOption> values;

	private Button up, down;
	private Set<HandlerRegistration> handlers = new HashSet<HandlerRegistration>();

	private PopupCallback callback;
	// Not as items on page
	private Set<ComboBoxMultiselectOption> currentSelection = new HashSet<ComboBoxMultiselectOption>();
	private boolean updatingSelection = false;

	private long lastAutoClosed;

	public MultiSelectPopup(List<ComboBoxMultiselectOption> values) {
		super(true);

		this.values = values;
		addCloseHandler(this);

		CellList.Resources resources = GWT.create(CellListResources.class);

		this.list = new CellList<ComboBoxMultiselectOption>(new Cell(), resources, this.keyProvider);
		this.list.setPageSize(values.size());
		this.list.setRowCount(values.size(), true);
		this.list.setRowData(0, values);
		this.list.setVisibleRange(0, values.size());
		this.list.setWidth("100%");
		this.list.setKeyboardPagingPolicy(HasKeyboardPagingPolicy.KeyboardPagingPolicy.INCREASE_RANGE);
		this.list.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

		this.list.setStyleName("c-combobox-options");

		this.selectionModel = new MultiSelectionModel<ComboBoxMultiselectOption>(this.keyProvider);
		final CellPreviewEvent.Handler<ComboBoxMultiselectOption> selectionEventManager = DefaultSelectionEventManager.createCheckboxManager();
		this.list.setSelectionModel(this.selectionModel, selectionEventManager);

		// Remove all handlers if any exist for any reason
		if (!this.handlers.isEmpty()) {
			for (HandlerRegistration handler : this.handlers) {
				handler.removeHandler();
			}
		}

		// Add CellList listeners
		this.handlers.add(this.list.addBitlessDomHandler(this, MouseMoveEvent.getType()));
		this.handlers.add(this.list.addHandler(this, KeyDownEvent.getType()));
		this.handlers.add(this.list.addCellPreviewHandler(this));

		// Add selection change handler
		this.handlers.add(this.selectionModel.addSelectionChangeHandler(this));

		setStyleName("c-combo-popup");

		VerticalPanel content = new VerticalPanel();
		content.setWidth("100%");

		this.up = new Button("");
		this.up	.getElement()
				.removeAttribute("type");
		this.up.setStyleName("c-combo-popup-prevpage");
		this.up.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				MultiSelectPopup.this.callback.prevPage();
			}
		});

		this.down = new Button("");
		this.down	.getElement()
					.removeAttribute("type");
		this.down.setStyleName("c-combo-popup-nextpage");
		this.down.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				MultiSelectPopup.this.callback.nextPage();
			}
		});

		// Add widgets to content panel
		content.add(this.up);
		content.add(this.list);
		content.add(this.down);

		// Init content widget
		add(content);
	}

	/**
	 * Get the selected object when in singleSelection mode
	 *
	 * @return Single selected object
	 */
	private ComboBoxMultiselectOption getSelectedObject() {
		return ((SingleSelectionModel<ComboBoxMultiselectOption>) this.selectionModel).getSelectedObject();
	}

	/**
	 * Get the set of selected objects when in multiselection mode
	 *
	 * @return Set of selected objects
	 */
	public Set<ComboBoxMultiselectOption> getSelectedObjects() {
		return ((MultiSelectionModel<ComboBoxMultiselectOption>) this.selectionModel).getSelectedSet();
	}

	/**
	 * Hide popup
	 */
	private void closePopup() {
		MultiSelectPopup.this.hide();
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
	 * @param selected
	 *            Selected item to focus if available
	 */
	public void focusSelection(ComboBoxMultiselectOption selected, boolean stealFocus) {
		if (this.values.contains(selected)) {
			// Focus selected item
			this.list.setKeyboardSelectedRow(this.values.indexOf(selected), stealFocus);
		} else if (!this.values.isEmpty()) {
			// Else focus first item if values exist
			this.list.setKeyboardSelectedRow(0, stealFocus);
		} else {
			// Else move focus to list
			this.list.setFocus(stealFocus);
		}
	}

	/**
	 * Set the current selection set for multiselection mode
	 *
	 * @param currentSelection
	 */
	public void setCurrentSelection(Set<ComboBoxMultiselectOption> currentSelection) {
		// Lock selection event so we don't send change events
		this.updatingSelection = true;
		this.currentSelection.clear();

		for (ComboBoxMultiselectOption value : currentSelection) {
			// If current view doesn't contain item then add item to current
			// selections that are selected but not visible!
			//
			// Note! currentSelection is always added to selection event
			// selected items.
			if (!this.values.contains(value)) {
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
	public void addPopupCallback(PopupCallback<ComboBoxMultiselectOption> callback) {
		this.callback = callback;
	}

	/**
	 * Remove popup callback
	 *
	 * @param callback
	 */
	public void removePopupCallback(PopupCallback<ComboBoxMultiselectOption> callback) {
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
	private class Cell extends AbstractCell<ComboBoxMultiselectOption> {

		@Override
		public void render(Context context, final ComboBoxMultiselectOption value, SafeHtmlBuilder sb) {
			final CheckBox checkBox = new CheckBox();
			checkBox.setValue(((MultiSelectionModel) MultiSelectPopup.this.selectionModel).isSelected(value));
			sb.appendHtmlConstant(checkBox	.getElement()
											.getString());
			sb.appendHtmlConstant("<span>"); // TODO: add something for icons?
			sb.appendEscaped(value.getName());
			sb.appendHtmlConstant("</span>");

		}
	}

	/**
	 * CellList item key provider
	 */
	private ProvidesKey<ComboBoxMultiselectOption> keyProvider = new ProvidesKey<ComboBoxMultiselectOption>() {
		public Object getKey(ComboBoxMultiselectOption item) {
			// Always do a null check.
			return (item == null) ? null : item.hashCode();
		}
	};

	// --- Event handler implementations ---

	@Override
	public void onKeyDown(KeyDownEvent event) {
		switch (event	.getNativeEvent()
						.getKeyCode()) {
		case KeyCodes.KEY_ESCAPE:
			this.callback.clear();
			closePopup();
			break;
		case KeyCodes.KEY_DOWN:
			if (this.list.getKeyboardSelectedRow() == this.list	.getVisibleItems()
																.size()
					- 1 && this.down.isEnabled()) {
				this.callback.nextPage();
			}
			break;
		case KeyCodes.KEY_UP:
			if (this.list.getKeyboardSelectedRow() == 0 && this.up.isEnabled()) {
				this.callback.prevPage();
			}
			break;
		case KeyCodes.KEY_TAB:
			if (this.callback != null) {
				this.callback.itemSelected(this.list.getVisibleItem(this.list.getKeyboardSelectedRow()));
			}
			closePopup();
			break;
		}
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		Element target = event	.getNativeEvent()
								.getEventTarget()
								.cast();
		for (int i = 0; i < this.list	.getVisibleItems()
										.size(); i++) {
			Element e = this.list.getRowElement(i);
			if (e.equals(target)) {
				this.list.setKeyboardSelectedRow(i, true);
				break;
			}
		}
	}

	@Override
	public void onCellPreview(CellPreviewEvent<ComboBoxMultiselectOption> event) {

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
			final ComboBoxMultiselectOption value = event.getValue();
			final Boolean state = !event.getDisplay()
										.getSelectionModel()
										.isSelected(value);
			event	.getDisplay()
					.getSelectionModel()
					.setSelected(value, state);
			event.setCanceled(true);
		}
	}

	public void onSelectionChange(SelectionChangeEvent event) {
		if (!this.updatingSelection && this.callback != null) {
			Set<ComboBoxMultiselectOption> selection = new HashSet<ComboBoxMultiselectOption>();
			selection.addAll(this.currentSelection);
			selection.addAll(getSelectedObjects());
			this.callback.itemsSelected(selection);
		}
	}
}
