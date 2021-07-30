/**
 *
 */
package com.insta.hms.master.ImageMarkers;

import com.bob.hms.common.DataBaseUtil;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class ViewImageMarkerAction extends DispatchAction {

	ImageMarkerDAO dao = new ImageMarkerDAO();
	public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		int imageId = Integer.parseInt(request.getParameter("image_id"));
		BasicDynaBean bean = dao.getBean();
		dao.loadByteaRecords(bean, "image_id", imageId);

		response.setContentType((String) bean.get("content_type"));
		OutputStream os = response.getOutputStream();
		os.write(DataBaseUtil.readInputStream((InputStream) bean.get("file_content")));
		os.flush();
		os.close();

		return null;
	}
}
