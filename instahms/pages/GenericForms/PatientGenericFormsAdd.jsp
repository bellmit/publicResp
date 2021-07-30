<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Choose Patient Generic Form - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="css" file="widgets.css"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<insta:js-bundle prefix="registration.patient"/>
	<style type="text/css">
		.genSearch {
			padding-bottom: 3px;
		}
	</style>
	<script type="text/javascript">
		function search(e) {
			if(e.keyCode == 13) {
				genFormSearch();
			}
		}
		var sort_by = function(field, reverse, primer){
			var key = function (x) {return primer ? primer(x[field]) : x[field]};
			return function (a,b) {
			var A = key(a), B = key(b);
				return (A < B ? -1 : (A > B ? 1 : 0)) * [1,-1][+!!reverse];
			}
		}
		function removeDoctypeDuplicates(doctypeJson) {
			doctypeJson.sort(sort_by('doc_type', false, function(x){return x.toUpperCase()}))
			for (var i=doctypeJson.length-1; i>0; i--) {
				var record1 = doctypeJson[i];
				var record2 = doctypeJson[i-1];
				if (record1.doc_type == record2.doc_type) {
					doctypeJson.splice(i-1, 1);
				}
			}
			return doctypeJson;
		}
		function genFormSearch() {
			document.getElementById('noSearchResults').style.display = 'none';
			var filterString = document.getElementById('gen_form_search').value.trim().toUpperCase();
			var filterDocType = document.getElementById('doc_type_search').value.trim().toUpperCase();
			var genFormsList = document.getElementById('genFormsList').getElementsByTagName('tr');
			var genFormsList_l = genFormsList.length;
			var filter_count = 1;
			for (var i = 1; i < genFormsList_l; i++) {
				var genform = genFormsList[i].title;
				var docType = document.getElementsByName('doct_type_name')[i-1].value;
				if(!empty(filterString) || !empty(filterDocType)){
					if(!empty(filterString) && genform.toUpperCase().indexOf(filterString) > -1){
						genFormsList[i].style.display = '';
						genFormsList[i].getElementsByTagName("td")[0].innerHTML = filter_count++;
					}else if(!empty(filterDocType) && docType.toUpperCase().indexOf(filterDocType) > -1 ){
						genFormsList[i].style.display = '';
						genFormsList[i].getElementsByTagName("td")[0].innerHTML = filter_count++;
					}else{
						genFormsList[i].style.display = 'none';
					}
				}else{
					if(genform.toUpperCase().indexOf(filterString) > -1) {
						genFormsList[i].style.display = '';
						genFormsList[i].getElementsByTagName("td")[0].innerHTML = filter_count++;
					} else {
						genFormsList[i].style.display = 'none';
					}
				}
			}
			if(filter_count == 1) {
				document.getElementById('noSearchResults').style.display = 'block';
			}
		}

		var genFormAutoComplete = null;
		var docTypeAutoComplete = null;
		var listJson = ${listJson}
		var doctypeJson = ${listJson}
		function autoFormname() {
			if (empty(document.getElementById('gen_form_search'))) return;

			YAHOO.example.ACJSArray = new function() {
				var formsArray = {result: listJson};
				datasource = new YAHOO.util.LocalDataSource(formsArray);
				datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
				datasource.responseSchema = {
					resultsList : 'result',
					fields : [ 	{key : 'form_name'},
								{key : 'id'}
							]
				};

				if (genFormAutoComplete != null) {
					genFormAutoComplete.destroy();
				}
				genFormAutoComplete = new YAHOO.widget.AutoComplete('gen_form_search','genformscontainer', datasource);
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
					genFormSearch();
				});
			}
		}
		function autoDocType() {
			doctypeJSON = removeDoctypeDuplicates(doctypeJson);
			if (empty(document.getElementById('doc_type_search'))) return;

			YAHOO.example.ACJSArray = new function() {
				var formsArray = {result: doctypeJSON};
				datasource = new YAHOO.util.LocalDataSource(formsArray);
				datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
				datasource.responseSchema = {
					resultsList : 'result',
					fields : [ 	{key : 'doc_type_name'},
								{key : 'doc_type'}
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
					genFormSearch();
				});
			}
		}

		function init() {
			autoFormname();
			autoDocType();
		}
	</script>
</head>
<body onload="init()">
	<div class="pageHeader" style="float: left;">Choose Patient Generic Form</div>
	<c:url var="searchUrl" value="GenericFormsAction.do" />
	<insta:patientsearch searchType="visit" fieldName="patient_id" searchUrl="${searchUrl}" buttonLabel="Find"
		searchMethod="getChooseGenericFormScreen"/>
	<insta:feedback-panel/>

	<insta:patientdetails  visitid="${param.patient_id}" showClinicalInfo="true"/>
	<c:if test="${!param.defaultScreen}">
		<table style="margin:10px;">
			<tr>
				<td>
					<div class="sfFieldSub" style="width: auto;">Form Name:</div>
				</td>
				<td>
					<div class="sfFieldSub" style="width:auto;">Document Type:</div>
				</td>	
				<td>
				</td>
			</tr>
			<tr>
				<td>
				<div class="autocomplete"  style="margin-right:10px;">
						<input type="text" id="gen_form_search" size="8" class="field"
							value="" onkeydown="search(event)" autocomplete="off"/>
						<div id="genformscontainer"></div>
				</div>
				</td>
				<td>
					<div class="autocomplete"  style="margin-right:10px;">
						<input type="text" id="doc_type_search" size="8" class="field"
							value="" onkeydown="search(event)" autocomplete="off"/>
						<div id="docttypecontainer"></div>
					</div>
				</td>
				<td>
					<div style="padding-top: 5px;">
						<input onclick="genFormSearch();" value="Search" type="submit"/>
					</div>
				</td>
			</tr>
		</table>
		<div class="resultList">
			<table class="dataTable" id="genFormsList" cellspacing="0" cellpadding="0" width="100%">
				<tr>
					<th>#</th>
					<th>Form Name</th>
					<th>Document Type</th>
				</tr>

				<c:forEach var="form" items="${list}" varStatus="status">
					<tr title="${form.map.form_name}">
						<td>${status.index+1}</td>
						<td>
							<c:url var="addformUrl" value="GenericFormsAction.do">
								<c:param name="_method" value="addOrEditGenericForm"/>
								<c:param name="insta_form_id" value="${form.map.id}"/>
								<c:param name="patient_id" value="${param.patient_id}"/>
							</c:url>
							<a href='<c:out value="${addformUrl}"/>' title="Add Generic Form">${form.map.form_name}</a>
						</td>
						<td>
							${form.map.doc_type_name}
							<input type="hidden" name="doct_type_name" id="doct_type_name" value="${form.map.doc_type_name}"> 
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div id="noSearchResults" style="display:none">
			<insta:noresults hasResults=""/>
		</div>
		<div class="screenActions">
			<c:url var="listUrl" value="GenericFormsAction.do">
				<c:param name="_method" value="list"/>
				<c:param name="patient_id" value="${param.patient_id}"/>
			</c:url>
			<a href='<c:out value="${listUrl}"/>' title="Generic Form List">Generic Form List</a>
		</div>
	</c:if>
</body>
</html>