package com.insta.hms.common;

import au.com.bytecode.opencsv.CSVWriter;

import java.util.List;
import java.util.Map;

/**
 * The Class CSVViewImpl to implement custom building of CSV documents.
 * 
 * @author tanmay.k
 */
public class CsVViewImpl extends AbstractCSVView {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.common.AbstractCSVView#buildCsVDocument(au.com.bytecode.opencsv.CSVWriter,
   * java.util.Map)
   */
  @Override
  protected void buildCsVDocument(CSVWriter csvWriter, Map<String, Object> model) {
    String[] headers = (String[]) model.get("headers");
    List<String[]> rows = (List<String[]>) model.get("rows");

    if (null != headers && headers.length > 0) {
      csvWriter.writeNext(headers);
    }

    if (null != rows && !rows.isEmpty()) {
      csvWriter.writeAll(rows);
    }
  }

}
