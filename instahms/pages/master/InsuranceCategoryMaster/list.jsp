<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>'/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Network/Plan Type  Master - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script type="text/javascript">
		var InsCatCenterapplicable = ${max_centers_inc_default > 1 ? 'true' : 'false'};
		var toolBar = {
			Edit : {
					title : "View/Edit",
					imageSrc : "icons/Edit.png",
					href : "/master/InsuranceCatMaster.do?_method=show",
					onclick : null,
					description : "View and/or Edit the contents of Insurance Plan"
				},
			Center_Applicability: {
					title: "Center Applicability",
					imageSrc : "icons/Edit.png",
					href: 'master/InsCatCenter.do?_method=getScreen',
					onclick: null,
					description : 'Center Applicability of this Network/Plan Type',
					show : InsCatCenterapplicable
			}
		};

		var insuNameList = ${insuranceCompaniesLists};
		var catNameList = ${categoryLists};

		var itAutoComplete = null;
		function initInsuAutoCmplt(){
			if (itAutoComplete != undefined) {
				itAutoComplete.destroy();
			}

			YAHOO.example.itemArray = [];
			var i=0;
			for(var j=0; j<insuNameList.length; j++){
				YAHOO.example.itemArray.length = i+1;
				YAHOO.example.itemArray[i] = insuNameList[j];
				i++;
			}

				YAHOO.example.ACJSArray = new function() {
					datasource = new YAHOO.util.LocalDataSource({result : YAHOO.example.itemArray});
					datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
					datasource.responseSchema = {
						resultsList : 'result',
						fields : [ 	{key : 'insurance_co_name'},
									{key : 'insurance_co_id'}
								]
					};

					itAutoComplete = new YAHOO.widget.AutoComplete('_insu_comp_tbox','_insu_comp_dropdown', datasource);
					itAutoComplete.prehightlightClassName = "yui-ac-prehighlight";
					itAutoComplete.typeAhead = true;
					itAutoComplete.useShadow = true;
					itAutoComplete.allowBrowserAutocomplete = false;
					itAutoComplete.minQueryLength = 0;
					itAutoComplete.maxResultsDisplayed = 20;
					itAutoComplete.autoHighlight = true;
					itAutoComplete.forceSelection = true;
					itAutoComplete.animVert = false;
					itAutoComplete.useIFrame = true;
					itAutoComplete.formatResult = Insta.autoHighlight;

					itAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
						var ele = new Array();
					  	ele= elItem[2];
					  	document.InsuranceCatForm.insurance_co_name.value = ele[0];
						document.InsuranceCatForm.insurance_co_id.value = ele[1];

					});
			       itAutoComplete.selectionEnforceEvent.subscribe(function(){
						document.InsuranceCatForm.insurance_co_id.value = '';
					});
			}
		}

		var strtAutoComplete = null;
		function initCatAutoCmplt(){
			if (strtAutoComplete != undefined) {
				strtAutoComplete.destroy();
			}

			YAHOO.example.itemArray = [];
			var i=0;
			for(var j=0; j<catNameList.length; j++){
				YAHOO.example.itemArray.length = i+1;
				YAHOO.example.itemArray[i] = catNameList[j];
				i++;
			}

				YAHOO.example.ACJSArray = new function() {
					datasource = new YAHOO.util.LocalDataSource({result : YAHOO.example.itemArray});
					datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
					datasource.responseSchema = {
						resultsList : 'result',
						fields : [ 	{key : 'category_name'},
									{key : 'category_id'}
								]
					};

					strtAutoComplete = new YAHOO.widget.AutoComplete('_category_name_tbox','_category_name_dropdown', datasource);
					strtAutoComplete.prehightlightClassName = "yui-ac-prehighlight";
					strtAutoComplete.typeAhead = true;
					strtAutoComplete.useShadow = true;
					strtAutoComplete.allowBrowserAutocomplete = false;
					strtAutoComplete.minQueryLength = 0;
					strtAutoComplete.maxResultsDisplayed = 20;
					strtAutoComplete.autoHighlight = true;
					strtAutoComplete.forceSelection = true;
					strtAutoComplete.animVert = false;
					strtAutoComplete.useIFrame = true;
					strtAutoComplete.formatResult = Insta.autoHighlight;

					strtAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
					  	var ele = new Array();
					  	ele= elItem[2];
					  	document.InsuranceCatForm.category_name.value = ele[0];
						document.InsuranceCatForm.category_id.value = ele[1];
					});
			      	strtAutoComplete.selectionEnforceEvent.subscribe(function(){
			      		var ele = new Array();
					  	ele= elItem[2];
						document.InsuranceCatForm.category_id.value = ele[1];
					});

			}
		}

		function init() {
			initInsuAutoCmplt();
			createToolbar(toolBar);
		}
	</script>
</head>

<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

	<h1>Network/Plan Type Master</h1>

	<insta:feedback-panel/>

	<form name="InsuranceCatForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="InsuranceCatForm" >
				<div class="sboField">
					<div class="sboFieldLabel" style="padding-top:2px;">Insurance Comp Name:</div>
					<div class="sboFieldInput">
						<div id="_insu_comp_wrapper" >
							<input type="text" name="_insu_comp_tbox" id="_insu_comp_tbox" value="" />
							<div id="_insu_comp_dropdown"></div>
							<input type="hidden" name="insurance_co_name" id="insurance_co_name"  />
							<input type="hidden" name="insurance_co_id"  id="insurance_co_id"  />
						</div>
					</div>
				</div>
			</div>
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Network/Plan Type:</div>
					<div class="sboFieldInput">
						<div id="_insu_comp_wrapper" >
							<input type="text" name="category_name" id="category_name"  />
							<input type="hidden" name="category_name@op"  value="ilike"  />
						</div>
					</div>
			</div>
			<div class="sboField">
					<div class="sboFieldLabel">Status:</div>
					<div class="sboFieldInput">
						<insta:selectoptions name="status" value="${param.status}" opvalues="A,I"
							optexts="Active,Inactive" dummyvalue="(All)" />
					</div>
				</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList" >
			<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="category_name" title="Network/Plan Type"/>
					<insta:sortablecolumn name="insurance_co_name" title="Insurance Comp Name"/>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable', {category_id: '${record.category_id}'},'');">

						<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.category_name}
						</td>
						<td>
							${record.insurance_co_name}
						</td>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${empty pagedList.dtoList}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="Url" value="InsuranceCatMaster.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float: left">
			<a href="${Url}">Add New Network/Plan Type</a>
		</div>

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
</form>
</body>
</html>




