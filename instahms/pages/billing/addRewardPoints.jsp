<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="org.apache.struts.Globals" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>


<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="URl" value="${cpath}/billing/RewardPoints"/>
<html>
<head>
	<title>Add/Remove Reward Points</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>

	<style type="text/css">
		td.forminfo { font-weight: bold; }
		form { padding: 0px; margin: 0px; }
		table.detailFormTable { font-family:Verdana,Arial,sans-serif; font-size:9pt; border-collapse: collapse;}
		table.detailFormTable td.label { padding: 0px 2px 0px 2px; overflow: hidden; }
		.stwMain { margin: 5px 7px }
		tr.deleted {background-color: #F2DCDC; color: gray; }
		tr.deleted input {background-color: #F2DCDC; color: gray;}
		tr.newRow {background-color: #E9F2C2; }
	</style>
	<script>
		var points_available = ${totalPointsAvailable}; //${rewardPointStatusBean!=null?(rewardPointStatusBean.points_earned - rewardPointStatusBean.points_redeemed - rewardPointStatusBean.open_points_redemmed):0};
		function submitForm()
		{
			var mr_no = document.addRewardsform.mr_no.value;
			var points = document.getElementById("points").value;
			var remarks = document.getElementById("remarks").value;
			if(mr_no==null || mr_no=='')
				{
					alert("Select MR No.");
					return false;
				}
			if(points==null || points=="" || points==0)
				{
					alert("Enter Points");
					return false;
				}
			if(remarks==null || remarks=='')
				{
					alert("Enter Remarks");
					return false;
				}
			
			
			//alert(formatAmountPaise(getPaise(points)+getPaise(points_available)));
			if(formatAmountPaise(getPaise(points)+getPaise(points_available)) < 0)
				{
					alert("Points to be subracted are greater than the available points for the patient");
					return false;
				}
			if(Math.floor(points)!=points)
				{
					alert("Points should be integer");
					return false;
				}
			document.addRewardsform.submit();
		}
	</script>
</head>


<body class="yui-skin-sam">
<h1 style="float: left">Add/Remove Reward Points</h1>

<insta:patientsearch searchType="mrNo" searchUrl="${URl}.do"  buttonLabel="Find"
	 searchMethod="addRewardPoints" fieldName="mr_no"/>
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}" id="mr_no">

<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${param.mr_no}" />



<form name="addRewardsform" action="${URl}.do" method="POST">
<input type ="hidden" name="_method" value="saveRewardPoints">
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}" id="mr_no">
<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel">Add/Remove Reward Points</legend>
<table class="formtable">
<tr>
<td class="formlabel">Total Points Available:</td>
<td class="number">${totalPointsAvailable}

</td>
</tr>
<tr>
<td class="formlabel">Reward Points:</td>
<td class="number">
<input type="number" name="points" value="" id="points" onkeypress="return enterNumOnlyANDhypen(event)" >
<span class="star">*</span> 
</td>
<td class="formlabel">Remarks:</td>
<td class="forminfo">
<input type="text" name="remarks" value="" id="remarks" >
<span class="star">*</span>
</td>
</tr>
</table>
</fieldset>
<%-- <input type="button" name="back" value="Back" onclick="return mapping.findForward("${URl}RewardPointsList")">--%>
<input type="button" name="save" value="Save" onclick="return submitForm();">
</form>

<c:url var="url_back" value="RewardPoints.do">
<c:param name="_method" value="getRewardPointsScreen">
</c:param>
</c:url>
<div>
	<a title="Back" href="${url_back}">Patient Reward Points</a>
</div>





</body>
</html>

