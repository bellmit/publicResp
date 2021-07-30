<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title><insta:ltext key="clinicaldata.scorecard.list.editscorecards"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<script language="javascript" type="text/javascript">
var cpath = '<%= request.getContextPath()%>';

var toolbar = {
	Edit: {
		title: "Edit Score Card",
		imageSrc: "icons/Report.png",
		href: '/clinical/ScoreCard.do?_method=edit',
		target: '_blank',
		onclick: null,
		description: "View and/or Edit the contents of this Score Card"
	},
	Rights: {
		title: "View Score Card",
		imageSrc: "icons/pdf-file-icon.png",
		href: '/clinical/ScoreCardList.do?_method=printScoreCardFromID',
		target: '_blank',
		onclick: null,
		description: "View Score Card PDF"
	},
};

function init(){
	createToolbar(toolbar);
}

function deleteCard(cardId, si) {
	var deletecard = confirm("Are you sure you want to delete score card "+si+"?")
	if(deletecard){
		document.ScoreCardListForm._method.value = 'delete';
		document.ScoreCardListForm.action= cpath+"/clinical/ScoreCardList.do"
		document.ScoreCardListForm._score_card_id_to_delete.value = cardId;
		document.ScoreCardListForm.submit();
	}
}

 function stopToolBar(chk) {
 	var e=window.event||arguments.callee.caller.arguments[0];
	if (e.stopPropagation) e.stopPropagation();
 }

function stopToolBarRightClick(chk) {
	var e=window.event||arguments.callee.caller.arguments[0];
	if (e.stopPropagation) e.stopPropagation();
	return false;
}


function clickIE() {
	if (document.all) {
		return false;
	}
}
function clickNS(e) {
	if(document.layers||(document.getElementById&&!document.all)) {
		if (e.which==2||e.which==3) {
		return false;
		}
	}
}

if (document.layers) {
	document.captureEvents(Event.MOUSEDOWN);
	document.onmousedown=clickNS;
} else{
		document.onmouseup=clickNS;
		document.oncontextmenu=clickIE;
}
document.oncontextmenu=new Function("return false")
</script>
</head>

<body  class="setMargin yui-skin-sam" onload="init();">

<jsp:useBean id="monthArray" class="java.util.HashMap"/>
<c:set target="${monthArray}" property="1" value="January"/>
<c:set target="${monthArray}" property="2" value="February"/>
<c:set target="${monthArray}" property="3" value="March"/>
<c:set target="${monthArray}" property="4" value="April"/>
<c:set target="${monthArray}" property="5" value="May"/>
<c:set target="${monthArray}" property="6" value="June"/>
<c:set target="${monthArray}" property="7" value="July"/>
<c:set target="${monthArray}" property="8" value="August"/>
<c:set target="${monthArray}" property="9" value="September"/>
<c:set target="${monthArray}" property="10" value="October"/>
<c:set target="${monthArray}" property="11" value="November"/>
<c:set target="${monthArray}" property="12" value="December"/>

	<h1 style="float: left"><insta:ltext key="clinicaldata.scorecard.list.view.edit.scorecard"/></h1>
		<c:url var="url" value="/clinical/ScoreCardList.do"/>
		<insta:patientsearch fieldName="mr_no" searchUrl="${url}" searchMethod="list" searchType="mrNo" />
		<insta:feedback-panel/>
		<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
		<form name="ScoreCardListForm"  method="GET" action="${cpath}/clinical/ScoreCardList.do">
			<input type="hidden" name="_method" value="list">
			<input type="hidden" name="_searchMethod" value="list"/>
			<input type="hidden" name="_score_card_id_to_delete" value=""/>
			<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">

			<c:if test="${not empty param.mr_no}">
				<c:choose>
					<c:when test="${not empty pagedList.dtoList}">
						<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
						<table width="100%" height="100%" border="0"  id="resultTable" class="resultList" align="center">
							<tr>
								<th style="width:70px;">&nbsp;</th>
								<th><insta:sortablecolumn name="card_month" add_th="false"
										title="Card Month" /></th>
								<th>
									<insta:sortablecolumn name="card_year" add_th="false"
										title="Card Year" />
								</th>
								<th>
									<insta:sortablecolumn name="nephrologist" add_th="false"
										title="Nephrologist" />
								</th>
								</th>
								<th>
									<insta:sortablecolumn name="save_date" add_th="false"
										title="Created Date" />
								</th>
								<th>
								</th>
							</tr>
							<c:forEach items="${pagedList.dtoList}" var="record" varStatus="st">
									<tr
										class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
										onclick="
										showToolbar( ${st.index}, event, 'resultTable',
										{mr_no: '${ifn:cleanJavaScript(param.mr_no)}', score_card_id: '${ifn:cleanJavaScript(record.score_card_id)}'},
										[${editEnabled}, true]);"
									    onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"
									>
										<td>${st.index + 1}</td>
										<c:set var="monNum">${record.card_month}</c:set>
										<td>${monthArray[monNum]}</td>
										<td>${record.card_year}</td>
										<td>${record.nephrologist}</td>
										<td>
											<fmt:formatDate pattern="dd-MM-yyyy" value="${record.save_date}"/>
										</td>
										<td  onclick="stopToolBar(this);">
											<c:if test="${editEnabled}">
												<img src="${cpath}/icons/Delete.png" class="imgDelete button" onclick="deleteCard('${record.score_card_id}','${st.index + 1}')"/>
											</c:if>
											<c:if test="${not editEnabled}">
												<img src="${cpath}/icons/Delete1.png" />
											</c:if>
										</td>
									</tr>
							</c:forEach>
						</table>
					</c:when>
					<c:otherwise>
						<table border="0" width="100%" height="100%">
							<tr>
								<td align="center" valign="top" style="font-size: 15pt;"><insta:ltext key="clinicaldata.scorecard.list.noscorecardscreated"/></td>
							</tr>
						</table>
					</c:otherwise>
				</c:choose>
				<div class="screenActions" style="float:left">
					<c:set var="addurl" value="${cpath}/clinical/ScoreCard.do?_method=show&mr_no=${param.mr_no}"/>
					<c:if test="${editEnabled}">
						<a href='<c:out value="${addurl}"/>'><insta:ltext key="clinicaldata.scorecard.list.addnewscorecard"/></a>
					</c:if>
				</div>
		 </c:if>
</form>
	<script>
	</script>
</body>
</html>