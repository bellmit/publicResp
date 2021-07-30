<%@ page contentType='text/html' isELIgnored='false'%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/fmt' prefix='fmt' %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/functions' prefix='fn' %>
<%@ taglib tagdir='/WEB-INF/tags' prefix='insta' %>

<html>
<head>
    <c:set var='cpath' value='${pageContext.request.contextPath}' scope='request' />
    <meta name="isHomePage" content="true"/>
</head>

<body>
    <div class='home-page-content'>
        <div style= "margin-top:8px">
            <div class="report-title"></div>
            <div class="refresh-button" onclick="refreshReport()">
                <span>
                    <img src="${cpath}/icons/Refresh.png" />
                </span> Refresh Report
            </div>
        </div>
        <iframe src='' id='fav-screen'></iframe>
   </div>
</body>
</html>
