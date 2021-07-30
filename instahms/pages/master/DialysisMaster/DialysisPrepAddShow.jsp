<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="generalmasters.dialysisprepmaster.addoredit.addoreditdialysisprepdetails"/></title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var prepParamAndParamIds = <%= request.getAttribute("prepParamAndParamIds") %>
	var backupName = '';
	var backupPrepState = '';

	<c:if test="${param._method != 'add'}">
   	 	Insta.masterData=${prepParamAndParamIds};
  	</c:if>


	function keepBackUp(){
		if(document.DialysisPrepMasterForm._method.value == 'update'){
				backupName = document.DialysisPrepMasterForm.prep_param.value;
				backupPrepState = document.DialysisPrepMasterForm.prep_state.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/DialysisPrepMaster.do?_method=list&sortOrder=prep_param&sortReverse=false&status=A";
	}

	function validateForm() {
		var prepParam = document.DialysisPrepMasterForm.prep_param.value;
		var prepState = document.DialysisPrepMasterForm.prep_state.value;
		if (empty(prepParam)) {
			showMessage("js.dialysismodule.commonvalidations.prepparam.required");
			document.getElementById('prep_param').focus();
			return false;
		}

		if (empty(prepState)) {
			showMessage("js.dialysismodule.commonvalidations.prepstate.required");
			document.getElementById('prep_state').focus();
			return false;
		}
		if(!checkDuplicate())
			return false;

		return true;
	}


	function checkDuplicate(){
		var newParamName = trimAll(document.DialysisPrepMasterForm.prep_param.value);
		var newPrepState = trimAll(document.DialysisPrepMasterForm.prep_state.value);
		var paramId = document.DialysisPrepMasterForm.prep_param_id.value;
		if(document.DialysisPrepMasterForm._method.value != 'update'){
			for(var i=0;i<prepParamAndParamIds.length;i++){
				item = prepParamAndParamIds[i];
				if(newParamName == item.prep_param && newPrepState == item.prep_state){
					var msg=document.DialysisPrepMasterForm.prep_param.value;
					msg+=getString("js.dialysismodule.commonvalidations.prepstate.exists");
					msg+=newPrepState;
					msg+=getString("js.dialysismodule.commonvalidations.prepstate.enterother");
					alert(msg);
			    	document.DialysisPrepMasterForm.prep_param.value='';
			    	document.DialysisPrepMasterForm.prep_param.focus();
			    	return false;
				}

			}
		}
	 	if(document.DialysisPrepMasterForm._method.value == 'update'){
	  		if (backupName != newParamName || backupPrepState != newPrepState){
				for(var i=0;i<prepParamAndParamIds.length;i++){
					item = prepParamAndParamIds[i];
					if(newParamName == item.prep_param && newPrepState == item.prep_state){
					var msg=document.DialysisPrepMasterForm.prep_param.value;
					msg+=getString("js.dialysismodule.commonvalidations.prepstate.exists");
					msg+=newPrepState;
					msg+=getString("js.dialysismodule.commonvalidations.prepstate.enterother");
					alert(msg);
				    return false;
	  				}
	  			}
	 		}
	 	}
	 	return true;
	}//end of function

</script>
	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
</head>
<body onload="keepBackUp();">
<c:set var="dummy">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>

<c:set var="status">
 <insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 <insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
</c:set>

<c:set var="prep">
 <insta:ltext key="generalmasters.dialysisprepmaster.list.pre"/>,
 <insta:ltext key="generalmasters.dialysisprepmaster.list.post"/>
</c:set>
<c:choose>
     <c:when test="${param._method != 'add'}">
        <h1 style="float:left"><insta:ltext key="generalmasters.dialysisprepmaster.addoredit.editdialysisprepdetails"/></h1>
	    <c:url var="searchUrl" value="/master/DialysisPrepMaster.do"/>
	    <insta:findbykey keys="prep_param,prep_param_id" method="show" fieldName="prep_param_id" url="${searchUrl}" />
     </c:when>
     <c:otherwise>
        <h1><insta:ltext key="generalmasters.dialysisprepmaster.addoredit.adddialysisprepdetails"/></h1>
     </c:otherwise>
</c:choose>


<form action="DialysisPrepMaster.do" method="POST" name="DialysisPrepMasterForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="prep_param_id" value="${bean.map.prep_param_id}"/>

	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.dialysisprepmaster.addoredit.prepparam"/></td>
				<td>
					<input type="text" name="prep_param" id="prep_param" value="${bean.map.prep_param}" maxlength="100"/>
				</td>
				<td colspan="5">&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.dialysisprepmaster.addoredit.prepstate"/></td>
				<td>
					<select id="prep_state" name="prep_state" class="dropdown">
						<option value="">${dummy}</option>
						<option value="pre" ${bean.map.prep_state == 'pre' ? 'selected' : ''}><insta:ltext key="generalmasters.dialysisprepmaster.list.pre"/></option>
						<option value="post" ${bean.map.prep_state == 'post' ? 'selected' : ''}><insta:ltext key="generalmasters.dialysisprepmaster.list.post"/></option>
					</select>
				</td>
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.dialysisprepmaster.addoredit.status"/></td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="${status}" /></td>
			</tr>
		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u><insta:ltext key="generalmasters.dialysisprepmaster.addoredit.s"/></u></b><insta:ltext key="generalmasters.dialysisprepmaster.addoredit.ave"/></td></button>
			<c:if test="${param._method=='show'}">
				<td>&nbsp;|&nbsp;</td>
				<td><a href="#" onclick="window.location.href='${cpath}/master/DialPrepMaster.do?_method=add'"><insta:ltext key="generalmasters.dialysisprepmaster.addoredit.add"/></a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="generalmasters.dialysisprepmaster.addoredit.dialysispreplist"/></td></a></td>
		</tr>
		</table>
</form>

</body>
</html>
