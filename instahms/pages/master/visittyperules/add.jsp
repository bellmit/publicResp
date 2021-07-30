<%@page import="com.insta.hms.master.URLRoute"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.OP_VISIT_TYPE_RULES %>"/>
<c:set var="genPrefs" value="${genPrefs[0]}" scope="request"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers" value="${ genPrefs.max_centers_inc_default}" scope="request"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Advanced Followup Rules - Insta HMS</title>
  <insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="script" file="masters/VisitTypeRules.js" />
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script>

 var opVisitTypeRuleMaster = ${ifn:convertListToJson(opVisitTypeRuleMaster)}; //"${discountPlanbean}";
 
</script>
</head>

<body>

	<form  name="OpVisitTypeRulesMasterForm" action="${cpath}${pagePath}/create.htm" method="post"> 

        <table width="100%">
			<tr>
				<td width="100%"><h1>Add Advanced Followup Rules</h1></td>
			</tr>
		</table>
		
		<insta:feedback-panel/>
			
		<fieldset class="fieldSetBorder" id="corporateInsu1">
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%" > 
				<tr>
					<td class="formlabel" >Rule Name: </td>
					<td class="forminfo" >
						<input type="text" name="rule_name" id="rule_name" maxlength="100" value="${bean.rule_name}" />
						<input type="hidden" name="rule_name1" id="rule_name1" value="${bean.rule_name}"/>
					</td>
					<td class="formlabel" width="100px" >Visit after followup: </td>
					<td class="forminfo" >
					    <insta:selectoptions name="post_limit_visit" value="" opvalues="R,M"
					optexts="Revisit,Main Visit" id="postLimitVisit" />
					</td>
					<td class="formlabel" colspan="3"></td>
				</tr>
				<tr>
					<td class="formlabel">Number of Op Followup visit(s): </td>
					<td>
					    <input name="op_main_visit_limit"
					           min="0" max="5" maxlength="5" id="op_main_visit_limit" value="0" 
					           onKeyUp="return isNumber(this.value,'please enter valid number!','op_main_visit_limit');"/>
					</td>
					<td class="formlabel">Number of Ip Followup visit(s): </td>
					<td>
					    <input name="ip_main_visit_limit" 
					           id="ip_main_visit_limit" 
					           min="0" max="5" maxlength="5" value="0" 
					           onKeyUp="return isNumber(this.value,'please enter valid number!', 'ip_main_visit_limit');" />
				    </td>
					<td></td>
					<td></td>
				</tr>
			 </table> 
		 </fieldset>
		
		 <fieldset class="fieldSetBorder" id="corporateInsu1"><legend class="fieldSetLabel">Visit Rules for OP visit</legend>
			<table class="dashboard" id="baseItemTblO" cellpadding="0" cellspacing="0" width="100%">
				<tr class="header">
					<th>Min Days</th>  
					<th>Max Days</th>
					<th>Visit type</th>
					<th>&nbsp;</th> 
				</tr>
				<tr id="" style="display: none">
				</tr>
				<tr>
					<td colspan="3"></td>
					<td>
						<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="AddRow('O')" >
							<img src="${cpath}/icons/Add.png" align="right"/>
						</button>
					</td>
				</tr>
			</table>
		</fieldSet>
		<fieldset class="fieldSetBorder" id="corporateInsu2"><legend class="fieldSetLabel">Visit Rules for IP visit</legend>
		<table class="dashboard" id="baseItemTblI" cellpadding="0" cellspacing="0" width="100%">
			<tr class="header">
				<th>Min Days</th>  
				<th>Max Days</th>
				<th>Visit type</th>
				<th>&nbsp;</th> 
			</tr>
			<tr id="" style="display: none">
			</tr>
			<tr>
				<td colspan="3"></td>
				<td>
					<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="AddRow('I')" >
						<img src="${cpath}/icons/Add.png" align="right"/>
					</button>
				</td>
			</tr>
		</table>
	</fieldSet>
			
		
		<div class="screenActions">
			<button type="button" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>  
			<a href="${cpath}${pagePath}/list.htm?sortOrder=rule_name">Advanced Followup Rules List</a>
		</div>			
	</form>
</body>
</html>
		