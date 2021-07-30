<%@page contentType="text/html" isELIgnored="false"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<%--
  Generic JSP page which shows a set of menus based on the group, as
  well as user authorization for the screens.

  Input param: name of the group to show
--%>

<%--
  The screen properties are stored in the screenConfig variable by the digester at app startup.
  The digester looks at information in screens.xml and converts the info into objects of class
  ScreenConfig, ScreenGroup and Screen (package com.bob.hms.common)
--%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="groupId" value="${param.group}"/>
<c:set var="group" value="${menuConfig.menuGroups[groupId]}"/>

<html>
<head>
  <title>Insta HMS</title>
</head>

<body >
        <center><h3>${group.name}</h3></center>
	<table align=center  cellpadding="0" cellspacing="0"
		style="border-collapse: collapse;margin-top:20px" >
		<c:forEach items="${group.menuItems}" var="menuItem">
		<!-- ${menuItem.name} : actionId: ${actionId} urlRight = ${urlRightsMap[menuItem.actionId]} --!>
			<c:if test="${urlRightsMap[menuItem.actionId] != 'N'}">
         		<tr>
				<td height="20" align="left" >
					<a class="modulesInnerHTMLTextBoxHide"  href='${cpath}/${actionUrlMap[menuItem.actionId]}?${menuItem.urlParams}' title="${menuItem.name}">
						${menuItem.name}
					</a>
				</td>
			</tr>
			</c:if>
        	</c:forEach>
      	</table>
</body>
</html>

