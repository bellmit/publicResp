<%@tag pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<%@attribute name="searchUrl" required="true"%>
<%@attribute name="searchMethod" required="true"%>
<%@attribute name="fieldName" required="true" %>
<style>
	#psContainer .yui-ac-content{
		max-height:30em;overflow:auto;overflow-x:auto; /* scrolling */
	}

</style>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<form name="incomingpatientSearch" action="<c:out value='${searchUrl}'/>" method="GET" onsubmit="return checkEmpty();">
	<input type="hidden" name="_method" value="${searchMethod }"/>
	<table style="float: right; margin-top: 10px">
		<tr>
			<td title="Search Incoming Patient with visit_id, patient name.">MR No./Incoming Visit Id/Patient Name</td>
			<td>
				<div id="ipsAutocomplete" style="width: 100px; padding-bottom: 20px">
					<input type="text" name="${fieldName}" id="ips_${fieldName}" style="width: 100px"/>
					<div id="ipsContainer" style="right: 0px; width: 200px;" ></div>
				</div>
			</td>
			<td >
				<input type="submit" value="Find" name="find" onclick="return checkEmpty();">
			</td>
		</tr>
	</table>
</form>
<div style="clear: both"></div>
<script>

	var ipsAc = Insta.incomingPatientAutocomplete('${cpath}', 'ips_${fieldName}', 'ipsContainer');

	function checkEmpty() {
		if ( document.getElementById("ips_${fieldName}").value == '' ) {
			alert("Please enter search value");
			document.getElementById("ips_${fieldName}").focus();
			return false;
		  }

	}

</script>