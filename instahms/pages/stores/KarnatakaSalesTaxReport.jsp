<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ page import="java.util.*,java.text.*" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>
<c:set var="storesListJSON" value='<%= new JSONSerializer().exclude("class").serialize(ConversionUtils.listBeanToListMap(new GenericDAO("stores").listAll(null, "status", "A", null))) %>' scope="request"/>

<head>
	<title>Karnataka Tax Report - Insta HMS</title>
	<script>
		var fromDate, toDate;

		function onInit() {
			setSelDateRange();
			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
			filterStores(document.inputform.center_id.value)
		}

		function onSubmit(option) {
			document.inputform.format.value = option;
			document.inputform.method.value = "printReport";
			if (option == 'pdf')
				document.inputform.target = "_blank";
			else
				document.inputform.target = "";
			return validateFromToDate(fromDate, toDate);
		}

		function filterStores(centerId){
			var allStores = ${storesListJSON};
			var centerStores = (centerId == '' ?  allStores : filterList(allStores, "center_id", centerId));;
			loadSelectBox(document.inputform.store_id, centerStores, 'dept_name', 'dept_id','-- Select --', '');
			setSelectedIndex(document.inputform.store_id,0);
		}

	</script>
</head>

<%@page import="com.insta.hms.common.GenericDAO"%>
<%@page import="flexjson.JSONSerializer"%>
<%@page import="com.insta.hms.common.ConversionUtils"%>
<html>
<body onload="onInit()">
<div class="pageHeader">Karnataka Tax Report</div>
<form name="inputform" method="GET"
	action="${cpath}/stores/KarnatakaSalesTax.do"><input type="hidden"
	name="method" value="getScreen"> <input type="hidden"
	name="format" value="screen">

<div class="tipText">This report gives Sales-Tax Account Register.<br />
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
					<td valign="top">Center:</td>
					<td valign="top">
						<insta:selectdb name="center_id" table="hospital_center_master" valuecol="center_id"
							displaycol="center_name" orderby="center_id" dummyvalue="(All)" dummyvalueId=""
							onchange="filterStores(this.value);"/>
					</td>
					<td valign="top">Store:</td>
					<td style="padding-left: 2em; vertical-align: top">
						<select name="store_id" class="listbox" multiple="multiple"/>
					</td>
				</tr>
</table>

<table align="center" style="margin-top: 1em">
	<tr>
		<td><button type="submit" accesskey="P"
			onclick="return onSubmit('pdf')"><b><u>P</u></b>rint</button></td>
	</tr>
</table>

</form>
</body>
</html>


