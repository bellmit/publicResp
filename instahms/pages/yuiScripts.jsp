<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="yuiStylesDir" value="${pageDirection == 'rtl' ? 'assets-rtl' : 'assets'}"/>
<!-- Individual YUI CSS files -->
<insta:link type="css" path="scripts/yui2.8.0r4/fonts/fonts-min.css"/>
<insta:link type="css" path="scripts/yui2.8.0r4/${yuiStylesDir}/skins/sam/button.css"/>
<insta:link type="css" path="scripts/yui2.8.0r4/${yuiStylesDir}/skins/sam/container.css"/>
<insta:link type="css" path="scripts/yui2.8.0r4/${yuiStylesDir}/skins/sam/calendar.css"/>
<insta:link type="css" path="scripts/yui2.8.0r4/${yuiStylesDir}/skins/sam/autocomplete.css"/>
<insta:link type="css" path="scripts/yui2.8.0r4/${yuiStylesDir}/skins/sam/menu.css"/>
<insta:link type="css" path="scripts/yui2.8.0r4/${yuiStylesDir}/skins/sam/logger.css"/>
<insta:link type="css" path="scripts/yui2.8.0r4/${yuiStylesDir}/skins/sam/treeview.css"/>
<c:if test="${pageDirection == 'rtl'}">
<insta:link type="css" file="yui-rtl-override.css"/>
</c:if>

<!-- Individual YUI JS files -->

<fmt:message key="yuiDebug" var="yuiDebug"/>
<c:choose>
<c:when test="${yuiDebug}">
<insta:link type="script" file="yui2.8.0r4/yahoo/yahoo-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/dom/dom-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/event/event-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/element/element-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/animation/animation-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/connection/connection-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/datasource/datasource-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/autocomplete/autocomplete-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/calendar/calendar-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/container/container-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/json/json-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/container/container_core-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/menu/menu-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/button/button-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/logger/logger-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/dragdrop/dragdrop-debug.js"/>
<insta:link type="script" file="yui2.8.0r4/cookie/cookie-debug.js"/>
</c:when>
<c:otherwise>
<insta:link type="script" file="yui2.8.0r4/yahoo-dom-event/yahoo-dom-event.js"/>
<insta:link type="script" file="yui2.8.0r4/element/element-min.js"/>
<insta:link type="script" file="yui2.8.0r4/animation/animation-min.js"/>
<insta:link type="script" file="yui2.8.0r4/connection/connection-min.js"/>
<insta:link type="script" file="yui2.8.0r4/datasource/datasource-min.js"/>
<insta:link type="script" file="yui2.8.0r4/autocomplete/autocomplete-min.js"/>
<insta:link type="script" file="yui2.8.0r4/calendar/calendar-min.js"/>
<insta:link type="script" file="yui2.8.0r4/container/container-min.js"/>
<insta:link type="script" file="yui2.8.0r4/json/json-min.js"/>
<insta:link type="script" file="yui2.8.0r4/container/container_core-min.js"/>
<insta:link type="script" file="yui2.8.0r4/menu/menu-min.js"/>
<insta:link type="script" file="yui2.8.0r4/button/button-min.js"/>
<insta:link type="script" file="yui2.8.0r4/logger/logger-min.js"/>
<insta:link type="script" file="yui2.8.0r4/dragdrop/dragdrop-min.js"/>
<insta:link type="script" file="yui2.8.0r4/cookie/cookie-min.js"/>
<insta:link type="script" file="yui2.8.0r4/yahoo/yahoo-min.js"/>
<insta:link type="script" file="yui2.8.0r4/get/get-min.js"/>
</c:otherwise>
</c:choose>

<insta:link type="script" file="yui2.8.0r4/treeview/treeview.js"/>
<insta:link type="script" file="accordion.js"/>

