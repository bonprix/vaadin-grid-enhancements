package org.vaadin.grid.enhancements.client.navigation;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.widget.grid.CellReference;
import com.vaadin.client.widgets.Grid;

public class NavigationHandler implements KeyDownHandler {

	Grid grid;

	public NavigationHandler(Grid grid) {
		this.grid = grid;
	}

	@Override
	public void onKeyDown(KeyDownEvent keyDownEvent) {

		Element focusedElement = WidgetUtil.getFocusedElement();
		if (!(focusedElement.getNodeName()
							.equals("INPUT")
				|| focusedElement	.getNodeName()
									.equals("BUTTON"))) {
			return;
		}

		final CellReference cellReference;
		switch (keyDownEvent.getNativeKeyCode()) {
		case KeyCodes.KEY_ENTER:
			focusedElement.blur();
			cellReference = this.grid.getCellReference(focusedElement.getParentElement());
			int rows = this.grid.getDataSource()
								.size();
			if (cellReference.getRowIndex() + 1 < rows) {
				NavigationUtil.focusCell(this.grid, cellReference.getRowIndex() + 1, cellReference.getColumnIndex());
				focusInputField();
			}
			break;
		case KeyCodes.KEY_ESCAPE:
			keyDownEvent.preventDefault();
			keyDownEvent.stopPropagation();

			// blur input element se we get a value change
			focusedElement.blur();
			cellReference = this.grid.getCellReference(focusedElement.getParentElement());
			NavigationUtil.focusCell(this.grid, cellReference.getRowIndex(), cellReference.getColumnIndex());

			break;
		case KeyCodes.KEY_UP:
			keyDownEvent.preventDefault();
			keyDownEvent.stopPropagation();

			// blur input element se we get a value change
			focusedElement.blur();
			cellReference = this.grid.getCellReference(focusedElement.getParentElement());
			if (cellReference.getRowIndex() > 0) {
				// move up one row
				NavigationUtil.focusCell(this.grid, cellReference.getRowIndex() - 1, cellReference.getColumnIndex());
				focusInputField();
			} else {
				// refocus element in case we can't move up
				focusedElement.focus();
			}
			break;
		case KeyCodes.KEY_DOWN:
			keyDownEvent.preventDefault();
			keyDownEvent.stopPropagation();

			// blur input element se we get a value change
			focusedElement.blur();
			cellReference = this.grid.getCellReference(focusedElement.getParentElement());
			if (cellReference.getRowIndex() + 1 < this.grid	.getDataSource()
															.size()) {
				// move down one row
				NavigationUtil.focusCell(this.grid, cellReference.getRowIndex() + 1, cellReference.getColumnIndex());
				focusInputField();
			} else {
				// refocus element in case we can't move down
				focusedElement.focus();
			}
			break;
		case KeyCodes.KEY_TAB:
			cellReference = this.grid.getCellReference(getCellElement(focusedElement));
			final Element tdElement = cellReference.getElement();

			// If first or last cell in grid stop default and keep focus
			if ((keyDownEvent.isShiftKeyDown() && NavigationUtil.isFirstCell(cellReference, tdElement))
					|| (!keyDownEvent.isShiftKeyDown()
							&& NavigationUtil.isLastCell(cellReference, this.grid, tdElement))) {
				keyDownEvent.preventDefault();
				keyDownEvent.stopPropagation();
			} else if (keyDownEvent.isShiftKeyDown()) {
				// If we have a previous sibling to focus then focus normally.
				if (NavigationUtil.hasPreviousInputElement(tdElement)) {
					break;
				}

				focusedElement.blur();
				Element firstEdtiableElement = NavigationUtil.getLastEditableElement(tdElement);
				CellReference editableCellReference = this.grid.getCellReference(firstEdtiableElement);

				// Prevent default and move one row up and to the last column
				keyDownEvent.preventDefault();
				keyDownEvent.stopPropagation();
				// Step up one row and to the last column
				NavigationUtil.focusCell(	this.grid, cellReference.getRowIndex() - 1,
											editableCellReference.getColumnIndex());
				focusInputField();
			} else if (!keyDownEvent.isShiftKeyDown()) {
				// If we have a previous sibling to focus then focus normally.
				if (NavigationUtil.hasNextInputElement(tdElement)) {
					break;
				}

				focusedElement.blur();
				Element firstEdtiableElement = NavigationUtil.getFirstEditableElement(tdElement);
				CellReference editableCellReference = this.grid.getCellReference(firstEdtiableElement);

				// Prevent default and move one row down and to the first column
				keyDownEvent.preventDefault();
				keyDownEvent.stopPropagation();
				// Step down one row and to the first column
				NavigationUtil.focusCell(	this.grid, cellReference.getRowIndex() + 1,
											editableCellReference.getColumnIndex());
				focusInputField();
			}
			break;
		}
	}

	/**
	 * Get the cell element even if we have to recurse one or two steps.
	 * 
	 * @param focusedElement
	 *            Element for which we want the parent cell element
	 * @return Grid cell element if found else last parent element
	 */
	private Element getCellElement(Element focusedElement) {
		if (!focusedElement.hasParentElement()) {
			return focusedElement;
		}
		if (focusedElement	.getParentElement()
							.getNodeName()
							.equals("TD")) {
			return focusedElement.getParentElement();
		}

		return getCellElement(focusedElement.getParentElement());
	}

	private void focusInputField() {
		// We need to delay cell focus for 2 animation frames so that the
		// escalator has time to populate the new cell.
		AnimationScheduler	.get()
							.requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
								@Override
								public void execute(double timestamp) {
									AnimationScheduler	.get()
														.requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
															@Override
															public void execute(double timestamp) {
																NavigationUtil.focusInputField(NavigationUtil	.getFocusedCell(NavigationHandler.this.grid)
																												.getElement());
															}
														});
								}
							});
	}

}