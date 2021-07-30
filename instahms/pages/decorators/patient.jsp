<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@page import="com.insta.hms.common.Encoder" %>
<jsp:useBean id="date" class="java.util.Date" />

<%--
  Main jsp page which shows a group of screen menus.
  Input parameter: group: ID of the screen group to show on the left frame.
--%>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="searchaction" value="${cpath}/SearchAction.do" scope="request"/>
<c:set var="prefDecimalDigits" value="<%= GenericPreferencesDAO.getGenericPreferences().getDecimalDigits() %>" scope="session"/>
<c:set var="calendarStartDay" value='<%= GenericPreferencesDAO.getPrefsBean().get("calendar_start_day") %>' scope="session"/>
<%
	String userid = (String) session.getAttribute("userid");
	String hospital = (String) session.getAttribute("sesHospitalId");
%>
<%--
meta attribute is set by the decorated page that supports i18n.
--%>

<c:set var="i18nSupported">
	<decorator:getProperty property="meta.i18nSupport"/>
</c:set>
<%--
textDirection is set to rtl in the CharacterEncodingFilter, if the user has chosen arabic
as the language of preference. if the textDirection is set to rtl and if the page supports
i18n, we change the page direction to rtl. Else we use ltr.
--%>
<c:set var="pageDirection" scope="request"
	value="${(i18nSupported == true && textDirection == 'rtl') ? 'rtl' : 'ltr'}" />

<%@page import="com.insta.hms.usermanager.RoleDAO"%>
<%@page import="com.bob.hms.common.Preferences"%>
<html dir="${pageDirection}">

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title><decorator:title default="Insta HMS"/></title>
	<jsp:include page="/pages/yuiScripts.jsp"/>
	<insta:link type="css" file="style.css"/>
	<insta:link type="css" file="menu.css"/>
	<insta:link type="css" file="accordion.css"/>
	<insta:link type="script" file="common.js"/>
	<insta:link type="script" file="jsvalidate.js"/>
	<insta:link type="script" file="instaautocomplete.js"/>
	<insta:link type="script" file="ToolBar.js"/>
	<insta:link type="script" file="infobox.js"/>
	<insta:link type="script" file="DisplayColumnsToolbar.js"/>
	<insta:js-bundle prefix="common"/>
	<style type="text/css">
		a.yuimenuitemlabel{outline:none;};
	</style>
	<script>
		var userid = '${ifn:cleanJavaScript(userid)}';
		var cpath = '${cpath}';
		var token = "${_insta_transaction_token}";
		var decDigits = "${prefDecimalDigits}";
		var gDefaultVal = "${prefDecimalDigits == 3 ? 0.000 : 0.00}";
		var actionId = "${actionId}";
		var gCalendarStartDay = ${calendarStartDay};
		var gPageDirection = "${pageDirection}";
		var schema = '${ifn:cleanJavaScript(hospital)}';

	</script>

	<decorator:head />
</head>
	<%
		com.insta.hms.master.SystemMessageMaster.SystemMessagesDAO dao =
			new com.insta.hms.master.SystemMessageMaster.SystemMessagesDAO();

		String screenId = (String) request.getAttribute("screenId");

		java.util.List generalMsgs=dao.listAll(new java.util.ArrayList(), "screen_id", "", "display_order");
		java.util.List screenMsgs=dao.listAll(new java.util.ArrayList(), "screen_id", screenId, "display_order");

		java.util.List allMsgs = new java.util.ArrayList();
		allMsgs.addAll(generalMsgs);
		allMsgs.addAll(screenMsgs);
		request.setAttribute("allMsgs", allMsgs);
	%>
<body
	style="overflow-y: scroll; <decorator:getProperty property="body.style" writeEntireProperty="false" /> "
	class="yui-skin-sam <decorator:getProperty property="body.class" writeEntireProperty="false" />"
	id="rootEl"
	onload="loadTransactionTokens('_insta_transaction_token', token); ${not empty allMsgs ? 'showinfo();' : ''}_Insta_checkForAutoComplete();_Insta_checkForAutoComplete_combo();setUserSchema(schema);
	<decorator:getProperty property="body.onload" writeEntireProperty="false" />" >

<script type="text/javascript">

	YAHOO.util.Event.onContentReady("topmenu", function () {
		var oMenu = new YAHOO.widget.MenuBar("topmenu", {
				autosubmenudisplay: false,
				hidedelay:1000, lazyload: true,
				submenualignment: [getCornerSpec("tl"), getCornerSpec("bl")]});
		oMenu.render();
	});
	/*
		TODO : This cannot handle the situation where the user clicks on a link and the link opens in a new tab / window.
		Explore if there is a way to do this using YUI
	*/

	// This handler will be called on every page unload
	window.onbeforeunload = function(e) {
		logOffStaleUser();
	}

	function changeRole(selEl) {
		var form = document.createElement('form');

		form.setAttribute('action', '${cpath}/RoleChangeAction.do');
		form.setAttribute('method', 'GET');
		form.setAttribute('name', 'roleform');

		var roleName = selEl.options[selEl.selectedIndex].text;

		var method = makeHidden('method', 'method', 'changeRole') ;
		var roleIdEl = makeHidden('userRoleId', 'userRoleId', selEl.value) ;
		var roleNameEl = makeHidden('roleName', 'roleName', roleName);

		form.appendChild(method);
		form.appendChild(roleIdEl);
		form.appendChild(roleNameEl);
		document.body.appendChild(form);
		document.roleform.submit();
		return true;
	}
</script>
<table id="tblMain"  width="100%" cellspacing="0" cellpadding="0">
  <tr>
	<td width="25%" height="25">&nbsp;</td>
    <td height="25" class="whitebg brTBN brRn headerPadding">&nbsp;</td>
	<td class="whitebg brTBN brLn txtRT headerContainer">
		<span style="margin: 0px 4px">
			<a href="${cpath}/patient/ChangePassword.do?method=changePassword">Change Password</a>
		</span>
		|
		<span style="margin: 0px 4px">
			<c:set var="logout"><insta:ltext key="page.header.logout"/></c:set>
			<insta:link type="href" path="patient/logout.do" content="${logout}" styleclass="supportlink"/>
		</span>
		|
		<span dir="${pageDirection}">
		<c:set var="strAt"><insta:ltext key="page.header.at"/></c:set>
		<c:set var="strAs"><insta:ltext key="page.header.as"/></c:set>

		<span id="pageuser" class="bold"><%= Encoder.cleanHtml(userid) %></span>
	</td>

	<td width="25%">&nbsp;</td>
  </tr>
  <tr>
    <td class="Navband GbrB" height="33px">&nbsp;</td>
    <td width="976" colspan="2" valign="bottom"  class="Navband "  >
    	<table width="976" cellspacing="0" cellpadding="0" >
    		<tr>
    			<td width="123" style="padding:0 0 1px 0;" class="GbrB brRn">
    				<div style="position:relative; width:100px; height:10px">
							<div style="background:#999; height:40px; width:109px; position:absolute; top:-30px; left:16px; ">
								<div>
									<img src="${cpath}/showScreenLogo.do" style="position:absolute; top:-3px; left:-3px;  z-index:0; height:30px; width:89px; background-color:#FFF; padding:5px 10px; border-left:1px solid #667fa1; border-top:1px solid #667fa1; border-right:1px solid #96a9c2; border-bottom:1px solid #96a9c2;"/>
								</div>
							</div>
						</div>
    			</td>

    			<td valign="bottom" class="GbrB">
    				<c:if test="${not empty userid}">
		    			<table cellspacing="0" cellpadding="0" width="100%">
						  <tr height="20px;">
						  	<td valign="bottom">
								<div id="topmenu" class="yuimenubar yuimenubarnav">
									<div class="bd">
										<ul class="first-of-type">
											<li class="yuimenubaritem"><a class="yuimenubaritemlabel" href="${cpath}/patient/MedicalRecord.do?_method=list">Medical Record History</a></li>
										</ul>
									</div>
								</div>
						    </td>
						  </tr>
						</table>
			  		</c:if>
    			</td>
    		</tr>
    	</table>
		</td>
		<td class="Navband GbrB" >&nbsp;</td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td class="whitebg contentarea" style="padding-top: 0px;" colspan="2">
			<table width="100%" cellspacing="0" cellpadding="0">
				<tr>
  					<td>
  						<div id="content" >
							<decorator:body />
							<div class="foottertxt" style="margin-top:5px;">
								<table width="100%">
									<tr>
										<td>
											<a href="http://www.instahealthsolutions.com/" class="footer-supportlink"
							                   title="Visit the web site (opens in a new window)" target="_blank">
							                   Insta by Practo.
							                </a> Version <fmt:message key="insta.software.version" />.
							                 Copyright &copy; <fmt:formatDate value="${date}" pattern="yyyy" /> Practo Technologies Pvt. Ltd. All Rights Reserved.										
										</td>
										<td align="right">
											<a href="${cpath}/help/Insta_Acknowledgements.pdf" target="_blank"
												class="supportlink">Acknowledgement</a>
										</td>
									</tr>
								</table>
							</div>
						</div>
					</td>
				</tr>
					</td>
				</tr>
				<tr>
					<td>
						<!-- screen ID is: ${screenId} -->
						<table  align="center" style="margin-top: 10px; width: 100%" >
							<tr >
								<td class="message" >
									<c:if test="${not empty allMsgs}">
										<div class="infoPanel" >
											<div id="infobox" style="align: center">
												<c:forEach items="${allMsgs}" var="msg" >
													<li class="infomsg_${msg.map.severity}" style="align: center; display:none">
													${fn:replace(fn:replace(msg.map.messages,'$2',msg.map.param2),'$1',msg.map.param1)}
													</li>
												</c:forEach>
											</div>
										</div>
									</c:if>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>

</body>
</html>
