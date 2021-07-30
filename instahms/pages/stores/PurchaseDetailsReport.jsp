<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.StoreMaster.StoreMasterDAO"%>
<%@page import="com.insta.hms.stores.SupplierMasterDAO"%>
<%@page import="com.insta.hms.master.StoreTypeMaster.StoreTypeMasterDAO"%>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>Purchase Details - Insta HMS</title>

	<script>
	var centerId = ${centerId};
	var jStores = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_STORES_MASTER) %>;

	function csvReport() {
		document.inputform.action="${cpath}/exportReport.do";
		document.inputform.method.value = 'purchaseDetailsExportCSV';
		document.inputform.submit();
	}
	function pdfReport() {
		document.inputform.action="${cpath}/stores/PurchaseDetailsReport.do";
		document.inputform.method.value = 'getReport';
		document.inputform.submit();
	}

	function getstores()
	{
		var st_type_id = document.forms[0].store_type_id.value;
		var store_id = document.forms[0].store_id;
		if(st_type_id != '*') {
		store_id.length = 1;
			for(var i=0;i<jStores.length;i++) {
				if(jStores[i].STORE_TYPE_ID == st_type_id) {
					store_id.length = store_id.length+1;
					var len = store_id.length;
					store_id.options[len-1].value = jStores[i].DEPT_ID;
					store_id.options[len-1].text = jStores[i].DEPT_NAME;
				}
			}
		} else {
			store_id.length = 1;
			for(var i=0;i<jStores.length;i++) {
				store_id.length = store_id.length+1;
				len = store_id.length;
				store_id.options[len-1].value = jStores[i].DEPT_ID;
				store_id.options[len-1].text = jStores[i].DEPT_NAME;
			}
		}
	}

	function onSubmitPdf() {
		var valid = validateFromToDate(document.inputform.fromDate, document.inputform.toDate);
		if (!valid)
			return false;

		var centerDisplayName="";
		for(i =0; i<document.forms[0].centerFilter.length;i++){
			if(document.forms[0].centerFilter.options[i].selected){
				centerDisplayName = document.forms[0].centerFilter.options[i].text;
			}
		}
		document.getElementById("centerName").value = centerDisplayName;
		setCenterClausePdf();

		pdfReport();
	}

	function onSubmitCsv() {
		var valid = validateFromToDate(document.inputform.fromDate, document.inputform.toDate);
		if (!valid)
			return false;

		var centerDisplayName="";
		for(i =0; i<document.forms[0].centerFilter.length;i++){
			if(document.forms[0].centerFilter.options[i].selected){
				centerDisplayName = document.forms[0].centerFilter.options[i].text;
			}
		}
		document.getElementById("centerName").value = centerDisplayName;
		setCenterClauseCsv();

		csvReport();
	}

	function setCenterClauseCsv() {
		var selectedCenter = document.getElementById("centerFilter").value;
		if ( selectedCenter == 0 ) {
			document.getElementById("centerClause").value = selectedCenter;
		} else {
			document.getElementById("centerClause").value = selectedCenter;
		}
	}
	function setCenterClausePdf() {
		var selectedCenter = document.getElementById("centerFilter").value;
		if ( selectedCenter == 0 ) {
			document.getElementById("centerClause").value = ( " AND gd.center_id = "+selectedCenter );
		} else {
			document.getElementById("centerClause").value = ( " AND gd.center_id = "+selectedCenter );
		}
	}

	function setCenterStores() {
			var selectedCenter = document.getElementById("centerFilter").value;
			var store_id = document.forms[0]["store_id"];
			if(selectedCenter != 0) {
				store_id.length = 1;
				for(var i=0;i<jStores.length;i++) {
					if(jStores[i].CENTER_ID == selectedCenter) {
						store_id.length = store_id.length+1;
						var len = store_id.length;
						store_id.options[len-1].value = jStores[i].DEPT_ID;
						store_id.options[len-1].text = jStores[i].DEPT_NAME;
					}
				}
			} else {
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


	<body onload="setDateRangeYesterday(fromDate, toDate);">
		<div class="pageHeader">Store Purchase Details Report</div>
		<form name="inputform" method="GET" target="_blank"	action="PurchaseDetailsReport.do">
			<input type="hidden" name="report" value="PharmacyPurchaseDetails">
			<input type="hidden" name="method" value="getReport">
			<input type="hidden" name="centerName" id="centerName" value="" />
			<input type="hidden" name="center_id" id="center_id" value="${centerId }"/>
			<input type="hidden" name="centerClause" id="centerClause" value=""/>

			<div class="tipText">
				This report gives a summary list of all the purchases made between the given
				dates, and optionally for a given supplier. The report contains details of every item
				purchased, its cost, MRP, tax etc. as per the invoice.  The report is
				grouped by suppliers and invoices, with totals for every invoice.
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
				<c:choose>
					<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
					<tr><td></br></td></tr>
					<tr style="height: 1em;">
						<td>Center Name:</td>
						<td><insta:selectdb name="centerFilter" id="centerFilter" table="hospital_center_master"
									valuecol="center_id" displaycol="center_name" value="${centerId}" onchange="setCenterStores();"/>
						</td>
					</tr>
					</c:when>
					<c:otherwise>
						<tr><td></br></td></tr>
						<input type="hidden"  name="centerFilter" id="centerFilter"  value="${centerId}"/>
					</c:otherwise>
				</c:choose>
				<tr>
					<td colspan="2" style="padding-top: 4px"><br/>Select  supplier and store for the report</td>
				</tr>
				<tr>
					<td style="padding-left: 4px">Supplier Name:</td>
					<td>
						<select name="supplier_id" style="width:12em;">
							<option value="*">(All)</option>
							<c:forEach var="supplier" items="<%= SupplierMasterDAO.getSupplierNamesInMaster() %>">
								<option value="${supplier.SUPPLIER_CODE}">${supplier.SUPPLIER_NAME}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td><br/></td>
				</tr>
				<tr>
					<td style="padding-left: 4px">Store Type:</td>
					<td>
							<select name="store_type_id" style="width:12em;" onchange="getstores();">
									<option value='*'>(All)</option>
								<c:forEach var="s" items="<%= StoreTypeMasterDAO.getStoreTypeNamesAndIds() %>" >
									<option value="${s.store_type_id}">${s.store_type_name}</option>
								</c:forEach>
							</select>
					</td>
				</tr>
				<tr>
					<td><br/></td>
				</tr>
				<tr>
					<td style="padding-left: 4px">Store Name:</td>
					<td>
						<select name="store_id" style="width:12em;">
							<option value="*">(All)</option>
							<c:forEach var="store" items="<%= StoreMasterDAO.getStoresInMaster() %>">
								<option value="${store.DEPT_ID}">${store.DEPT_NAME}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
			</table>


			<table align="center" style="margin-top: 1em">
				<tr>
					<td><br/></td>
				</tr>
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>
					<td>
						<input type="submit" value="Generate Report" onclick="onSubmitPdf();">
					</td>
					<td>
						<input type="submit" value="Export to CSV" onclick="onSubmitCsv();">
					</td>
				</tr>
			</table>

		</form>
	</body>
</html>

