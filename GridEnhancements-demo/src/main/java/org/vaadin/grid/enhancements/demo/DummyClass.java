package org.vaadin.grid.enhancements.demo;

public class DummyClass {
	private Long id;
	private String name;

	public DummyClass() {
	}

	public DummyClass(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DummyClass)) {
			return false;
		}

		DummyClass otherDummyClass = (DummyClass) obj;
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

	@Override
	public String toString() {
		return getName();
	}

}