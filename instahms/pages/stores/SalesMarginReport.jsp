<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.stores.DirectStockEntryDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Margin Report - Insta HMS</title>

	<script>
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


		var jmedNames = <%= DirectStockEntryDAO.getMedicineNames() %>;
		  /**
			*  this method contains itemNames AutoComplete
			*
			*/
		function initItemAutoComplete() {
			YAHOO.example.ACJSAddArray = new function() {
				var dataSource = new YAHOO.widget.DS_JSArray(jmedNames);
				oAutoComp = new YAHOO.widget.AutoComplete('item_name', 'med_dropdown', dataSource);
				oAutoComp.maxResultsDisplayed = 10;
				oAutoComp.allowBrowserAutocomplete = false;
				oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
				oAutoComp.typeAhead = false;
				oAutoComp.useShadow = false;
				oAutoComp.minQueryLength = 0;
				oAutoComp.forceSelection = true;
			}
		}

		function selectFilter() {
			if (document.forms[0].filter.value == 'M') {
				document.getElementById('med_wrapper').style.display = 'block';
				document.getElementById('storeDiv').style.display = 'none';
			}else{
				document.getElementById('med_wrapper').style.display = 'none';
				document.getElementById('storeDiv').style.display = 'block';
			}
		}

	</script>
</head>

<html>
	<body onload="onInit()" class="yui-skin-sam">
		<div class="pageHeader">Sales Margin Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="method" value="getReport">

			<div class="tipText">
			</div>

			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
				<jsp:param name="skipWeek" value="Y"/>
			</jsp:include>

			<table style="margin-top: 1em" width="40%" align="center">
<%-- 				<tr>
					<td style="padding-left: 4px" align="center">Group By:</td>
					<td><insta:selectoptions name="groupBy" value="*" optexts="..Select..,Item Name, Store" opvalues="*,M,S" onchange="setGroupBy();"/></td>
				</tr>
--%>
				<tr>
					<td style="padding-left: 4px" align="center">Filter By Item:</td>
					<td valign="top" style="width: 18em">
						<div id="med_wrapper" style="width: 18em; padding-bottom:0.2em;">
							<input type="text" name="item_name" id="item_name" style="width: 18em"  maxlength="100" tabindex="1"/>
							<div id="med_dropdown"></div>
						</div>
				    </td>
				</tr>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr style="height: 3em">
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>
					<td>
						<button type="button" accesskey="G" onclick="onSubmit('getReport')"><b><u>G</u></b>enerate Report</button>
					</td>
					<td>
						<button type="button" accesskey="E" onclick="onSubmit('getCsv')"><b><u>E</u></b>xport to CSV</button>
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>

