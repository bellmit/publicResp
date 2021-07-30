package com.insta.hms.common;

import au.com.bytecode.opencsv.CSVWriter;

import org.springframework.web.servlet.view.AbstractView;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractCSVView extends AbstractView {
  private String fileName;

  public void setFileName(String fileName) {
    this.fileName = fileName.concat(".csv");
  }

  protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
    String headerKey = "Content-Disposition";
    String headerValue = String.format("attachment; filename=\"%s\"", fileName);
    response.setContentType("text/csv");
    response.setHeader(headerKey, headerValue);
  }

  protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
      HttpServletResponse response) throws IOException {

    CSVWriter csvWriter = new CSVWriter(response.getWriter(), CSVWriter.DEFAULT_SEPARATOR);
    buildCsVDocument(csvWriter, model);
    csvWriter.close();
  }

  protected abstract void buildCsVDocument(CSVWriter csvWriter, Map<String, Object> model);
}