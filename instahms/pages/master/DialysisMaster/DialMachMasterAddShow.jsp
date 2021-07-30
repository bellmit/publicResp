<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.dialysismachinemasterinstahms"/></title>
<insta:link type="css" file="hmsNew.css"/>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function doClose() {
			window.location.href = "${cpath}/master/DialMachMaster.do?_method=list&status=A&sortOrder=machine_name&sortReverse=false";
		}
	function modelvalidate(){
		var machineModel = document.DialMachineMaster.model_number.value;
		var dialysisnumber = document.DialMachineMaster.d_no.value;
		dialysisnumber = trimAll(dialysisnumber);
		if(machineModel == 'DBB06' &&(empty(dialysisnumber) || dialysisnumber == '')){
			alert("For Model No. DBB-06 Dialysis Machine Number Required");
			document.DialMachineMaster.d_no.focus();
			return false;
		}
		if(!empty(dialysisnumber)){
			var dlength = dialysisnumber.length;
					if(dlength < 8 || dlength > 8){
						alert("Dialysis machine Number should be of 8 characters")
						document.DialMachineMaster.d_no.focus();
						return false;
					}
		}
		if(document.DialMachineMaster.location_id.value == ''){
			alert("Location is mandatory field");
			document.DialMachineMaster.location_id.focus();
			return false;
		};
		return true;
	}
	
	</script>

	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>


</head>
<body>
	<form  name ="DialMachineMaster" action="DialMachMaster.do" method="POST">
		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>

		<c:set var="addText"><insta:ltext key="generalmasters.dialysiscommon.addoredit.add"/></c:set>
		<c:set var="editText"><insta:ltext key="generalmasters.dialysiscommon.addoredit.edit"/></c:set>
		<h1>${param._method == 'add' ? addText : editText} <insta:ltext key="generalmasters.dialysismachinemasters.addoredit.dialysismachine"/></h1>
		<insta:feedback-panel/>
		<c:if test="${param._method == 'show'}">
			<input type="hidden" name="machine_id" value="${bean.map.machine_id}"/>

		</c:if>
		<c:set var="status">
 			<insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 			<insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>,
 			<insta:ltext key="generalmasters.dialysismachinemasters.list.temporarilydown"/>
		</c:set>
		<c:set var="Machinetypeapplicable">
 			<insta:ltext key="generalmasters.dialysismachinemasters.addoredit.machinetypenotapplicable"/>,
 			<insta:ltext key="generalmasters.dialysismachinemasters.addoredit.machinetype.nikkiso"/>,
 			<insta:ltext key="generalmasters.dialysismachinemasters.addoredit.machinetype.nikkiso06"/>
		</c:set>
		<c:set var="selectText">
			<insta:ltext key="registration.patient.commonselectbox.patientGender.defaultText"/>
		</c:set>
		<fieldset class="fieldsetborder">

			<table class="formTable">
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.machinename"/></td>
					<td>
						 <input type="text" name="machine_name" value="${bean.map.machine_name}"
						 	class="required" title='<insta:ltext key="generalmasters.dialysismachinemasters.addoredit.dialysismachinemasterisrequired"/>' maxlength="30"/>
					</td>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.machinetype"/></td>
					<td>
						<input type="text" name="machine_type" value="${bean.map.machine_type}">
					</td>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.machinetyperequired"/></td>
					<td>
						 <insta:selectoptions name="model_number" value="${bean.map.model_number}" opvalues="N,DBB27,DBB06" optexts="${Machinetypeapplicable}" />
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.status"/></td></td>
					<td>
						 <insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,T,I" optexts="${status}"/>
					</td>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.location"/></td></td>
					<td>
						<select name="location_id" id="location_id" class="dropdown">
							<option value="">${selectText}</option>
							<c:forEach items="${locations}" var="location">
								<option value="${location.location_id}"
									${bean.map.location_id == location.location_id ? 'selected' : ''}>${location.location_name}</option>
							</c:forEach>
						</select>
					</td>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.machinenumber"/></td>
					<td>
						<input type="text" name="d_no" value="${bean.map.d_no}">
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.networkaddress"/></td></td>
					<td>
						<input type="text" name="network_address" value="${bean.map.network_address }" maxlength="50"/>
					</td>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.networkport"/></td></td>
					<td>
						<input type="text" name="network_port" value="${bean.map.network_port }" maxlength="50"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.remarks"/></td></td>
					<td>
						 <textarea name="remarks" cols="16" rows="3"/>${bean.map.remarks}</textarea>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
			</table>

		</fieldset>

		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" onclick="return modelvalidate();"><b><u><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.s"/></u></b><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.ave"/></td></button>
			<c:if test="${param._method=='show'}">
				<td>&nbsp;|&nbsp;</td>
				<td><a href="#" onclick="window.location.href='${cpath}/master/DialMachMaster.do?_method=add'"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.add"/></a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="generalmasters.dialysismachinemasters.addoredit.dialysismachinelist"/></td></a></td>
		</tr>
		</table>
	</form>
</body>
</html>
