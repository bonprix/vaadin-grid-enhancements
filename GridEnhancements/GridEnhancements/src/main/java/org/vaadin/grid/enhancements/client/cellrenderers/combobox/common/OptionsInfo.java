package org.vaadin.grid.enhancements.client.cellrenderers.combobox.common;

/**
 * @author Mikael Grankvist - Vaadin Ltd
 */
public class OptionsInfo {
	int currentPage = -1;
	int startItem = -1;
	int endItem = -1;
	int totalItems = -1;
	int pageAmount = -1;

	public OptionsInfo() {
	}

	public OptionsInfo(int pageAmount) {
		this.pageAmount = pageAmount;
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
}
