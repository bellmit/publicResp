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
	<insta:link type="js" file="masters/addoredithvftemplate.js"/>
	<insta:link type="js" file="hmsvalidation.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}" />
		<style type="text/css">
			tr.newRow {background-color: #E9F2C2; }
			tr.deleted {background-color: #F2DCDC; color: gray; }

		</style>
	<script>
		var contextPath = '${cpath}';
	</script>
</head>
<body>
	<h1>${param._method == 'add' ? 'Add' : 'Edit'} HVF Template</h1>
	<insta:feedback-panel/>

	<jsp:useBean id="actionUrlMap" class="java.util.HashMap"/>
	<c:set target="${actionUrlMap}" property="mlc" value="MLCTemplate.do"/>
	<c:set target="${actionUrlMap}" property="service" value="ServiceTemplate.do"/>
	<c:set target="${actionUrlMap}" property="reg" value="RegistrationTemplate.do"/>
	<c:set target="${actionUrlMap}" property="insurance" value="InsuranceTemplate.do"/>
	<c:set target="${actionUrlMap}" property="dietary" value="DietaryTemplate.do"/>
	<c:set target="${actionUrlMap}" property="op_case_form_template" value="OpFormTemplate.do"/>
	<c:set target="${actionUrlMap}" property="ot" value="OTTemplate.do"/>
	<c:set target="${actionUrlMap}" property="consultation_form" value="ConsultationTemplate.do"/>

	<jsp:useBean id="docTypeMap" class="java.util.HashMap"/>
	<c:set target="${docTypeMap}" property="mlc" value="4"/>
	<c:set target="${docTypeMap}" property="service" value="SYS_ST"/>
	<c:set target="${docTypeMap}" property="reg" value="SYS_RG"/>
	<c:set target="${docTypeMap}" property="insurance" value="SYS_INS"/>
	<c:set target="${docTypeMap}" property="dietary" value="SYS_DIE"/>
	<c:set target="${docTypeMap}" property="op_case_form_template" value="SYS_OP"/>
	<c:set target="${docTypeMap}" property="ot" value="SYS_OT"/>
	<c:set target="${docTypeMap}" property="consultation_form" value="SYS_CONSULT"/>

	<c:set var="actionUrl" value="GenericDocumentTemplate.do"/>
	<c:set var="doc_type" value="" />
	<c:if test="${specialized}">
		<c:set var="actionUrl" value="${actionUrlMap[documentType]}"/>
		<c:set var="doc_type" value="${docTypeMap[documentType]}"/>
	</c:if>
	<form action="${actionUrl}" method="POST" autocomplete="off" name="hvfTemplateForm" onsubmit="return false;">
		<input type="hidden" name="_method" value="${param._method == 'add'?'create' : 'update'}"/>
		<input type="hidden" name="specialized" id="specialized" value="${specialized}"/>
		<input type="hidden" name="format" id="format" value="${ifn:cleanHtmlAttribute(param.format)}"/>
		<input type="hidden" name="template_id" id="template_id" value="${template_details.map.template_id}">
		<input type="hidden" name="documentType" id="documentType" value="${ifn:cleanHtmlAttribute(param.documentType)}">
		<fieldset class="fieldSetBorder">
			<table class="formtable" >
				<tr>
					<td class="formlabel">Template Name: </td>
					<td>
						<input type="text" name="template_name" id="template_name" class="required validate-length "
						length="100" title="Template Name is mandatory and should not exceed 100 characters."
						value="${template_details.map.template_name}"/></td>
					<td class="formlabel">&nbsp;</td>
					<td></td>
					<td class="formlabel">&nbsp;</td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Title: </td>
					<td>
						<input type="text" name="title" id="title" class="required validate-length "
						length="100" title="Title is mandatory and should not exceed 100 characters."
						value="${template_details.map.title}"/>
					</td>
				</tr>
				<c:if test="${displayDept}">
					<tr>
						<td class="formlabel">Department: </td>
						<td>
							<insta:selectdb name="dept_name" table="department" valuecol="dept_id" displaycol="dept_name"
								dummyvalue="-- Select --" value="${template_details.map.dept_name}"
								class="${documentType == 'op_case_form_template' ? 'validate-not-empty' : ''} dropdown"
								filtered="false" title="Department Name is mandatory."/>
						</td>
					</tr>
				</c:if>
				<c:choose>
					<c:when test="${specialized}">
						<input type="hidden" name="doc_type" value="${doc_type}"/>
					</c:when>
					<c:otherwise>
						<tr>
							<td class="formlabel">Document Type: </td>
							<td>
								<insta:selectdb name="doc_type" table="doc_type" valuecol="doc_type_id"
									displaycol="doc_type_name" value="${template_details.map.doc_type}"
									dummyvalue="----select----" filtered="true" filtercol="status" class="validate-not-empty dropdown"
									title="Document Type is mandatory."/>
							</td>
						</tr>
					</c:otherwise>
				</c:choose>
				<tr>
					<td class="formlabel">Status: </td>
					<td>
						<input type="radio" name="status" id="status" value="A" class="validate-one-required"
						<c:choose>
							<c:when test="${param._method == 'add'}">checked</c:when>
							<c:when test="${template_details.map.status == 'A'}">checked</c:when>
						</c:choose>	title="Status is mandatory."/>Active
						 <input type="radio" name="status" id="status" value="I" <c:if test="${template_details.map.status == 'I'}">checked</c:if>>Inactive
					</td>
				</tr>
				<c:if test="${displayAccessRights}">
					<tr>
						<td class="formlabel">Access Right:</td>
						<td>
							<insta:selectoptions name="access_rights" value="${template_details.map.access_rights}"
							opvalues="U,A" optexts="Unrestricted,Author Only"/>
						</td>
					</tr>
				</c:if>
				<tr>
					<td class="formlabel">Print Templates:</td>
					<td>
						<insta:selectdb name="print_template_name" table="hvf_print_template"
							valuecol="template_name"
							displaycol="template_name" filtered="false" dummyvalue="--select--"
							value="${template_details.map.print_template_name}"/></td>
				</tr>

			</table>
		</fieldset>
		<table cellspacing="0" cellpadding="0" style="margin-top: 10px">
			<tr>
				<td>
					<table class="delActionTable" id="hvfFieldsTable" cellspacing="0" cellpadding="0" style="empty-cells: show;">
						<tr class="header">
							<td class="first">Heading</td>
							<td>Display Order</td>
							<td>No. Of Lines</td>
							<td>Default Text</td>
							<td>Field Input</td>
							<td>Print</td>
							<td>Status</td>
						</tr>
						<c:choose>
							<c:when test="${not empty hvf_template_fields}">
								<c:forEach items="${hvf_template_fields}" var="fields" varStatus="status">
									<tr>
										<td><input type="text" name="field_name" id="field_name" style="width: 200px" title="Field name "  value="${fields.map.field_name}" class="${status.last?'':'previousEl'} first"/>
											<input type="hidden" name="field_id" id="field_id" value="${fields.map.field_id}"/></td>
										<td><input type="text" name="display_order" id="display_order" style="width: 100px;" maxlength="4" value="${fields.map.display_order}" class="validate-number ${status.last?'':'previousEl'}" title="Display Order is mandatory and it should be Number."/></td>
										<td><input type="text" name="num_lines" id="num_lines" style="width: 100px;" maxlength="5" value="${fields.map.num_lines}" class="validate-number ${status.last?'':'previousEl'}" title="No. Of Lines is mandatory and it should be Number." /></td>
										<td><input type="text" name="default_value" id="default_value" style="width: 300px;" maxlength="500" value="${fields.map.default_value}" class="${status.last?'':'previousEl'}"></td>
										<td> <insta:selectoptions name="field_input" id="field_input"
												opvalues="D,E,P,B" optexts="Default,External Device,Paste Image,Browse Image"
												value="${fields.map.field_input}"/>
										</td>
										<td class="border">
											<input type="checkbox" name="print_column_chk" id="print_column_chk${status.index+1}"
												onclick="toggleValue(this, 'print_column${status.index+1}')" ${fields.map.print_column == 'Y' ? 'checked' : ''}/>
											<input type="hidden" name="print_column" id="print_column${status.index+1}" value="${fields.map.print_column}"/>
										</td>
										<td class=" last">
											<input type="checkbox" name="field_status_chk" id="field_status_chk${status.index+1}"
												onclick="toggleValue(this, 'field_status${status.index+1}')" ${fields.map.field_status == 'A' ? 'checked' : ''}/>
											<input type="hidden" name="field_status" id="field_status${status.index+1}" value="${fields.map.field_status}"/>
										</td>
									</tr>
								</c:forEach>
							</c:when>
							<c:otherwise>
								<tr>
									<td><input type="text" name="field_name" id="field_name" style="width: 200px" title="Field name " class="first"/>
										<input type="hidden" name="field_id" id="field_id" /></td>
									<td><input type="text" name="display_order" id="display_order" class="validate-number " style="width: 100px;" maxlength="4" title="Display Order is mandatory and it should be Number." /></td>
									<td><input type="text" name="num_lines" id="num_lines" class="validate-number " style="width: 100px;" maxlength="5" title="No. Of Lines is mandatory and it should be Number." /></td>
									<td><input type="text" name="default_value" id="default_value" style="width: 300px;" maxlength="500" ></td>
									<td> <insta:selectoptions name="field_input" id="field_input" value="D"
												opvalues="D,E,P,B" optexts="Default,External Device,Paste Image,Browse Image" />
									</td>
									<td class="border">
										<input type="checkbox" name="print_column_chk" id="print_column_chk1"
											onclick="toggleValue(this, 'print_column1')" checked/>
										<input type="hidden" name="print_column" id="print_column1" value="Y"/>
									</td>
									<td class=" last">
										<input type="checkbox" name="field_status_chk" id="field_status_chk1"
												onclick="toggleValue(this, 'field_status1')" checked/>
										<input type="hidden" name="field_status" id="field_status1" value="A"/>
									</td>
								</tr>
							</c:otherwise>
						</c:choose>
					</table>
				</td>
				<td valign="bottom">
					<input type="button" name="addRow" value="+" width="16" class="plus" onclick="return addNewRow();"/>
				</td>
			</tr>
		</table>
		<div class="screenActions">
			<c:url var="dashboardUrl" value="${actionUrl}">
				<c:param name="_method" value="list"/>
				<c:param name="sortOrder" value="template_name"/>
				<c:param name="sortReverse" value="false"/>
				<c:param name="status" value="A"/>
			</c:url>
			<button type="button" name="save" id="save" accesskey="S" onclick="return validateFields();"><b><u>S</u></b>ave</button> |
			<a href="${dashboardUrl}" >Templates List</a>
			<c:if test="${specialized == false}">
			    <c:if test="${max_centers_inc_default > 1}">
		            <c:if test="${param.template_id != null}">
		|	            <insta:screenlink screenId="mas_doctempl_cen_app" extraParam="?_method=getScreen&template_id=${param.template_id}&format=doc_hvf_templates&template_name=${template_details.map.template_name}&templ_cen_status=A"
					    label="Center Applicability" />
		            </c:if>
		        </c:if>
			</c:if>
		</div>

	</form>
</body>
</html>
