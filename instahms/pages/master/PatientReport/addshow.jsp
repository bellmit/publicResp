<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
	<title>Edit Patient Report - Insta HMS</title>
	<insta:link type="js" file="tiny_mce/tiny_mce.js" />
	<insta:link type="js" file="editor.js" />
	<insta:link type="js" file="widgets.js" />
	<insta:link type="js" file="masters/PatientReport.js" />

	<script>
		/* initialize the tinyMCE editor: todo: font and size to be customizable */
		initEditor("report_file", '${cpath}', 'sans-serif', 12);
	</script>

</head>
<c:set var="bodyWidth" value="${prefs.page_width - prefs.left_margin - prefs.right_margin}"/>
<body>
<form  name="mainform" action="PatientReport.do?_method=${param._method == 'add' ? 'create' : 'update'}" method="POST" enctype="multipart/form-data">
	<input type="hidden" name="format" value="${ifn:cleanHtmlAttribute(param.format)}">

	<c:if test="${param._method == 'show'}">
		<c:if test="${param.format == 'F'}">
			<input type="hidden" name="form_id" value="${ifn:cleanHtmlAttribute(report.form_id)}"/>
		</c:if>
		<c:if test="${param.format == 'T'}">
			<input type="hidden" name="format_id" value="${report.format_id}"/>
		</c:if>
		<c:if test="${param.format == 'P'}">
			<input type="hidden" name="template_id" value="${report.template_id}"/>
		</c:if>
	</c:if>
	<%-- no extra params for add operation --%>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Discharge Summary Report Template</h1>
	<insta:feedback-panel/>

	<table class="formtable">
		<tr>
			<td class="formlabel">Template Name:</td>
			<c:choose>
				<c:when test="${param.format == 'T'}">
					<c:set var="name" value="template_caption"/>
					<c:set var="title" value="template_title"/>
				</c:when>
				<c:when test="${param.format == 'F'}">
					<c:set var="name" value="form_caption"/>
					<c:set var="title" value="form_title"/>
				</c:when>
				<c:when test="${param.format == 'P'}">
					<c:set var="name" value="template_name"/>
					<c:set var="title" value="template_title"/>
				</c:when>
			</c:choose>
			<td >
				<input type="text" name="${name}" value="${report[name]}"
					class="required validate-length field" length="100"
					title="Name is required and max length of name can be 100" />
			</td>
			<td class="formlabel">&nbsp;</td>
			<td >&nbsp;</td>
			<td class="formlabel">&nbsp;</td>
			<td >&nbsp;</td>
		</tr>

		<c:if test="${param.format != 'P'}">
		<tr>
				<td class="formlabel">Title:</td>
				<td >
					<input type="text" name="${title}" value="${report[title]}"
						class="validate-length field" length="500"
						title="Title cannot be more than 500 characters" />
				</td>
		</tr>
		</c:if>

		<c:if test="${param.format == 'T'}">
		<tr>
				<td class="formlabel">Patient Header Template: </td>
				<td>
					<c:set var="phTemplateId" value="${report['pheader_template_id']}"/>
					<select name="pheader_template_id" id="pheader_template_id" class="dropdown">
						<option value="" ${empty phTemplateId?'selected':''}>None</option>
						<option value="0" ${phTemplateId == 0?'selected':''}>System Default</option>
						<c:forEach var="phTemplate" items="${phTemplates}">
							<option value="${phTemplate.map.template_id}" ${phTemplateId == phTemplate.map.template_id?'selected':''}>${phTemplate.map.template_name}</option>
						</c:forEach>
					</select>
				</td>
		</tr>
		</c:if>
		<tr>
			<td class="formlabel">Access Right:</td>
			<td >
				<insta:selectoptions name="access_rights" value="${template_details.map.access_rights}" opvalues="U,A" optexts="Unrestricted,Author Only"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td >
				<input type="radio" name="status" value="A"
						${empty report.status || report.status == 'A' ? 'checked' : ''}/>Active
				<input type="radio" name="status" value="I"
						${report.status == 'I' ? 'checked' : ''}/>Inactive
			</td>
		</tr>
		<c:if test="${param.format == 'P'}">
			<tr>
				<td class="formlabel">Current File: </td>
				<c:url value="GenericDocumentTemplate.do" var="url">
					<c:param name="_method" value="view"/>
					<c:param name="template_id" value="${report.template_id}"/>
					<c:param name="format" value="doc_pdf_form_templates"/>
				</c:url>
				<td >
					<c:choose>
						<c:when test="${not empty report.template_id}">
							<a href="<c:out value='${url}' />" target="_blank"><b>View</b></a>
						</c:when>
						<c:otherwise>
							View
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
			<tr>
				<td class="formlabel">.odt File: </td>
				<c:url value="GenericDocumentTemplate.do" var="odturl">
					<c:param name="_method" value="getOdtFile"/>
					<c:param name="template_id" value="${report.template_id}"/>
					<c:param name="format" value="${param.format}"/>
				</c:url>
				<td>
					<c:choose>
						<c:when test="${not empty report.odt_file}">
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
				<td ><input type="file" name="odt_file" id="odt_file"
							<c:if test="${param._method == 'add'}">class="required"</c:if>
							title=".odt File Upload is mandatory" accept="<insta:ltext key="upload.accept.odt"/>"/></td>
			</tr>
			<tr>
				<td class="formlabel" style="white-space:nowrap">Upload File <b>(limit: 10MB)</b>: </td>
				<td ><input type="file" name="template_content" id="template_content"
					<c:if test="${param._method == 'add'}">class="required"</c:if>
					title="File Upload is mandatory"  accept="<insta:ltext key="upload.accept.pdf"/>"/></td>
			</tr>
			<c:if test="${param._method == 'show'}">
				<tr>
					<td class="formlabel">Last updated by: </td>
					<td class="forminfo">${ifn:cleanHtml(report.user_name)}</td>
				</tr>
				<tr>
					<td class="formlabel">Modified date:</td>
					<td class="forminfo"><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${report.mod_time}"/></td>
				</tr>
			</c:if>
		</c:if>
	</table>


	<c:if test="${param.format == 'T'}">
		<table >
			<tr>
				<td >
					<textarea id="report_file" name="report_file" style="width: ${bodyWidth}pt; height: 450;">
						<c:out value="${report.report_file}"/>
					</textarea>
				</td>
			</tr>
		</table>
	</c:if>

	<c:if test="${param.format == 'F'}">
		<table id="fieldsTable" style="margin-top: 10px" cellspacing="0" cellpadding="0" class="delActionTable">
			<tr class="header">
				<td class="first">Heading</td>
				<td >Display Order</td>
				<td >No. of Lines</td>
				<td >Default Text</td>
				<td >&nbsp;</td>
			</tr>
			<c:if test="${param._method == 'show'}">
				<c:forEach items="${fields}" var="f" varStatus="st">
					<tr>
						<td ><input type="text" name="caption" class="first ${st.last?'':'previousEl'}" value="${f.caption}"></td>
						<td ><input type="text" class="validate-number ${st.last?'':'previousEl'}" name="displayorder" value="${f.displayorder}"></td>
						<td ><input type="text" class="validate-number ${st.last?'':'previousEl'}" name="no_of_lines" value="${f.no_of_lines}"></td>
						<td ><input type="text" class="${st.last?'':'previousEl'}" style="width: 400px;" name="default_text" value="${f.default_text}"></td>
						<td class="last">
							<a href="javascript:void(0)" onclick="changeElsColor('deleted', ${st.index+1})"><img src="${cpath}/icons/Delete.png" class="imgDelete"/></a>
							<input type="hidden" name="deleted" id="deleted${st.index+1}" value="N">
							<input type="hidden" name="field_id" value="${f.field_id}">
						</td>
					</tr>
				</c:forEach>
			</c:if>
			<c:if test="${param._method == 'add'}">
				<tr>
					<td ><input type="text" name="caption" class="first" ></td>
					<td ><input type="text" class="validate-number" name="displayorder"></td>
					<td ><input type="text" class="validate-number" name="no_of_lines"></td>
					<td ><input type="text" style="width: 400px;" name="default_text"></td>
					<td class="last">
						<a href="javascript:void(0)" onclick="changeElsColor('deleted', 1)"><img src="${cpath}/icons/Delete.png" class="imgDelete"/></a>
						<input type="hidden" name="deleted" id="deleted1" value="N">
						<input type="hidden" name="field_id" value="_">
					</td>
				</tr>
			</c:if>
		</table>
		<div class="fltR " style="width: 90px;margin-top: -20px">
			<input type="button" name="addRow" value="+" width="16" class="plus" onclick="return addHeadingRow();"/>
		</div>

	</c:if>
	<div class="screenActions">
		<button type="button" accesskey="S" onclick="submitValues('${ifn:cleanJavaScript(param.format)}')"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
			<c:choose>
				<c:when test="${param.format eq 'T'}"><a href="${cpath}/master/PatientReport.do?_method=add&format=T">Add New Editable Template</a></c:when>
				<c:when test="${param.format eq 'F'}"><a href="${cpath}/master/PatientReport.do?_method=add&format=F">Add New Fixed Fields Template</a></c:when>
				<c:otherwise><a href="${cpath}/master/PatientReport.do?_method=add&format=P">Add Pdf Form Template</a></c:otherwise>
			</c:choose>
		|
		</c:if>

		<a href="PatientReport.do?_method=list&sortOrder=caption&sortReverse=false&status=A" >Templates List</a>
	</div>

</form>

</body>
</html>

