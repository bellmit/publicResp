<%@page import="com.insta.hms.master.URLRoute"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.FOLLOWUP_RULES_APPLICABILITY %>"/>
<c:set var="rulePagePath" value="<%=URLRoute.OP_VISIT_TYPE_RULES %>"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Advanced Followup Rules Applicability - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="masters/VisitTypeRules.js" />
	<script type="text/javascript">

		var rules = ${ifn:convertListToJson(rules)};
		var doctors = ${ifn:convertListToJson(doctors)};
		var centerDoctors = ${ifn:convertListToJson(centerDoctors)};
		var departments = ${ifn:convertListToJson(departments)};
		var tpa = ${ifn:convertListToJson(tpa)};
		var centers = ${ifn:convertListToJson(centers)};
		
	</script>

</head>

<body onload="init();">
	
	<form  name="OpVisitTypeRulesApplicability" action="${cpath}${pagePath}/create.htm" method="post"> 
		<input type="hidden" name="_method" id="_method" value="insert"/>
	
		<table width="100%">
			<tr>
				<td width="100%"><h1>Advanced Followup Rules Applicability</h1></td>
				<td>&nbsp; &nbsp;</td>
			</tr>
		</table>		
	
		<insta:feedback-panel/>
	
		<table description="Applicability" class="resultList" cellpadding="0" cellspacing="0" width="100%" >
			<tr>
				<th>Center</th>
				<th>Sponsor</th>
				<th>Department</th>
				<th>Doctor</th>
				<th>Rule</th>
				<th></th>
			</tr>
			<c:forEach var="activityItem" items="${applicabilities}" varStatus="status">
				<tr>
					<td>${activityItem.center_name}</td>
					<td>${activityItem.tpa_name}</td>
					<td>${activityItem.dept_name}</td>
					<td>${activityItem.doctor_name}</td>
					<td>${activityItem.rule_name}</td>
                    <td>
                    	<input type="hidden" name="${activityItem.center_id}${activityItem.tpa_id}${activityItem.dept_id}${activityItem.doctor_id}" value="" />
                    	<img alt="Delete" src="${cpath}/icons/Delete.png" onclick="confirmDeleteApplicbility(${activityItem.rule_applicability_id})"/>
                	</td>
				</tr>
			</c:forEach>
			<tr>
				<td>
					<select required="required" onchange="filterDoctorsWithCenter()" data-recipient=""  class="dropdown" id="centers" name="center_id"></select>
				</td>
				<td>
					<select required="required" data-recipient=""  class="dropdown" id="tpas" name="tpa_id"></select>
				</td>
				<td>
					<select required="required" onchange="loadDoctors()" class="dropdown" id="departments" name="dept_id"></select>
				</td>
				<td>
					<select required="required" class="dropdown" id="doctors" name="doctor_id"></select>
				</td>
				<td>
					<select required="required"  class="dropdown" id="rules" name="rule_id"></select>
				</td>
				<td>
				</td>
			</tr>
		 </table> 
		<div class="screenActions">
			  <button type="button" accesskey="S" onclick="return validateApplicability();"><strong><u>S</u></strong>ave</button>  
		<a href="${cpath}${rulePagePath}/add.htm">|&nbsp; Add New rule&nbsp;|</a>
		<a href="${cpath}${rulePagePath}/list.htm?status=A">Advanced Followup Rules List</a>
		
		</div>			
	</form>
</body>
</html>

