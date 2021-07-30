<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Issue MRD Case File - Insta HMS</title>
<insta:link type="js" file="dashboardsearch.js" />
<insta:link type="js" file="medicalrecorddepartment/issuemrdcasefile.js"/>
<insta:link type="js" file="medicalrecorddepartment/mrdcasefiledialog.js"/>
<insta:link type="js" file="ajax.js"/>
</head>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Available"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>
<c:set target="${statusDisplay}" property="L" value="Lost"/>
<c:set target="${statusDisplay}" property="U" value="Issued"/>

<body class="yui-skin-sam" onload="init();">
<form action="./MRDCaseFileIssue.do" method="POST" name="issueCasefileForm">
	<input type="hidden" name="_method" value="issueCasefile">

	<h1>Issue MRD Case File </h1>

	<insta:feedback-panel/>
	<jsp:useBean id="now" class="java.util.Date"/>
	<c:set var="dt" value="${now}"/>
	<fmt:formatDate value="${dt}" pattern="HH:mm" var="time"/>
	<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="date"/>

	<input type="hidden" name="indentOnhid" id="indentOnhid" value=""/>
	<input type="hidden" name="indentOnDatehid" id="indentOnDatehid" value=""/>
	<input type="hidden" name="indentOnTimehid" id="indentOnTimehid" value=""/>

	<div class="resultList">
		<table  class="resultList" id="casefileIssueTable">
			<tr id="mrdRow0">
				<th>MR No</th>
				<th>Patient Name</th>
				<th>Casefile No</th>
				<th>Indented Department</th>
				<th>Issued To</th>
				<th>Indented On</th>
				<th>Purpose</th>
				<th style="width:20px"></th>
				<th style="width:20px"></th>
				<th style="width:20px"></th>
			</tr>
			<c:set var="numCasefiles" value="${fn:length(contentMap)}"/>
			<c:forEach var="i" begin="1" end="${numCasefiles+1}" varStatus="loop">
				<c:set var="caseMap" value="${contentMap[i-1]}"/>
				<c:choose>
					<c:when test="${empty caseMap}">
							<c:set var="style" value='style="display:none"'/>
					</c:when>
					<c:otherwise>
							<c:set var="rowId" value="mrdRow${i}"/>
					</c:otherwise>
				</c:choose>
			<tr ${style}>
				<td>
					<label id="mrnum">${caseMap.mr_no}</label>
					<input type="hidden" name=mrNo value="${caseMap.mr_no}"/>
				</td>
				<td>
					<insta:truncLabel value="${caseMap.patient_full_name}" length="15" id="pat_name"/>
					<input type="hidden" name="patname"  value="${caseMap.patient_full_name}" />
				</td>
				<td <c:if test="${caseMap.mlc_status == 'Y'}">class="mlcIndicator"</c:if>><label id="case_file">${caseMap.casefile_no}</label>
					<input type="hidden" name="casefile" value="${caseMap.casefile_no}"/>
					<input type="hidden" name="mlc_status" id="mlc_status" value="${caseMap.mlc_status}"/>
				</td>
				<td>
					<label id="requested_by">
						<insta:truncLabel value="${caseMap.requesting_dept}" length="15"/>
					</label>
					<input type="hidden" name="requestedBy"  value="${caseMap.requesting_dept}"/>
					<input type="hidden" name="issuedto"  value="${caseMap.requested_by}"/>
				</td>
				<td>
					<c:choose>
					<c:when test="${caseMap.requesting_dept ne '' && caseMap.requesting_dept !=null}">
					<label id="issued_to">
						<insta:truncLabel value="${caseMap.requesting_dept}" length="15"/></label>
						<input type="hidden" name="issuedToId" value="${caseMap.req_dept_id}"/>
						<input type="hidden" name="issuedToName" value="${caseMap.requesting_dept}"/>
						<input type="hidden" name="issueType" value="D"/>
					</c:when>
					<c:otherwise>
					<label id="issued_to">
						<insta:truncLabel value="${caseMap.dept_name}" length="15"/></label>
						<input type="hidden" name="issuedToId" value="${caseMap.dept_id}"/>
						<input type="hidden" name="issuedToName" value="${caseMap.dept_name}"/>
						<input type="hidden" name="issueType" value="D"/>
					</c:otherwise>
					</c:choose>
				</td>
				<td>
					<label id="indented_on"><insta:truncLabel value="${caseMap.indent_date}" length="10"/></label>
					<input type="hidden" name="indentedOnDate" id="indentedOnDate" value="${caseMap.ind_date}"/>
					<input type="hidden" name="indentedOnTime" id="indentedOnTime" value="${caseMap.ind_time}"/>
					<input type="hidden" name="issuedOnDate" value="${ifn:cleanHtmlAttribute(date)}"/>
					<input type="hidden" name="issuedOnTime" value="${ifn:cleanHtmlAttribute(time)}"/>
					<label id="reg_date"></label>
					<input type="hidden" name="regDate"  value="${caseMap.regdate}"/>
					<label id="reg_time"></label>
					<input type="hidden" name="regTime"  value="${caseMap.regtime}"/>
				</td>
				<td>
					<label id="lblPurpose">
						<insta:truncLabel value="${caseMap.purpose}" length="10"/></label>
					<input type="hidden" name="purpose" value="${caseMap.purpose}"/>
				</td>
				<td><a href="javascript:void(0)" onclick="return cancelCasefile(this);" title="Cancel case file "><img src="${cpath}/icons/Delete.png"/></a>
				</td>
				<td>
					<a href="javascript:void(0)" onclick="return editCasefileDialog(this)" title="Edit case file">
						<img src="${cpath}/icons/Edit.png"/>
				</td>
			</tr>
			</c:forEach>
			<tr>
				<td colspan="8"/>
					<td>
						<c:if test="${GMRDDetails.allowIndentBasedIssue == 'Y'}">
							<button type="button" name="disabledbtnAddCasefiles" id="disabledbtnAddCasefiles">
								<img src="${cpath}/icons/Add1.png" class="button"/>
							</button>
						</c:if>
						<c:if test="${GMRDDetails.allowIndentBasedIssue == 'N'}">
							<button type="button" name="btnAddCasefiles" id="btnAddCasefiles" class="imgButton"
								title="Add New Case files" onclick="addMrdDialog.start(this);" accesskey="+">
								<img src="${cpath}/icons/Add.png" class="button"/>
							</button>
						</c:if>
				</td>
				<td>
					<c:if test="${GMRDDetails.allowIndentBasedIssue == 'N'}">
						<button type="button" name="btnAddMultCasefile" id="btnAddMultCasefile" class="imgButton"
							title="Add multiple Case files" onclick="addMultiCasefileDialog(this);" accesskey="*">
							<img src="${cpath}/icons/funnel_plus.png" class="button"/>
						</button>
					</c:if>
				</td>
			</tr>
		</table>
		</br>
		</div>

	<table class="screenActions">
		<tr>
		<td>
		<button type="submit" accesskey="I" name="save" onclick="return onSaveValidate();">
		 <label><b><u>I</u></b>ssue</label></button>&nbsp;</td>

			<td>&nbsp;|&nbsp; </td>
			<td><a href="javascript:void(0)" onclick="backToSearch();return true;">MRD Case Files Search</a></td>
		</tr>
	</table>
</form>
<form name="editForm">
	<input type="hidden" name="editRowId" value=""/>
	<div id="editCasefileDialog" style="visibility: hidden; display: none;">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Edit case file </legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Issue To :</td>
						<td valign="top">
							<div id="issuedTo_wrapper">
								<input type="text" name="edit_issued_to" id="edit_issued_to" value="${ifn:cleanHtmlAttribute(param.edit_issued_to)}"
								style="width: 200px"/>
								<div id="editIssuedToDropdown" class="autocomplete" style="width: 220px"></div>
								<input type="hidden" name="edit_issued_to_id" id="edit_issued_to_id" value=""/>
								<input type="hidden" name="edit_issued_to_type" id="edit_issued_to_type" value=""/>
							</div>
						</td>
						<td class="formlabel">Indent Date :</td>
						<td>
							<table>
								<tr>
									<td>
										<c:choose>
										<c:when test="${(roleId == 1) || (roleId == 2) || (actionRightsMap.allow_backdate == 'A') }">
										<insta:datewidget name="indent_date" id="indent_date" value="${param.indent_date}"/>
										<input type="text" name="indent_time" id="indent_time" value="${ifn:cleanHtmlAttribute(param.indent_time)}" class="timeField"/>
										<input type="hidden" name="issue_date" id="issue_date" value="${ifn:cleanHtmlAttribute(date)}"/>
										<input type="hidden" name="issue_time" id="issue_time" value="${time}" class="timeField"/>
										</c:when>
										<c:otherwise>
										<insta:datewidget name="indent_date" id="indent_date" value="${param.indent_date}" readonly="true"/>
										<input type="text" name="indent_time" id="indent_time" value="${ifn:cleanHtmlAttribute(param.indent_time)}"  readonly="true" class="timeField"/>
										<input type="hidden" name="issue_date" id="issue_date" value="${ifn:cleanHtmlAttribute(date)}" readonly="true"/>
										<input type="hidden" name="issue_time" id="issue_time" value="${time}" readonly="true" class="timeField"/>
										</c:otherwise>
										</c:choose>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<td class="formlabel"> Purpose of issue :</td>
						<td colspan="3"><input type="text" name="issuePurpose" id="issuePurpose" style="width:400px"/></td>
					</tr>
				</table>
			</fieldset>
			<table>
				<tr>
					<td>
						<button type="button" name="btnEdit" id="btnEdit" accesskey="E" onclick="return onEditCasefile();">
							<b><u>E</u></b>dit</button>
					</td>
				</tr>
			</table>
		</div>
	</div>
</form>
<form name="addMultipleCasefileform">
	<div id="addMultipleCasefileDialog" style="visibility: hidden; display: none;">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Multiple Case Files </legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Department :</td>
						<td valign="top">
							<div id="issuedTo_wrapper">
								<input type="text" name="req_issued_to" id="req_issued_to" value="${ifn:cleanHtmlAttribute(param.req_issued_to)}"/>
								<div id="reqIssuedToDropdown" class="autocomplete" ></div>
								<input type="hidden" name="issue_to_id" value=""/>
								<input type="hidden" name="issue_to_type" value=""/>
							</div>
						</td>
					</tr>
					<tr>
							<td class="formlabel">Indented Date:</td>
							<td class="forminfo">
								<insta:datewidget name="request_date" id="request_date" value="${date}"/>
							</td>
					</tr>
					<tr>
						<td class="formlabel">Year :</td>
						<td class="forminfo"><input type="text" name="year" value="${ifn:cleanHtmlAttribute(param.year)}" id="year"/></td>
					</tr>
				</table>
			</fieldset>
			<table>
				<tr>
					<td>
						<button type="button" name="btnAdd" id="btnAdd" accesskey="D" onclick="onAddMulitpleCasefile();">
							A<b><u>d</u></b>d</button>
						<button type="button" name="close" id="close"  onclick="closeMultiDialog();">
							Close</button>
					</td>
				</tr>
			</table>
		</div>
	</div>
</form>
	<jsp:include page="/pages/medicalrecorddepartment/AddMRDCasefilesDialog.jsp"></jsp:include>
<script>
	var contextPath = '${cpath}';
	var indentbasedissue = '${ifn:cleanJavaScript(GMRDDetails.allowIndentBasedIssue)}';
</script>
</body>
</html>
