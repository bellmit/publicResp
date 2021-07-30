<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.STATE_MASTER_PATH %>"/>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>State - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

	<script>
		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?_method=list?sortOrder=state_name&sortReverse=false&status=A";
		}
		
		function restrictInactiveState() {
			var stateId = document.forms[0].state_id.value;
			var stateName =  document.forms[0].state_name.value;
			var status = document.forms[0].status.value;
			var cityList = ${ifn:convertListToJson(citiesList)};
			
			for (var i=0;i<cityList.length;i++) {
				if(status == 'I'){
					if(cityList[i].state_id == stateId && cityList[i].status == 'A') {
						alert("Active cities are mapped with this state "+stateName+". Hence, it can not be marked as Inactive.");
							return false;
					}
				}
			}
			return true;
		}
	</script>

</head>
<body>
<c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
<form action="${actionUrl}" method="POST">
	<input type="hidden" name="_method" value="create">
	
	<h1>Add State</h1>

	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Country:</td>
				<td>
					 <insta:selectdb name="country_id" value="${bean.country_id}" table="country_master" valuecol="country_id" displaycol="country_name" 
					 	filtercol="status,nationality" filtervalue="A,f" orderby="country_name" />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">State:</td>
				<td>
					<input type="text" name="state_name" value="${bean.state_name}" onblur="capWords(state_name)" class="required validate-length" length="70" title="Name is required and max length of name can be 70" />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>

		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" onclick="return restrictInactiveState()"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">State List</a></td>
		</tr>
	</table>

</form>

</body>
</html>
