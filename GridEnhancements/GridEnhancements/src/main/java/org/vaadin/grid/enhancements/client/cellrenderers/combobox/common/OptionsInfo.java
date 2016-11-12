package org.vaadin.grid.enhancements.client.cellrenderers.combobox.common;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class OptionsInfo {
	private int currentPage = -1;
	private int startItem = -1;
	private int endItem = -1;
	private int totalItems = -1;
	private int pageAmount = -1;
	private String inputPrompt = "type here";
	private String selectAllText = null;
	private String deselectAllText = null;

	public OptionsInfo() {
	}

	public OptionsInfo(int pageAmount, String inputPrompt, String selectAllText, String deselectAllText) {
		this.pageAmount = pageAmount;
		this.inputPrompt = inputPrompt;
		this.setSelectAllText(selectAllText);
		this.setDeselectAllText(deselectAllText);
	}

	public int getCurrentPage() {
		return this.currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getStartItem() {
		return this.startItem;
	}

	public void setStartItem(int startItem) {
		this.startItem = startItem;
	}

	public int getEndItem() {
		return this.endItem;
	}

	public void setEndItem(int endItem) {
		this.endItem = endItem;
	}

	public int getTotalItems() {
		return this.totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public void setPageAmount(int pageAmount) {
		this.pageAmount = pageAmount;
	}

	public int getPageAmount() {
		return this.pageAmount;
	}

	public String getInputPrompt() {
		return this.inputPrompt;
	}

	public void setInputPrompt(String inputPrompt) {
		this.inputPrompt = inputPrompt;
	}

	public String getSelectAllText() {
		return this.selectAllText;
	}

	public void setSelectAllText(String selectAllText) {
		this.selectAllText = selectAllText;
	}

	public String getDeselectAllText() {
		return this.deselectAllText;
	}

	public void setDeselectAllText(String deselectAllText) {
		this.deselectAllText = deselectAllText;
	}
}
