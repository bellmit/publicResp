<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.ENCOUNTER_TYPE_PATH %>"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Encounter Type Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function keepBackUp(){
			backupName = document.encountertypeform.encounter_type_desc.value;
		}
		function validate() {
			var opApplicable = document.encountertypeform.op_applicable.value;
			var ipApplicable = document.encountertypeform.ip_applicable.value;
			var daycareApplicable = document.encountertypeform.daycare_applicable.value;
			var encounterVisitType = document.encountertypeform.encounter_visit_type.value;

			if(opApplicable == 'N') {
				if (document.encountertypeform.op_encounter_default.value == 'Y') {
					alert("Not applicable for op");
					document.encountertypeform.op_encounter_default.focus();
					return false;
				}
			}

			if(ipApplicable == 'N') {
				if (document.encountertypeform.ip_encounter_default.value == 'Y') {
					alert("Not applicable for ip");
					document.encountertypeform.ip_encounter_default.focus();
					return false;
				}
			}

			if(daycareApplicable == 'N') {
				if (document.encountertypeform.daycare_encounter_default.value == 'Y') {
					alert("Not applicable for daycare");
					document.encountertypeform.daycare_encounter_default.focus();
					return false;
				}
			}

			if(encounterVisitType == '' || encounterVisitType == null) {
        alert("Visit Type is mandatory");
        document.encountertypeform.encounter_visit_type.focus();
        return false;
      }
			return true;
		}
		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=encounter_type_desc" +
						"&sortReverse=false";
		}
		function focus() {
			document.encountertypeform.encounter_type_desc.focus();
		}

         Insta.masterData=${ifn:convertListToJson(encounterTypeDetails)};

	</script>
</head>
<body onload= "keepBackUp();" >
        <h1 style="float:left">Edit Encounter Type</h1>
        <c:set var="searchUrl" value="${cpath}/${pagePath}/show.htm"/>
        <insta:findbykey keys="encounter_type_desc,encounter_type_id" fieldName="encounter_type_id" method="show" url="${searchUrl}"/>
<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm?"/>
<form action="${actionUrl}"  name="encountertypeform" method="POST">
	<input type="hidden" name="encounter_type_id" value="${bean.encounter_type_id}"/>

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Encounter Type:</td>
			<td>
				<input type="text" name="encounter_type_desc" value="${bean.encounter_type_desc}"/>
			</td>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,InActive"/>
			</td>
			<td class="formlabel">Visit Type</td>
      <td>
        <insta:selectdb name="encounter_visit_type" table="encounter_types_visits"
         dummyvalue="---Select---" dummyvalueId="" value="${bean.encounter_visit_type}"
         valuecol="encounter_types_visit_id" displaycol="encounter_types_visit_name"/>
         <span class="star">*</span>
      </td>
		</tr>
		<tr>
			<td class="formlabel">Op Applicable:</td>
			<td>
				<insta:selectoptions name="op_applicable" value="${bean.op_applicable}" opvalues="Y,N" optexts="Yes,No"/>
			</td>

			<td class="formlabel">Ip Applicable:</td>
			<td>
				<insta:selectoptions name="ip_applicable" value="${bean.ip_applicable}" opvalues="Y,N" optexts="Yes,No"/>
			</td>

			<td class="formlabel">Daycare Applicable:</td>
			<td>
				<insta:selectoptions name="daycare_applicable" value="${bean.daycare_applicable}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
		</tr>

		<tr>
			<td class="formlabel">OP Encounter Defaullt:</td>
			<td>
				<insta:selectoptions name="op_encounter_default" value="${bean.op_encounter_default}" opvalues="Y,N" optexts="Yes,No"/>
			</td>

			<td class="formlabel">IP Encounter Defaullt:</td>
			<td>
				<insta:selectoptions name="ip_encounter_default" value="${bean.ip_encounter_default}" opvalues="Y,N" optexts="Yes,No"/>
			</td>

			<td class="formlabel">Daycare Encounter Defaullt:</td>
			<td>
				<insta:selectoptions name="daycare_encounter_default" value="${bean.daycare_encounter_default}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
			<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
			| <a href="javascript:void(0)" onclick="doClose();">Encounter Type List</a>
	</div>

</form>
</body>
</html>
