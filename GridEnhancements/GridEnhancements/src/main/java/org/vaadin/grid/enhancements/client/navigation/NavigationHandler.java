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
        if (!(focusedElement.getNodeName().equals("INPUT") || focusedElement.getNodeName().equals("BUTTON"))) {
            return;
        }

        final CellReference cellReference;
        switch (keyDownEvent.getNativeKeyCode()) {
            case KeyCodes.KEY_ENTER:
                focusedElement.blur();
                cellReference = grid.getCellReference(focusedElement.getParentElement());
                int rows = grid.getDataSource().size();
                if (cellReference.getRowIndex() + 1 < rows) {
                    NavigationUtil.focusCell(grid, cellReference.getRowIndex() + 1, cellReference.getColumnIndex());
                    focusInputField();
                }
                break;
            case KeyCodes.KEY_ESCAPE:
                keyDownEvent.preventDefault();
                keyDownEvent.stopPropagation();

                // blur input element se we get a value change
                focusedElement.blur();
                cellReference = grid.getCellReference(focusedElement.getParentElement());
                NavigationUtil.focusCell(grid, cellReference.getRowIndex(), cellReference.getColumnIndex());

                break;
            case KeyCodes.KEY_UP:
                keyDownEvent.preventDefault();
                keyDownEvent.stopPropagation();

                // blur input element se we get a value change
                focusedElement.blur();
                cellReference = grid.getCellReference(focusedElement.getParentElement());
                if (cellReference.getRowIndex() > 0) {
                    // move up one row
                    NavigationUtil.focusCell(grid, cellReference.getRowIndex() - 1, cellReference.getColumnIndex());
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
                cellReference = grid.getCellReference(focusedElement.getParentElement());
                if (cellReference.getRowIndex() + 1 < grid.getDataSource().size()) {
                    // move down one row
                    NavigationUtil.focusCell(grid, cellReference.getRowIndex() + 1, cellReference.getColumnIndex());
                    focusInputField();
                } else {
                    // refocus element in case we can't move down
                    focusedElement.focus();
                }
                break;
            case KeyCodes.KEY_TAB:
                cellReference = grid.getCellReference(getCellElement(focusedElement));

                // If first or last cell in grid stop default and keep focus
                if ((keyDownEvent.isShiftKeyDown() && NavigationUtil.isFirstCell(cellReference)) || (!keyDownEvent.isShiftKeyDown() && NavigationUtil.isLastCell(cellReference, grid))) {
                    keyDownEvent.preventDefault();
                    keyDownEvent.stopPropagation();
                } else if (keyDownEvent.isShiftKeyDown() && NavigationUtil.isfirstColumn(cellReference)) {
                    // If we have a sibling button to focus then focus normally.
                    if (focusedElement.getNodeName().equals("BUTTON") && focusedElement.getPreviousSiblingElement() != null) {
                        break;
                    }
                    // Prevent default and move one row up and to the last column
                    keyDownEvent.preventDefault();
                    keyDownEvent.stopPropagation();
                    // Step up one row and to the last column
                    NavigationUtil.focusCell(grid, cellReference.getRowIndex() - 1, grid.getColumnCount() - 1);
                    focusInputField();
                } else if (!keyDownEvent.isShiftKeyDown() && NavigationUtil.isLastColumn(cellReference, grid)) {
                    // If we have a sibling button to focus then focus normally.
                    if (focusedElement.getNodeName().equals("BUTTON") && focusedElement.getNextSiblingElement() != null) {
                        break;
                    }
                    // Prevent default and move one row down and to the first column
                    keyDownEvent.preventDefault();
                    keyDownEvent.stopPropagation();
                    // Step down one row and to the first column
                    NavigationUtil.focusCell(grid, cellReference.getRowIndex() + 1, 0);
                    focusInputField();
                }
                break;
        }
    }

    /**
     * Get the cell element even if we have to recurse one or two steps.
     * @param focusedElement Element for which we want the parent cell element
     * @return Grid cell element if found else last parent element
     */
    private Element getCellElement(Element focusedElement) {
        if (!focusedElement.hasParentElement()) {
            return focusedElement;
        }
        if (focusedElement.getParentElement().getNodeName().equals("TD")) {
            return focusedElement.getParentElement();
        }

        return getCellElement(focusedElement.getParentElement());
    }

    private void focusInputField() {
        // We need to delay cell focus for 2 animation frames so that the escalator has time to populate the new cell.
        AnimationScheduler.get().requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
            @Override
            public void execute(double timestamp) {
                AnimationScheduler.get().requestAnimationFrame(new AnimationScheduler.AnimationCallback() {
                    @Override
                    public void execute(double timestamp) {
                        NavigationUtil.focusInputField(NavigationUtil.getFocusedCell(grid).getElement());
                    }
                });
            }
        });
    }


}