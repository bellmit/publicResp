<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
	<head>
		<title>Raise Case file Indent - Insta HMS</title>
		<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"/>
		<insta:link type="js" file="dashboardsearch.js" />
		<insta:link type="js" file="ajax.js" />
		<insta:link type="js" file="medicalrecorddepartment/raiseindentcasefile.js"/>
		<insta:link type="js" file="medicalrecorddepartment/mrdcasefiledialog.js"/>
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		<script>
			var contextPath = '${cpath}'
			var roleId = '${ifn:cleanJavaScript(roleId)}';
			var allowBackdate = '${actionRightsMap.allow_backdate}';
			var dept_list = ${deptlist};
		</script>
	</head>
	<body onload="init();" name="indentForm"  class="yui-skin-sam">
		<div class="pageheader">Raise Case file Indent</div>
		<form method="POST" action="./RaiseMRDCasefileIndent.do" name="raiseIndentForm">
			<input type="hidden"	name="_method" value="raiseMRDCasefileIndent"/>
			<div><insta:feedback-panel/></div>
		  <jsp:useBean id="now" class="java.util.Date"/>
			<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Requested By:</td>
					<td><label>${ifn:cleanHtml(userName)}</label></td>
					<td class="formlabel">Indent Date :</td>
					<td>
						<c:set var="dt" value="${now}"/>
						<fmt:formatDate value="${dt}" pattern="HH:mm" var="time"/>
						<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="date"/>
						<table>
							<tr>
								<td>
									<c:choose>
									<c:when test="${ (roleId == 1) || (roleId == 2) || (actionRightsMap.allow_backdate =='A') }">
									<insta:datewidget name="indent_date" id="indent_date" value="${date}" />
									<input type="text" name="indent_time" id="indent_time" value="${time}" class="timeField"/>
									</c:when>
									<c:otherwise>
									<insta:datewidget name="indent_date" id="indent_date" value="${date}" readonly="true"/>
									<input type="text" name="indent_time" id="indent_time" value="${time}" readonly="true" class="timeField"/>
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
			 <table  class="resultList" id="mrdIndentTable">
				 <tr id="mrdRow0">
					 <th>MR No</th>
					 <th>Patient Name</th>
					 <th>Casefile No</th>
					 <th>Indent Department</th>
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
						<c:set var="rowId" value="mrdRow${i}"/>
					</c:otherwise>
				 </c:choose>
				 <tr ${style}>
					 <td>
						 <label id="mrnum">${caseMap.mr_no}</label>
						 <input type="hidden" id="mrNo" name="mrNo" value="${caseMap.mr_no}"/>
					 </td>
					 <td>
						 <label id="pat_name">${caseMap.patient_full_name} </label>
						 <input type="hidden" name="patname" value="${caseMap.patient_full_name}" />
					 </td>
					 <td <c:if test="${caseMap.mlc_status == 'Y'}">class="mlcIndicator"</c:if>>
						 <label id="case_file">${caseMap.casefile_no}</label>
						 <input type="hidden" name="casefile" value="${caseMap.casefile_no}"/>
						 <input type="hidden" name="mlc_status" id="mlc_status" value="${caseMap.mlc_status}"/>
					 </td>
					 <td>
						 <label id="dept_name">${caseMap.dept_name} </label>
						 <input type="hidden" name="deptName" value="${caseMap.dept_name}"/>
						 <input type="hidden" name="deptId" value="${caseMap.dept_id}"/>
						 <label id="reg_date"></label>
						 <input type="hidden" name="regDate"  value="${caseMap.regdate}"/>
						 <label id="reg_time"></label>
						 <input type="hidden" name="regTime"  value="${caseMap.regtime}"/>
					 </td>
					 <td>
						 <a href="javascript:void(0)" onclick="return cancelCasefile(this);" title="Cancel case file "><img src="${cpath}/icons/Delete.png"/></a>
					</td>
					<td>
						<button type="button" name="btnEditCasefiles" id="btnEditCasefiles"
							title="Edit Case File" onclick="editCaseFile(this)"
							accesskey="" class="imgButton"> <img src="${cpath}/icons/Edit.png" class="button" />
					</td>
				 </tr>
				 </c:forEach>
				 <tr>
					 <td colspan="4"/>
					 <td>
						 <button type="button" name="btnAddCasefiles" id="btnAddCasefiles"
							 title="Add New Case files " onclick="addMrdDialog.start()"
							 accesskey="+" class="imgButton"> <img src="${cpath}/icons/Add.png" class="button"/>
				 </td>
				 </tr>

			 </table>

			 <div id="dialog" style="visibility:hidden">
			 	<div class="bd">
			 		<table cellspacing="0" cellpadding="0" width="100%">
			 			<tr>
			 				<td>Mr No</td>
			 				<td>Patient Name</td>
			 				<td>Case File No</td>
			 				<td>Department</td>
			 			</tr>
			 			<tr>
			 				<td><label id="mno"></label></td>
			 				<td><label id="patient_name"></label></td>
			 				<td><label id="casefileno"></label></td>
			 				<td valign="top"><div id="dept_auto" style="width: 17em; padding-bottom:0.2em; z-index: 9000;">
										<input type="text" name="depname" id="depname" tabindex="4" style="width: 16em"  />
										<div id="dept_dropdown"></div>
									</div>
									<input type="hidden" name="depid" id="depid" />
									<input type="hidden" name="selected_row_id" id="selected_row_id" />
							</td>
			 			</tr>
			 			<tr>
			 			</tr>
			 			<tr>
							<td>
								<button type="button" id="OK" name="OK" accesskey="A"  style="display: inline;" class="button" onclick="handelSubmit();"tabindex="6"><label><b>OK</b></label></button>
								<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel();"tabindex="7"><label><b>Cancel</b></label></button>
							</td>
						</tr>

			 		</table>

			 	</div>
			 </div>

		<table style="padding-top: 10">
			<tr>
				<td>Indent Remarks :	</td>
				<td><input type="text" name="remarks" id="remarks" style="width: 50em"/></td>
			</tr>
		</table>
		</div>
			<table class="screenActions">
				<tr>
					<td>
						<button type="submit" name="indent" accesskey="I" onclick="return validateIndent();"/>
							<label>Raise <b><u>I</u></b>ndent</label></button>&nbsp;
					</td>
			<td>&nbsp;|&nbsp; </td>
			<td><a href="javascript:void(0)" onclick="backToSearch();return true;">MRD Case Files Search</a></td>
				</tr>
			</table>
		</form>
		<jsp:include page="/pages/medicalrecorddepartment/AddMRDCasefilesDialog.jsp"></jsp:include>
		</body>
</html>


