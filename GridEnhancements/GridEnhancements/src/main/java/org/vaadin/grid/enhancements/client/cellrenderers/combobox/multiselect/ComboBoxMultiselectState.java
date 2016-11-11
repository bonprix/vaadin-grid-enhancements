package org.vaadin.grid.enhancements.client.cellrenderers.combobox.multiselect;

import java.util.HashSet;
import java.util.Set;

import org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.OptionElement;

import com.vaadin.shared.communication.SharedState;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class ComboBoxMultiselectState extends SharedState {

	private static final long serialVersionUID = -1074682470927601262L;

	public Set<OptionElement> value = new HashSet<OptionElement>();

}
