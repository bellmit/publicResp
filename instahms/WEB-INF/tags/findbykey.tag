<%@tag pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<%@attribute name="keys" required="true" %><!--keys are DB column names(comma seperated values) first one is for name which
												we want to display on the autoComplete and second one is id for to fetch the result -->
<%@attribute name="url" required="true" %>
<%@attribute name="method" required="true" %>
<%@attribute name="fieldName" required="true"%><!-- fieldName should be the same name which we are using in the action class to fetch the Id -->
<%@attribute name="extraParamKeys" required="false"%><!-- We can pass extraParams if action class requires any extra parameters. -->
<%@attribute name="extraParamValues" required="false"%><!-- Here we can pass values to that extra parameters. values must be in sameOrder with respect to Keys -->

<form name="searchKeyForm" action="<c:out value='${url}' />"  >
	<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}" />
	<input type="hidden" name="${fieldName}" id="${fieldName}" value=""/>
	<c:set var="params" value="${fn:split(extraParamKeys, ',')}"/>
	<c:set var="paramVal" value="${fn:split(extraParamValues, ',')}"/>
		<c:forEach var="param" items="${params}" varStatus="st">
			<input type="hidden" name="${params[st.index]}" value="${paramVal[st.index]}" />
		</c:forEach>
	<table style="float:right; padding-top:8px">
		<tr>
			<td>Enter Name:</td>
			<td>
				<div id="autocomplete" style="width: 100px; padding-bottom: 20px">
					<input type="text" name="search_${fieldName}" id="search_${fieldName}"  >
					<div id="search_${fieldName}_container" style="right: 0px; width: 200px"></div>
				</div>
			</td>
			<td><input type="submit" name="submit" value="Get Details" onclick="return checkEmpty();"></td>
		</tr>
	</table>

</form>
<div style="clear: both"></div>

<script>
	function checkEmpty() {
		var id = document.getElementById('search_${fieldName}');
		id.blur();
		if (id.value == '') {
			alert("Please enter the search value");
			id.focus();
			return false;
		}
		return true;
	}
	var keys = '${keys}';
	parameters = keys.split(",");
	var hidden_id = '${fieldName}';

	Insta.mastersAutocomplete('search_${fieldName}', 'search_${fieldName}_container');

</script>
