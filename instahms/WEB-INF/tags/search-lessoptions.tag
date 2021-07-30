<%@tag pageEncoding="UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@attribute name="form"      required="true" %>   <%-- name of form that contains the search fields --%>
<%@attribute name="validateFunction" required="false" %>
<%-- screen level validate function name ex: validateSearchForm() (Note: no semicolon at the end of the function.)--%>

<div class="searchTitle">
	<div style="float: left; width: 100%" >
		<div class="searchTitleContents" style="font-weight: bold; width: 75px;"><insta:ltext key="search.search"/></div>
	</div>
</div>
<c:set var="searchTitle">
<insta:ltext key="search.search"/>
</c:set>
<table style="margin-top: 0px;" cellspacing="0">
	<tr>
		<td rowspan="2" width="820" style="background-color:#f8fbfe; border: 1px #e6e6e6 solid; border-right: none; border-top: none;">
			<jsp:doBody/>
		</td>
		<td valign="top" style="background-color: #eaf2f8; border: 1px #e6e6e6 solid; padding: 10px"
				width="132" height="100%">
			<table style="height: 100%">
				<tr>
					<td valign="middle">
						<input type="submit" style="margin: 5px;" class="button" id="Search" value='<insta:ltext key="search.search"/>'
						onclick="${validateFunction} ${not empty validateFunction? '&&' :''} document.forms.${form}.submit()"/>
						<a href="#" onclick="clearForm(document.forms.${form});"><insta:ltext key="search.clear"/></a>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>

