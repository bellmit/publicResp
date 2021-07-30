<%@page import="com.insta.hms.common.MenuItem"%>
<%@page import="com.insta.hms.common.UrlUtil"%>
<script>
var navList = [];
var nav;
var subNav;
var submenu;
</script>

<insta:js-bundle prefix="topnav.menu"/>
<% request.setAttribute("genPrefsBean", com.insta.hms.master.GenericPreferences.GenericPreferencesDAO.getPrefsBean()); %>

<%-- count the number of top level menus --%>
<c:forEach items="${menuConfig.topMenu}" var="menuId" varStatus="status">
	<c:set var="menu" value="${menuConfig.menuGroups[menuId]}"/>
	<c:if test="${menuAvlblMap[menu.id] == 'Y' && menu.id !='grp_settings'}">
		<c:set var="numMenus" value="${numMenus+1}"/>
	</c:if>
</c:forEach>
<c:if test="${empty numMenus}">
	<c:set var="numMenus" value="0"/>
</c:if>

<c:if test="${not empty genPrefsBean.map.menu_background_color}">
	<style type="text/css">
		.yui-skin-sam .yuimenubarnav .yuimenubaritemlabel-hassubmenu {
			background-color: ${ifn:cleanJavaScript(genPrefsBean.map.menu_background_color)};
		}
	</style>
</c:if>

<table style="display:none;" cellspacing="0" cellpadding="0" width="100%">
	<tr>
		<td class="menuBarPadding" width="19">
		</td>
		<td>

<div id="topmenu" class="yuimenubar yuimenubarnav">
<div class="bd">

<ul class="first-of-type" id="topmenu_ul">

	<c:forEach items="${menuConfig.topMenu}" var="menuId" varStatus="status">
		<c:set var="menu" value="${menuConfig.menuGroups[menuId]}"/>

		<c:if test="${menuAvlblMap[menu.id] == 'Y' && menu.id != 'grp_settings'}">
			<li class="yuimenubaritem" style="display: block;">
				<a class="yuimenubaritemlabel" href="#${menu.id}">${menu.name}</a>
				<script>
					nav = {};
					var subMenuItemName = getString("${menu.name}");
					nav.menuId = "${menu.id}";
					nav.labelName = subMenuItemName;
					nav.id= "${menu.id}";
					nav.subNavList = [];
					navList.push(nav);
				</script>
				
				<div id="${menu.id}" class="yuimenu">
					<div class="bd">
						<ul>

							<c:if test="${menu.subGroupCount == 0}">	<%-- normal menu, without sub-menus --%>
								<c:forEach items="${menu.menuItems}" var="menuitem">
								<% MenuItem menuItem = ((MenuItem)pageContext.getAttribute("menuitem")); %>
									<c:choose>
										<c:when test="${menuitem.isSeparator}">
											</ul><ul>
											<script>
												nav.subNavList.push("separator");
											</script>
										</c:when>
										<c:when test="${urlRightsMap[menuitem.actionId] == 'A'}">
<%-- 											<c:choose>
												<c:when test="${empty menuitem.urlParams and empty menuitem.hashFragment}">
													<c:set var="actionUrl" value="${cpath}/${actionUrlMap[menuitem.actionId]}.htm"/>
												</c:when>
												
												<c:when test="${empty menuitem.urlParams}">
													<c:set var="actionUrl" value="${cpath}/index/${menuitem.module}.htm#${menuitem.hashFragment}"/>
												</c:when>
													
												<c:otherwise>
													<c:set var="actionUrl" value="${cpath}/${actionUrlMap[menuitem.actionId]}.htm?${menuitem.urlParams}"/>
	 											</c:otherwise>
											</c:choose> --%>
											<c:set var="actionUrl"
														value='<%= UrlUtil.buildURL(menuItem.getActionId(), null, menuItem.getUrlParams(), menuItem.getHashFragment(), null) %>'/>
											<li class="yuimenuitem">
												<a class="yuimenuitemlabel" href="${actionUrl}">${menuitem.name}</a>
												<script>
													subNav = {};
													var subMenuItemName = getString("${menuitem.name}");
													subNav.labelName = subMenuItemName;
													subNav.linkUrl = "${actionUrl}";
													subNav.hash = "${menuitem.hashFragment}";
													subNav.type = "${menuitem.type}";
													subNav.action_id = "${menuitem.actionId}";
													subNav.query = "${menuitem.urlParams}";
													nav.subNavList.push(subNav);
												</script>
											</li>
										</c:when>
									</c:choose>
								</c:forEach>
							</c:if>

							<c:if test="${menu.subGroupCount > 0}">
								<c:forEach items="${menu.subGroups}" var="subgroup">
									<c:set var="submenu" value="${menuConfig.menuGroups[subgroup.id]}"/>
										<c:if test="${menuAvlblMap[submenu.id] == 'Y'}">
											<li class="yuimenuitem"><a class="yuimenuitemlabel">${submenu.name}</a>
												<script>
													subNav = {};
													var subMenuItemName = getString("${submenu.name}");
													subNav.labelName = subMenuItemName;
													subNav.submenuNavList=[];
													nav.subNavList.push(subNav);
												</script>
												<div class="yuimenu">
							            			<div class="bd">
														<ul>
				              									<c:forEach items="${submenu.menuItems}" var="sitem">
				              									<% MenuItem subMenuItem = ((MenuItem)pageContext.getAttribute("sitem")); %>
																<c:choose>
																	<c:when test="${sitem.isSeparator}">
																		</ul><ul>
																		<script>
																			subNav.submenuNavList.push("separator");
																		</script>
																	</c:when>
				                									<c:when test="${urlRightsMap[sitem.actionId] == 'A'}">
<%-- 				                										<c:choose>
				                										<c:when test="${empty sitem.urlParams and empty sitem.hashFragment}">
				                											<c:set var="actionUrl"
																			value="${cpath}/${actionUrlMap[sitem.actionId]}"/>
				                										</c:when>
				                										<c:when test="${empty sitem.urlParams}">
				                											<c:set var="actionUrl"
																			value="${cpath}/index/${sitem.module}.htm#${sitem.hashFragment}"/>
				                										</c:when>
				                										<c:otherwise>
				                											<c:set var="actionUrl"
																			value="${cpath}/${actionUrlMap[sitem.actionId]}.htm?${sitem.urlParams}"/>
				                										</c:otherwise>
																		</c:choose> --%>
																		<c:set var="actionUrl"
																			value='<%= UrlUtil.buildURL(subMenuItem.getActionId(), null, subMenuItem.getUrlParams(), subMenuItem.getHashFragment(), null) %>'/>
																		<li class="yuimenuitem">
																			<a class="yuimenuitemlabel" href="${actionUrl}">${sitem.name}</a>
																			<script>
																				submenu = {};
																				var subMenuItemName = getString("${sitem.name}");
																				submenu.labelName = subMenuItemName;
																				submenu.linkUrl = "${actionUrl}";
																				submenu.type = "${sitem.type}";
																				submenu.hash = "${sitem.hashFragment}";
																				submenu.action_id = "${sitem.actionId}";
																				submenu.query = "${sitem.urlParams}";
																				subNav.submenuNavList.push(submenu);
																			</script>
																		</li>
				                									</c:when>
																</c:choose>
				              									</c:forEach>
				            							</ul>
													</div>
												</div>
											</li>
										</c:if>
									</c:forEach>
							</c:if>
						</ul>
					</div>
				</div>
			</li>


		</c:if>	<%-- end if menu available --%>
	</c:forEach>	<%-- end for all menus --%>
</ul>

</div>
</div>
		</td>
	</tr>
</table>

