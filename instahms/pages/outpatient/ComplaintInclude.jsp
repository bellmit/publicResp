<%
SystemGeneratedSectionsDAO formsDAO = new SystemGeneratedSectionsDAO();
int formId = Integer.parseInt(request.getParameter("section_id"));
request.setAttribute("bean", formsDAO.findByKey("section_id", formId));
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
</head>

<%-- this page is being used in consulation and triage screens. --%>
<input type="hidden" name="formName" id="formName" value="${ifn:cleanHtmlAttribute(param.form_name)}"/>
<input type="hidden" name="formId" id="formId" value="${ifn:cleanHtmlAttribute(param.form_id)}"/>
<input type="hidden" name="field_ph_cat_id" id="field_ph_cat_id" value="${bean.map.field_phrase_category_id}"/>
<input type="hidden" name="cpath" id="cpath" value="${cpath}"/>


<fieldset class="fieldSetBorder" style="margin-top: 10px">
	<legend class="fieldSetLabel">Complaints
		<c:if test="${bean.map.section_mandatory}">
			<span class="star">*</span>
		</c:if>
	</legend>
	<table class="formtable" id="complaintsTable">
		<tr>
			<td class="formlabel">Chief Complaint:</td>
			<td colspan="3">
				<c:set var="textBoxId" value="complaint"/>
				<input type="hidden" name="textBoxId" id="textBoxId" value="${textBoxId}"/>

					<input type="text" id="complaint" name="complaint" ${status == 'C' ?'readOnly':''}
						value="${consultation_bean.complaint}" style="width: 520px"/>
			</td>
			<td>
				<c:set var="null_check" value=""/>
				<c:if test="${not empty bean.map.field_phrase_category_id}">
				<div class="multiInfoEditBtn">
					<a href="javascript:void(0);"
						onclick="return showPhraseComplaintDialog(this, '${ifn:cleanJavaScript(param.form_name)}', '${textBoxId}',
						'${bean.map.field_phrase_category_id}');"
						title="Select Values">
						<img class="button" src="${cpath}/icons/openbook.png"/>
					</a>
				</div>
				</c:if>
			</td>
				<td style="text-align:center;width: 70px">
					<button type="button" onclick="javascript:addSecondaryComplaint(this);">+</button>
				</td>
		</tr>
		<c:forEach items="${secondary_complaints}" var="s_complaint" varStatus="st">
			<tr>
				<td class="formlabel">Other Complaint: </td>
				<td colspan="3">
					<input type="hidden" name="s_complaint_row_id" value="${s_complaint.map.row_id}"/>
						<input type="text" id="s_complaint_${st.index}" name="s_complaint" ${status == 'C' ?'readOnly':''}
							value="${s_complaint.map.complaint}" style="width: 520px"/>
				</td>
				<td>
					<c:if test="${not empty bean.map.field_phrase_category_id}">
						<div class="multiInfoEditBtn">
							<a href="javascript:void(0);"
								onclick="return showPhraseComplaintDialog(this, '${ifn:cleanJavaScript(param.form_name)}', 's_complaint_${st.index}',
								'${bean.map.field_phrase_category_id}');"
								title="Select Values">
								<img class="button" src="${cpath}/icons/openbook.png"/>
							</a>
						</div>
					</c:if>
				</td>
			</tr>
		</c:forEach>
	</table>
	<table class="formtable">
		<tr>
			<td style="text-align: right;">
				<c:choose>
					<c:when test="${screenId eq 'triage_form' }">
					<td style="text-align: right"><insta:screenlink screenId="complaints_tri_audit_log" label="Complaints Audit Log"
								  addPipe="false" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=patient_complaints_audit_log_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.consultation_id}"/></td>
					</c:when>

					<c:when test="${screenId eq 'op_prescribe' }">
						<td style="text-align: right"><insta:screenlink screenId="complaints_cons_audit_log" label="Complaints Audit Log"
								  addPipe="false" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=patient_complaints_audit_log_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.consultation_id}"/></td>
					</c:when>

					<c:when test="${screenId eq 'visit_summary' }">
						<td style="text-align: right"><insta:screenlink screenId="complaints_ip_audit_log" label="Complaints Audit Log"
								  addPipe="false" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=patient_complaints_audit_log_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.consultation_id}"/></td>
					</c:when>

					<c:when test="${screenId eq 'patient_generic_form_list' }">
						<td style="text-align: right"><insta:screenlink screenId="complaints_gen_audit_log" label="Complaints Audit Log"
								  addPipe="false" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=patient_complaints_audit_log_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.consultation_id}"/></td>
					</c:when>

					<c:when test="${screenId eq 'ot_record' }">
						<td style="text-align: right"><insta:screenlink screenId="complaints_surgery_audit_log" label="Complaints Audit Log"
								  addPipe="false" target="_blank"  extraParam="?_method=getAuditLogDetails&al_table=patient_complaints_audit_log_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.consultation_id}"/></td>
					</c:when>
				</c:choose>
			</td>
		</tr>
	</table>
</fieldset>
