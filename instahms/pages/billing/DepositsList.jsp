<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
	<title><insta:ltext key="billing.patientdeposits.depositlists.patientdeposits.instahms"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="billing.depositlist"/>
	<script>
	var toolbarOptions = getToolbarBundle("js.billing.depositlist.toolbar");
	var loadToolBar = true;
	</script>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="billing/commonFilters.js"/>
	<insta:link type="script" file="billing/deposits.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<style type="text/css">
		.autocomplete1 {
			width:130px;
			padding-bottom:10px;
		}
	</style>
<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
<insta:js-bundle prefix="billing.deposits"/>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="patientList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty patientList}"/>
<c:set var="mod_adv_packages" value="${preferences.modulesActivatedMap.mod_adv_packages}"/>

<body onload="initSearch(); showFilterActive(document.searchForm)" >
<c:set var="visitstatus">
<insta:ltext key="billing.patientdeposits.depositlists.active"/>,
<insta:ltext key="billing.patientdeposits.depositlists.inactive"/>,
<insta:ltext key="billing.patientdeposits.depositlists.withoutvisits"/>
</c:set>
<c:set var="mrno">
	<insta:ltext key="ui.label.mrno"/>
	</c:set>
<div class="pageHeader"><insta:ltext key="billing.patientdeposits.depositlists.patientdeposits"/></div>

<form method="GET" action="${cpath}/pages/BillDischarge/Deposits.do" name="searchForm">

<input type="hidden" name="_method" value="getDeposits" id="_method">
<input type="hidden" name="_searchMethod" value="getDeposits" id="_searchMethod">
<input type="hidden" name="mrNoLink" value="${mrNoLink}">
<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

<!-- new css and new list pattern-->

<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResults}">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="billing.patientdeposits.depositlists.mrno.or.patientname"/>: </div>
			<div class="sboFieldInput">
				<div id="mrnoAutoComplete">
					<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
					<div id="mrnoContainer"></div>
				</div>
			</div>
		</div>
	</div>
<div id="optionalFilter" style="clear:both; display:${hasResults ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel"><insta:ltext key="billing.patientdeposits.depositlists.deposittotal"/>:</div>
				<div class="sfField">
						<input type="text" name="hosp_total_deposits" size="10" onkeypress="return enterNumOnly(event);"
						value="${ifn:cleanHtmlAttribute(param.hosp_total_deposits)}" class="number"/> <insta:ltext key="billing.patientdeposits.depositlists.ormore"/>
						<input type="hidden" name="hosp_total_deposits@type" value="numeric"/>
						<input type="hidden" name="hosp_total_deposits@op" value="ge"/>
				</div>
			</td>
			<td>
				<div class="sfLabel"> <insta:ltext key="billing.patientdeposits.depositlists.depositavailable"/>:</div>
				<div class="sfField">
					<input type="text" name="hosp_total_balance" size="10" value="${ifn:cleanHtmlAttribute(param.hosp_total_balance)}"
	                 onkeypress="return enterNumOnly(event);" class="number"/>  <insta:ltext key="billing.patientdeposits.depositlists.ormore"/>
						 <input type="hidden" name="hosp_total_balance@type" value="numeric"/>
						 <input type="hidden" name="hosp_total_balance@op" value="ge"/>

				</div>
			</td>
			<td>
				<div class="sfLabel"><insta:ltext key="billing.patientdeposits.depositlists.status"/>:</div>
				<div class="sfField">
						 <insta:checkgroup name="visit_status" selValues="${paramValues.visit_status}"
						 opvalues="A,I,N" optexts="${visitstatus}"/>
				</div>
			</td>
			<td>
				<div class="sfLabel"><insta:ltext key="billing.patientdeposits.depositlists.country"/>:</div>
				<div class="sfField">
					<div id="autocountry">
						<input type="text" name="country_name" id="country_name"  value="${ifn:cleanHtmlAttribute(param.country_name)}" />
						<div id="country_dropdown" class="autocomplete1"></div>
						<input type="hidden" name="country_name@op" value="ilike">
					</div>
				</div>
				<div class="sfLabel" style="margin-top:30px"><insta:ltext key="billing.patientdeposits.depositlists.state"/>:</div>
				<div class="sfField">
					<div id="autostate">
						<input type="text" name="state_name" id="state_name" value="${ifn:cleanHtmlAttribute(param.state_name)}" />
						<div id="state_dropdown" class="autocomplete1"></div>
					</div>
				</div>
			</td>
			<td class="last">
				<div class="sfLabel"><insta:ltext key="billing.patientdeposits.depositlists.city"/>:</div>
				<div class="sfField">
					<div class="autocity" >
						<input type="text" name="city_name" id="city_name" value="${ifn:cleanHtmlAttribute(param.city_name)}" />
						<div id="city_dropdown" class="autocomplete1"></div>
					</div>
				</div>
				<div class="sfLabel" style="margin-top:30px"><insta:ltext key="billing.patientdeposits.depositlists.area"/>:</div>
				<div class="sfField">
					<div id="autoarea">
						<input type="text" name="patient_area" id="patient_area" value="${ifn:cleanHtmlAttribute(param.patient_area)}"/>
						<div id="area_dropdown" class="autocomplete1"></div>
					</div>
				</div>
			</td>
			</tr>
	</table>
</div>
</insta:search>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable"
				onmouseover="hideToolBar('');" >
				<tr onmouseover="hideToolBar('');">
					<insta:sortablecolumn name="mr_no" title="${mrno}"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<th><insta:ltext key="billing.patientdeposits.depositlists.age.or.gender"/></th>
					<th><insta:ltext key="billing.patientdeposits.depositlists.mobileno"/></th>
					<th class="number"><insta:ltext key="billing.patientdeposits.depositlists.tot.deposits"/></th>
					<c:if test="${prefs.depositChequeRealizationFlow == 'Y'}">
						<th class="number"><insta:ltext key="billing.patientdeposits.depositlists.unrealizedamount"/></th>
					</c:if>
					<c:if test="${mod_adv_packages == 'Y'}">
					<th class="number"><insta:ltext key="billing.patientdeposits.depositlists.pkg.deposit"/></th>
					<th class="number"><insta:ltext key="billing.patientdeposits.depositlists.pkg.availabledeposits"/></th>
					</c:if>
					<th class="number"><insta:ltext key="billing.patientdeposits.depositlists.tot.availabledeposits"/></th>
				</tr>

				<c:forEach var="patient" items="${patientList}" varStatus="status">
				<c:set var="flagColor" >
				<c:choose>
					<c:when test="${patient.map.visit_status == 'A'}">empty</c:when>
					<c:when test="${patient.map.visit_status == 'I'}">grey</c:when>
					<c:when test="${patient.map.visit_status == 'N'}">blue</c:when>
				</c:choose>
				</c:set>
				<tr class="${status.index == 0 ?'firstRow': ''}  ${status.index % 2 == 0? 'even':'odd' }"
					onclick="showToolbar(${status.index}, event, 'resultTable',{mrNo: '${patient.map.mr_no}'});" onmouseover="hideToolBar(${status.index})" id="toolbarRow${status.index}">

					<td>${patient.map.mr_no}</td>

						<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/><insta:truncLabel value="${patient.map.full_name}" length="30"/></td>
						<c:set var="agein" value=""/>
						<c:choose>
							<c:when test="${patient.map.agein eq 'D'}">
								<c:set var="agein" value="Days"/>
							</c:when>
							<c:when test="${patient.map.agein eq 'M'}">
								<c:set var="agein" value="Months"/>
							</c:when>
							<c:otherwise>
								<c:set var="agein" value="Years"/>
							</c:otherwise>
						</c:choose>

						<td>${patient.map.age} ${agein}/${patient.map.patient_gender}</td>
						<td>${patient.map.patient_phone}</td>
						<td class="number">${patient.map.hosp_total_deposits + patient.map.hosp_unrealized_amount}</td>
						<c:if test="${prefs.depositChequeRealizationFlow == 'Y'}">
							<td class="number">${patient.map.hosp_unrealized_amount}</td>
						</c:if>
						<c:if test="${mod_adv_packages == 'Y'}">
						<td class="number">${patient.map.package_deposit}</td>
						<td class="number">${patient.map.package_deposit-patient.map.package_setoff}</td>
						</c:if>
						<td class="number">${patient.map.hosp_total_balance}</td>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${param.method == 'getDeposits'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

</div>
	<script>
	var areaList = ${areaList};
	var countryList = ${countryList};
	var stateList = ${stateList};
	var cityList = ${cityList};
	</script>

</form>
<table width="100%">
<tr width="100%">
<td width="72%">&nbsp;</td>
<div class="legend" style="display:${hasResults ? 'block':'none'}" >
      <td  align="right" width="28%">
	<div class="flag"><img src='${cpath}/images/grey_flag.gif'></img></div>
	<div class="flagText"><insta:ltext key="billing.patientdeposits.depositlists.inactivepatients"/></div>
	<div class="flag"><img src='${cpath}/images/blue_flag.gif'></img></div>
	<div class="flagText"><insta:ltext key="billing.patientdeposits.depositlists.patientwithoutvisit"/></div>
     </td>
</div>
</tr>
</table>
</body>
</html>

