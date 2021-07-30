/**
 *
 */
package com.insta.hms.master.PerDiemCodes;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.ServiceGroup.ServiceGroupDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;

import flexjson.JSONSerializer;

/**
 * @author lakshmi
 *
 */
public class PerDiemCodesAction extends BaseAction {

	static Logger logger = LoggerFactory.getLogger(PerDiemCodesAction.class);

	PerDiemCodesDAO dao = new PerDiemCodesDAO();
	PerDiemCodesChargesDAO cdao = new PerDiemCodesChargesDAO();
	ServiceGroupDAO sergrpdao = new ServiceGroupDAO();
	JSONSerializer js = new JSONSerializer().exclude("class");
	
    private static final GenericDAO itemGroupTypeDAO = new GenericDAO("item_group_type");
    private static final GenericDAO itemGroupsDAO = new GenericDAO("item_groups");

	@SuppressWarnings("unchecked")
	public ActionForward list (ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {

		Map requestParams = getParameterMap(req);
		String orgId = req.getParameter("org_id");
		if ( (orgId == null) || orgId.equals("")) {
			orgId = "ORG0001";
		}

		requestParams.remove("org_id");
		PagedList list = dao.search(requestParams, ConversionUtils.getListingParameter(req.getParameterMap()));
		req.setAttribute("pagedList", list);

		List<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("org_id", orgId);

		List<BasicDynaBean> chargeList = cdao.getAllPerDiemChargesForOrganisation(orgId);
		req.setAttribute("charges", ConversionUtils.listBeanToMapMapBean(chargeList, "per_diem_code", "bed_type"));

		return m.findForward("list");
	}

	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {
		List<BasicDynaBean> perdiemCodesList = PerDiemCodesDAO.getMrdCodesPerDiemCodes();
		req.setAttribute("perdiemCodesList", ConversionUtils.listBeanToListMap(perdiemCodesList));
		req.setAttribute("perdiemCodesListJSON", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(perdiemCodesList)));
		List<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDAO.findAllByKey("item_group_type_id","TAX")));
		req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDAO.findAllByKey("status","A"))));
		//req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_sub_groups").findAllByKey("status","A"))));
		List <BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository().getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
		Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
		List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateStr = sdf.format(new java.util.Date());
		while(itemSubGroupListIterator.hasNext()) {
			BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
			if(itenSubGroupbean.get("validity_end") != null){
				Date endDate = (Date)itenSubGroupbean.get("validity_end");
				
				try {
					if(sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
						validateItemSubGrouList.add(itenSubGroupbean);
					}
				} catch (ParseException e) {
					continue;
				}
			} else {
				validateItemSubGrouList.add(itenSubGroupbean);
			}
		}
		req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException {

		HttpSession session = req.getSession();
    	String userName = (String)session.getAttribute("userid");
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect=new ActionRedirect(mapping.findForward("addRedirect"));

		ArrayList errors = new ArrayList();

		BasicDynaBean perdiemBean = dao.getBean();
		BasicDynaBean chargeDetails = cdao.getBean();
		perdiemBean.set("username", userName);
		perdiemBean.set("mod_time", DateUtil.getCurrentTimestamp());

		ConversionUtils.copyToDynaBean(req.getParameterMap(), perdiemBean, errors, true);

		BasicDynaBean exists = dao.findExistsByKey("per_diem_code", perdiemBean.get("per_diem_code"));
		if(exists != null) {
			flash.error( "Duplicate Perdiem Code: "+ perdiemBean.get("per_diem_code"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		String perDiemCode = (String)perdiemBean.get("per_diem_code");
		String[] subGrps = req.getParameterValues("service_groups_incl");
		StringBuilder sbgrps = new StringBuilder();
		StringBuilder sbgrpNames = new StringBuilder();
		if (subGrps != null && subGrps.length > 0) {
			for (int i = 0; i < subGrps.length; i++) {
				sbgrps.append(subGrps[i]);
				sbgrpNames.append((sergrpdao.findByKey("service_group_id",  new Integer(subGrps[i]))).get("service_group_name"));
				if (i+1 != subGrps.length) {
					sbgrps.append(",");
					sbgrpNames.append(",");
				}
			}
		}

		perdiemBean.set("service_groups_incl", sbgrps.toString());
		perdiemBean.set("service_groups_names", sbgrpNames.toString());

		ConversionUtils.copyToDynaBean(req.getParameterMap(), chargeDetails, errors);
		chargeDetails.set("per_diem_code", perDiemCode);

		String[] beds = req.getParameterValues("bed_type");

		List<BasicDynaBean> chargeList = new ArrayList<BasicDynaBean>();
		for (int i=0; i<beds.length; i++) {
			BasicDynaBean charge = cdao.getBean();
			charge.set("username", userName);
			ConversionUtils.copyToDynaBean(req.getParameterMap(), charge, errors);
			ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, charge, errors);
			charge.set("per_diem_code", perDiemCode);
			chargeList.add(charge);
		}

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			// 1. Insert the Per diem code
			boolean success = dao.insert(con, perdiemBean);
			if (!success) {
				flash.put("error", "Per diem code already exists");
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			if(success) {
				String perdiemCode = (String) perdiemBean.get("per_diem_code");
				success = saveOrUpdateItemSubGroup(perdiemCode,con,req);
			}

			// 2. Insert the charge for all ORG0001 and all bed types
			for (BasicDynaBean c: chargeList) {
				cdao.insert(con, c);
			}

			// 3. Copy the stuff for all orgs and inactive beds
			cdao.copyGeneralChargesToAllOrgs(con, perDiemCode);

			allSuccess = true;
			flash.put("success", "Per diem code inserted successfully");
			redirect=new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("per_diem_code", perDiemCode);
			redirect.addParameter("org_id", req.getParameter("org_id"));

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward show(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException, Exception {
		
		String orgId = req.getParameter("org_id");
		List<String> bedTypes = BedMasterDAO.getUnionOfBedTypes();

		String perDiemCode = req.getParameter("per_diem_code");
		BasicDynaBean bean = dao.findByKey("per_diem_code", perDiemCode);
		req.setAttribute("bean", bean);
		String inclServiceGrps = (String)bean.get("service_groups_incl");
		String[] inclSerGrps = inclServiceGrps.split(",");
		req.setAttribute("inclSerGrps", inclSerGrps);
		List<BasicDynaBean> chargeList = cdao.getAllChargesForBedTypesAndOrg(orgId, perDiemCode);
		req.setAttribute("charges", ConversionUtils.listBeanToMapMapBean(chargeList, "per_diem_code", "bed_type"));
		req.setAttribute("bedTypes", bedTypes);
		req.setAttribute("org_id", orgId);
		req.setAttribute("taxsubgroup", ConversionUtils.copyListDynaBeansToMap(dao.getPerdiemSubGroupDetails(perDiemCode)));
		req.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(itemGroupTypeDAO.findAllByKey("item_group_type_id","TAX")));
		req.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(itemGroupsDAO.findAllByKey("status","A"))));
		//req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_sub_groups").findAllByKey("status","A"))));
		List <BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository().getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
		Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
		List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateStr = sdf.format(new java.util.Date());
		while(itemSubGroupListIterator.hasNext()) {
			BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
			if(itenSubGroupbean.get("validity_end") != null){
				Date endDate = (Date)itenSubGroupbean.get("validity_end");
				
				try {
					if(sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
						validateItemSubGrouList.add(itenSubGroupbean);
					}
				} catch (ParseException e) {
					continue;
				}
			} else {
				validateItemSubGrouList.add(itenSubGroupbean);
			}
		}
		req.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));
		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException {

		HttpSession session = req.getSession();
    	String userName = (String)session.getAttribute("userid");
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect= new ActionRedirect(mapping.findForward("showRedirect"));

		ArrayList errors = new ArrayList();

		BasicDynaBean perdiemBean = dao.getBean();
		BasicDynaBean chargeDetails = cdao.getBean();
		perdiemBean.set("username", userName);
		perdiemBean.set("mod_time", DateUtil.getCurrentTimestamp());

		ConversionUtils.copyToDynaBean(req.getParameterMap(), perdiemBean, errors, true);
		String perDiemCode = req.getParameter("per_diem_code");

		String[] subGrps = req.getParameterValues("service_groups_incl");
		StringBuilder sbgrps = new StringBuilder();
		StringBuilder sbgrpNames = new StringBuilder();
		if (subGrps != null && subGrps.length > 0) {
			for (int i = 0; i < subGrps.length; i++) {
				sbgrps.append(subGrps[i]);
				sbgrpNames.append((sergrpdao.findByKey("service_group_id", new Integer(subGrps[i]))).get("service_group_name"));
				if (i+1 != subGrps.length) {
					sbgrps.append(",");
					sbgrpNames.append(",");
				}
			}
		}

		perdiemBean.set("service_groups_incl", sbgrps.toString());
		perdiemBean.set("service_groups_names", sbgrpNames.toString());

		ConversionUtils.copyToDynaBean(req.getParameterMap(), chargeDetails, errors);
		String[] beds = req.getParameterValues("bed_type");

		List<BasicDynaBean> chargeList = new ArrayList<BasicDynaBean>();
		for (int i=0; i<beds.length; i++) {
			BasicDynaBean charge = cdao.getBean();
			ConversionUtils.copyToDynaBean(req.getParameterMap(), charge, errors);
			ConversionUtils.copyIndexToDynaBean(req.getParameterMap(), i, charge, errors);
			charge.set("username", userName);
			chargeList.add(charge);
		}

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
		}

		Connection con = null;
		boolean allSuccess = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			// 1. Update the Per diem code.
			boolean success = (1 == dao.update(con, perdiemBean.getMap(), "per_diem_code", perDiemCode));
			if (!success) {
				flash.put("error", "Per diem code already exists");
			}
			// update the tax sub group
			if(success) {
				String perdiemCode = (String) perdiemBean.get("per_diem_code");
				success = saveOrUpdateItemSubGroup(perdiemCode,con,req);
			}

			// 2. Update the charge for all ORG0001 and all bed types
			for (BasicDynaBean c: chargeList) {
				cdao.updateWithNames(con, c.getMap(), new String[] {"per_diem_code", "org_id", "bed_type"});
			}

			allSuccess = true;
			flash.put("success", "Per diem code updated successfully");

		} finally {
			DataBaseUtil.commitClose(con, allSuccess);
		}
		redirect.addParameter("per_diem_code", perDiemCode);
		redirect.addParameter("org_id", req.getParameter("org_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward groupUpdate(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {


		return null;
	}
	
	private boolean saveOrUpdateItemSubGroup(String perdiemCode, Connection con,HttpServletRequest request)
			throws SQLException, IOException{
			Map params = request.getParameterMap();
			List errors = new ArrayList();
			
			boolean flag = true;
			String [] itemSubgroupId = request.getParameterValues("item_subgroup_id");
			String [] delete = request.getParameterValues("deleted");
			
			if (errors.isEmpty()) {
				if(itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")){
					GenericDAO itemsubgroupdao = new GenericDAO("perdiem_code_item_sub_groups");
					BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
					ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
					List records = itemsubgroupdao.findAllByKey("per_diem_code", perdiemCode);
					if (records.size() > 0)
						flag = itemsubgroupdao.delete(con, "per_diem_code", perdiemCode);
					
					for(int i=0; i<itemSubgroupId.length; i++){
						if(itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
							if (delete[i].equalsIgnoreCase("false")) {
								itemsubgroupbean.set("per_diem_code", perdiemCode);
								itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
								flag = itemsubgroupdao.insert(con, itemsubgroupbean);
							}
						}
					}
				}	
			}
			return flag;

		}

}
