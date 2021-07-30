<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title><insta:ltext key="patient.dialysis.sessions.screen"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
	<script>
    	var toolbarOptions = getToolbarBundle("js.dialysismodule.commonvalidations.toolbar");
	</script>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="dialysis/dialysissessions.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<style type="text/css">
		table.dashboard td table td{
			border: 0px ;
		}
		table.dashboard th {
			background-color: lightgrey;
			padding: 4px 6px 4px 6px;
			font-weight: bold;
			text-align: left;
		}

		.alert { background-color: 	#EAD6BB }
		table.sessionDetails {
			border-bottom:1px #e6e6e6 solid;
			border-top:1px #e6e6e6 solid;
			border-right:1px #e6e6e6 solid;
			border-left:1px #e6e6e6 solid;
		}
		table.sessionDetails tr.weights, tr.dialysis, tr.vitals, tr.details  {
			height : 22px;
			background-color : lightgrey;
		}
		table.sessionDetails tr.header th {
			padding:0 10px 0px 10px;
			color: #333;
			background-color: lightgrey;
			border-bottom: none;
			border-right: 1px #cad6e3  solid;
			border-top: 1px #cad6e3  solid;
			height: 24px;
			cursor: pointer;
			text-align: left;
			margin-top: 1px;
		}
	</style>
	<script>
		var contextPath = '<%=request.getContextPath()%>';
	</script>

	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
	<insta:js-bundle prefix="dashboard.commonvalidations"/>
</head>
<c:set var="sesList" value="${pagedList.dtoList}"/>
<c:set var="noOfRecords" value="${pagedList.totalRecords}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty sesList}"/>
<c:set var="completedOptions">
 <insta:ltext key="patient.dialysis.sessions.completed"/>,
 <insta:ltext key="patient.dialysis.sessions.closed"/>
</c:set>

<c:set var="alertOptions">
 <insta:ltext key="patient.dialysis.sessions.session.no.alert"/>,
 <insta:ltext key="patient.dialysis.sessions.session.alert"/>
</c:set>

<body  onload="initMrnoAutoComplete();setDates();" class="yui-skin-sam">
<div class="pageHeader"><insta:ltext key="patient.dialysis.sessions.pageHeader"/></div>
<insta:feedback-panel/>
<form  name="summaryForm" method="get" action="${cpath}/dialysis/DialysisSessionsSummary.do">
	<input type="hidden" name="_method" value="list" />
	<input type="hidden" name="_searchMethod" value="list" />
	<insta:search form="summaryForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="doSearch()">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="patient.dialysis.sessions.sbofieldLabel"/></div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete" style="padding-bottom: 2em;">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
				<input type="hidden" name="mr_no@op" value="ilike" />
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable" >
				<tr>
					<td>
						<div class="sfLabel" style="width: 240px"><insta:ltext key="patient.dialysis.sessions.date.range"/></div>
						<div class="sfField">
							<insta:ltext key="patient.dialysis.sessions.last"/><input type="radio" name="dateRange" id="30Range" value="30" ${param.dateRange == '30' ? 'checked' : '' } checked  onclick="setDates();"><insta:ltext key="patient.dialysis.sessions.30"/>
							<input type="radio" name="dateRange" id="60Range" value="60" ${param.dateRange == '60' ? 'checked' : '' } onclick="setDates();"> <insta:ltext key="patient.dialysis.sessions.60"/>
							<input type="radio" name="dateRange" id="90Range" value="90" ${param.dateRange == '90' ? 'checked' : '' } onclick="setDates();"> <insta:ltext key="patient.dialysis.sessions.90"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="patient.dialysis.sessions.from"/></div>
							<insta:datewidget name="start_date" id="start_date" value="${param.start_date}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="patient.dialysis.sessions.to"/></div>
							<insta:datewidget name="end_date" id="end_date" value="${param.end_date}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.dialysis.sessions.status"/></div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
								opvalues="F,C" optexts="${completedOptions}"/>
						</div>

					</td>
					<td class="last">
						<div class="sfLabel"><insta:ltext key="patient.dialysis.sessions.alert"/></div>
						<div class="sfField">
							<insta:checkgroup name="alerts" selValues="${paramValues.alerts}"
								opvalues="0,1" optexts="${alertOptions}"/>
						</div>
						<input type="hidden" id="operator" name="operator" value="" />
						<input type="hidden" id="value" name="value" value=""  />
					</td>
					<td class="last"></td>
					<td class="last"></td>
				</tr>
			</table>
		</div>
	</insta:search>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<c:if test="${param._method == 'list' && hasResults}">
	<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true"/>
	<div >
		<table border="0" class="sessionDetails" cellpadding="0" cellspacing="0">
			<tr class="header">
				<insta:sortablecolumn name="start_time" title="Date"/>
				<c:forEach items="${sesList}" var="ses">
					<th><fmt:formatDate value="${ses.start_time}" pattern="dd-MM-yyyy HH:mm"></fmt:formatDate></th>
				</c:forEach>
			</tr>
			<tr>
				<td >
					<table width="100%" class="formtable">
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.location"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.attendants"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.accesstype"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.dialyzer.lot"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.reprocessed"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.duration.hrs"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.status"/></td></tr>
					</table>
				</td>
				<c:forEach items="${sesList}" var="ses">
					<td>
						<table width="100%" class="formtable">
							<tr><td align="center">${not empty ses.location_name ? ses.location_name : '-'}</td></tr>
							<tr><td align="center">${not empty ses.attendants ? ses.attendants : '-'}</td></tr>
							<tr><td align="center">${not empty ses.access_type ? ses.access_type:'-'}</td></tr>
							<tr><td align="center">${not empty ses.dialyzer_lot ? ses.dialyzer_lot : '-'}</td></tr>
							<tr><td align="center">${not empty ses.dialyzer_repr_count ? ses.dialyzer_repr_count : '-'}</td></tr>
							<tr><td align="center">${not empty ses.est_duration ? ses.est_duration : '-'}</td></tr>
							<tr><td align="center">${not empty ses.status_name ? ses.status_name : '-'} (${not empty ses.completion_status ? ses.completion_status : ''})</td></tr>
						</table>
					</td>
				</c:forEach>
			</tr>
			<tr class="weights"><td colspan="${noOfRecords+1}"><insta:ltext key="patient.dialysis.sessions.weight"/></td></tr>
			<tr>
				<td >
					<table  width="100%" class="formtable">
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.uf.rate"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.weight"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.target.weight"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.weight.loss"/></td></tr>
					</table>
				</td>
				<c:forEach items="${sesList}" var="ses">
					<td>
						<table width="100%" class="formtable">
							<tr><td align="center">${ses.last_uf_rate}</td></tr>
							<tr><td align="center">${not empty ses.in_real_wt ? ses.in_real_wt :'-'} &raquo; ${not empty ses.fin_real_wt ? ses.fin_real_wt :'-'}</td></tr>
							<tr><td align="center">${not empty ses.target_wt ? ses.target_wt:'-'}</td></tr>
							<tr><td align="center">${not empty ses.total_wt_loss ? ses.total_wt_loss :'-'}</td></tr>
						</table>
					</td>
				</c:forEach>
			</tr>
			<tr class="dialysis"><td colspan="${noOfRecords+1}"><insta:ltext key="patient.dialysis.sessions.dialysis"/></td></tr>
			<tr>
				<td >
					<table  width="100%" class="formtable">
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.uf.removed"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.total.hep"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.alerts"/></td></tr>
					</table>
				</td>
				<c:forEach items="${sesList}" var="ses">
					<td>
						<table width="100%" class="formtable">
							<tr><td align="center">${not empty ses.last_uf_removed ? ses.last_uf_removed :'-'} </td></tr>
							<tr><td align="center">${not empty ses.total_heparin ? ses.total_heparin : '-'}</td></tr>
							<tr><td align="center">${not empty ses.alerts ? ses.alerts :'-'}</td></tr>
						</table>
					</td>
				</c:forEach>
			</tr>
			<tr class="vitals"><td colspan="${noOfRecords+1}"><insta:ltext key="patient.dialysis.sessions.vitals"/></td></tr>
			<tr>
				<td >
					<table  width="100%" class="formtable">
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.temperature"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.pulse"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.blood.presure"/></td></tr>
						<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.min.bp"/></td></tr>
					</table>
				</td>
				<c:forEach items="${sesList}" var="ses">
					<td>
						<table width="100%" class="formtable">
							<tr><td align="center">${not empty ses.in_temperature?ses.in_temperature:'-'} &raquo; ${not empty ses.fin_temperature?ses.fin_temperature:'-'}</td></tr>
							<tr><td align="center">${not empty ses.first_pulse_rate ? ses.first_pulse_rate:'-'} &raquo; ${not empty ses.last_pulse_rate ? ses.last_pulse_rate :'-'}</td></tr>
							<tr><td align="center">${not empty ses.first_bp_high ? ses.first_bp_high :'-'}/${not empty ses.first_bp_low ? ses.first_bp_low :'-' } &raquo; ${not empty ses.last_bp_high ? ses.last_bp_high:'-'}/${not empty ses.last_bp_low ? ses.last_bp_low :'-'}</td></tr>
							<tr><td align="center">${ses.min_bp_time }</td></tr>
						</table>
					</td>
				</c:forEach>
			</tr>
			<c:if test="${empty module}">
				<tr class="details"><td colspan="${noOfRecords+1}"><insta:ltext key="patient.dialysis.sessions.details"/></td></tr>
				<tr>
					<td >
						<table  width="100%" class="formtable">
							<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.session.details"/></td></tr>
							<tr><td align="right"><insta:ltext key="patient.dialysis.sessions.prescription.date"/></td></tr>
						</table>
					</td>
					<c:forEach items="${sesList}" var="ses">
						<td>
							<table width="100%" class="formtable">
								<tr>
									<td align="center">
										<a href="PreDialysisSessions.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&dialysisprescId=${ses.prescription_id}&order_id=${ses.order_id}"><insta:ltext key="patient.dialysis.sessions.pre"/></a>
										<c:if test="${ses.status == 'I' || ses.status == 'F' || ses.status == 'C'}">
										| <a href="IntraDialysisSessions.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&order_id=${ses.order_id}"><insta:ltext key="patient.dialysis.sessions.intra"/></a>
										</c:if>
										<c:if test="${ses.status == 'F' || ses.status == 'C'}">
											| <a href="PostDialysisSessions.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&order_id=${ses.order_id}"><insta:ltext key="patient.dialysis.sessions.post"/></a>
										</c:if>
									</td>
								</tr>
								<tr>
									<td align="center">
										<a href="DialysisPrescriptions.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&dialysis_presc_id=${ses.prescription_id}"><fmt:formatDate pattern="dd-MM-yyyy" value="${ses.presc_date}" /></a>
									</td>
								</tr>
							</table>
						</td>
					</c:forEach>
				</tr>
			</c:if>
		</table>
	</div>
	<table>
		<tr>&nbsp;</tr>
		<tr><td><a href="${cpath}/dialysis/DialysisSessionsSummary.do?_method=getDateRangeSelectionScreen&mrNo=${patient.mr_no}" ><insta:ltext key="patient.dialysis.sessions.print.dialysis.flow.sheet"/></a></td></tr>
	</table>
	</c:if>

<script>
	var methodName = '${ifn:cleanJavaScript(param._method)}';
</script>
</body>
</html>