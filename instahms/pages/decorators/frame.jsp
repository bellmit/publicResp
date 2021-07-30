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

<c:set var="hasSettingsAccess" value='<%=menuAvlblMap.containsKey("grp_settings") && ((String) menuAvlblMap.get("grp_settings")).equals("Y") %>' />
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

<%@page import="com.insta.hms.usermanager.RoleDAO"%>
<html dir="${pageDirection}">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="referrer" content="no-referrer" />

<title><decorator:title default="Insta HMS" /></title>
<insta:link type="css" path="scripts/yui2.8.0r4/fonts/fonts-min.css"/>
<c:set var="yuiStylesDir" value="${pageDirection == 'rtl' ? 'assets-rtl' : 'assets'}"/>
<insta:link type="css" path="scripts/yui2.8.0r4/${yuiStylesDir}/skins/sam/container.css"/>
<insta:link type="css" path="scripts/yui2.8.0r4/${yuiStylesDir}/skins/sam/menu.css"/>
<insta:link type="css" path="scripts/yui2.8.0r4/${yuiStylesDir}/skins/sam/autocomplete.css"/>

<c:if test="${pageDirection == 'rtl'}">
<insta:link type="css" file="yui-rtl-override.css"/>
</c:if>
<insta:link type="script" file="yui2.8.0r4/yahoo-dom-event/yahoo-dom-event.js"/>
<insta:link type="script" file="yui2.8.0r4/animation/animation-min.js"/>
<insta:link type="script" file="yui2.8.0r4/container/container-min.js"/>
<insta:link type="script" file="yui2.8.0r4/container/container_core-min.js"/>
<insta:link type="script" file="yui2.8.0r4/menu/menu-min.js"/>
<insta:link type="script" file="yui2.8.0r4/datasource/datasource-min.js"/>
<insta:link type="script" file="yui2.8.0r4/autocomplete/autocomplete-min.js"/>
<insta:link type="css" file="style.css" />
<insta:link type="css" file="topnav.css" />
<insta:link type="css" file="homePage/dialogBox.css" />
<style type="text/css">
a.yuimenuitemlabel {
	outline: none;
}
td.zero-height{
	font-size:0;
	padding: 0;
	margin:0;
}
</style>
<insta:link type="script" file="common.js" />
<insta:link type="script" file="infobox.js" />
<insta:js-bundle prefix="common" />
<insta:link type="script" file="tourUtil.js" />
<insta:link type="script" file="jquery-2.2.4.min.js" />
<insta:link type="script" file="analytics/tracking.js" />
<insta:link type="script" file="topnav.js" />
<insta:link type="script" file="breakTheGlass.js" />
<insta:link type="script" file="Home.js" />

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
	style=" margin:0; overflow-y: scroll; <decorator:getProperty property="body.style" writeEntireProperty="false" /> "
	class="yui-skin-sam <decorator:getProperty property="body.class" writeEntireProperty="false" />"
	id="rootEl"
	onload="deleteDialog();addToHomePageDialog();loadAddToHomePageDialog();roleDialog();centerDialog();loadTransactionTokens('_insta_transaction_token', token);setUserSchema(schema);
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

	// This event handler on the window is invoked whenever a browser window is brought
	// into focus, either by switching tabs or windows. This would also be called when
	// the application is first called.

	YAHOO.util.Event.on(window, "focus", logOffStaleUser);
</script>
<!-- To change menu bar color -->
<c:if test="${not empty genPrefsBean.map.menu_background_color}">
<script>
    defaultColor= '${ifn:cleanJavaScript(genPrefsBean.map.menu_background_color)}';
</script>
</c:if>

	<table id="tblMain" width="100%" cellspacing="0" cellpadding="0">
		<tr style="display: none;">
			<td class='zero-height' width="25%" height="25">&nbsp;</td>
			<td class='zero-height' height="25" class="whitebg brTBN brRn headerPadding">&nbsp;</td>
			<td class="whitebg brTBN brLn txtRT headerContainer"><a
				href="${cpath}/home.do"> <img src="${cpath}/icons/home.png"
					class="homeImg" />
			</a> <c:if
					test="${(preferences.modulesActivatedMap['mod_notification'] eq 'Y')}">
		|<span style="margin: 0px 4px"> <a
						href="${cpath}/message/MyNotifications.do?_method=list&sortOrder=last_sent_date&sortReverse=true&message_status=S">My
							Notifications (<b>${count}</b>)
					</a>
					</span>
				</c:if> | <span style="margin: 0px 4px"> <c:set var="changePaswd">
						<insta:ltext key="page.header.changepassword" />
					</c:set> <insta:link type="href"
						file="AdminModule/ChangePassword.do?method=changePassword"
						content="${changePaswd}" styleclass="supportlink" />
			</span> | <span style="margin: 0px 4px"> <c:set var="logout">
						<insta:ltext key="page.header.logout" />
					</c:set> <insta:link type="href" path="logout.do" content="${logout}"
						styleclass="supportlink" />
			</span> <c:if test="${centerId != 0}">
		    |
			<span style="margin: 0px 4px"> <c:set var="center">
							<insta:ltext key="page.header.center" />
						</c:set> ${center} : <span id="centerName" class="bold">${ifn:cleanHtml(centerName)}</span>
					</span>
				</c:if> | <span dir="${pageDirection}"> <c:set var="strAt">
						<insta:ltext key="page.header.at" />
					</c:set> <c:set var="strAs">
						<insta:ltext key="page.header.as" />
					</c:set> <span id="pageuser" class="bold">${ifn:cleanHtml(userid)}</span> ${strAt} <span
					class=" bold">${ifn:cleanHtml(hospital)}</span> ${strAs}: <c:choose>
						<c:when
							test="${roleId == 1 || roleId == 2 || loggedInRoleId == 1 || loggedInRoleId == 2}">
							<%
							    Integer roleId = (Integer) session.getAttribute("roleId");
							            Integer loggedInRoleId = session
							                    .getAttribute("loggedInRoleId") == null ? roleId
							                            : (Integer) session
							                                    .getAttribute("loggedInRoleId");
							            Boolean excludeInstaAdmin = false;
							            if (loggedInRoleId == 2) {
							                excludeInstaAdmin = true;
							            }
							            request.setAttribute("all_roles",
							                    com.insta.hms.usermanager.RoleDAO
							                            .getRoles(excludeInstaAdmin));
							%>
							<select name="userRoleId" style="cursor: pointer; width: 120px;"
								class="dropdown_link" onchange="changeRole(this)">
								<c:forEach items="${all_roles}" var="curr_role">
									<option value="${curr_role.map.role_id}"
										${roleId == curr_role.map.role_id ? 'selected' : ''}>${curr_role.map.role_name}</option>
								</c:forEach>
							</select>
						</c:when>
						<c:otherwise>
							<font color="#336699">${ifn:cleanHtml(roleName)}</font>
						</c:otherwise>
					</c:choose>
			</span> <c:if
					test="${max_centers > 1 && (centerId == 0 || loggedInCenterId == 0)}">
			|
			<%
     request.setAttribute("center_list",
                 com.insta.hms.master.CenterMaster.CenterMasterDAO
                         .getAllCentersAndSuperCenterAsFirst());
 %>
					<select name="modifiedCenterId"
						style="cursor: pointer; width: 120px;" class="dropdown_link"
						onchange="changeCenter(this)">
						<c:forEach items="${center_list}" var="center">
							<option value="${center.map.center_id}"
								${centerId == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
						</c:forEach>
					</select>
				</c:if></td>

			<td width="25%">&nbsp;</td>
		</tr>


		<tr>
			<td class='zero-height' width="25%">&nbsp;</td>
			<td class='zero-height' width="976" colspan="2" id="tour-area" valign="bottom"
				style="border-left: 1px #96a9c2 solid; border-right: 1px #96a9c2 solid;">
				<table style="visibility: hidden;" width="976"></table>
				<table width="100%">
					<tr>

						<td>
							<div class="topnav">
								<div class="topnav-overlay" style="display: none"></div>
								<c:if test="${not empty userid}">
									<jsp:directive.include file="/pages/decorators/menu.jsp" />

									<div class="leftnav">
										<ul class="leftnav-btn">
											<li></li>
											<li></li>
											<li></li>
										</ul>
										<div class="leftnav-content hide"></div>
									</div>
								</c:if>

								<div class="usernav">
									<div class="usernav-btn">
										<span> <img src="${cpath}/icons/User.png" /> <span>User</span>
										</span>

									</div>
									<div class="topnav-tooltip">
									</div>
									<div class="usernav-content hide">
										<div class="usernav-content-div">
											<table class="un-userInfo">
												<tr>
													<td><img src="${cpath}/icons/User_Bubble.png" /></td>
													<td>
														<p>${ifn:cleanHtml(userid)}</p>
														<p>
														
															Role:  <span id="curRole"> 
																		<c:choose>
																			<c:when test="${not empty all_roles }">
																				<c:forEach items="${all_roles}" var="curr_role">
																						<c:if test="${roleId == curr_role.map.role_id}">
																							${curr_role.map.role_name}
																						</c:if>
																				</c:forEach>
																			</c:when>
																			<c:otherwise>
																				${ifn:cleanHtml(roleName)}
																			</c:otherwise>
																		</c:choose>
																	</span>
														</p>
														<p>
															Schema: <span> ${ifn:cleanHtml(hospital)} </span>
														</p>
													</td>
												</tr>
											</table>


											<ul class="usernavMenu">
												<li><a
													onclick="removeMask(); showRoleDialog(this); initRoleCond(); closeusernav();"
													class="changeCursor"><p>Change Role</p></a></li>
												<li><a
													href="${cpath}/pages/AdminModule/ChangePassword.do?method=changePassword"><p>Change
															Password</p></a></li>
											</ul>
											<table class="un-userInfo">
												<tr>
													<td><img src="${cpath}/showScreenLogo.do"
														style="border-radius: 50%; height: 35px; width: 35px; border: 1px solid #CECECE;"></td>
													<td>
														<p id="curCenter">
															<c:choose>
																<c:when test="${ centerId == 0} ">
																	<c:forEach items="${center_list}" var="center">
																		<c:if test="${centerId == center.map.center_id}">
																			${center.map.center_name}
																	</c:if>
															</c:forEach>
																</c:when>
																<c:otherwise>
																${centerName}
																</c:otherwise>
														</c:choose>
													
														</p>
													</td>
												</tr>
											</table>
 
											<ul class="usernavMenu">
												
											<c:if test="${not empty center_list  }">		
												<li><a
													onclick="removeMask(); showCenterDialog(this); initCenterCond(); closeusernav();"
													class="changeCursor"><p>Change Center</p></a></li> 
											</c:if>
												
											<c:choose>
												<c:when test="${not empty center_list}">
													<li id="logout-spec"><a
														onclick="window.location.href='${cpath}/logout.do'"
														class="changeCursor"><p>Logout</p></a></li>
												</c:when>
												<c:otherwise>
													
													<li><a
														onclick="window.location.href='${cpath}/logout.do'"
														class="changeCursor"><p>Logout</p></a></li>	
												</c:otherwise>
											</c:choose>
											</ul>
										</div>
									</div>
								</div>
								<div class="helpnav">
									<div class="helpnav-btn">
										<span> <img src="${cpath}/icons/Help.png" /> <span>Help</span>
										</span>
									</div>
									<div class="topnav-tooltip">
									</div>
									<div class="helpnav-content hide">
										<div class="helpnav-content-div">
											<ul class="helpnavMenu">
												<li><a href="${cpath}/help/index.htm" 
													target="_blank"><p><insta:ltext key="js.topnav.menu.insta.hms.help"/></p></a></li>
												<li><a href="${cpath}/help/release_${majorMinor}.html"
													target="_blank"><p><insta:ltext key="js.topnav.menu.insta.release.notes"/></p></a></li>
												<li><a href="${cpath}/help/Insta_Acknowledgements.pdf"
													target="_blank"><p><insta:ltext key="js.topnav.menu.acknowledgement"/></p></a></li>
												<li><a href="${cpath}/help/main.html"  
													target="_blank"><p><insta:ltext key="js.topnav.menu.insta.api.docs"/></p></a></li>
												<li><a href="${cpath}/help/fa_documentation.html"  
													target="_blank"><p><insta:ltext key="js.topnav.menu.insta.fa.docs"/></p></a></li>
												<li><a href="${cpath}/help/Insta_Diagnostics_Integration_Guide.pdf"
													target="_blank"><p><insta:ltext key="js.topnav.menu.insta.diag.docs"/></p></a></li>
											</ul>
										</div>
									</div>
								</div>


								<div class="msgnav">
									<div class="msgnav-btn">
										<span> <img src="${cpath}/icons/Messages.png" /> <span>Messages</span>
										</span>
									</div>
									<div class="topnav-tooltip">
									</div>
									<div class="msgnav-content hide">
										<div class="msgnav-content-div">
											<ul class="msgnavMenu">
												<c:choose>
													<c:when test="${not empty allMsgs}">
														<c:forEach begin="0" end="2" items="${allMsgs}" var="msg"
															varStatus="i">
															<li><c:if test="${msg.map.severity=='I'}">
																	<img src="${cpath}/images/blue_flag.gif">Information<br>
																</c:if> <c:if test="${msg.map.severity=='W'}">
																	<img src="${cpath}/images/yellow_flag.gif">Warning<br>
																</c:if> <c:if test="${msg.map.severity=='A'}">
																	<img src="${cpath}/images/red_flag.gif">Alert<br>
																</c:if> <span>${fn:replace(fn:replace(msg.map.messages,'$2',msg.map.param2),'$1',msg.map.param1)}</span>
															</li>

														</c:forEach>
													</c:when>
													<c:otherwise>
														<li>No Message</li>
													</c:otherwise>
												</c:choose>
											</ul>
										</div>
									</div>
								</div>
                                <c:if test="${hasSettingsAccess}">
                                <div class="settingsDiv">
                                    <insta:analytics tagType="hyperLink" category="Settings" action="open" label="${broadVersionNumber}"
                                                     href="${cpath}/index/applicationsettings.htm#/applicationsettings" >
                                        <div class="settings-btn">
                                            <span> 
                                                <img src="${cpath}/icons/Settings.png" 
                                                     height="14px" width="14px" /> 
                                                <span>
                                                    <insta:ltext key="js.topnav.menu.settings"></insta:ltext>
                                                </span>
                                            </span>
                                        </div>
                                    </insta:analytics>
                                </div>
                                </c:if>
								<div class="homeDiv">
									<a href="${cpath}/home.do">
										<div class="home-btn">
											<span> <img src="${cpath}/icons/nav_home.png"
												height="14px" width="14px" /> <span>Home</span>
											</span>
										</div>
									</a>
								</div>



							</div>
						</td>
					</tr>
				</table>
			</td>
			<td class='zero-height' width="25%">&nbsp;</td>
		</tr>

		<tr>
			<td></td>
			<td></td>
			<td></td>
		</tr>

		<tr>
			<td></td>
			<td></td>
			<td></td>
		</tr>

		<tr>
			<td>&nbsp;</td>
			<td class="whitebg" style="padding-top: 0px;" colspan="2">
				<table width="100%" cellspacing="0" cellpadding="0">
					<tr>
						<td>
							<div id="content">
                                <!-- Home Page Header (Shortcut) -->
                                <c:if test="${isHomePage eq true || homePageTabNumber != null}">
                                    <jsp:include page="/pages/headerHomePage.jsp"></jsp:include>
                                    <div class='hr-style'> </div>
                                </c:if>
                                <decorator:body /> 
                            </div> 
						</td>
					</tr>

					<tr style="display: none;">
						<td>
							<!-- screen ID is: ${screenId} -->
							<table align="center" style="margin-top: 10px; width: 100%">
								<tr>
									<td class="message"><c:if test="${not empty allMsgs}">
											<div class="infoPanel">
												<div id="infobox" style="align: center">
													<c:forEach items="${allMsgs}" var="msg" varStatus="i">
														<li class="infomsg_${msg.map.severity}"
															style="align: center; display: ${i.index == 0 ? 'block' : 'none'}; opacity: 0;">
															${fn:replace(fn:replace(msg.map.messages,'$2',msg.map.param2),'$1',msg.map.param1)}
														</li>
													</c:forEach>
												</div>
											</div>
										</c:if></td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
                        <td style="padding: 0 10px 10px;">
                            <jsp:include page="/pages/footer.jsp"></jsp:include>
                        </td>
                    </tr>
				</table>
			</td>
		</tr>
	</table>
		<jsp:include page="/pages/dialogBox/breakTheGlass.jsp"></jsp:include>
    <jsp:include page="/pages/dialogBox/changeRoleAndCenterDialog.jsp"></jsp:include>
    <jsp:include page="/pages/dialogBox/addToHomePageBox.jsp"></jsp:include>
    <jsp:include page="/pages/dialogBox/deleteBox.jsp"></jsp:include>
    <jsp:include page="/pages/bpmchat.jsp"></jsp:include>
</body>
</html>
