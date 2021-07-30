<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>

	<title><insta:ltext key="laboratory.equipmentqualitytest.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="script" file="ajax.js" />
	<insta:link type="script" file="dashboardColors.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />

	<script>

		var existingEquipments = ${existingEquipments};
		var category = '${category == 'DEP_LAB' ? 'Lab' : 'Rad'}';
		var equipmentToolbar = {
			Equipment: {
				title: "Record New Quality Data",
				imageSrc: "icons/Add.png",
				href: 'Diagnostic'+category+'Module/'+category+'EquipmentQAAction.do?_method=add',
				description: "Record New Quality Data"
			}
		};

		var conductedToolbar = {
			Conducted: {
				title: "View/Edit Quality data",
				imageSrc: "icons/Edit.png",
				href: 'Diagnostic'+category+'Module/'+category+'EquipmentQAAction.do?_method=show',
				description: "View/Edit Quality data"
			}
		};

		function create() {
			createToolbar(equipmentToolbar, 'equipmentToolbar');
			createToolbar(conductedToolbar, 'conductedToolbar');
		}

		function initEquipmentAutoComplete() {
			YAHOO.example.eqNamesArray = [];
			YAHOO.example.eqNamesArray.length =existingEquipments.length;

			for (var i=0;i<existingEquipments.length;i++) {
				var item = existingEquipments[i]
					YAHOO.example.eqNamesArray[i] = item["equipment_name"];
			}

			YAHOO.example.ACJSArray = new function() {
				// Instantiate first JS Array DataSource
				datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.eqNamesArray);
				var autoComp = new YAHOO.widget.AutoComplete('equipment_name','equipmentContainer', datasource);
				autoComp.prehighlightClassName = "yui-ac-prehighlight";
				autoComp.typeAhead = true;
				autoComp.useShadow = true;
				autoComp.allowBrowserAutocomplete = false;
				autoComp.minQueryLength = 1;
				autoComp.maxResultsDisplayed = 20;
				autoComp.autoHighlight = false;
				autoComp.forceSelection = false;
				autoComp.textboxFocusEvent.subscribe(function() {
						var sInputValue = YAHOO.util.Dom.get('equipment_name').value;
						if(sInputValue.length === 0) {
							var oSelf = this;
							setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
						}

				});
			}
		}

	</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="equipmentsList" value="${pagedList.dtoList}" />
<c:set var="hasResults" value="${not empty equipmentsList}"/>
<c:set var="completionStatus">
 <insta:ltext key="laboratory.equipmentqualitytest.list.completed"/>,
 <insta:ltext key="laboratory.equipmentqualitytest.list.pending"/>
</c:set>
<body onload="create();initEquipmentAutoComplete();" class="yui-skin-sam">
	<div class="pageHeader"><insta:ltext key="laboratory.equipmentqualitytest.list.equipmentqualitytest"/></div>
	<insta:feedback-panel/>

	<form method="GET" name="equipmentQAForm">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="equipmentQAForm" optionsId="optionalFilter" closed="${hasResults}">

	<div class="searchBasicOpts" >
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="laboratory.equipmentqualitytest.list.equipment"/></div>
			<div class="sboFieldInput">
				<div id="equipmentAutoComplete">
					<input type="text" name="equipment_name" id="equipment_name"
						value="${ifn:cleanHtmlAttribute(param.equipment_name)}" style="width: 120px" />
					<input type="hidden" name="equipment_name@op" value="ilike" />
					<div id="equipmentContainer" style="width: 220px"></div>
				</div>
			</div>
		</div>
	 </div>
	 <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.equipmentqualitytest.list.completionstatus"/> :</div>
							<div class="sfField">
							<insta:checkgroup name="test_record_complete" selValues="${paramValues.test_record_complete}"
								opvalues="Y,N" optexts="${completionStatus}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.equipmentqualitytest.list.conductiondate"/>:</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="laboratory.equipmentqualitytest.list.from"/>:</div>
							<insta:datewidget name="conducted_on" id="conducted_on1" value="${paramValues.conducted_on[0]}"/>
							<input type="hidden" name="conducted_on@type" value="date"/>
							<input type="hidden" name="conducted_on@op" value="ge"/>
							<input type="hidden" name="conducted_on@cast" value="y"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="laboratory.equipmentqualitytest.list.to"/>:</div>
							<insta:datewidget name="conducted_on" id="conducted_on2" value="${paramValues.conducted_on[1]}"/>
							<input type="hidden" name="conducted_on@op" value="le"/>
						</div>
					</td>
				</tr>
			</table>
	</div>

	 </insta:search>

		<table id="equipmentsList" class="resultList" cellspacing="0" cellpadding="0" onmouseover="hideToolBar('');" style="empty-cells: show">
			<tr onmouseover="hideToolBar('');">
				<th><insta:ltext key="laboratory.equipmentqualitytest.list.equipment"/></th>
				<th><insta:ltext key="laboratory.equipmentqualitytest.list.conductedon"/></th>
				<th><insta:ltext key="laboratory.equipmentqualitytest.list.conductedby"/></th>
			</tr>
			<c:set var="rowIndex" value="0"/>
			<c:forEach items="${equipmentsList}" var="equipment" varStatus="st">
				<c:set var="rowIndex" value="${rowIndex + 1}"/>
				<tr class="${st.first ? 'firstRow' : ''}"
					 onclick="showToolbar(${rowIndex}, event, 'equipmentsList',
								{equipmentId: '${equipment.equipmentId}'},[true],'equipmentToolbar')"
								onmouseover="hideToolBar(${rowIndex})" id="toolbarRow${rowIndex}">
					<td colspan="3">${equipment.equipmentName }</td>
				</tr>
				<c:set var="equipmentDetails" value="${equipment.equipmentDetails }"/>
				<c:if test="${not empty equipment.equipmentDetails }">
				<c:forEach items="${equipmentDetails}" var="equipmentConducted">
					<c:set var="rowIndex" value="${rowIndex + 1}"/>
					<c:set var="flagColor">
						<c:choose>
							<c:when test="${equipmentConducted.map.test_record_complete eq 'N'}">red</c:when>
							<c:otherwise>empty</c:otherwise>
						</c:choose>
					</c:set>
					<tr onclick="showToolbar(${rowIndex}, event, 'equipmentsList',
										{equipmentId: '${equipmentConducted.map.equipment_id}',
										eqConductedId:'${equipmentConducted.map.equipment_conducted_id}'}
										,[true],'conductedToolbar')"
										onmouseover="hideToolBar(${rowIndex})" id="toolbarRow${rowIndex}">
						<td class="indent" >&nbsp;</td>
						<td valign="top" class="subResult">
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							${equipmentConducted.map.conducted_on }
						</td>
						<td valign="top" class="subResult">${equipmentConducted.map.conducted_by }</td>
					</tr>
				</c:forEach>
				</c:if>
			</c:forEach>
		</table>
	</form>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="laboratory.equipmentqualitytest.list.pending"/></div>
	</div>
	<insta:noresults hasResults="${hasResults}"/>
</body>
</html>