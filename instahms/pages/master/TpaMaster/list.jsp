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
<title>TPA/Sponsor Master List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		
	<script type="text/javascript">
	    
		var tpaCenterapplicable = ${max_centers_inc_default > 1 ? 'true' : 'false'};
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "/master/TpaMaster.do?_method=show",
				onclick : null,
				description : "View and/or Edit the contents of this Tpa/Sponsor"
				},
			Center_Association : {
				title : "Center Association",
				imageSrc : "icons/Edit.png",
				href : "/master/TpaMasterCenterAssociation.do?_method=getScreen",
				onclick : null,
				description : "Center Association of this Tpa/Sponsor",
				show : tpaCenterapplicable
				}
		};

		function init() {

			createToolbar(toolBar);

		}
		 
		var tpaNameJson = <%= request.getAttribute("tpaMastersLists") %>;
		var autoComp = null;
		function tpaNameAutocomplete() {
			YAHOO.example.tpaNamesArray = [];
			YAHOO.example.tpaNamesArray.length =tpaNameJson.length;

			for (var i=0;i<tpaNameJson.length;i++) {
				var item = tpaNameJson[i]
					YAHOO.example.tpaNamesArray[i] = item["tpa_name"];
			}

			YAHOO.example.ACJSArray = new function() {
			// Instantiate first JS Array DataSource
			datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.tpaNamesArray);
			var autoComp = new YAHOO.widget.AutoComplete('tpa_name','tpaNameContainer', datasource);
			autoComp.prehighlightClassName = "yui-ac-prehighlight";
			autoComp.typeAhead = true;
			autoComp.useShadow = true;
			autoComp.allowBrowserAutocomplete = false;
			autoComp.minQueryLength = 1;
			autoComp.maxResultsDisplayed = 20;
			autoComp.autoHighlight = false;
			autoComp.forceSelection = false;
			autoComp.textboxFocusEvent.subscribe(function() {
					var sInputValue = YAHOO.util.Dom.get('tpa_name').value;
					if(sInputValue.length === 0) {
						var oSelf = this;
						setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
					}

			});
		}
	}
	</script>
</head>
<body onload="tpaNameAutocomplete(); init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>TPA / Sponsor Master</h1>

<insta:feedback-panel/>

<form name="TpaSponsorForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="TpaSponsorForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">TPA/Sponsor Name:</div>
				<div class="sboFieldInput">
					<input type="text" name="tpa_name" id="tpa_name" value="${ifn:cleanHtmlAttribute(param.tpa_name)}" />
					<input type="hidden" name="tpa_name@op" value="ico"/>
					<div id="tpaNameContainer" style="width: 220px"></div>
				</div>
			</div>
			<div class="sboField" style="height:69">
				<div class="sboFieldLabel">Status:</div>
				<div class="sboFieldInput">
					<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
						<input type="hidden" name="status@op" value="in"/>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table  class="searchFormTable">
				<tr>
					<td class="last">
						<div class="sfLabel">Sponsor Type:</div>
						<div class="sfField">
							<input type="hidden" name="sponsor_type_id@cast" value="Y"/> 
							<insta:selectdb  name="sponsor_type_id" value="${sponsor_type_id}" table="sponsor_type"
						     valuecol="sponsor_type_id" displaycol="sponsor_type_name" filtercol="status"  filtervalue="A"
						      dummyvalue="----All----" dummyvalueId="" orderby="sponsor_type_name"/>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>
	
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
			<tr onmouseover="hideToolBar();">
				<th>#</th>
				<insta:sortablecolumn name="tpa_name" title="TPA/Sponsor Name"/>
				<th>Sponsor Type</th>
				<th>Email-id</th>
				<th>ContactNo</th>
			</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable', {tpa_id: '${record.tpa_id}'},'');">

					<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${record.tpa_name}
					</td>
					<td>${record.sponsor_type_name}</td>
					<td>${record.contact_email}</td>
					<td>${record.contact_mobile}</td>
				</tr>
			</c:forEach>
		</table>
	</div>

		<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		<c:url var="Url" value="TpaMaster.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float: left">
			<a href="${Url}">Add New TPA/Sponsor</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

</form>
</body>
</html>
