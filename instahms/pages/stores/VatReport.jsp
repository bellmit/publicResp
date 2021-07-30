<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.StoreMaster.StoreMasterDAO"%>
<%@page import="com.insta.hms.master.StoreTypeMaster.StoreTypeMasterDAO"%>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="taxLabel" value='<%=GenericPreferencesDAO.getAllPrefs().get("procurement_tax_label")%>'/>

<head>
<title>Store Items Tax Report - Insta HMS</title>
<script>
	var centerId = ${centerId};
	var jStores = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_STORES_MASTER) %>;

	function getstores() {
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

	function onSubmit() {
		var valid = validateFromToDate(document.forms[0].fromDate,document.forms[0].toDate);
		if (!valid)
			return false;

		var centerDisplayName="";
		for(i =0; i<document.forms[0].centerFilter.length;i++){
			if(document.forms[0].centerFilter.options[i].selected){
				centerDisplayName = document.forms[0].centerFilter.options[i].text;
			}
		}
		document.getElementById("centerName").value = centerDisplayName;
		setCenterClause();
	}

	function setCenterClause() {
		var selectedCenter = document.getElementById("centerFilter").value;
		if ( selectedCenter == 0 ) {
			document.getElementById("centerClause").value = ( " AND s.center_id = "+selectedCenter );
		} else {
			document.getElementById("centerClause").value = ( " AND s.center_id = "+selectedCenter );
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

<html>
	<body onload="setDateRangeYesterday(document.forms[0].fromDate, document.forms[0].toDate);">
<%-- 		<c:if test="${taxLabel eq 'V'}"> --%>
<!-- 		<div class="pageHeader">Sale Items VAT Report</div> -->
<%-- 		</c:if> --%>
<%-- 		<c:if test="${taxLabel eq 'G'}"> --%>
<!-- 		<div class="pageHeader">Sale Items GST Report</div> -->
<%-- 		</c:if> --%>
		<div class="pageHeader">Sale Items Tax Report</div>
		<form name="inputform" method="GET" target="_blank"	action="TaxDetailsReport.do">
			<input type="hidden" name="report" value="vatreport">
			<input type="hidden" name="method" value="getReport" />
			<input type="hidden" name="centerName" id="centerName" value="" />
			<input type="hidden" name="center_id" id="center_id" value="${centerId }"/>
			<input type="hidden" name="centerClause" id="centerClause" value=""/>

			<div class="tipText">
				This report lists tax wise sales in the given time period and grouped by stores.
				<br>Note that bill level round-offs are excluded from the bill amount shown in the report.
			This report shows how much amount has been added to revenue on a date basis.\
			<br/> <br/><font color='red'> NOTE: We would recommend that you use the Sales Items Report Builder in Report Type - 
			<b>Tabular Summary where Horizontal Axis = (Summary Fields), Vertical Axis = Type and Vertical Sub Axis = Store Name or Tax % </b>
			 This report is now not being maintained and would be removed soon. Do contact us at insta-support@practo.com
			  if you need support or any more details.</font>
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
					<td colspan="2" style="padding-top: 4px">Select a Store for the report</td>
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
						<button type="submit" accesskey="G"
							onclick="onSubmit();">
							<b><u>G</u></b>enerate Report</button>
					</td>
			</table>

		</form>
	</body>
</html>

