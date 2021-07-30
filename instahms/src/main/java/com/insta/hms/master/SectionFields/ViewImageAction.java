/**
 *
 */
package com.insta.hms.master.SectionFields;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class ViewImageAction extends DispatchAction {

	SectionFieldsDAO fieldsDao = new SectionFieldsDAO();
	public ActionForward viewImage(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		GenericDAO imageDAO = new GenericDAO("patient_section_images");
		String imageIdStr = request.getParameter("image_id");
		imageIdStr = imageIdStr == null || imageIdStr.equals("") ? "0" : imageIdStr;
		if (Integer.parseInt(imageIdStr) == 0) {
			int fieldId = Integer.parseInt(request.getParameter("field_id"));
			
			Map imgDetails = fieldsDao.getImageDetails(fieldId);
			if (imgDetails != null) {
				response.setContentType(imgDetails.get("content_type").toString());
				byte[] bytes = DataBaseUtil.readInputStream((java.io.InputStream) imgDetails.get("file_content"));
				OutputStream stream = response.getOutputStream();
				stream.write(bytes);
				stream.flush();
				stream.close();
			}
		} else {
			int imageId = Integer.parseInt(imageIdStr);
			BasicDynaBean bean = imageDAO.getBean();
			imageDAO.loadByteaRecords(bean, "image_id", imageId);
			response.setContentType(bean.get("content_type").toString());
			byte[] bytes = DataBaseUtil.readInputStream((java.io.InputStream) bean.get("file_content"));
			OutputStream stream = response.getOutputStream();
			stream.write(bytes);
			stream.flush();
			stream.close();
		}
		return null;
	}
}
