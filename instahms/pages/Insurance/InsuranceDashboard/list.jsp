<%@page import="com.bob.hms.common.Constants" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<title>Insurance Patients List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="Insurance/insurancedashboard.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="dashboardsearch.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>

<style type="text/css">
	.status_A { background-color: #EAD6BB;}
	.status_F { background-color: #DDDA8A}
	.status_C { background-color: #E0E8E0}
	.status_D { background-color: #ffa07a}
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
<body onload="initMrnoAutoComplete();" class="yui-skin-sam">
<input type="hidden" name="searchField"  id="searchField"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="insList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty insList}"/>
<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>
<div class="pageHeader">Insurance Cases</div>
<insta:feedback-panel/>
<form name="NewCaseForm" method="GET" action="InsuranceDashboard.do" >
	<input type="hidden" name="_method" value="list" id="method">
	<input type="hidden" name="method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="bill_no" value="">
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="NewCaseForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="doSearch()">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">MR No/Patient Name:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
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
					<div class="sfLabel">Sponsor</div>
					<%--
					<c:choose>
						<c:when test="${corpInsurance eq 'Y'}">
							<div class="sfLabel">Sponsor</div>
						</c:when>
						<c:otherwise>
							<div class="sfLabel">TPA</div>
						</c:otherwise>
					</c:choose>
					--%>
					<div class="sfField">
						<insta:selectdb name="tpa_id" table="tpa_master" valuecol="tpa_id"
							displaycol="tpa_name" orderby="tpa_name" filtered="true" filtercol="status"  value="${paramValues.tpa_id}" multiple="true"/>
					</div>
				</td>
				<td>
					<div class="sfLabel">Case No</div>
					<div class="sfField">
						<input type="text" name="insurance_id" size="10" value="${ifn:cleanHtmlAttribute(param.insurance_id)}"/>
						<div class="sfLabel">Admission Date:</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
								<insta:datewidget name="gen_reg_date0" id="gen_reg_date0" value="${param.gen_reg_date0}"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
								<insta:datewidget name="gen_reg_date1" id="gen_reg_date1" value="${param.gen_reg_date1}"/>
							</div>
						</div>
				</td>
				<td>
				   <div class="sflabel">Patient Type</div>
				   <div class="sffield">
				        <insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
							opvalues="i,o" optexts="IP,OP"/>
				  </div>
				</td>
				<td class="last">
					<div class="sfLabel">Status</div>
					<div class="sfField">
						<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="P,A,F,C,D" optexts="Prior Auth,Approved,Finalized,Closed,Denied"/>
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
				<th>Sl.No</th>
				<insta:sortablecolumn name="mr_no" title="MR No"/>
				<th>Patient Name</th>
				<th>Patient ID</th>
				<insta:sortablecolumn name="insurance_id" title="Case "/>
				<th>Status</th>
				<th>Sponsor</th>
				<%--
				<c:choose>
					<c:when test="${corpInsurance eq 'Y'}">
					<th>Sponsor</th>
					</c:when>
					<c:otherwise>
					<th>TPA</th>
					</c:otherwise>
				</c:choose>
 				--%>
 				<th>Patient Type</th>
				<insta:sortablecolumn name="reg_date" title="Admitted"/>
				<th>Bill</th>
			</tr>
			<c:forEach var="patient" items="${insList}" varStatus="st">
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${patient.status == 'A'}">green</c:when>
						<c:when test="${patient.status == 'F'}">yellow</c:when>
						<c:when test="${patient.status == 'C'}">blue</c:when>
						<c:when test="${patient.status == 'D'}">red</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{mr_no: '${patient.mr_no}', visit_id: '${patient.patient_id}', insurance_id: '${patient.insurance_id}',whichScreen: 'AddOrEditDashboard' },
						[true,true,true,true]);" onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${st.index +1 }</td>
					<td>${patient.mr_no}</td>
					<td>${patient.patient_name}</td>
					<td>${patient.patient_id}</td>
					<td>${patient.insurance_id}</td>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${patient.status_name}</td>
					<td>${patient.tpa_name}</td>
					<c:if test="${patient.visit_type == 'o'}">
					<td>OP</td>
					</c:if>
					<c:if test="${patient.visit_type == 'i'}">
					<td>IP</td>
					</c:if>
					<c:if test="${empty patient.visit_type}">
					<td></td>
					</c:if>
					<td><fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/></td>
					<td>${patient.bill_no}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
	<c:if test="${param.method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
	</c:if>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText">Approved</div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"> Finalized</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"> Closed</div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"> Denied</div>
	</div>
</form>
</body>

</html>
