/**
 *
 */
package com.insta.hms.stores;

import au.com.bytecode.opencsv.CSVReader;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author krishna
 *
 */
public class BatchPOAction extends DispatchAction {

	private static Logger logger = LoggerFactory.getLogger(BatchPOAction.class);
	private static GenericDAO poMainDAO = new GenericDAO("store_po_main");
	private static GenericDAO poDetailsDAO = new GenericDAO("store_po");
	private static PharmacymasterDAO itemMasterDAO = new PharmacymasterDAO();
	private static SupplierMasterDAO supplierDAO = new SupplierMasterDAO();

  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		HttpSession session = request.getSession(false);
		String dept_id = (String) session.getAttribute("pharmacyStoreId");
		if (dept_id != null && !dept_id.equals("")) {
			BasicDynaBean dept = new GenericDAO("stores").findByKey("dept_id", Integer.parseInt(dept_id));
			request.setAttribute("dept_id", dept_id);

			request.setAttribute("isSuperStore", dept.get("is_super_store"));
			request.setAttribute("default_store", "Yes");
		}
		if (dept_id != null && dept_id.equals("")) {
			request.setAttribute("dept_id", dept_id);
			request.setAttribute("default_store", "No");
		}

		return mapping.findForward("batchpo");
	}

	public ActionForward createBatchPO(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("batchpoRedirect"));
		FlashScope flash = FlashScope.getScope(request);

		BatchPOForm bpf = (BatchPOForm) form;
		CSVReader csvReader = new CSVReader(new InputStreamReader(bpf.getCsv_file().getInputStream()));
		String[] header = csvReader.readNext();
		StringBuilder errorsSB = new StringBuilder();
		HttpSession session = request.getSession(false);
		String username = (String) session.getAttribute("userId");

		String error = null;

		validate: {

			if (header.length < 1) {
				error = "Uploaded file does not appear to be a CSV file (no headers found)";
				break validate;
			}

			if (!header[0].matches("\\p{Print}*")) {
				error = "Uploaded file does not appear to be a CSV file (non-printable characters found)";
				break validate;
			}
		}

		if (error != null) {
			flash.put("error", error);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		// trim all the headers: common mistake by people
		for (int i=0; i<header.length; i++) {
			header[i] = header[i].trim();
		}

		int lineNum = 1;
		String[] line = null;
		BasicDynaBean pomainBean = null;
		BasicDynaBean poDetailsBean = null;
		Map<String, Map<String, BigDecimal>> supplierMap = new LinkedHashMap<String, Map<String, BigDecimal>>();
		Integer storeId = Integer.parseInt(request.getParameter("deptId"));
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean allSuccess = false;
		String allPONOs = "";
		int noOfErrors = 0;
		PreparedStatement ps = null;
		try {
			txn : {
				int numDecimals = 2;
				GenericPreferencesDTO genPrefsDTO = GenericPreferencesDAO.getGenericPreferences(con);
				numDecimals = genPrefsDTO.getDecimalDigits();

				String showVAT = genPrefsDTO.getShowVAT();
				BigDecimal vatRate =  BigDecimal.ZERO ;
				String vatType = "MB";


				while ((line = csvReader.readNext()) != null) {
					lineNum++;
					pomainBean = poMainDAO.getBean();
					poDetailsBean = poDetailsDAO.getBean();

					String supplierId = "";
					Integer supplierCreditPeriod = 0;
					int medicine_id = 0;
					BigDecimal orderQty = BigDecimal.ZERO;
					BigDecimal costPrice = BigDecimal.ZERO;
					BasicDynaBean itemBean = null;

					boolean processingError = false;
					for (int i=0; i<header.length && i<line.length; i++) {
						String value = line[i].trim();

						if (header[i].equals("Item")) {
							itemBean = itemMasterDAO.findByKey(con, "medicine_name", value);
							if (itemBean != null) {
								medicine_id = (Integer) itemBean.get("medicine_id");
								vatRate = showVAT.equals("N") ? (BigDecimal)itemBean.get("tax_rate") : vatRate;
								vatType = showVAT.equals("N") ? (String)itemBean.get("tax_type") : vatType;

							} else {
								addError(lineNum, "Item : "+value+" doesn't exists in the master", errorsSB);
								processingError = true;
								noOfErrors++;
								continue;
							}
						} else if (header[i].equals("Preferred Supplier")) {
							BasicDynaBean supplier = supplierDAO.getSupplierDetails(con, value);
							if (supplier == null) {
								addError(lineNum, "Supplier : "+value+" doesn't exists in the master", errorsSB);
								noOfErrors++;
								processingError = true;
								continue;
							} else {
								supplierId = (String) supplier.get("supplier_code");
								supplierCreditPeriod = (Integer) ((BigDecimal)supplier.get("credit_period")).intValueExact();
							}
						} else if (header[i].equals("Order Package")) {
							try {
								orderQty = new BigDecimal(value).setScale(0, BigDecimal.ROUND_HALF_UP);
							} catch(NumberFormatException e) {
								logger.error("", e);
								addError(lineNum, "Order Package Qty should be numeric : "+value, errorsSB);
								noOfErrors++;
								processingError = true;
								continue;
							}
							if (orderQty.compareTo(BigDecimal.ZERO) < 1) {
								addError(lineNum, "Order Package Qty should be greater than Zero : ", errorsSB);
								noOfErrors++;
								processingError = true;
								continue;
							}
						} else if (header[i].equals("Cost Price")) {
							try {
								costPrice = new BigDecimal(value).setScale(numDecimals, BigDecimal.ROUND_HALF_UP);
								if (costPrice.compareTo(BigDecimal.ZERO) < 1 || costPrice.compareTo(BigDecimal.ZERO) == 0) {
									addError(lineNum, "Rate should be greater than Zero : ", errorsSB);
									noOfErrors++;
									processingError = true;
									continue;
								}
							} catch(NumberFormatException e) {
								logger.error("", e);
								addError(lineNum, "Cost Price should be numeric : "+value, errorsSB);
								noOfErrors++;
								processingError = true;
								continue;
							}
						}
					}
					if (processingError) continue;
					/*
					 * while uploading from csv we are always assuming qty selection in package units.
					 *	so all the calculations will be done based on the package units.
					 *
					 * store_po table saves the qty in issue units. so while saving into store_po convert the
					 * qty into issue units by multiplying with package size(issue_base_unit).
					 */
					BasicDynaBean poItem = StockReorderDAO.getPOItem(con, storeId, medicine_id);
					if ( poItem == null ) {
						flash.put("error", "Uploaded file does not appear to be correct CSV file (medicine not found)");
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					}
					BigDecimal issue_base_unit = ((BigDecimal) poItem.get("issue_base_unit")).setScale(numDecimals, BigDecimal.ROUND_HALF_UP);

					BigDecimal rate = costPrice;
					BigDecimal mrp = ((BigDecimal) poItem.get("mrp")).setScale(numDecimals, BigDecimal.ROUND_HALF_UP);

					BigDecimal disc = BigDecimal.ZERO;
					BigDecimal ced = BigDecimal.ZERO;
					BigDecimal admrp = BigDecimal.ZERO;
					BigDecimal hundred = new BigDecimal(100).setScale(numDecimals, BigDecimal.ROUND_HALF_UP);
					BigDecimal medTotal = BigDecimal.ZERO;

					if (vatRate.compareTo(BigDecimal.ZERO) > 0) {
						// formula : (mrp/(100+vatRate)) * 100
						admrp = (mrp.divide(hundred.add(vatRate), 4, BigDecimal.ROUND_HALF_UP)).multiply(hundred);
					} else {
						admrp = mrp;
					}
					admrp = admrp.setScale(numDecimals, BigDecimal.ROUND_HALF_UP);

					BigDecimal vat = BigDecimal.ZERO;
					if (vatRate.compareTo(BigDecimal.ZERO) > 0) {
						if (vatType.equals("M")) {
							// formula (admrp * hvattaxper/100)*qty)
							vat = (admrp.multiply(vatRate.divide(hundred))).multiply(orderQty);
						} else if (vatType.equals("MB")) {
							// formula (admrp * hvattaxper/100)*(qty+bqty)
							BigDecimal bonusQty = BigDecimal.ZERO;
							vat = (admrp.multiply(vatRate.divide(hundred))).
									multiply(orderQty.add(bonusQty));
						} else if (vatType.equals("CB")) {
							// formula ((rate * (qty+bqty) - disc) * hvattaxper/100)
							BigDecimal bonusQty = BigDecimal.ZERO;
							vat = rate.multiply(orderQty.add(bonusQty)).
									subtract(disc).multiply(vatRate.divide(hundred));
						} else {
							// formula ((rate * qty - disc + ced) * hvattaxper/100)
							vat = rate.multiply(orderQty).
									subtract(disc).add(ced).multiply(vatRate.divide(hundred));
					    }
						vat = vat.setScale(numDecimals, BigDecimal.ROUND_HALF_UP);
					} else vat = vat.setScale(numDecimals, BigDecimal.ROUND_HALF_UP);

					medTotal = rate.multiply(orderQty).add(vat).subtract(disc).add(ced);
					medTotal = medTotal.setScale(numDecimals, BigDecimal.ROUND_HALF_UP);

					String poNo = null;
					if (supplierMap.containsKey(supplierId)) {
						Map<String, BigDecimal> poAmtsMap = (Map<String, BigDecimal>) supplierMap.get(supplierId);
						for (Map.Entry<String, BigDecimal> entry : poAmtsMap.entrySet()) {
							BigDecimal poTotal = entry.getValue();
							poTotal = poTotal.add(medTotal);
							poAmtsMap.put(entry.getKey(), poTotal);
							poNo = entry.getKey();
						}
						supplierMap.put(supplierId, poAmtsMap);

					} else {
						poNo = PurchaseOrderDAO.getNextId(""+storeId);
						Date currentDate = new Date();
						java.sql.Timestamp timeStamp =new java.sql.Timestamp(currentDate.getTime());
						java.sql.Date sqlCurrentDate = new java.sql.Date(currentDate.getTime());

						pomainBean.set("po_date", sqlCurrentDate);
						pomainBean.set("po_no", poNo);
						pomainBean.set("supplier_id", supplierId);
						pomainBean.set("po_total", medTotal);
						pomainBean.set("actual_po_date", timeStamp);
						pomainBean.set("user_id", username);
						pomainBean.set("mrp_type", "I");
						pomainBean.set("status", "O");
						pomainBean.set("store_id", storeId);
						pomainBean.set("vat_rate", vatRate);
						pomainBean.set("vat_type", vatType);
						pomainBean.set("discount", BigDecimal.ZERO);
						pomainBean.set("round_off", BigDecimal.ZERO);
						pomainBean.set("po_qty_unit", "P");
						pomainBean.set("dept_id", request.getParameter("dept"));
						pomainBean.set("credit_period", supplierCreditPeriod);
						pomainBean.set("last_modified_by", username);
						if (!poMainDAO.insert(con, pomainBean)) {
							error = "Failed to insert the details into PO Main";
							break txn;
						}
						Map<String, BigDecimal> poAmtsMap = new HashMap<String, BigDecimal>();
						poAmtsMap.put(poNo, medTotal);
						supplierMap.put(supplierId, poAmtsMap);
					}

					poDetailsBean.set("po_no", poNo);
					poDetailsBean.set("medicine_id", medicine_id);
					poDetailsBean.set("qty_req", ((orderQty).multiply(issue_base_unit)).setScale(numDecimals, BigDecimal.ROUND_HALF_UP));
					poDetailsBean.set("bonus_qty_req", BigDecimal.ZERO);
					poDetailsBean.set("mrp", mrp);
					poDetailsBean.set("adj_mrp", admrp);
					poDetailsBean.set("cost_price", costPrice);
					poDetailsBean.set("vat", vat);
					poDetailsBean.set("discount", disc);
					poDetailsBean.set("med_total", medTotal);
					poDetailsBean.set("vat_rate", vatRate);
					poDetailsBean.set("discount_per", BigDecimal.ZERO);
					poDetailsBean.set("po_pkg_size", issue_base_unit);
					poDetailsBean.set("item_ced_per", BigDecimal.ZERO);
					poDetailsBean.set("item_ced", BigDecimal.ZERO);
					poDetailsBean.set("vat_type", vatType);
					if (!poDetailsDAO.insert(con, poDetailsBean)) {
						error = "Failed to insert the details in PO Details table";
						break txn;
					}

				}
				ps = con.prepareStatement("UPDATE store_po_main SET po_total=? WHERE po_no=?");
				boolean first = true;
				int i=0;
				for (Map.Entry<String, Map<String, BigDecimal>> supplier: supplierMap.entrySet()) {
					for (Map.Entry<String, BigDecimal> po: supplier.getValue().entrySet()) {
						ps.setBigDecimal(1, po.getValue());
						ps.setString(2, po.getKey());
						if (ps.executeUpdate() == 0) {
							error = "Failed to update PO total for PO. NO. :"+po.getKey();
							break txn;
						}
						if (!first)
							allPONOs += ",";
						allPONOs += po.getKey();
						first = false;
						if (i == 15) {
							i=0;
							allPONOs += "<br/>";
						} else	i++;
					}
				}

				allSuccess = true;
			}
		} finally {
      if (ps != null) {
        ps.close();
      }
			DataBaseUtil.commitClose(con, allSuccess);
		}
		StringBuilder infoMessage = new StringBuilder();
		if (!allPONOs.isEmpty())
			infoMessage.append("Created PO.NO.s are : "+allPONOs + "<br/>");
		if (noOfErrors > 0) {
			infoMessage.append("Lines with errors: ").append(noOfErrors).append("<br>") ;
			infoMessage.append("<hr>");
		}
		infoMessage.append(errorsSB);

		flash.put("error", error);
		flash.put("info", infoMessage.toString());
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	private void addError(int line, String msg, StringBuilder errorsSB) {
		if (line > 0) {
			errorsSB.append("Line ").append(line).append(": ");
		} else {
			errorsSB.append("Error in header: ");
		}
		errorsSB.append(msg).append("<br>");
		logger.error("Line " + line + ": " + msg);
	}

}
