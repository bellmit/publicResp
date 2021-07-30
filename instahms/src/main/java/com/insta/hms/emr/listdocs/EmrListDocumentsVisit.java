package com.insta.hms.emr.listdocs;

import com.bob.hms.common.RequestContext;
import com.insta.hms.emr.EMRDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class EmrListDocumentsVisit extends EmrListDocuments {

  private String visitId;
  private Logger logger = LoggerFactory.getLogger(EmrListDocumentsVisit.class);
  
  public EmrListDocumentsVisit(int start, int end, HttpServletRequest req, String visitId) {
    super(start, end, req);
    this.visitId = visitId;
  }

  @Override
  public List<EMRDoc> call() throws Exception {
    RequestContext.setRequest(req);
    listDocuments();
    return allDocs;
  }

  @Override
  public void listDocuments() {
    List<EMRDoc> list = null;
    for (int i = start; i < end; i++) {
      try {
        list = docProviders[i].getProviderImpl().listDocumentsByVisit(visitId);
        synchronized (obj) {
          allDocs = emrDocFilter.applyFilter(allDocs, list, req, userInRc);
        }
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }
  }

}
