<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@page import="com.insta.hms.master.StoreMaster.StoreMasterDAO"%>
<%@page import="com.insta.hms.master.StoreTypeMaster.StoreTypeMasterDAO"%>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>


<head>
<title>TN Tax Report - Insta HMS</title>



	<script>
	function init () {
		setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
		document.getElementById('pd').checked = true;
	}
	var jStores = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_STORES_MASTER) %>;
	function getstores()
		{
			var st_type_id = document.forms[0].store_type_id.value;
			var store_id = document.forms[0].store_id;
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
	<body onload="init();" class="yui-skin-sam">
		<div class="pageHeader">TN Tax Report</div>
		<form name="inputform" method="GET" target="_blank" >
			<input type="hidden" name="method" value="exportToXls" />

			<div class="tipText">
				This report lists the Purchase and Sales Tax in the given time period.
			</div></br>
			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
				<jsp:param name="skipWeek" value="Y"/>
			</jsp:include></br>
			<table>
				<tr>
					<td style="padding-left: 4px">Store Type:</td>
					<td>
							<select name="store_type_id" style="width:12em;" onchange="getstores();">
									<option value=''>...Select...</option>
								<c:forEach var="s" items="<%= StoreTypeMasterDAO.getStoreTypeNamesAndIds() %>" >
									<option value="${s.store_type_id}">${s.store_type_name}</option>
								</c:forEach>
							</select>
					</td>
				</tr>
				<tr>
					<td style="padding-left: 4px">Store Name:</td>
					<td>
						<select name="store_id" style="width:12em;">
							<option value=''>...select...</option>
							<c:forEach var="store" items="<%= StoreMasterDAO.getStoresInMaster() %>">
								<option value="${store.DEPT_ID}">${store.DEPT_NAME}</option>
							</c:forEach>
						</select>
					</td>
				</tr>

			</table>

			<div class="screenActions">
				<input type="submit" value="Export to CSV" >
			</div>
			<dl>
        	<dt style="padding-left: 2.5em;padding-bottom: 0.5em;font-weight: bold;" > Commodity Code.</dt>
        		<dd>* 4% - 2044</dd>
        		<dd>* 5% - 2044</dd>
        		<dd>* 12.5% - 301</dd>
        		<dd>* 14% - 301</dd>
        		<dd>* 14.50% - 301</dd>
                <dd>* 0% - 752</dd>
                <dd>* others - 0</dd>
         </dl>

		</form>
	</body>
</html>

