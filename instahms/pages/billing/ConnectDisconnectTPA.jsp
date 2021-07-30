<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="ip_credit_limit_rule" value='<%=GenericPreferencesDAO.getAllPrefs().get("ip_credit_limit_rule") %>' />

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="title">
 <insta:ltext key="billing.connect.disconnect.details.title"/>
</c:set>
<c:set var="title" value="${title}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="i18nSupport" content="true"/>
	<title>${title}</title>
	<script>
		function handleCancelAction() {
			var opener = window.opener;
			if (opener) {
				opener.handleSubPageCancelActionNewFlow(window);
			}
		}
	</script>
	<c:if test="${isNewUX == 'Y' || isNewUXSuccess == 'Y'}">
		<script>
			function newUXFlowButtonClickHandler() {
				const opener = window.opener;
				if (opener) {
                  opener.handleSubPageConfirmActionNewFlow(window);
				}
			}
		</script>
	</c:if>
	<c:if test="${isNewUXSuccess == 'Y'}">
		<script>
			newUXFlowButtonClickHandler();
		</script>
	</c:if>
	<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		var isTpa = '${isTpa}';
		function showAlert(){
			if(isTpa != 'true')
			alert(getString("js.billing.newrates.notapplyingtobill")+"\n"+getString("js.billing.newrates.throughoption"));

			return true;
		}
	</script>
	<insta:js-bundle prefix="billing.connecttpa"/>
	<insta:js-bundle prefix="billing.newrates"/>
</head>
<c:set var="ok">
	<insta:ltext key="billing.connect.disconnect.details.ok"/>
</c:set>
<c:set var="cancel">
	<insta:ltext key="billing.connect.disconnect.details.cancel"/>
</c:set>
<body>
	<div class="pageHeader">${title}</div>
	<insta:feedback-panel/>

	<insta:patientdetails visitid="${param.visitId}" />
	<c:set var="tpaName" value="${patient.tpa_name}"/>
	<input type="hidden" name="tpaName" value="${patient.tpa_name}">

	<div class="helpPanel">
		<table>
			<tr>
				<td valign="top" style="width: 30px"><img src="${cpath}/images/information.png"/></td>
				<td style="padding-bottom: 5px">
					<insta:ltext key="billing.connect.disconnect.details.connectto.ins.sponsor"/> :<br/>
					<insta:ltext key="billing.connect.disconnect.details.claimamounts.calculated"/>
				</td>
			</tr>
			<tr>
				<td valign="top"><img src="${cpath}/images/information.png"/></td>
				<td>
					<insta:ltext key="billing.connect.disconnect.details.disconnectto.ins.sponsor"/>:<br/>
					<insta:ltext key="billing.connect.disconnect.details.claimamounts.removed"/>
				</td>
			</tr>
			<c:if test="${patient.visit_type == 'i' && (ip_credit_limit_rule == 'W' || ip_credit_limit_rule == 'B')}">
				<tr>
				<td valign="top" style="width: 30px"><img src="${cpath}/images/information.png"/></td>
				<td style="padding-bottom: 5px">
					<insta:ltext key="billing.connect.disconnect.details.ipcreditlimitinfomsg"/><br/>
				</td>
			</tr>
			</c:if>
		</table>
	</div>

	<c:choose>
		<c:when test="${empty error}">
		<form method="POST" action="ConnectDisconnectTPA.do">
			<input type="hidden" name="_method" value="tpaConnectDisconnect"/>
			<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(param.billNo)}" />
			<input type="hidden" name="isNewUX" value="${ifn:cleanHtmlAttribute(param.isNewUX)}" />
			<table align="center">
				<tr>
					<td>
						<c:choose>
							<c:when test="${isTpa}">
								<insta:ltext key="billing.connect.disconnect.details.confirm.disconnectbill"/>
									<b>${ifn:cleanHtml(param.billNo)}</b>
								<insta:ltext key="billing.connect.disconnect.details.fromsponsor"/>?
							</c:when>
							<c:otherwise>
								<insta:ltext key="billing.connect.disconnect.details.confirm.connectbill"/>
									<b>${ifn:cleanHtml(param.billNo)}</b>
								<insta:ltext key="billing.connect.disconnect.details.tosponsor"/>?
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</table>
			<br/>
			<table align="center">
				<tr>
					<td><input type="submit" value="${ok}" onclick="showAlert();"></td>
					<td><input type="button" value="${cancel}" onclick="window.history.back(); handleCancelAction();"/></td>
				</tr>
			</table>
		</form>
		</c:when>
		<c:otherwise>
			<table align="center">
				<tr>
					<td><input type="button" value="Cancel" onclick="window.history.back(); handleCancelAction();"/></td>
				</tr>
			</table>
		</c:otherwise>
	</c:choose>
</body>
</html>

