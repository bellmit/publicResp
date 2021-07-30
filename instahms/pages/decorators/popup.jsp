<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@page contentType="text/html" isELIgnored="false"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%--
  Main jsp page which shows a group of screen menus.
  Input parameter: group: ID of the screen group to show on the left frame.
--%>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="searchaction" value="${cpath}/SearchAction.do" scope="request"/>

<html>

<head>
	<meta http-equiv="Content-Type" content="text/html"/>
	<title><decorator:title default="Insta HMS"/></title>
	<jsp:include page="/pages/yuiScripts.jsp"/>
	<insta:link type="css" file="style.css"/>
	<insta:link type="css" file="menu.css"/>
	<insta:link type="css" file="accordion.css"/>
	<insta:link type="script" file="common.js"/>
	<insta:link type="script" file="jsvalidate.js"/>
	<insta:link type="script" file="dblclickblocker.js"/>
	<insta:link type="script" file="instaautocomplete.js"/>
	<insta:link type="script" file="infobox.js"/>
	<insta:link type="script" file="ToolBar.js"/>
	<style type="text/css">
		a.yuimenuitemlabel{outline:none;};
	</style>

	<decorator:head />
</head>
<body
	onload="attachToAllForms(); _Insta_checkForAutoComplete();_Insta_checkForAutoComplete_combo();
	<decorator:getProperty property="body.onload" writeEntireProperty="false" />"
	<decorator:getProperty property="body.class" writeEntireProperty="true" />
	<decorator:getProperty property="body.style" writeEntireProperty="true" /> class="yui-skin-sam" id="rootEl">

<script type="text/javascript">

</script>
<table id="tblMain"  width="100%" cellspacing="0" cellpadding="0">
  <tr>
	<td width="25%" height="25">&nbsp;</td>
    <td height="25" class="whitebg brTBN" style="padding-left:10px; border-right:none;">&nbsp;</td>
		<td class="whitebg brTBN txtRT" style="padding:6px 10px 2px 0; border-left:none;">

	</td>
	<td width="25%">&nbsp;</td>
  </tr>

	<tr>
		<td>&nbsp;</td>
		<td class="whitebg contentarea" style="padding-top: 0px;" colspan="2">
			<table width="100%" cellspacing="0" cellpadding="0">
				<tr>
  					<td colspan="3">
  						<div id="content" >
							<decorator:body />
						</div>
					</td>
				</tr>

			</table>
		</td>
	</tr>
</table>

<script>
	var cpath = '${cpath}';
</script>
</body>
</html>
