<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Add/Edit Template - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="masters/GenericDocumentTemplate.js"/>
	<insta:link type="js" file="tiny_mce/tiny_mce.js" />
	<insta:link type="js" file="editor.js" />

	<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<script>
		/* initialize the tinyMCE editor: todo: font and size to be customizable */
		initEditor("template_content", '${cpath}', 'sans-serif', 12);

	</script>

</head>

<body>
	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Rich Text Template</h1>
	<insta:feedback-panel/>

	<jsp:useBean id="actionUrlMap" class="java.util.HashMap"/>
	<c:set target="${actionUrlMap}" property="mlc" value="MLCTemplate.do"/>
	<c:set target="${actionUrlMap}" property="service" value="ServiceTemplate.do"/>
	<c:set target="${actionUrlMap}" property="reg" value="RegistrationTemplate.do"/>
	<c:set target="${actionUrlMap}" property="insurance" value="InsuranceTemplate.do"/>
	<c:set target="${actionUrlMap}" property="dietary" value="DietaryTemplate.do"/>
	<c:set target="${actionUrlMap}" property="op_case_form_template" value="OpFormTemplate.do"/>
	<c:set target="${actionUrlMap}" property="ot" value="OTTemplate.do"/>

	<jsp:useBean id="docTypeMap" class="java.util.HashMap"/>
	<c:set target="${docTypeMap}" property="mlc" value="4"/>
	<c:set target="${docTypeMap}" property="service" value="SYS_ST"/>
	<c:set target="${docTypeMap}" property="reg" value="SYS_RG"/>
	<c:set target="${docTypeMap}" property="insurance" value="SYS_INS"/>
	<c:set target="${docTypeMap}" property="dietary" value="SYS_DIE"/>
	<c:set target="${docTypeMap}" property="op_case_form_template" value="SYS_OP"/>
	<c:set target="${docTypeMap}" property="ot" value="SYS_OT"/>

	<c:set var="actionUrl" value="GenericDocumentTemplate.do"/>
	<c:set var="doc_type" value="" />
	<c:if test="${specialized}">
		<c:set var="actionUrl" value="${actionUrlMap[documentType]}"/>
		<c:set var="doc_type" value="${docTypeMap[documentType]}"/>
	</c:if>

	<form action="${actionUrl}" method="POST">
		<input type="hidden" name="_method" value="${param._method == 'add'?'create':'update'}"/>
		<input type="hidden" name="specialized" id="specialized" value="${specialized}"/>
		<input type="hidden" name="format" id="format" value="${ifn:cleanHtmlAttribute(param.format)}"/>
		<input type="hidden" name="template_id" id="template_id" value="${template_details.map.template_id}"/>
		<input type="hidden" name="consultation_id" id="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}"/>
		<fieldset class="fieldSetBorder">
			<table class="formtable" >
				<tr >
					<td class="formlabel">Template Name: </td>
					<td ><input type="text" name="template_name" id="template_name" class="required validate-length " length="100" title="Template Name is mandatory and should not exceed 100 characters." value="${template_details.map.template_name}"/></td>
					<td class="formlabel">&nbsp;</td>
					<td>&nbsp;</td>
					<td class="formlabel">&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr >
					<td class="formlabel">Title: </td>
					<td ><input type="text" name="title" id="title" value="${template_details.map.title}" class="validate-length " length="100" title="Title should not exceed 100 characters."/></td>
				</tr>
				<c:if test="${displayDept}">
					<tr>
						<td class="formlabel">Department: </td>
						<td><insta:selectdb name="dept_name" table="department" valuecol="dept_id" displaycol="dept_name" value="${template_details.map.dept_name}"
								dummyvalue="--select--" class="${documentType == 'op_case_form_template' ? 'validate-not-empty' : ''} dropdown" filtered="false" title="Department Name is mandatory."/></td>
					</tr>
				</c:if>
				<c:choose>
					<c:when test="${specialized}">
						<input type="hidden" name="doc_type" value="${doc_type}"/>
					</c:when>
					<c:otherwise>
						<tr>
							<td class="formlabel">Document Type: </td>
							<td ><insta:selectdb name="doc_type" table="doc_type" valuecol="doc_type_id" displaycol="doc_type_name" value="${template_details.map.doc_type}"
															dummyvalue="----select----" filtered="true" filtercol="status" class="validate-not-empty" title="Document Type is mandatory."/></td>
						</tr>
					</c:otherwise>
				</c:choose>
				<tr>
					<td class="formlabel">Patient Header Template: </td>
					<td >
						<c:set var="phTemplateId" value="${template_details.map.pheader_template_id}"/>
						<select name="pheader_template_id" id="pheader_template_id" class="dropdown">
							<option value="" ${empty phTemplateId?'selected':''}>None</option>
							<option value="0" ${phTemplateId == 0?'selected':''}>System Default</option>
							<c:forEach var="phTemplate" items="${phTemplates}">
								<option value="${phTemplate.map.template_id}" ${phTemplateId == phTemplate.map.template_id?'selected':''}>${phTemplate.map.template_name}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<c:if test="${displayAccessRights}">
					<tr>
						<td class="formlabel">Access Right:</td>
						<td>
							<insta:selectoptions name="access_rights" value="${template_details.map.access_rights}" opvalues="U,A" optexts="Unrestricted,Author Only"/>
						</td>
					</tr>
				</c:if>

				<c:if test="${documentType eq 'reg'}">
					<tr>
						<td class="formlabel">Auto Generate on IP Registration:</td>
						<td class="forminfo"><input type="checkbox" name="auto_gen_ip" value="Y" <c:if test="${template_details.map.auto_gen_ip eq 'Y'}">checked</c:if> ></td>
					</tr>
					<tr>
						<td class="formlabel">Auto Generate on OP Registration:</td>
						<td class="forminfo"><input type="checkbox" name="auto_gen_op" value="Y" <c:if test="${template_details.map.auto_gen_op eq 'Y'}">checked</c:if>></td>
					</tr>
				</c:if>

				<tr >
					<td class="formlabel">Status: </td>
					<td ><input type="radio" name="status" id="status" value="A" class="validate-one-required" title="Status is mandatory."
						<c:choose>
							<c:when test="${param._method == 'add'}">checked</c:when>
							<c:when test="${template_details.map.status == 'A'}">checked</c:when>
						</c:choose>	/>Active
						 <input type="radio" name="status" id="status" value="I" <c:if test="${template_details.map.status == 'I'}">checked</c:if>>Inactive
					</td>
				</tr>
			</table>
		</fieldset>
		<table ><tr><td >
			<textarea id="template_content" name="template_content"
				style="width: 480pt; height: 500;"><c:out value="${template_details.map.template_content}"/></textarea>
		</td></tr></table>
		<div class="screenActions" id="actions">
			<c:url var="dashboardUrl" value="${actionUrl}">
				<c:param name="_method" value="list"/>
				<c:param name="sortOrder" value="template_name"/>
				<c:param name="sortReverse" value="false"/>
				<c:param name="status" value="A"/>
			</c:url>
			<button type="submit" name="save" id="save" accesskey="S" ><b><u>S</u></b>ave</button> | <a href="${dashboardUrl}">Templates List</a>
			<c:if test="${specialized == false}">
			    <c:if test="${max_centers_inc_default > 1}">
		            <c:if test="${param.template_id != null}">
		|	            <insta:screenlink screenId="mas_doctempl_cen_app" extraParam="?_method=getScreen&template_id=${param.template_id}&format=${param.format}&template_name=${template_details.map.template_name}&templ_cen_status=A"
					    label="Center Applicability" />
		            </c:if>
		        </c:if>
			</c:if>
		</div>
</body>
</html>
