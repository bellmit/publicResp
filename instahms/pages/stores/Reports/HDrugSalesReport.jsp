<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.StoreMaster.StoreMasterDAO"%>
<%@page import="com.insta.hms.master.StoreTypeMaster.StoreTypeMasterDAO"%>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Controlled Drugs Sales Report - Insta HMS</title>

	<script>
	var centerId = ${centerId};
	var jStores = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_STORES_MASTER) %>;
		function onInit() {
			document.getElementById('pd').checked = true;
			setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
		}

		function onSubmit(method) {

			var valid = validateFromToDate(document.inputform.fromDate, document.inputform.toDate);
			if (!valid)
				return false;

			var dateRange = daysDiff( getDate("fromDate") , getDate("toDate") );
			if ( dateRange > 31 ) {
				alert("Date range should be less than 31 days");
				return false;
			}
			var centerDisplayName="";
			for(i =0; i<document.forms[0].centerFilter.length;i++){
				if(document.forms[0].centerFilter.options[i].selected){
					centerDisplayName = document.forms[0].centerFilter.options[i].text;
				}
			}
			document.getElementById("centerName").value = centerDisplayName;
			setCenterClause();

			document.inputform.method.value = method;
			document.inputform.submit();
		}

		function setCenterClause() {
			var selectedCenter = document.getElementById("centerFilter").value;
			if ( selectedCenter == 0 ) {
				document.getElementById("centerClause").value = ( " AND gd.center_id = "+selectedCenter );
			} else {
				document.getElementById("centerClause").value = ( " AND gd.center_id = "+selectedCenter );
			}
		}

		function getstores()
		{
			var st_type_id = document.forms[0].store_type_id.value;
			var store_id = document.forms[0]["store_idArray@int"];
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

		function setCenterStores() {
			var selectedCenter = document.getElementById("centerFilter").value;
			var store_id = document.forms[0]["store_idArray@int"];
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
	<body onload="onInit()">
		<div class="pageHeader">Controlled Drugs Sales Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="method" value="getReport">
			<input type="hidden" name="centerName" id="centerName" value="" />
			<input type="hidden" name="center_id" id="center_id" value="${centerId }"/>
			<input type="hidden" name="centerClause" id="centerClause" value=""/>

			<div class="tipText">

			</div>

			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
				<jsp:param name="skipWeek" value="Y"/>
				<jsp:param name="skipYear" value="Y"/>
			</jsp:include>

			<table align="center">
			<tr style="height: 1em">
				<td>Control Type:</td>
				<td><insta:selectdb  name="control_type_id" id="control_type_id" value="" table="store_item_controltype" valuecol="control_type_id"
							 		displaycol="control_type_name" filtered="false" />
			    </td>
			</tr>
			<c:choose>
				<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
				<tr style="height: 1em">
					<td>Center Name:</td>
					<td><insta:selectdb name="centerFilter" id="centerFilter" table="hospital_center_master"
								valuecol="center_id" displaycol="center_name" value="${centerId}" onchange="setCenterStores();" />
					</td>
				</tr>
				</c:when>
				<c:otherwise>
						<input type="hidden"  name="centerFilter" id="centerFilter"  value="${centerId}"/>
				</c:otherwise>
			</c:choose>
			<tr style="height: 1em">
			    <td></td>
			</tr>
			<tr>
				<td>Store Type:</td>
				<td>
					<select name="store_type_id" style="width:12em;" onchange="getstores();">
							<option value=''>...Select...</option>
						<c:forEach var="s" items="<%= StoreTypeMasterDAO.getStoreTypeNamesAndIds() %>" >
							<option value="${s.store_type_id}">${s.store_type_name}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
			<tr style="height: 1em">
			   	<td>Store Name:</td>
				<td>
					<select name="store_idArray@int" style="width:12em;">
						<option value=''>...Select...</option>
						<c:forEach var="store" items="<%= StoreMasterDAO.getStoresInMaster()%>" >
							<option value="${store.DEPT_ID}">${store.DEPT_NAME}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
				</table>

			<table align="center" style="margin-top: 1em">

				<tr style="height: 3em">
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>
					<td>
						<button type="button" accesskey="G" onclick="onSubmit('getReport')">
						<b><u>G</u></b>enerate Report</button>
					</td>

				</tr>
			</table>

		</form>
	</body>
</html>

