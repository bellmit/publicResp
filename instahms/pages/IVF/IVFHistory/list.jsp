<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title>IVF History - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="ivf/ivfsessions.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>


</head>
<c:set var="ivfsesList" value="${PagedList.dtoList}"/>
<c:set var="noOfRecords" value="${PagedList.totalRecords}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty ivfsesList}"/>
<body class="yui-skin-sam">
<h1 style="float: left">IVF History</h1>
<insta:patientsearch searchType="visit" searchUrl="IVFHistory.do" activeOnly="true"
			buttonLabel="Find" searchMethod="getPatientVisitDetails" fieldName="patient_id"/>
<insta:feedback-panel/>
<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<c:if test="${param._method == 'list' && hasResults}">
		<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true"/>
	</c:if>
<form  name="historyForm" method="get" action="${cpath}/IVF/IVFHistory.do">

		<table border="0" class="resultList"" cellpadding="0" cellspacing="0" width="100%" id="resultTable">
			<tr class="header">
				<th>Cycle Start Date</th>
				<th>Protocol</th>
				<th>Total dose of GT</th>
				<th>Peak E</th>
				<th>Oocyte Obtained</th>
				<th>No. Fertilized</th>
				<th colspan="2">Embryos Transfer </th>
				<th colspan="3">Embryos Frozen </th>
				<th colspan="2"></th>
			</tr>
			<tr class="header">
				<th colspan="6"></th>
				<th>No</th>
				<th>Grade</th>
				<th>No</th>
				<th>Stage</th>
				<th>Grade</th>
				<th colspan="2"></th>
			</tr>
			<c:forEach var="ivfses" items="${ivfsesList}">
			<tr>
				<td align="center"  valign="top">${ivfses.start_date}</td>
				<td align="center"  valign="top">${ivfses.protocol}</td>
				<td align="center"  valign="top">${ivfses.gndtropin_dose}</td>
				<td align="center"  valign="top">${ivfses.peak_e}</td>
				<td align="center"  valign="top"><c:if test="${ivfses.tot_oocyte!=0}">${ivfses.tot_oocyte}</c:if></td>
				<td align="center"  valign="top">${ivfses.fertilization_rate_number}</td>
				<td colspan="2">
					<table class="formtable">
						<c:forEach var="et" items="${etList}">
							<c:if test="${et.ivf_cycle_id == ivfses.ivf_cycle_id}">
								<tr>
									<td  valign="top">${et.emb_number}</td>
									<td  valign="top">${et.emb_grade}</td>
								</tr>
							</c:if>
						</c:forEach>
					</table>
				</td>
				<td colspan="3">
					<table class="formtable">
						<c:forEach var="ef" items="${efList}">
							<c:if test="${ef.ivf_cycle_id == ivfses.ivf_cycle_id}">
								<tr>
									<td  valign="top">${ef.emb_number}</td>
									<td  valign="top">${ef.emb_state}</td>
									<td  valign="top">${ef.emb_grade}</td>
								</tr>
							</c:if>
						</c:forEach>
					</table>
				</td>
				<td align="center" valign="bottom">
				<a href="${cpath}/IVF/IVFPreCycle.do?_method=show&mr_no=${ivfses.mr_no}&patient_id=${ivfses.patient_id}
					&ivf_cycle_id=${ivfses.ivf_cycle_id}&start_date=${ivfses.start_date}">Pre | </a>
				<a href="${cpath}/IVF/IVFDailyTreatment.do?_method=list&mr_no=${ivfses.mr_no}&patient_id=${ivfses.patient_id}
					&ivf_cycle_id=${ivfses.ivf_cycle_id}&start_date=${ivfses.start_date}">Daily | </a>
				<a href="${cpath}/IVF/IVFCycleCompletion.do?_method=show&mr_no=${ivfses.mr_no}&patient_id=${ivfses.patient_id}
					&ivf_cycle_id=${ivfses.ivf_cycle_id}&start_date=${ivfses.start_date}">Completion</a>
				</td>
			</tr>
			</c:forEach>
		</table>
		<table>
			<tr>&nbsp;</tr>
			<tr><td><a href="${cpath}/IVF/IVFHistory.do?_method=getIVFHistoryPrint&mr_no=${ifn:cleanURL(param.mr_no)}" >Print IVF History</a></td></tr>
		</table>
</form>
</body>
</html>