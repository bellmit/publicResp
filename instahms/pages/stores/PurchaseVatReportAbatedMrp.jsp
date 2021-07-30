<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.StoreMaster.StoreMasterDAO"%>
<%@page import="com.insta.hms.stores.SupplierMasterDAO"%>
<%@page import="com.insta.hms.master.StoreTypeMaster.StoreTypeMasterDAO"%>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Purchase Tax Report-Abated MRP - Insta HMS</title>

	<script type="text/javascript">
		var jStores = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_STORES_MASTER) %>;
		function setReport(){
			document.inputform.report_name.value = document.inputform.reportname.value;
		}

		function getstores()
		{
			var st_type_id = document.forms[0]["store_type_id_Array@int"].value;
			var store_id = document.forms[0]["store_id_Array@int"];
			if(st_type_id != '') {
			store_id.length = 1;
			for(var i=0;i<jStores.length;i++) {
				if(jStores[i].STORE_TYPE_ID == st_type_id) {
					store_id.length = store_id.length+1;
					var len = store_id.length;
					store_id.options[len-1].value = jStores[i].DEPT_ID;
					store_id.options[len-1].text = jStores[i].DEPT_NAME;
				}
			}
		}
		else {
				store_id.length = 1;
				for(var i=0;i<jStores.length;i++) {
					store_id.length = store_id.length+1;
					len = store_id.length;
					store_id.options[len-1].value = jStores[i].DEPT_ID;
					store_id.options[len-1].text = jStores[i].DEPT_NAME;
				}
		}

		}
	</script>
</head>

<html>
	<body onload="setDateRangeYesterday(fromDate, toDate);">
		<div class="pageHeader">Purchase Tax Report-Abated MRP</div>
		<form name="inputform" method="GET" target="_blank" >
			<input type="hidden" name="report_name" value="purchasevatsummaryreport">
			<input type="hidden" name="method" value="getReport">
			<div class="tipText">
				This report lists tax wise purchases in the given time period.

			</div>

			<table align="center">
				<tr>
					<td colspan="2">
						<br/>
						<jsp:include page="/pages/Common/DateRangeSelector.jsp">
							<jsp:param name="addTable" value="N" />
							<jsp:param name="skipWeek" value="Y" />
						</jsp:include>
					</td>
				</tr>
				<tr>
					<td style="padding-left: 4px">Store Type:</td>
					<td>
							<select name="store_type_id_Array@int" style="width:12em;" onchange="getstores();">
									<option value="">(All)</option>
								<c:forEach var="s" items="<%= StoreTypeMasterDAO.getStoreTypeNamesAndIds() %>" >
									<option value="${s.store_type_id}">${s.store_type_name}</option>
								</c:forEach>
							</select>
					</td>
					<td style="padding-left: 4px">Store Name:</td>
					<td>
						<select name="store_id_Array@int" style="width:12em;">
							<option value="">(All)</option>
							<c:forEach var="store" items="<%= StoreMasterDAO.getStoresInMaster() %>">
								<option value="${store.DEPT_ID}">${store.DEPT_NAME}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td colspan="2" align="center"><br/>Group by:
					<insta:selectoptions name="reportname" value="purchasevatsummaryreport"
							opvalues="purchasevatsummaryreport,purchasevatreportvattype,purchasevatreport"
							optexts="--Select--,Tax Type,Supplier" onchange="setReport();"/>
					</td>
				</tr>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>
					<td>
						<button type="submit" accesskey="G"
							onclick="return validateFromToDate(document.inputform.fromDate, document.inputform.toDate)">
							<b><u>G</u></b>enerate Report</button>
					</td>
					</tr>
			</table>

		</form>
	</body>
</html>
