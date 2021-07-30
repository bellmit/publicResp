<%@tag pageEncoding="UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@attribute name="form"      required="true" %>   <%-- name of form that contains the search fields --%>
<%@attribute name="closed"    required="true" %>   <%-- initially closed or open --%>
<%@attribute name="optionsId" required="true" %>   <%-- ID of element that needs to be collapsed/shown. --%>
<%@attribute name="widthInPercent" required="false"%>
<%@attribute name="validateFunction" required="false" %>
<%@attribute name="clearFunction" required="false" %>
<%-- screen level validate function name ex: validateSearchForm() (Note: no semicolon at the end of the function.)--%>

	<div style="width: 100%; background-color: #f8fbfe">
		<div class="searchBody" style="clear: both; width: ${empty widthInPercent ? '820' : widthInPercent}${empty widthInPercent ? 'px' : '%'}">
			<jsp:doBody/>
		</div>
	</div>
<script>
	if (${not empty validateFunction}) {
		YAHOO.util.Event.addListener(document.forms.${form}, "submit", validateSearchTagForm);
	}
	function validateSearchTagForm(event) {
		if (!${empty validateFunction ? true : validateFunction}) {
			YAHOO.util.Event.stopEvent(event);
			return false;
		}
		// 1) onchange of saved favourite search it submits the page with setting the method to getMySearch and action
		// 		as SearchAction.do. it will returns result list with the selected favourite search criteria.
		// 2) now clinck on the browser back button it will loads the previous page. with setting the methos as
		// 		as getMySearch only. now if user clicks on search button it will give error because getMySearch method
		//		will not be there in the corresponging action.
		// since _method and _searchMethod contains the same values. set the _searchMethod value to the _method parameter
		// so that it will forward to the correct method. refer BUG : 23040
		var searchForm = document.forms.${form};
		searchForm._method.value = searchForm._searchMethod.value;

		return true;
	}
</script>
