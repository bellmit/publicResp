package com.insta.hms.stores;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.paymentterms.PaymentTermsController;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Deprecated
public class PaymentTermsMasterAction extends BaseAction {

  @MigratedTo(value = PaymentTermsController.class, method = "list")
  public ActionForward getTemplateDashBoard(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    Map map = getParameterMap(request);
    PagedList list = PaymentTermsMasterDAO.searchPhterms(map,
        ConversionUtils.getListingParameter(map));
    request.setAttribute("pagedList", list);
    return mapping.findForward("templatedashboard");
  }

  @MigratedTo(value = PaymentTermsController.class, method = "show")
  public ActionForward getTemplateDetailsScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    String tempCode = request.getParameter("tempName");
    JSONSerializer js = new JSONSerializer().exclude("class");
    if (tempCode != null) {
      BasicDynaBean tempdto = new GenericDAO("ph_payment_terms").findByKey("template_code",
          tempCode);
      request.setAttribute("tempdto", tempdto);
    }
    ArrayList<String> templates = PaymentTermsMasterDAO.getTemplateNamesInMaster();
    request.setAttribute("templateCode", templates);
    request.setAttribute("templateList",
        js.serialize(PaymentTermsMasterDAO.getTemplateNamesMaster()));
    return mapping.findForward("templatedetails");
  }

  @MigratedTo(value = PaymentTermsController.class, method = "create")
  public ActionForward saveTemplateDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
    FlashScope flash = FlashScope.getScope(request);
    BasicDynaBean bean = null;
    ActionRedirect redirect = new ActionRedirect("tempdetails.do");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("_method", "getTemplateDetailsScreen");
    String operation = request.getParameter("operation");
    String template_code = request.getParameter("template_code");
    String template_name = request.getParameter("template_name");
    Map params = request.getParameterMap();
    List errors = new ArrayList();
    boolean flag = true;
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      if (operation.equalsIgnoreCase("insert")) {
        bean = new GenericDAO("ph_payment_terms").getBean();
        ConversionUtils.copyToDynaBean(params, bean, errors);
        template_code = AutoIncrementId.getNewIncrUniqueId("TEMPLATE_CODE", "PH_PAYMENT_TERMS",
            "TERMSTEMPLATE");
        bean.set("template_code", template_code);
        if (flag)
          flag = new GenericDAO("ph_payment_terms").insert(con, bean);
      } else {
        bean = new GenericDAO("ph_payment_terms").getBean();
        ConversionUtils.copyToDynaBean(params, bean, errors);
        if (flag)
          flag = new GenericDAO("ph_payment_terms").update(con, bean.getMap(), "template_code",
              template_code) > 0;
      }
      if (flag) {
        con.commit();
        if (operation.equalsIgnoreCase("insert"))
          flash.put("success",
              "Template  :" + template_name + " Details are Successfully Inserted");
        else
          flash.put("success", "Template  :" + template_name + " Details are Successfully Updated");
      }
    } catch (Exception e) {
      if (con != null) {
        con.rollback();
      }
      flash.put("error", "Transaction Failure");
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    if (bean != null) {
      redirect.addParameter("tempName", bean.get("template_code"));
    }

    return redirect;
  }

}
