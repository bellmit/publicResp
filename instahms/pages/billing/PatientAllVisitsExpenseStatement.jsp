<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<head>
	<title><insta:ltext key="billing.patientstatement.print.title"/></title>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
<script type="text/javascript">

	function init(){
	  setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
	  initMrNoAutoComplete(cpath);
	 }
    function onSubmit(option) {
	if(document.inputform.mr_no.value == ""){
	 showMessage("js.billing.newbill.entermrno");
	 document.inputform.mr_no.focus();
	 return false;
	}
	if (option == 'pdf') {
	if ( document.inputform.printType.value == 'text' )
		 document.inputform._method.value = 'getText';
		 else
		 document.inputform._method.value = 'getReport';
         document.inputform.target = "_blank";
	}else {
		document.inputform.target = "";
    }
	return validateFromToDate(document.inputform.fromDate, document.inputform.toDate);
}
</script>
<insta:js-bundle prefix="billing.newbill"/>
</head>
<html>
	<body onload="init();">
<c:set var="printtype">
<insta:ltext key="billing.patientstatement.print.pdf"/>,
<insta:ltext key="billing.patientstatement.print.text"/>
</c:set>
		<div class="pageHeader"><insta:ltext key="billing.patientstatement.print.patientstatement"/></div>
		<form name="inputform" method="GET" action="${cpath}/billing/PatientExpenseStatement.do"/>
			<input type="hidden" name="_method" value="getReport">
			<input type="hidden" name="detailed" value="DET"/>
		<div class="searchBasicOpts">
		   <div class="sboField">
			 <div class="sboFieldLabel"><insta:ltext key="billing.patientstatement.print.mrno.or.patientname"/>: </div>
			    <div class="sboFieldInput">
				   <div id="mrnoAutoComplete">
					<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
					<div id="mrnoContainer"></div>
				</div>
			</div>
		</div>
	</div>
		<jsp:include page="/pages/Common/DateRangeSelector.jsp">
			<jsp:param name="skipWeek" value="Y"/>
			</jsp:include>
			<table align="center" style="margin-top: 1em">
				<tr>
				   <td>
				      <insta:selectoptions name="printType" value="pdf" opvalues="pdf,text" optexts="${printtype}" style="width: 5em" />
			          <button type="submit" accesskey="P" onclick="return onSubmit('pdf')"><b><u><insta:ltext key="billing.patientstatement.print.p"/></u></b><insta:ltext key="billing.patientstatement.print.rint"/></button>
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
