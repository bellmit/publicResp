package com.insta.hms.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class PagedList. Simple class to hold a list of DTOs, and some integers to indicate the
 * position of the page in the entire list. Useful for fetching/using paged data from the db.
 */
public class PagedList {
  /*
   * Members
   */
  /** The dto list. */
  private List dtoList;

  /** The total records. */
  private int totalRecords;

  /** The page size. */
  private int pageSize;

  /** The page number. */
  private int pageNumber;

  /** The count info. */
  private Map countInfo; // to hold any other information that comes with the count query

  /**
   * Constructor Instantiates a new paged list.
   */
  public PagedList() {
    this.dtoList = new ArrayList();
    this.totalRecords = 0;
    this.pageSize = 20;
    this.pageNumber = 0;
  }

  /**
   * Instantiates a new paged list.
   *
   * @param dtoList      the dto list
   * @param totalRecords the total records
   * @param pageSize     the page size
   * @param pageNumber   the page number
   */
  public PagedList(List dtoList, int totalRecords, int pageSize, int pageNumber) {
    this.dtoList = dtoList;
    this.totalRecords = totalRecords;
    this.pageSize = pageSize;
    this.pageNumber = pageNumber;
  }

  /**
   * Instantiates a new paged list.
   *
   * @param dtoList      the dto list
   * @param totalRecords the total records
   * @param pageSize     the page size
   * @param pageNumber   the page number
   * @param countInfo    the count info
   */
  public PagedList(List dtoList, int totalRecords, int pageSize, int pageNumber, Map countInfo) {
    this.dtoList = dtoList;
    this.totalRecords = totalRecords;
    this.pageSize = pageSize;
    this.pageNumber = pageNumber;
    this.countInfo = countInfo;
  }

  /**
   * Gets the dto list.
   *
   * @return the dto list
   */
  public List getDtoList() {
    return dtoList;
  }

  /**
   * Sets the dto list.
   *
   * @param value the new dto list
   */
  public void setDtoList(List value) {
    dtoList = value;
  }

  /**
   * Gets the total records.
   *
   * @return the total records
   */
  public int getTotalRecords() {
    return totalRecords;
  }

  /**
   * Sets the total records.
   *
   * @param value the new total records
   */
  public void setTotalRecords(int value) {
    totalRecords = value;
  }

  /**
   * Gets the page size.
   *
   * @return the page size
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Sets the page size.
   *
   * @param value the new page size
   */
  public void setPageSize(int value) {
    pageSize = value;
  }

  /**
   * Gets the page number.
   *
   * @return the page number
   */
  public int getPageNumber() {
    return pageNumber;
  }

  /**
   * Sets the page number.
   *
   * @param value the new page number
   */
  public void setPageNumber(int value) {
    pageNumber = value;
  }

  /**
   * Gets the count info.
   *
   * @return the count info
   */
  public Map getCountInfo() {
    return countInfo;
  }

  /**
   * Sets the count info.
   *
   * @param value the new count info
   */
  public void setCountInfo(Map value) {
    countInfo = value;
  }

  /**
   * Gets the num pages.
   *
   * @return the num pages
   */
  public int getNumPages() {
    if (dtoList == null || dtoList.isEmpty()) {
      return 0;
    }
    int mod = totalRecords % pageSize;
    if (mod == 0) {
      return totalRecords / pageSize;
    } else {
      return totalRecords / pageSize + 1;
    }
  }

}
