package accounts;

import java.io.Serializable;

public class AccountSearchCriteria implements Serializable {

	private static final long serialVersionUID = 1L;

	/** User-provided string matching to the start of an account number. */
	private String accountString;

    /** The maximum number of search results per page. */
    private int pageSize = 5;

    /** The current search results page number. */
    private int page;
    
	public String getAccountString() {
		return accountString;
	}

	public void setAccountString(String accountString) {
		this.accountString = accountString;
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

	public void nextPage() {
		page++;
	}

	public void previousPage() {
		if (page > 0) {
			page--;
		}
	}
	
	public void resetPage() {
		page = 0;
	}
}
