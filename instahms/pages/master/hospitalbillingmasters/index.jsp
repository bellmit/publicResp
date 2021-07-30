<%@ page pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true" />
<meta name="decorator" content="reactDecorator" />
<title>Master - Insta HMS</title>
</head>

<body>
	<div id="app"></div>
	<script>
		contextPath = "${pageContext.request.contextPath}";
		publicPath = contextPath + "/ui/";
		isSettingsPage = true;
		masterType = "${masterType}";
	</script>
	<insta:ui type="js" file="${language}-manifest.js" />
	<insta:ui type="js" file="${language}-vendor.js" />
	<insta:ui type="js" file="${language}-hospitalBillingMasters.js" />
</body>
