package org.vaadin.grid.enhancements.client.cellrenderers.combobox;

import com.vaadin.shared.communication.SharedState;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxState extends SharedState {

    public boolean isMultiSelect = false;
    public String value = "";

}
