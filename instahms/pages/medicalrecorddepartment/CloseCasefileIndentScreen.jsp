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
		<title>Close Case file Indent - Insta HMS</title>
		<meta http-equiv="Content-type" content="text/html; charset=iso-8859-1"/>
		<insta:link type="js" file="dashboardsearch.js" />
		<insta:link type="js" file="ajax.js" />
		<insta:link type="js" file="medicalrecorddepartment/closeindentcasefile.js"/>
		<insta:link type="js" file="medicalrecorddepartment/mrdcasefiledialog.js"/>
		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		<script>
			var contextPath = '${cpath}'
		</script>
	</head>
	<body onload="init();" name="indentForm"  class="yui-skin-sam">
		<div class="pageheader">Close Case file Indent</div>
		<form method="POST" action="./CloseMRDCasefileIndent.do" name="closemrdCasefileForm">
			<input type="hidden"	name="_method" value="closeCasefileIndent"/>
			<div><insta:feedback-panel/></div>
		  <jsp:useBean id="now" class="java.util.Date"/>
			<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Closed By:</td>
					<td><label>${ifn:cleanHtml(userName)}</label></td>
					<td class="formlabel">Closed Date :</td>
					<td>
						<c:set var="dt" value="${now}"/>
						<fmt:formatDate value="${dt}" pattern="HH:mm" var="time"/>
						<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="date"/>
						<table>
							<tr>
								<td>
									${ifn:cleanHtml(date)} ${ifn:cleanHtml(time)}</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</fieldset>
		 <div class="resultList">
			 <table  class="resultList" id="closeIndentTable">
				 <tr id="mrdRow0">
					 <th>MR No</th>
					 <th>Patient Name</th>
					 <th>Casefile No</th>
					 <th>Department</th>
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
						 <input type="hidden" name=mrNo  value="${caseMap.mr_no}"/>
					 </td>
					 <td>
						 <label id="pat_name">${caseMap.patient_full_name} </label>
						 <input type="hidden" name="patname" value="${caseMap.patient_full_name}" />
					 </td>
					 <td <c:if test="${caseMap.mlc_status == 'Y'}">class="mlcIndicator"</c:if>>
						 <label id="case_file">${caseMap.casefile_no} </label>
						 <input type="hidden" name="casefile" value="${caseMap.casefile_no}"/>
						 <input type="hidden" name="mlc_status" id="mlc_status" value="${caseMap.mlc_status}"/>
					 </td>
					 <td>
						 <label id="dept_name">${caseMap.dept_name}</label>
						 <input type="hidden" name="deptname" value="${caseMap.dept_name}"/>
					 </td>
					 <td><a href="javascript:void(0)" onclick="cancelCasefile(this)" title="Cancel case file "><img src="${cpath}/icons/Delete.png"/></a></td>
				 </tr>
				 </c:forEach>
				 <tr>
					 <td colspan="4"/>
					 <td>
						 <button type="button" name="btnAddCasefiles" id="btnAddCasefiles" class="imgButton"
							 title="Add New Case files" onclick="addMrdDialog.start();" accesskey="+">
							 <img src="${cpath}/icons/Add.png" class="button"/></button>
				 </td>
				 </tr>

			 </table>
		</div>
			<table class="screenActions">
				<tr>
					<td>
					<button type="submit" name="indent" accesskey="C" onclick="return validateIndent();">
					<label><b><u>C</u></b>lose Indent</label></button>&nbsp;

					</td>
			<td>&nbsp;|&nbsp; </td>
			<td><a href="javascript:void(0)" onclick="backToSearch();return true;">MRD Case Files Search</a></td>
				</tr>
			</table>
		</form>
		<jsp:include page="/pages/medicalrecorddepartment/AddMRDCasefilesDialog.jsp"></jsp:include>
	</body>
</html>


