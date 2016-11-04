package org.vaadin.grid.enhancements.client.cellrenderers.multiselect;

import java.util.HashSet;
import java.util.Set;

import com.vaadin.shared.communication.SharedState;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class MultiSelectState<BEANTYPE> extends SharedState {

	public Set<BEANTYPE> value = new HashSet<BEANTYPE>();

}
