package org.vaadin.grid.enhancements.events;

import java.util.ArrayList;
import java.util.List;

public class EventListenerList<LISTENER extends Listener<EVENT>, EVENT> {
    private final List<LISTENER> listeners;

    public EventListenerList() {
        this.listeners = new ArrayList<LISTENER>();
    }

    public void addListener(final LISTENER l) {
        this.listeners.add(l);
    }

    public void removeListener(final LISTENER l) {
        this.listeners.remove(l);
    }

    public void clearListeners() {
        this.listeners.clear();
    }

    public void dispatch(final EVENT event) {
        for(final LISTENER l : this.listeners) {
            l.onEvent(event);
        }
    }
}