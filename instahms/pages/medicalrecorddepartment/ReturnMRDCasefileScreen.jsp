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
<title>Return MRD Case File - Insta HMS</title>
<insta:link type="js" file="dashboardsearch.js" />
<insta:link type="js" file="medicalrecorddepartment/returnmrdcasefile.js"/>
<insta:link type="js" file="medicalrecorddepartment/mrdcasefiledialog.js"/>
<insta:link type="js" file="ajax.js"/>
<style>
	.unselectedRow{background-color:lightgrey; }
</style>
</head>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Available"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>
<c:set target="${statusDisplay}" property="L" value="Lost"/>
<c:set target="${statusDisplay}" property="U" value="Issued"/>

<body class="yui-skin-sam" onload="init();">
<form action="./MRDCaseFileReturn.do" method="POST" name="returnCasefileForm">
	<input type="hidden" name="_method" value="returnCasefile">

	<h1>Return MRD Case File </h1>

	<insta:feedback-panel/>

	<jsp:useBean id="now" class="java.util.Date"/>
	<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Returned User:</td>
				<td><label>${ifn:cleanHtml(userName)}</label></td>
				<td class="formlabel">Returned On :</td>
				<td>
					<c:set var="dt" value="${now}"/>
					<fmt:formatDate value="${dt}" pattern="HH:mm" var="time"/>
					<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="date"/>
					<table>
						<tr>
							<td>
								<c:choose>
										<c:when test="${ (roleId == 1) || (roleId == 2) || (actionRightsMap.all_backdate == 'A') }">
												<insta:datewidget name="return_date" id="return_date" value="${date}"/>
												<input type="text" name="return_time" id="return_time" value="${time}" class="timeField"/>
										</c:when>
										<c:otherwise>
												<insta:datewidget name="return_date" id="return_date" value="${date}" readonly="true"/>
												<input type="text" name="return_time" id="return_time" value="${time}" readonly="true"
															class="timeField"/>
										</c:otherwise>
								</c:choose>
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</fieldset>

	<div class="resultList">
		<table  class="resultList" id="casefileReturnTable">
			<tr>
				<th>Select</th>
				<th>MR No</th>
				<th>Patient Name</th>
				<th>Casefile No</th>
				<th>Case file with</th>
				<th style="width:20px"></th>
				<th style="width:20px"></th>
			</tr>
			<c:set var="numCasefiles" value="${fn:length(contentMap)}"/>
			<c:forEach var="i" begin="1" end="${numCasefiles+1}" varStatus="loop">
			<c:set var="caseMap" value="${contentMap[i-1]}"/>
			<c:choose>
			<c:when test="${empty caseMap}">
			<c:set var="rowId" value="mrdRow_tempalate"/>
			<c:set var="style" value="style=display:none"/>
			</c:when>
			<c:otherwise>
			<c:set var="rowId" value="mrdRow${i-1}"/>
			</c:otherwise>
			</c:choose>
			<tr ${style} id=${rowId}>
				<td>
					<input type="hidden" name="selected" id="selected" value="true"/>
					<c:if test="${not empty caseMap}">
					<input type="checkbox" name="select_case_file" id="select_case_file" checked  onClick = "return setSelectedValue(this,${i-1})"/>
					</c:if>
				</td>
				<td>
					<label id="mrnum">${caseMap.mr_no}</label>
					<input type="hidden" name="requestedBy" value="${caseMap.requested_by}"/>
					<input type="hidden" name=mrNo value="${caseMap.mr_no}"/>
				</td>
				<td>
					<label id="pat_name">${caseMap.patient_full_name} </label>
					<input type="hidden" name="patname" value="${caseMap.patient_full_name}" />
				</td>
				<td <c:if test="${caseMap.mlc_status == 'Y'}">class="mlcIndicator"</c:if>>
					<label id="case_file">${caseMap.casefile_no}</label>
					<input type="hidden" name="casefile" name="casefile" value="${caseMap.casefile_no}"/>
					<input type="hidden" name="mlc_status" id="mlc_status" value="${caseMap.mlc_status}"/>
				</td>
				<td>
					<c:choose>
					<c:when test="${caseMap.issued_to_dept ne '' && caseMap.issued_to_dept ne null}">
							<label id="dept_name">${caseMap.casefile_with}</label>
							<input type="hidden" name="deptName" value="${caseMap.casefile_with}"/>
							<input type="hidden" name="deptId" value="${caseMap.issued_to_dept}"/>
							<input type="hidden" name="deptType" value="D"/>
					</c:when>
					<c:when test="${caseMap.issued_to_user ne '' && caseMap.issued_to_user ne null}">
							<label id="dept_name">${caseMap.casefile_with}</label>
							<input type="hidden" name="deptName" value="${caseMap.casefile_with}"/>
							<input type="hidden" name="deptId" value="${caseMap.issued_to_user}"/>
							<input type="hidden" name="deptType" value="U"/>
					</c:when>
					</c:choose>
					<label id="reg_date"></label>
					<input type="hidden" name="regDate"  value="${caseMap.regdate}"/>
					<label id="reg_time"></label>
					<input type="hidden" name="regTime"  value="${caseMap.regtime}"/>
				</td>
				<td><a href="javascript:void(0)" onclick="return cancelCasefile(this);" title="Cancel case file "><img src="${cpath}/icons/Delete.png"/></a></td>
				</td>
			</tr>
			</c:forEach>
			<tr>
				<td colspan="5"/>
					<td>
						<button type="button" name="btnAddCasefiles" id="btnAddCasefiles" class="imgButton"
							title="Add New Case files" onclick="addMrdDialog.start();" accesskey="+">
							<img src="${cpath}/icons/Add.png" class="button"/></button>
					</td>
					<td>
						<button type="button" name="btnAddMultCasefile" id="btnAddMultCasefile" class="imgButton"
							title="Add multiple Case files" onclick="addMultiCasefileDialog(this);" accesskey="*">
							<img src="${cpath}/icons/funnel_plus.png" class="button"/>
						</button>
					</td>

				</tr>
		</table>
		</div>

	<table class="screenActions">
		<tr>
<td><button type="submit" accesskey="R" name="save" onclick="return onReturnValidate()"><label><b><u>R</u></b>eturn Case file</label></button>&nbsp;</td>

			<td>&nbsp;|&nbsp; </td>
			<td><a href="javascript:void(0)" onclick="backToSearch();return true;">MRD Case Files Search</a></td>
		</tr>
	</table>
</form>
	<jsp:include page="/pages/medicalrecorddepartment/AddMRDCasefilesDialog.jsp"></jsp:include>
<form name="addMultipleCasefileform">
	<div id="addMultipleCasefileDialog" style="visibility: hidden; display: none;">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Multiple Case Files </legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Patient Type:</td>
						<td class="forminfo">
						<insta:selectoptions name="patient_type"  opvalues="i,o" optexts="IP,OP"
						value="${param.patient_type}"  dummyvalue="(All)" id="patient_type" />
						</td>
					</tr>
					<tr>
						<td class="formlabel">Department :</td>
						<td valign="top">
							<div id="returnDiv">
								<input type="text" name="return_dept_name" id="return_dept_name" value="${ifn:cleanHtmlAttribute(param.return_dept_name)}"/>
								<div id="reqIssuedToDropdown" class="autocomplete" ></div>
								<input type="hidden" name="return_dept_id" value=""/>
								<input type="hidden" name="return_dept_type" value=""/>
							</div>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Issued Date:</td>
						<td class="forminfo">
							<insta:datewidget name="issued_on" id="issued_on" value="${date}"/>
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
						<button type="button" name="close" id="close"  onclick="closeDailog();">
							Close</button>
					</td>
				</tr>
			</table>
		</div>
	</div>
</form>
<script>
	var contextPath = '${cpath}';
</script>
</body>
</html>
