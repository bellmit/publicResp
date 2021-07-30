<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.stores.DirectStockEntryDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>PH Dues Report-Insta HMS</title>

	<insta:link type="js" file="instaautocomplete.js" />
	<script>
	var jpatNames = <%= DirectStockEntryDAO.getPatientNames() %>;
	var jmrnos = <%= DirectStockEntryDAO.getMrnos() %>;


	function init(){
		setDateRangeYesterday(document.forms[0].fromDate, document.forms[0].toDate);
		Insta.initMRNoAcSearch(cpath, "mrno", "mrnoAcDropdown", 'all',
		function(type, args) {getMRNo});
	}

	function getMRNo() {
		var mrNo = YAHOO.Dom.Util.get()
	}
	function validate() {
		var valid = validateFromToDate(document.forms[0].fromDate, document.forms[0].toDate);
			if (!valid)
				return false;
			document.forms[0].submit();

	}
	</script>
</head>

<html>
	<body onload="init();" class="yui-skin-sam">
		<div class="pageHeader">Pharmacy Dues Report</div>
		<form name="inputform" method="GET" target="_blank"	>
		<input type="hidden" name="report" value="PharmacyDuesReport">
		<input type="hidden" name="method" value="getReport" />


			<div class="tipText">
				This report lists  (Sales & Returns ) of
<pre>
  1) All pending payments from Retail Credit customers.
  2) All pending payments from unpaid Bill Now
</pre>

			</div>

			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
				<jsp:param name="skipWeek" value="Y"/>
			</jsp:include></br>

			<table style="padding-left: 54ex;">
				<tr>
					<td valign="top">
						<table>
							<tr>
								<td style="padding-top: 9px;">MR No/Patient Name:</td>
								<td valign="top">
									<input type="text" id="mrno" name="mrno"
										style="width:135px; margin-top:4px; display: inline"/>
									<div id="mrnoAcDropdown" style="width: 34em"></div>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>
					<td>
						<button type="button" accesskey="G" onclick="return validate();"><b><u>G</u></b>enerate Report</button>
					</td>
			</table>

		</form>
	</body>
</html>

