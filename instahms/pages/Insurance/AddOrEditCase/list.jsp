<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
	<title>Add New Case</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="/Insurance/insurance.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<style type="text/css">
		.status_N { background-color: #E0E8E0}
		table.legend {border-collapse : collapse ; margin-left : 6px }
		table.legend td {border : 1px solid grey ;padding 2px 5px }
		table.search td { white-space: nowrap }

		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
		    _height:11em; /* ie6 */
		}
	</style>
	<script>
		var contextPath = '<%=request.getContextPath()%>';
	</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="insList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty insList}"/>

<body onload="initMrnoAutoComplete();" class="yui-skin-sam">
	<div class="pageHeader">Add New Case</div>
	<insta:feedback-panel/>

<form name="NewCaseForm" method="GET" action="">
	<input type="hidden" name="method" value="list" id="method">
	<input type="hidden" name="bill_no" value="">
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<insta:search form="NewCaseForm" optionsId="optionalFilter" closed="${hasResults}" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">MR No/Patient Name:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<input type="hidden" name="mr_no@op" value="ilike" />
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
		<table class="searchFormTable" >
			<tr>
				<td>
					<div class="sfLabel">Patient Details</div>
					<div class="sfField">
						<div class="sfFieldSub">Mobile No.:</div>
								<input type="text" name="phone" size ="10" value="${ifn:cleanHtmlAttribute(param.phone)}"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub">Old Reg:</div>
								<input type="text" name="phone" size ="10" value="${ifn:cleanHtmlAttribute(param.phone)}"/>
					</div>
					<div class="sfLabel">Admission Date</div>
					<div class="sfField">
						<div class="sfFieldSub">From:</div>
						<insta:datewidget name="fdate" id="fdate" value="${paramValues.fdate[0]}"/>
						<input type="hidden" name="fdate@op" value="ge,le"/>
						</div>
					<div class="sfField">
						<div class="sfFieldSub">To:</div>
						<insta:datewidget name="tdate" id="tdate1" value="${paramValues.tdate[1]}"/>
						<%-- NOTE: tell the query-builder to use >= and <= operators for the dates --%>
						<input type="hidden" name="tdate@op" value="ge,le"/>
					</div>
				</td>
				<td>
					<div class="sfLabel">Status</div>
					<div class="sfField">
						<insta:checkgroup name="patstatus" selValues="${paramValues.patstatus}"
							opvalues="A,I,N" optexts="Active,Inactive,No Visits"/>
					</div>
				</td>
				<td>
					<div class="sfLabel">Type</div>
					<div class="sfField">
						<insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
							opvalues="i,o" optexts="IP,OP"/>
					</div>
				</td>
				<td>
					<div class="sfLabel">Doctor</div>
					<div class="sfField">
						<insta:selectdb name="doctor_id" size="8" multiple="true" table="doctors" valuecol="doctor_id"
								orderby="doctor_name" displaycol="doctor_name" filtered="false"
								values="${paramValues.doctor_id}"/>
					</div>
				</td>
				<td>
					<div class="sfLabel">Referral Doctor</div>
					<div class="sfField">
						<insta:selectdb name="referrer" size="8" multiple="true"
								table="all_referrers_view" valuecol="referrer" orderby="referrer" displaycol="referrer"
								filtered="false" values="${paramValues.referrer}"/>
					</div>
				</td>
			</tr>
		</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" cellpadding="0" cellspacing="0" id="resultTable" onmouseover="hideToolBar('');"  >
			<tr onmouseover="hideToolBar();">
				<insta:sortablecolumn name="mr_no" title="MR No"/>
				<insta:sortablecolumn name="patient_id" title="Visit ID"/>
				<th>Patient Name</th>
				<th>Age / Gender</th>
				<th>Mobile No.</th>
				<th>Doctor</th>
				<th>Referral Doctor</th>
				<th>Admitted</th>
			</tr>
			<c:forEach var="patient" items="${insList}" varStatus="st">
				<c:set var="flagColor">
				<c:choose>
					<c:when test="${patient.status == 'A'}">green</c:when>
					<c:when test="${patient.status == 'I'}">gray</c:when>
					<c:when test="${patient.status == 'N'}">purple</c:when>
					<c:otherwise>empty</c:otherwise>
				</c:choose>
				</c:set>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{mrno: '${patient.mr_no}', visit_id: '${patient.patient_id}',whichScreen: 'AddOrEditDashboard' ,bill_no:'${patient.bill_no}',insurance_id: '${patient.insurance_id}' },
						[true,true,true,true]);" onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${patient.mr_no}</td>
					<td>${patient.patient_id}</td>
					<td>${patient.patient_name}</td>
					<c:set var="ageIn" value=""/>
					<c:choose>
						<c:when test="${patient.agein eq 'D'}">
							<c:set var="agein" value="Days"/>
						</c:when>
						<c:when test="${patient.agein eq 'M'}">
							<c:set var="ageIn" value="Months"/>
						</c:when>
						<c:otherwise>
							<c:set var="ageIn" value="Years"/>
						</c:otherwise>
					</c:choose>
					<td>${patient.age} ${ageIn}/${patient.patient_gender}</td>
					<td>${patient.patient_phone}</td>
					<td>${patient.doctor_name}</td>
					<td>${patient.referer}</td>
					<td><fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/>
							<fmt:formatDate type="time" value="${patient.reg_time}" pattern="hh:mm a"/>
					</td>
				</tr>
			</c:forEach>
		</table>
	</div>
	<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
	</c:if>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"> Active</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"> Inactive</div>
		<div class="flag"><img src='${cpath}/images/purple_flag.gif'></div>
		<div class="flagText"> No Visits</div>
	</div>
</form>
</body>
</html>

