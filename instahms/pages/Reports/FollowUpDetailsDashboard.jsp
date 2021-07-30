<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>

<head>
	<title><insta:ltext key="registration.followupdetails.list.title"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="/patient/patientsearch.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<style type="text/css">
		table.legend {border-collapse : collapse ; margin-left : 6px }
		table.legend td {border : 1px solid grey ;padding 2px 5px }
		table.search td { white-space: nowrap }
	</style>
	<script>
		var cpath = '${cpath}';
		function setDateRange(fromDate, toDate, fieldName) {
			var today = new Date(getServerDate());
			if (fromDate == 'today') {
				document.getElementsByName(fieldName)[0].value = formatDate(today);
			} else {
				document.getElementsByName(fieldName)[0].value = fromDate;
			}

			if (toDate == 'tomorrow') {
				var tomorrow = new Date(today);
				tomorrow.setDate(today.getDate() + 1);
				document.getElementsByName(fieldName)[1].value = formatDate(tomorrow);

			} else {
				document.getElementsByName(fieldName)[1].value = toDate;
			}

		}
		function init() {
			initMrNoAutoComplete(cpath);
		}
	</script>

</head>
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>

<c:set var="patientList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty patientList}"/>
<c:set var="method_name" value= "getDashBoardSearchDetails"/>
<body onload="
		init();
		setDateRange('${paramValues['fud.followup_date'][0]}', '${paramValues['fud.followup_date'][1]}', 'fud.followup_date');
		setDateRange('${paramValues['pr.reg_date'][0]}', '${paramValues['pr.reg_date'][1]}', 'pr.reg_date');
		">
	<div class="pageHeader"><insta:ltext key="registration.followupdetails.list.followupdetails"/></div>

	<form method="GET" action="${cpath}/pages/followUpDashboard.do" name="followUpDashboardForm">
	<input type="hidden" name="_method" value="getDashBoardSearchDetails"/>
	<input type="hidden" name="_searchMethod" value="getDashBoardSearchDetails"/>
	<input type="hidden" name="mrNoLink" value="${ifn:cleanHtmlAttribute(param.mrNoLink)}">
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>

	<insta:search form="followUpDashboardForm" optionsId="optionalFilter" closed="${hasResults}" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="registration.followupdetails.list.mrno.or.patientname"/>:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="pd.mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param['pd.mr_no'])}" />
						<input type="hidden" name="mr_no@op" value="ilike" />
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.outpatientlist.consult.details.followupdate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="registration.followupdetails.list.from"/>:</div>
							<%-- NOTE: datewidget requires that each date field have a unique id --%>
							<insta:datewidget name="fud.followup_date" id="followup_date1" />
			        		<input type="hidden" name="fud.followup_date@type" value="date"/>
							<input type="hidden" name="fud.followup_date@op" value="ge"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="registration.followupdetails.list.to"/>:</div>
							<insta:datewidget name="fud.followup_date" id="followup_date2" />
				        	<input type="hidden" name="fud.followup_date@type" value="date"/>
							<input type="hidden" name="fud.followup_date@op" value="le"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="billing.dialysisorders.approvals.visitdate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="registration.followupdetails.list.from"/>:</div>
								<%-- NOTE: datewidget requires that each date field have a unique id --%>
							<insta:datewidget name="pr.reg_date" id="visit_date1" />
							<input type="hidden" name="pr.reg_date@type" value="date"/>
							<input type="hidden" name="pr.reg_date@op" value="ge"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="registration.followupdetails.list.to"/>:</div>
							<insta:datewidget name="pr.reg_date" id="visit_date2" />
							<input type="hidden" name="pr.reg_date@type" value="date"/>
							<input type="hidden" name="pr.reg_date@op" value="le"/>
						</div>
					</td>

					<td>
						<div class="sfLabel"><insta:ltext key="registration.followupdetails.list.department"/></div>
						<div class="sfField">
							<insta:selectdb name="dept.dept_id" id="department" table="department" valuecol="dept_id" displaycol="dept_name"
								values="${paramValues['dept.dept_id']}" multiple="true" size="8" class="listbox"/>
							<input type="hidden" name="dept.dept_id@type" value="text"/>
							<input type="hidden" name="dept.dept_id@op" value="in"/>
						</div>
					</td>

					<td class="last">
						<div class="sfLabel"><insta:ltext key="registration.followupdetails.list.doctor"/></div>
						<div class="sfField">
							<insta:selectdb name="doc.doctor_id" id="doctor" table="doctors" valuecol="doctor_id" displaycol="doctor_name"
								values="${paramValues['doc.doctor_id']}" multiple="true" size="8" class="listbox"/>
							<input type="hidden" name="dept.doctor_id@type" value="text"/>
							<input type="hidden" name="dept.doctor_id@op" value="in"/>
						</div>
					</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" cellpadding="0" cellspacing="0" width="100%" >
			<tr>
				<th><insta:ltext key="registration.followupdetails.list.s.no"/></th>
				<insta:sortablecolumn name="mrno" title="${mrno}"/>
				<%-- no sorting on patient name --%>
				<th><insta:ltext key="ui.label.patient.name"/></th>
				<th><insta:ltext key="registration.followupdetails.list.mobileno"/>.</th>
				<th><insta:ltext key="registration.followupdetails.list.followupdate"/></th>
				<th><insta:ltext key="registration.followupdetails.list.followupdepartment"/></th>
				<th><insta:ltext key="registration.followupdetails.list.followupdoctor"/></th>
				<th><insta:ltext key="registration.followupdetails.list.remarks"/></th>
			</tr>

			<c:forEach var="patient" items="${patientList}" varStatus="st">
				<tr >
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
					<td>${patient.mrno}</td>
					<td>${patient.patientName}</td>
						<td>${patient.phone}</td>
					<td><fmt:formatDate value="${patient.followUpDate}"  pattern="dd-MM-yyyy" /></td>
					<td>${patient.followUpDeptName}</td>
					<td>${patient.followUpDocName}</td>
					<td>${patient.followUpRemarks}</td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${hasResults}"/>
	</div>
</form>
</body>
</html>

