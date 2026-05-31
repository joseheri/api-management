package com.lmco.jsf.fmt.dataprovider.common.pagination;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * HATEOAS navigation links for paginated API responses.
 * 
 * <p>Provides hypermedia controls for navigating between pages,
 * following REST HATEOAS (Hypermedia as the Engine of Application State) principles.
 * 
 * <p>Clients can follow these links without constructing URLs themselves,
 * making the API more discoverable and resilient to URL structure changes.
 * 
 * <p>Example:
 * <pre>
 * {
 *   "self": "/api/v1/aircrafts/AF-001/operational-statuses?limit=10&offset=0",
 *   "next": "/api/v1/aircrafts/AF-001/operational-statuses?limit=10&offset=10",
 *   "prev": null,
 *   "first": "/api/v1/aircrafts/AF-001/operational-statuses?limit=10&offset=0",
 *   "last": "/api/v1/aircrafts/AF-001/operational-statuses?limit=10&offset=30"
 * }
 * </pre>
 * 
 * @author MxSys Engineering Team
 * @version 1.0.0
 * @since 2026-05-22
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NavigationLinks {
    
    private final String self;
    private final String next;
    private final String prev;
    private final String first;
    private final String last;
    
    /**
     * Creates navigation links for pagination.
     * 
     * @param self link to current page (required)
     * @param next link to next page (null if none)
     * @param prev link to previous page (null if none)
     * @param first link to first page (required)
     * @param last link to last page (required)
     */
    public NavigationLinks(String self, String next, String prev, String first, String last) {
        this.self = self;
        this.next = next;
        this.prev = prev;
        this.first = first;
        this.last = last;
    }
    
    /**
     * Gets the link to the current page.
     * 
     * @return the self link
     */
    public String getSelf() {
        return self;
    }
    
    /**
     * Gets the link to the next page.
     * 
     * @return the next page link, or null if on last page
     */
    public String getNext() {
        return next;
    }
    
    /**
     * Gets the link to the previous page.
     * 
     * @return the previous page link, or null if on first page
     */
    public String getPrev() {
        return prev;
    }
    
    /**
     * Gets the link to the first page.
     * 
     * @return the first page link
     */
    public String getFirst() {
        return first;
    }
    
    /**
     * Gets the link to the last page.
     * 
     * @return the last page link
     */
    public String getLast() {
        return last;
    }
    
    @Override
    public String toString() {
        return String.format("NavigationLinks{self='%s', next='%s', prev='%s', first='%s', last='%s'}", 
            self, next, prev, first, last);
    }
}
