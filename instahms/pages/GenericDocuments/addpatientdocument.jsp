<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Document - Insta HMS</title>
	<style>
		.status_I {
			background-color: #dbe7f6;
		}
	</style>
	<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		var specialized = ${specialized};
		var uplodLimit = '${genericPrefs.upload_limit_in_mb}';
		function setTemplateParams(radio, templateId, format) {
			document.addDocument.template_id.value = templateId;
			document.addDocument.format.value = format;
		}
		function validate() {
			var flag = false;
			var selectTemplate = document.getElementsByName("selectTemplate");
			for (var i=0; i<selectTemplate.length; i++) {
				if (selectTemplate[i].checked) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				alert("Please select the template");
				return false;
			}

			if (!specialized) {
				var addDocFor = document.getElementsByName("addDocFor");
				for (var i=0; i<addDocFor.length; i++) {
					if (addDocFor[i].checked) {
						if (addDocFor[i].value == 'patient') {
							document.addDocument.patient_id.value = ''
						} else if (addDocFor[i].value == 'visit') {
							var visit_id = document.addDocument.visit_dropdown.value;
							if (visit_id == '') {
								alert('Please select the visit.');
								document.addDocument.visit_dropdown.focus();
								return false;
							}
							document.addDocument.patient_id.value = visit_id;
						}
					}
				}
			}
			document.addDocument.submit();
			return false;
		}
		function openNewUploadDocumentPopUp(mrNo) {
			window.visit_type = 'all';
			window.uplodLimitInMb = uplodLimit;
			window.setUploadDocMode('');
			var addDocFor = document.getElementsByName("addDocFor");
			for (var i=0; i<addDocFor.length; i++) {
				if (addDocFor[i].checked) {
					if (addDocFor[i].value == 'patient') {
						window.routeParams = {
								patientId :mrNo,
						};
						window.fetchVisitsList();
						window.changeSelectedFilter(mrNo);

					} else if (addDocFor[i].value == 'visit') {
						var visit_id = document.addDocument.visit_dropdown.value;
						if (visit_id == '') {
							alert('Please select the visit.');
							document.addDocument.visit_dropdown.focus();
							return false;
						}
						window.routeParams = {
								patientId : mrNo,
								visitId: visit_id,
						};
						window.fetchVisitsList();
						window.changeSelectedFilter(visit_id);
					}
				}
			}
			window.getUploadedDocumetsList();
			window.fetchDocumentTypes();
			window.openAddDocument();
		}
        		var sort_by = function(field, reverse, primer){
        			var key = function (x) {return primer ? primer(x[field]) : x[field]};
        			return function (a,b) {
        			var A = key(a), B = key(b);
        				return (A < B ? -1 : (A > B ? 1 : 0)) * [1,-1][+!!reverse];
        			}
        		}
    		function removeDoctypeDuplicates(doctypeJson) {
        			doctypeJson.sort(sort_by('doc_type_id', false, function(x){return x.toUpperCase()}))
        			for (var i=doctypeJson.length-1; i>0; i--) {
        				var record1 = doctypeJson[i];
        				var record2 = doctypeJson[i-1];
        				if (record1.doc_type_id === record2.doc_type_id) {
        					doctypeJson.splice(i-1, 1);
        				}
        			}
        			return doctypeJson;
        		}


        var searchTemplateId='';
        var searchDocTypeId='';
        var searchTemplateName='';
        var documentTemplateName='';
    		function genFormSearch() {
    		       searchTemplateName = document.getElementById("gen_template_search").value;
    		       documentTemplateName = document.getElementById("doc_type_search").value;
    		       var refresh = true;
    		       if(searchTemplateName !='' && searchTemplateId === '') {
    		          alert("Please choose the template name from the drop down");
    		          refresh = false;
    		       }
    		       if(documentTemplateName !== '' && searchDocTypeId === '') {
    		         alert("Please choose the document name from the drop down");
    		         refresh = false;
    		       } else if(refresh ===  true){
                 location.href = location.href.substr(0, location.href.indexOf('?'))+"?mr_no=${param.mr_no}&_method=addPatientDocument&pageNum=1&docTypeId="+searchDocTypeId
                 +"&templateId="+searchTemplateId+"&templateName="+searchTemplateName;
                 searchTemplateId='';
                 searchDocTypeId='';
                 searchTemplateName='';
               }
        }
        		var genFormAutoComplete = null;
            var docTypeAutoComplete = null;
            var listJson = ${listJson}
            var doctypeJson = ${listJson}
            function autoFormname() {
            			if (empty(document.getElementById('gen_template_search'))) return;

            			YAHOO.example.ACJSArray = new function() {
            				var formsArray = {result: listJson};
            				datasource = new YAHOO.util.LocalDataSource(formsArray);
            				datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
            				datasource.responseSchema = {
            					resultsList : 'result',
            					fields : [ 	{key : 'template_name'},
            								{key : 'template_id'}
            							]
            				};

            				if (genFormAutoComplete != null) {
            					genFormAutoComplete.destroy();
            				}
            				genFormAutoComplete = new YAHOO.widget.AutoComplete('gen_template_search','genformscontainer', datasource);
            				genFormAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
            				genFormAutoComplete.useShadow = true;
            				genFormAutoComplete.minQueryLength = 0;
            				genFormAutoComplete.autoHighlight = false;
            				genFormAutoComplete.allowBrowserAutocomplete = false;
            				genFormAutoComplete.forceSelection = false;
            				genFormAutoComplete.resultTypeList = false;
            				genFormAutoComplete.queryMatchContains = true;
            				genFormAutoComplete.maxResultsDisplayed = 20;
            				if (genFormAutoComplete._elTextbox.value != '') {
            					genFormAutoComplete._bItemSelected = true;
            					genFormAutoComplete._sInitInputValue = genFormAutoComplete._elTextbox.value;
            				}

            				genFormAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
                        searchTemplateId = elItem[2].template_id;
                    });
            			}

            		}
            		function autoDocType() {
            			doctypeJson = removeDoctypeDuplicates(doctypeJson);
            			if (empty(document.getElementById('doc_type_search'))) return;

            			YAHOO.example.ACJSArray = new function() {
            				var formsArray = {result: doctypeJson};
            				datasource = new YAHOO.util.LocalDataSource(formsArray);
            				datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
            				datasource.responseSchema = {
            					resultsList : 'result',
            					fields : [ 	{key : 'doc_type_name'},
            								{key : 'doc_type_id'}
            							]
            				};

            				if (docTypeAutoComplete != null) {
            					docTypeAutoComplete.destroy();
            				}
            				docTypeAutoComplete = new YAHOO.widget.AutoComplete('doc_type_search','docttypecontainer', datasource);
            				docTypeAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
            				docTypeAutoComplete.useShadow = true;
            				docTypeAutoComplete.minQueryLength = 0;
            				docTypeAutoComplete.autoHighlight = false;
            				docTypeAutoComplete.allowBrowserAutocomplete = false;
            				docTypeAutoComplete.forceSelection = false;
            				docTypeAutoComplete.resultTypeList = false;
            				docTypeAutoComplete.queryMatchContains = true;
            				docTypeAutoComplete.maxResultsDisplayed = 20;
            				if (docTypeAutoComplete._elTextbox.value != '') {
            					docTypeAutoComplete._bItemSelected = true;
            					docTypeAutoComplete._sInitInputValue = docTypeAutoComplete._elTextbox.value;
            				}
            				docTypeAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
                       searchDocTypeId = elItem[2].doc_type_id;
                    });
            			}
            		}
            		function init() {
                			autoFormname();
                			autoDocType();
                		}
	</script>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init();">

	<jsp:useBean id="actionUrlMap" class="java.util.HashMap"/>
	<c:set target="${actionUrlMap}" property="mlc" value="MLCDocumentsAction.do"/>
	<c:set target="${actionUrlMap}" property="reg" value="RegistrationDocuments.do"/>
	<c:set target="${actionUrlMap}" property="insurance" value="InsuranceGenericDocuments.do"/>

	<c:set var="actionUrl" value="GenericDocumentsAction.do" />
	<c:if test="${specialized}">
		<c:set var="actionUrl" value="${actionUrlMap[documentType]}" />
		<c:set var="doc_type" value="${docTypeMap[documentType]}" />
	</c:if>

	<c:set var="templateslist" value="${pagedList.dtoList}" />
	<h1 style="float: left">Add Patient Document</h1>
	<c:url var="searchUrl" value="${actionUrl}" />
	<insta:patientsearch searchType="mrNo" fieldName="mr_no"
		searchUrl="${searchUrl}" buttonLabel="Find"
		searchMethod="addPatientDocument" />
	<insta:feedback-panel />
	<c:choose>
		<c:when test="${not empty param.patient_id}">
			<insta:patientdetails visitid="${param.patient_id}"
				showClinicalInfo="true" />
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails mrno="${param.mr_no}"
				showClinicalInfo="true" />
		</c:otherwise>
	</c:choose>
<table style="margin-top: 10px; margin-bottom: 10px;">
		<tr>
			<td>
				<div class="sfFieldSub" style="width: auto;">Template Name:</div>
			</td>
			<td>
				<div class="sfFieldSub" style="width: auto;">Document Type:</div>
			</td>
			<td></td>
		</tr>
		<tr>
			<td>
				<div class="autocomplete" style="margin-right: 10px;">
					<input type="text" id="gen_template_search" size="8" class="field"
						value=""  autocomplete="off" />
					<div id="genformscontainer"></div>
				</div>
			</td>
			<td>
				<div class="autocomplete" style="margin-right: 10px;">
					<input type="text" id="doc_type_search" size="8" class="field"
						value="" autocomplete="off" />
					<div id="docttypecontainer"></div>
				</div>
			</td>
			<td>
				<div style="margin-right: 10px;">
					<input onclick="genFormSearch();" value="Search" type="submit" />
				</div>
			</td>
		</tr>
	</table>
<c:if test="${!param.defaultScreen}">
	<form action="" autocomplete="off" name="addDocument">
		<input type="hidden" name="_method" value="add"/>
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
		<input type="hidden" name="insurance_id" value="${ifn:cleanHtmlAttribute(param.insurance_id)}"/>
		<input type="hidden" name="format" value=""/>
		<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}"/>
		<input type="hidden" name="prescription_id" value="${ifn:cleanHtmlAttribute(param.prescription_id)}"/>
		<input type="hidden" name="template_id" value=""/>

		<h2>Select a Template: </h2>
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<div class="resultList">
			<table class="dataTable" cellspacing="0" cellpadding="0" width="100%">
				<tr>
					<th>Select</th>
					<th>Template Name</th>
					<th>Document Type</th>
					<th>Format</th>
				</tr>

					<c:forEach var="template" items="${templateslist}"
						varStatus="status">

						<c:url var="addDocUrl" value="${actionUrl}">
							<c:param name="_method" value="add" />
							<c:param name="template_id" value="${template.map.template_id}" />
							<c:param name="format" value="${template.map.format}" />
							<c:param name="activeVisitId" value="${param.activeVisitId}" />
							<c:param name="insurance_id" value="${param.insurance_id}" />
							<c:choose>
								<c:when test="${not empty param.patient_id}">
									<c:param name="patient_id" value="${param.patient_id}" />
								</c:when>
								<c:when test="${not empty param.inactive_visit}">
									<c:param name="patient_id" value="${param.inactive_visit}" />
								</c:when>
								<c:otherwise>
									<c:param name="patient_id" value="" />
								</c:otherwise>
							</c:choose>
						</c:url>
						<c:set var="templateFormat" value="" />
						<c:set var="templateColor" value="" />

					<c:choose>
						<c:when test="${template.map.format == 'doc_hvf_templates'}">
							<c:set var="templateFormat" value="HVF Template"/>
							<c:set var="templateColor" value="hvf"/>
						</c:when>
						<c:when test="${template.map.format == 'doc_rich_templates'}">
							<c:set var="templateFormat" value="Rich Text Template"/>
							<c:set var="templateColor" value="richtext"/>
						</c:when>
						<c:when test="${template.map.format == 'doc_pdf_form_templates'}">
							<c:set var="templateFormat" value="PDF Form Template"/>
							<c:set var="templateColor" value="pdfform"/>
						</c:when>
						<c:otherwise >
							<c:set var="templateFormat" value="RTF Template"/>
							<c:set var="templateColor" value="rtf"/>
						</c:otherwise>
					</c:choose>
					<tr class="${status.first ? 'firstRow' : ''}"
					    title="${template.map.template_name}">
						<td>
							<input type="radio" name="selectTemplate" onclick="setTemplateParams(this, ${template.map.template_id}, '${template.map.format}')"/>
						</td>
						<td>${template.map.template_name}</td>
						<td>${template.map.doc_type_name}
						<input type="hidden"
            								name="doct_type_name" id="doct_type_name"
            								value="${template.map.doc_type_name}">
						</td>

						<td><font class="${templateColor}">${templateFormat}</font></td>
					</tr>
				</c:forEach>
				<c:if test="${uploadFile == true}">
					<tr>
						<td>
							<input type="radio" name="selectTemplate" onclick="setTemplateParams(this, '${template.map.template_id}', 'doc_fileupload')"/>
						</td>
						<td colspan="3" >Uploadfile</td>
					</tr>
				</c:if>
				<c:if test="${docLink == true}">
					<tr>
						<td>
							<input type="radio" name="selectTemplate" onclick="setTemplateParams(this, '${template.map.template_id}', 'doc_link')"/>
						</td>
						<td colspan="3">Link to External Document</td>
					</tr>
				</c:if>
			</table>
			<insta:noresults message="No templates found" hasResults="${not empty templateslist}"/>
		</div>
		<div style="margin-top: 10px; display: ${specialized ? 'none' : 'block'};">
			<%-- add document for patient will not shown for specialized document at all --%>
			<h2>Add Document for: </h2>
			Patient <input type="radio" name="addDocFor" value="patient" ${((empty param.addDocFor || param.addDocFor == 'patient' ) && empty activeVisitBean) ? 'checked' : ''}>
			<c:if test="${not empty visitsList}">
				Visit <input type="radio" name="addDocFor" value="visit" ${(param.addDocFor == 'visit' || not empty activeVisitBean) ? 'checked' : ''}>
				<select name="visit_dropdown" id="visit_dropdown" class="dropdown">
					<option value="">---Select Visit---</option>
					<c:forEach var="visit" items="${visitsList}">
						<option value="${visit.map.patient_id}" class="${visit.map.status == 'I' ? 'status_I' : ''}"
							${((not empty param.patient_id && param.patient_id == visit.map.patient_id) ||(activeVisitBean.map.patient_id == visit.map.patient_id)) ? 'selected' : ''}>
								${visit.map.patient_id}-${visit.map.doctor_name}-(<fmt:formatDate pattern="dd-MM-yyyy" value="${visit.map.reg_date}"/>)
						</option>
					</c:forEach>
				</select>
			</c:if>
			<a href="${cpath}/pages/GenericDocuments/GenericDocumentsAction.do?_method=searchPatientGeneralDocuments&mr_no=${ifn:cleanURL(param.mr_no)}">Generic Documents List</a>
		<c:if test="${uploadFile == true}">
    			&nbsp;|&nbsp;
    				<a style="cursor: pointer;" onclick="return openNewUploadDocumentPopUp('${param.mr_no}')" />
    					Upload Documents
    				</a>
    			</c:if>
    		</div>
		</div>
    <div id="noSearchResults" style="display: none">
			<insta:noresults hasResults="" />
		</div>
		<div class="screenActions" >
		<button type="button" name="add" accesskey="A" onclick="return validate()">
		<label><u><b>A</b></u>dd</label></button>&nbsp;

		</div>
	</form>
</c:if>
</body>
</html>
