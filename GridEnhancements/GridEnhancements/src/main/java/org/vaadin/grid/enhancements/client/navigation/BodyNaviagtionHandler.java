package org.vaadin.grid.enhancements.client.navigation;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.widget.grid.CellReference;
import com.vaadin.client.widget.grid.events.BodyKeyDownHandler;
import com.vaadin.client.widget.grid.events.GridKeyDownEvent;

public class BodyNaviagtionHandler implements BodyKeyDownHandler {

	@Override
	public void onKeyDown(GridKeyDownEvent event) {
		switch (event.getNativeKeyCode()) {
		case KeyCodes.KEY_ENTER:
			if (isCellContainingComponent(event.getFocusedCell())) {
				// Don't propagate enter to component
				event.preventDefault();
				event.stopPropagation();

				final Element componentElement = extractComponentElement(event.getFocusedCell());

				// Run focus as deferred command so the Navigation handler
				// doesn't catch the event.
				Scheduler	.get()
							.scheduleDeferred(new Command() {

								@Override
								public void execute() {
									WidgetUtil.focus(componentElement);
									NavigationUtil.focusInputField(componentElement);
								}
							});

			}
			break;
		}
	}

	private Element extractComponentElement(CellReference cell) {
		// Only check recursively if we are looking at a table
		if (cell.getElement()
				.getNodeName()
				.equals("TD")) {
			return NavigationUtil.getInputElement(cell	.getElement()
														.getChildNodes());
		}
		return null;
	}

	private boolean isCellContainingComponent(CellReference cell) {
		// Only check recursively if we are looking at a table
		if (cell.getElement()
				.getNodeName()
				.equals("TD")) {
			return containsInput(cell	.getElement()
										.getChildNodes());
		}
		return false;
	}

	private boolean containsInput(NodeList<Node> nodes) {
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.getItem(i);
			if (node.getNodeName()
					.equals("INPUT")
					|| node	.getNodeName()
							.equals("BUTTON")) {
				return true;
			} else if (node	.getChildNodes()
							.getLength() > 0) {
				if (containsInput(node.getChildNodes())) {
					return true;
				}
			}
		}

		return false;
	}
}