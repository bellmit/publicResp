package com.insta.hms.master.InsuranceCategoryMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.InsCatCenter.InsCatCenterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InsuranceCategoryMasterAction extends DispatchAction{
		public ActionForward list(ActionMapping m, ActionForm f,
				HttpServletRequest req, HttpServletResponse resp)
				throws IOException, ServletException, Exception {
			JSONSerializer js = new JSONSerializer().exclude("class");
			InsuranceCategoryMasterDAO dao = new InsuranceCategoryMasterDAO();
			InsuCompMasterDAO icdao = new InsuCompMasterDAO();
			PagedList pagedList = InsuranceCategoryMasterDAO.getRecords(req.getParameterMap(), ConversionUtils.getListingParameter(req.getParameterMap()));
			req.setAttribute("insuranceCompaniesLists", js.serialize(icdao.getInsuranceCompaniesNamesAndIds()));
			req.setAttribute("categoryLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(dao.getInsuranceCategoryAutoCmpltList())));
			req.setAttribute("pagedList", pagedList);
			return m.findForward("list");
		}

		public ActionForward add(ActionMapping m, ActionForm f,
				HttpServletRequest req, HttpServletResponse resp)
				throws IOException, ServletException, Exception {
				JSONSerializer js = new JSONSerializer().exclude("class");
				InsuranceCategoryMasterDAO dao = new InsuranceCategoryMasterDAO();
				InsuCompMasterDAO icdao = new InsuCompMasterDAO();
				req.setAttribute("insuranceCompaniesLists", js.serialize(icdao.getInsuranceCompaniesNamesAndIds()));
				req.setAttribute("categoryLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(dao.getInsuranceCategoryAutoCmpltList())));
				return m.findForward("addshow");
		}

		public ActionForward create(ActionMapping m, ActionForm f,
				HttpServletRequest req, HttpServletResponse resp)
				throws IOException, ServletException, Exception {

			Map params = req.getParameterMap();
			List errors = new ArrayList();
			Connection con = null;

			InsuranceCategoryMasterDAO dao = new InsuranceCategoryMasterDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			FlashScope flash = FlashScope.getScope(req);
			ActionRedirect redirect = null;

			try {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				if (errors.isEmpty()) {
					Map filterMap = new HashMap();
					filterMap.put("category_name", bean.get("category_name"));
					filterMap.put("insurance_co_id", bean.get("insurance_co_id"));
					List searchList = dao.listAll(null, filterMap, null);
					BasicDynaBean exists = searchList==null||searchList.isEmpty()? null: (BasicDynaBean)searchList.get(0);
					if (exists == null) {
						bean.set("category_id",dao.getNextSequence());
						boolean success = dao.insert(con, bean);

						InsCatCenterDAO inscatcenterdao = new InsCatCenterDAO();
						if (success) {
							BasicDynaBean cbean = inscatcenterdao.getBean();
							cbean.set("inscat_center_id", inscatcenterdao.getNextSequence());
							cbean.set("category_id", bean.get("category_id"));
							cbean.set("center_id", 0);
							cbean.set("status", "A");
							success &= inscatcenterdao.insert(con, cbean);
						}

						if (success) {
							con.commit();
							redirect = new ActionRedirect(m.findForward("showRedirect"));
							redirect.addParameter("category_id", bean.get("category_id"));
							flash.success("Insurance Company details inserted successfully..");
							redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
							return redirect;
						} else {
							con.rollback();
							flash.error("Failed to add Insurance Plan Type Details..");
						}
					} else {
						flash.error("Insurance Plan Type name already exists..");
					}
				} else {
					flash.error("Incorrectly formatted values supplied");
				}
				DataBaseUtil.closeConnections(con, null);
				redirect = new ActionRedirect(m.findForward("addRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				redirect.addParameter("category_id", bean.get("category_id"));
			}finally {
				DataBaseUtil.closeConnections(con, null);
			}
			return redirect;
		}

		public ActionForward show(ActionMapping m, ActionForm f,
				HttpServletRequest req, HttpServletResponse resp)
				throws IOException, ServletException, Exception {

			JSONSerializer js = new JSONSerializer().exclude("class");
			InsuranceCategoryMasterDAO dao = new InsuranceCategoryMasterDAO();
			if(req.getParameter("category_id")!= null && !req.getParameter("category_id").equals("")) {
				BasicDynaBean bean = dao.getCategoryDetails(Integer.parseInt(req.getParameter("category_id")));
				req.setAttribute("bean", bean);
			} else {

			}
			req.setAttribute("insuranceCompaniesLists", js.serialize(dao.getInsuranceCompaniesNamesAndIds()));
			req.setAttribute("categoryLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(dao.getInsuranceCategoryAutoCmpltList())));

			return m.findForward("addshow");
		}

		public ActionForward update(ActionMapping m, ActionForm f,
				HttpServletRequest req, HttpServletResponse resp)
				throws IOException, ServletException, Exception {

			Connection con = null;
			Map params = req.getParameterMap();
			List errors = new ArrayList();

			InsuranceCategoryMasterDAO dao = new InsuranceCategoryMasterDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			FlashScope flash = FlashScope.getScope(req);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			redirect.addParameter("category_id",req.getParameter("category_id"));
			try {
				con =  DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				Object key = req.getParameter("category_id");
				Map<String,Object> keys = new HashMap<String,Object>();
				keys.put("category_id", Integer.parseInt(key.toString()));

				Map filterMap = new HashMap();
				filterMap.put("category_name", bean.get("category_name"));
				filterMap.put("insurance_co_id", bean.get("insurance_co_id"));
				List<BasicDynaBean> searchList = dao.listAll(null, filterMap, null);
				BasicDynaBean exists= null;
				BasicDynaBean origBean = dao.findByKey("category_id", Integer.parseInt(req.getParameter("category_id")));
				if(!origBean.get("category_name").equals(bean.get("category_name")) && searchList!=null && !searchList.isEmpty()) {
					for(BasicDynaBean b: searchList) {
						if(Integer.parseInt(req.getParameter("category_id")) != ((Integer)b.get("category_id"))) {
							if(bean.get("category_name").equals(b.get("category_name"))){
								exists = b;
							}
						}
					}
				}

				if (exists == null) {
					if (errors.isEmpty()) {

						if (bean.get("status") != null && ((String)bean.get("status")).equals("I")) {
							List<BasicDynaBean> planDependants = getPlanDependants(key);
							if (planDependants != null && planDependants.size() > 0) {
								flash.error("Cannot mark Insurance Plan Type as as inactive. <br/>" +
										" One (or) more Insurance Plans are linked with this Insurance Plan Type.");
								return redirect;
							}
						}
						int success = dao.update(con, bean.getMap(), keys);
						if (success > 0) {
							con.commit();
							flash.success("Insurance Plan Type details updated successfully..");
						} else {
							con.rollback();
							flash.error("Failed to update Insurance Plan Type details..");
						}
					}
					else {
						flash.error("Incorrectly formatted values supplied");
					}
				} else {
					flash.error("This Plan Type name already exists for the insurance company..");
				}
			}finally {
				DataBaseUtil.closeConnections(con, null);
			}
			return redirect;
		}

		public static final String GET_PLAN_DEPENDANTS =
			" SELECT * FROM insurance_plan_main WHERE category_id::text = ? AND status = 'A' ";

		private List<BasicDynaBean> getPlanDependants(Object planType) throws SQLException {
			return DataBaseUtil.queryToDynaList(GET_PLAN_DEPENDANTS, new Object[]{planType});
		}
}