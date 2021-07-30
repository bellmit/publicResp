<%@ page pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true" />
<meta name="decorator" content="reactDecorator" />
<insta:ui type="css" file="font-awesome.min.css" />
<title>topnav - Insta HMS</title>
<style>
	select[name="modifiedCenterId"] {
		height: 216px;
    	margin-top: -4px;
    	font-size: 12px;
	}
</style>
</head>
<body>
	<div id="app"></div>
	<script>
		activityPaneLayout="withFlow";
		flowType = "opFlow";
		activity = "consultation";
		contextPath = "${pageContext.request.contextPath}";
		publicPath = contextPath + "/ui/";
	</script>
	<insta:ui type="js" file="${language}-manifest.js" />
	<insta:ui type="js" file="${language}-vendor.js" />
	<insta:ui type="js" file="${language}-topNav.js" />
</body>
</html>
