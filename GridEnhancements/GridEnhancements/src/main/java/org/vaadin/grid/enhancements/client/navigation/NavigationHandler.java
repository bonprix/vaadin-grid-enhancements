package org.vaadin.grid.enhancements.client.navigation;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.vaadin.client.VConsole;
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
        VConsole.log("Key down! " + focusedElement.getNodeName());
        if (!focusedElement.getNodeName().equals("INPUT")) {
            return;
        }
        VConsole.log("Handle key! " + keyDownEvent.getNativeEvent().getType() + " : " + keyDownEvent.getNativeEvent().getType().equals(BrowserEvents.KEYDOWN) +
                " :: " + keyDownEvent.getNativeEvent().getKeyCode());

        final CellReference cellReference;
        switch (keyDownEvent.getNativeKeyCode()) {
            case KeyCodes.KEY_ENTER:
                cellReference = grid.getCellReference(focusedElement.getParentElement());
                int rows = grid.getDataSource().size();
                int columns = grid.getVisibleColumns().size();
                if (cellReference.getColumnIndex() + 1 < columns) {
                    NavigationUtil.focusCell(grid, cellReference.getRowIndex(), cellReference.getColumnIndex() + 1);
                    focusInputField();
                } else if (cellReference.getRowIndex() + 1 < rows) {
                    NavigationUtil.focusCell(grid, cellReference.getRowIndex() + 1, 0);
                    focusInputField();
                }
                break;
            case KeyCodes.KEY_ESCAPE:
                keyDownEvent.preventDefault();
                keyDownEvent.stopPropagation();

                cellReference = grid.getCellReference(focusedElement.getParentElement());
                NavigationUtil.focusCell(grid, cellReference.getRowIndex(), cellReference.getColumnIndex());

                break;
            case KeyCodes.KEY_UP:
                keyDownEvent.preventDefault();
                keyDownEvent.stopPropagation();

                cellReference = grid.getCellReference(focusedElement.getParentElement());
                if (cellReference.getRowIndex() > 0) {
                    NavigationUtil.focusCell(grid, cellReference.getRowIndex() - 1, cellReference.getColumnIndex());
                    focusInputField();
                }
                break;
            case KeyCodes.KEY_DOWN:
                keyDownEvent.preventDefault();
                keyDownEvent.stopPropagation();

                cellReference = grid.getCellReference(focusedElement.getParentElement());
                if (cellReference.getRowIndex() + 1 < grid.getDataSource().size()) {
                    NavigationUtil.focusCell(grid, cellReference.getRowIndex() + 1, cellReference.getColumnIndex());
                    focusInputField();
                }
                break;
        }
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