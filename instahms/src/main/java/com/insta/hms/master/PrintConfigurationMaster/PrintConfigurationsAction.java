package com.insta.hms.master.PrintConfigurationMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PrintConfigurationsAction extends DispatchAction{

	static Logger log = LoggerFactory.getLogger(PrintConfigurationsAction.class);
	PrintConfigurationsDAO dao = new PrintConfigurationsDAO("hosp_print_master");

	public ActionForward list(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)
		throws IOException,ServletException,Exception{

		Map listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
		PagedList pagedList = dao.getPrintMaster(listingParams);
		request.setAttribute("pagedList", pagedList);
		PrintConfigurationsDAO imageDao = new PrintConfigurationsDAO("hosp_print_master_files");
		HttpSession session = request.getSession();
		int centerId = (Integer)session.getAttribute("centerId");
		request.setAttribute("centerIdSel", centerId);
		int max_centers_inc_default = (Integer)GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
		List logoCenters = null;
		if(max_centers_inc_default <= 1) {
			logoCenters = imageDao.getCentersAndLogoSizes(0);
		}else {
			if(centerId == 0) {
		        logoCenters = imageDao.getCentersAndLogoSizes(-1);
		    }else {
		    	logoCenters = imageDao.getCentersAndLogoSizes(centerId);
		    }
		}
        request.setAttribute("centersAndLogoSizes", logoCenters);
        if(logoCenters != null) {
            request.setAttribute("centers_count", logoCenters.size());
        } else {
        	request.setAttribute("centers_count", 0);
        }
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("status", "A");
		request.setAttribute("centerList", new GenericDAO("hospital_center_master").listAll(null, filterMap, "center_name"));
		/*int size = imageDao.getFileSizes(centerId);
		request.setAttribute("fileSize", size);*/

		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)
	throws IOException,ServletException,Exception{

	return mapping.findForward("addshow");
	}

	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException,SQLException,Exception{

		Connection con = null;
		boolean success = false;
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			Map<String, Object> params = getLogoMap(request);
			List error = new ArrayList();
			BasicDynaBean paramBean = dao.getBean();
			ConversionUtils.copyToDynaBean(params, paramBean, error);

			int centerId = (Integer) request.getSession(false).getAttribute("centerId");
			Object key = ((Object[])params.get("print_type"))[0];
			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("print_type", key.toString());
			keys.put("center_id", centerId);

			if (error.isEmpty()) {
				if (centerId == 0) {
					success = (dao.update(con, paramBean.getMap(), keys) > 0);
				} else {
					// update or insert the center specific print configurations.
					BasicDynaBean centerbean = dao.getBean();
					centerbean.set("print_type", key.toString());
					centerbean.set("printer_id", Integer.parseInt(request.getParameter("center_printer_id")));
					centerbean.set("pre_final_watermark", request.getParameter("center_pre_final_watermark"));
					centerbean.set("duplicate_watermark", request.getParameter("center_duplicate_watermark"));
					centerbean.set("center_id", centerId);
					centerbean.set("header1", request.getParameter("center_header1"));
					centerbean.set("header2", request.getParameter("center_header2"));
					centerbean.set("header3", request.getParameter("center_header3"));
					centerbean.set("footer1", request.getParameter("center_footer1"));
					centerbean.set("footer2", request.getParameter("center_footer2"));
					centerbean.set("footer3", request.getParameter("center_footer3"));

					if (dao.getRecord(key.toString(), centerId) ==  null)
						success = dao.insert(con, centerbean);
					else
						success = (dao.update(con, centerbean.getMap(), keys) > 0);
				}
				if (success) {
					flash.put("success", "Print details updated successfully");
					redirect.addParameter(FlashScope.FLASH_KEY,flash.key());
					redirect.addParameter("print_type", request.getParameter("print_type"));
					return redirect;
				} else {
					request.setAttribute("error", "Failed to update print details");
				}

			} else {
				request.setAttribute("error","incorrectly formatted values supplied");
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		redirect.addParameter(FlashScope.FLASH_KEY,flash.key());
		redirect.addParameter("print_type", request.getParameter("print_type"));
		return redirect;
	}


	public ActionForward show(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException,SQLException,Exception{
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		BasicDynaBean centerbean = dao.getRecord(request.getParameter("print_type"), centerId);
		BasicDynaBean bean = dao.getRecord(request.getParameter("print_type"), 0);

		request.setAttribute("bean", bean);
		request.setAttribute("centerbean", centerId != 0 ? centerbean: null);
		return mapping.findForward("addshow");
	}

	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException,ServletException, Exception {
		int center_id = Integer.parseInt(request.getParameter("center_id"));
		boolean success = false;
		if(center_id != -1) {
		    success = PrintConfigurationsDAO.deleteLogo(center_id);
		}
		FlashScope flash = FlashScope.getScope(request);
		String msg = "";
		if(success = true){
			msg = "Logo deleted Successfully ";
		}else {
			msg = "Transaction failed Could not delete Logo ";
		}
		flash.put("success", msg);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}


	public Map<String ,Object> getLogoMap(HttpServletRequest request)
	 throws FileUploadException, IOException	{

		Map<String,Object> params = new HashMap<String, Object>();
		String contentType = null;
		if (request.getContentType().split("/")[0].equals("multipart")){

			DiskFileItemFactory factory = new DiskFileItemFactory();

			ServletFileUpload upload = new ServletFileUpload(factory);
			List<FileItem> items  = upload.parseRequest(request);
			Iterator it = items.iterator();
			while (it.hasNext()){
				FileItem item = (FileItem) it.next();
				if (item.isFormField()){
					String name = item.getFieldName();
					String value = item.getString();
					if(params.containsKey(name)) {
						Object[] objArr = (Object[])(params.get(name));
                        Object[] newObjArr = new Object[objArr.length + 1];
                        for(int i = 0; i<objArr.length; i++) {
                        	newObjArr[i] = objArr[i];
                        }
                        newObjArr[objArr.length] = value;
						params.put(name, newObjArr);
					} else {
					    params.put(name, new Object[]{value});
					}
				}else {
					String fieldName = item.getFieldName();
					String fileName = item.getName();
					contentType = item.getContentType();
					boolean isInMempry = item.isInMemory();
					long sizeInBytes = item.getSize();
					if (!fileName.equals("")){
						params.put(fieldName, new InputStream[]{item.getInputStream()});
						params.put("content_type", new Object[]{item.getInputStream()});
					}
				}

			}
		}else {
			params.putAll(request.getParameterMap());
		}
		return params;
	}

	public ActionForward updateLogo(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception {

		Connection con = null;
		boolean success = false;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			Map<String, Object> params = getLogoMap(request);
			PrintConfigurationsDAO logoDao = new PrintConfigurationsDAO("hosp_print_master_files");
			Object[] centers_countArr = (Object[])params.get("centers_count");
			Integer centers_count = 0;
			if(centers_countArr != null && centers_countArr.length > 0 && !centers_countArr[0].toString().equals("")) {
			    centers_count = Integer.parseInt(centers_countArr[0].toString());
			}
			for(int i = 0; i<centers_count; i++) {
				Object[] center = (Object[])params.get("center_id" + i);
				if(center != null && center.length > 0) {
				    if(!center[0].toString().equals("-1")) {
				    	Object[] logo = (Object[])params.get("logo" + i);
				    	if(logo != null && logo.length > 0) {
				    		//InputStream[] stream = new InputStream[]{(InputStream)logo[0]};
				    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
				    		byte[] buffer = new byte[1024];
				    		int len;
				    		while ((len = ((InputStream)logo[0]).read(buffer)) > -1 ) {
				    		    baos.write(buffer, 0, len);
				    		}
				    		baos.flush();
				    		InputStream is1 = new ByteArrayInputStream(baos.toByteArray()); 
				    		InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
				    		String contentType = (String) ConvertUtils.convert(MimeTypeDetector.getMimeTypes(is1));
				        	if (!contentType.split("/")[0].equals("image")) {
				        		String error = "Invalid File Type was selected..";
				        		FlashScope flash = FlashScope.getScope(request);
				        		flash.put("error", error);
				        		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
				        		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				        		return redirect;
				        	}
				    		int center_id = Integer.parseInt(center[0].toString());
				    		if(logoDao.exist("center_id", center_id)) {
				    		    Map keys = new HashMap(); 
				    		    keys.put("center_id", center_id);
				    		    Map columns = new HashMap();
				    		    columns.put("logo", is2);
							    success = (logoDao.update(con, columns,keys) >0);
				    		} else {
				    			BasicDynaBean bean = logoDao.getBean();
				    			bean.set("center_id", center_id);
				    			bean.set("logo", is2);
				    			success = logoDao.insert(con, bean);
				    		}
				    	}
				    }
				}
			}
		}finally{
			DataBaseUtil.commitClose(con, success);
		}
		if(success){
		    FlashScope flash = FlashScope.getScope(request);
			flash.put("success", "Print details updated successfully");
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY,flash.key());
			return redirect;
		}else{
			request.setAttribute("error", "Failed to update print details");
		}
		return mapping.findForward("list");
	}
}
