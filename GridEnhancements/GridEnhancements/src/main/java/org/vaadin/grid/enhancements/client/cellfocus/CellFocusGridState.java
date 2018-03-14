package org.vaadin.grid.enhancements.client.cellfocus;

import com.vaadin.shared.communication.SharedState;

@SuppressWarnings("serial")
public class CellFocusGridState extends SharedState {

    public boolean hasCellFocusListener = false;

    public boolean hasFocusListener = false;

    public boolean hasRowFocusListener = true;

}
