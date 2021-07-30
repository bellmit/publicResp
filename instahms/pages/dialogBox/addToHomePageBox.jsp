<%@ page contentType='text/html' isELIgnored='false'%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/fmt' prefix='fmt' %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/functions' prefix='fn' %>
<%@ taglib tagdir='/WEB-INF/tags' prefix='insta' %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<head>
    <insta:link type='css' file='homePage/dialogBox.css' />
    <%
    	String userid = (String) session.getAttribute("userid");
            request.setAttribute("mysearches",com.insta.hms.search.SearchDAO.getUserSavedSearch(userid));
            request.setAttribute("myFavReports",com.insta.hms.common.FavouriteReportDAO.getUserFavReports(userid));
            request.setAttribute("tab_list",com.insta.hms.addtohomepagemaster.AddToHomePageMasterDAO
                                                         .getAllHomePageTabs((String) session.getAttribute("userid")));
    %>  
</head>

<!-- Add to home Page Dialog Box -->
<div id="add-to-home-page-dialog">
    <div class="bd dialog-style">
        <fieldset class="fieldSetBorder fieldSetBorder-style" name="userRoleId" >
            <legend class="fieldSetLabel">Add Worklist</legend>
            	<c:choose>
	                <c:when test="${mysearches.size() > 0 }"> 
	                    <div class="default-list"> 
	                        My Saved Search
	                    </div>
	                    <select size='5' class="select-box-style">
	                        <c:forEach var="search" items="${mysearches}">
	                            <option onclick="selAddToHomePageSavedSearch(this)" value="${search.map.search_id}"
	                                    title='${ifn:cleanHtmlAttribute(search.map.search_name)}'
	                                    onmouseenter="addToHomePageHover(this)"
	                                    onmouseleave="addToHomePageNormal(this)" >
	                                        ${ifn:cleanHtmlAttribute(search.map.search_name)}
	                            </option>
	                        </c:forEach>
	                    </select>
	                    <hr>
	                    <div class="default-list"> 
	                    	Default Lists
	                	</div>
	                	<select id="add-to-home-page-work-list-content" 
	                      		name="addtoHomePageWorkListContent" size='6' 
	                            class="select-box-style">
	                	</select>
	                </c:when> 
	                <c:otherwise>
		                <div class="default-list"> 
		                    Default Lists
		                </div>
		                <select id="add-to-home-page-work-list-content" 
		                        name="addtoHomePageWorkListContent" size='14' 
		                        class="select-box-style">
		                </select>
		            </c:otherwise>
                </c:choose>
        </fieldset>
        
        <div class="add-to-home-page-divider">
            OR
        </div>
        
        <fieldset class="fieldSetBorder fieldSetBorder-style" name="userRoleId">
            <legend class="fieldSetLabel">Add Reports</legend>
                <div class="default-list"> My Favourite List</div>
                <select size='14' class="select-box-style">
                    <c:forEach var="reports" items="${myFavReports}">
                        <option onclick="selAddToHomePageFavReport(this)" value="${reports.map.report_id}"
                                title = '${reports.map.report_title}'
                                class="select-box-option"
                                onmouseenter="addToHomePageHover(this)"
                                onmouseleave="addToHomePageNormal(this)" >
                                    ${reports.map.report_title}
                        </option>
                    </c:forEach>
                </select>
        </fieldset>
        
        <table>
            <tbody>
                <tr>
                    <td class="selected-value">Selected List:
                        <span id="sel-add-to-home-page"></span>
                        <span id="value-option"></span>
                        <span id="type-option"></span>
                    </td>
                </tr>
            </tbody>
        </table>
        <table>
            <tbody>
                <tr>
                    <td class="dialog-button-position">
                        <button type="button"
                                value="Save" 
                                onclick="addTofavScreen(lastSelAddToHomePage,lastSelAddToHomePageOptionValue,lastSelAddToHomePageTypeValue);" 
                                disabled
                                id="save-add-to-home-page">
                                <b>Save</b>
                        </button>
                    </td>
                    <td class="dialog-button-position">
                        <button type="button" id="close-add-to-home-page-dialog" value="Cancel">
                            <b>Cancel</b>
                        </button>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</div>
<!-- Add to home Page Dialog Box ends-->