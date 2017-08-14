package org.vaadin.grid.enhancements.client.cellrenderers.combobox.common.option;

public class OptionElement {

    private Long id;
    private String name;

    public OptionElement() {
    }

    public OptionElement(final Long id) {
        this(id, null);
    }

    public OptionElement(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof OptionElement)) {
            return false;
        }

        final OptionElement otherDummyClass = (OptionElement) obj;
        if (otherDummyClass.getId() == null) {
            return false;
        }

        return otherDummyClass.getId()
            .equals(this.getId());
    }

    @Override
    public int hashCode() {
        if (getId() == null) {
            return 0;
        }
        return (int) (getId() ^ (getId() >>> 32));
    }
}
