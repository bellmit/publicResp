<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %> 
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<jsp:useBean id="date" class="java.util.Date" />

<%
	String userid = (String) session.getAttribute("userid");
	String hospital = (String) session.getAttribute("sesHospitalId");
%>


<html >

<head>
	<c:set var="totalRecords" value="${pagedList.totalRecords}"/>
	<c:set var="sameDoctor" value="${totalRecords > (pagedList.pageSize * pagedList.pageNumber)}"/>
	<c:set var="doctorNo" value="${sameDoctor ? param.doctorNo : ((fn:length(consultation_doctors)-1 == param.doctorNo) ? 0 : param.doctorNo+1) }"/>
	<c:set var="pageNumber" value="${sameDoctor ? param.pageNumber+1 : 1}"/>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8;"/>
	<c:url var="url" value="ConsultationTokenDisplayAction.do">
		<c:param name="_method" value="getDisplaySystem"/>
		<c:param name="doctorNo" value="${doctorNo}"/>
		<c:param name="pageNumber" value="${pageNumber}"/>
	</c:url>
	<meta http-equiv="refresh" content="${ifn:cleanHtmlAttribute(interval)}; <c:out value='${url}' />" >
	<title>Consultation List - ${doctordetails.map.doctor_name} - Insta HMS</title>
	<jsp:include page="/pages/yuiScripts.jsp"/>
	<insta:link type="css" file="style.css"/>
	<insta:link type="script" file="common.js"/>
	<insta:link type="script" file="jsvalidate.js"/>
	<insta:link type="script" file="instaautocomplete.js"/>
	<insta:link type="script" file="ToolBar.js"/>
	<insta:link type="script" file="infobox.js"/>
	<insta:link type="script" file="DisplayColumnsToolbar.js"/>
	<insta:js-bundle prefix="common"/>

	<script>
        var userid = '${ifn:cleanJavaScript(userid)}';
		var cpath = '${cpath}';
		var token = "${_insta_transaction_token}";
		var decDigits = "${prefDecimalDigits}";
		var gDefaultVal = "${prefDecimalDigits == 3 ? 0.000 : 0.00}";
		var actionId = "${actionId}";
		var schema = '${ifn:cleanJavaScript(hospital)}';
	</script>

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
	style="overflow-y: scroll; "
	class="yui-skin-sam "
	id="rootEl"
	onload="loadTransactionTokens('_insta_transaction_token', token); ${not empty allMsgs ? 'showinfo();' : ''}" >

<script type="text/javascript">

	// This function will be called everytime the user brings the insta window into focus.
	YAHOO.util.Event.on(window, "focus", logOffStaleUser);

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
		<a href="${cpath}/home.do">
			<img src="${cpath}/icons/home.png" class="homeImg"/>
		</a>
		|
		<span style="margin: 0px 4px">
			<c:set var="changePaswd"><insta:ltext key="page.header.changepassword"/></c:set>
			<insta:link type="href" file="AdminModule/ChangePassword.do?method=changePassword"
			content="${changePaswd}" styleclass="supportlink"/>
		</span>
		|
		<span style="margin: 0px 4px">
			<c:set var="logout"><insta:ltext key="page.header.logout"/></c:set>
			<insta:link type="href" path="logout.do" content="${logout}" styleclass="supportlink"/>
		</span>
		|
		<span dir="${pageDirection}">
		<c:set var="strAt"><insta:ltext key="page.header.at"/></c:set>
		<c:set var="strAs"><insta:ltext key="page.header.as"/></c:set>
        <c:set var='hospital'  value="<%=hospital %>" />  
		<span id="pageuser" class="bold">${ifn:cleanHtml(userid)}</span> ${ifn:cleanHtml(strAt)}
		<span class=" bold">${ifn:cleanHtml(hospital)}</span> ${ifn:cleanHtml(strAs)}:
		<c:choose>
   			<c:when test="${roleId == 1 || roleId == 2 || loggedInRoleId == 1 || loggedInRoleId == 2}">
   				<%
   					Integer roleId = (Integer) session.getAttribute("roleId");
   					Integer loggedInRoleId = session.getAttribute("loggedInRoleId") == null ?
   							 roleId : (Integer) session.getAttribute("loggedInRoleId");
   					Boolean excludeInstaAdmin = false;
   					if (loggedInRoleId == 2) {
						excludeInstaAdmin = true;
   					}
   					request.setAttribute("roles", com.insta.hms.usermanager.RoleDAO.getRoles(excludeInstaAdmin));

   				%>
   				<select name="userRoleId" style="cursor: pointer; width : 120px;" class="dropdown_link" onchange="changeRole(this)">
   					<c:forEach items="${roles}" var="role">
						<option value="${role.map.role_id}" ${roleId == role.map.role_id ? 'selected' : ''}>${role.map.role_name}</option>
   					</c:forEach>
   				</select>

			</c:when>
			<c:otherwise><font color="#336699">${ifn:cleanHtml(roleName)}</font>
			</c:otherwise>
		</c:choose>
		</span>
	</td>

	<td width="25%">&nbsp;</td>
  </tr>
  <tr >
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
  							<h1>Consultation List</h1>
  							<fieldset class="fieldSetBorder">
  								<legend class="fieldSetLabel">Doctor Details</legend>
	  							<table style="margin-top: 10px" class="formtable">
	  								<tr>
	  									<td class="formlabel">Doctor Name: </td>
	  									<td class="forminfo">${doctordetails.map.doctor_name}</td>
	  									<td class="formlabel">Qualification: </td>
	  									<td class="forminfo">${doctordetails.map.qualification}</td>
	  									<td class="formlabel">Consulting Times: </td>
	  									<td class="forminfo">
	  										<c:forEach var="doctorTimings" items="${doctor_timings}">
	  											<fmt:formatDate pattern="HH:mm" value="${doctorTimings.map.from_time}"/> To <fmt:formatDate pattern="HH:mm" value="${doctorTimings.map.to_time}"/>
	  											</br>
	  										</c:forEach>
	  									</td>
	  								</tr>
	  								<tr>
	  									<td class="formlabel">Photo: </td>
	  									<td><img src="${cpath}/master/DoctorMasterCharges.do?_method=viewPhoto&doctor_id=${doctordetails.map.doctor_id}"
	  											width="100px" height="100px"/>
	  									</td>
	  									<td class="formlabel">Department: </td>
	  									<td class="forminfo">${doctordepartment.map.dept_name}</td>
	  								</tr>
	  							</table>
	  						</fieldset>
							<table class="resultList" style="margin-top: 10px">
								<tr>
									<th>MR No.</th>
									<th>Token No.</th>
									<th>Visited Date</th>
									<c:if test="${not empty display_patient_name}">
										<th>Patient Name</th>
									</c:if>
								</tr>
								<c:forEach items="${pagedList.dtoList}" var="token">
									<tr>
										<td>${token.map.mr_no}</td>
										<td >${token.map.token_no}</td>
										<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${token.map.visited_date}" /></td>
										<c:if test="${not empty display_patient_name}">
											<td>${token.map.salutation} ${token.map.patient_name} ${token.map.middle_name} ${token.map.last_name}</td>
										</c:if>
									</tr>
								</c:forEach>
							</table>

						</div>
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
				<tr>
					<td>
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
										<a href="${cpath}/help/release_notes.html" target="_blank"
											class="supportlink"> Release Notes</a> |
										<a href="${cpath}/help/Insta_Acknowledgements.pdf" target="_blank"
											class="supportlink">Acknowledgement</a> |
										<a href="${cpath}/help/InstaHMS_help.pdf" target="_blank"
											class="supportlink" >Help</a> |
										<a href="mailto:insta-support@practo.com"
											title="Email Insta customer support team to report a problem or request for assistance"
											target="_blank" class="supportlink">Customer Support</a>
									</td>
								</tr>
							</table>
						</div>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>

</body>
</html>
