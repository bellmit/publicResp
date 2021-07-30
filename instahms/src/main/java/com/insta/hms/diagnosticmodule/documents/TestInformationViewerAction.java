package com.insta.hms.diagnosticmodule.documents;

import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.documentpersitence.TestDocumentAbstractImpl;
import com.insta.hms.master.DocumentTypeMaster.DocumentTypeMasterDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class TestInformationViewerAction.
 *
 * @author krishna
 */
public class TestInformationViewerAction extends DispatchAction {

  /**
   * List.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ServletException, IOException {
    int prescId = Integer.parseInt(request.getParameter("prescribed_id"));
    BasicDynaBean prescBean = LaboratoryDAO.getPrescribedDetails(prescId);
    Integer collPrescId = (Integer) prescBean.get("coll_prescribed_id");
    BasicDynaBean collPrescBean = null;
    if (collPrescId != null && collPrescId.intValue() != 0) {
      collPrescBean = LaboratoryDAO.getPrescribedDetails(collPrescId);
    }

    request.setAttribute("collPrescBean", collPrescBean);
    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("prescBean", prescBean);
    List<TestDocumentDTO> docList = getList(prescId);
    request.setAttribute("docList", docList);
    request.setAttribute("docListJSON", js.deepSerialize(docList));
    request.setAttribute("docTypeValues",
        js.exclude("class").serialize(new DocumentTypeMasterDAO().getdocTypeDetails()));
    return mapping.findForward("list");
  }

  /**
   * Gets the list.
   *
   * @param prescribedId
   *          the prescribed id
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  private List<TestDocumentDTO> getList(int prescribedId) throws SQLException {
    TestDocumentAbstractImpl impl = new TestDocumentAbstractImpl();
    List<TestDocumentDTO> list = new ArrayList<>();
    List<BasicDynaBean> docList = impl.getTestDocuments(prescribedId);

    for (BasicDynaBean bean : docList) {
      TestDocumentDTO dto = new TestDocumentDTO();

      dto.setDocid((Integer) bean.get("doc_id") + "");
      dto.setVisitid((String) bean.get("pat_id"));
      dto.setTitle("Supporting document for " + bean.get("test_name") + " : "
          + bean.get("doc_name").toString());
      String displayUrl = "";
      if (bean.get("category").equals("DEP_LAB")) {
        dto.setType("SYS_LR");
        displayUrl = "/Laboratory/TestDocumentsPrint.do?_method=print&doc_id=" + bean.get("doc_id");
      } else {
        dto.setType("SYS_RR");
        displayUrl = "/Radiology/TestDocumentsPrint.do?_method=print&doc_id=" + bean.get("doc_id");
      }
      if (bean.get("doc_format").equals("doc_link")) {
        dto.setDisplayUrl((String) bean.get("doc_location"));
        dto.setExternalLink(true);
      } else {
        dto.setDisplayUrl(displayUrl);
      }
      dto.setDoctor((String) bean.get("doctor_name"));
      dto.setDate((java.sql.Date) bean.get("doc_date"));
      dto.setUpdatedBy((String) bean.get("username"));
      dto.setVisitDate((java.sql.Date) bean.get("reg_date"));
      dto.setCenterName((String) bean.get("center_name"));
      list.add(dto);
    }

    return list;
  }

}
