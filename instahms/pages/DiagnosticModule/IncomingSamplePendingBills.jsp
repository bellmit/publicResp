<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>

<head>
<title><insta:ltext key="laboratory.incomingsamplependingbills.search.title"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="script" file="dashboardColors.js" />
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
<jsp:include page="/pages/Common/BillNoPrefix.jsp" />


<script type="text/javascript">
incomingHospitalJSON = ${incomingHospitalJSON};
function init(){
	populateIncomingHospital('labName','labContainer');
	enableBillStatus();
	enableBillType();
	createToolbar(toolbar);
}

function doSearch() {

	if (!doValidateDateField(document.forms[0].fdate))
		return false;
	if (!doValidateDateField(document.forms[0].tdate))
		return false;
	return true;
}

function clearSearch() {

	document.forms[0].fdate.value = "";
	document.forms[0].tdate.value = "";
	document.forms[0].patName.value = "";
	document.forms[0].labName.value = "";
	document.forms[0].billNo.value = "";
	document.forms[0].statusAll.checked = true;
	document.forms[0].typeAll.checked = true;
	enableBillStatus();
	enableBillType();

}


function enableBillStatus() {
	var disabled =  document.forms[0].statusAll.checked;

	document.forms[0].statusOpen.disabled = disabled;
	document.forms[0].statusClosed.disabled = disabled;
}

function enableBillType() {
	var disabled = document.forms[0].typeAll.checked;
	 
	document.forms[0].typeBillNow.disabled = disabled;
	document.forms[0].typeBillLater.disabled = disabled;
}



/*
 * Complete the Bill No.
 */
function onKeyPressBillNo(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeBillNo();
	} else {
		return true;
	}
}

function onChangeBillNo() {
	var billNoBox = document.forms[0].billNo;

	// complete
	var valid = addPrefix(billNoBox, gBillNoPrefix, gBillNoDigits);

	if (!valid) {
		alert("Invalid Bill No. Format");
		document.forms[0].billNo.value = ""
		document.forms[0].billNo.focus();
		return false;
	}
}

var toolbar = {
	Edit: {
		title: "View/Edit Bill",
		imageSrc: "icons/Edit.png",
		href: 'billing/BillAction.do?_method=getCreditBillingCollectScreen',
		onclick: null,
		description: "View and/or Edit the contents of this bill"
	}
}

function populateIncomingHospital(labName, labContainer) {

	var dataSource = new YAHOO.util.LocalDataSource(incomingHospitalJSON);
	dataSource.responseSchema = {resultsList : "result",
								 fields : [ {key :["hospital_name"]},{key :["hospital_id"]}] };

    var autoComp = new YAHOO.widget.AutoComplete(labName, labContainer, dataSource);
    autoComp.prehighlightClassName = "yui-ac-prehighlight";
    autoComp.typeAhead = true;
    autoComp.useShadow = true;
    autoComp.allowBrowserAutocomplete = false;
    autoComp.minQueryLength = 0;
    autoComp.maxResultsDisplayed = 20;
    autoComp.autoHighlight = true;
    autoComp.forceSelection = false;
    autoComp.animVert = false;
}

</script>

	<style type="text/css">
		.status_A.type_M { background-color: #EAD6BB }
		.status_C { background-color: #C5D9A3 }
	</style>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<jsp:useBean id="typeDisplay" class="java.util.HashMap"/>
<c:set target="${typeDisplay}" property="M" value="Bill Now"/>
<c:set target="${typeDisplay}" property="C" value="Bill Later"/>
<c:set target="${typeDisplay}" property="P" value="Bill Now"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>

<body onload="init();ajaxForPrintUrls();">
<c:set var="billno">
 <insta:ltext key="laboratory.incomingsamplependingbills.search.billno"/>
</c:set>
<c:set var="patientname">
 <insta:ltext key="ui.label.patient.name"/>
</c:set>
<c:set var="hospitalname">
 <insta:ltext key="laboratory.incomingsamplependingbills.search.hospitalname"/>
</c:set>
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
<div class="pageHeader"><insta:ltext key="laboratory.incomingsamplependingbills.search.incomingsamplependingbills"/> </div>
<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<B><c:out value="${param.msg}" /></B>
<c:choose>
	<c:when test="${category == 'DEP_LAB'}">
		<c:set var="Url" value="IncomingSamplePendingBill.do"/>
			</c:when>
	<c:otherwise>
    	<c:set var="Url" value="IncomingSamplePendingBillRad.do"/>
	    	</c:otherwise>
</c:choose>


<html:form action="/pages/DiagnosticModule/${Url}" method="GET" >
<input type="hidden" name="_method" 	value="getIncomingSamplePendingBillsList"/>
<input type="hidden" name="_searchMethod" value="getIncomingSamplePendingBillsList"/>
<c:set var="sampList" value="${pagedList.dtoList}"/>
<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>

<insta:search form="IncomingPendingBillForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="laboratory.incomingsamplependingbills.search.billno"/>:</div>
				<div class="sboFieldInput">
					<html:text property="billNo" onkeypress="onKeyPressBillNo(event);"
						onblur="onChangeBillNo()" size="15"/>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="ui.label.patient.name"/>:</div>
						<div class="sfFieldSub">
							<html:text property="patName" size="15"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.incomingsamplependingbills.search.hospitalname"/>:</div>
						<div class="sfField">
							<div class="myAutoComplete">
								<input type="text" name="labName" id="labName" maxlength="100" 
									value="${ifn:cleanHtmlAttribute(param.labName)}"/>
								<div id="labContainer" style="width:250px;"></div>
							</div>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.incomingsamplependingbills.search.billstatus"/>:</div>
						<div class="sfField">
							<html:checkbox property="statusAll" onclick="enableBillStatus()"><insta:ltext key="laboratory.incomingsamplependingbills.search.all"/></html:checkbox><br/>
							<html:checkbox property="statusOpen"><insta:ltext key="laboratory.incomingsamplependingbills.search.open"/></html:checkbox><br/>
							<html:checkbox property="statusClosed"><insta:ltext key="laboratory.incomingsamplependingbills.search.closed"/></html:checkbox><br/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.incomingsamplependingbills.search.billtype"/>:</div>
						<div class="sfField">
							<html:checkbox property="typeAll" onclick="enableBillType()"><insta:ltext key="laboratory.incomingsamplependingbills.search.all"/></html:checkbox><br>
							<html:checkbox property="typeBillNow"><insta:ltext key="laboratory.incomingsamplependingbills.search.billnow"/></html:checkbox><br>
							<html:checkbox property="typeBillLater"><insta:ltext key="laboratory.incomingsamplependingbills.search.billlater"/></html:checkbox>
						</div>
					</td>
				</tr>
				<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
					<td class="last">
						<div class="sfLabel"><insta:ltext key="laboratory.incomingsamplependingbills.search.center"/>:</div>
						<div class="sfField">
							<select class="dropdown" name="center_id" id="center_id">
								<option value="">${select}</option>
								<c:forEach items="${centers}" var="center">
									<c:if test="${center.map.center_id != 0}">
										<option value="${center.map.center_id}"
											${requestCenter == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
									</c:if>
								</c:forEach>
							</select>
						</div>
					</td>
				</c:if>
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="laboratory.incomingsamplependingbills.search.incomingpatient.otherinfo"/>:</div>
						<div class="sfFieldSub">
							<html:text property="patOtherInfo" size="15"/>
						</div>
					</td>
				</tr>
			</table>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>


	<c:set var="sampleList" value="${pagedList.dtoList}"/>
			<table class="dashboard" cellpadding="0" id="resultTable" cellspacing="0" align="center" width="100%" >
			<tr>
				<insta:sortablecolumn name="patientname" title="${patientname}"/>
				<insta:sortablecolumn name="hospname" title="${hospitalname}"/>
				<th><insta:ltext key="laboratory.incomingsamplependingbills.search.billtype"/></th>
				<th><insta:ltext key="laboratory.incomingsamplependingbills.search.billstatus"/></th>
				<insta:sortablecolumn name="billno" title="${billno}"/>

			</tr>
			<c:choose>
				<c:when test="${category == 'DEP_LAB'}">
					<c:set var="UrlSampleList" value="IncomingSamplePendingBill.do?_method=getSamplePendingBills"/>
				</c:when>
				<c:otherwise>
					<c:set var="UrlSampleList" value="IncomingSamplePendingBillRad.do?_method=getSamplePendingBills"/>
				</c:otherwise>
			</c:choose>
			<c:forEach var="sample" items="${sampleList}" varStatus="st">

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{category: '${ifn:cleanJavaScript(category)}', billNo: '${sample.BILLNO}',
						 incomingvisitid: '${sample.INCOMING_VISIT_ID}'},
						[true,${typeChangeEnabled},${orderEnabled},${orderEnabled}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${sample.PATIENT_NAME }</td>
					<td>${sample.HOSPITAL_NAME }</td>
					<td>${typeDisplay[sample.BILL_TYPE]}</td>
					<td>${statusDisplay[sample.STATUS]}</td>
					<td>${sample.BILLNO}</td>

				</tr>
			</c:forEach>
			</table>

</html:form>

</body>
</html>
