/**
 *
 */
package com.insta.hms.master.GenericImageMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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



/**
 * @author krishna.t
 *
 */
public class GenericImageMasterAction extends DispatchAction {

	GenericImageDAO genericimagedao = new GenericImageDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, Exception {
		Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
		request.setAttribute("pagedList", genericimagedao.searchGenericImages(listingParams));
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, Exception {
		//String image_id = request.getParameter("image_id");

		return mapping.findForward("addshow");
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, FileUploadException,
			SQLException {

		String error = "";
		Map<String, Object> params = null;

		//	Create a factory for disk-based file items
		params = new HashMap<String, Object>();
		DiskFileItemFactory factory = new DiskFileItemFactory();


		ServletFileUpload upload = new ServletFileUpload(factory);

		//		Parse the request
		List /* FileItem */ items = upload.parseRequest(request);

		//		Process the uploaded items
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();

		    if (item.isFormField()) {
		    	String name = item.getFieldName();
		        String value = item.getString("UTF-8");
		        params.put(name, new Object[]{value});

		    } else {
		    	String fieldName = item.getFieldName();
		        String fileName = item.getName();
		        boolean isInMemory = item.isInMemory();
		        long sizeInBytes = item.getSize();
		        if (!fileName.equals("")) {
		        	params.put(fieldName, new InputStream[]{item.getInputStream()});
		        	String contentType = (String) ConvertUtils.convert(MimeTypeDetector.getMimeTypes(item.getInputStream()));
		        	if (!contentType.split("/")[0].equals("image")) {
		        		error = "Invalid File Type was selected..";
		        		FlashScope flash = FlashScope.getScope(request);
		        		flash.put("error", error);
		        		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		        		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		        		return redirect;
		        	} else if (sizeInBytes > 10*1024*1024) {
		        		error = "Unable to upload the file: file size greater than 10 MB";
		        		FlashScope flash = FlashScope.getScope(request);
		        		flash.put("error", error);
		        		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		        		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		        		return redirect;

		        	}
		        	params.put("content_type", new Object[]{contentType});
		        }
		    }
		}
		List errorFields = new ArrayList();
		BasicDynaBean bean = genericimagedao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errorFields);

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			if (errorFields.isEmpty()) {
				boolean exists = genericimagedao.exist("image_name", bean.get("image_name"));
				if (!exists) {
					int image_id = genericimagedao.getNextSequence();
					bean.set("image_id", image_id);
 					boolean success = genericimagedao.insert(con, bean);
					if (success) {
						con.commit();
						FlashScope flash = FlashScope.getScope(request);
						flash.put("success", "Image inserted successfully..");
						ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					} else {
						con.rollback();
						error = "Failed to add  Image..";
					}
				} else {
					error = "Image name already exists";
				}
			}else {
				error = "Incorrectly formatted values supplied";
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

		FlashScope flash = FlashScope.getScope(request);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, SQLException {

		String image_id = request.getParameter("image_id");
		BasicDynaBean bean = genericimagedao.getBean();
		boolean loaded = genericimagedao.loadByteaRecords(bean, "image_id", Integer.parseInt(image_id));

		response.setContentType(bean.get("content_type").toString());
		byte[] bytes = DataBaseUtil.readInputStream((java.io.InputStream)bean.get("image_content"));
		OutputStream stream = response.getOutputStream();
		stream.write(bytes);
		stream.flush();
		stream.close();

		return null;
	}

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, Exception {
		String[] image_ids = request.getParameterValues("delete_image");
		Connection con = null;
		String msg = null;
		String error = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			con.setAutoCommit(false);
			if (image_ids != null) {
				boolean success = true;
				for (String image_id : image_ids) {
					if (genericimagedao.delete(con, "image_id", Integer.parseInt(image_id))) {

					} else {
						success = false;
						break;
					}
				}
				if (success) {
					con.commit();
					msg = ((image_ids.length > 1)?"Images":"Image") + " deleted successfully..";
				} else {
					con.rollback();
					error = "Failed to delete " + ((image_ids.length > 1)?"Images":"Image..");
				}
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		FlashScope flash = FlashScope.getScope(request);
		flash.put("error", error);
		flash.put("success", msg);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}
