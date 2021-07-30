<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="/WEB-INF/esapi.tld" prefix="esapi" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.messaging.MessageActionService"%>
<%@page import="java.util.Map"%>
<%@page import="com.insta.hms.common.Encoder" %>

<%--
  Main jsp page which shows a group of screen menus.
  Input parameter: group: ID of the screen group to show on the left frame.
--%>
<%
	String userid = (String) session.getAttribute("userid");
    String hospital = (String) session.getAttribute("sesHospitalId");
    Map menuAvlblMap = (Map) session.getAttribute("menuAvlblMap");
    request.setAttribute("genPrefsBean", com.insta.hms.master.GenericPreferences.GenericPreferencesDAO
                                            .getPrefsBean()); 
    request.setAttribute("tab_list",com.insta.hms.addtohomepagemaster.AddToHomePageMasterDAO
                                       .getAllHomePageTabs((String) session.getAttribute("userid")));
%>

<c:set var="hasSettingsAccess" value='<%=menuAvlblMap.containsKey("grp_settings") && ((String)menuAvlblMap.get("grp_settings")).equals("Y") %>' />
<c:set var="cpath" value="${pageContext.request.contextPath}"
    scope="request" />
<c:set var="searchaction" value="${cpath}/SearchAction.do"
    scope="request" />
<c:set var="prefDecimalDigits"
    value="<%=GenericPreferencesDAO.getGenericPreferences().getDecimalDigits()%>"
    scope="session" />
<c:set var="calendarStartDay"
    value='<%=GenericPreferencesDAO.getPrefsBean().get("calendar_start_day")%>'
    scope="session" />
<c:set var="max_centers"
    value='<%=GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default()%>'
    scope="request" />
<c:set var="use_smart_card"
    value='<%=GenericPreferencesDAO.getPrefsBean().get("use_smart_card")%>' />
<c:set var="enable_force_selection_for_mrno_search"
    value='<%=GenericPreferencesDAO.getPrefsBean().get("enable_force_selection_for_mrno_search")%>' />
<c:set var="hospital" value="<%=hospital %>" />
<c:set var="counterName" value='<%=session.getAttribute("billingcounterName") %>' />
<c:set var="count"
    value='<%=MessageActionService.notificationCount((String) session.getAttribute("userid"))%>'
    scope="session" />
<%--
meta attribute is set by the decorated page that supports i18n.
--%>

<c:set var="i18nSupported">
    <decorator:getProperty property="meta.i18nSupport" />
</c:set>

<!-- Value is true if at home Page -->
<c:set var="isHomePage">
    <decorator:getProperty property="meta.isHomePage" />
</c:set>

<!-- Tab from which screen was called -->
<c:set var="homePageTabNumber" 
       value='<%=request.getParameter("_homePagetab") %>' />
       
<%--
textDirection is set to rtl in the CharacterEncodingFilter, if the user has chosen arabic
as the language of preference. if the textDirection is set to rtl and if the page supports
i18n, we change the page direction to rtl. Else we use ltr.
--%>
<c:set var="pageDirection" scope="request"
    value="${(i18nSupported == true && textDirection == 'rtl') ? 'rtl' : 'ltr'}" />

<c:set var="versionNumber">
    <insta:ltext key="insta.software.version" />
</c:set>
<c:set var="VersionNumberWithoutBuild" value="${fn:split(versionNumber, '.')}" />
<c:set var="broadVersionNumber" value="${VersionNumberWithoutBuild[0]}.${VersionNumberWithoutBuild[1]}" />
<c:set var="majorMinor" value="${VersionNumberWithoutBuild[0]}${fn:length(VersionNumberWithoutBuild[1]) == 1 ? '0':''}${VersionNumberWithoutBuild[1]}" />

<%@page import="com.insta.hms.usermanager.RoleDAO"%>
<!DOCTYPE html>
<html dir="${pageDirection}">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="referrer" content="no-referrer" />

<title><decorator:title default="Insta HMS" /></title>
<jsp:include page="/pages/yuiScripts.jsp" />
<insta:link type="css" file="menu.css" />
<insta:link type="css" file="accordion.css" />
<insta:link type="css" file="topnav.css" />
<insta:link type="css" file="homePage/dialogBox.css" />
<style type="text/css">
a.yuimenuitemlabel {
    outline: none;
}
#changeCenterDialog_c, #changeRoleDialog_c, #changeCenterDialog {
    z-index: 1100;
}
.usernav-btn, .helpnav-btn, .msgnav-btn {
    height: 100%;
}
/* Change center/role dialog */
.fieldSetBorder {
    border: 1px #CCCCCC solid;
    margin: 0px 0px 5px 0px;
    padding: 1px 6px 4px 6px;
    height: 260px !important;
    overflow:hidden !important;
}

.fieldSetLabel {
    font-weight:bold;
    color:#666;
    margin: 0px;
    padding: 10px 4px;
}

.changeCursor {
    cursor: pointer;
}

.usernav-btn, .helpnav-btn, .msgnav-btn {
    padding: 11px 16px 0px;
}
.leftnav-btn {
    padding: 5px 12px;
}

.leftnav-btn > li {
    margin: 5px 0;
}
.home-btn, .settings-btn {
    padding-top:5px;
    background-color: rgba(247, 247, 250, 0.1);
} 

.usernav-content, .helpnav-content, .msgnav-content {
    top: 40px;
}

.roleCenterSelect {
    cursor: pointer; 
    width: 100%; 
    height: 240px; 
    border: none;
}

.yui-skin-sam .mask {
    opacity: 0.07;
}

.yui-skin-sam .yui-panel .bd, .yui-skin-sam .yui-panel .ft {
    background-color:#FFFFFF;
}

.homeDiv > a, .settingsDiv > a {
    text-decoration: none !important;
}
.instacollection {
    width:100%;
    display:block;
    font-size: 12px;
    line-height: 2.5;
    text-align: center;
    color: #ffffff; 
}
.instacollection.expired {
    background-color: #ff0000;
}
.instacollection.due {
    background-color: #00b400;
}
.instacollection.overdue {
    background-color: #ffa000;
}

</style>
<insta:link type="script" file="common.js" />
<insta:link type="script" file="jsvalidate.js" />
<insta:link type="script" file="instaautocomplete.js" />
<insta:link type="script" file="ToolBar.js" />
<insta:link type="script" file="infobox.js" />
<insta:link type="script" file="DisplayColumnsToolbar.js" />
<insta:js-bundle prefix="common" />
<insta:link type="script" file="instadate.js" />
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="tourUtil.js" />
<insta:link type="script" file="jquery-2.2.4.min.js" />
<insta:link type="script" file="analytics/tracking.js" />
<insta:link type="script" file="request_callback.js" />

<script>
        var loggedInCenterId = ${ifn:cleanJavaScript(centerId)};        
        var loggedInRoleId = <%= (Integer) session.getAttribute("roleId")%>;
        
        var isSettingsPage = false;
        var defaultColor= '${not empty genPrefsBean.map.menu_background_color ? ifn:cleanJavaScript(genPrefsBean.map.menu_background_color) : ""}';
        var flagSchema='false';
        
        var userid = '${ifn:cleanJavaScript(userid)}';
        var cpath = '${cpath}';
        var token = "${_insta_transaction_token}";
        var tokenKey = '_insta_transaction_token';
        var decDigits = "${prefDecimalDigits}";
        var gDefaultVal = "${prefDecimalDigits == 3 ? 0.000 : 0.00}";
        var actionId = "${actionId}";
        var gScreenId = "${screenId}";
        var gCalendarStartDay = ${calendarStartDay};
        var gPageDirection = "${pageDirection}";
        var gUse_smart_card = '${use_smart_card}';
        var enlanguage = '${language}';
        var fin_yr_start_month = '<%=GenericPreferencesDAO.getAllPrefs().get("fin_year_start_month")%>';
        var fin_yr_end_month = '<%=GenericPreferencesDAO.getAllPrefs().get("fin_year_end_month")%>';
        var enableForceSelectionForMRNoSearch = '${enable_force_selection_for_mrno_search}';
        var schema = '${ifn:cleanJavaScript(hospital)}';
        var isHomePage = '${isHomePage}';
        var headerLength= '${tab_list.size()}';
        var homePageTabNumber = '${ifn:cleanJavaScript(homePageTabNumber)}';
        var broadVersionNumber = '${VersionNumberWithoutBuild[0]}.${VersionNumberWithoutBuild[1]}';
        var counterName = '${counterName}';
    </script>
<decorator:head />
</head>
<%
    com.insta.hms.master.SystemMessageMaster.SystemMessagesDAO dao = new com.insta.hms.master.SystemMessageMaster.SystemMessagesDAO();

            String screenId = (String) request.getAttribute("screenId");

            java.util.List generalMsgs = dao.listAll(new java.util.ArrayList(), "screen_id", "", "display_order");
            java.util.List screenMsgs = dao.listAll(new java.util.ArrayList(), "screen_id", screenId, "display_order");

            java.util.List allMsgs = new java.util.ArrayList();
            allMsgs.addAll(generalMsgs);
            allMsgs.addAll(screenMsgs);
            request.setAttribute("allMsgs", allMsgs);
%>
<body
    style=" margin:0; overflow-y: auto <decorator:getProperty property="body.style" writeEntireProperty="false" /> "
    class="yui-skin-sam <decorator:getProperty property="body.class" writeEntireProperty="false" />"
    id="rootEl"
    onload="loadTransactionTokens('_insta_transaction_token', token);_Insta_checkForAutoComplete_combo();setUserSchema(schema);
    <decorator:getProperty property="body.onload" writeEntireProperty="false" />">
    <div class="mask" id = "default-mask">&nbsp;</div>
    <script type="text/javascript">

    YAHOO.util.Event.onContentReady("topmenu", function () {
        var oMenu = new YAHOO.widget.MenuBar("topmenu", {
                autosubmenudisplay: false,
                hidedelay:1000, lazyload: true,
                submenualignment: [getCornerSpec("tl"), getCornerSpec("bl")]});
        oMenu.render();
    });
    YAHOO.util.Event.on(window, "load", logOffStaleUser); 

    // This event handler on the window is invoked whenever a browser window is brought
    // into focus, either by switching tabs or windows. This would also be called when
    // the application is first called.

    YAHOO.util.Event.on(window, "focus", logOffStaleUser);

</script>

<c:if test="${roleId == 1 || roleId == 2 || loggedInRoleId == 1 || loggedInRoleId == 2}">
    <%
        Integer roleId = (Integer) session.getAttribute("roleId");
        Integer loggedInRoleId = session.getAttribute("loggedInRoleId") == null ? roleId
                                 : (Integer) session.getAttribute("loggedInRoleId");
        Boolean excludeInstaAdmin = false;
        if (loggedInRoleId == 2) {
            excludeInstaAdmin = true;
        }
        request.setAttribute("all_roles",com.insta.hms.usermanager.RoleDAO.getRoles(excludeInstaAdmin));
    %>
                        
</c:if>
<jsp:directive.include file="/pages/decorators/instacollection.jsp" />
<table id="tblMain" width="100%" cellspacing="0" cellpadding="0">
        
        <tr>
            <td class="whitebg contentarea" style="padding-top: 0px;" colspan="2">
                <table width="100%" cellspacing="0" cellpadding="0">
                    <tr>
                        <td>
                            <div id="content">
                                <decorator:body /> 
                            </div> 
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
  
<c:if test="${max_centers > 1 && (centerId == 0 || loggedInCenterId == 0)}">
    <%
    request.setAttribute("center_list",com.insta.hms.master.CenterMaster.CenterMasterDAO.getAllCentersAndSuperCenterAsFirst());
    %>
</c:if>
    <jsp:include page="/pages/dialogBox/changeRoleAndCenterDialog.jsp"></jsp:include>
    <jsp:include page="/pages/dialogBox/supportRequestCallback.jsp"></jsp:include>
    <jsp:include page="/pages/bpmchat.jsp"></jsp:include>
</body>
</html>
