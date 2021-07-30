<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>

	<title>OP Medicine Frequency - Insta HMS</title>
	<insta:link type="js" file="master/medicinedosage/medicinedosage.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>

	<script>

		function doClose() {
			window.location.href = "${cpath}/master/MedicineDosage.do?_method=list&sortOrder=dosage_name" +
						"&sortReverse=false";

		}

		<c:if test="${param._method != 'add'}">
		      Insta.masterData=${medicineDosageLists};
		</c:if>
	</script>
</head>

<body >
  <c:choose>
      <c:when test="${param._method != 'add'}">
           <h1 style="float:left">Edit OP Medicine Frequency</h1>
           <c:url var="searchUrl" value="/master/MedicineDosage.do"/>
           <insta:findbykey keys="dosage_name,dosage_name" fieldName="med_dosage_name" method="show" url="${searchUrl}"/>
      </c:when>
      <c:otherwise>
         <h1>Add OP Medicine Frequency</h1>
      </c:otherwise>
  </c:choose>

	<insta:feedback-panel/>
	<form action="MedicineDosage.do"  name="medicineDosageForm" method="POST">
		<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(param._method=='add'?'create':'update')}"/>
		<input type="hidden" name="keyForUpdate" value="${ifn:cleanHtmlAttribute(param._method=='add'?'':dosageBean.map.dosage_name)}" />
		<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Frequency: </td>
				<td>
					<input type="text" name="dosage_name" id="dosage_name" maxlength="150"
						value="${ifn:cleanHtmlAttribute(param._method=='show'?dosageBean.map.dosage_name:'')}"
						${ifn:cleanHtmlAttribute(param._method=='show'?'readonly':'')}
						/>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Per Day Frequency: </td>
				<td>
					<input type="text" name="per_day_qty" id="per_day_qty"
						value="${ifn:cleanHtmlAttribute(param._method=='show'?dosageBean.map.per_day_qty:'')}"
						class="number"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Frequency Value: </td>
				<td><input type="text" name="frequency_value" id="frequency_value" value="${dosageBean.map.frequency_value}" onkeypress="return enterNumOnlyzeroToNine(event)"></td>

				<td colspan="2">per:<insta:selectoptions name="frequency_type" id="frequency_type" value="${dosageBean.map.frequency_type}" opvalues="Hour,Day,Week,Once" optexts="HOUR,DAY,WEEK,ONCE" dummyvalue="---Select---" dummyvalueId=""/></td>
			</tr>
		</table>
		</fieldset>

		<div class="screenActions">
			<button type="submit" accesskey="S" onclick="return  validate(event);"><b><u>S</u></b>ave</button>
			|
			<c:if test="${param._method != 'add'}">
				<a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/MedicineDosage.do?_method=add'">Add</a>
			|
			</c:if>
			<a href="javascript:void(0)" onclick="doClose();">Dosages List</a>
		</div>

	</form>
</body>
</html>