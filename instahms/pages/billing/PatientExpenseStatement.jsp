<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
	<title>Pat Expense Statement-Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">

	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="/billing/patientexpense_stmtsearch.js"/>

	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<style type="text/css">
		.autocomplete1 {
		width:130px;
		padding-bottom:10px;
		}

		.status_I  {background-color : #C5D9A3 }
		.status_ { background-color: #E0E8E0}
		table.legend {border-collapse : collapse ; margin-left : 6px }
		table.legend td {border : 1px solid grey ;padding 2px 5px }
		table.search td { white-space: nowrap }


			.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
		    _height:11em; /* ie6 */
		}
	</style>

</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="patientList" value="${pagedList.dtoList}"/>
<c:set var="hasResult" value="${not empty patientList}"/>
<jsp:useBean id="typeDisplay" class="java.util.HashMap"/>
<c:set target="${typeDisplay}" property="i" value="IP"/>
<c:set target="${typeDisplay}" property="o" value="OP"/>
<c:set target="${typeDisplay}" property="r" value="Retail"/>
<c:set var="results" value="${not empty patientList}"/>

<body onload="init()" class="yui-skin-sam">
	<div class="pageHeader">
			Patient Expense Statement
	</div>

	<form  name="PatientExpenseStatementForm" method="GET" >
	<input type="hidden" name="_method" value="searchVisits" id="_method">
	<input type="hidden" name="mrNoLink" value="${ifn:cleanHtmlAttribute(param.mrNoLink)}">
	<input type="hidden" name="countryid" />
	<input type="hidden" name="stateid" />
	<input type="hidden" name="cityid" />
	<input type="hidden" name="_actionId" id="_actionId" value="${empty param._actionId ? _actionId : param._actionId}"/>
	<input type="hidden" name="_searchMethod" id="_searchMethod" value="searchVisits"/>
	<insta:search form="PatientExpenseStatementForm" optionsId="optionalFilter" closed="${hasResult}">
	<div class="searchBasicOpts">

		<div class="sboField">
			<div class="sboFieldLabel">MR No:</div>
			<div class="sboFieldInput">
					<div class="mrnoAutocomplte">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
						<input type="hidden" name="mr_no@op" value="like"/>
						<div id="mrnoContainer"/>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div id="optionalFilter" style="clear: both; display : ${hasResult ? 'none': 'block'}">
		<table class="searchFormTable">
			<tr>
				<td>
					<div class="sfLabel">Patient Details</div>
						<div class="sfField">
							<div class="sfFieldSub">First Name:</div>
							<input type="text" name="patient_name" id="patient_name" value="${ifn:cleanHtmlAttribute(param.patient_name)}"/>
							<input type="hidden" name="patient_name@op" value="ilike"/>
						</div>
						<div class="sfField">
							<div class="sffieldSub">Last Name:</div>
								<input type="text" name="last_name" id="last_name" value="${ifn:cleanHtmlAttribute(param.last_name)}"/>
								<input type="hidden" name="last_name@op" value="ilike"/>
							</div>
							<div class="sfField">
									<div class="sfFieldSub">Mobile No.:</div>
									<input type="text" name="patient_phone" id="patient_phone" value="${ifn:cleanHtmlAttribute(param.patient_phone)}"/>
							</div>
					</div>
				</td>
				<td>
					<div class="sfLabel">Visit</div>
					<div class="sfField">
						<insta:checkgroup name="revisit" opvalues="N,Y" optexts="New,Revisit" selValues="${paramValues.revisit}"/>
					</div>
				</td>
				<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="visit_status"  opvalues="A,I" optexts="Active,Inactive"
							selValues="${paramValues.visit_status}"/>
						</td>
						<td>
							<div class="sfLabel">Admission Date:</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
								<insta:datewidget name="reg_date" id="reg_date0" value="${paramValues.reg_date[0]}"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
								<insta:datewidget name="reg_date" id="reg_date1" value="${paramValues.reg_date[1]}"/>
								<input type="hidden" name="reg_date@op" value="ge,le"/>
							</div>
				</td>
				<td class="last">
						<div class="sfLabel">Type</div>
						<div class="sfField">
							<insta:checkgroup name="visit_type" opvalues="i,o" optexts="IP,OP" selValues="${paramValues.visit_type}"/>
						</div>
					</td>
				</tr>
				<tr>
					<td>
							<div class="sfLabel">Department</div>
							<div class="sfField">
								<insta:selectdb name="dept_id" table="department" valuecol="dept_id" displaycol="dept_name"
								class="listbox" values="${paramValues.dept_id}" multiple="true"/>
							</div>
						</td>
						<td>
								<div class="sfLabel">Doctor:</div>
								<div class="sfField">
									<insta:selectdb name="doctor" table="doctors" valuecol="doctor_id" displaycol="doctor_name" class="listbox" values="${paramvalues.doctor_id}" multiple="true"/>
								</div>
							</td>
							<td class="last">
								<table>
									<tr>
										<td>
											<div class="sfLabel">Location</div>
											<div class="sfField">
												<div>Country</div>
												<div id="autocountry">
													<input type="text" name="country_name" id="country_name"  value="${ifn:cleanHtmlAttribute(param.country_name)}" />
													<div id="country_dropdown" class="autocomplete1"></div>
												</div>
											</div>
										</td>
									</tr>
									<tr>
										<td>
											<div class="sfField">
												<div>State</div>
												<div id="autostate" >
													<input type="text" name="statename" id="statename" value="${ifn:cleanHtmlAttribute(param.statename)}" />
													<div id="state_dropdown" class="autocomplete1"></div>
												</div>
											</div>
										</td>
									</tr>
									<tr>
										<td>
											<div class="sfField">
												<div>City</div>
												<div class="autocity" >
													<input type="text" name="cityname" id="cityname" value="${ifn:cleanHtmlAttribute(param.cityname)}" />
													<div id="city_dropdown" class="autocomplete1"></div>
												</div>
											</div>
										</td>
									</tr>
									<tr>
										<td>
											<div class="sfField">
												<div>Area</div>
												<div id="autoarea">
													<input type="text" name="patient_area" id="patient_area" value="${ifn:cleanHtmlAttribute(param.patient_area)}"/>
													<div id="area_dropdown" class="autocomplete1"></div>
												</div>
											</div>
										</td>
									</tr>
								</table>
							</td>
			</tr>
			</table>

	</div>

	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div style="overflow: auto: width: 953" class="resultList">
		<table class="resultList" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable"
				 onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar('');">
				<th>S.No</th>
				<insta:sortablecolumn name="mrno" title="MR No"/>
				<insta:sortablecolumn name="patid" title="Visit No"/>
				<%-- no sorting on patient name --%>
				<th>Patient Name</th>
				<th>Mobile No.</th>
				<th>Department</th>
				<th>Doctor</th>
			</tr>
			<c:forEach var="patient" items="${patientList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow': ''} ${st.index %2 == 0? 'even' : 'odd'}"  onclick="showToolbar(${st.index}, event, 'resultTable', {mr_no: '${patient.mr_no}', visit_id: '${patient.patient_id}', rawprint: '${pref.map.bill_print_text_mode}'});"	onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">

					<input type="hidden" id="rawprint" name="rawprint" value="${pref.map.bill_print_text_mode}">

					<c:choose>
						<c:when test="${patient.visit_status == 'A'}">
							<c:set var="flagColor" value="empty"/>
						</c:when>
						<c:when test="${patient.visit_status == 'I'}">
							<c:set var="flagColor" value="grey"/>
						</c:when>
					</c:choose>
					<td>${(pagedList.pageNumber-1) * 15 + st.index + 1 }</td>
					<td>
						<div style="width: 15px; float: left"><img src="${cpath}/images/${flagColor}_flag.gif"/></div>
						${patient.mr_no}
					</td>
					<td>${patient.patient_id}</td>

					<td>${patient.salutation} ${patient.patient_name} ${patient.last_name}</td>
					<td>${patient.patient_phone}</td>
					<td>${patient.dept_name}</td>
					<td>${patient.doctor_name}</td>
				</tr>
			</c:forEach>
			<insta:noresults  hasResults="${results}"/>
		</table>
	</div>
	<div class="fltR " style="width:30%; margin:10px 0 0 0px; display: ${results?'block':'none'}" >
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"/></div>
		<div class="flagText">Active Patients</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"/></div>
		<div class="flagText">Inactive Patients</div>
	</div>
	<script>
		var areaList = ${areaList};
		var countryList = ${countryList};
		var stateList = ${stateList};
		var cityList = ${cityList};
		var prescribeRights = '${screenRightsMap['prescribe']}';
		var prescribeCancelRights = '${screenRightsMap['prescribeCancel']}';
	</script>

</form>

<script type="text/javascript">
var dashboard = '${whichDashBoard}';
</script>
</body>
</html>
:wq
c
