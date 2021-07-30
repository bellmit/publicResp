<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>	
<!-- Change Role Dialog Begin -->
	<div id="changeRoleDialog" style="display: none; max-width: 400px;">
		<div class="bd">
			<fieldset class="fieldSetBorder" name="userRoleId"
				style="cursor: pointer; height: 250px; overflow: auto;">
				<legend class="fieldSetLabel">Change Role</legend>
				<c:choose>
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
						            request.setAttribute("roles",
						                    com.insta.hms.usermanager.RoleDAO
						                            .getRoles(excludeInstaAdmin));
						%>

						<select name="userRoleId" class="roleCenterSelect" size="15">
							<c:forEach items="${roles}" var="role">
								<option class="roleOpt"
									onmouseenter="roleHover(this)" onmouseleave="roleNormal(this)"
									value="${role.map.role_id}"
									${roleId == role.map.role_id ? 'selected' : ''}
									style="padding: 5px 0px 5px 4px">
									${ifn:cleanHtml(role.map.role_name)}</option>
							</c:forEach>
						</select>

					</c:when>
					<c:otherwise>
						<font color="#336699">${ifn:cleanHtml(roleName)}</font>
					</c:otherwise>
				</c:choose>
			</fieldset>
			<table>
				<tbody>
					<tr>
						<td style="font-size: 12px; color: #646668;">Selected Role: <span
							id="selRole" style="font-weight: bold;"></span>
						</td>
					</tr>
				</tbody>
			</table>
			<table>
				<tbody>
					<tr>
						<td style="padding-top: 10px;"><button type="button"
								value="Save" onclick="changeRole(lastSelRole);" disabled
								id="saveRole">
								<b>Save</b>
							</button></td>
						<td style="padding-top: 10px;"><button type="button"
								id="changeRoleCancelBtn" value="Cancel">
								<b>Cancel</b>
							</button></td>
					</tr>
				</tbody>
			</table>

		</div>
	</div>
	<!-- Change Role Dialog End -->
	
	<!-- Change Center Dialog Begin -->
	<div id="changeCenterDialog" style="display: none; max-width: 400px;">
		<div class="bd">
			<fieldset class="fieldSetBorder" name="userRoleId"
				style="cursor: pointer; height: 250px; overflow: auto;">
				<legend class="fieldSetLabel">Change Center</legend>

				<c:if
					test="${max_centers > 1 && (centerId == 0 || loggedInCenterId == 0)}">
					<%
					    request.setAttribute("center_list",
					                com.insta.hms.master.CenterMaster.CenterMasterDAO
					                        .getAllCentersAndSuperCenterAsFirst());
					%>

					<select name="modifiedCenterId" class="roleCenterSelect" size="15">
						<c:forEach items="${center_list}" var="center">
							<option class="centerOpt"
								onmouseenter="centerHover(this)"
								onmouseleave="centerNormal(this)"
								value="${center.map.center_id}"
								${centerId == center.map.center_id ? 'selected' : ''}
								style="padding: 5px 0px 5px 4px;">${center.map.center_name}</option>
						</c:forEach>
					</select>

				</c:if>
			</fieldset>
			<table>
				<tbody>
					<tr>
						<td style="font-size: 12px; color: #646668;">Selected Center:
							<span id="selCenter" style="font-weight: bold;"></span>
						</td>
					</tr>
				</tbody>
			</table>
			<table>
				<tbody>
					<tr>
						<td style="padding-top: 10px;"><button type="button"
								value="Save" onclick="changeCenter(lastSelCenter);" disabled
								id="saveCenter">
								<b>Save</b>
							</button></td>
						<td style="padding-top: 10px;"><button type="button"
								id="changeCenterCancelBtn" value="Cancel">
								<b>Cancel</b>
							</button></td>
					</tr>
				</tbody>
			</table>

		</div>
	</div>
	<!-- Change Center Dialog End -->