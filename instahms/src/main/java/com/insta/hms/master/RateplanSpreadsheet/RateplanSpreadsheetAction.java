package com.insta.hms.master.RateplanSpreadsheet;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RateplanSpreadsheetAction extends DispatchAction{

	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException, SQLException {


		return mapping.findForward("rateplanChoosenScreen");
	}

	public static RatePlanSpreadsheetProvider provider;

	static {
		provider = new RatePlanSpreadsheetProvider();
	}

	public ActionForward downloadCharges(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException, SQLException {

		String orgId = request.getParameter("org_id");
		BasicDynaBean orgBean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
		String orgName = (String) orgBean.get("org_name");
		List<String> subHeaders4AllCharges = Arrays.asList("Hospital", "Doctor", "Anaes", "Total");
		List<String> subHeaders4OnlyHospital = Arrays.asList("Hospital");
		boolean showDiscount = false;
		boolean onlyHospitalCharge = false;
		int serviceGrpId = Integer.parseInt(request.getParameter("service_group_id"));
		String serviceGroupName = (String)new GenericDAO("service_groups").
					findByKey("service_group_id", serviceGrpId).get("service_group_name");
		HSSFWorkbook workBook = new HSSFWorkbook();
		List<String> subHeaders = subHeaders4AllCharges;

		if (request.getParameter("includeDiscount") != null)
			showDiscount = true;
		if (request.getParameter("includeOnlyHospCharge") != null) {
			onlyHospitalCharge = true;
			subHeaders = subHeaders4OnlyHospital;
		}


		int servicegroupId = Integer.parseInt(request.getParameter("service_group_id"));

		provider.setShowDiscount(showDiscount);
		provider.setOnlyHospitalCharge(onlyHospitalCharge);
		provider.setSubHeaders(subHeaders);

		setFieldsToProvider(servicegroupId, orgId, workBook, showDiscount, onlyHospitalCharge);

		response.setHeader("Content-type", "application/vnd.ms-excel");
		response.setHeader("Content-disposition",
				"attachment; filename="+"\"Charges_"+serviceGroupName+"_"+orgName+".xls\"");
		response.setHeader("Readonly", "true");
		java.io.OutputStream os = response.getOutputStream();
		workBook.write(os);
		os.flush();
		os.close();

		return null;
	}

	private void setFieldsToProvider(int servicegroupId, String orgId,
			HSSFWorkbook workBook, boolean showdiscount, boolean onlyHospitalCharge)throws SQLException {

		List<BasicDynaBean> dynaList = null;
		RateplanSpreadsheetDAO dao = new RateplanSpreadsheetDAO();
		/*For services*/

		provider.setAnesthesiaCharges(Arrays.asList(new String[] {"dummyCharge"}));
		provider.setDoctorCharges(Arrays.asList(new String[] {"dummyCharge"}));
		provider.setHospitalCharges(Arrays.asList(new String[] {"unit_charge"}));
		provider.setOrgColumns(Arrays.asList("item_code"));
		provider.setUserDefinedColumns(Arrays.asList("Service"));

		provider.setOrgTable("service_org_details");
		provider.setBedColumn("bed_type");
		provider.setChargeColumnId("service_id");
		provider.setItemTable("services");
		provider.setItemTableId("service_id");
		provider.setOrgColumnName("org_id");
		provider.setServiceGroupId(servicegroupId);
		provider.setDiscountColName("discount");
		provider.setChargeTable("service_master_charges");
		provider.setStatusColumnName("status");

		dynaList = provider.generateQueryFOrSpreadsheet(Arrays.asList("service_code", "service_name"), orgId, servicegroupId);
		provider.insertListIntoSpreadSheet(workBook, "SERVICES", dynaList);

		/*For tests*/

		provider.setAnesthesiaCharges(Arrays.asList(new String[] {"dummyCharge"}));
		provider.setDoctorCharges(Arrays.asList(new String[] {"dummyCharge"}));
		provider.setHospitalCharges(Arrays.asList(new String[] {"charge"}));
		provider.setOrgColumns(Arrays.asList(""));
		provider.setUserDefinedColumns(Arrays.asList("Diagnostics"));

		provider.setOrgTable(null);
		provider.setBedColumn("bed_type");
		provider.setChargeColumnId("test_id");
		provider.setItemTable("diagnostics");
		provider.setItemTableId("test_id");
		provider.setOrgColumnName("org_name");
		provider.setServiceGroupId(servicegroupId);
		provider.setDiscountColName("discount");
		provider.setChargeTable("diagnostic_charges");
		provider.setStatusColumnName("status");

		dynaList = provider.generateQueryFOrSpreadsheet(Arrays.asList("diag_code", "test_name"), orgId, servicegroupId);
		provider.insertListIntoSpreadSheet(workBook, "TESTS", dynaList);

		/*For package*/

		provider.setAnesthesiaCharges(Arrays.asList(new String[] {"dummyCharge"}));
		provider.setDoctorCharges(Arrays.asList(new String[] {"dummyCharge"}));
		provider.setHospitalCharges(Arrays.asList(new String[] {"charge"}));
		provider.setOrgColumns(Arrays.asList("item_code"));
		provider.setUserDefinedColumns(Arrays.asList("Package"));

		provider.setOrgTable("pack_org_details");
		provider.setBedColumn("bed_type");
		provider.setChargeColumnId("package_id");
		provider.setItemTable("packages");
		provider.setItemTableId("package_id");
		provider.setOrgColumnName("org_id");
		provider.setServiceGroupId(servicegroupId);
		provider.setDiscountColName("discount");
		provider.setChargeTable("package_charges");
		provider.setStatusColumnName("package_active");

		dynaList = provider.generateQueryFOrSpreadsheet(Arrays.asList("package_code", "package_name"), orgId, servicegroupId);
		provider.insertListIntoSpreadSheet(workBook, "PACKAGE", dynaList);

		/*For consultation*/

		provider.setAnesthesiaCharges(Arrays.asList(new String[] {"dummyCharge"}));
		provider.setDoctorCharges(Arrays.asList(new String[] {"dummyCharge"}));
		provider.setHospitalCharges(Arrays.asList(new String[] {"charge"}));
		provider.setOrgColumns(Arrays.asList("item_code"));
		provider.setUserDefinedColumns(Arrays.asList("Consultation"));

		provider.setOrgTable("consultation_org_details");
		provider.setBedColumn("bed_type");
		provider.setChargeColumnId("consultation_type_id");
		provider.setItemTable("consultation_types");
		provider.setItemTableId("consultation_type_id");
		provider.setOrgColumnName("org_id");
		provider.setServiceGroupId(servicegroupId);
		provider.setDiscountColName("discount");
		provider.setChargeTable("consultation_charges");
		provider.setStatusColumnName("status");

		dynaList = provider.generateQueryFOrSpreadsheet(Arrays.asList("consultation_code", "consultation_type"), orgId, servicegroupId);
		provider.insertListIntoSpreadSheet(workBook, "CONSULTATION", dynaList);

		dynaList = dao.getListForOperations(orgId, showdiscount, onlyHospitalCharge, servicegroupId);
		provider.insertListIntoSpreadSheet(workBook, "OPERATION", dynaList);

		dynaList = dao.getListForEquipments(orgId, showdiscount, onlyHospitalCharge, servicegroupId);
		provider.insertListIntoSpreadSheet(workBook, "EQUIPMENT", dynaList);

		dynaList = dao.getListForCommoncharges(orgId, showdiscount, onlyHospitalCharge, servicegroupId);
		provider.insertListIntoSpreadSheet(workBook, "COMMON CHARGES",dynaList);

	}
	public StringBuilder errors;

	public ActionForward importChargesBasedOnServiceGroups(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException, SQLException {

		RateplanSpreadsheetForm rateplanSpreadsheetform = (RateplanSpreadsheetForm) form;
		HSSFWorkbook workBook = new HSSFWorkbook(rateplanSpreadsheetform.getXlsRateplanfile().getInputStream());
		String orgId = rateplanSpreadsheetform.getOrgId();

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedierect"));

		errors = new StringBuilder();
		provider.setChargeColumns(new String[] {"charge", "discount"});
		provider.setChargeTypeColumns(new String[]{"hospital", "doctor", "anaes", "total"});
		provider.setItemColumns(new String[]{"code", "item", "type"});

		provider.importCharges(workBook, orgId, errors, (String)request.getSession(false).getAttribute("userid"));

		if (errors.length() > 0)
			flash.put("error", errors);
		else
			flash.put("info", "File successfully uploaded");

		redirect.addParameter("org_id", orgId);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}