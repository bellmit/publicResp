package com.insta.hms.master.Hl7Master;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class Hl7ConfigAction extends DispatchAction{

	static Logger logger = LoggerFactory.getLogger(Hl7ConfigAction.class);
	
    private static final GenericDAO hospitalCenterMasterDAO =
        new GenericDAO("hospital_center_master");
    private static final GenericDAO hl7CenterInterfacesDAO =
        new GenericDAO("hl7_center_interfaces");

	Hl7ConfigDAO dao = new Hl7ConfigDAO();
	JSONSerializer js = new JSONSerializer().exclude("class");

	@SuppressWarnings("rawtypes")
	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{

		
		Map map= request.getParameterMap();
		PagedList pagedList = dao.getInterfaceDetails(map,ConversionUtils.getListingParameter(request.getParameterMap()));
		request.setAttribute("pagedList", pagedList);
		return m.findForward("list");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ActionForward add(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		JSONSerializer js = new JSONSerializer().exclude("class");
		ArrayList<String>  interfacfes = (ArrayList)Hl7ConfigDAO.getAvalInterfaces();
		request.setAttribute("interfaceNameList", js.serialize(interfacfes));
		request.setAttribute("centersJson", js.serialize(ConversionUtils.copyListDynaBeansToMap(hospitalCenterMasterDAO.listAll())));		
		return m.findForward("addShow");
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	public ActionForward create(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		Map params = request.getParameterMap();
		List errors = new ArrayList();
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);

		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String error = null;
		boolean success = false;
		
		try {
			if (errors.isEmpty()) {
				boolean exists = dao.exist("interface_name", (String) bean.get("interface_name"));
				if (exists) {
					error = "Interface name already exists.....";
				} else {
					bean.set("hl7_lab_interface_id", dao.getNextSequence());
					success = dao.insert(con, bean);
					boolean status = insertOrUpdateHl7Center(con,request, bean);
					if (!success) {
						error = "Fail to set up HL7 interface";
					}
				}
			} else {
				error = "Incorrectly formatted values supplied..";
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(request);
		if (error != null) {
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			flash.error(error);

		}else {
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("hl7_lab_interface_id", bean.get("hl7_lab_interface_id"));
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;

	}
	
	@SuppressWarnings("rawtypes")
	public ActionForward update(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{
		
		
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map params = request.getParameterMap(); 
			
			List errors = new ArrayList();

			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			
			// Setting default vaule of check box , if it is null
			if(bean.get("equipment_code_required") == null){
				bean.set("equipment_code_required", "N");
			}
			if(bean.get("conducting_doctor_mandatory") == null){
				bean.set("conducting_doctor_mandatory", "N");
			}
			if(bean.get("append_doctor_signature") == null){
				bean.set("append_doctor_signature", "N");
			}
			if(bean.get("consolidate_multiple_obx") == null){
				bean.set("consolidate_multiple_obx", "N");
			}
			if(bean.get("rcv_supporting_doc") == null){
				bean.set("rcv_supporting_doc", "N");
			}			

			String hl7_lab_interface_id = request.getParameter("hl7_lab_interface_id");

			Map<String, Integer> keys = new HashMap<String, Integer>();
			keys.put("hl7_lab_interface_id", Integer.parseInt(hl7_lab_interface_id));
			FlashScope flash = FlashScope.getScope(request);

			if (errors.isEmpty()) {
				int success = dao.update(con, bean.getMap(), keys);
				if (success > 0) {
					insertOrUpdateHl7Center(con, request, bean);
					con.commit();
					flash.success("Interface details updated successfully..");
				} else {
					con.rollback();
					flash.error("Failed to update Interface details..");
				}
			} else {
				flash.error("Incorrectly formatted values supplied");
			}
			ActionRedirect redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("hl7_lab_interface_id", hl7_lab_interface_id);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}
	
	@SuppressWarnings("unused")
	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {
		String hl7_lab_interface_id_str = req.getParameter("hl7_lab_interface_id");
		BasicDynaBean bean = dao.findByKey("hl7_lab_interface_id", 
				(null != hl7_lab_interface_id_str && !hl7_lab_interface_id_str.equals("")) ? Integer.parseInt(hl7_lab_interface_id_str) : null);
		List<BasicDynaBean> centerInterface = dao.getInterfaceCenterDetails(req.getParameter("hl7_lab_interface_id"));
		
		req.setAttribute("bean", bean);
		req.setAttribute("centerInterfaces", centerInterface);
		req.setAttribute("centersJson", js.serialize(ConversionUtils.copyListDynaBeansToMap(hospitalCenterMasterDAO.listAll())));		
		return m.findForward("addShow");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean insertOrUpdateHl7Center(Connection con, HttpServletRequest request, BasicDynaBean bean) throws SQLException, IOException{
		boolean status = false;
		
		String[] inserted = request.getParameterValues("inserted");
		String[] edited = request.getParameterValues("edited");
		String[] deleted = request.getParameterValues("deleted");
		String[] hl7_center_interface_ids = request.getParameterValues("hl7_center_interface_id");
		List errors = new ArrayList();
		
		if(inserted != null) {
			BasicDynaBean hl7Centerbean =null;
			for(int i=0; i<inserted.length-1;i++) {
				hl7Centerbean = hl7CenterInterfacesDAO.getBean();
				ConversionUtils.copyIndexToDynaBean(request.getParameterMap(), i, hl7Centerbean, errors);
				hl7Centerbean.set("interface_name", request.getParameter("interface_name"));
				hl7Centerbean.set("hl7_lab_interface_id", bean.get("hl7_lab_interface_id"));
				if(errors.isEmpty()){
					if(inserted[i].equals("true")){
						hl7Centerbean.set("hl7_center_interface_id", hl7CenterInterfacesDAO.getNextSequence());
						status = (hl7CenterInterfacesDAO.insert(con, hl7Centerbean));
					}else if(deleted[i].equals("false") && edited[i].equals("true")){
						Map keys = new HashMap();
						keys.put("hl7_center_interface_id", hl7Centerbean.get("hl7_center_interface_id"));
						status = hl7CenterInterfacesDAO.update(con, hl7Centerbean.getMap(), keys)>0;
					}else if(deleted[i].equals("true")){
						status = (hl7CenterInterfacesDAO.delete(con, "hl7_center_interface_id", Integer.parseInt(hl7_center_interface_ids[i])));
					}
					
				}
					
			}

		}
		
		return status;
	}
}
