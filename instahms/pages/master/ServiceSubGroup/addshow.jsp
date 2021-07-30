<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Service Sub Group - Insta HMS</title>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="ServiceGroup/ServiceSubGroup.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<script>
		var serviceSubGroupsList = ${serviceSubGroupsList};
	</script>
</head>

<body onload="serviceSubGroupAutoComplete();">

	<form name="ServiceSubGroupForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="service_sub_group_id" id="service_sub_group_id" value="${bean.map.service_sub_group_id }">
	<c:set var="canInactivate" value="${bean.map.service_sub_group_id != -1}"/>

	<table width="100%">
		<tr>
			<td >
				<c:if test="${param._method eq 'add'}"><h1>Add Service Sub Group</h1></c:if>
				<c:if test="${param._method ne 'add'}"><h1>Edit Service Sub Group</h1></c:if>
			</td>

			<c:if test="${param._method ne 'add'}">
			<td align="right">
				<table>
					<tr>
						<td >Service Sub Group Name:</td>
						<td valign="top" style="width: 12em">
							<div id="serviceSubGroupAutocomplete">
								<input type="text" name="serviceSub_group_name" id="serviceSub_group_name"
									class="field" style="width:140px;" />
								<div id="serviceSubGroupContainer"></div>
							</div>
							<input type="hidden" name="serviceSub_group_id" id="serviceSub_group_id" >
						</td>
						<td >
							<input name="getDetails" type="submit" class="button" value="Find" onclick="return searchSubGroup();">
						</td>
					</tr>
				</table>
			</td>
			</c:if>
		</tr>
	</table>
	<insta:feedback-panel/>


	<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Service Group Name:</td>
				<td>
				<select name="service_group_id" id="service_group_id" ${canInactivate?'':'disabled'} class="dropdown">
					<option value=''>-- Select --</option>
				<c:forEach items="${serviceGroupsList}" var="group">
					<option value="${group.map.service_group_id }"
						<c:if test="${group.map.service_group_id == bean.map.service_group_id}">selected</c:if>>
						${group.map.service_group_name }
					</option>
				</c:forEach>
				</select>
				</td>
				<td class="formlabel">Account Head:</td>
				<td><insta:selectdb name="account_head_id"
						table="bill_account_heads" value="${bean.map.account_head_id}"
						displaycol="account_head_name" valuecol="account_head_id"
						dummyvalue="--(Charge Head Default)---" class="dropdown"/>
				</td>
				<td class="formlabel">Status:</td>
				<td>
					<select name="status" id="status" ${canInactivate?'':'disabled'} class="dropdown">
						<option value="A"<c:if test="${bean.map.status eq 'A'}">selected</c:if>>Active</option>
						<option value="I" <c:if test="${bean.map.status eq 'I'}">selected</c:if>>Inactive</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Service Sub Group Name:</td>
				<td valign="top">
					<input type="text" name="service_sub_group_name" id="service_sub_group_name"
						value="${bean.map.service_sub_group_name}" class="field" ${canInactivate?'':'disabled'}>
				</td>
				<td class="formlabel">Alias/Code:</td>
  	            <td>
	  	        	<input type="text" name="service_sub_group_code" id="service_sub_group_code" value="${bean.map.service_sub_group_code }"/>
	  	        </td>
			</tr>
			<tr>
				<td class="formlabel">Display Order:</td>
				<td>
					<input type="text" name="display_order" id="display_order"
						value="${bean.map.display_order}" onkeypress="return enterNumOnly(event)">
				</td>
				<td class="formlabel">Last Modified User:</td>
				<td><b>${bean.map.username}</b></td>
				<td class="formlabel">Last Modified Date:</td>
				<td><b><fmt:formatDate value="${bean.map.mod_time}" pattern="dd-MM-yyyy HH:mm"/></b></td>
			</tr>
			<c:if test="${preferences.modulesActivatedMap['mod_reward_points'] eq 'Y'}">
				<tr>
					<td class="formlabel">Eligible to Earn Points:</td>
					<td class="forminfo">
						<insta:selectoptions name="eligible_to_earn_points" id="eligible_to_earn_points"
							opvalues="N,Y" optexts="No,Yes" value="${bean.map.eligible_to_earn_points}"/>
					</td>

					<td class="formlabel">Eligible to Redeem Points:</td>
					<td class="forminfo">
						<insta:selectoptions name="eligible_to_redeem_points" id="eligible_to_redeem_points"
							opvalues="N,Y" optexts="No,Yes" value="${bean.map.eligible_to_redeem_points}"/>
					</td>
					<td class="formlabel">Redemption Cap Percent(%):</td>
					<td class="forminfo">
						<input type="text" name="redemption_cap_percent" id="redemption_cap_percent"
							value="${bean.map.redemption_cap_percent}">
						<img class="imgHelpText" src="${cpath}/images/help.png"
							title="If service sub group is eligible to redeem points then Redemption Cap Percent is required (Can be zero)."/>
					</td>
				</tr>
			</c:if>
			<c:if test="${param._method ne 'add'}">
			</c:if>
		</table>
	</fieldset>


	<c:url var="url" value="ServiceSubGroup.do">
		<c:param name="_method" value="list"/>
	</c:url>
	<table class="screenActions">
		<tr>
			<td >
				<button type="button" accesskey="S" onclick="validate();"><b><u>S</u></b>ave</button> |
				<a href="<c:out value='${url}' />">Service Sub Group Lists</a>
			</td>
		</tr>
	</table>

	</form>
</body>

</html>
