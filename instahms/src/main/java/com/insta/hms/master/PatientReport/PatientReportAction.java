package com.insta.hms.master.PatientReport;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Patient Reports are of two kinds: Fixed Field forms and Editable Template. This
 * is stored in the following tables:
 *
 * Fixed Field forms:
 *  form_header (form_id, form_caption, status, form_title, form_type)
 *  fields (field_id, form_id, caption, displayorder, status, field_type, no_of_lines, default_text)
 *
 * Editable Templates:
 *  discharge_format (format_id, template_caption, status, template_title, template_type, report_file)
 *
 */

public class PatientReportAction extends BaseAction {

    static Logger log = LoggerFactory.getLogger(PatientReportAction.class);

    PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();
	static PatientReportCommonDAO cDao = new PatientReportCommonDAO();
	static TemplateReportDAO tDao = new TemplateReportDAO();
	static FormHeaderDAO fDao = new FormHeaderDAO();
	static GenericDAO ffDao = new GenericDAO("fields");
	GenericDocumentTemplateDAO pdftemplateDao = new GenericDocumentTemplateDAO("doc_pdf_form_templates");

	/*
	 * List objects available
	 */
	public ActionForward list(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(req.getParameterMap());

		PagedList pagedList = cDao.list(req.getParameterMap(), listingParams);
		req.setAttribute("pagedList", pagedList);

		return m.findForward("list");
	}

	/*
	 * Get the screen for adding a new report template
	 */
	public ActionForward add(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		req.setAttribute("phTemplates", phTemplateDao.getTemplates("D", "A"));
		// nothing special to do: just return the screen
		return m.findForward("addshow");
	}

	/*
	 * Show an existing object for viewing/editing
	 */
	public ActionForward show(ActionMapping m, ActionForm f, HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		String format = req.getParameter("format");
		if (format == null) {
			log.warn("Show: no format supplied: assuming F");
			format = "F";
		}

		String id = req.getParameter("id");

		if (id == null || id.equals(""))
			return m.findForward("addshow");

		if (format.equals("F")) {
			BasicDynaBean report = fDao.findByKey("form_id", id);

			List<BasicDynaBean> fields = ffDao.listAll(null, "form_id", id);

			req.setAttribute("report", report.getMap());
			req.setAttribute("fields", ConversionUtils.copyListDynaBeansToMap(fields));

		} else if (format.equals("T")) {
			/* rich text template format */
			BasicDynaBean report = tDao.findByKey("format_id", id);
			req.setAttribute("report", report.getMap());
			req.setAttribute("phTemplates", phTemplateDao.getTemplates("D", "A"));
		} else if (format.equals("P")) {
			/* editable pdf form template */
			BasicDynaBean report = pdftemplateDao.getBean();
			pdftemplateDao.loadByteaRecords(report, "template_id", Integer.parseInt(id));
			req.setAttribute("report", report.getMap());
		}

		return m.findForward("addshow");
	}


	/*
	 * POST action: create a new report template
	 */
	public ActionForward create(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		FlashScope flash = FlashScope.getScope(req);
		Map<String, Object[]> params = getParameterMap(req);
		List errors = new ArrayList();
		String formId = null;

		Object objFormat = ((Object[])params.get("format"))[0];
		String format = null;
		if (objFormat == null || ((String)objFormat).equals("")) {
			log.warn("Update: no format supplied: assuming F");
			format = "P";
		} else {
			format = (String) objFormat;
		}

		ActionRedirect redirect = null;
		BasicDynaBean report = null;
		List<BasicDynaBean> fields = null;
		String name = null;
		String error = "Unknown error";
		String userName = (String) req.getSession(false).getAttribute("userid");

		if (format.equals("F")) {

			report = fDao.getBean();
			ConversionUtils.copyToDynaBean(params, report, errors);
			name = (String) report.get("form_caption");
			if (name != null) {
				report.set("form_caption", name.trim());
			}

			// get a list of fields from the screen
			fields = new ArrayList();
			Object[] deleted = (Object[]) params.get("deleted");
			log.debug("Number of items: " + deleted.length);
			for (int i=0; i<deleted.length; i++) {
				String sDeleted = (String) deleted[i];
				if (!sDeleted.equals("Y")) {
					BasicDynaBean b = ffDao.getBean();
					ConversionUtils.copyIndexToDynaBean(params, i, b, errors);
					if (b.get("caption") != null)
						fields.add(b);
				}
			}

		} else if (format.equals("T")) {
			report = tDao.getBean();
			ConversionUtils.copyToDynaBean(params, report, errors);
			name = (String) report.get("template_caption");
			if (name != null) {
				report.set("template_caption", name.trim());
			}
		} else if (format.equals("P")) {
			report = pdftemplateDao.getBean();
			//Map map = getParameterMap(req);

			if (params.get("fileSizeError") != null) {
				error = ((Object[])params.get("fileSizeError"))[0].toString();
				flash.put("error", error);
				redirect = new ActionRedirect(m.findForward("addRedirect"));
				redirect.addParameter("format", format);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			String contentType = ((Object[])params.get("content_type"))[params.get("odt_file") == null ? 0 : 1].toString();
			if (!contentType.equals("application/pdf")) {
				error = "File type not supported: upload only pdf's";
				flash.put("error", error);
				redirect = new ActionRedirect(m.findForward("addRedirect"));
				redirect.addParameter("format", format);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

			if (params.get("template_content") != null && params.get("odt_file") == null) {

				flash.put("error", "Please upload the odt file.");
				redirect = new ActionRedirect(m.findForward("addRedirect"));
				redirect.addParameter("format", format);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

			if (params.get("odt_file") != null &&
					!params.get("content_type")[0].equals("application/vnd.oasis.opendocument.text")) {
				flash.put("error", "File type not suppored. Please upload the odt file.");
				redirect = new ActionRedirect(m.findForward("addRedirect"));
				redirect.addParameter("format", format);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}


			ConversionUtils.copyToDynaBean(params, report, errors);
			if (report.get("template_name") != null) {
				name = (String) report.get("template_name");
				report.set("template_name", name.trim());
			}
		}

		if (!errors.isEmpty()) {
			log.error("Incorrectly formatted values supplied " + errors);
			flash.put("error", "Incorrectly formatted values supplied");
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter("format", format);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		Connection con = null;
		boolean success = false;


		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);



			if (format.equals("F")) {

				if (fDao.exist("form_caption", (String) report.get("form_caption"))) {
					flash.put("error", "Duplicate Report Name: " + name + " already exists");
					redirect = new ActionRedirect(m.findForward("addRedirect"));
					redirect.addParameter("format", format);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
				formId = fDao.getNextPrefixedId("frm");

				report.set("form_id", formId);
				success = fDao.insert(con, report);
				if (success) {
					for (DynaBean b : fields) {
						b.set("field_id", ffDao.getNextPrefixedId("fld"));
						b.set("form_id", formId);
					}
					success = ffDao.insertAll(con, fields);
				}
			} else if (format.equals("T")) {
				if (tDao.exist("template_caption", (String) report.get("template_caption"))) {
					flash.put("error", "Duplicate Report Name: " + name + " already exists");
					redirect = new ActionRedirect(m.findForward("addRedirect"));
					redirect.addParameter("format", format);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
				formId = tDao.getNextId();
				report.set("format_id", formId);
				success = tDao.insert(con, report);
			} else  {
				if ( pdftemplateDao.exist(true, "SYS_DS", null, null,(String) report.get("template_name")) ) {
					flash.put("error", "Duplicate Report Name: " + name + " already exists");
					redirect = new ActionRedirect(m.findForward("addRedirect"));
					redirect.addParameter("format", format);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
				int templateId = pdftemplateDao.getNextSequence();
				formId = new Integer(templateId).toString();

				report.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
				report.set("user_name", userName);
				report.set("template_id", templateId);
				report.set("specialized", true);
				report.set("doc_type", "SYS_DS");
				success = pdftemplateDao.insert(con, report);
			}

		} catch (SQLException e) {
			success = false;
			if (DataBaseUtil.isDuplicateViolation(e))
				error = "Duplicate Report Name: " + name + " already exists";
			else
				throw (e);
		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if (success) {
			flash.put("success", "Report Template created successfully.");
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("id", formId);
			redirect.addParameter("format", format);
		} else {
			flash.put("error", error);
			redirect = new ActionRedirect(m.findForward("addRedirect"));
			redirect.addParameter("format", format);
		}

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}


	/*
	 * POST action: update a report
	 */
	public ActionForward update(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse resp)
		throws IOException, ServletException, Exception {

		FlashScope flash = FlashScope.getScope(req);
		Map<String, Object[]> params = getParameterMap(req);
		List errors = new ArrayList();
		String error = "Unknown Error";
		String userName = (String) req.getSession(false).getAttribute("userid");

		Object objFormat = ((Object[])params.get("format"))[0];
		String format = null;
		if (objFormat == null || ((String)objFormat).equals("")) {
			log.warn("Update: no format supplied: assuming F");
			format = "P";
		} else {
			format = (String) objFormat;
		}

		ActionRedirect redirect = null;
		String name = null;
		String id = null;
		BasicDynaBean report = null;
		List<BasicDynaBean> addedFields = null;
		List<BasicDynaBean> modifiedFields = null;
		List<BasicDynaBean> deletedFields = null;

		if (format.equals("F")) {
			report = fDao.getBean();
			ConversionUtils.copyToDynaBean(params, report, errors);
			id = (String) report.get("form_id");
			name = (String) report.get("form_caption");
			if (name != null) {
				report.set("form_caption", name.trim());
			}

			addedFields = new ArrayList();
			modifiedFields = new ArrayList();
			deletedFields = new ArrayList();

			Object[] deleted = (Object[]) params.get("deleted");
			Object[] fieldId = (Object[]) params.get("field_id");
			//log.debug("Number of items: " + deleted.length);

			if (deleted != null) {
				for (int i=0; i<deleted.length; i++) {
					String sDeleted = (String) deleted[i];
					String sFieldId =  (String) fieldId[i];
					if (sDeleted.equals("Y") && sFieldId.equals("_")) {
						// do nothing: new field deleted.
						log.debug("Unchanged: " + i);
					} else {
						BasicDynaBean b = ffDao.getBean();
						ConversionUtils.copyIndexToDynaBean(params, i, b, errors);
						if (sFieldId.equals("_")) {
							addedFields.add(b);
							log.debug("item: " + i + " is added");
						} else if (sDeleted.equals("Y")) {
							deletedFields.add(b);
							log.debug("item: " + i + " is deleted");
						} else {
							modifiedFields.add(b);
							log.debug("item: " + i + " is modified");
						}
					}
				}
			}

		} else if (format.equals("T")) {
			report = tDao.getBean();
			ConversionUtils.copyToDynaBean(params, report, errors);
			id = (String) report.get("format_id");
			name = (String) report.get("template_caption");
			if (name != null) {
				report.set("template_caption", name.trim());
			}
		} else {
			report = pdftemplateDao.getBean();
			ConversionUtils.copyToDynaBean(params, report, errors);
			id = (Integer)report.get("template_id") + "";

			if (params.get("fileSizeError") != null) {
				error = ((Object[])params.get("fileSizeError"))[0].toString();
				flash.put("error", error);
				redirect = new ActionRedirect(m.findForward("showRedirect"));
				redirect.addParameter("format", format);
				redirect.addParameter("id", id);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			if (params.get("template_content") != null) {
				String contentType = ((Object[])params.get("content_type"))[params.get("odt_file") == null ? 0 : 1].toString();
				if (!contentType.equals("application/pdf")) {
					error = "File type not supported: upload only pdf's";
					flash.put("error", error);
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					redirect.addParameter("format", format);
					redirect.addParameter("id", id);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
			}
			if (params.get("template_content") != null && params.get("odt_file") == null) {

				flash.put("error", "Please upload the odt file.");
				redirect = new ActionRedirect(m.findForward("showRedirect"));
				redirect.addParameter("format", format);
				redirect.addParameter("id", id);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

			if (params.get("odt_file") != null &&
					!params.get("content_type")[0].equals("application/vnd.oasis.opendocument.text")) {
				flash.put("error", "File type not suppored. Please upload the odt file.");
				redirect = new ActionRedirect(m.findForward("showRedirect"));
				redirect.addParameter("format", format);
				redirect.addParameter("id", id);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			name = (String) report.get("template_name");
			if (name != null) {
				report.set("template_name", name.trim());
			}
		}

		if (!errors.isEmpty()) {
			flash.put("error", "Incorrectly formatted values supplied");
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("id", id);
			redirect.addParameter("format", format);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		Connection con = null;
		boolean success = false;


		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (format.equals("F")) {
outer:
				do {
					if (fDao.exist("form_caption", report.get("form_caption"), "form_id", id)) {
						flash.put("error", "Duplicate Report Name: " + name + " already exists");
						redirect = new ActionRedirect(m.findForward("showRedirect"));
						redirect.addParameter("format", format);
						redirect.addParameter("id", id);
						redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						return redirect;
					}
					int updates = fDao.update(con, report.getMap(), "form_id", id);
					if (updates < 1) break outer;

					for (BasicDynaBean b : modifiedFields) {
						updates = ffDao.update(con, b.getMap(), "field_id", (String) b.get("field_id"));
						if (updates != 1) {
							log.error("Update failed for: " + b.get("field_id") + ", rows updated=" + updates);
							break outer;
						}
					}

					for (BasicDynaBean b : deletedFields) {
						log.debug("Deleting: " + b.get("field_id"));
						boolean tempSuccess = ffDao.delete(con, "field_id", b.get("field_id"));
						if (!tempSuccess) {
							log.error("Could not delete!: " + b.get("field_id"));
							break outer;
						}
					}

					for (BasicDynaBean b : addedFields) {
						b.set("field_id", ffDao.getNextPrefixedId("fld"));
						b.set("form_id", id);
						boolean tempSuccess = ffDao.insert(con, b);
						if (!tempSuccess) {
							log.error("Could not insert!: " + b.get("field_id"));
							break outer;
						}
					}

					success = true;

				} while (false);

			} else if (format.equals("T")) {
				if (tDao.exist("template_caption", report.get("template_caption"), "format_id", id)) {
					flash.put("error", "Duplicate Report Name: " + name + " already exists");
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					redirect.addParameter("format", format);
					redirect.addParameter("id", id);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
				int updates = tDao.update(con, report.getMap(), "format_id", id);
				success = (updates > 0);
			} else {
				if (pdftemplateDao.exist(true, "SYS_DS",null, Integer.parseInt(id),
						(String) report.get("template_name"))
					) {
					flash.put("error", "Duplicate Report Name: " + name + " already exists");
					redirect = new ActionRedirect(m.findForward("showRedirect"));
					redirect.addParameter("format", format);
					redirect.addParameter("id", id);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					return redirect;
				}
				//report.set("doc_type", "SYS_DS");
				report.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
				report.set("user_name", userName);
				int updates = pdftemplateDao.update(con, report.getMap(), "template_id", Integer.parseInt(id));
				success = (updates > 0);
			}

		} catch (SQLException e) {
			success = false;
			if (DataBaseUtil.isDuplicateViolation(e)) {
				error = "Duplicate Report Name: " + name;
			} else {
				throw (e);
			}

		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		if (success) {
			flash.put("success", "Report Template updated successfully.");
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("id", id);
			redirect.addParameter("format", format);
		} else {
			flash.put("error", error);
			redirect = new ActionRedirect(m.findForward("showRedirect"));
			redirect.addParameter("id", id);
			redirect.addParameter("format", format);
		}

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}