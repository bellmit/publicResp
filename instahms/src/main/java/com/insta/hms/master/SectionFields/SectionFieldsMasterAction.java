/**
 *
 */
package com.insta.hms.master.SectionFields;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.master.ImageMarkers.ImageMarkerDAO;
import com.insta.hms.master.RegularExpression.RegularExpressionDAO;
import com.insta.hms.master.Sections.SectionsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class SectionFieldsMasterAction extends BaseAction {

	SectionFieldsDAO fieldsDao = new SectionFieldsDAO();
	SectionFieldOptionsDAO optionDao = new SectionFieldOptionsDAO();
	SectionsDAO sectionDao = new SectionsDAO();
	ImageMarkerDAO markerDao = new ImageMarkerDAO();

	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ParseException {
		Map params = new HashMap(request.getParameterMap());
		Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(params);
		request.setAttribute("pagedList", fieldsDao.searchSectionFields(params, listingParams));
		return mapping.findForward("list");
	}

	public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		Integer section_id = Integer.parseInt(request.getParameter("section_id"));
		List<BasicDynaBean> regExpFieldList = RegularExpressionDAO.getAllActiveRegExp();
		request.setAttribute("regExpFieldList", regExpFieldList);
		request.setAttribute("regExpPattern", RegularExpressionDAO.getRegPatternNames());
		request.setAttribute("section_details", sectionDao.findByKey("section_id", section_id));
		request.setAttribute("available_markers", markerDao.getMarkers());
		return mapping.findForward("addshow");
	}

	public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		int field_id = Integer.parseInt(request.getParameter("field_id"));
		BasicDynaBean fieldDetails = fieldsDao.getFieldDetails(field_id);
		List<BasicDynaBean> regExpFieldList = RegularExpressionDAO.getAllActiveRegExp();
		request.setAttribute("regExpFieldList", regExpFieldList);
		request.setAttribute("regExpPattern", RegularExpressionDAO.getRegPatternNames());
		request.setAttribute("selected_markers", markerDao.getMarkers(field_id));
		request.setAttribute("available_markers", markerDao.getMarkers());
		request.setAttribute("field_details", fieldDetails);
		request.setAttribute("options_list", fieldsDao.getOptionDetails(field_id));
		return mapping.findForward("addshow");
	}

	/*
	 * this method inserts the field details in section_field_desc table.
	 * and inserts it's options into the options table(for field type of dropdown and checkbox).
	 */
	public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException, FileUploadException {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		Map params = getParameterMap(request);

		if (params.get("fileSizeError") != null) {
			// if the file size is greater than 10 MB prompting the user with the failure message.
			redirect = new ActionRedirect(mapping.findForward("fileUploadSizeError"));
			return redirect;
		}
		List errors = new ArrayList();
		BasicDynaBean bean = fieldsDao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String regExp = getParameter(params, "regexp_field_id");
		if (regExp != null && !regExp.equals("")) {
			bean.set("pattern_id", Integer.parseInt(regExp));
		}
		if (bean.get("allow_others") == null)
			bean.set("allow_others", "N");
		if (bean.get("allow_normal") == null)
			bean.set("allow_normal", "N");

		if (bean.get("field_type").equals("image")) {
			String[] markers = getParameterValues(params, "selected_markers");
			String commaSeparatedMarkers = "";
			boolean first = true;
			for (int i=0; i<markers.length; i++) {
				if (first)
					commaSeparatedMarkers += markers[i];
				else
					commaSeparatedMarkers += "," + markers[i];
				first = false;
			}
			bean.set("markers", commaSeparatedMarkers);
		}
		bean.set("status", getParameter(params, "field_status"));
		bean.set("display_order", Integer.parseInt(getParameter(params, "field_display_order")));

		String field_phrase_category = getParameter(params, "field_phrase_category_id");
		String field_type = (String) bean.get("field_type");
		if (field_phrase_category != null && !field_phrase_category.equals("")
				&& (field_type.equals("text") || field_type.equals("wide text")))
			bean.set("phrase_category_id", Integer.parseInt(field_phrase_category));

		String error = null;
		if (errors.isEmpty()) {
			if (fieldsDao.fieldNameExists((String) bean.get("field_name"), (Integer) bean.get("section_id"))) {
				error = "Field Name "+ bean.get("field_name")+" already exists";
			} else {
				Connection con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				bean.set("field_id", fieldsDao.getNextSequence());
				boolean flag = false;
				try {
					txn: {
						if (!fieldsDao.insert(con, bean)) break txn;
						
						if (bean.get("is_mandatory") != null && (Boolean) bean.get("is_mandatory")) {
						  
						  BasicDynaBean sectionBean = sectionDao.getBean();
						  sectionBean.set("section_mandatory", true);

						  if (sectionDao.update(con, sectionBean.getMap(), "section_id", (Integer) bean.get("section_id")) == 0) break txn;
						}

						if (!bean.get("field_type").equals("text") && !bean.get("field_type").equals("image")) {
							String[] option_id = getParameterValues(params, "option_id");
							String[] option_value = getParameterValues(params, "option_value");
							String[] value_code = getParameterValues(params, "value_code");
							String[] option_status = getParameterValues(params, "option_status");
							String[] option_display_order = getParameterValues(params, "option_display_order");
							String[] option_phrase_category = getParameterValues(params, "option_phrase_category");
							String[] option_regexp_pattern = getParameterValues(params, "option_regexp_field");
							if (option_id != null) {
								List optionsList = new ArrayList(); // newly added options list
								for (int i=0; i<option_id.length-1; i++) {
									BasicDynaBean optionBean = optionDao.getBean();
									optionBean.set("option_value", option_value[i]);
									optionBean.set("value_code", value_code[i]);
									optionBean.set("status", option_status[i]);
									optionBean.set("display_order", Integer.parseInt(option_display_order[i]));
									optionBean.set("option_id", optionDao.getNextSequence());
									optionBean.set("field_id", bean.get("field_id"));
									if (field_type.equals("checkbox") && !option_phrase_category[i].equals("")) {
										optionBean.set("phrase_category_id", Integer.parseInt(option_phrase_category[i]));
									}
									if (field_type.equals("checkbox") && !option_regexp_pattern[i].equals("")) {
										optionBean.set("pattern_id", Integer.parseInt(option_regexp_pattern[i]));
									}
									optionsList.add(optionBean);
								}
								if (!optionsList.isEmpty()) {
									if (!optionDao.insertAll(con, optionsList)) {
										error = "Failed to insert the Field options";
										break txn;
									}
								}
							}
						}
						flag = true;
					}
				} finally {
					DataBaseUtil.commitClose(con, flag);
				}
			}
		} else {
			error = "Incorrectly formatted values supplied..";
		}
		if (error == null) {
			redirect.addParameter("section_id", bean.get("section_id"));
			redirect.addParameter("field_id", bean.get("field_id"));
		} else {
			redirect = new ActionRedirect(mapping.findForward("addRedirect"));
			redirect.addParameter("section_id", bean.get("section_id"));
			flash.put("error", error);
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, ServletException, FileUploadException {
		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(request);
		Map params = getParameterMap(request);
		if (params.get("fileSizeError") != null) {
			// if the file size is greater than 10 MB prompting the user with the failure message.
			redirect = new ActionRedirect(mapping.findForward("fileUploadSizeError"));
			return redirect;
		}
		List errors = new ArrayList();
		BasicDynaBean bean = fieldsDao.getBean();
		ConversionUtils.copyToDynaBean(params, bean, errors);
		String regExp = getParameter(params, "regexp_field_id");
		if (regExp != null && !regExp.equals("")){
			bean.set("pattern_id", Integer.parseInt(regExp));
		} else {
			bean.set("pattern_id", null);
		}
		if (bean.get("allow_others") == null)
			bean.set("allow_others", "N");
		if (bean.get("allow_normal") == null)
			bean.set("allow_normal", "N");

		if (bean.get("field_type").equals("image")) {
			String[] markers = getParameterValues(params, "selected_markers");
			String commaSeparatedMarkers = "";
			boolean first = true;
			for (int i=0; i<markers.length; i++) {
				if (first)
					commaSeparatedMarkers += markers[i];
				else
					commaSeparatedMarkers += "," + markers[i];
				first = false;
			}
			bean.set("markers", commaSeparatedMarkers);
		}

		bean.set("status", getParameter(params, "field_status"));
		bean.set("display_order", Integer.parseInt(getParameter(params, "field_display_order")));
		String error = null;

		String field_phrase_category = getParameter(params, "field_phrase_category_id");
		String field_type = (String) bean.get("field_type");
		if (field_phrase_category != null && !field_phrase_category.equals("")
				&& (field_type.equals("text") || field_type.equals("wide text")))
			bean.set("phrase_category_id", Integer.parseInt(field_phrase_category));
		else
			bean.set("phrase_category_id", null);
		
		String defaultToCurrentDatetime = getParameter(params, "default_to_current_datetime");
		if (defaultToCurrentDatetime != null && defaultToCurrentDatetime.equals("Y")) {
			bean.set("default_to_current_datetime", defaultToCurrentDatetime);
		} else {
			bean.set("default_to_current_datetime", "N");
		}
		

		if (errors.isEmpty()) {
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			boolean flag = false;
			try {
				if (fieldsDao.exists(con, (String) bean.get("field_name"), (Integer) bean.get("section_id"),
						(Integer) bean.get("field_id"))) {
					error = "Field Name "+ bean.get("field_name")+" already exists";
				} else {
					txn: {
						if (fieldsDao.update(con, bean.getMap(), "field_id", bean.get("field_id")) == 0) break txn;

						if (bean.get("is_mandatory") != null && (Boolean) bean.get("is_mandatory")) {

              BasicDynaBean sectionBean = sectionDao.getBean();
              sectionBean.set("section_mandatory", true);

              if (sectionDao.update(con, sectionBean.getMap(), "section_id", (Integer) bean.get("section_id")) == 0) break txn;
            }

						if (!bean.get("field_type").equals("text") && !bean.get("field_type").equals("image")) {
							String[] option_id = getParameterValues(params, "option_id");
							String[] option_value = getParameterValues(params, "option_value");
							String[] value_code = getParameterValues(params, "value_code");
							String[] option_status = getParameterValues(params, "option_status");
							String[] option_display_order = getParameterValues(params, "option_display_order");
							String[] option_phrase_category = getParameterValues(params, "option_phrase_category");
							String[] option_regexp_pattern = getParameterValues(params, "option_regexp_field");
							if (option_id != null) {
								List optionsList = new ArrayList(); // newly added options list
								for (int i=0; i<option_id.length-1; i++) {
									BasicDynaBean optionBean = optionDao.getBean();
									optionBean.set("option_value", option_value[i]);
									optionBean.set("value_code", value_code[i]);
									optionBean.set("status", option_status[i]);
									optionBean.set("display_order", Integer.parseInt(option_display_order[i]));
									optionBean.set("field_id", bean.get("field_id"));
									if (field_type.equals("checkbox") && !option_phrase_category[i].equals("")) {
										optionBean.set("phrase_category_id", Integer.parseInt(option_phrase_category[i]));
									} else {
										optionBean.set("phrase_category_id", null);
									}
									if (field_type.equals("checkbox") && !option_regexp_pattern[i].equals("")) {
										optionBean.set("pattern_id", Integer.parseInt(option_regexp_pattern[i]));
									} else {
										optionBean.set("pattern_id", null);
									}

									if (option_id[i].equals("_")) {
										optionBean.set("option_id", optionDao.getNextSequence());
										optionsList.add(optionBean);
									} else {
										Map<String, Object> keys = new HashMap<String, Object>();
										keys.put("option_id", Integer.parseInt(option_id[i]));
										if (optionDao.update(con, optionBean.getMap(), keys) != 1) {
											error = "Failed to update the Field Option "+ option_value[i];
											break txn;
										}
									}
								}
								if (!optionsList.isEmpty()) {
									if (!optionDao.insertAll(con, optionsList)) {
										error = "Failed to insert the Field options";
										break txn;
									}
								}
							}
						}
						flag = true;
					}
				}
			} finally {
				DataBaseUtil.commitClose(con, flag);
			}
		} else {
			error = "Incorrectly formatted values supplied..";
		}
		flash.put("error", error);
		redirect.addParameter("section_id", bean.get("section_id"));
		redirect.addParameter("field_id", bean.get("field_id"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}


}
