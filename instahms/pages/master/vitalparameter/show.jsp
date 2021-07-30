<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<c:set var="pagePath" value="<%=URLRoute.VITAL_PARAMETER_PATH %>"/>
	<c:set var="pagePathForRange" value="<%=URLRoute.VITAL_PARAMETER_RANGE %>"/>
<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Insta HMS</title>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="vitalparameter/vitalparameter.js" />
	<insta:link type="script" file="hmsvalidation.js"/>

	<script type="text/javascript">
		var paramContainer = '${bean.param_container}';
		function doCancel()
		{
			window.location.href="list.htm?&sortOrder=param_container&sortReverse=false&param_status=A";
		}
		function setCheckbox(obj){
			var mandatorycheck = document.getElementById("mandatory_in_tx_chk");
			if(mandatorycheck.checked){
				document.getElementById("mandatory_in_tx").value = 'Y';
			}
			else{
				document.getElementById("mandatory_in_tx").value = 'N';
			}
		}
	</script>

</head>
<body>

	<h1>Edit Vital(I/O) Parameter</h1>
	<insta:feedback-panel/>
	<form action="update.htm" method="POST">
		<input type="hidden" name="param_id" value="${ifn:cleanHtmlAttribute(param.param_id)}" />
		<input type="hidden" name="_method" value="update"/>
		
		<fieldset class="fieldSetBorder">
			<table class="formtable">

				<tr>
					<td class="formlabel">Vital(I/O) Category:</td>
					<td><insta:selectoptions name="param_container" value="${bean.param_container}" opvalues="I,O" optexts="Intake,Output"  onchange="changeVisit(this);" /></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel">Visit Type:</td>
					<td>
						<select name="visit_type" id="visit_type" class="dropdown" onchange="setHiddenvar();" <c:out value="${(bean.param_container == 'O' || bean.param_container == 'I') ? 'disabled' : ''}"/>>
							<option value="" ${empty bean.visit_type ? 'selected' : ''}>All</option>
							<option value="O" <c:out value="${bean.visit_type == 'O' ? 'selected' : ''}"/>>Only for OP</option>
							<option value="I" <c:out value="${bean.visit_type == 'I' ? 'selected' : ''}"/>>Only for IP</option>
						</select>
						<input type="hidden" name="h_visit_type" id="h_visit_type" value="${bean.visit_type}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Name:</td>
					<td><input type="text" name="param_label" id="param_label" class="required validate-length" length="50" value="${bean.param_label}" ${bean.system_vital eq 'Y' ? 'readOnly' : ''}></td>
				</tr>
				<tr>
					<td class="formlabel">UOM:</td>
					<td><input type="text" name=param_uom id="param_uom" onpaste="return false" maxlength="15" value="${bean.param_uom}"></td>
				</tr>

				<tr id="expressionRow" style="display: ${(bean.param_container eq 'V') ? 'table-row' : 'none'};">
					<td class="formlabel">Expression For Parameter:</td>
					<td colspan="3"><input type="text" style="width: 440px" name="expr_for_calc_result" id="expr_for_calc_result" maxlength="100" value="<c:out value="${bean.expr_for_calc_result}"/>"/></td>
				</tr>

				<tr>
					<td class="formlabel">Order:</td>
					<td><input type="text" name=param_order id="param_order" onkeypress="return enterNumOnlyzeroToNine(event);"
						class="required validate-length" length="3" value="${bean.param_order}"></td>
				</tr>

				<tr>
					<td class="formlabel">Status:</td>
					<td><insta:selectoptions name="param_status" value="${bean.param_status}" opvalues="A,I"
						optexts="Active,InActive" /></td>
				</tr>
				<tr>
					<td class="formlabel">Observation Type:</td>
					<td><insta:selectdb name="observation_type" table="mrd_supported_code_types"
							valuecol="code_type" displaycol="code_type" value="${bean.observation_type}"
							dummyvalue="--Select--"/></td>
				</tr>
				<tr>
					<td class="formlabel">Observation Code:</td>
					<td><input type="text" name="observation_code" id="observation_code" value="${bean.observation_code}"></td>
				</tr>
				<tr>
					<td class="formlabel">Mandatory:</td>
					<td>
						<input type="checkbox" id="mandatory_in_tx_chk" name="mandatory_in_tx_chk"  onchange="setCheckbox(this);" value="${bean.mandatory_in_tx}" 
						${bean.mandatory_in_tx == 'Y' ? 'checked' : ''}/>
						<input type="hidden" name="mandatory_in_tx" id="mandatory_in_tx" value="${bean.mandatory_in_tx}"/>
					</td>
					
				</tr>

			</table>
		</fieldset>
		<div class="screenActions" id="divforIntake" style="display: ${(bean.param_container eq 'I' or bean.param_container eq 'O') ? 'block' : 'none'}">
			<button type="button" accesskey="S" name="save" id="save" onclick="validate()"><b><u>S</u></b>ave</button>
			|
				<a href="${cpath}/${pagePath}/add.htm" >Add</a>
			|
			<a href='list.htm?&sortOrder=param_container&sortReverse=false&param_status=A&param_container=I&param_container=O'>Vital(I/O) Parameter List</a>
		</div>

	</form>
<script>
		var vitalParameterJSON=${ifn:convertListToJson(selectedVitaldList)};
		var avlVitalList= ${ifn:convertListToJson(avlVitalList)};
</script>
</body>
</html>
