package admin.accounts;

import java.io.Serializable;

public class AccountSearchCriteria implements Serializable {

	/** User-provided string matching to the start of an account number. */
	private String searchString = "";

    /** The maximum number of search results per page. */
    private int pageSize;

    /** The current search results page number. */
    private int page;
    
	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}
	
	public void previousPage() {
		if (page > 0) {
			page--;
		}
	}
	
	public void nextPage() {
		page++;
	}
	
	public void resetPage() {
		page = 0;
	}

}
