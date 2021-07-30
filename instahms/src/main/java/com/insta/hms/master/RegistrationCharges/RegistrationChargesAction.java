/**
 *
 */
package com.insta.hms.master.RegistrationCharges;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesAction;
import com.insta.hms.xls.exportimport.ChargesImportExporter;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna.t
 *
 */
public class RegistrationChargesAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(RegistrationPreferencesAction.class);
	RegistrationChargesDAO dao = new RegistrationChargesDAO();
	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception{

		JSONSerializer json = new JSONSerializer().exclude("class");
		String orgId = null;
		if ((request.getParameter("orgId") == null) || (request.getParameter("orgId").equals(""))) {
			orgId = "ORG0001";
		} else {
			orgId = request.getParameter("orgId");
		}
		List bedTypes = BedMasterDAO.getUnionOfBedTypes();
		List<BasicDynaBean> beans = dao.getRegistrationChargesBeans(orgId);

		List<BasicDynaBean> derivedRatePlanDetails = dao.getDerivedRatePlanDetails(orgId);

		if(derivedRatePlanDetails.size()<0)
			request.setAttribute("derivedRatePlanDetails", json.serialize(Collections.EMPTY_LIST));
        else
        	request.setAttribute("derivedRatePlanDetails", json.serialize(ConversionUtils.copyListDynaBeansToMap(derivedRatePlanDetails)));

		request.setAttribute("bedTypes", bedTypes);
		request.setAttribute("beans", beans);
		request.setAttribute("orgId", orgId);

		return mapping.findForward("show");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception{

		Connection con = null;
		boolean allSuccess = false;
		boolean success = true;
		String orgId = request.getParameter("orgId");
		List errors = new ArrayList();
		String[] beds = request.getParameterValues("beds");
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("orgId", orgId);
		String[] derivedRateplanIds = request.getParameterValues("ratePlanId");

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			BasicDynaBean bean = dao.getBean();
			for (int i=0; i<beds.length; i++) {
				Map keys = new HashMap<String, String>();
				keys.put("org_id", orgId);
				keys.put("bed_type", beds[i]);
				ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, bean, errors);
				success = dao.update(con, bean.getMap(), keys) > 0;
				if (!success) {
					flash.put("error", "Transaction failed while updation");
					return redirect;
				}
			}

			String[] ip_reg_charge = request.getParameterValues("ip_reg_charge");
			String[] op_reg_charge = request.getParameterValues("op_reg_charge");
			String[] gen_reg_charge = request.getParameterValues("gen_reg_charge");
			String[] reg_renewal_charge = request.getParameterValues("reg_renewal_charge");
			String[] mrcharge = request.getParameterValues("mrcharge");
			String[] ip_mlccharge = request.getParameterValues("ip_mlccharge");
			String[] op_mlccharge = request.getParameterValues("op_mlccharge");

			String[] ip_reg_charge_discount = request.getParameterValues("ip_reg_charge_discount");
			String[] op_reg_charge_discount = request.getParameterValues("op_reg_charge_discount");
			String[] gen_reg_charge_discount = request.getParameterValues("gen_reg_charge_discount");
			String[] reg_renewal_charge_discount = request.getParameterValues("reg_renewal_charge_discount");
			String[] mrcharge_discount = request.getParameterValues("mrcharge_discount");
			String[] ip_mlccharge_discount = request.getParameterValues("ip_mlccharge_discount");
			String[] op_mlccharge_discount = request.getParameterValues("op_mlccharge_discount");

			Double[] ipRegCharge = new Double[ip_reg_charge.length];
			Double[] opRegCharge = new Double[op_reg_charge.length];
			Double[] genRegCharge = new Double[gen_reg_charge.length];
			Double[] regRenewalCharge = new Double[reg_renewal_charge.length];
			Double[] mrCharge = new Double[mrcharge.length];
			Double[] ipMlcCharge = new Double[ip_mlccharge.length];
			Double[] opMlcCharge = new Double[op_mlccharge.length];

			Double[] ipRegDisc = new Double[ip_reg_charge_discount.length];
			Double[] opRegDisc = new Double[op_reg_charge_discount.length];
			Double[] genRegDisc = new Double[gen_reg_charge_discount.length];
			Double[] regRenewalDisc = new Double[reg_renewal_charge_discount.length];
			Double[] mrDisc = new Double[mrcharge_discount.length];
			Double[] ipMlcDisc = new Double[ip_mlccharge_discount.length];
			Double[] opMlcDisc = new Double[op_mlccharge_discount.length];

			for(int i=0; i<ip_reg_charge.length; i++) {
				ipRegCharge[i] = new Double(ip_reg_charge[i]);
				opRegCharge[i] = new Double(op_reg_charge[i]);
				regRenewalCharge[i] = new Double(reg_renewal_charge[i]);
				genRegCharge[i] = new Double(gen_reg_charge[i]);
				mrCharge[i] = new Double(mrcharge[i]);
				ipMlcCharge[i] = new Double(ip_mlccharge[i]);
				opMlcCharge[i] = new Double(op_mlccharge[i]);

				ipRegDisc[i] = new Double(ip_reg_charge_discount[i]);
				opRegDisc[i] = new Double(op_reg_charge_discount[i]);
				genRegDisc[i] = new Double(gen_reg_charge_discount[i]);
				regRenewalDisc[i] = new Double(reg_renewal_charge_discount[i]);
				mrDisc[i] = new Double(mrcharge_discount[i]);
				ipMlcDisc[i] = new Double(ip_mlccharge_discount[i]);
				opMlcDisc[i] = new Double(op_mlccharge_discount[i]);
			}

			if(null != derivedRateplanIds && derivedRateplanIds.length > 0) {
				allSuccess = dao.updateChargesForDerivedRatePlans(con,orgId,derivedRateplanIds,ipRegCharge,
						opRegCharge,regRenewalCharge,genRegCharge,mrCharge,ipMlcCharge,opMlcCharge,
						beds, ipRegDisc,opRegDisc,genRegDisc,regRenewalDisc,mrDisc,ipMlcDisc,opMlcDisc);
			}

			allSuccess = true;
			flash.put("success", "Updation done successfully");

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);

		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}

	public ActionForward exportRegChargesToXls(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException, SQLException {

		String orgId = request.getParameter("orgId");
		BasicDynaBean orgBean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
		String orgName = (String) orgBean.get("org_name");
		List<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();

		XSSFWorkbook workBook = new XSSFWorkbook();
		XSSFSheet workSheet = workBook.createSheet("REGISTRATION CHARGES");
		Map<String, List<String>> columnNamesMap = new HashMap<String, List<String>>();
		columnNamesMap.put("mainItems", null);
		columnNamesMap.put("bedTypes", BedMasterDAO.getUnionOfBedTypes());
		columnNamesMap.put("charges", Arrays.asList(new String[] {"Ip Visit Charge", "Ip Visit Discount", "Op Visit Charge", "Op Visit Discount",
				"Registration Charge", "Registration Discount", "Registration Renewal Charge", "Registration Renewal Discount", "Medical Record Charge",
				"Medical Record Discount", "Ip MLC Charge", "Ip MLC Discount", "Op MLC Charge", "Op MLC Discount"}));
		List<String> charges =  Arrays.asList(new String[] {"ip_reg_charge", "ip_reg_charge_discount", "op_reg_charge", "op_reg_charge_discount",
				"gen_reg_charge", "gen_reg_charge_discount", "reg_renewal_charge", "reg_renewal_charge_discount", "mrcharge",
				"mrcharge_discount", "ip_mlccharge", "ip_mlccharge_discount", "op_mlccharge", "op_mlccharge_discount"});

		List<BasicDynaBean> list = RegistrationChargesDAO.getRegChargesForOrganization(orgId, charges, bedTypes);
		HsSfWorkbookUtils.createPhysicalCellsWithValues(list, columnNamesMap, workSheet, false);

		response.setHeader("Content-type", "application/vnd.ms-excel");
		String fileName = "\"RegistrationCharges_"+orgName+".xls\"";
		response.setHeader("content-disposition", "attachment; filename="+fileName);
		response.setHeader("Readonly", "true");
		java.io.OutputStream outputstream = response.getOutputStream();
		workBook.write(outputstream);
		outputstream.flush();
		outputstream.close();

		return null;
	}

	public StringBuilder errors;

	private static ChargesImportExporter importExporter;
	static {

		importExporter = new ChargesImportExporter(null, null,
				"registration_charges", null, "", null, null,
				new String[] {}, new String[] {},
				new String[] {}, new String[] {},
				new String[] {"ip_reg_charge", "ip_reg_charge_discount", "op_reg_charge",
				"op_reg_charge_discount", "gen_reg_charge", "gen_reg_charge_discount", "reg_renewal_charge",
				"reg_renewal_charge_discount", "mrcharge", "mrcharge_discount", "ip_mlccharge",
				"ip_mlccharge_discount", "op_mlccharge", "op_mlccharge_discount"}

				, new String[] {"ip visit charge", "ip visit discount", "op visit charge", "op visit discount",
				"registration charge", "registration discount",	"registration renewal charge",
				"registration renewal discount", "medical record charge", "medical record discount", "ip mlc charge",
				"ip mlc discount", "op mlc charge", "op mlc discount"});

		importExporter.setOrgKey("");
		importExporter.setChargeKey("noKey");
		importExporter.setItemWhereFieldKeys(new String[] {""});
		importExporter.setOrgWhereFieldKeys(new String[] {""});
		importExporter.setChargeWhereFieldKeys(new String[] {"org_id"});
		importExporter.setMandatoryFields(new String[] {});
		importExporter.setItemName("");
		importExporter.setUserColumnName(null);
	}

	public ActionForward importRegistrationCharges(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws ServletException,
				IOException, SQLException,Exception {

		RegistrationChargesForm regChargeForm = (RegistrationChargesForm) form;
		regChargeForm.getXlsRegistrationFile().getFileData();
		String orgId = regChargeForm.getOrg_id();
		XSSFWorkbook workBook = new XSSFWorkbook(regChargeForm.getXlsRegistrationFile().getInputStream());
		XSSFSheet sheet = workBook.getSheetAt(0);
		this.errors = new StringBuilder();

		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("orgId", orgId);
		String userName = (String)request.getSession(false).getAttribute("userid");

		/*
		 * Keep a backup of the rates for safety: TODO: be able to checkpoint and revert
		 * to a previous version if required.
		 */
		RegistrationChargesDAO.backUpCharges(userName, orgId);
		importExporter.importCharges(true, orgId, sheet, userName, this.errors);

		dao.updateChargesForDerivedRatePlans(orgId, userName, "registration",true);

		if (this.errors.length() > 0)
			flash.put("error", this.errors);
		else
			flash.put("info", "File successfully uploaded");

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}
}
