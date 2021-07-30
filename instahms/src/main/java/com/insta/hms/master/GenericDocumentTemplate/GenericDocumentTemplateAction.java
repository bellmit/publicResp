/**
 *
 */
package com.insta.hms.master.GenericDocumentTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.DocumentTemplateCenterApplicability.CenterDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna.t
 *
 */
public class GenericDocumentTemplateAction extends BaseAction {

	PatientHeaderTemplateDAO phTemplateDao = new PatientHeaderTemplateDAO();
	
	private static final GenericDAO docTypeDao = new GenericDAO("doc_type");
    private static final GenericDAO docPdfTemplateExtFieldsDAO = new GenericDAO("doc_pdf_template_ext_fields");

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, Exception {

		Boolean specialized = new Boolean(mapping.getProperty("specialized"));
		String specializedDocType = mapping.getProperty("documentType");
		String allowedTemplates = mapping.getProperty("allowed_templates");
		if (allowedTemplates == null) {
			allowedTemplates = "doc_hvf_templates,doc_rich_templates,doc_pdf_form_templates,doc_rtf_templates";
		}

		Map reqParams = new HashMap(getParameterMap(request));
		if (specialized) {
			String doc_type_id = "";
			if ("mlc".equalsIgnoreCase(specializedDocType))
				doc_type_id = "4";
			else if ("service".equalsIgnoreCase(specializedDocType))
				doc_type_id = "SYS_ST";
			else if ("reg".equalsIgnoreCase(specializedDocType))
				doc_type_id = "SYS_RG";
			else if ("insurance".equalsIgnoreCase(specializedDocType))
				doc_type_id = "SYS_INS";
			else if ("dietary".equalsIgnoreCase(specializedDocType))
				doc_type_id = "SYS_DIE";
			else if ("tpapreauth".equalsIgnoreCase(specializedDocType))
				doc_type_id = "SYS_TPA";
			else if ("op_case_form_template".equalsIgnoreCase(specializedDocType))
				doc_type_id = "SYS_OP";
			else if ("consultation_form".equalsIgnoreCase(specializedDocType))
				doc_type_id = "SYS_CONSULT";
			else if ("ot".equalsIgnoreCase(specializedDocType))
				doc_type_id = "SYS_OT";

			reqParams.put("doc_type_id", new String[]{doc_type_id});
		}
		Map<LISTING, Object> listing = ConversionUtils.getListingParameter(reqParams);
		PagedList list = GenericDocumentTemplateDAO.getGenericDocTemplates(reqParams, specialized, listing);
		request.setAttribute("pagedList", list);

		request.setAttribute("documentType", specializedDocType);
		request.setAttribute("specialized", specialized);
		request.setAttribute("allowedTemplates", allowedTemplates);
		request.setAttribute("displayDept", new Boolean(mapping.getProperty("displayDept")));
		request.setAttribute("displayDoctor", new Boolean(mapping.getProperty("displayDoctor")));
		request.setAttribute("displayAccessRights", new Boolean(mapping.getProperty("displayAccessRights")));

		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse respose) throws IOException, ServletException, Exception {
		Object objFormat = request.getAttribute("format");
		if (objFormat == null) {
			objFormat = request.getParameter("format");
		}
		String format = objFormat.toString();

		request.setAttribute("doctypelist", docTypeDao.list());

		String documentType = mapping.getProperty("documentType");
		Boolean specialized = new Boolean(mapping.getProperty("specialized"));
		String pHeaderTemplateType = PatientHeaderTemplate.Documents.getType();
		if (specialized && documentType.equals("service"))
			pHeaderTemplateType = PatientHeaderTemplate.Ser.getType();
		request.setAttribute("documentType", documentType);
		request.setAttribute("specialized", specialized);
		request.setAttribute("phTemplates", phTemplateDao.getTemplates(pHeaderTemplateType, "A"));
		request.setAttribute("displayDept", new Boolean(mapping.getProperty("displayDept")));
		request.setAttribute("displayDoctor", new Boolean(mapping.getProperty("displayDoctor")));
		request.setAttribute("displayAccessRights", new Boolean(mapping.getProperty("displayAccessRights")));

		return mapping.findForward(getForward(format));
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, Exception{

		String format = request.getParameter("format");
		int template_id = Integer.parseInt(request.getParameter("template_id"));

		GenericDocumentTemplateDAO dao = new GenericDocumentTemplateDAO(format);
		request.setAttribute("doctypelist", docTypeDao.list());

		if (format.equals("doc_hvf_templates")) {
			request.setAttribute("template_details", dao.findByKey("template_id", template_id));
			GenericDocumentTemplateDAO hvfFieldsDao = new GenericDocumentTemplateDAO("doc_hvf_template_fields");
			request.setAttribute("hvf_template_fields", hvfFieldsDao.listAll(null, "template_id", template_id, "display_order"));

		} else if (format.equals("doc_rich_templates")) {
			request.setAttribute("template_details", dao.findByKey("template_id", template_id));
		} else {

			if (format.equals("doc_pdf_form_templates")) {
				request.setAttribute("pdf_template_ext_fields",
						ConversionUtils.listBeanToListMap(docPdfTemplateExtFieldsDAO.listAll(null, "template_id", template_id, "field_name")));
			}

			BasicDynaBean bean = dao.getBean();
			dao.loadByteaRecords(bean, "template_id", template_id);
			request.setAttribute("template_details", bean);
		}

		String documentType = mapping.getProperty("documentType");
		Boolean specialized = new Boolean(mapping.getProperty("specialized"));
		String pHeaderTemplateType = PatientHeaderTemplate.Documents.getType();
		if (specialized && documentType.equals("service"))
			pHeaderTemplateType = PatientHeaderTemplate.Ser.getType();
		request.setAttribute("documentType", documentType);
		request.setAttribute("specialized", specialized);
		request.setAttribute("phTemplates", phTemplateDao.getTemplates(pHeaderTemplateType, "A"));
		request.setAttribute("displayDept", new Boolean(mapping.getProperty("displayDept")));
		request.setAttribute("displayDoctor", new Boolean(mapping.getProperty("displayDoctor")));
		request.setAttribute("displayAccessRights", new Boolean(mapping.getProperty("displayAccessRights")));

		return mapping.findForward(getForward(format));
	}

	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, Exception{

		String format = null;
		Connection con = null;
		String error = "";
		FlashScope flash = FlashScope.getScope(request);
		try {
			Map<String, Object[]> params = getParameterMap(request);

			List errors = new ArrayList();


			if (params.get("fileSizeError") != null) {
				// when file upload size exception thrown we cant get the request parameters(like format which
				//is required to forward it to addRedirect page)
				// hence forwarding to the list screen without parameters.
				error = getParameter(params, "fileSizeError");
				flash.put("error", error);
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}
			format = ((Object[])params.get("format"))[0].toString();
			if (params.get("odt_file") != null &&
					!params.get("content_type")[0].equals("application/vnd.oasis.opendocument.text")) {
				flash.put("error", "File type not suppored. Please upload the odt file.");
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
				redirect.addParameter("format", format);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				return redirect;
			}

			GenericDocumentTemplateDAO dao = new GenericDocumentTemplateDAO(format);
			BasicDynaBean bean = dao.getBean();

			ConversionUtils.copyToDynaBean(params, bean, errors);
			if (bean.get("template_name") != null) {
				bean.set("template_name", ((String) bean.get("template_name")).trim() );
			}

			String userName = (String) request.getSession(false).getAttribute("userid");
			if (format.equals("doc_pdf_form_templates")) {
				bean.set("user_name", userName);
				bean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
			}

			Object[] autoGenIP = (Object[]) params.get("auto_gen_ip");
			Object[] autoGenOP = (Object[]) params.get("auto_gen_op");

			if ( (format.equals("doc_pdf_form_templates") || format.equals("doc_rich_templates" )) ) {
				if (autoGenIP == null )
					bean.set("auto_gen_ip", "N");
				if (autoGenOP == null)
					bean.set("auto_gen_op", "N");
			}

			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			GenericDocumentTemplateDAO hvfFieldsDao = null;

			if (errors.isEmpty()) {
				boolean exists = dao.exist((Boolean) bean.get("specialized"), (String) bean.get("doc_type"), (String) bean.get("dept_name"),
										null, (String) bean.get("template_name"));
				if (!exists) {
					int template_id = dao.getNextSequence();
					bean.set("template_id", template_id);
					boolean success = dao.insert(con, bean);
					if (success) {
						if (format.equals("doc_hvf_templates")) {

							Object[] field_names = (Object[])params.get("field_name");
							List<BasicDynaBean> hvf_template_rows = null;
							if (field_names != null && field_names[0] != null) {
								hvf_template_rows = new ArrayList<BasicDynaBean>();
								int index = 0;
								hvfFieldsDao = new GenericDocumentTemplateDAO("doc_hvf_template_fields");
								for(Object field : field_names) {
									BasicDynaBean fieldsBean = hvfFieldsDao.getBean();
									ConversionUtils.copyIndexToDynaBean(params, index++, fieldsBean, errors);
									int fieldId = hvfFieldsDao.getNextSequence();
									fieldsBean.set("field_id", fieldId);
									fieldsBean.set("template_id", template_id);
									hvf_template_rows.add(fieldsBean);
								}
								success = hvfFieldsDao.insertAll(con, hvf_template_rows);
							}
							//fieldsBean.set("field_id", hvfFieldsDao.getNextSequence());
						}


						if (format.equals("doc_pdf_form_templates")) {

							String[] fieldIdArr = (String[])params.get("field_id");
							String[] fieldNameArr = (String[])params.get("field_name");
							String[] displayNameArr = (String[])params.get("display_name");
							String[] fieldInputArr = (String[])params.get("field_input");

							if (fieldIdArr != null &&  fieldIdArr.length > 0) {

								for (int i = 0; i < fieldIdArr.length; i++) {
									if (fieldIdArr[i] != null && fieldIdArr[i].startsWith("_")
											&& fieldInputArr[i] != null && !fieldInputArr[i].equals("")) {
										int fieldId = docPdfTemplateExtFieldsDAO.getNextSequence();
										BasicDynaBean extFieldBean = docPdfTemplateExtFieldsDAO.getBean();
										extFieldBean.set("template_id", template_id);
										extFieldBean.set("field_id", fieldId);
										extFieldBean.set("field_name", fieldNameArr[i]);
										extFieldBean.set("display_name", displayNameArr[i]);
										extFieldBean.set("field_input", fieldInputArr[i]);

										docPdfTemplateExtFieldsDAO.insert(con, extFieldBean);
									}
								}
							}
						}
					}
					if (success) {
						CenterDAO docTemplCenterDAO = new CenterDAO();
						BasicDynaBean beanTemp = docTemplCenterDAO.getBean();
						String type = "";
						if(format.equals("doc_hvf_templates"))
							type = "H";
						else if(format.equals("doc_rich_templates"))
							type = "R";
						else if(format.equals("doc_pdf_form_templates"))
							type = "P";
						else if(format.equals("doc_rtf_templates"))
							type = "T";
						beanTemp.set("template_id", template_id);
						beanTemp.set("center_id", 0);
						beanTemp.set("doc_template_type", type);
						beanTemp.set("status", "A");
						success &= docTemplCenterDAO.insert(con, beanTemp);
						if(success) {
						    con.commit();
						    flash.put("success", "Template details inserted successfully..");
						    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
						    redirect.addParameter("format", format);
						    redirect.addParameter("template_id", new Integer(template_id));
						    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						    return redirect;
						} else {
							con.rollback();
							error = "Failed to add  Template default center applicability..";							
						}
					} else {
						con.rollback();
						error = "Failed to add  Template..";
					}
				} else {
					error = "Template name already exists";
				}
			} else {
				error = "Incorrectly formatted values supplied";
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("addRedirect"));
		redirect.addParameter("format", format);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, Exception {

		Map<String, Object[]> params = getParameterMap(request);
		String error = null;
		FlashScope flash = FlashScope.getScope(request);

		if (params.get("fileSizeError") != null) {
			error = getParameter(params, "fileSizeError");
			flash.put("error", error);
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		String format = getParameter(params, "format");
		String key = getParameter(params, "template_id");

		if (format.equals("doc_pdf_form_templates") &&
				params.get("template_content") != null && params.get("odt_file") == null) {
			flash.put("error", "Please upload the odt file.");
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("format", format);
			redirect.addParameter("template_id", key);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		if (params.get("odt_file") != null &&
				!params.get("content_type")[0].equals("application/vnd.oasis.opendocument.text")) {
			flash.put("error", "File type not suppored. Please upload the odt file.");
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
			redirect.addParameter("format", format);
			redirect.addParameter("template_id", key);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		Map<String, Object> keys = new HashMap<String, Object>();
		keys.put("template_id", Integer.parseInt(key));

		List errors = new ArrayList();
		int index = 0;

		GenericDocumentTemplateDAO dao = new GenericDocumentTemplateDAO(format);
		BasicDynaBean bean = dao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);

		String userName = (String) request.getSession(false).getAttribute("userid");
		if (format.equals("doc_pdf_form_templates")) {
			bean.set("user_name", userName);
			bean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
			bean.set("html_template", null);
		}

		if (bean.get("template_name") != null) {
			bean.set("template_name", ((String) bean.get("template_name")).trim() );
		}

		Object[] autoGenIP = (Object[]) params.get("auto_gen_ip");
		Object[] autoGenOP = (Object[]) params.get("auto_gen_op");
		if ( (format.equals("doc_pdf_form_templates") || format.equals("doc_rich_templates" )) ) {
			if (autoGenIP == null )
				bean.set("auto_gen_ip", "N");
			if (autoGenOP == null)
				bean.set("auto_gen_op", "N");
		}

		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			GenericDocumentTemplateDAO hvfFieldsDao = null;
			boolean success = false;

			if (errors.isEmpty()) {

				boolean exists = dao.exist((Boolean) bean.get("specialized"), (String) bean.get("doc_type"), (String) bean.get("dept_name"),
										bean.get("template_id"), (String) bean.get("template_name"));
				if (!exists) {
					int count = dao.update(con, bean.getMap(), keys);
					if (count > 0) {
						success = true;
						if (format.equals("doc_hvf_templates")) {
							success = false;
							count = 0;
							hvfFieldsDao = new GenericDocumentTemplateDAO("doc_hvf_template_fields");
							BasicDynaBean fieldsBean = hvfFieldsDao.getBean();
							Map<String, Object> feilds_keys = new HashMap<String, Object>();
							feilds_keys.put("template_id", Integer.parseInt(key));

							String[] selectedFields = (String[])params.get("hDeleteRow");

							Object[] object = (Object[])params.get("field_id");
							if (object != null && object[0] != null) {
								for (int i=0; i< object.length; i++) {
									Object field_id = object[i];
									ConversionUtils.copyIndexToDynaBean(params, index++, fieldsBean, errors);
									fieldsBean.set("template_id", Integer.parseInt(key));
									if (field_id.toString().equals("")) {
										fieldsBean.set("field_id", hvfFieldsDao.getNextSequence());
										if (hvfFieldsDao.insert(con, fieldsBean)) {
											count++;
										} else {
											break;
										}
									} else {
										feilds_keys.put("field_id", Integer.parseInt(field_id.toString()));
										count += hvfFieldsDao.update(con, fieldsBean.getMap(), feilds_keys);
									}
								}
							}
							if (index == count) {
								success = true;
							}
						}

						if (format.equals("doc_pdf_form_templates")) {

							String[] fieldIdArr = (String[])params.get("field_id");
							String[] fieldNameArr = (String[])params.get("field_name");
							String[] displayNameArr = (String[])params.get("display_name");
							String[] fieldInputArr = (String[])params.get("field_input");
							String[] fieldDeleteArr = (String[])params.get("field_delete");

							if (fieldIdArr != null &&  fieldIdArr.length > 0) {

								for (int i = 0; i < fieldIdArr.length; i++) {
									if (fieldIdArr[i] != null && fieldIdArr[i].startsWith("_")
											&& fieldInputArr[i] != null && !fieldInputArr[i].equals("")) {

										int fieldId = docPdfTemplateExtFieldsDAO.getNextSequence();
										BasicDynaBean extFieldBean = docPdfTemplateExtFieldsDAO.getBean();
										extFieldBean.set("template_id", Integer.parseInt(key));
										extFieldBean.set("field_id", fieldId);
										extFieldBean.set("field_name", fieldNameArr[i]);
										extFieldBean.set("display_name", displayNameArr[i]);
										extFieldBean.set("field_input", fieldInputArr[i]);

										docPdfTemplateExtFieldsDAO.insert(con, extFieldBean);

									}else if (fieldIdArr[i] != null && !fieldIdArr[i].equals("")
											&& fieldInputArr[i] != null && !fieldInputArr[i].equals("")) {

										if (fieldDeleteArr[i] != null && fieldDeleteArr[i].equals("Y")) {
											int fieldId = Integer.parseInt(fieldIdArr[i]);
											LinkedHashMap identifiers = new LinkedHashMap();
											identifiers.put("field_id", fieldId);
											identifiers.put("template_id", Integer.parseInt(key));

											docPdfTemplateExtFieldsDAO.delete(con, identifiers);

										}else {
											int fieldId = Integer.parseInt(fieldIdArr[i]);
											BasicDynaBean extFieldBean = docPdfTemplateExtFieldsDAO.findByKey("field_id", fieldId);
											extFieldBean.set("field_name", fieldNameArr[i]);
											extFieldBean.set("display_name", displayNameArr[i]);
											extFieldBean.set("field_input", fieldInputArr[i]);

											docPdfTemplateExtFieldsDAO.updateWithName(con, extFieldBean.getMap(), "field_id");
										}
									}
								}
							}
						}
					}


					if (success) {
						con.commit();
						flash.put("success", "Template details updated successfully..");
						//ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
						//redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
						//return redirect;
					} else {
						con.rollback();
						error = "Failed to update Template details..";
					}
				} else {
					error = "Template name already exists..";
				}
			} else {
				error = "Incorrectly formatted values supplied";
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}

		flash.put("error", error);

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("format", format);
		redirect.addParameter("template_id", key);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		return redirect;
	}

	/**
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 * @throws SQLException
	 */
	public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, SQLException {
		String format = request.getParameter("format");
		String template_id = request.getParameter("template_id");
		GenericDocumentTemplateDAO dao = new GenericDocumentTemplateDAO(format);
		BasicDynaBean bean = dao.getBean();
		dao.loadByteaRecords(bean, "template_id", Integer.parseInt(template_id));

		if (format.equals("doc_pdf_form_templates")) {
			response.setContentType("application/pdf");
		} else {
			response.setContentType(bean.get("content_type").toString());
		}

		byte[] bytes = DataBaseUtil.readInputStream((java.io.InputStream)bean.get("template_content"));
		OutputStream stream = response.getOutputStream();
		stream.write(bytes);
		stream.flush();
		stream.close();

		return null;
	}

	public ActionForward getOdtFile(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, SQLException {
		String template_id = request.getParameter("template_id");
		GenericDocumentTemplateDAO dao = new GenericDocumentTemplateDAO("doc_pdf_form_templates");
		BasicDynaBean bean = dao.getBean();
		dao.loadByteaRecords(bean, "template_id", Integer.parseInt(template_id));

		response.setContentType("application/vnd.oasis.opendocument.text");

		byte[] bytes = DataBaseUtil.readInputStream((java.io.InputStream)bean.get("odt_file"));
		OutputStream stream = response.getOutputStream();
		stream.write(bytes);
		stream.flush();
		stream.close();

		return null;
	}

	private String getForward(String format) {
		String forward = null;
		if (format.equals("doc_hvf_templates")) {
			forward = "addshowHVF";
		} else if (format.equals("doc_rich_templates")) {
			forward = "addshowRichText";
		} else {
			forward = "addshowPDFandRTF";
		}

		return forward;
	}
}
