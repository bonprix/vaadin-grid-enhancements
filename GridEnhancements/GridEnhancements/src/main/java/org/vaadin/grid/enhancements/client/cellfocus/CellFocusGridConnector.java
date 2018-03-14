package org.vaadin.grid.enhancements.client.cellfocus;

import org.vaadin.grid.enhancements.CellFocusGrid;
import org.vaadin.grid.enhancements.client.cellfocus.FocusTracker.FocusListener;

import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.widgets.Grid;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(CellFocusGrid.class)
public class CellFocusGridConnector extends AbstractExtensionConnector {

    private Grid<Object> grid;
    private FocusTracker focusTracker;
    private CellFocusGridServerRPC rpc;

    @Override
    @SuppressWarnings("unchecked")
    protected void extend(final ServerConnector target) {
        this.grid = (Grid<Object>) ((ComponentConnector) target).getWidget();
        this.rpc = getRpcProxy(CellFocusGridServerRPC.class);
        this.focusTracker = new FocusTracker(this.grid);


        this.focusTracker.addListener(new FocusListener() {
            @Override
            public void focusMoved(final int currentRow, final int currentCol, final int lastRow,
                    final int lastCol) {
                if(getState().hasFocusListener) {
                    CellFocusGridConnector.this.rpc.focusUpdated(currentRow, currentCol);
                }
            }
        });

        updateFocusTracking();


    }

    private int abs(final int number) {
        return (number < 0) ? -number : number;
    }

    @OnStateChange({"hasFocusListener", "hasCellFocusListener"})
    void updateFocusTracking() {
        final CellFocusGridState state = getState();
        if (state.hasFocusListener || state.hasCellFocusListener) {
            this.focusTracker.start();

        } else {
            this.focusTracker.stop();
        }
    }

    @Override
    public CellFocusGridState getState() {
        return ((CellFocusGridState) super.getState());
    }
}