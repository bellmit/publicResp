<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Add/Edit Template - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="masters/addoreditpdftemplate.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}" />
</head>

<body onload="init();">
	<h1><c:if test="${param.format == 'doc_pdf_form_templates'}">PDF Form</c:if>
	<c:if test="${param.format == 'doc_rtf_templates'}">RTF</c:if> Template</h1>
	<insta:feedback-panel/>

	<jsp:useBean id="fieldInputDisplay" class="java.util.HashMap"/>
	<c:set target="${fieldInputDisplay}" property="C" value="Canvas"/>
	<c:set target="${fieldInputDisplay}" property="E" value="External Device"/>
	<c:set target="${fieldInputDisplay}" property="D" value="Default"/>
	<c:set target="${fieldInputDisplay}" property="P" value="Paste Image"/>
	<c:set target="${fieldInputDisplay}" property="B" value="Browse Image"/>

	<jsp:useBean id="actionUrlMap" class="java.util.HashMap"/>
	<c:set target="${actionUrlMap}" property="mlc" value="MLCTemplate.do"/>
	<c:set target="${actionUrlMap}" property="service" value="ServiceTemplate.do"/>
	<c:set target="${actionUrlMap}" property="reg" value="RegistrationTemplate.do"/>
	<c:set target="${actionUrlMap}" property="insurance" value="InsuranceTemplate.do"/>
	<c:set target="${actionUrlMap}" property="dietary" value="DietaryTemplate.do"/>
	<c:set target="${actionUrlMap}" property="tpapreauth" value="TpaPreauthFormsTemplate.do"/>
	<c:set target="${actionUrlMap}" property="op_case_form_template" value="OpFormTemplate.do"/>
	<c:set target="${actionUrlMap}" property="ot" value="OTTemplate.do"/>

	<jsp:useBean id="docTypeMap" class="java.util.HashMap"/>
	<c:set target="${docTypeMap}" property="mlc" value="4"/>
	<c:set target="${docTypeMap}" property="service" value="SYS_ST"/>
	<c:set target="${docTypeMap}" property="reg" value="SYS_RG"/>
	<c:set target="${docTypeMap}" property="insurance" value="SYS_INS"/>
	<c:set target="${docTypeMap}" property="dietary" value="SYS_DIE"/>
	<c:set target="${docTypeMap}" property="tpapreauth" value="SYS_TPA"/>
	<c:set var="actionUrl" value="GenericDocumentTemplate.do"/>
	<c:set target="${docTypeMap}" property="op_case_form_template" value="SYS_OP"/>
	<c:set target="${docTypeMap}" property="ot" value="SYS_OT"/>
	<c:set var="doc_type" value="" />
	<c:if test="${specialized}">
		<c:set var="actionUrl" value="${actionUrlMap[documentType]}"/>
		<c:set var="doc_type" value="${docTypeMap[documentType]}"/>
	</c:if>

	<form action="${actionUrl}?_method=${param._method == 'add'?'create' : 'update'}" method="POST" enctype="multipart/form-data" name="pdfForm">

		<input type="hidden" name="specialized" id="specialized" value="${specialized}"/>
		<input type="hidden" name="format" id="format" value="${ifn:cleanHtmlAttribute(param.format)}"/>
		<input type="hidden" name="template_id" id="template_id" value="${template_details.map.template_id}">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">${param._method == 'add' ? 'Add' : 'Edit'}</legend>
			<table class="formtable" align="left">
				<tr >
					<td class="formlabel">Template Name: </td>
					<td ><input type="text" name="template_name" id="template_name" class="required validate-length field" length="100" style="width: 200px" value="${template_details.map.template_name}" title="Template Name is mandatory and should not exceed 100 characters."/></td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<c:if test="${displayDept}">
					<tr>
						<td class="formlabel">Department: </td>
						<td ><insta:selectdb name="dept_name" table="department" valuecol="dept_id" displaycol="dept_name" dummyvalue="--select--" value="${template_details.map.dept_name}"
									class="${documentType == 'op_case_form_template' ? 'validate-not-empty': ''} dropdown" filtered="false" title="Department Name is mandatory."/></td>
					</tr>
				</c:if>
				<c:choose>
					<c:when test="${specialized}">
						<input type="hidden" name="doc_type" value="${doc_type}"/>
					</c:when>
					<c:otherwise>
						<tr >
							<td class="formlabel">Document Type: </td>
							<td ><insta:selectdb name="doc_type" table="doc_type" valuecol="doc_type_id" displaycol="doc_type_name" value="${template_details.map.doc_type}"
															dummyvalue="----select----" filtered="true" filtercol="status" class="validate-not-empty dropdown" title="Document Type is mandatory."/></td>
						</tr>
					</c:otherwise>
				</c:choose>
				<c:if test="${displayAccessRights}">
					<tr>
						<td class="formlabel">Access Right:</td>
						<td >
							<insta:selectoptions name="access_rights" value="${template_details.map.access_rights}" opvalues="U,A" optexts="Unrestricted,Author Only"/>
						</td>
					</tr>
				</c:if>

				<c:if test="${documentType eq 'reg' and param.format eq 'doc_pdf_form_templates'}">
					<tr>
						<td class="formlabel">Auto Generate on IP Registration:</td>
						<td class="forminfo"><input type="checkbox" name="auto_gen_ip" value="Y" <c:if test="${template_details.map.auto_gen_ip eq 'Y'}">checked</c:if> ></td>
					</tr>
					<tr>
						<td class="formlabel">Auto Generate on OP Registration:</td>
						<td class="forminfo"><input type="checkbox" name="auto_gen_op" value="Y" <c:if test="${template_details.map.auto_gen_op eq 'Y'}">checked</c:if> ></td>
					</tr>
				</c:if>

				<tr >
					<td class="formlabel">Status: </td>
					<td ><input type="radio" name="status" id="status" value="A" class="validate-one-required" title="Status is mandatory."
						<c:choose>
							<c:when test="${param._method == 'add'}">checked</c:when>
							<c:when test="${template_details.map.status == 'A'}">checked</c:when>
						</c:choose>	/>Active
						 <input type="radio" name="status" id="status" value="I" <c:if test="${template_details.map.status == 'I'}">checked</c:if> >Inactive
					</td>
				</tr>
				<tr>
					<td class="formlabel">Current File: </td>
					<c:url value="${actionUrl}" var="url">
						<c:param name="_method" value="view"/>
						<c:param name="template_id" value="${template_details.map.template_id}"/>
						<c:param name="format" value="${param.format}"/>
					</c:url>
					<td >
						<c:choose>
							<c:when test="${not empty template_details.map.template_id}">
								<a href="<c:out value='${url}' />" target="_blank"><b>View</b></a>
							</c:when>
							<c:otherwise>
								View
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
				<c:if test="${param.format eq 'doc_pdf_form_templates'}">
					<tr>
						<td class="formlabel">.odt File: </td>
						<c:url value="${actionUrl}" var="odturl">
							<c:param name="_method" value="getOdtFile"/>
							<c:param name="template_id" value="${template_details.map.template_id}"/>
							<c:param name="format" value="${param.format}"/>
						</c:url>
						<td>
							<c:choose>
								<c:when test="${not empty template_details.map.odt_file}">
									<a href="<c:out value='${odturl}' />" target="_blank"><b>Download</b></a>
								</c:when>
								<c:otherwise>
									Download
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
					<tr>
						<td class="formlabel" style="white-space: nowrap">Upload .odt File <b>(limit: 10MB)</b>: </td>
						<td><input type="file" name="odt_file" id="odt_file"
							<c:if test="${param._method == 'add'}">class="required"</c:if>
							title=".odt File Upload is mandatory" accept="<insta:ltext key="upload.accept.odt"/>"/></td>
					</tr>
					<tr>
						<td class="formlabel" style="white-space: nowrap">Upload File <b>(limit: 10MB)</b>: </td>
						<td ><input type="file" name="template_content" id="template_content"
							<c:if test="${param._method == 'add'}">class="required"</c:if>
							title="File Upload is mandatory"  accept="<insta:ltext key="upload.accept.pdf"/>"/></td>
					</tr>
				</c:if>
				<c:if test="${param.format eq 'doc_rtf_templates'}">
				<tr>
					<td class="formlabel" style="white-space: nowrap">Upload File <b>(limit: 10MB)</b>: </td>
					<td ><input type="file" name="template_content" id="template_content"
						<c:if test="${param._method == 'add'}">class="required"</c:if>
						title="File Upload is mandatory"  accept="<insta:ltext key="upload.accept.rtf"/>"/></td>
				</tr>
				</c:if>
				<c:if test="${param._method == 'show' && param.format == 'doc_pdf_form_templates'}">
					<tr>
						<td class="formlabel">Last updated by: </td>
						<td class="forminfo">${template_details.map.user_name}</td>
					</tr>
					<tr>
						<td class="formlabel">Modified date:</td>
						<td class="forminfo"><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${template_details.map.mod_time}"/></td>
					</tr>
				</c:if>
			</table>
		</fieldset>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"> Image Capture Fields </legend>
			<table class="detailList" cellspacing="0" cellpadding="0" id="imgFieldsTable" border="0" style="width:750px">
				<tr>
					<th style="width: 100px">Field Name</th>
					<th style="width: 100px">Display Name</th>
					<th style="width: 100px">Field Input</th>
					<th style="width: 10px"></th>
					<th style="width: 10px"></th>
				</tr>
				<c:choose>
				<c:when test="${empty pdf_template_ext_fields}">
					<tr style="display:none">
						<td>
							<label> </label>
							<input type="hidden" name="field_id" id="field_id" value=""/>
							<input type="hidden" name="field_name" id="field_name" value=""/>
							<input type="hidden" name="display_name" id="display_name" value=""/>
							<input type="hidden" name="field_input" id="field_input" value=""/>
							<input type="hidden" name="field_delete" id="field_delete" value="N"/>
						</td>
						<td> <label> </label> </td>
						<td> <label> </label> </td>
						<td>
							<a href="javascript:Delete" title="Delete Field">
							<img src="${cpath}/icons/delete.gif" class="button" onclick="return deleteImgFields(this);"/>
							</a>
						</td>
						<td>
							<a href="javascript:Edit" title="Edit Field">
							<img src="${cpath}/icons/Edit.png" class="button" onclick="return showImgFieldsDialog(this);"/>
							</a>
						</td>
					</tr>
				</c:when>
				<c:otherwise>
					<c:forEach items="${pdf_template_ext_fields}" var="pdfext">
						<tr>
							<td> <label> ${pdfext.field_name} </label>
								<input type="hidden" name="field_id" id="field_id" value="${pdfext.field_id}"/>
								<input type="hidden" name="field_name" id="field_name" value="${pdfext.field_name}"/>
								<input type="hidden" name="display_name" id="display_name" value="${pdfext.display_name}"/>
								<input type="hidden" name="field_input" id="field_input" value="${pdfext.field_input}"/>
								<input type="hidden" name="field_delete" id="field_delete" value="N"/>
							</td>
							<td> <label> ${pdfext.display_name} </label> </td>
							<td> <label> ${fieldInputDisplay[pdfext.field_input]} </label> </td>
							<td>
								<a href="javascript:Delete" title="Delete Field">
								<img src="${cpath}/icons/delete.gif" class="button" onclick="return deleteImgFields(this);"/>
								</a>
							</td>
							<td>
								<a href="javascript:Edit" title="Edit Field">
								<img src="${cpath}/icons/Edit.png" class="button" onclick="return showImgFieldsDialog(this);"/>
								</a>
							</td>
						</tr>
					</c:forEach>
				</c:otherwise>
				</c:choose>
			</table>
			<table class="addButton" style="width:750px">
				<tr>
					<td style="width:660px">
					</td>
					<td align="center">
						<button type="button" name="btnAddField" id="btnAddField" title="Add New Image Field"
							onclick="showImgFieldsDialog(this, 'add');return false;"
							accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
					</td>
				</tr>
			</table>
		</fieldset>



		<div id="actions" class="screenActions">
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
	</form>

<form name="imgFieldsForm">
<input type="hidden" id="imgRowId" value=""/>
<div id="imgFieldsDialog" style="display:none">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Add/Edit Image Field</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Field Name:</td>
				<td><input type="text" name="field_name" style="width: 180px"/></td>
			</tr>
			<tr>
				<td class="formlabel">Display Name:</td>
				<td><input type="text" name="display_name" style="width: 180px"/></td>
			</tr>
			<tr>
				<td class="formlabel">Field Input:</td>
				<td><insta:selectoptions name="field_input" value="E"
						opvalues="E,C" optexts="External Device,Canvas" />
						</td>
			</tr>
		</table>
	</fieldset>
	<table>
		<tr>
			<td><input type="button" onclick="onSubmit()" value="OK" /></td>
			<td><input type="button" onclick="onCancel()" value="Cancel"/></td>
			<td><input type="button" id="prevFieldBtn" onclick="showPreviousField()" value="<<Prev"/></td>
			<td><input type="button" id="nextFieldBtn" onclick="showNextField()" value="Next>>"/></td>
		</tr>
	</table>
</div>
</div>
</form>
</body>
</html>
