<%JSONSerializer js = new JSONSerializer().exclude("class");
	int sectionId = Integer.parseInt(request.getParameter("section_id"));

	SectionsDAO sectionDescDAO = new SectionsDAO();
	SectionFieldsDAO fieldDescDAO = new SectionFieldsDAO();
	
	BasicDynaBean sectionDesc = (BasicDynaBean) ((java.util.Map) request.getAttribute("sectionsDefMap")).get(sectionId);
	request.setAttribute("sectionDesc", sectionDesc.getMap());

	// fields available in this form
	List<BasicDynaBean> fieldDescs = fieldDescDAO.getFields(sectionId, "A");
	request.setAttribute("fieldDescs", ConversionUtils.listBeanToListMap(fieldDescs));

	/*
	 * options for each field available in this form
	 *  field1 => [option1Map, option2Map]
	 *  field2 => [option1Map]
	 */
	java.util.List<BasicDynaBean> formOptions = sectionDescDAO.getSectionFieldOptions(sectionId);
	request.setAttribute("fieldOptions", ConversionUtils.listBeanToMapListMap(formOptions, "field_id"));
	request.setAttribute("optionsMapJSON", js.serialize(
				ConversionUtils.listBeanToMapMap(formOptions, "option_id")));

	/*
	 * actual values for this consultation
	 *  field1 => [value1Map, value2Map]
	 *  field2 => [value1Map]
	 */
	String formType = request.getParameter("form_type");
	AbstractInstaForms formDAO = AbstractInstaForms.getInstance(formType);

	int sectionItemId = formDAO.getSectionItemId(request.getParameterMap());
	String mrNo = request.getParameter("mr_no");
	String patientId = request.getParameter("patient_id");
	int formId = Integer.parseInt(request.getParameter("insta_form_id"));
	String itemType = (String) formDAO.getKeys().get("item_type");

	int genericFormId = 0;
	String genericFormIdStr = request.getParameter("generic_form_id");
	if (genericFormIdStr != null && !genericFormIdStr.equals(""))
		genericFormId = Integer.parseInt(genericFormIdStr);
	
	BasicDynaBean patientSectionDetailsBean = PatientSectionDetailsDAO.getRecord(mrNo, patientId, sectionItemId, genericFormId, sectionId, formId, itemType);
	
	List<BasicDynaBean> sectionValues = formDAO.getSectionDetails(formId, formType, itemType, sectionId,
			(String) sectionDesc.get("linked_to"), mrNo, patientId, sectionItemId, genericFormId, patientSectionDetailsBean);
	
	Boolean displayDefaultText = patientSectionDetailsBean == null;
	
	if (sectionValues.isEmpty()) {
        HashMap temp_sectionValues = new LinkedHashMap();
        if(patientSectionDetailsBean != null) {
            temp_sectionValues.put(Integer.toString((Integer)patientSectionDetailsBean.get("section_detail_id")), new LinkedHashMap());
            request.setAttribute("newSections", 0);
        } else {
            temp_sectionValues.put(sectionId + "_new_1", new LinkedHashMap());
            request.setAttribute("newSections", 1);
        }
        request.setAttribute("allFieldValues", temp_sectionValues);
    }
    else {
        request.setAttribute("allFieldValues", ConversionUtils.listBeanToMapMapListMap(sectionValues, "section_detail_id", "field_id"));
        request.setAttribute("newSections", 0);
    }
	
	String finalized = "N";
	if (patientSectionDetailsBean != null) {
	    finalized = (String)patientSectionDetailsBean.get("finalized");
	}
	
	request.setAttribute("finalized", finalized);
	
	List<BasicDynaBean> imageMarkersForSection = (List<BasicDynaBean>) ((java.util.Map) request.getAttribute("sectionsImageMarkers")).get(sectionId);
 	request.setAttribute("imageMarkers", imageMarkersForSection == null ? new java.util.LinkedHashMap() : ConversionUtils.listBeanToMapListMap(imageMarkersForSection, "field_id"));

	request.setAttribute("displayDefaultText", displayDefaultText);%>

<%@page import="com.insta.hms.common.ConversionUtils" %>
<%@page import="org.apache.commons.beanutils.BasicDynaBean"%>
<%@page import="com.insta.hms.master.Sections.SectionsDAO"%>
<%@page import="java.util.List"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="flexjson.JSONSerializer"%>
<%@page import="com.insta.hms.master.SectionFields.SectionFieldsDAO"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.insta.hms.master.ImageMarkers.ImageMarkerDAO"%>
<%@page import="com.insta.hms.master.RegularExpression.RegularExpressionDAO" %>

<%@page import="com.insta.hms.instaforms.AbstractInstaForms"%>
<%@page import="com.insta.hms.instaforms.PatientSectionDetailsDAO"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="insta_section_id" value="${param.section_id}"/>
<c:set var="regexp" value="${regExpMapDesc}"/>
<script>
	var sectionFormName = "${param.form_name}";
	var regExp = ${regExpPatternMap};
	var mrNo = "${param.mr_no}"
</script>
<style>
	.togglebutton {
		width: 30px;
		height: 20px;
	}
	.wideTextArea {
		width: 750px;
	}
	.normalTextArea {
		width: 120px;
	}
	.checkboxText{
		width: 130px;
		float: left;
		padding-top: 2px;
	}
	.newsection{
		float: right;
		cursor: pointer;
	}
	.mandatoryFieldColor{
		color: #FF2C00;
	}
	textarea[readonly] {
		color: #999;
		background-color: #efebe7;
	}
</style>

<!-- To maintain total number of new sections added -->
<input type="hidden" id="new_sections_count_${insta_section_id}" name="new_sections_count_${insta_section_id}" value="${newSections}" />

<!-- To maintain all insta sections -->
<input type="hidden" name="insta_sections" value="${insta_section_id}"/>
<input type="hidden" name="is_sections_mandatory" id="section_mandatory_${insta_section_id}" value="${sectionDesc.section_mandatory}"/>

<c:set var="is_flds_in_stn_mandatory" value="${false}"/>
<!-- For validation of fields in insta section  -->
<c:forEach items="${fieldDescs}" var="field" varStatus="i">
	<input type="hidden" name="field_ids_${insta_section_id}" value="${field.field_id}"/>
	<input type="hidden" id="field_phrase_category_id_${field.field_id}" value="${field.field_phrase_category_id}"/>
	<input type="hidden" name="field_type_${field.field_id}" value="${field.field_type}"/>
	<c:if test="${field.field_type == 'checkbox'}">
		<c:forEach items="${fieldOptions[field.field_id]}" var="option" varStatus="j">
			<input type="hidden" id="option_phrase_category_id_${option.option_id}" value="${option.option_phrase_category_id}"/>
			<input type="hidden" name="pattern_id_${field.field_id}_${option.option_id}" value="${option.pattern_id}"/>
		</c:forEach>
	</c:if>
	<input type="hidden" name="is_fields_mandatory" id="is_field_mandatory_${field.field_id}"
		value="${field.is_mandatory}"/>
	<c:if test="${field.is_mandatory}">
		<c:set var="is_flds_in_stn_mandatory" value="${true}"/>
	</c:if>
	<input type="hidden" name="pattern_id_${field.field_id}" value="${field.pattern_id}"/>
</c:forEach>
<c:set var="is_stn_in_list" value="${false}"/>
<c:forEach items="${section_rights}" var="stn">
	<c:if test="${stn == insta_section_id}">
		<c:set var="is_stn_in_list" value="${true}"/>
	</c:if>
</c:forEach>
<c:set var="stn_right_access"
	value="${((roleId == 1 || roleId == 2 || is_stn_in_list) && finalized != 'Y')}"/>

<c:if test="${insta_section_id > 0 && group_patient_sections == 'Y' && sectionDesc.linked_to == 'patient' && stn_right_access}">
	<input type="hidden" name="is_patient_history_mandatory" value="${is_flds_in_stn_mandatory || sectionDesc.section_mandatory}"/>
</c:if>
<c:forEach items="${allFieldValues}" var="insta_section_details" varStatus="isd_id_c">
	<c:set var="insta_section_detail_id" value="${insta_section_details.key}"/>
	<input type="hidden" name="section_detail_ids_${insta_section_id}" value="${insta_section_detail_id}"/> <!-- to maintain all duplicate sections -->
	<fieldset class="fieldSetBorder" id="insta_section_detail_id">
		<c:choose>
			<c:when test="${param.displayExpandedSection == 'block'}">
				<c:set var="sectionDisplay" value="${isd_id_c.last ? 'block' : 'none'}"/>
			</c:when>
			<c:otherwise>
				<c:set var="sectionDisplay" value="none"/>
			</c:otherwise>
		</c:choose>
		<legend class="fieldSetLabel">
			<span id="section_title_${insta_section_detail_id}">${sectionDesc.section_title} ${isd_id_c.index == 0 ? '' :  isd_id_c.index}</span>
			<c:if test="${(sectionDesc.section_mandatory || is_flds_in_stn_mandatory) && stn_right_access}">
				<span class="mandatoryFieldColor">*</span>
			</c:if>
			<c:if test="${!(roleId == 1 || roleId == 2 || is_stn_in_list)}">
				<i>[Read-Only]</i>
			</c:if>
			<c:if test="${finalized == 'Y'}">
				<i>[Finalized]</i>
			</c:if>
			<input class="togglebutton" type="button" id="insta_section_btn_${insta_section_detail_id}"
				onclick="toggleInstaSection('${insta_section_id}','${insta_section_detail_id}', this);"
				value="${sectionDisplay == 'none' ? '+' : '-'}"/>
		</legend>
		<span id="${insta_section_detail_id}_minText" style="display: ${sectionDisplay == 'none' ? 'block' : 'none'}">
			<c:if test="${sectionDisplay == 'none'}">
			<c:forEach items="${fieldDescs}" var="field" varStatus="st">
				<c:set var="values" value="${allFieldValues[insta_section_detail_id][field.field_id][0]}"/>
				<c:if test="${not empty values}">
					<c:choose>
						<c:when test="${field.field_type == 'text' || field.field_type == 'wide text'}">
							<b>${field.field_name} </b>:
							<c:set var="value" value="${allFieldValues[insta_section_detail_id][field.field_id][0]}"/>
							<c:if test="${not empty value.option_remarks}">
								<b>${field.field_name} </b>:
								<c:out value="${value.option_remarks}"/>;
							</c:if>
						</c:when>
						<c:when test="${field.field_type == 'dropdown' && fn:length(values) > 0}">
							<c:set var="value" value="${allFieldValues[insta_section_detail_id][field.field_id][0]}"/>
							
							<c:if test="${fn:length(values) != 0}">
								<b>${field.field_name} </b>:
								<c:choose>
									<c:when test="${value.option_id == -1}">
										Others - <c:out value="${value.option_remarks}"/>
									</c:when>
									<c:otherwise>
										<c:out value="${value.option_value}"/>
									</c:otherwise>
								</c:choose>;
							</c:if>
						</c:when>
						<c:when test="${field.field_type == 'checkbox'}">
							<c:set var="values" value="${allFieldValues[insta_section_detail_id][field.field_id]}"/>
							<c:if test="${fn:length(values) > 0}">
								<b>${field.field_name} </b>:
								<c:forEach items="${values}" var="value" varStatus="vst">
									<c:out value="${value.option_value}"/><c:if test="${not empty value.option_remarks}">-</c:if><c:out value="${value.option_remarks}"/>
									<c:if test="${not vst.last}">,</c:if>
								</c:forEach>;
							</c:if>
						</c:when>
						<c:when test="${field.field_type == 'date'}">
							<c:set var="value" value="${allFieldValues[insta_section_detail_id][field.field_id][0]}"/>
							<c:if test="${not empty value.date}">
								<b>${field.field_name} </b>:
								<fmt:formatDate value="${value.date}" pattern="dd-MM-yyyy"/>;
							</c:if>
						</c:when>
						<c:when test="${field.field_type == 'datetime'}">
							<c:set var="value" value="${allFieldValues[insta_section_detail_id][field.field_id][0]}"/>
							<c:if test="${not empty value.date_time}">
								<b>${field.field_name} </b>:
								<fmt:formatDate value="${value.date_time}" pattern="dd-MM-yyyy HH:mm"/>;
							</c:if>
						</c:when>
						<c:otherwise>
						</c:otherwise>
					</c:choose>
				</c:if>
			</c:forEach>
		</c:if>
		</span>
		<span id="section_detail_view_${insta_section_detail_id}" style="display: ${sectionDisplay};">
		<table class="formtable"  id="insta_section_table${insta_section_detail_id}">
			<tr>
				<c:set var="colsPerRow" value="0"/>
				<c:forEach items="${fieldDescs}" var="field" varStatus="st">
					<c:set var="showInEntireRow" value="false"/>
					<c:if test="${colsPerRow > 0 && (colsPerRow % 3) == 0}"> <!-- Constraints to 3 fields in a row -->
						<c:out value="</tr><tr>" escapeXml="false"/>
					</c:if>
					<c:if test="${field.field_type == 'image' || field.field_type == 'wide text'}">
						<%-- if the field type is a image or wide text, then display it in a next row --%>
						<c:if test="${colsPerRow%3 != 0}">
							<c:forEach var="empty-cells" begin="1" end="${3-(colsPerRow%3)}">
								<td class="formlabel">&nbsp;</td>
								<td >&nbsp;</td>
							</c:forEach>
							<c:out value="</tr><tr>" escapeXml="false"/>
						</c:if>
						<c:set var="colsPerRow" value="${colsPerRow + (3-(colsPerRow%3))}"/>
						<c:set var="showInEntireRow" value="true"/>
					</c:if>
					<td class="formlabel">
						<span id="field_title_${insta_section_detail_id}_${field.field_id}">${field.field_name}</span>
						<c:if test="${field.is_mandatory}">
							<span class="mandatoryFieldColor">*</span>
						</c:if>
						:
					</td>
					<td class="forminfo" colspan="${showInEntireRow ? 5 : 1}">
						<c:choose>
	
							<c:when test="${field.field_type == 'text' || field.field_type == 'wide text'}">
								<c:set var="value" value="${allFieldValues[insta_section_detail_id][field.field_id][0]}"/>
								<input type="hidden" name="${insta_section_detail_id}_field_detail_id_${field.field_id}" value="${value.field_detail_id}"/>
								<div style="float: left">
									<textarea class="${field.field_type == 'wide text' ? 'wideTextArea' : 'normalTextArea'}"
											style="resize: none;"
											name="${insta_section_detail_id}_option_remarks_${field.field_id}" rows="${field.no_of_lines}"
											onmouseup="adjustTextArea('${insta_section_id}', '${patient.mr_no}');"
											${!stn_right_access ? 'Readonly' : ''}><c:out value="${displayDefaultText && empty value.option_remarks ? field.normal_text : value.option_remarks}"/></textarea>
									<c:if test="${field.pattern_id != null && not empty regexp[field.pattern_id] && empty field.field_phrase_category_id}" >
										<img class="imgHelpText" src="${cpath}/images/help.png" title="${regexp[field.pattern_id]}"/>
									</c:if>
								</div>
								<c:if test="${not empty field.field_phrase_category_id}">
									<div class="multiInfoEditBtn" style="float: left; margin-left: 8px">
										<a href="javascript:void(0);"
											<c:if test="${stn_right_access}">
												onclick="return showPhraseEditDialog(this,
												'${insta_section_detail_id}', ${field.field_id}, null, ${field.field_phrase_category_id});"
											</c:if>
												title="Select Values">
											<img class="button" src="${cpath}/icons/openbook.png" />
										</a>
									</div>
								</c:if>
								<!-- option id is set for text field as -2 instead of null -->
								<input type="hidden" name="${insta_section_detail_id}_field_${field.field_id}" value="-2"/>
							</c:when>
	
							<c:when test="${field.field_type == 'date' || field.field_type == 'datetime'}">
								<c:set var="value" value="${allFieldValues[insta_section_detail_id][field.field_id][0]}" />
								<input type="hidden" name="${insta_section_detail_id}_field_detail_id_${field.field_id}" value="${value.field_detail_id}"/>
								<c:choose>
									<c:when test="${stn_right_access && field.default_to_current_datetime == 'Y' && empty value.date_time && displayDefaultText}">
										<jsp:useBean id="now" class="java.util.Date"/>
										<fmt:formatDate var="section_field_date" value="${now}" pattern="dd-MM-yyyy"/>
										<fmt:formatDate var="section_field_time" value="${now}" pattern="HH:mm"/>
									</c:when>
									<c:otherwise>
										<fmt:formatDate var="section_field_date" value="${field.field_type == 'date' ? value.date : value.date_time}" pattern="dd-MM-yyyy"/>
										<fmt:formatDate var="section_field_time" value="${value.date_time}" pattern="HH:mm"/>
									</c:otherwise>
								</c:choose>
								<c:if test="${field.field_type == 'date'}">
									<!-- option id is set for date field as -3 instead of null -->
									<input type="hidden" name="${insta_section_detail_id}_field_${field.field_id}" value="-3"/>
								</c:if>
								<div>
									<insta:datewidget name="${insta_section_detail_id}_date_${field.field_id}" id="${insta_section_detail_id}_date_${field.field_id}"
										value="${section_field_date}" editValue="${!stn_right_access}"/>
									<c:if test="${field.field_type == 'datetime'}">
										<input type="text" style="width: 40px" name="${insta_section_detail_id}_time_${field.field_id}" 
											id="${insta_section_detail_id}_time_${field.field_id}" value="${section_field_time}" ${!stn_right_access ? 'Readonly' : ''}/>
										<!-- option id is set for date time field as -4 instead of null -->
										<input type="hidden" name="${insta_section_detail_id}_field_${field.field_id}" value="-4"/>
									</c:if>
								</div>
							</c:when>
							
							<c:when test="${field.field_type == 'dropdown'}">
								<%-- Single valued field: show a dropdown and optional "others" text box --%>
								<c:set var="value" value="${allFieldValues[insta_section_detail_id][field.field_id][0]}"/>
								<input type="hidden" name="${insta_section_detail_id}_field_detail_id_${field.field_id}" value="${value.field_detail_id}"/>
								<select name="${insta_section_detail_id}_field_${field.field_id}" class="dropdown" style="font-weight: bold"
										onchange="onChangeOption(${field.field_id}, '${insta_section_detail_id}')" ${!stn_right_access ? 'Disabled' : ''}>
									<c:set var="dropbox_value" value=""/>	
									<option value="" ${empty value ? 'selected' : ''}></option>
									<c:if test="${field.allow_normal == 'Y'}">
										<option value="0" ${value.option_id == 0 ? 'selected' : ''}>Normal</option>
										<c:if test="${value.option_id == 0}">
											<c:set var="dropbox_value" value="0"/>
										</c:if>
									</c:if>
									<c:forEach items="${fieldOptions[field.field_id]}" var="option">
										<option value="${option.option_id}" ${value.option_id==option.option_id ? 'selected' : ''}>
											<c:out value="${option.option_value}"/>
											<c:if test="${value.option_id==option.option_id}">
												<c:set var="dropbox_value" value="${option.option_id}"/>
											</c:if>
										</option>
									</c:forEach>
									<c:if test="${field.allow_others == 'Y'}">
										<!-- option id is set for date time field as -1 instead of null -->
										<option value="-1" ${value.option_id == -1 ? 'selected' : ''}>Others</option>
										<c:if test="${value.option_id == -1}">
											<c:set var="dropbox_value" value="-1"/>
										</c:if>
									</c:if>
								</select>
								<c:if test="${!stn_right_access}">
									<input type="hidden" name="${insta_section_detail_id}_field_${field.field_id}" value="${dropbox_value}"/>
								</c:if>
								<c:if test="${field.allow_others == 'Y'}">
									<input type="text" name="${insta_section_detail_id}_option_remarks_${field.field_id}" value="${value.option_id == -1 ? value.option_remarks : ''}"
										${value.option_id == -1 && stn_right_access ? '' : 'readonly'}/>
								</c:if>
							</c:when>
							
							<c:when test="${field.field_type == 'checkbox'}">
								<%-- multivalued field: show the values as text and an edit dialog for the actual value --%>
								<c:set var="values" value="${allFieldValues[insta_section_detail_id][field.field_id]}"/>
								<input type="hidden" name="${insta_section_detail_id}_field_detail_id_${field.field_id}" value="${values[0].field_detail_id}"/>
								<%-- text to display --%>
								<div id="fieldText_${insta_section_detail_id}_${field.field_id}" class="checkboxText">
									<c:if test="${empty values}">--</c:if>
									<c:if test="${(not empty values) && (values[0].option_id == 0)}">Normal</c:if>
									<c:forEach items="${values}" var="value" varStatus="vst">
										<c:out value="${value.option_value}"/><c:if test="${not empty value.option_remarks}">-</c:if><c:out value="${value.option_remarks}"/>
										<c:if test="${not vst.last}"><br/></c:if>
									</c:forEach>
								</div>
								<%-- edit icon --%>
								<div class="multiInfoEditBtn" style="float: left">
										<a href="javascript:void(0);"
												<c:if test="${stn_right_access}">
													onclick="return showPysicianFormEditDialog('${insta_section_detail_id}',${field.field_id});"
												</c:if>
												title="Select Values">
											<img src="${cpath}/icons/${stn_right_access ? 'Edit':'Edit1'}.png" class="button"/>
										</a>
								</div>
								<%-- actual values in a dialog, hidden initially --%>
								<div id="editDialog_${insta_section_detail_id}_${field.field_id}" style="display: none; visibility: hidden">
									<div class="bd">
										<input type="hidden" name="allowNormal${insta_section_detail_id}_${field.field_id}"
											id="allowNormal${insta_section_detail_id}_${field.field_id}" value="${field.allow_normal}"/>
										<input type="hidden" name="allowOther${insta_section_detail_id}_${field.field_id}"
											id="allowNormal${insta_section_detail_id}_${field.field_id}" value="${field.allow_others}"/>
										<fieldset class="fieldSetBorder">
											<legend class="fieldSetLabel">${field.field_name}</legend>
											<table class="formtable">

												<%-- Normal checkbox --%>
												<c:if test="${field.allow_normal == 'Y'}">
													<tr>
														<td class="formlabel"><b><span id="${insta_section_detail_id}_option_value_${field.field_id}_0">Normal</span></b></td>
														<td>
															<input type="checkbox" name="${insta_section_detail_id}_field_${field.field_id}" value="0"
																${(not empty values) && (values[0].option_id == 0) ? 'checked' : ''}
																onclick="onClickNormal(${field.field_id}, '${insta_section_detail_id}')"/>
															<input type="text" id="${insta_section_detail_id}_option_remarks_${field.field_id}_0" name="${insta_section_detail_id}_option_remarks_${field.field_id}_0" 
																value='<c:out value="${(not empty values) && (values[0].option_id == 0) ? values[0].option_remarks : ''}"/>'
																${(not empty values) && (values[0].option_id == 0) ? '' : 'disabled'}/>
														</td>
													</tr>
												</c:if>
												<c:set var="index" value="0"/>
												<%-- List of checkboxes: one for each option allowed for this field --%>
												<c:forEach items="${fieldOptions[field.field_id]}" var="option">
													<c:set var="matches" value="0"/>
													<c:set var="option_remarks" value=""/>
													<c:forEach items="${values}" var="value">
														<c:if test="${value.option_id==option.option_id}">
															<c:set var="matches" value="${matches + 1}"/>
															<c:set var="option_remarks" value="${value.option_remarks}"/>
														</c:if>
													</c:forEach>
													<tr>
														<td class="formlabel"><span id="${insta_section_detail_id}_option_value_${field.field_id}_${option.option_id}">${option.option_value}</span></td>
														<td>
															<div style="float: left">
																<input type="checkbox" name="${insta_section_detail_id}_field_${field.field_id}"
																	value="${option.option_id}" ${matches gt 0 ? 'checked' : ''}
																	${(not empty values) && (values[0].option_id == 0) ? 'disabled' : ''}
																	onclick="onClickChkBox(this, ${field.field_id}, '${insta_section_detail_id}')"/>
																<input type="text" id="${insta_section_detail_id}_option_remarks_${field.field_id}_${option.option_id}" name="${insta_section_detail_id}_option_remarks_${field.field_id}_${option.option_id}"
																	value='<c:out value="${option_remarks}"/>' ${matches gt 0 ? '' : 'disabled'}/>
															</div>
															<c:if test="${option.pattern_id != null && not empty regexp[option.pattern_id] && empty option.option_phrase_category_id}" >
															      <img class="imgHelpText" src="${cpath}/images/help.png" title="${regexp[option.pattern_id]}"/>
														    </c:if>
															<c:if test="${not empty option.option_phrase_category_id}">
																<div class="multiInfoEditBtn" style="float: left; padding-top: 5px;margin-left: 8px">
																	<a href="javascript:void(0);"
																		onclick="return showPhraseEditDialog(this, '${insta_section_detail_id}', ${field.field_id}, ${option.option_id}, ${option.option_phrase_category_id});"
																			title="Select Values">
																		<img class="button" src="${cpath}/icons/openbook.png" />
																	</a>
																</div>
															</c:if>
														</td>
													</tr>
												</c:forEach>
												<%-- Others checkbox and input field --%>
												<c:if test="${field.allow_others == 'Y'}">
													<c:set var="matches" value="0"/>
													<c:set var="others_text" value=""/>
													<c:forEach items="${values}" var="value">
														<c:if test="${value.option_id==-1}">
															<c:set var="matches" value="${matches + 1}"/>
															<c:set var="others_text" value="${value.option_remarks}"/>
														</c:if>
													</c:forEach>
													<tr>
														<td class="formlabel">Others:</td>
														<td>
															<input type="checkbox" name="${insta_section_detail_id}_field_${field.field_id}" value="-1"
																${matches gt 0 ? 'checked' : ''}
																${(not empty values) && (values[0].option_id == 0) ? 'disabled' : ''}
																onclick="onClickChkBox(this, ${field.field_id}, '${insta_section_detail_id}')"/>
															<input type="text" id="${insta_section_detail_id}_option_remarks_${field.field_id}_-1" name="${insta_section_detail_id}_option_remarks_${field.field_id}_-1"
																value='<c:out value="${others_text}"/>' ${matches gt 0 ? '' : 'disabled'}/>
														</td>
													</tr>
												</c:if>
											</table>
										</fieldset>
										<table>
											<tr>
												<td><input type="button" onclick="updateChecboxFieldText(${field.field_id}, '${insta_section_detail_id}')" value="OK" /></td>
											</tr>
										</table>
									</div>
								</div>
							</c:when>

							<c:when test="${field.field_type == 'image'}">
								<c:set var="xy_values" value="${allFieldValues[insta_section_detail_id][field.field_id]}"/>
								<%-- option value '-5' is not inserted into the database, it is given just to save the x, y
									coordinates of the selected portion of the image. since empty option value elements are ignored.--%>
								<input type="hidden" name="${insta_section_detail_id}_field_${field.field_id}" value="-5"/>
								<input type="hidden" name="${insta_section_detail_id}_field_detail_id_${field.field_id}" value="${not empty xy_values ? xy_values[0].field_detail_id : ''}"/>
								<c:set var="image_id" value="${not empty xy_values ? xy_values[0].image_id : ''}"/>
								<input type="hidden" name="${insta_section_detail_id}_image_id_${field.field_id}" value="${image_id}"/>
								<div id="image_${insta_section_detail_id}_${field.field_id}" onclick="updateXY(event, '${insta_section_detail_id}', ${field.field_id}, '${param.form_name}');"
									style="width: 800px; height: 400px;
											background-image: url('${cpath}/master/SectionFields/ViewImage.do?_method=viewImage&image_id=${image_id}&field_id=${field.field_id}'); background-repeat:no-repeat;">
									<c:set var="numMarkers" value="${fn:length(xy_values)}"/>
									<%-- list of image markers placed on the main image. --%>
									<c:forEach begin="1" end="${numMarkers+1}" var="i" varStatus="loop">
										<c:set var="image" value="${xy_values[i-1]}"/>
										<c:if test="${empty image || (not empty image.marker_id && image.marker_id > 0)}">
											<div style="display: ${empty image ? 'none' : 'block'}; height: 0px" id="markerTemplateDiv_${insta_section_detail_id}_${field.field_id}"
												name="markerTemplateDiv_${insta_section_detail_id}_${field.field_id}" >
												<input type="hidden" name="${insta_section_detail_id}_marker_detail_id_${field.field_id}" id="${insta_section_detail_id}_marker_detail_id_${field.field_id}" 
													value="${image.marker_detail_id}"/>
												<input type="hidden" name="${insta_section_detail_id}_marker_id_${field.field_id}" id="${insta_section_detail_id}_marker_id_${field.field_id}"
													value="${image.marker_id}"/>
												<input type="hidden" name="${insta_section_detail_id}_coordinate_x_${field.field_id}" id="${insta_section_detail_id}_coordinate_x_${field.field_id}"
													value="${image.coordinate_x}"/>
												<input type="hidden" name="${insta_section_detail_id}_coordinate_y_${field.field_id}" id="${insta_section_detail_id}_coordinate_y_${field.field_id}"
													value="${image.coordinate_y}"/>
												<input type="hidden" name="${insta_section_detail_id}_notes_${field.field_id}" value="${image.notes}">
												<c:set var="markerUrl" value="${cpath}/master/ImageMarkers/ViewImage.do?_method=view&image_id=${image.marker_id}"/>
												<img class="deleteMarkerClass" src="${not empty image ? markerUrl : ''}" name="cross_${insta_section_detail_id}_${field.field_id}" id="cross_${insta_section_detail_id}_${field.field_id}"
													style="top: ${not empty image.coordinate_y ? image.coordinate_y : 0}px;
														left: ${not empty image.coordinate_x ? image.coordinate_x: 0}px; position:relative;display:block;z-index:2;"
													onclick="deleteMarker(this, '${insta_section_detail_id}', ${field.field_id})" title="${image.notes}"/>
											</div>
										</c:if>
									</c:forEach>
								</div>
								<div style="clear: both"></div>
								<%-- display list of image markers, delete marker and show marker labels fields
									1) markers without labels
									2) markers with labels.
								--%>
								<div id="image_markers_${insta_section_detail_id}_${field.field_id}" style="width: 800px; display:${ stn_right_access ? 'block' : 'none'}">
									<div style="float:right; margin-left: 10px; font-weight: normal">
										<label style="float:left" for="chkbox_toggle_mrkr_tables_${insta_section_detail_id}_${field.field_id}">Show Marker Labels:</label>
										<div style="float:right; margin-top: -3px">
											<input type="checkbox" name="chkbox_toggle_mrkr_tables_${insta_section_detail_id}_${field.field_id}"
												id="chkbox_toggle_mrkr_tables_${insta_section_detail_id}_${field.field_id}" onclick="toggleMarkerTables('${insta_section_detail_id}', ${field.field_id})"/>
										</div>
									</div>
									<%-- margin top and bottom are not applying on the label element(Delete Marker label) hence made it 0px and used padding-bottom on div element. --%>
									<div style="float:right; font-weight: normal;border: 1px outset;height: 16px;padding-top:4px " onclick="toggleMarkerDelete(this, '${insta_section_detail_id}', ${field.field_id});">
										<label style="margin: 0px 5px 0px 5px" id="delete_marker_label_${insta_section_detail_id}_${field.field_id}">Delete Marker</label>
										<input type="hidden" name="hidden_delete_marker_${insta_section_detail_id}_${field.field_id}" id="hidden_delete_marker_${insta_section_detail_id}_${field.field_id}" value="false"/>
									</div>
									<div style="clear: both"></div>
									<input type="hidden" name="marker_selected_${insta_section_detail_id}_${field.field_id}" id="marker_selected_${insta_section_detail_id}_${field.field_id}" value=""/>
									<table class="marker_with_label" style="display: none; margin-top: 10px;" id="mrkr_with_lbl_table_${insta_section_detail_id}_${field.field_id}">
										<tr>
											<c:forEach items="${imageMarkers[field.field_id]}" var="marker" varStatus="st">
												<c:if test="${st.index > 0 && (st.index%3) == 0}">
													<c:out value="</tr><tr>" escapeXml="false"/>
												</c:if>
													<td class="img">
														<img title="${marker.label}" name="mrkr_img_with_lbl_${insta_section_detail_id}_${field.field_id}" id="mrkr_img_with_lbl_${insta_section_detail_id}_${field.field_id}_${marker.marker_id}"
															onclick="markerSelected(${insta_section_detail_id}, ${field.field_id}, ${marker.marker_id})"
															src="${cpath}/master/ImageMarkers/ViewImage.do?_method=view&image_id=${marker.marker_id}"/>
													</td>
													<td class="label">
														<label for="mrkr_img_with_lbl_${insta_section_detail_id}_${field.field_id}_${marker.marker_id}"
															onclick="markerSelected('${insta_section_detail_id}', ${field.field_id}, ${marker.marker_id})">${marker.label}</label>
													</td>
													<c:if test="${st.last && (st.index%3) != 0}">
													<c:forEach var="empty-cells" begin="2" end="${3-(st.index%3)}">
														<td class="img"></td>
														<td class="label"></td>
													</c:forEach>
												</c:if>
											</c:forEach>
										</tr>
									</table>
									<table class="marker_without_label" style="display: block; margin-top: 10px;" id="mrkr_without_lbl_table_${insta_section_detail_id}_${field.field_id}">
										<tr>
											<c:forEach items="${imageMarkers[field.field_id]}" var="marker" varStatus="st">
												<c:if test="${st.index > 0 && (st.index%10) == 0}">
													<c:out value="</tr><tr>" escapeXml="false"/>
												</c:if>
													<td style="text-align: center">
														<img title="${marker.label}" name="mrkr_img_without_lbl_${insta_section_detail_id}_${field.field_id}" id="mrkr_img_without_lbl_${insta_section_detail_id}_${field.field_id}_${marker.marker_id}"
															onclick="markerSelected('${insta_section_detail_id}', ${field.field_id}, ${marker.marker_id})"
															src="${cpath}/master/ImageMarkers/ViewImage.do?_method=view&image_id=${marker.marker_id}"/>
													</td>
												<c:if test="${st.last && (st.index%10) != 0}">
													<!-- st.index starts from 0 so begin should start from 2 -->
													<c:forEach var="empty-cells" begin="2" end="${10-(st.index%10)}">
														<td></td>
													</c:forEach>
												</c:if>
											</c:forEach>
										</tr>
									</table>
								</div>
							</c:when>
						</c:choose>
					</td>
					<c:if test="${st.last && (!showInEntireRow) && (colsPerRow%3 == 0)}">
						<td class="formlabel">&nbsp;</td>
						<td></td>
						<td class="formlabel">&nbsp;</td>
						<td></td>
					</c:if>
					<c:if test="${st.last && (!showInEntireRow) && (colsPerRow%3 == 1)}">
						<td class="formlabel"></td>
						<td></td>
					</c:if>
					<c:set var="colsPerRow" value="${showInEntireRow ? colsPerRow : colsPerRow+1}"/>
				</c:forEach>
				</tr>
		</table>
		<c:if test="${sectionDesc.allow_all_normal == 'Y' && sectionDesc.status == 'A' && stn_right_access}">
			<div style="margin-top: 5px;" id="allow_all_normal_div${insta_section_detail_id}">
				<input type="button" onclick="allNormal('${insta_section_id}','${insta_section_detail_id}')" value="All other systems normal"/>
			</div>
		</c:if>
		</span>
		<c:if test="${isd_id_c.index == 0}">
			<table style="width:100%; white-space:nowrap;">
				<tr>
				<td style="width:100%;"></td>
				<c:if test="${sectionDesc.allow_duplicate && stn_right_access}">
					<td style="padding-left:10px;padding-right:10px;"><span class="newsection"  onclick="addNewSection(${insta_section_id})"><a>Add New Section</a></span> </td>
				</c:if>
				<td style="padding-right:5px;">Finalize</td>
				<td> <span><input class="finalize" id="${insta_section_id}_finalized"  type="checkbox" value="true" ${ finalized == 'Y' ? "checked" : "" } onclick="changeFinalized(this,${insta_section_id});"  
																								${finalized == 'Y' ? ((roleId == 1 || roleId == 2 || actionRightsMap.undo_section_finalization == 'A') ? '' : 'disabled') : 
																								((roleId == 1 || roleId == 2 || is_stn_in_list) ? '' : 'disabled')}></input>
																								<input type="hidden" value="${finalized}" name="${insta_section_id}_finalized" ></input></span></td>
				<tr>
			</table>
		</c:if>
	</fieldset>
</c:forEach>
<span id="newSectionArea_${insta_section_id}"></span>

<c:if test="${sectionDesc.allow_duplicate && stn_right_access}">
<!-- Insta Section Template For Duplications -->
<fieldset class="fieldSetBorder" id="section_${insta_section_id}_new" style="display:none">
	<input type="hidden" name="section_detail_ids_${insta_section_id}" value=""/>
	<legend class="fieldSetLabel">
		<span id="section_title_${insta_section_id}_new">${sectionDesc.section_title}</span>
		<c:if test="${sectionDesc.section_mandatory || is_flds_in_stn_mandatory}">
			<span class="mandatoryFieldColor">*</span>
		</c:if>
		<input class="togglebutton" type="button" id="insta_section_btn_${insta_section_id}_new"
			onclick="toggleInstaSection('${insta_section_id}','${insta_section_id}_new', this);"
			value="-"/>
		<!-- look into toggleInstaSection() -->
	</legend>
	<span id="${insta_section_id}_new_minText"></span>
	<span id="section_detail_view_${insta_section_id}_new">
	<table class="formtable" id="insta_section_table${insta_section_id}_new">
		<tr>
			<c:set var="colsPerRow" value="0"/>
			<c:forEach items="${fieldDescs}" var="field" varStatus="st">
				<c:set var="showInEntireRow" value="false"/>
				<c:if test="${colsPerRow > 0 && (colsPerRow % 3) == 0}"> <!-- Constraints to 3 fields in a row -->
					<c:out value="</tr><tr>" escapeXml="false"/>
				</c:if>
				<c:if test="${field.field_type == 'image' || field.field_type == 'wide text'}">
					<%-- if the field type is a image or wide text, then display it in a next row --%>
					<c:if test="${colsPerRow%3 != 0}">
						<c:forEach var="empty-cells" begin="1" end="${3-(colsPerRow%3)}">
							<td class="formlabel">&nbsp;</td>
							<td >&nbsp;</td>
						</c:forEach>
						<c:out value="</tr><tr>" escapeXml="false"/>
					</c:if>
					<c:set var="colsPerRow" value="${colsPerRow + (3-(colsPerRow%3))}"/>
					<c:set var="showInEntireRow" value="true"/>
				</c:if>
				<td class="formlabel">
					<span id="field_title_${insta_section_id}_new_${field.field_id}">${field.field_name}</span>
					<c:if test="${field.is_mandatory}">
						<span class="mandatoryFieldColor">*</span>
					</c:if>
					:
				</td>
				<td class="forminfo" colspan="${showInEntireRow ? 5 : 1}">
					<input type="hidden" name="${insta_section_id}_new_field_detail_id_${field.field_id}" value=""/>
					<c:choose>

						<c:when test="${field.field_type == 'text' || field.field_type == 'wide text'}">
							<%-- <c:set var="value" value="${fieldValues[field.field_id][0]}"/> --%>
							<div style="float: left">
								<textarea class="${field.field_type == 'wide text' ? 'wideTextArea' : 'normalTextArea'}"
										style="resize: none;"
										name="${insta_section_id}_new_option_remarks_${field.field_id}" rows="${field.no_of_lines}"
										onmouseup="adjustTextArea('${insta_section_id}', '${patient.mr_no}');" ><c:out value="${field.normal_text}"/></textarea>
								<c:if test="${field.pattern_id != null && not empty regexp[field.pattern_id] && empty field.field_phrase_category_id}" >
									<img class="imgHelpText" src="${cpath}/images/help.png" title="${regexp[field.pattern_id]}"/>
								</c:if>
							</div>
							<c:if test="${not empty field.field_phrase_category_id}">
								<div class="multiInfoEditBtn" style="float: left; margin-left: 8px">
									<a href="javascript:void(0);" id="pharse_${insta_section_id}_new_${field.field_id}"
										onclick="return showPhraseEditDialog(this,
										'${insta_section_id}_new', ${field.field_id}, null, ${field.field_phrase_category_id});"
										title="Select Values">
										<img class="button" src="${cpath}/icons/openbook.png" />
									</a>
								</div>
							</c:if>
							<!-- option id is set for text field as -2 instead of null -->
							<input type="hidden" name="${insta_section_id}_new_field_${field.field_id}" value="-2"/>
						</c:when>
	
						<c:when test="${field.field_type == 'date' || field.field_type == 'datetime'}">
							<c:if test="${field.field_type == 'date'}">
								<!-- option id is set for date field as -3 instead of null -->
								<input type="hidden" name="${insta_section_id}_new_field_${field.field_id}" value="-3"/>
							</c:if>
							<div>
								<span id="datewidget_${insta_section_id}_new_date_${field.field_id}"></span>
								<c:if test="${field.field_type == 'datetime'}">
									<input type="text" style="width: 40px" name="${insta_section_id}_new_time_${field.field_id}" 
										id="${insta_section_id}_new_time_${field.field_id}" />
									<!-- option id is set for date time field as -4 instead of null -->
									<input type="hidden" name="${insta_section_id}_new_field_${field.field_id}" value="-4"/>
								</c:if>
							</div>
							<input type="hidden" id="${insta_section_id}_${field.field_id}_default_to_current_datetime" value="${field.default_to_current_datetime}" />
						</c:when>

						<c:when test="${field.field_type == 'dropdown'}">
							<select name="${insta_section_id}_new_field_${field.field_id}" class="dropdown" style="font-weight: bold"
									onchange="onChangeOption(${field.field_id}, '${insta_section_id}_new')">
								<option value="" selected></option>
								<c:if test="${field.allow_normal == 'Y'}">
									<option value="0" >Normal</option>
								</c:if>
								<c:forEach items="${fieldOptions[field.field_id]}" var="option">
									<option value="${option.option_id}" >
										<c:out value="${option.option_value}"/>
									</option>
								</c:forEach>
								<c:if test="${field.allow_others == 'Y'}">
									<!-- option id is set for date time field as -1 instead of null -->
									<option value="-1" >Others</option>
								</c:if>
							</select>
							<c:if test="${field.allow_others == 'Y'}">
								<input type="text" name="${insta_section_id}_new_option_remarks_${field.field_id}" value=""
									disabled/>
							</c:if>
						</c:when>

						<c:when test="${field.field_type == 'checkbox'}">
							<%-- text to display --%>
							<div id="fieldText_${insta_section_id}_new_${field.field_id}" class="checkboxText">
								 --
							</div>
							<%-- edit icon --%>
							<div class="multiInfoEditBtn" style="float: left">
								<a href="javascript:void(0);" id="checkboxEditBtn_${insta_section_id}_new_${field.field_id}" onclick="return showPysicianFormEditDialog('${insta_section_id}_new',${field.field_id});"
										title="Select Values">
									<img src="${cpath}/icons/Edit.png" class="button"/>
								</a>
							</div>
							<%-- actual values in a dialog, hidden initially --%>
							<div id="editDialog_${insta_section_id}_new_${field.field_id}" style="display: none; visibility: hidden">
								<div class="bd">
									<input type="hidden" name="allowNormal${insta_section_id}_new_${field.field_id}"
										id="allowNormal${insta_section_id}_new_${field.field_id}" value="${field.allow_normal}"/>
									<input type="hidden" name="allowOther${insta_section_id}_new_${field.field_id}"
										id="allowNormal${insta_section_id}_new_${field.field_id}" value="${field.allow_others}"/>
									<fieldset class="fieldSetBorder">
										<legend class="fieldSetLabel">${field.field_name}</legend>
										<table class="formtable">

											<%-- Normal checkbox --%>
											<c:if test="${field.allow_normal == 'Y'}">
												<tr>
													<td class="formlabel"><b><span id="${insta_section_id}_new_option_value_${field.field_id}_0">Normal</span></b></td>
													<td>
														<input type="checkbox" name="${insta_section_id}_new_field_${field.field_id}" value="0"
															onclick="onClickNormal(${field.field_id}, '${insta_section_id}_new')"/>
														<input type="text" id="${insta_section_id}_new_option_remarks_${field.field_id}_0" name="${insta_section_id}_new_option_remarks_${field.field_id}_0" 
															value='' disabled/>
													</td>
												</tr>
											</c:if>
											<c:set var="index" value="0"/>
											<%-- List of checkboxes: one for each option allowed for this field --%>
											<c:forEach items="${fieldOptions[field.field_id]}" var="option">
												<tr>
													<td class="formlabel">
														<span id="${insta_section_id}_new_option_value_${field.field_id}_${option.option_id}">${option.option_value}</span>
													</td>
													<td>
														<div style="float: left">
															<input type="checkbox" name="${insta_section_id}_new_field_${field.field_id}"
																value="${option.option_id}" 
																onclick="onClickChkBox(this, ${field.field_id}, '${insta_section_id}_new')"/>
															<input type="text" id="${insta_section_id}_new_option_remarks_${field.field_id}_${option.option_id}"
																name="${insta_section_id}_new_option_remarks_${field.field_id}_${option.option_id}" disabled/>
														</div>
														<c:if test="${option.pattern_id != null && not empty regexp[option.pattern_id] && empty option.option_phrase_category_id}" >
														      <img class="imgHelpText" src="${cpath}/images/help.png" title="${regexp[option.pattern_id]}"/>
													    </c:if>
														<c:if test="${not empty option.option_phrase_category_id}">
															<div class="multiInfoEditBtn" style="float: left; padding-top: 5px;margin-left: 8px">
																<a href="javascript:void(0);" id="phrase_${insta_section_id}_new_${field.field_id}_${option.option_id}"
																	onclick="return showPhraseEditDialog(this, '${insta_section_id}_new', ${field.field_id},
																	${option.option_id}, ${option.option_phrase_category_id});"
																	title="Select Values">
																		<img class="button" src="${cpath}/icons/openbook.png" />
																</a>
															</div>
														</c:if>
													</td>
												</tr>
											</c:forEach>
											<%-- Others checkbox and input field --%>
											<c:if test="${field.allow_others == 'Y'}">
												<tr>
													<td class="formlabel"><span id="${insta_section_id}_new_option_value_${field.field_id}_-1">Others:</span></td>
													<td>
														<input type="checkbox" name="${insta_section_id}_new_field_${field.field_id}" value="-1"
															onclick="onClickChkBox(this, ${field.field_id}, '${insta_section_id}_new')" />
														<input type="text" id="${insta_section_id}_new_option_remarks_${field.field_id}_-1"
															name="${insta_section_id}_new_option_remarks_${field.field_id}_-1"/>
													</td>
												</tr>
											</c:if>
										</table>
									</fieldset>
									<table>
										<tr>
											<td><input type="button" id="btn_editDialog_${insta_section_id}_new_${field.field_id}"
												onclick="updateChecboxFieldText(${field.field_id}, '${insta_section_id}_new')" value="OK" />
											</td>
										</tr>
									</table>
								</div>
							</div>
						</c:when>

						<c:when test="${field.field_type == 'image'}">
								<%-- option value '-6' is not inserted into the database, it is given just to save the x, y
									coordinates of the selected portion of the image. since empty option value elements are ignored.--%>
								<input type="hidden" name="${insta_section_id}_new_field_${field.field_id}" value="-6"/>
								<input type="hidden" name="${insta_section_id}_new_image_id_${field.field_id}" value=""/>
								<div id="image_${insta_section_id}_new_${field.field_id}" onclick="updateXY(event, '${insta_section_id}_new', ${field.field_id}, '${param.form_name}');"
									style="width: 800px; height: 400px;
											background-image: url('${cpath}/master/SectionFields/ViewImage.do?_method=viewImage&field_id=${field.field_id}'); background-repeat:no-repeat;">
										<div style="display: none; height: 0px" id="markerTemplateDiv_${insta_section_id}_new_${field.field_id}"
											name="markerTemplateDiv_${insta_section_id}_new_${field.field_id}" >
											<input type="hidden" name="${insta_section_id}_new_marker_detail_id_${field.field_id}" id="${insta_section_id}_new_marker_detail_id_${field.field_id}"
												value=""/>
											<input type="hidden" name="${insta_section_id}_new_marker_id_${field.field_id}" id="${insta_section_id}_new_marker_id_${field.field_id}"
												value=""/>
											<input type="hidden" name="${insta_section_id}_new_coordinate_x_${field.field_id}" id="${insta_section_id}_new_coordinate_x_${field.field_id}"
												value=""/>
											<input type="hidden" name="${insta_section_id}_new_coordinate_y_${field.field_id}" id="${insta_section_id}_new_coordinate_y_${field.field_id}"
												value=""/>
											<input type="hidden" name="${insta_section_id}_new_notes_${field.field_id}" value="${image.notes}">
											<img class="deleteMarkerClass" src="" name="cross_${insta_section_id}_new_${field.field_id}" id="cross_${insta_section_id}_new_${field.field_id}"
												style="top: 0px; left: 0px; position:relative; display:block; z-index:2;"
												onclick="deleteMarker(this, '${insta_section_id}_new', ${field.field_id})" title=""/>
											</div>
								</div>
								<div style="clear: both"></div>
								<%-- display list of image markers, delete marker and show marker labels fields
									1) markers without labels
									2) markers with labels.
								--%>
								<div id="image_markers_${insta_section_id}_new_${field.field_id}" style="width: 800px">
									<div style="float:right; margin-left: 10px; font-weight: normal">
										<label style="float:left" for="chkbox_toggle_mrkr_tables_${insta_section_id}_new_${field.field_id}">Show Marker Labels:</label>
										<div style="float:right; margin-top: -3px">
											<input type="checkbox" name="chkbox_toggle_mrkr_tables_${insta_section_id}_new_${field.field_id}"
												id="chkbox_toggle_mrkr_tables_${insta_section_id}_new_${field.field_id}" onclick="toggleMarkerTables('${insta_section_id}_new', ${field.field_id})"/>
										</div>
									</div>
									<%-- margin top and bottom are not applying on the label element(Delete Marker label) hence made it 0px and used padding-bottom on div element. --%>
									<div style="float:right; font-weight: normal;border: 1px outset;height: 16px;padding-top:4px "
										id="toggle_marker_delete_${insta_section_id}_new_${field.field_id}"
										onclick="toggleMarkerDelete(this, '${insta_section_id}_new', ${field.field_id});">
											<label style="margin: 0px 5px 0px 5px" id="delete_marker_label_${insta_section_id}_new_${field.field_id}">Delete Marker</label>
											<input type="hidden" name="hidden_delete_marker_${insta_section_id}_new_${field.field_id}" id="hidden_delete_marker_${insta_section_id}_new_${field.field_id}" value="false"/>
									</div>
									<div style="clear: both"></div>
									<input type="hidden" name="marker_selected_${insta_section_id}_new_${field.field_id}" id="marker_selected_${insta_section_id}_new_${field.field_id}" value=""/>
									<table class="marker_with_label" style="display: none; margin-top: 10px;" id="mrkr_with_lbl_table_${insta_section_id}_new_${field.field_id}">
										<tr>
											<c:forEach items="${imageMarkers[field.field_id]}" var="marker" varStatus="st">
												<c:if test="${st.index > 0 && (st.index%3) == 0}">
													<c:out value="</tr><tr>" escapeXml="false"/>
												</c:if>
													<td class="img">
														<img title="${marker.label}" name="mrkr_img_with_lbl_${insta_section_id}_new_${field.field_id}" id="mrkr_img_with_lbl_${insta_section_id}_new_${field.field_id}_${marker.marker_id}"
															onclick="markerSelected('${insta_section_id}_new', ${field.field_id}, ${marker.marker_id})"
															src="${cpath}/master/ImageMarkers/ViewImage.do?_method=view&image_id=${marker.marker_id}"/>
													</td>
													<td class="label">
														<label for="mrkr_img_with_lbl_${insta_section_id}_new_${field.field_id}_${marker.marker_id}"
															id="mrkr_img_lbl_${insta_section_id}_new_${field.field_id}_${marker.marker_id}"
															onclick="markerSelected('${insta_section_id}_new', ${field.field_id}, ${marker.marker_id})">${marker.label}</label>
													</td>
													<c:if test="${st.last && (st.index%3) != 0}">
													<c:forEach var="empty-cells" begin="2" end="${3-(st.index%3)}">
														<td class="img"></td>
														<td class="label"></td>
													</c:forEach>
												</c:if>
											</c:forEach>
										</tr>
									</table>
									<table class="marker_without_label" style="display: block; margin-top: 10px;" id="mrkr_without_lbl_table_${insta_section_id}_new_${field.field_id}">
										<tr>
											<c:forEach items="${imageMarkers[field.field_id]}" var="marker" varStatus="st">
												<c:if test="${st.index > 0 && (st.index%10) == 0}">
													<c:out value="</tr><tr>" escapeXml="false"/>
												</c:if>
													<td style="text-align: center">
														<img title="${marker.label}" name="mrkr_img_without_lbl_${insta_section_id}_new_${field.field_id}" id="mrkr_img_without_lbl_${insta_section_id}_new_${field.field_id}_${marker.marker_id}"
															onclick="markerSelected('${insta_section_id}_new', ${field.field_id}, ${marker.marker_id})"
															src="${cpath}/master/ImageMarkers/ViewImage.do?_method=view&image_id=${marker.marker_id}"/>
													</td>
												<c:if test="${st.last && (st.index%10) != 0}">
													<!-- st.index starts from 0 so begin should start from 2 -->
													<c:forEach var="empty-cells" begin="2" end="${10-(st.index%10)}">
														<td></td>
													</c:forEach>
												</c:if>
											</c:forEach>
										</tr>
									</table>
								</div>
							</c:when>

					</c:choose>
				</td>
				<c:if test="${st.last && (!showInEntireRow) && (colsPerRow%3 == 0)}">
					<td class="formlabel">&nbsp;</td>
					<td></td>
					<td class="formlabel">&nbsp;</td>
					<td></td>
				</c:if>
				<c:if test="${st.last && (!showInEntireRow) && (colsPerRow%3 == 1)}">
					<td class="formlabel"></td>
					<td></td>
				</c:if>
					<c:set var="colsPerRow" value="${showInEntireRow ? colsPerRow : colsPerRow+1}"/>
			</c:forEach>
			</tr>
	</table>
	<span id="remove_${insta_section_id}_new" class="newsection" onclick="removeSection(${insta_section_id}, 1)"><a>Remove Section</a></span>
	<c:if test="${sectionDesc.allow_all_normal == 'Y' && sectionDesc.status == 'A'}">
			<div style="margin-top: 5px;" id="allow_all_normal_div${insta_section_id}_new">
				<input type="button" onclick="allNormal('${insta_section_id}','${insta_section_id}_new')" value="All other systems normal"/>
			</div>
	</c:if>
	</span>
</fieldset>
</c:if>
