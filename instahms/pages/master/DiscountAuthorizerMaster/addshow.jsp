<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Discount Authorizer Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
	<script>

		var backupName = '';
		var centerLists = '${centerLists}';
		var centerIds = '${centerIds}';
        var max_centers_inc_default = ${max_centers_inc_default};

		function keepBackUp(){
            document.getElementById("multiCenterDiv").style.display = '';
            document.getElementById("applicable_for_centers").checked = true;
	                 onCheckApplForAllCenters();
			if(document.discountAuthorizerForm._method.value == 'update'){
				backupName = document.discountAuthorizerForm.disc_auth_name.value;
				var multiselectedVal = document.getElementById("multicenterid");

                 if (centerIds != "0"){
                  	 onCheckChooseCenters();
                     document.getElementById("choose_centers").checked = true;
	                 var centerIdsArray =centerIds.split(',');
					 setMultipleSelectedIndexs(multiselectedVal,centerIdsArray);

					}

			}
		}

       function onCheckChooseCenters(){
	       document.getElementById("multiCenterDiv").style.display = '';
	       var check = document.getElementById("applicable_for_centers");
	       if (check.checked == true)
	       		check.checked = false;
       }

       function onCheckApplForAllCenters(){
	       	var check = document.getElementById("choose_centers");
	       	if (check.checked == true){
	       		check.checked = false;
	       	}
	       document.getElementById("multiCenterDiv").style.display = 'none';
       }

		function doClose() {
			window.location.href = "${cpath}/master/DiscountAuthorizerMaster.do?_method=list&sortOrder=disc_auth_name" +
							"&sortReverse=false&status=A";
		}

		var authorizersList = <%= request.getAttribute("authorizersList") %>;
		var auth_id = '${bean.map.disc_auth_id}';

		function checkduplicate(){
			var newAuthName = trimAll(document.discountAuthorizerForm.disc_auth_name.value);
			for(var i=0;i<authorizersList.length;i++){
				item = authorizersList[i];
				if(auth_id!=item["disc_auth_id"]){
				   var actualAuthName = item["disc_auth_name"];
				    if (newAuthName == actualAuthName) {
				    	alert(document.discountAuthorizerForm.disc_auth_name.value+" already exists pls enter other name");
				    	document.discountAuthorizerForm.disc_auth_name.focus();
				    	return false;
				    }
			     }
			}
			var chooseCenChk = document.getElementById("choose_centers").checked;
			if(chooseCenChk == true){
				var el = document.getElementById("multicenterid");
				if( el.value == ""){
				    alert("Please choose atleast one center OR select for all centers");
				    return false;
				}
			}
			document.discountAuthorizerForm.submit();
      }

      <c:if test="${param._method != 'add'}">
            Insta.masterData=${discountsLists};
      </c:if>
	</script>

</head>
<body onload="keepBackUp(); document.getElementById('disc_auth_name').focus();">
<c:choose>
    <c:when test="${param._method != 'add'}">
         <h1 style="float:left">Edit Discount Authorizer</h1>
         <c:url var="searchUrl" value="/master/DiscountAuthorizerMaster.do"/>
         <insta:findbykey keys="disc_auth_name,disc_auth_id" fieldName="disc_auth_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Discount Authorizer</h1>
    </c:otherwise>
</c:choose>

<form action="DiscountAuthorizerMaster.do" name="discountAuthorizerForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="disc_auth_id" value="${bean.map.disc_auth_id}"/>
	</c:if>

	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
		<table class="formtable" >
			<tr>
				<td class="formlabel">Authorizer Name:</td>
				<td>
					<input type="text" name="disc_auth_name" id="disc_auth_name" value="${bean.map.disc_auth_name}" class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Status</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>

				<tr style="${max_centers_inc_default > 1 ?'':'display:none' }">
					<td>
					<div style="float: right"><input type="radio" name="applicable_for_centers" id="applicable_for_centers" value="0" onchange="onCheckApplForAllCenters();"/></div></td>
					<td>Applicable For All Centers</td>
				</tr>
				<tr  style="${max_centers_inc_default > 1 ?'':'display:none' }">
					<td><div style="float: right">OR</div></td>
				</tr>
				<tr  style="${max_centers_inc_default > 1 ?'':'display:none' }">
					<td>
						<div style="float: right">
							<input type="radio" id="choose_centers" value="choose_centers" onchange="onCheckChooseCenters();"/>
						</div>
					</td>
					<td>Applicable Only For Following Centers</td>
				</tr>

		       <tr id="multiCenterDiv" style="display:none;">
					<td>&nbsp;</td>
					<td>
						<select name="multicenterid" id="multicenterid" class="listbox" style="width: 200px" multiple="multiple" >
							<c:forEach var="center" items="${centerLists}">
								<option value="${center.map.center_id}">${center.map.center_name}</option>
							</c:forEach>
						</select>
					</td>
				</tr>

		</table>
	</fieldset>

	<div class="screenActions">
		<button type="button" accesskey="S" onclick="return checkduplicate();"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
		<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/DiscountAuthorizerMaster.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();">Authorizers List</a>
	</div>

</form>
</body>
</html>
