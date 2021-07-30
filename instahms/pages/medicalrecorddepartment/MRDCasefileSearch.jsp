<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>MRD Casefile Search -- Insta HMS</title>

	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="medicalrecorddepartment/mrdcasefilesearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

</head>
<body class="yui-skin-sam" onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
	<c:set var="dtoList" value="${pagedList.dtoList}"/>
	<h1> MRD Casefile Search </h1>
	<insta:feedback-panel/>
	<jsp:useBean id="now" class="java.util.Date"/>
	<c:set var="dt" value="${now}"/>
	<fmt:formatDate value="${dt}" pattern="HH:mm" var="time"/>
	<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="date"/>
	<form name="MRDCaseSearchForm" method="GET" >
		<input type="hidden" name="_method" value="searchCasefiles"/>
		<input type="hidden" name="_searchMethod" value="searchCasefiles"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="MRDCaseSearchForm" optionsId="optionalFilter" closed="${hasResults}"
		validateFunction="searchValidation()">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">MR No:</div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete" class="autocomplete">
							<input type="text" name="_mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param._mr_no)}" />
							<input type="hidden" name="_mr_no@op" value="ilike" />
							<div id="mrnoContainer"></div>
						</div>
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td>
					    	<div class="sfLabel">Case File No:</div>
					    	<div class="sfField">
					    			<input type="text" name="casefile_no" size="10" value="${ifn:cleanHtmlAttribute(param.casefile_no)}"/>
					    			<input type="hidden" name="casefile_no@op" value="ico" />
								</div>
					    	<div class="sfLabel">Patient Department:</div>
					    	<div class="sfField">
				    			<insta:selectdb displaycol="dept_name" name="_dept_id" table="department"
								    dummyvalue="-- Select --"	valuecol="dept_id" value="${param._dept_id}" orderby="dept_name"/>
				    				<input type="hidden" name="_dept_id@op" value="ico" />
								</div>
							<div class="sfLabel">Type:</div>
							<div class="sfField">
								<insta:checkgroup name="_visit_type" opvalues="i,o,n"
									optexts="IP,OP,No Visit" selValues="${paramValues._visit_type}"/>
								<input type="hidden" name="visit_type@op" value="in" />
							</div>
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="_visit_status" opvalues="A,I"
									optexts="Active,Inactive" selValues="${paramValues._visit_status}"/>
								<input type="hidden" name="_visit_status@op" value="in" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Requested by Department</div>
							<div class="sfField">
							<insta:selectdb name="_req_dept_id" id="req_dept_id" table="department"
								valuecol="dept_id"	displaycol="dept_name"  value="${param._req_dept_id}"
								filtercol="status"		dummyvalue="-- Select --" orderby="dept_name"/>
							</div>
							<div class="sfLabel">Requested by User</div>
							<div class="sfField">
								<insta:selectdb name="requested_by" id="requested_by" table="u_user" valuecol="emp_username"
								displaycol="emp_username"  value="${param.requested_by}" filtercol="emp_status"
								dummyvalue="-- Select --" orderby="emp_username"/>
							</div>
							<div class="sfLabel">Indent Status</div>
							<div class="sfField">
									<insta:checkgroup name="indented" id="indented" opvalues="Y,N" optexts="Indented,Non-indented"
									selValues="${paramValues.indented}"/>
									<input type="hidden" name="indented@op" id="indented@op" value="in"/>
							</div>
							<div class="sfLabel">Indented On:</div>
							<div class="sfField">
							<div class="sfFieldSub">From:</div>
									<insta:datewidget name="requested_date" id="requested_date0" valid="past" value="${paramValues.requested_date[0]}" />
									<input type="hidden" name="request_date@type" value="date"/>
									<input type="hidden" name="request_date@op" value="ge,le"/>
									<input type="hidden" name="request_date@cast" value="y"/>
									<input type="text" name="requested_time" id="requested_time0"
									value="00:00" class="timefield"/>
					   	</div>
					   <c:choose>
					   	<c:when test="${empty param.requested_date}">
						<div class="sfField">
					    <div class="sfFieldSub">To:</div>
							<insta:datewidget name="requested_date" id="requested_date1" value="${paramValues.requested_date[1]}"/>
							<input type="text" name="requested_time" id="requested_time1"
									value="23:59" class="timefield" />
							</div>
						</c:when>
						<c:otherwise>
						<div class="sfField">
					    <div class="sfFieldSub">To:</div>
							<insta:datewidget name="requested_date" id="requested_date1" value="today"/>
							<input type="text" name="requested_time" id="requested_time1"
									value="23:59" class="timefield" />
							</div>
						</c:otherwise>
						</c:choose>
						</td>
						<td>
							<div class="sfLabel">Issued to Department:</div>
							<div class="sfField">
								<insta:selectdb name="_issued_to_dept" id="issued_to_dept" table="department"
									valuecol="dept_id" displaycol="dept_name" value="${param._issued_to_dept}"
									filtercol="status"	dummyvalue="-- Select --" orderby="dept_name"/>
							</div>
							<div class="sfLabel">Issued to User</div>
							<div class="sfField">
									<insta:selectdb name="_issued_to_user" id="issued_to_user" table="mrd_casefile_users"
											valuecol="file_user_id"	displaycol="file_user_name"  value="${param._issued_to_user}"
											filtercol="status" 	dummyvalue="-- Select --" orderby="file_user_name"/>
								    <input type="hidden" name="issued_to_user@type" value="integer"/>
								    <input type="hidden" name="issued_to_user@cast" value="y"/>
							</div>
							<div class="sfLabel">Issued On:</div>
							<div class="sfField">
							<div class="sfFieldSub">From:</div>
									<insta:datewidget name="issued_on" id="issued_on0" valid="past"	value="${paramValues.issued_on[0]}" />
									<input type="hidden" name="issued_on@type" value="date"/>
									<input type="hidden" name="issued_on@op" value="ge,le"/>
									<input type="hidden" name="issued_on@cast" value="y"/>
									<input type="text" name="issued_on_time" id="issued_on_time0"
										value="00:00" class="timefield"/>
					   	</div>
					   	<c:choose>
					   	<c:when test="${empty param.issued_on}">
							<div class="sfField">
					    		<div class="sfFieldSub">To:</div>
										<insta:datewidget name="issued_on" id="issued_on1" value="${paramValues.issued_on[1]}"/>
										<input type="text" name="issued_on_time" id="issued_on_time1"
											value="23:59" class="timefield"/>
								</div>
						</c:when>
						<c:otherwise>
							 <div class="sfField">
					    		<div class="sfFieldSub">To:</div>
										<insta:datewidget name="issued_on" id="issued_on1" value="today"/>
										<input type="text" name="issued_on_time" id="issued_on_time1"
											value="23:59" class="timefield"/>
								</div> 
						</c:otherwise>
						</c:choose>
						</td>
						<td class="last">
							<div class="sfLabel">Issued Status</div>
							<div class="sfField">
									<insta:checkgroup name="file_status" opvalues="U,A,L,I" optexts="Issued,Not Issued,Lost,Inactive"
									selValues="${paramValues.file_status}"/>
									<input type="hidden" name="file_status@op" value="in"/>
							</div>
							<div class="sfLabel">Case file Status:</div>
							<div class="sfField">
								<insta:checkgroup name="case_status" optexts="Pending,MRD Updated,Inactive" opvalues="P,A,I" selValues="${paramValues.case_status}"/>
								<input type="hidden" name="case_status@op" value="in" />
							</div>

						</td>
					</tr>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	</form>

	<form name="MRDSearchResults" method="POST" action="./MRDCaseFileSearch.do">
		<input type="hidden" name="_mrdscreen" value="${ifn:cleanHtmlAttribute(mrdscreen)}"/>
		<input type="hidden" name="_method" value="redirectScreens"/>
		<div style="padding-bottom: 10px">
			<table>
				<tr>
					<td>Action:&nbsp</td>
					<td>
						<insta:selectoptions name="_action"  opvalues="indent,issue,return,close" optexts="Raise Indent,Issue Casefile,Return Casefile,Close Indent"
						value="${param.action}"  dummyvalue="-- Select --" id="action" onchange="onChangeAction();"/>
					</td>
				</tr>
			</table>
		</div>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>Select</th>
					<insta:sortablecolumn name="mca.mr_no" title="MR No"/>
					<insta:sortablecolumn name="casefile_no" title="Case file number"/>
					<th>Patient Name</th>
					<th>Casefile with</th>
					<th>Requested Dept </th>
					<th>Requested By </th>
					<th>Issued On</th>
					<insta:sortablecolumn name="indent_date" title="Indented On"/>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="status">
					<c:set var="i" value="${status.index}"/>

						<c:choose>
						<c:when test="${record.file_status == 'L'}">
								<c:set var="enableChk" value="disabled"/>
							</c:when>
							<c:otherwise>
								<c:set var="enableChk" value=""/>
							</c:otherwise>
						</c:choose>

					<tr class="${status.index == 0 ? 'firstRow' : ''} ${status.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${status.index}, event, 'resultTable',
						{mr_no: '${ifn:cleanJavaScript(record.mr_no)}', mrdscreen: '${ifn:cleanJavaScript(param.mrdscreen)}',status:'${ifn:cleanJavaScript(record.status)}'},'');" id="toolbarRow${ifn:cleanHtmlAttribute(status.index)}">
							<td>
								<input type="checkbox" name="_fileCheck" id="fileCheck${i}" value="${record.mr_no}"
								onclick="onFileCheck(this, 'hiddenMrno${i}',${i})" ${enableChk}/>
									<input type="hidden" name="_hiddenMrno" id="hiddenMrno${i}"/>
									<input type="hidden" name="_caseStatus" id="caseStatus${i}"
									value="${record.case_status}"/>
									<input type="hidden" name="_regDate" id="regDate${i}"
									value='<fmt:formatDate value="${record.regdate}" pattern="dd-MM-yyyy"/>'/>
									<input type="hidden" name="_regTime" id="regTime${i}"
									value='<fmt:formatDate value="${record.regtime}" pattern="HH:MM"/>'/>
									<input type="hidden" name="_hiddenCaseNo" id="hiddenCaseNo${i}" value="${record.casefile_no}"/>
									<c:set var="patientName" value="${record.patient_full_name}"/>
									<input type="hidden" name="_patientName" id="patientName${i}" value="${ifn:cleanHtmlAttribute(patientName)}"/>
									<input type="hidden" name="_department" id="department${i}" value="${record.dept_name}"/>
									<input type="hidden" name="_deptId" id="deptId${i}" value="${record.dept_id}"/>
									<input type="hidden" name="_casefileWith" id="casefileWith${i}" value="${record.casefile_with}"/>

									<input type="hidden" name="_fileStatus" id="fileStatus${i}" value="${record.file_status}"/>
									<input type="hidden" name="_indented" id="indented${i}" value="${record.indented}"/>
									<input type="hidden" name="_issuedToDept" id="issuedToDept${i}" value="${record.issued_to_dept}"/>
									<input type="hidden" name="_issuedToUser" id="issuedToUser${i}" value="${record.issued_to_user}"/>
									<input type="hidden" name="_requestedBy" id="requestedBy${i}" value="${record.requesting_dept}"/>
									<input type="hidden" name="_requestedByDept" id="requestedByDept${i}" value="${record.req_dept_id}"/>
									<input type="hidden" name="_mlcStatus" id="mlcStatus${i}" value="${record.mlc_status}"/>
								</td>
						<c:choose>
							<c:when test="${record.status eq 'I'}"><c:set var="color" value="grey"/></c:when>
							<c:when test="${record.status eq 'A'}"><c:set var="color" value="empty"/></c:when>
							<c:when test="${record.death_status eq 'D'}"><c:set var="color" value="black"/></c:when>
							<c:otherwise><c:set var="color" value="green"/></c:otherwise>
						</c:choose>
						<td >
							<img src='${cpath}/images/${color}_flag.gif'> ${record.mr_no}
						</td>
						<td <c:if test="${record.mlc_status == 'Y' }">class="mlcIndicator"</c:if>>${record.casefile_no}</td>
						<c:set var="fullName" value="${record.patient_full_name}"/>
						<td><insta:truncLabel value="${fullName}" length="20"/></td>
						<td>
							<c:if test="${record.file_status eq 'A'}">MRD</c:if>
							<c:if test="${record.file_status eq 'L'}">Lost</c:if>
							<c:if test="${record.file_status eq 'U'}">${record.casefile_with}</c:if>
							<c:if test="${record.file_status eq 'I'}">Inactive</c:if>
						</td>
						<td><insta:truncLabel value="${record.requesting_dept}" length="20"/></td>
						<td><insta:truncLabel value="${record.requested_by}" length="15"/></td>
						<td><c:if test="${record.file_status ne 'A'}"><fmt:formatDate value="${record.issued_on}" pattern="dd-MM-yyyy HH:mm"/></c:if></td>
						<td>${record.indent_date}
						<input type="hidden" name="_indentDateTimeHid" id="indentDateTimeHid${i}" value="${record.indent_date}"/>
						<input type="hidden" name="_indentDateHid" id="indentDateHid${i}" value="${record.ind_date}"/>
						<input type="hidden" name="_indentTimeHid" id="indentTimeHid${i}" value="${record.ind_time}"/>
						</td>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>

		<table>
			<tr>
				<td>
					<div style="float: left">
						<input type="radio" name="_selectItem" id="singlecase" value="singleFile"
						onclick="onCheckRadio(this.value)">
						Select Single Casefile
					</div>
					<div style="float: left">
						<input type="radio" name="_selectItem" id="allcase" value="all" onclick="onCheckRadio(this.value)">
						Select All Casefiles
					</div>
				</td>
			</tr>
		</table>

		<div class="screenAction">
				<button type="submit" name="_btnActions" id="btnActions" accesskey="A"
					onclick="return onSubmitAction();"> <b><u>A</u></b>ction</button>
				<button type="button" name="_printPatients" id="_printPatients" accesskey="P"
					onclick="return printPatientsList()"><b><u>P</u></b>rint</button>
		</div>
	</form>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive Patients</div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText">Patients Without Visits</div>
		<div class="flag"><img src='${cpath}/images/black_flag.gif'></div>
		<div class="flagText">Patient dead</div>
	</div>
	<script>
		var cpath = '${cpath}';
		var allowIndentBasedIssue = '${ifn:cleanJavaScript(GMRDDetails.allowIndentBasedIssue)}';
	</script>
</body>
</html>
