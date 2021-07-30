<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.stores.DirectStockEntryDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Bonus Items Report - Insta HMS</title>

	<script>
		var jmedNames = <%= DirectStockEntryDAO.getMedicineNames() %>;
		  /**
			*  this method contains itemNames AutoComplete
			*
			*/
			function initItemAutoComplete() {
				YAHOO.example.ACJSAddArray = new function() {
					var dataSource = new YAHOO.widget.DS_JSArray(jmedNames);
					oAutoComp = new YAHOO.widget.AutoComplete('medName', 'med_dropdown', dataSource);
					oAutoComp.maxResultsDisplayed = 10;
					oAutoComp.allowBrowserAutocomplete = false;
					oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
					oAutoComp.typeAhead = false;
					oAutoComp.useShadow = false;
					oAutoComp.minQueryLength = 0;
					oAutoComp.forceSelection = true;
				}

			}
		function onInit() {
			document.getElementById('pd').checked = true;
			setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
			initItemAutoComplete();
		}

		function onSubmit(method) {
			var valid = validateFromToDate(document.inputform.fromDate, document.inputform.toDate);
			if (!valid)
				return false;

			document.inputform.method.value = method;
			document.inputform.submit();
		}
	</script>
</head>

<html>
	<body onload="onInit();" class="yui-skin-sam">
		<div class="pageHeader">Bonus Items Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="method" value="getReport">

			<div class="tipText">
			</div>

			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
				<jsp:param name="skipWeek" value="Y"/>
			</jsp:include>
			<table align="center" style="margin-top: 1em">

				<tr>
					<td colspan="2" style="padding-top: 8px">Select a filters for the report</td>
				</tr>
				<tr>
					<td style="padding-left: 4px">Supplier Name:</td>
					<td><insta:selectdb name="supplier_id" table="supplier_master" valuecol="supplier_code"
									displaycol="supplier_name" dummyvalue="(All)"></insta:selectdb>	</td>
				</tr>
				<tr>
					<td style="padding-left: 4px">Item Name:</td>
					<td valign="top"><div id="med_wrapper" style="width: 18em; padding-bottom:0.2em; ">
							     <input type="text" name="medName" id="medName" style="width: 18em"  maxlength="100" tabindex="1"/><div id="med_dropdown"></div></div>
				    </td>
				</tr>
				<tr style="height: 3em;" valign="bottom">

					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>
					<td>
						<button type="button" accesskey="G" onclick="onSubmit('getReport')"><b><u>G</u></b>enerate Report</button>
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>

