package com.insta.hms.stores;

import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StoreTaxReportAction extends DispatchAction{

	static Logger log = LoggerFactory.getLogger(StoreTaxReportAction.class);

	@IgnoreConfidentialFilters
	public ActionForward getScreen(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		return m.findForward("taxReportScreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward exportToXls(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		res.setHeader("Content-type","application/vnd.ms-excel");
		res.setHeader("Content-disposition","attachment; filename=csv.xls");
		res.setHeader("Readonly","true");

		String[] purchaseHeaders = {"serial_no","name_of_seller","seller_tin","commodity_code","invoice_no","invoice_date","purchase_value",
				  "tax_rate","vat_cst_paid","category"};

		String[] salesHeaders = {"serial_no","name_of_buyer","buyer_tin","commodity_code","invoice_no","invoice_date","sales_value",
				  "tax_rate","vat_cst_paid","category"};

		String[] returnHeaders = {"serial_no","sec_code","seller_tin","commodity_code","inputtax_value","tax_rate","reversal_credit"};

		String[] sheets = {"ANNEX I","ANNEX II","ANNEX III"};

		String fromDate = req.getParameter("fromDate");
		String toDate = req.getParameter("toDate");
		String deptId = req.getParameter("store_id");
		String storeType = req.getParameter("store_type_id");

		try {
			HSSFWorkbook workbook = new HSSFWorkbook();

			for (int h=0;h<sheets.length;h++) {
				StoreTaxReportDAO.resetDummySequence();
				HSSFSheet worksheetOP = workbook.createSheet(sheets[h]);
				List<BasicDynaBean> Charges = StoreTaxReportDAO.getChargesXLS(sheets[h],fromDate,toDate,deptId,storeType);

				HSSFRow row=worksheetOP.createRow(0);


				int k=0;
				for (String key: h == 0 ? purchaseHeaders : h == 1 ? salesHeaders : returnHeaders) {
					row.createCell((k)).setCellValue(new HSSFRichTextString(key.toUpperCase()));
					k++;
				}

				for(int j=0;j<Charges.size();j++){

					row=worksheetOP.createRow(j+1);
					BasicDynaBean bean = Charges.get(j);
					int n=0;
					DynaProperty[] d = bean.getDynaClass().getDynaProperties();
					for ( String key : h == 0 ? purchaseHeaders : h == 1 ? salesHeaders : returnHeaders )
						{
						String type = d[n].getType().getName();

						if (type.equalsIgnoreCase("java.lang.Long")) row.createCell(n).setCellValue((Long)bean.get(key));
						else if (type.equalsIgnoreCase("java.lang.Integer")) row.createCell(n).setCellValue((Integer)bean.get(key));
						else if (type.equalsIgnoreCase("java.sql.Date")) row.createCell(n).setCellValue((Date)bean.get(key));
						else if (type.equalsIgnoreCase("java.math.BigDecimal")) row.createCell(n).setCellValue(((BigDecimal)bean.get(key)).doubleValue());
						else  row.createCell(n).setCellValue(new HSSFRichTextString(bean.get(key).toString()));

						n++;
			         }
				}

			}
			java.io.OutputStream os = res.getOutputStream();
			workbook.write(os);

			os.flush();
			os.close();
			} catch (Exception e) {
				log.error("Caught exception, rolling back", e);
			}
			return null;
	}

}