package com.insta.hms.master.sectionrolerights;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO;
import com.insta.hms.usermanager.Role;
import com.insta.hms.usermanager.RoleDAO;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SectionRoleRightsAction extends BaseAction{

	SectionRoleRightsDAO dao = new SectionRoleRightsDAO();
	SectionsDAO sdao = new SectionsDAO();
	SystemGeneratedSectionsDAO sysdao = new SystemGeneratedSectionsDAO();
	public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException {
		Integer section_id = Integer.parseInt(request.getParameter("section_id"));
		Connection con = null;
		List<Role> all_roles = null;
		try {
			con = DataBaseUtil.getConnection();
			RoleDAO rdao = new RoleDAO(con);
			all_roles = rdao.getAllRoles();
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		Map<String, Object> temp_filter = new HashMap<String, Object>();
		temp_filter.put("section_id", section_id);
		if(section_id > 0) {
			request.setAttribute("section", sdao.findByKey(temp_filter));
		} else {
			request.setAttribute("section", sysdao.findByKey(temp_filter));
		}
		request.setAttribute("available_roles", all_roles);
		request.setAttribute("selected_roles", dao.getallroles(section_id));
		return mapping.findForward("edit");
		
	}
	
	public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, SQLException, FileUploadException {
		Map<String, Object> params = getParameterMap(request);
		Integer sectionId = Integer.parseInt(getParameter(params, "section_id"));
		dao.save((String[]) getParameterValues(params, "selected_roles"), sectionId);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("redirect"));
		redirect.addParameter("section_id", sectionId);
		return redirect;
	}
}
