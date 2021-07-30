<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.usermanager.*"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<html>

<head>
<title>Users - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="usermanager/user.js" />
<insta:link type="script" file="usermanager/checkPasswordStrength.js"/>
<insta:link type="script" file="usermanager/showPassword.js" />
<insta:link type="css" file="../styles/font-awesome.min.css"/>
<style>
	input[disabled] {
		background: #EFEBE7;
	}
</style>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<script>
		<%-- map of userId => userDetails, eg, { "admin": {roleId: "2", remarks: "Hospital Admin"}, ... } --%>
		var userDetailsMap = <%= request.getAttribute("userDetailsMapJSON") %>
		<%-- map of roleId => roleDetails, eg: { "1": {remarks: "Special User"}, ... } --%>
		var roleDetailsMap = <%= request.getAttribute("roleDetailsMapJSON") %>
		var jDocDeptNameList=<%= request.getAttribute("docDeptNameList") %>;
		var malaffiMappedHospitalRoleIds = <%= request.getAttribute("malaffiMappedHospitalRoleIds") %>;
		var modMalaffiEnabled = "${preferences.modulesActivatedMap['mod_malaffi']}" === 'Y';
		var storesJSON = ${storesJSON};
		var theatresJSON = ${theatresJSON};
		var userTheatreList = ${userTheatreList};
		var billingAuthorizerJSON = <%= request.getAttribute("billingAuthorizerJSON") %>;
		var pharmacy_counters = ${pharmacy_counters};
		var billing_counters = ${billing_counters};
		var max_centers_inc_default = ${max_centers_inc_default};
		var passwordRules = ${passwordRules};
		var sampleCollectionCentersJSON = ${sampleCollectionCentersJSON};
		var defaultTheatreId = '<%=request.getAttribute("defaultTheatreId") %>';
		var centerId = '<%=(Integer) session.getAttribute("centerId") %>';
	</script>

<style type="text/css">
	select { width: 19em; }
</style>
</head>

<%--
	TODO:
	 - password should not be displayed on page, we need a separate password change
	   or password reset screen.
--%>
<body class="setMargin yui-skin-sam" onload="updateUserDetails();">
<h1>${empty param.userName  ? 'Add' : 'Edit'} User</h1>
	<div id="disMsg" style="display: block">
		<c:if test="${not empty requestScope.strResultMsg}">
			${strResultMsg}
		</c:if>
	</div>

	<form name="userForm" action="${cpath}/pages/usermanager/UserAction.do" method="POST" enctype="multipart/form-data" autocomplete="off">
	<input type="hidden" name="method" value="saveUser"></input>
	<input type="hidden" name="selUserName" value="${userName}"></input>
	<input type="hidden" name="op" value="create"></input>
	<input type="hidden" name="schedulerDefaultDoctor" styleId="doctor"></input>
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
		<table class="formtable" cellpadding="0%"
			cellspacing="0%" >

			<tr>
	            <td class="formlabel">User Name:</td>
				<td style="white-space:nowrap"><input type="text" name="name" class="field" style="width: 200px;" onkeypress="return enterAlphanNumerical(event);" maxlength="25"/> <span class="star">*</span></td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>

			<tr>
				<td class="formlabel">SSO Only User:</td>
				<td style="white-space:nowrap">
					<input type="checkbox" name="ssoOnlyUser"/>
				</td>
			</tr>

			<tr>
                <td class="formlabel">Password:</td>
                <td style="white-space:nowrap">
                <input type="password" id="password" name="password" maxlength="20"
                    class="field" style="width: 200px;" onkeyup="checkPasswordStrengthOnKeyup('strength1','password')" />
                <span class="star">*</span>
                <i class="fa fa-eye" onclick="toggleShowPassword('password','togglePassword1');" id="togglePassword1" style="padding-right:5px;color:#666666;cursor:pointer"></i>
                <span id="strength1"></span>
                </td>
            </tr>

            <tr>
                <td class="formlabel">Confirm Password:</td>
                <td style="white-space:nowrap">
                <input type="password" id="confirmPassword" name="confirmPassword" maxlength="20"
                    class="field" style="width: 200px;" onkeyup="checkPasswordStrengthOnKeyup('strength2','confirmPassword')" />
                    <span class="star">*</span>
                <i class="fa fa-eye" onclick="toggleShowPassword('confirmPassword','togglePassword2');" id="togglePassword2" style="padding-right:5px;color:#666666;cursor:pointer"></i>
                <span id="strength2"></span>
            </tr>
            <tr>
            	<td class="formlabel">User must change password during next login:</td>
            	 <td style="white-space:nowrap">
            			<input type="checkbox" name="forcePasswordChange" checked="true"/>
            	</td>
            </tr>
			<tr>
				<td class="formlabel" style="white-space:nowrap">Name(to display on prints):</td>
				<td><input type="text" name="fullname" styleId="fullname" class="field" style="width: 200px;"
					maxlength="50"></input> <span class="star">*</span></td>
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<td style="white-space:nowrap"><select name="status" class="dropdown" style="width: 200px;">
					<option value="A">Active</option>
					<option value="I">Inactive</option>
				</select> <span class="star">*</span></td>
			</tr>

			<tr>
				<td class="formlabel">First Name:</td>
				<td style="white-space:nowrap">
					<input type="text" name="firstName" class="field" style="width: 200px;" onkeypress="return enterAlpha(event);" maxlength="30" value=" "/>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Middle Name:</td>
				<td style="white-space:nowrap">
					<input type="text" name="middleName" class="field" style="width: 200px;" onkeypress="return enterAlpha(event);" maxlength="50" value=" "/>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Last Name:</td>
				<td style="white-space:nowrap">
					<input type="text" name="lastName" class="field" style="width: 200px;" onkeypress="return enterAlpha(event);" maxlength="30" value=" "/>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Gender:</td>
				<td style="white-space:nowrap">
					<select name="gender" class="dropdown" style="width: 200px;">
						<option value="">Select</option>
						<option value="M">Male</option>
						<option value="F">Female</option>
						<option value="O">Other</option>
					</select>
				</td>
			</tr>

			<tr>
				<td class="formlabel" style="white-space:nowrap">Email Id:</td>
				<td style="white-space:nowrap">
					<input type="text" name="emailId" class="filed" style="width: 200px;" />
				</td>
			</tr>

			<tr>
				<td class="formlabel" style="white-space:nowrap">Mobile No:</td>
				<td style="white-space:nowrap">
					<input type="text" name="mobileNo" class="filed" maxlength="15" onkeypress="return enterPhone(event)"/>
				</td>
			</tr>

			<tr>
				<td class="formlabel" style="white-space:nowrap">Employee ID:</td>
				<td style="white-space:nowrap">
						<input type="text" name="employeeId" class="filed" style="width: 200px;" />
				</td>
			</tr>

			<tr>
				<td class="formlabel" style="white-space:nowrap">Profession:</td>
				<td style="white-space:nowrap">
						<input type="text" name="profession" class="filed" style="width: 200px;" />
				</td>
			</tr>

			<tr>
				<td class="formlabel" style="white-space:nowrap">Employee Category:</td>
				<td style="white-space:nowrap">
						<input type="text" name="employeeCategory" class="filed" style="width: 200px;" />
				</td>
			</tr>

			<tr>
				<td class="formlabel" style="white-space:nowrap">Employee Major:</td>
				<td style="white-space:nowrap">
						<input type="text" name="employeeMajor" class="filed" style="width: 200px;" />
				</td>
			</tr>

			<tr>
			    <td class="formlabel">Hospital Roles:</td>
				<td><insta:selectdb name="hospitalRoleIds" id="hospitalRoleIds" table="hospital_roles_master" multiple="true"
						valuecol="hosp_role_id" displaycol="hosp_role_name" values="${userHospitalRoleIds}"
						orderby="hosp_role_name" style="width :200px;"/>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Confidentiality Groups:</td>
				<td>
					<select name="confidentialityGroupIds" multiple class="listbox" style="width: 200px">
						<c:forEach items="${confidentialityGroups}" var="option">
							<c:set var="value" value="${option.get('confidentiality_grp_id')}"/>
							<c:choose>
								<c:when test="${ifn:arrayFind(userConfidentialityGroupIds,value) ne -1}">
									<c:set var="attr" value="selected='true'"/>
								</c:when>
								<c:otherwise>
									<c:set var="attr" value=""/>
								</c:otherwise>
							</c:choose>
							<option value='<c:out value="${value}"/>' ${attr}>
								${option.get("name")}
							</option>
						</c:forEach>
					</select>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Shared Login:</td>
				<td style="white-space:nowrap">
					<select name="sharedLogin" class="dropdown" style="width: 200px;">
						<option value="Y">Yes</option>
						<option value="N">No</option>
					</select>
				</td>
			</tr>

			<tr>
				<td valign="top" class="formlabel">Application Role:</td>
				<td style="white-space:nowrap"><select styleId="roleId" name="roleId" class="dropdown" style="width: 200px;"
					onchange="updateRoleRemarks()">
					<option value="">..Role..</option>
					<c:forEach items="${reqRolesList}" var="item">
						<option value="${item.roleId}">${item.name}</option>
					</c:forEach>
				</select> <span class="star">*</span><br>
				<textarea cols="23" rows="2" name="roleRemarks" readonly="1" style="width:16.7em"></textarea>
				</td>
			</tr>

			<tr>
				<td class="formlabel">User Center:</td>
				<td>
					<insta:selectdb name="userCenter" table="hospital_center_master" style="width: 200px;"
				     valuecol="center_id" displaycol="center_name"
				     onchange="filterMultiStores(this.value);filterCounters();filterDefaultPharmStore();sampleCollectionCenterChanges(this);filterBillingAuthorization(this.value);filterMultiTheatres(this.value);filterDefaultTheatre();"/>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Reporting Center Group:</td>
				<td>
					<insta:selectdb name="reportCenter" table="center_group_master" style="width: 200px;"
				     dummyvalue="-- Select --" valuecol="center_group_id" displaycol="center_group_name" orderby="center_group_name"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Doctor:</td>
				<td>
					<insta:selectdb table="doctors" valuecol="doctor_id" class="dropdown" style="width: 200px"
					 	dummyvalue="-- Select --" displaycol="doctor_name" name="doctorId" orderby="doctor_name"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Scheduler Department:</td>
				<td class="formpg">
					<select name="schedulerDepartment" onchange="initDoctorDept(document.userForm.schedulerDepartment.value);"
						class="dropdown" style="width:200px">
					<option value=""></option>
					<c:forEach items="${reqAllDeptNamesList}" var="item">
						<option value="${item.DEPT_ID}">${item.DEPT_NAME}</option>
					</c:forEach>
				</select>
				</td>
				<td>
				<img class="imgHelpText" title="Default department for Scheduler Day View" src="${cpath}/images/help.png"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Doctor login controls applicable for appt. booking: </td>
				<td class="forminfo">
					<select name="loginControlsApplicable" class="dropdown">
						<option value="Y">Yes</option>
						<option value="N">No</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Billing Counter:</td>
				<td>
					<c:if test="${empty param.userName}">
						<img class="imgHelpText" title="Please create the user then map the billing counter" src="${cpath}/images/help.png"/>	
					</c:if>			
					<c:if test="${not empty param.userName}">
						<c:set var="counterMappingUrl" value="${cpath}/master/usercentercounters/show.htm?emp_username=${param.userName}"/>
						<a href="<c:out value='${counterMappingUrl}'/>" >Add/Remove Billing Counter</a>
					</c:if>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Store Access:</td>
				<td><select name="multiStoreId" class="listbox" style="width: 200px" multiple="multiple" onchange="filterDefaultPharmStore();">
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Default Store:</td>
				<td>
					<select name="pharmacyStoreId" class="dropdown" style="width: 200px" >
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Pharmacy Counter:</td>
				<td>
					<select name="pharmacycounterId" id="pharmacycounterId" class="dropdown" style="width: 200px">
					</select>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Specialization:</td>
				<td><select name="specialization" class="dropdown" style="width: 200px">
					<option value=""></option>
					<c:forEach items="${reqSpecificationsList}" var="item">
						<option value="${item.DEPT_ID}">${item.DEPT_NAME}</option>
					</c:forEach>
				</select></td>
			</tr>

			<tr>
				<td class="formlabel">Diagnostic Department:</td>
				<td><select name="labDepartment" class="dropdown" style="width: 200px">
					<option value=""></option>
					<c:forEach items="${reqLabDeptNamesList}" var="item">
						<option value="${item.DDEPT_ID}">${item.DDEPT_NAME}</option>
					</c:forEach>
				</select></td>
			</tr>
			
			<tr>
				<td class="formlabel">Service Department:</td>
				<td><insta:selectdb name="serdeptid" id="serdeptid" table="services_departments" multiple="true"
						valuecol="serv_dept_id" displaycol="department" values="${userServiceDeptsIds}" 
						filtercol="status" filtervalue='A' orderby="department" style="width :200px;"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel" >Scheduler Default Doctor:</td>
				<td class="formpg" style="padding-bottom:20px"><div id="doc_dept_wrapper">
					<input type="text" name="doctor_name" id="doctor_name" style="width:200px"/>
				<div id="doc_dept_dropdown"></div>
				</td>
				<td>
				<img class="imgHelpText" title="Default doctor for Scheduler Week View" src="${cpath}/images/help.png"/>
				</td>

			</tr>
			<tr>
				<td class="formlabel">Theater / Room :</td>
				<td><select name="multiTheatreId" class="listbox" style="width: 200px" multiple="multiple" onchange="filterDefaultTheatre();">
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Default Theatre:</td>
				<td>
					<select name="defaultTheatresId" class="dropdown" style="width: 200px">
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel" >Billing Authorizer:</td>
				<td>
					<select name="discAuthorizer" id="discAuthorizer" class="dropdown" style="width: 200px">
					</select>
				</td>
			</tr>

			<%--  reference bug 5374
			<tr>
				<td class="formlabel">PO Masters Access:</td>
				<td>
					<select name="masterUpdate" >
						<option value="NO">NO</option>
						<option value="YES">YES</option>
					</select>
				</td>
			</tr>
	 --%>

			<!--    reference bug HMS-23079, for prescription note taker--> 
			
			<tr>
				<td class="formlabel">
					Default Ward:
				</td>
				<td class="formpg">
				<insta:selectdb name="bedViewDefaultWard" table="ward_names" valuecol="ward_no"
					displaycol="ward_name" orderby="ward_no"/>
				</td>
			</tr>
			<tr id="sampleCollectionTR">
				<td class="formlabel">Sample Collection Center</td>
				<td>
				     <select name="sampleCollectionCenter" id="sampleCollectionCenter" class="dropdown">
				     	<option value="">--Select--</option>
				     <c:forEach items="${collectionCenters}" var="collection">
				     	<c:if test="${collection.map.status=='A' && collection.map.collection_center_id!=-1}">
				     		<option value="${collection.map.collection_center_id}">${collection.map.collection_center}</option>
				     	</c:if>
				     </c:forEach>
				     </select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">PO Approval Limit Upto</td>
				<td>
					<input type="text" name="poApprovalLimit" styleId="poApprovalLimit" class="field" style="width: 200px;"
					onkeypress="return enterNumAndDot(event);" maxlength="13"></input>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Bill Write Off Limit Upto</td>
				<td>
					<input type="text" name="writeOffLimit" styleId="writeOffLimit" class="field" style="width: 200px;"
					onkeypress="return enterNumAndDot(event);" maxlength="13"></input>
					<img class="imgHelpText" title="Bill Patient Due Write Off Limit Upto" src="${cpath}/images/help.png"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel" style="white-space:nowrap">Permissible Discount Cap%:</td>
				<td style="white-space:nowrap">
					<input type="text" name="permissibleDiscountCap" class="filed" maxlength="5" onkeypress="return enterNumAndDot(event);"/>
				</td>
			</tr>
			<tr>
				<td valign="top" class="formlabel">Remarks:</td>
				<td><textarea cols="23" rows="2" name="remarks" style="width:16.7em"></textarea></td>
			</tr>
			<tr>
				<td class="formlabel">Signature: </td>
				<td class="forminfo"><input type="file" name="userSignature" id="userSignature" accept="<insta:ltext key="upload.accept.image"/>"/>
				<c:if test="${not empty signature_username}">
					<a href="${cpath}/pages/usermanager/UserAction.do?method=viewSignature&empUserName=${ifn:cleanURL(param.userName)}" title="View Signature" target="_blank">View Signature</a>
				</c:if>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Allow signature to be used by others: </td>
				<td class="forminfo">
					<select name="allow_sig_usage_by_others" class="dropdown">
						<option value="N">No</option>
						<option value="Y">Yes</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Allow Bill Finalization with Patient Due: </td>
				<td class="forminfo">
					<select name="allowBillFinalization" class="dropdown">
						<option value="N">No</option>
						<option value="Y">Yes</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Last Modified By:</td>
				<td><b><label id="mod_user"></label></b>  <b><label id="mod_date"></label></b></td>
			</tr>
		</table>
	</fieldset>

	<div id="buttons" class="screenActions">
		<button type="button" style="button" onclick="return submitFun()" accesskey="S"><b><u>S</u></b>ubmit</button>
		|
		<c:if test="${not empty param.userName}">
			<c:set var="url" value="${cpath}/pages/usermanager/UserAction.do?method=getUserScreen"/>
			<a href="<c:out value='${url}'/>" >Add New User</a>
		|
		</c:if>
			<c:set var="url1" value="${cpath}/pages/usermanager/UserDashBoard.do?_method=list"/>
			<a href="${url1}">User DashBoard</a>
	</div>
</form>

<div id="debug" style="display:block; font-size: 7pt;"></div>
</body>
</html>

