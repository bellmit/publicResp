package com.insta.hms.stores;

import com.insta.hms.common.ConversionUtils;
import flexjson.JSONSerializer;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CommonReportAction extends Action {

  private static String[] stockfieldNames = { "manf_name", "category_name", "dept_name",
      "control_type_name", "package_type", "generic_name", "reorder_level", "package_cp", "mrp",
      "qty", };

  private static String[] stockfieldDisplayNames = { "Manufacturer", "Category", "Store",
      "Control Type", "Package Type", "Generic Name", "Reorder Level", "Cost Price", "MRP",
      "Quantity", };

  private static HashMap stockfieldDisplayNamesMap;
  
  private static int i;

  static {
    stockfieldDisplayNamesMap = new HashMap();
    for (i = 0; i < stockfieldNames.length; i++) {
      stockfieldDisplayNamesMap.put(stockfieldNames[i], stockfieldDisplayNames[i]);
    }
  }

  private static String[] salefieldNames = { "bill_type", "visit_type", "patient_full_name",
      "doctor_name", "sponsor_name", "type", "generic_name", "category_name", "control_type_name",
      "tax_rate", "store_name", "username", };

  private static String[] salefieldDisplayNames = { "Bill Type", "Visit Type", "Patient Name",
      "Doctor", "Sponsor", "Sale Type", "Generic Name", "Patient Category", "Control Type", "Vat%",
      "Store", "UserName", };

  private static HashMap salefieldDisplayNamesMap;

  static {
    salefieldDisplayNamesMap = new HashMap();
    for (i = 0; i < salefieldNames.length; i++) {
      salefieldDisplayNamesMap.put(salefieldNames[i], salefieldDisplayNames[i]);
    }
  }

  private static String[] purchaseItemfieldNames = {

      "purchase_type", "medicine_name", "category_name", "generic_name", "control_type_name",
      "manf_name", "store_name", "tax_name_and_tax_per",

  };

  private static String[] purchaseItemfieldDisplayNames = { "Purchase Type", "Item Name",
      "Category", "Generic Name", "Control Type", "Manufacturer", "Store Name",
      "Tax Type and Tax Per"

  };

  private static HashMap purchaseItemfieldDisplayNamesMap;

  static {
    purchaseItemfieldDisplayNamesMap = new HashMap();
    for (i = 0; i < purchaseItemfieldNames.length; i++) {
      purchaseItemfieldDisplayNamesMap.put(purchaseItemfieldNames[i],
          purchaseItemfieldDisplayNames[i]);
    }
  }

  private static String[] purchaseInvoicefieldNames = { "supplier_name", "invoice_status",
      "purchase_type", "tax_name",

  };

  private static String[] purchaseInvoicefieldDisplayNames = { "Supplier", "Invoice Status",
      "Purchase Type", "Tax Type",

  };

  private static HashMap purchaseInvoicefieldDisplayNamesMap;

  static {
    purchaseInvoicefieldDisplayNamesMap = new HashMap();
    for (i = 0; i < purchaseInvoicefieldNames.length; i++) {
      purchaseInvoicefieldDisplayNamesMap.put(purchaseInvoicefieldNames[i],
          purchaseInvoicefieldDisplayNames[i]);
    }
  }

  /**
   * Execute method of ComonReportAction.
   * 
   */
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, SQLException {

    JSONSerializer js = new JSONSerializer().exclude("class");
    request.setAttribute("stockfieldNames", stockfieldNames);
    request.setAttribute("stockfieldDisplayNamesMap", stockfieldDisplayNamesMap);
    request.setAttribute("salefieldNames", salefieldNames);
    request.setAttribute("salefieldDisplayNamesMap", salefieldDisplayNamesMap);
    request.setAttribute("purchaseItemfieldNames", purchaseItemfieldNames);
    request.setAttribute("purchaseItemfieldDisplayNamesMap", purchaseItemfieldDisplayNamesMap);
    request.setAttribute("purchaseInvoicefieldNames", purchaseInvoicefieldNames);
    request.setAttribute("purchaseInvoicefieldDisplayNamesMap",
        purchaseInvoicefieldDisplayNamesMap);
    request.setAttribute("checkpointJSON", js.serialize(
        ConversionUtils.listBeanToListMap(StoresStockCheckPointDAO.getCheckPointNames())));
    return mapping.findForward("getReportsCommonScreen");
  }

}
