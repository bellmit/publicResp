package com.insta.hms.instasubscriptions;

import com.chargebee.ListResult;
import com.chargebee.Result;
import com.chargebee.models.Download;
import com.chargebee.models.Plan;
import com.chargebee.org.json.JSONException;
import com.chargebee.org.json.JSONObject;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.FlashScope;

import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class InstaSubscriptionsAction.
 */
public class InstaSubscriptionsAction extends BaseAction {

  /** The service. */
  ChargebeeService service = new ChargebeeService();

  /**
   * Gets the customer payment details.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param responce
   *          the responce
   * @return the customer payment details
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public ActionForward getCustomerPaymentDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse responce) throws SQLException, IOException {
    if (service.getCustomerId() == null || service.getCustomerId().isEmpty()) {
      request.setAttribute("error", "Please update Customer details");
    } else {
      try {
        service.setChargebeeEnvironment();
        ListResult dueInvoiceList = service.getDueInvoiceList();    
        ListResult paidInvoiceList = service.getPaidInvoiceList(null);
        
        Map<String, ArrayList<JSONObject>> invoiceMap = new HashMap<>();
        for (ListResult.Entry entry : dueInvoiceList) {
          if (invoiceMap.containsKey(entry.invoice().subscriptionId())) {
            invoiceMap.get(entry.invoice().subscriptionId()).add(entry.invoice().jsonObj);
          } else {
            ArrayList<JSONObject> newList = new ArrayList<JSONObject>();
            newList.add(entry.invoice().jsonObj);
            invoiceMap.put(entry.invoice().subscriptionId(), newList);
          }
        }
        Map<Integer, String> pagination = new HashMap<Integer, String>();
        pagination.put(1, null);
        pagination.put(2, paidInvoiceList.nextOffset());
        request.getSession(false).setAttribute("instaPaymentsPagination", pagination);

        ArrayList<JSONObject> otherInvoiceArrayList = new ArrayList<JSONObject>();
        ListResult otherInvoiceList = service.getOtherInvoiceList();
        for (ListResult.Entry entry : otherInvoiceList) {
          otherInvoiceArrayList.add(entry.invoice().jsonObj);
        }

        ArrayList<JSONObject> paidInvoiceArrayList = new ArrayList<JSONObject>();
        for (ListResult.Entry entry : paidInvoiceList) {
          paidInvoiceArrayList.add(entry.invoice().jsonObj);
        }

        Map<String, JSONObject> planMap = new HashMap<String, JSONObject>();
        ArrayList<JSONObject> customerSubscriptionArrayList = new ArrayList<JSONObject>();
        ListResult customerSubscriptionList = service.getCustomerSubscriptionList();
        for (ListResult.Entry entry : customerSubscriptionList) {
          customerSubscriptionArrayList.add(entry.subscription().jsonObj);
          Result result = Plan.retrieve(entry.subscription().planId()).request();
          Plan plan = result.plan();
          planMap.put(entry.subscription().id(), plan.jsonObj);
        }

        request.setAttribute("page", 1);
        request.setAttribute("next", paidInvoiceList.nextOffset() != null);
        request.setAttribute("planMap", planMap);
        request.setAttribute("otherInvoiceList", otherInvoiceArrayList);
        request.setAttribute("paidInvoiceList", paidInvoiceArrayList);
        request.setAttribute("invoiceMap", invoiceMap);
        request.setAttribute("customerSubscriptionList", customerSubscriptionArrayList);
      } catch (UnknownHostException ex) {
        request.setAttribute("error", "Please Check Internet Connection");
      }
    }
    return mapping.findForward("details");
  }

  /**
   * Pay customer invoice.
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
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings("unused")
  public ActionForward payCustomerInvoice(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, SQLException, ParseException {
    String invoiceId = (String) request.getParameter("recordInvoice");
    Integer recordAmount = Integer.parseInt(request.getParameter("recordAmount")) * 100;
    String paymentMethod = (String) request.getParameter("paymentMethod");
    String paymentDate = (String) request.getParameter("paymentDate");
    String refNo = (String) request.getParameter("refNo");
    String comments = (String) request.getParameter("comments");
    service.setChargebeeEnvironment();
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("redirect"));
    FlashScope flash = FlashScope.getScope(request);
    try {
      Result result = service.recordPayment(invoiceId, recordAmount, paymentMethod, refNo, comments,
          paymentDate);
      flash.info("Payment successfully recorded");
    } catch (UnknownHostException ex) {
      flash.error(ex.getMessage());
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
  }

  /**
   * Gets the invoice pdf.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the invoice pdf
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws JSONException
   *           the JSON exception
   */
  public ActionForward getInvoicePdf(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, JSONException {
    String invoiceId = (String) request.getParameter("invoiceId");
    service.setChargebeeEnvironment();
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    try {
      Result result = service.getInvoicePdf(invoiceId);
      Download download = result.download();
      response.sendRedirect((String) download.jsonObj.get("download_url"));
    } catch (UnknownHostException ex) {
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("redirect"));
      FlashScope flash = FlashScope.getScope(request);
      flash.error(ex.getMessage());
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }
    return null;
  }

  /**
   * Gets the next paid invoices.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the next paid invoices
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @SuppressWarnings("unchecked")
  public ActionForward getNextPaidInvoices(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    Integer page = Integer.parseInt(request.getParameter("page"));
    service.setChargebeeEnvironment();
    Map<Integer, String> pagination = (HashMap<Integer, String>) request.getSession()
        .getAttribute("instaPaymentsPagination");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    try {
      ListResult paidInvoiceList = service.getPaidInvoiceList(pagination.get(page));
      ArrayList<Object> paidInvoiceArrayList = new ArrayList<Object>();
      for (ListResult.Entry entry : paidInvoiceList) {
        paidInvoiceArrayList.add(entry.invoice().jsonObj);
      }
      Map<String, Serializable> map = new HashMap<String, Serializable>();
      map.put("list", paidInvoiceArrayList);
      map.put("nextOffset", paidInvoiceList.nextOffset());
      map.put("page", page);
      pagination.put(page + 1, paidInvoiceList.nextOffset());
      request.getSession().setAttribute("instaPaymentsPagination", pagination);
      JSONObject jsonOb = new JSONObject(map);
      response.getWriter().print(jsonOb);
    } catch (UnknownHostException ex) {
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("redirect"));
      FlashScope flash = FlashScope.getScope(request);
      flash.error(ex.getMessage());
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }
    return null;
  }
}
