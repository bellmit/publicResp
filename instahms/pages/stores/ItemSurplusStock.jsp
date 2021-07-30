<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page isELIgnored="false"%>
<%@page import="com.insta.hms.master.StoreMaster.StoreMasterDAO"%>
<%
response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-store");
response.setHeader("Expires", "0");
%>

<c:set var = "contextPath" value="${pageContext.request.contextPath}"/>

<head>
	<title>Surplus Stock Report-Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="hmsvalidation.js"/>
<script type="text/javascript">
			var fromDate, toDate,category ;

		function onInit() {
			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
			document.getElementById("pd").checked = true;
			setDateRangeYesterday(fromDate, toDate);
			document.getElementById("noMoving").checked =true;
		}
		function validateCategory(){
			var form = document.forms[0];
			document.forms[0].method.value = 'getReport';

			if (document.getElementById("slowMoving").checked || document.getElementById("fastMoving").checked){
				if(document.inputform.qty.value == ""){
					alert("Select the quantity");
					document.inputform.qty.value=0;
					document.inputform.qty.focus();
					return false;
				}
			}
			if (validateFromToDate(document.inputform.fromDate,document.inputform.toDate)){
				if(document.getElementById("noMoving").checked){
					document.inputform.report_name.value = "ItemSurplusStockReport";
				}else if(document.getElementById("slowMoving").checked){
					document.inputform.report_name.value = "SlowMovingItemReport";
					document.getElementById("qtyDiv").style.display="block"
				}else if (document.getElementById("fastMoving").checked){
					document.inputform.report_name.value = "FastMovingItemReport";
					document.getElementById("qtyDiv").style.display='block';
				}else if(document.getElementById("avg").checked){
					document.inputform.report_name.value = "ItemAvgConsumption";
				}
				document.forms[0].quantity.value = parseInt(document.forms[0].qty.value);
				document.forms[0].diffdays.value = daysDiff(parseDateStr(fromDate.value),parseDateStr(toDate.value))+1;
				return true;
			}else{
				return false;
			}
		}

		function showHideDiv(){
				if(document.getElementById("noMoving").checked){
					document.getElementById("qtyDiv").style.display='none';
				}else if(document.getElementById("slowMoving").checked){
					document.getElementById("qtyDiv").style.display='block';
					document.getElementById("labelName").innerHTML="Sale Qty less than :";
					document.inputform.qty.value=0;
				}else if (document.getElementById("fastMoving").checked){
					document.getElementById("qtyDiv").style.display='block';
					document.getElementById("labelName").innerHTML="Sale Qty more than :";
					document.inputform.qty.value=0;
				}else if(document.getElementById("avg").checked){
					document.getElementById("qtyDiv").style.display='none';
				}

		}

		function exportCSV(){

		if (document.getElementById("slowMoving").checked || document.getElementById("fastMoving").checked){
				if(document.inputform.qty.value == ""){
					alert("Select the quantity");
					document.inputform.qty.value=0;
					document.inputform.qty.focus();
					return false;
				}
			}
			document.forms[0].diffdays.value = daysDiff(parseDateStr(fromDate.value),parseDateStr(toDate.value))+1;
			document.forms[0].method.value = 'getCsv';
			document.forms[0].submit();
			return true;
		}

	</script>
</head>

<html>
	<body onload="onInit()">
		<div class="pageHeader">Surplus Stock Report</div>
		<form name="inputform"  target="_blank"	method="GET" >
		<input type="hidden" name="report_name">
		<input type="hidden" name="method" value="getReport" />
		<input type="hidden" name="quantity">
		<input type="hidden" name="diffdays">

		<div class="tipText">
				This report shows the surplus stock within given dates.
				The report will display Item Name,Qty and value of stock in terms of cost price and mrp.
		</div>

		<table align="center" >
			<tr>
				<td>
					<input type="radio" name="fsn" id="noMoving" value="n" onclick="showHideDiv();">NonMoving
				</td>

				<td>
					<input type="radio" name="fsn" id="slowMoving" value="s" onclick="showHideDiv();">SlowMoving
				</td>
				<td>
					<input type="radio" name="fsn" id="fastMoving" value="f" onclick="showHideDiv();">FastMoving
				</td>
				<td>
					<input type="radio" name="fsn" id="avg" value="a" onclick="showHideDiv();">Monthly Average
				</td>

			</tr>
			<tr>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td colspan="4" align="left">
					<jsp:include page="/pages/Common/DateRangeSelector.jsp">
						<jsp:param name="skipWeek" value="Y"/>
					</jsp:include>
				</td>
			</tr>
			</table>
			<table align="center">
			<tr>
				<td colspan="3">
					<div id="qtyDiv" style="display: none;">
						<table>
							<tr>
							<td>
								<label id="labelName"> </label>
							</td>
							<td>
								<input type="text" name="qty" id="qty" size="5" class="number" onkeypress="return enterNumOnlyzeroToNine(event)" >
							</td>
							</tr>
						</table>
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<div>Store Name:
						<select name="store_id" style="width:12em;">
								<c:forEach var="store" items="<%= StoreMasterDAO.getStoresInMaster() %>">
									<option value="${store.DEPT_ID}">${store.DEPT_NAME}</option>
								</c:forEach>
						</select>
					</div>
				</td>
			</tr>
		</table>
		<table align="center" style="margin-top: 1em;">
			<tr>
				<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>
				<td>
					<input type="submit" value="Generate Report"
						onclick="return validateCategory()">
				</td>
				<td>
					<input type="button" value="Export CSV"
						onclick="return exportCSV();">
				</td>
			</tr>

		</table>
		</form>
	</body>
</html>
