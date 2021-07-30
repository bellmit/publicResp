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
	<legend class="fieldSetLabel">Complaints</legend>
	<table class="formtable" id="complaintsTable">
		<tr>
			<td class="formlabel">Chief Complaint:</td>
			<td colspan="3">
				<c:set var="textBoxId" value="complaint"/>
				<input type="hidden" name="textBoxId" id="textBoxId" value="${textBoxId}"/>

					<input type="text" id="complaint" name="complaint" ${patient.op_type != 'O'?'readOnly':''}
						value="${preauthPrescBean.map.complaint}" style="width: 520px"/>
			</td>
			<c:if test="${patient.op_type == 'O'}">
			<td>
				<c:set var="null_check" value=""/>
				<c:if test="${bean.map.field_phrase_category_id != null_check && bean.map.field_phrase_category_id != null}">
				<div class="multiInfoEditBtn">
					<a href="javascript:void(0);"
						onclick="return showPhraseComplaintDialog(this, '${ifn:cleanJavaScript(param.form_name)}', '${textBoxId}',
						${bean.map.field_phrase_category_id});"	title="Select Values">
						<img class="button" src="${cpath}/icons/openbook.png"/>
					</a>
				</div>
				</c:if>
			</td>
			<td style="text-align:center;width: 70px">
				<button type="button" onclick="javascript:addSecondaryComplaint(this)">+</button>
			</td>
			</c:if>

		</tr>
		<c:forEach items="${secondary_complaints}" var="s_complaint" varStatus="st">
			<tr>
				<td class="formlabel">Other Complaint: </td>
				<td colspan="3">
					<input type="hidden" name="s_complaint_row_id" value="${s_complaint.map.row_id}"/>
						<input type="text" id="s_complaint_${st.index}" name="s_complaint" ${patient.op_type != 'O'?'readOnly':''}
							value="${s_complaint.map.complaint}" style="width: 452px"/>
				</td>
			</tr>
		</c:forEach>
	</table>
</fieldset>
