package com.insta.hms.accountinglog;

import java.io.File;
import java.io.FilenameFilter;

/**
 * The Class FileNameStartsWithFilter.
 *
 * @author krishna
 */
public class FileNameStartsWithFilter implements FilenameFilter {

  /** The starts with. */
  public String startsWith;

  /**
   * Instantiates a new file name starts with filter.
   *
   * @param dbFileName
   *          the db file name
   */
  public FileNameStartsWithFilter(String dbFileName) {
    this.startsWith = dbFileName;
  }

  /**
   * Accept.
   *
   * @param dir the dir
   * @param name the name
   * @return true, if successful
   * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
   */
  public boolean accept(File dir, String name) {
    return name.startsWith(startsWith);
  }
}
