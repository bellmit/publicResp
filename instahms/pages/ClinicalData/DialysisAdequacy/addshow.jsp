<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="clinicaldata.dialysisadequacy.addoredit.addoreditdialysisadequacydetails"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="DilaysisAdequacy/adequacy.js"/>

<script>
</script>
<insta:js-bundle prefix="clinicaldata.dialysisadequacy"/>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
<insta:js-bundle prefix="clinicaldata.common"/>
</head>
<body>
<c:choose>
<c:when test="${empty param.mr_no}">
	<h1 style="float: left"><insta:ltext key="clinicaldata.dialysisadequacy.addoredit.dialysisadequacydetails"/></h1>
	<c:url var="url" value="/clinical/DialysisAdequacy.do"/>
	<insta:patientsearch fieldName="mr_no" searchUrl="${url}" searchMethod="show" searchType="mrNo" />
	<form name="adequacyForm" action="${cpath}/clinical/DialysisAdequacy.do" method="post">
	<input type="hidden" name="_searchMethod" value="show"/>
	<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:when>
<c:otherwise>
	<h1><insta:ltext key="clinicaldata.dialysisadequacy.addoredit.dialysisadequacydetails"/></h1>
	<form name="adequacyForm" action="${cpath}/clinical/DialysisAdequacy.do" method="post">
	<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:otherwise>
</c:choose>
<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
	<c:if test="${not empty param.mr_no}">
		<insta:paginate curPage="${dateList.pageNumber}" numPages="${dateList.numPages}" totalRecords="${dateList.totalRecords}"/>
		<c:choose>
			<c:when test="${not empty dateList.dtoList}">
				<fieldset class="fieldsetborder">
					<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
						<tr>
							<th>&nbsp;</th>
							<c:forEach items="${dateList.dtoList}" var="dateRecord">
								<th title="<fmt:formatDate pattern="dd-MM-yyyy" value="${dateRecord.values_as_of_date}"/>">
									<fmt:formatDate pattern="dd-MM" value="${dateRecord.values_as_of_date}"/>
								</th>
							</c:forEach>
						</tr>
							<tr>
								<td class="formlabel">
									<insta:ltext key="clinicaldata.dialysisadequacy.addoredit.kt.v"/>
								</td>
								<c:forEach  items="${dateList.dtoList}" var="rec" varStatus="loop">
									<td>
										<label>${rec.ktv}</label>

									</td>
								</c:forEach>
							</tr>
							<tr>
								<td class="formlabel">
									<insta:ltext key="clinicaldata.dialysisadequacy.addoredit.urr"/>
								</td>
								<c:forEach  items="${dateList.dtoList}" var="rec" varStatus="loop">
									<td>
									   	<label>${rec.urr}</label>
									</td>
								</c:forEach>
							</tr>
					</table>
				</fieldset>
			</c:when>
			<c:otherwise>
				<table border="0" width="100%" height="100%">
					<tr>
						<td align="center" valign="top" style="font-size: 15pt;"><insta:ltext key="clinicaldata.dialysisadequacy.addoredit.norecordstodisplay"/></td>
					</tr>
				</table>
			</c:otherwise>
		</c:choose>
	</c:if>
	<div class="screenActions">
		<%--<button type="submit" accesskey="S" onclick="return validateForm()"><b><u>S</u></b>ave</button>--%>
		 <a href="${cpath}/clinical/DialysisAdequacy.do?_method=list"><insta:ltext key="clinicaldata.dialysisadequacy.addoredit.adequacysearch"/></a>
		| <a href="${cpath}/dialysis/PreDialysisSessions.do?_method=showDialysis&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="clinicaldata.dialysisadequacy.addoredit.predialysis"/></a>
	</div>
</form>

</body>
</html>

			<%-- 	<td>
					<a title="Cancel Dialysis Parameters Row">
						<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteItem(this)"/>
					</a>
				<td>
					<a title="Edit Dialysis Parameters" >
						<img src="${cpath}/icons/Edit.png" class="button" id="editIcon${index}" name="editIcon" onclick="openEditDialogBox(this)"/>
					</a>
				</td>
				<td>&nbsp;</td> --%>
	<%-- 	<table class="addButton">
			<tr>
				<td align="right">
					<button type="button" name="btnAddItem" id="btnAddItem" title="Press (Alt+Shift+(+)) Key to Add New Dialysis Parameters"
							onclick="openDialogBox();"
							accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</tr>
		</table>
		<div id="adequacyDialog" style="visibility:hidden">
		<div class="hd" id="itemdialogheader"></div>
		<div class="bd">
			<fieldset class="fieldsetborder">
			<legend class="fieldSetLabel">Dialysis Parameters</legend>
			<table class="formtable" cellpadding="0" cellspacing="0">
				<tr>
					<td class="formlabel">Observation Date:</td>
					<td>
						<input type="hidden" name="dialogId">
						<insta:datewidget name="dialog_values_as_of_date" id="dialog_values_as_of_date" value="${valueDate}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">URR</td>
					<td>
						<input type="text" name="dialog_urr" id="dialog_urr" value="" style="width:75px" onkeypress="return enterNumOnly(event)">
					</td>
				</tr>
				<tr>
					<td class="formlabel">Kt/V:</td>
					<td>
						<input type="text" name="dialog_ktv" id="dialog_ktv" value="" style="width:75px" onkeypress="return enterNumOnly(event)">
					</td>
				</tr>
			</table>
			</fieldset>
			<table>
				<tr>
					<td>
						<button type="button" name="Save" accesskey="S" onclick="addRecord();"><b><u>S</u></b>ave</button>
						<button type="button" name="Cancel" accesskey="C" onclick="cancelDialog();"><b><u>C</u></b>ancel</button>
					</td>
				</tr>
			</table>
		</div>
		</div>--%>