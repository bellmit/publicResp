package com.insta.hms.master.SampleType;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




public class SampleTypeAction extends DispatchAction {
  
  private static final GenericDAO hospitalCenterMasterDAO =
      new GenericDAO("hospital_center_master");
  
  private static final GenericDAO sampleTypeNumPrefDAO = new GenericDAO("sample_type_number_prefs");


	public ActionForward list(ActionMapping m, ActionForm af,HttpServletRequest req, HttpServletResponse res)
	throws SQLException, IOException, ParseException,Exception  {

		SampleTypeDAO dao = new SampleTypeDAO();
		Map requestParams = req.getParameterMap();
		PagedList pagedList = dao.search(requestParams, ConversionUtils.getListingParameter(requestParams));
    	req.setAttribute("pagedList", pagedList);
    	req.setAttribute("sampleNumPrefMap", getSampleNumberPrefsForSampleTypes());
		return m.findForward("list");
	}


	public ActionForward add(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		SampleTypeDAO dao = new SampleTypeDAO();
		JSONSerializer js = new JSONSerializer().exclude("class");
		List<String> columns = Arrays.asList(new String[] {"sample_type_number_prefs_id", "sample_prefix"});
		req.setAttribute("allSampleLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(sampleTypeNumPrefDAO.listAll(columns))));
		req.setAttribute("listOfCentersExcldDefault", dao.getListOfCentersExcludingDefault());
		req.setAttribute("defaultCenterRec", hospitalCenterMasterDAO.findByKey("center_id", 0));
		return m.findForward("addshow");
	}

	public ActionForward create(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		Map params = req.getParameterMap();
		List errors = new ArrayList();
		SampleTypeDAO dao = new SampleTypeDAO();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		FlashScope fScope = FlashScope.getScope(req);
		ActionRedirect redirect = null;
		if (errors.isEmpty()) {
			BasicDynaBean exists = dao.findByKey("sample_type", bean.get("sample_type"));

			if (exists == null) {
				bean.set("sample_type_id", (dao.getNextSequence()));
				boolean success = dao.insert(con, bean);
				success &= saveSampleNumberPrefs(con, params, bean.get("sample_type_id"));
				if (success) {
					con.commit();
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					fScope.success("Sample Type details inserted successfully..");
					redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
					redirect.addParameter("sample_type_id", bean.get("sample_type_id"));
					DataBaseUtil.closeConnections(con, null);
					return redirect;
				}
			} else {

				fScope.error("Sample Type Name already exists..");

			}

		} else {
			con.close();
			fScope.success("Incorrectly Formated values supplied..");
		}
		redirect = new ActionRedirect(m.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, fScope.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward show(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res)throws IOException, ServletException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		SampleTypeDAO dao = new SampleTypeDAO();
		List<String> columns = Arrays.asList(new String[] {"sample_type_number_prefs_id", "sample_prefix"});

		Integer sampleTypeID = Integer.parseInt(req.getParameter("sample_type_id"));
		BasicDynaBean bean = dao.findByKey("sample_type_id", sampleTypeID);
		req.setAttribute("bean", bean);
		req.setAttribute("samplesLists", js.serialize(dao.getSamplesNamesAndIds()));
		req.setAttribute("allSampleLists", js.serialize(ConversionUtils.copyListDynaBeansToMap(sampleTypeNumPrefDAO.listAll(columns))));
		req.setAttribute("listOfCentersExcldDefault", dao.getListOfCentersExcludingDefault());
		req.setAttribute("defaultCenterRec", hospitalCenterMasterDAO.findByKey("center_id", 0));
		req.setAttribute("centerWiseSampleNumPrefs",
				ConversionUtils.listBeanToMapBean(sampleTypeNumPrefDAO.findAllByKey("sample_type_id", sampleTypeID), "center_id") );

		return m.findForward("addshow");
	}

	public ActionForward update(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws IOException, ServletException, Exception {

		Map params = req.getParameterMap();
		Map requestParams = new HashMap();
		requestParams.putAll(params);
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		SampleTypeDAO dao = new SampleTypeDAO();
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(requestParams, bean, errors);
		Object key = req.getParameter("sample_type_id");
		Map<String, Integer> keys = new HashMap<String, Integer>();
		keys.put("sample_type_id", Integer.parseInt(key.toString()));
		String specimenName = req.getParameter("specimenName");
		FlashScope flash = FlashScope.getScope(req);
		ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
		BasicDynaBean exists = null;
		boolean success = true;

		if (errors.isEmpty()) {
			if(!(specimenName).equals(bean.get("sample_type"))){
				exists = dao.findByKey("sample_type", bean.get("sample_type"));
			}
			if (exists != null) {
				flash.error("Sample Type name already exists..");
			}else{
				success = dao.update(con, bean.getMap(), keys) > 0;
				success &=  saveSampleNumberPrefs(con, requestParams, key);

				if (success) {
					con.commit();
					flash.success("Sample Type details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Sample Type details..");
				}
			}
		} else {
			flash.error("Incorrectly formatted values supplied..");
		}

		redirect.addParameter("sample_type_id", bean.get("sample_type_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		DataBaseUtil.closeConnections(con, null);
		return redirect;
	}

	public ActionForward isSampletypeExistsWithTest(ActionMapping mapping, ActionForm form, HttpServletRequest request,
						HttpServletResponse response)throws IOException, ServletException, SQLException {
		String sampleTypeId = request.getParameter("sampleTypeId");
		int sampleId = (sampleTypeId != null && !sampleTypeId.equals("")) ? Integer.parseInt(sampleTypeId) : null;
		String status = "true";
		LinkedHashMap<String, Object> identifiers = new LinkedHashMap<String, Object>();
		identifiers.put("status", "A");
		identifiers.put("sample_type_id", sampleId);
		BasicDynaBean bean = new GenericDAO("diagnostics").findByKey(Collections.EMPTY_LIST, identifiers);
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		PrintWriter pw = response.getWriter();
		if (bean == null) {
			status = "false";
		}
		pw.write(status);
		pw.flush();
		pw.close();
		return null;
	}

	private boolean saveSampleNumberPrefs(Connection con, Map<String, Object[]> requestParams,
			Object sampleTypeID)throws SQLException, IOException {

		String[] sampleTypeNumPrefIDs = (String[])requestParams.get("sample_type_number_prefs_id");
		String[] samplePrefixs = (String[])requestParams.get("sample_prefix");
		String[] startNumbers = (String[])requestParams.get("start_number");
		Map<String, Object> paramsForUpdation = new HashMap<String, Object>();
		paramsForUpdation.putAll(requestParams);
		//no need to update start number
		paramsForUpdation.remove("start_number");
		BasicDynaBean bean = null;
		boolean success = true;


		for (int i=0; i<sampleTypeNumPrefIDs.length; i++) {
			if (sampleTypeNumPrefIDs[i] == null || sampleTypeNumPrefIDs[i].equals("")
					&& (samplePrefixs[i] != null && !samplePrefixs[i].equals("") && startNumbers[i] != null &&
							!startNumbers[i].equals(""))) {
				bean = sampleTypeNumPrefDAO.getBean();
				ConversionUtils.copyIndexToDynaBean(requestParams, i, bean);
				bean.set("sample_type_id", Integer.parseInt(sampleTypeID.toString()));

				success &= sampleTypeNumPrefDAO.insert(con, bean);

			} else if (sampleTypeNumPrefIDs[i] != null && !sampleTypeNumPrefIDs[i].equals("")) {
				bean = sampleTypeNumPrefDAO.getBean();
				ConversionUtils.copyIndexToDynaBean(paramsForUpdation, i, bean);

				success &= sampleTypeNumPrefDAO.updateWithName(con, bean.getMap(), "sample_type_number_prefs_id") > 0;

			}
		}
		return success;
	}

	private Map getSampleNumberPrefsForSampleTypes()throws SQLException {

		List<Map> list = ConversionUtils.copyListDynaBeansToMap(sampleTypeNumPrefDAO.listAll());
		HashMap<String, List<Map>> map = new HashMap<String, List<Map>>();
		for (int i=0; i<list.size(); i++ ) {
			Map subMap = list.get(i);
			String sampleTypeID = subMap.get("sample_type_id").toString();

			if (map.get(sampleTypeID) == null) {
				ArrayList<Map> subList = new ArrayList<Map>();
				subList.add(subMap);
				map.put(sampleTypeID, subList);
			} else {
				map.get(sampleTypeID).add(subMap);
			}

		}

		Map returnMap = new HashMap();

		Set set = map.entrySet();
		Iterator iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			entry.getKey();
			returnMap.put(entry.getKey().toString(), ConversionUtils.listMapToMapListMap((List)entry.getValue(), "center_id"));
		}

		return map;

	}

}
