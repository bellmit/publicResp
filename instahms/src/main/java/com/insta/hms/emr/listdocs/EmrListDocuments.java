package com.insta.hms.emr.listdocs;

import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRDocFilter;
import com.insta.hms.emr.EMRInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.servlet.http.HttpServletRequest;

/**
 * The Class EmrListDocuments.
 */
public abstract class EmrListDocuments implements Callable<List<EMRDoc>>{
  
  /** The obj. */
  protected final Object obj = new Object();
  
  /** The all docs. */
  protected List<EMRDoc> allDocs = Collections.synchronizedList(new ArrayList<EMRDoc>());
  
  /** The doc providers. */
  protected static EMRInterface.Provider[] docProviders;
  
  /** The no of doc providers. */
  protected static int noOfProviders;
  
  /** The start index of providers list. */
  protected int start;
  
  /** The end index of providers list. */
  protected int end;
  
  /** HTTP request. */
  protected HttpServletRequest req;
  
  /** User in request context. */
  protected boolean userInRc = false;
  
  /** The emr doc filter. */
  protected EMRDocFilter emrDocFilter = new EMRDocFilter();
  static {
    docProviders = EMRInterface.Provider.values();
    noOfProviders = docProviders.length; 
  }

  /**
   * Instantiates a new emr list documents.
   *
   * @param start the start
   * @param end the end
   * @param req the req
   */
  protected EmrListDocuments(int start, int end, HttpServletRequest req) {
    this.start = start;
    this.end = end;
    this.req = req;
  }
  
  /**
   * List documents.
   */
  public abstract void listDocuments();
}
