<%@ page pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="i18nSupport" content="true" />
    <meta name="decorator" content="reactDecorator" />
    <title>Order - Insta HMS</title>
    <style>
        select[name="modifiedCenterId"] {
            height: 216px;
            margin-top: -4px;
            font-size: 12px;
        }
    </style>
</head>
<c:set var="bundle">
	<c:choose>
			<c:when test="${flowType == 'opFlow'}">${language}-v12.js</c:when>
			<c:otherwise>${language}-ipFlow.js</c:otherwise>
	</c:choose>
</c:set>
<body>
	<div id="app"></div>
	<script>
		activityPaneLayout="withFlow";
		contextPath = "${pageContext.request.contextPath}";
		publicPath = contextPath + "/ui/";
		activity = 'order';
		flowType = "${flowType}";
		
	</script>
	<insta:ui type="js" file="${language}-manifest.js" />
	<insta:ui type="js" file="${language}-vendor.js" />
	<insta:ui type="js" file="${bundle}" />
</body>
