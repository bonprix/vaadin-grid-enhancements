package org.vaadin.grid.enhancements.client.cellfocus;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.vaadin.client.widgets.Grid;

/**
 * Actively track position of focus in Grid using RequestAnimationFrame
 */
public class FocusTracker {

    public interface FocusListener {
        public void focusMoved(int currentRow, int currentCol, int lastRow,
                int lastCol);
    }

    private final List<FocusListener> listeners;
    private final Grid<?> grid;
    private int currentRow;
    private int currentCol;
    private int lastRow;
    private int lastCol;
    private boolean run;

    public FocusTracker(final Grid<?> g) {
        this.grid = g;
        this.currentRow = GridViolators.getFocusedRow(g);
        this.currentCol = GridViolators.getFocusedCol(g);
        this.lastRow = this.currentRow;
        this.lastCol = this.currentCol;
        this.listeners = new ArrayList<FocusListener>();
        this.run = false;
    }

    public void start() {
        if(!this.run) {
            this.run = true;
            this.updateLoop.execute(0);
        }
    }

    public void stop() {
        this.run = false;
    }

    public boolean isRunning() {
        return this.run;
    }

    private void notifyFocusMoved() {
        for (final FocusListener l : this.listeners) {
            l.focusMoved(this.currentRow, this.currentCol, this.lastRow, this.lastCol);
        }
    }

    public void addListener(final FocusListener l) {
        this.listeners.add(l);
    }

    public void removeListener(final FocusListener l) {
        this.listeners.remove(l);
    }

    public void clearListeners() {
        this.listeners.clear();
    }

    private final AnimationCallback updateLoop = new AnimationCallback() {
        @Override
        public void execute(final double timestamp) {

            // TODO: do not update position or notify of moved focus if focus is in header/footer!

            final int row = GridViolators.getFocusedRow(FocusTracker.this.grid);
            final int col = GridViolators.getFocusedCol(FocusTracker.this.grid);

            if (row != FocusTracker.this.currentRow || col != FocusTracker.this.currentCol) {
                FocusTracker.this.lastRow = FocusTracker.this.currentRow;
                FocusTracker.this.currentRow = row;
                FocusTracker.this.lastCol = FocusTracker.this.currentCol;
                FocusTracker.this.currentCol = col;
                notifyFocusMoved();
            }

            if (FocusTracker.this.run) {
                AnimationScheduler.get().requestAnimationFrame(FocusTracker.this.updateLoop);
            }
        }
    };

}
