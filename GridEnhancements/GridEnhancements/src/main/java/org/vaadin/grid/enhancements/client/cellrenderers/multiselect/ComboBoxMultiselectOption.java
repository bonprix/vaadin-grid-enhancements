package org.vaadin.grid.enhancements.client.cellrenderers.multiselect;

public class ComboBoxMultiselectOption {

	private Long id;
	private String name;

	public ComboBoxMultiselectOption() {
	}

	public ComboBoxMultiselectOption(Long id, String name) {
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

}
