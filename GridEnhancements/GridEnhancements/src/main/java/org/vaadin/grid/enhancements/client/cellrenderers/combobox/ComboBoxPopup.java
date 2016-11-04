package org.vaadin.grid.enhancements.client.cellrenderers.combobox;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.vaadin.client.ui.VOverlay;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxPopup extends VOverlay implements MouseMoveHandler, KeyDownHandler, SelectionChangeEvent.Handler {

	private final CellList<ComboBoxElement> list;
	private final SelectionModel<ComboBoxElement> selectionModel;
	private List<ComboBoxElement> values;

	private Button up, down;
	private Set<HandlerRegistration> handlers = new HashSet<HandlerRegistration>();

	// TODO
	private PopupCallback callback;

	private long lastAutoClosed;

	public ComboBoxPopup(List<ComboBoxElement> values) {
		super(true);

		this.values = values;
		addCloseHandler(this);

		CellList.Resources resources = GWT.create(CellListResources.class);

		this.list = new CellList<ComboBoxElement>(new Cell(), resources, this.keyProvider);
		this.list.setPageSize(values.size());
		this.list.setRowCount(values.size(), true);
		this.list.setRowData(0, values);
		this.list.setVisibleRange(0, values.size());
		this.list.setWidth("100%");
		this.list.setKeyboardPagingPolicy(HasKeyboardPagingPolicy.KeyboardPagingPolicy.INCREASE_RANGE);
		this.list.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

		this.list.setStyleName("c-combobox-options");

		this.selectionModel = new SingleSelectionModel<ComboBoxElement>(this.keyProvider);
		this.list.setSelectionModel(this.selectionModel);

		// Remove all handlers if any exist for any reason
		if (!this.handlers.isEmpty()) {
			for (HandlerRegistration handler : this.handlers) {
				handler.removeHandler();
			}
		}

		// Add CellList listeners
		this.handlers.add(this.list.addBitlessDomHandler(this, MouseMoveEvent.getType()));
		this.handlers.add(this.list.addHandler(this, KeyDownEvent.getType()));

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
	private ComboBoxElement getSelectedObject() {
		return ((SingleSelectionModel<ComboBoxElement>) this.selectionModel).getSelectedObject();
	}

	/**
	 * Hide popup
	 */
	private void closePopup() {
		ComboBoxPopup.this.hide();
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
	public void focusSelection(ComboBoxElement selected, boolean stealFocus) {
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
	 * Add a popup callback
	 *
	 * @param callback
	 *            ComboBox callback
	 */
	public void addPopupCallback(PopupCallback<ComboBoxElement> callback) {
		this.callback = callback;
	}

	/**
	 * Remove popup callback
	 *
	 * @param callback
	 */
	public void removePopupCallback(PopupCallback<ComboBoxElement> callback) {
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
	private class Cell extends AbstractCell<ComboBoxElement> {

		@Override
		public void render(Context context, final ComboBoxElement value, SafeHtmlBuilder sb) {
			sb.appendHtmlConstant("<span>");
			// TODO: add something for icons?
			sb.appendEscaped(value.getName());
			sb.appendHtmlConstant("</span>");

		}
	}

	/**
	 * CellList item key provider
	 */
	private ProvidesKey<ComboBoxElement> keyProvider = new ProvidesKey<ComboBoxElement>() {
		public Object getKey(ComboBoxElement item) {
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

	public void onSelectionChange(SelectionChangeEvent event) {
		if (this.callback != null) {
			this.callback.itemSelected(getSelectedObject());
		}
		closePopup();
	}
}
