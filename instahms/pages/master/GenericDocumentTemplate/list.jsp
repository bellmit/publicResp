<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<jsp:useBean id="typeDisplay" class="java.util.HashMap"/>
<c:set target="${typeDisplay}" property="doc_hvf_templates" value="HVF Template"/>
<c:set target="${typeDisplay}" property="doc_rich_templates" value="Rich Text Template"/>
<c:set target="${typeDisplay}" property="doc_pdf_form_templates" value="PDF Form Template"/>
<c:set target="${typeDisplay}" property="doc_rtf_templates" value="RTF Template"/>

<jsp:useBean id="pageHeaderMap" class="java.util.HashMap"/>
<c:set target="${pageHeaderMap}" property="mlc" value="MLC Templates"/>
<c:set target="${pageHeaderMap}" property="service" value="Service Templates"/>
<c:set target="${pageHeaderMap}" property="reg" value="Registration Templates"/>
<c:set target="${pageHeaderMap}" property="insurance" value="Insurance Templates"/>
<c:set target="${pageHeaderMap}" property="dietary" value="Dietary Templates"/>
<c:set target="${pageHeaderMap}" property="tpapreauth" value="TPA Preauth Form Templates"/>
<c:set target="${pageHeaderMap}" property="op_case_form_template" value="OP Case Forms"/>
<c:set target="${pageHeaderMap}" property="ot" value="OT Templates"/>
<c:set target="${pageHeaderMap}" property="consultation_form" value="Consultation Templates"/>


<jsp:useBean id="actionUrlMap" class="java.util.HashMap"/>
<c:set target="${actionUrlMap}" property="mlc" value="MLCTemplate.do"/>
<c:set target="${actionUrlMap}" property="service" value="ServiceTemplate.do"/>
<c:set target="${actionUrlMap}" property="reg" value="RegistrationTemplate.do"/>
<c:set target="${actionUrlMap}" property="insurance" value="InsuranceTemplate.do"/>
<c:set target="${actionUrlMap}" property="dietary" value="DietaryTemplate.do"/>
<c:set target="${actionUrlMap}" property="tpapreauth" value="TpaPreauthFormsTemplate.do"/>
<c:set target="${actionUrlMap}" property="op_case_form_template" value="OpFormTemplate.do"/>
<c:set target="${actionUrlMap}" property="ot" value="OTTemplate.do"/>
<c:set target="${actionUrlMap}" property="consultation_form" value="ConsultationTemplate.do"/>

<c:set var="pageHeader" value="Generic Document Templates"/>
<c:set var="actionUrl" value="GenericDocumentTemplate.do"/>

<c:if test="${specialized}">
	<c:set var="pageHeader" value="${pageHeaderMap[documentType]}"/>
	<c:set var="actionUrl" value="${actionUrlMap[documentType]}"/>
</c:if>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>${pageHeader} - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="masters/GenericDocumentTemplate.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<script type="text/javascript">
	    var doctorCenterapplicable = ${max_centers_inc_default > 1 ? 'true' : 'false'};
		var toolbar = {
			Edit: {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/${actionUrl}?_method=show',
				onclick: null,
				description: "Edit Template"
				},
			Cen_Applicability: {
				title: "Center Applicability",
				imageSrc: "icons/Edit.png",
				href: 'master/GenericDocumentTemplate/DocTemplCenterApplicability.do?_method=getScreen',
				onclick: null,
				description: "Center Applicability for this template",
				show : doctorCenterapplicable
				}	
		};
		
		function init() {
			createToolbar(toolbar);
			showFilterActive(document.searchForm);
		}
	</script>

</head>

<c:set var="doc_templates_list" value="${pagedList.dtoList}"/>
<c:set var="results" value="${not empty doc_templates_list }"/>
<body onload="init()">


	<h1>${pageHeader}</h1>
	<insta:feedback-panel/>
	<form action="${actionUrl}" method="GET" name="searchForm">
		<input type="hidden" value="${actionUrl}" name="_actionUrl"/>
		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResults}" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Template Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="template_name" id="templateName" value="${ifn:cleanHtmlAttribute(param.template_name)}" class="field"/>
						<input type="hidden" name="template_name@op" value="ico"/>
					</div>
				</div>
			</div>
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<c:if test="${!specialized}">
							<td>
								<c:set var="colspan" value="2"/>
								<div class="sfLabel">Document Type</div>
								<div class="sfField">
									<insta:selectdb name="doc_type_id" table="doc_type" valuecol="doc_type_id"
													displaycol="doc_type_name" values="${paramValues['doc_type_id']}"
													multiple = "true" filtered="false" class="listbox" size="8"/>
								</div>
							</td>
						</c:if>
						<c:if test="${displayDept}">
							<td>
								<div class="sfLabel">Department</div>
								<div class="sfField">
									<insta:selectdb name="dept_name" table="department" valuecol="dept_id" displaycol="dept_name"
										values="${paramValues.dept_name}" multiple="true" size="10" class="listbox" />
								</div>

							</td>
						</c:if>
						<c:if test="${displayDoctor}">
							<td>
								<div class="sfLabel">Doctor</div>
								<div class="sfField">
									<insta:selectdb name="doctor_id" table="doctors" valuecol="doctor_id" displaycol="doctor_name"
										values="${paramValues.doctor_id}" multiple="true" size="10" class="listbox" />
								</div>

							</td>
						</c:if>
						<td>
							<div class="sfLabel">Format</div>
							<div class="sfField">
								<c:forEach var="type" items="${allowedTemplates}">
									<c:set var="allowedFormatDisplays" value="${allowedFormatDisplays}${typeDisplay[type]},"/>
								</c:forEach>
								<insta:checkgroup name="format" selValues="${paramValues.format}"
											opvalues="${allowedTemplates}" optexts="${allowedFormatDisplays}"/>
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status</div>
							<div class="sfField">
								<insta:checkgroup name="status" selValues="${paramValues.status}"
											opvalues="A,I" optexts="Active,Inactive"/>
							</div>
						</td>
						<c:if test="${specialized}">
							<td class="last">&nbsp;</td>
						</c:if>
						<c:if test="${!displayDept}">
							<td class="last">&nbsp;</td>
						</c:if>
						<c:if test="${!displayDoctor}">
							<td class="last">&nbsp;</td>
						</c:if>
					</tr>
				</table>
			</div>
		</insta:search>
    <c:set var="docTemplateCenterApplicable" value="${max_centers_inc_default > 1 ? 'true' : 'false'}"/>
    <c:if test="${specialized}">
        <c:set var="docTemplateCenterApplicable" value="false"/>
    </c:if>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList" >
		<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr>
				<th>#</th>
				<insta:sortablecolumn name="template_name" title="Template Name"/>
				<c:if test="${!specialized}"> <insta:sortablecolumn name="doc_type_name" title="Document Type"/></c:if>
				<insta:sortablecolumn name="format" title="Format"/>
			</tr>
			<c:forEach items="${doc_templates_list}" var="doc_temp" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				<c:if test="${!specialized}">
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{template_id:'${doc_temp.map.template_id}', format:'${doc_temp.map.format}', template_name:'${doc_temp.map.template_name }', templ_cen_status:'${doc_temp.map.templ_cen_status }'},[true, ${docTemplateCenterApplicable}],'');" id="toolbarRow${st.index}"</c:if>
				<c:if test="${specialized}">
				onclick="showToolbar(${st.index}, event, 'resultTable',
							{template_id:'${doc_temp.map.template_id}', format:'${doc_temp.map.format}', template_name:'${doc_temp.map.template_name }'},[true, ${docTemplateCenterApplicable}],'');" id="toolbarRow${st.index}"
				</c:if>>							
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td><div style="width: 15px; float: left"><img src="${cpath}/images/${doc_temp.map.status == 'A'?'empty':'grey'}_flag.gif"/></div>${doc_temp.map.template_name}</td>
					<c:if test="${!specialized}">
						<td>${doc_temp.map.doc_type_name}</td>
					</c:if>
					<td><c:choose>
						<c:when test="${doc_temp.map.format == 'doc_hvf_templates'}"><font class="hvf">HVF Template</font></c:when>
						<c:when test="${doc_temp.map.format == 'doc_rich_templates'}"><font class="richtext">Rich Text Template</font></c:when>
						<c:when test="${doc_temp.map.format == 'doc_pdf_form_templates'}"><font class="pdfform">PDF Form Template</font></c:when>
						<c:when test="${doc_temp.map.format == 'doc_rtf_templates'}"><font class="rtf">RTF Template</font></c:when>
						</c:choose>
						</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${results}"/>
	</div>

	</form>

	<c:url value="${actionUrl}" var="addUrl">
		<c:param name="_method" value="add"/>
	</c:url>

	<table style="margin-top: 10px;">
		<tr>
			<td> Add New Template: </td>
			<td><select name="format_for_add" id="format_for_add" class="dropdown">
					<option value="">-----select format----</option>
					<c:forEach items="${allowedTemplates}" var="type">
						<option value="${type}">${typeDisplay[type]}</option>
					</c:forEach>
				</select>
				<a id="add" href="" onmousedown="return addTemplate(event, '${ifn:cleanJavaScript(addUrl)}');"
				onclick="return addTemplate(event, '${ifn:cleanJavaScript(addUrl)}')">Add</a>
			</td>
		</tr>
	</table>
	<div style="float: right; margin-bottom: 20px">
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"/></div>
		<div class="flagText">Inactive Templates</div>
	</div>

</body>
</html>
