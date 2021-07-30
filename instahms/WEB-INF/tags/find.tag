<%@tag pageEncoding="UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/esapi.tld" prefix="esapi" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@attribute name="form"      required="true" %>   <%-- name of form that contains the search fields --%>
<%@attribute name="closed"    required="true" %>   <%-- initially closed or open --%>
<%@attribute name="optionsId" required="true" %>   <%-- ID of element that needs to be collapsed/shown. --%>
<%@attribute name="validateFunction" required="false" %>
<%@attribute name="clearFunction" required="false" %>
<%-- screen level validate function name ex: validateSearchForm() (Note: no semicolon at the end of the function.)--%>

<%
String userid = (String) session.getAttribute("userid");
request.setAttribute("mysearches",
	com.insta.hms.search.SearchService.getMySearches((String) request.getAttribute("actionId")));
%>

<c:set var="homePageTabNumber" 
       value='<%=request.getParameter("_homePagetab") %>' />
      
<!-- closed is ${closed} -->
<div class="searchTitle">
	<div class="fltL" style="width: 85%" onclick="showMore('${optionsId}');">
		<div class="searchTitleContents" style="font-weight: bold; width: 75px;"><insta:ltext key="search.search"/></div>
		<div class="searchTitleContents searchTitleSeparator" style="width: 120px;">
			<a id="aMore">
				<c:choose>
				<c:when test="${closed}"><insta:ltext key="search.more.options"/></c:when>
				<c:otherwise><insta:ltext key="search.less.options"/></c:otherwise>
				</c:choose>
			</a>
		</div>
		<div id="_filters_active" class="searchTitleContents" style="width: 200px; display: none">
			<div class="searchFilterStatus">
				<img width="16" height="16" src="<%=request.getContextPath()%>/images/arrow_down.png">
			</div>
			<div class="fltLft"><insta:ltext key="search.search.filters.active"/></div>
		</div>
	</div>

	<div class="searchList">
		<select size="1" id="_mysearch" name="_mysearch" onchange="onSearchChange(this.value,
			document.forms.${form})">
			<option value="nosearch"><insta:ltext key="search.my.searches"/></option>
			<c:forEach var="search" items="${mysearches}">
				<option value="${search.map.search_id}">${ifn:cleanHtml(search.map.search_name)}</option>
			</c:forEach>
		</select>
	</div>
</div>

<table cellspacing="0">
	<tr>
		<td width="820" class="searchBody">
			<jsp:doBody/>
		</td>

		<td valign="top" style="background-color: #eaf2f8; border: 1px #e6e6e6 solid; padding: 10px"
				width="132" height="100%">
			<table style="height: 100%">
				<tr>
					<td valign="top" height="100%">
						<a id="_save_search" style="cursor:pointer; display: ${closed ? 'none' : 'block'}"
						onclick="showSaveInputs();"><insta:ltext key="search.save.search"/></a>
						<div id="_save_inputs" style="display: none">
							<input name="_search_name" id="_search_name" type="text" value=""
							style="width:100px; margin-bottom:5px;"/><br/>

							<input type="hidden" name="_actionId" value="${actionId}"/>
							<c:if test="${homePageTabNumber != null}">
								<input type="hidden" name="_homePagetab" value="${ifn:cleanHtmlAttribute(homePageTabNumber)}"/>
							</c:if>
							<input type="button" value="<insta:ltext key="search.save"/>"
							onclick="return validateSearchTagForm(event) && saveSearch(document.forms.${form});"/>

							<a href="#"
							onclick="document.getElementById('_search_name').value=''; return false;">
							<insta:ltext key="search.clear"/></a>
						</div>
					</td>
				</tr>
				<tr>
					<td valign="middle">
						<input type="submit" class="button" id="Search" value='<insta:ltext key="search.search"/>'/>
						<a href="#" onclick="${empty clearFunction ? 'clearForm' : clearFunction}(document.forms.${form});"><insta:ltext key="search.clear"/></a>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
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
		//var searchForm = document.forms.${form};
		//searchForm._method.value = searchForm._searchMethod.value;

		return true;
	}
</script>

