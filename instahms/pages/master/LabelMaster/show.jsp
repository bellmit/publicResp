<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title>
         <insta:ltext key="generalmasters.labelmaster.show.title"/>
</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var chkLabelName = <%= request.getAttribute("labelsList") %>;
	var backupName = '';
	function keepBackUp(){
		if(document.labelMaster._method.value == 'update'){
				backupName = document.labelMaster.label_short.value;
		     }
		}

	function validate() {
		var labelName = document.getElementById('label_short').value.trim();
		if (empty(labelName)) {
		    alert(getString("js.generalmasters.labelmaster.show.labelname.required"));
			document.getElementById('label_short').focus();
			return false;
		}
	   var objDesc = document.getElementById('label_msg').value.trim();
	   var length = objDesc.length;
	   var fixedLen = 1000;
	   if (length > fixedLen) {
		  alert(getString("js.generalmasters.labelmaster.show.labelmsg.length.check")+" "+fixedLen+" "+getString("js.generalmasters.labelmaster.show.labelmsg.length.check.character"));
		  document.labelMaster.label_msg.focus();
		  return false;
	   }

		if (!checkDuplicate()) return false;

		return true;
	}

	function checkDuplicate() {

		var newlabelName = trimAll(document.labelMaster.label_short.value);

		if(document.labelMaster._method.value != 'update'){
			for(var i=0;i<chkLabelName.length;i++){
				item = chkLabelName[i];
				if (newlabelName == item.LABEL_SHORT){
					showMessage("js.generalmasters.labelmaster.show.labelname.checkduplicate");
			    	document.labelMaster.label_short.value='';
			    	document.labelMaster.label_short.focus();
			    	return false;
				}
			}
		}

		if(document.labelMaster._method.value == 'update'){
		  		if (backupName != newlabelName){
					for(var i=0;i<chkLabelName.length;i++){
						item = chkLabelName[i];
						if(newlabelName == item.LABEL_SHORT){
							showMessage("js.generalmasters.labelmaster.show.labelname.checkduplicate");
					    	document.labelMaster.label_short.focus();
					    	return false;
		  				}
		  			}
		 		}
		 	}
		return true;
	}
</script>
<insta:js-bundle prefix="generalmasters.labelmaster"/>
</head>
<c:set var="labelmsg">
 <insta:ltext key="generalmasters.labelmaster.show.labelmsg"/>
</c:set>
<c:set var="status">
   <insta:ltext key="generalmasters.labelmaster.list.active"/>,
   <insta:ltext key="generalmasters.labelmaster.list.inactive"/>
</c:set>
<c:set var="add">
 <insta:ltext key="generalmasters.labelmaster.show.add"/>
</c:set>
<c:set var="edit">
 <insta:ltext key="generalmasters.labelmaster.show.edit"/>
</c:set>
<body onload="keepBackUp();">

<form action="LabelMaster.do" method="POST" name="labelMaster">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="label_id" id="label_id" value="${bean.map.label_id}"/>

	<h1>${param._method == 'add' ? add : edit } <insta:ltext key="generalmasters.labelmaster.show.label"/> </h1>
	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel"><insta:ltext key="generalmasters.labelmaster.show.labeldetails"/></legend>

		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.labelmaster.list.labelname"/>:</td>
				<td colspan="2">
					<input type="text" name="label_short" id="label_short" value="<c:out value="${bean.map.label_short}"/>" maxlength="50" class="required" title="${labelmsg}"><span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.labelmaster.list.status"/></td>
				<td colspan="2"><insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="${status}" /></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.labelmaster.list.labeldescription"/>:</td>
				<td colspan="2">
					<textarea name="label_msg" id="label_msg" cols="30" rows="5" title='<insta:ltext key="generalmasters.labelmaster.list.labeldescription"/>'><c:out value="${bean.map.label_msg}"/></textarea>
				</td>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u><insta:ltext key="generalmasters.labelmaster.show.s"/></u></b><insta:ltext key="generalmasters.labelmaster.show.ave"/></button>
		<c:if test="${param._method=='show'}">| <a href="LabelMaster.do?_method=add" ><insta:ltext key="generalmasters.labelmaster.list.add"/></a></c:if>
		| <a href="LabelMaster.do?_method=list&sortOrder=label_id&sortReverse=false&status=A"><insta:ltext key="generalmasters.labelmaster.show.labellist"/></a>
	</div>
</form>

</body>
</html>
