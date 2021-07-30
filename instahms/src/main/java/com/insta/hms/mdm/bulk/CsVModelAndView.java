package com.insta.hms.mdm.bulk;

import com.insta.hms.common.AbstractCSVView;
import com.insta.hms.common.CsVViewImpl;

import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * The Class CsVModelAndView to create a CSV Document with a filename, Headers and rows.
 * 
 * @author anupama.mr
 */
public class CsVModelAndView extends ModelAndView {

  /**
   * Instantiates a new CsV model and view.
   *
   * @param fileName
   *          the file name
   * @param headers
   *          the headers
   */
  public CsVModelAndView(String fileName, String[] headers) {
    super(new CsVViewImpl());
    addHeader(headers);
    setFileName(fileName);
  }

  /**
   * Instantiates a new CsV model and view.
   *
   * @param fileName
   *          the file name
   */
  public CsVModelAndView(String fileName) {
    super(new CsVViewImpl());
    addHeader(null);
    setFileName(fileName);
  }

  /**
   * Sets the file name.
   *
   * @param fileName
   *          the new file name
   */
  public void setFileName(String fileName) {
    AbstractCSVView vw = (AbstractCSVView) this.getView();
    vw.setFileName(fileName);
  }

  /**
   * Adds the data.
   *
   * @param data
   *          the data
   */
  public void addData(List<String[]> data) {
    addObject("rows", data);
  }

  /**
   * Adds the entity.
   *
   * @param csvEntity
   *          the csv entity
   */
  public void addEntity(CsVBulkDataEntity csvEntity) {
    addObject("csvEntity", csvEntity);
  }

  /**
   * Adds the header.
   *
   * @param headers
   *          the headers
   */
  public void addHeader(String[] headers) {
    addObject("headers", headers);
  }

}
