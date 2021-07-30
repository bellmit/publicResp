<%@ page contentType='text/html' isELIgnored='false'%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/fmt' prefix='fmt' %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/functions' prefix='fn' %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir='/WEB-INF/tags' prefix='insta' %>
<%@page import="com.insta.hms.common.UrlUtil"%>
<%@page import="java.util.List" %>
<%@page import="org.apache.commons.beanutils.BasicDynaBean" %>
<%@page import="com.insta.hms.common.Encoder" %>

<head>
    <insta:link type='css' file='toolTip.css' />
    <insta:link type='css' file='homePage/Home.css' />
    <c:set var='cpath' value='${pageContext.request.contextPath}' scope='request' />
    <%
    	List<BasicDynaBean> homePageTabList = com.insta.hms.addtohomepagemaster.AddToHomePageMasterDAO
    	        .getAllHomePageTabs((String) session.getAttribute("userid"));
    	

            for(int i=0; i<homePageTabList.size();i++) {
                homePageTabList.get(i).set("action_id",UrlUtil.buildURL((String)homePageTabList.get(i).get("action_id"), null, (String)homePageTabList.get(i).get("query_params"), null, null));
    	    }
            request.setAttribute("homePageTablist",homePageTabList);
            request.setAttribute("savedSearchList",com.insta.hms.search.SearchDAO
                                                      .getUserSavedSearch((String) session.getAttribute("userid")));
            request.setAttribute("favReportsList",com.insta.hms.common.FavouriteReportDAO
                                                      .getUserFavReports((String) session.getAttribute("userid")));
    %>
    <script>
        tabListHeader.push(['${homePageTablist[0].map.search_id}','${ifn:cleanJavaScript(homePageTablist[0].map.screen_name)}']);
        tabListHeader.push(['${homePageTablist[1].map.search_id}','${ifn:cleanJavaScript(homePageTablist[1].map.screen_name)}']);
        tabListHeader.push(['${homePageTablist[2].map.search_id}','${ifn:cleanJavaScript(homePageTablist[2].map.screen_name)}']);
        tabListHeader.push(['${homePageTablist[3].map.search_id}','${ifn:cleanJavaScript(homePageTablist[3].map.screen_name)}']);
        tabListHeader.push(['${homePageTablist[4].map.search_id}','${ifn:cleanJavaScript(homePageTablist[4].map.screen_name)}']);
    </script>
</head>

<!-- Home Page Header Content --> 
<div class='container'>
    <div class='home-page-header'> 
        <c:forEach items='${homePageTablist}' var='tab' varStatus="status">
            <div class='home-page-tab'>
                <c:choose>
                    <c:when test='${not empty tab.map.search_id}'>
                        <c:forEach items='${savedSearchList}' var='savedSearch'>
                            <c:if test='${savedSearch.map.search_id eq tab.map.search_id}'>
                                <span class='cancel' onclick='showDeleteDialog(this)' title='close'>
                                    <img src='${cpath}/images/close-button1.png'/>
                                </span>
                                <div class='screen-name' title='${ifn:cleanHtmlAttribute(tab.map.screen_name)}'
                                     name='${tab.map.home_screen_id}'>
                                    <insta:analytics category="Homepage Shortcut Clicks" 
                                                     action="HomePageShortcutTabNumber=${status.count}" 
                                                     label="${actionUrlMap[savedSearch.map.action_id]}"
                                                     href="${cpath}/${actionUrlMap[savedSearch.map.action_id]}${savedSearch.map.query_params}&_homePagetab=${status.count}&_savedsearch=${ifn:cleanHtmlAttribute(tab.map.screen_name)}"
                                                     tagType="hyperLink"> ${fn:toUpperCase(ifn:cleanHtml(tab.map.screen_name))}
                                    </insta:analytics>
                                </div>
                            </c:if>
                        </c:forEach>
                    </c:when>  
                    <c:when test='${not empty tab.map.report_id}'>
                        <c:forEach items='${favReportsList}' var='favReport'>
                            <c:if test='${favReport.map.report_id eq tab.map.report_id}'>
                                <span class='cancel' onclick='showDeleteDialog(this)' title='close'>
                                    <img src='${cpath}/images/close-button1.png' />
                                </span>
                                <div class='screen-name' title='${ifn:cleanHtmlAttribute(tab.map.screen_name)}'
                                     name='${tab.map.home_screen_id}' type='fav'>
                                    <insta:analytics category="Homepage Shortcut Clicks" 
                                                     action="HomePageShortcutTabNumber=${status.count}" 
                                                     label="${actionUrlMap[favReport.map.action_id]}" 
                                                     href="${cpath}/homeFavTabRedirect.do?_homePagetab=${status.count}"
                                                     tagType="hyperLink"> ${fn:toUpperCase(ifn:cleanHtml(tab.map.screen_name))}
                                    </insta:analytics>
                                    <input class='screen-link' value=
                                        '${cpath}/${actionUrlMap[favReport.map.action_id]}${favReport.map.query_params}'/>
                                </div>
                            </c:if>  
                        </c:forEach> 
                    </c:when>    
                     <c:otherwise>
                        <span class='cancel' onclick='showDeleteDialog(this)' title='close'>
                            <img src='${cpath}/images/close-button1.png' />
                        </span>
                        <div class='screen-name' title='${ifn:cleanHtmlAttribute(tab.map.screen_name)}'
                             name='${tab.map.home_screen_id}'>
                            <insta:analytics category="Homepage Shortcut Clicks" 
                                             action="HomePageShortcutTabNumber=${status.count}" 
                                             label="${ifn:cleanJavaScript(tab.map.action_id)}"
                                             href="${tab.map.action_id}&_homePagetab=${status.count}"
                                             tagType="hyperLink">${fn:toUpperCase(ifn:cleanHtml(tab.map.screen_name))}
                            </insta:analytics>
                        </div>
                    </c:otherwise>
                </c:choose>
                <div class='topnav-tooltip-header'></div>
            </div>
        </c:forEach> 

        <c:if test='${homePageTablist.size() < 5}'>
            <div class='add-to-home-page-button' onclick='showAddToHomePageDialog(this);'>
                <div class='add-to-home-page-div'>
                    <img class='add-to-home-page-img' src='${cpath}/icons/plusAddToHomePage.png' />
                </div>
                <div class='add-to-home-page-content'  title='Add to home page'>
                    ADD TO HOME PAGE
                </div>
            </div>
        </c:if> 
    </div>
</div>
