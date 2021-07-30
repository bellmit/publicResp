package com.insta.hms.master.FixedAssetMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.stores.StoresDBTablesUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class FixedAssetMasterAction extends BaseAction{

	static Logger logger = LoggerFactory.getLogger(FixedAssetMasterAction.class);
    private static final GenericDAO locationMasterDAO = new GenericDAO("location_master");
    private static final GenericDAO fixedAssetUploadsDAO = new GenericDAO("fixed_asset_uploads");

	public ActionForward list(ActionMapping m,ActionForm f,
			HttpServletRequest request,HttpServletResponse response) throws Exception{


		Map<Object,Object> map= getParameterMap(request);
		HttpSession session = request.getSession(false);
		int roleId = (Integer) session.getAttribute("roleId");
		Integer dept_id = null;
		map.remove("dept_id");
		String selectedDept = (String)request.getParameter("dept_id");
		if ((null != selectedDept) && (!(selectedDept.equals("")))){
				 dept_id = Integer.parseInt(selectedDept);
		} else{
			Map actMap = (Map)session.getAttribute("actionRightsMap");
			String storeAccess = (String) session.getAttribute("multiStoreAccess");
			String userDefaultDeptId = (String)session.getAttribute("pharmacyStoreId");
			selectedDept = (selectedDept == null || selectedDept.equals("")) ?
					(userDefaultDeptId == null || userDefaultDeptId.equals("")) ?
							(roleId == 1 || roleId == 2 || storeAccess.equals("A") ? "-99999" : null) : userDefaultDeptId : selectedDept;
			if (null != selectedDept){
				dept_id = Integer.parseInt(selectedDept);
			}
		}
		PagedList list = null;
		int centerId = (Integer)request.getSession().getAttribute("centerId");
		if (null != selectedDept){
			list = FixedAssetMasterDAO.getFixedAssetDetails(map, ConversionUtils.getListingParameter(map), Integer.parseInt(selectedDept),centerId);

		}
		request.setAttribute("pagedList", list);
		request.setAttribute("dept_id", dept_id);
		return m.findForward("list");
	}


	public ActionForward show(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		BasicDynaBean bean = FixedAssetMasterDAO.getAssetDetails(Integer.parseInt(req.getParameter("asset_seq")));
		req.setAttribute("bean", bean);
		int filecount = FixedAssetMasterDAO.getFileSizes(Integer.parseInt(req.getParameter("asset_seq")));
		req.setAttribute("filelen",filecount );
		if (filecount > 0) req.setAttribute("fileseq", FixedAssetMasterDAO.getFilecount(Integer.parseInt(req.getParameter("asset_seq"))));
		return m.findForward("addshow");
	}


	public ActionForward update(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException, Exception {

		Connection con = null;
		boolean flag = true;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			Map<Object,Object> params= getParameterMap(req);
			Object filename[] = (Object[])params.get("fileName");
			Object oldrnew[] = (Object[])params.get("record");
			Object file_upload[] = (Object[])params.get("file_upload");
			Object content_type[] = (Object[])params.get("content_type");
			int len = filename != null ? filename.length : 0;

			List errors = new ArrayList();

			FixedAssetMasterDAO dao = new FixedAssetMasterDAO();
			BasicDynaBean bean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, bean, errors);
			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("asset_seq", new Integer(((Object[])params.get("asset_seq"))[0].toString()));

			String assloc = ((Object[])params.get("asset_location"))[0].toString();
			String asspid = ((Object[])params.get("parent_asset_id"))[0].toString();

			if (!assloc.equals("")){
				BasicDynaBean locbean = locationMasterDAO.findByKey("location_name", assloc);
				if (locbean == null){
					int id = locationMasterDAO.getNextSequence();
					BasicDynaBean locationBean = locationMasterDAO.getBean();
					locationBean.set("location_id", id);
					locationBean.set("location_name", assloc);
					locationMasterDAO.insert(con, locationBean);
					bean.set("asset_location_id", id);
				} else {
					bean.set("asset_location_id", locbean.get("location_id"));
				}
		    }
			if (!asspid.equals("")){
				String itemId = StoresDBTablesUtil.itemNameToId(asspid);
				bean.set("parent_asset_id", Integer.parseInt(itemId));
			}
			for (int i=0;i<len;i++){
				if (!filename[i].toString().equals("")){
					BasicDynaBean uploadbean = fixedAssetUploadsDAO.getBean();
					//ConversionUtils.copyToDynaBean(params, uploadbean, errors);
					uploadbean.set("asset_file_name", filename[i]);
					uploadbean.set("asset_upload_file", (InputStream)file_upload[i]);

					uploadbean.set("content_type", content_type[i].toString());
					uploadbean.set("asset_seq", new Integer(((Object[])params.get("asset_seq"))[0].toString()));
					if (flag) flag = fixedAssetUploadsDAO.insert(con, uploadbean);
				}
			}
			if (errors.isEmpty()) {

					int success = dao.update(con, bean.getMap(), keys);
					if (success > 0 && flag) {
						con.commit();
						FlashScope flash = FlashScope.getScope(req);
						flash.success("Fixed Asset master details updated successfully.");
						ActionRedirect redirect = new ActionRedirect(m.findForward("listRedirect"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					} else {
						con.rollback();
						req.setAttribute("error", "Failed to update Fixed Asset master details.");
					}

			}
			else {
				req.setAttribute("error", "Incorrectly formatted values supplied.");
			}
		} finally {
			if (con != null) con.close();
		}

		return m.findForward("show");
	}

	@SuppressWarnings("unchecked")
	public ActionForward getUplodedDocs(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse resp)
	throws IOException, ServletException, Exception {
		String id = req.getParameter("asset_file_seq");
		String fileName = "";
		String contentType = "";
		if (id != null) {
			//fileName = FixedAssetMasterDAO.getUplodedFile(Integer.parseInt(id));
			Map<String,Object> uploadMap = FixedAssetMasterDAO.getUploadedData(Integer.parseInt(id));
			if (!(uploadMap.isEmpty())){
				fileName = (String)uploadMap.get("filename");
				contentType = (String)uploadMap.get("contenttype");
				if (!(fileName.equals(""))){
					resp.setHeader("Content-disposition", "attachment; filename=\""+fileName+"\"");
					resp.setContentType(contentType);
					OutputStream os = resp.getOutputStream();

					InputStream s = (InputStream)uploadMap.get("uploadfile");
					if (s != null) {
						byte[] bytes = new byte[4096];
						int len = 0;
						while ( (len = s.read(bytes)) > 0) {
							os.write(bytes, 0, len);
						}

						os.flush();
						s.close();
						return null;
					}

				} else{
					return m.findForward("error");
				}

			}
		}

		return m.findForward("error");

	}

	public ActionForward deleteUplodedDoc(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException,ServletException, SQLException {
		int id = Integer.parseInt(request.getParameter("asset_file_seq"));
		int assetid = Integer.parseInt(request.getParameter("asset_seq"));
		boolean flag = FixedAssetMasterDAO.DeleteUplodedForm(id);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("asset_seq",assetid);

		return redirect ;
	}

}