<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Department - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<c:set var="pagePath" value="<%=URLRoute.DEPARTMENT_PATH %>"/>
<script>
		var chkDeptList =${ifn:convertListToJson(departments)};
		var hiddenDeptId = '${bean.dept_id}';
		function doClose() {
			console.log("Entered");
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=dept_name" +
							"&sortReverse=false&status=A";
		}
		
		function checkduplicate(){
			var newDeptName = trimAll(document.editDepartmentForm.dept_name.value);
			for(var i=0;i<chkDeptList.length;i++){
				item = chkDeptList[i];
				if(hiddenDeptId!=item.dept_id){
				   var actualDeptName = item.dept_name;
				    if (newDeptName.toLowerCase() == actualDeptName.toLowerCase()) {
				    	alert(document.editDepartmentForm.dept_name.value+" already exists pls enter other name");
				    	document.editDepartmentForm.dept_name.value='';
				    	document.editDepartmentForm.dept_name.focus();
				    	return false;
				    }
			     }
			}
      }

	function focus(){
		document.editDepartmentForm.dept_name.focus();
	}
	
	function autoCompleteForDepartmentNames() {
		var datasource = new YAHOO.util.LocalDataSource({result: chkDeptList});
		datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "dept_name"},{key : "dept_id"} ]
		};
		var rAutoComp = new YAHOO.widget.AutoComplete('dept_name','dept_name_container', datasource);
		rAutoComp.minQueryLength = 0;
	 	rAutoComp.maxResultsDisplayed = 20;
	 	rAutoComp.forceSelection = true ;
	 	rAutoComp.animVert = false;
	 	rAutoComp.resultTypeList = false;
	 	rAutoComp.typeAhead = false;
	 	rAutoComp.allowBroserAutocomplete = false;
	 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
		rAutoComp.autoHighlight = true;
		rAutoComp.useShadow = false;
	 	if (rAutoComp._elTextbox.value != '') {
				rAutoComp._bItemSelected = true;
				rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
		}
	 	rAutoComp.itemSelectEvent.subscribe(setIds);
	}

	function setIds(oself, elItem) {
		document.searchKeyForm.dept_id.value = elItem[2].dept_id;
	}

	function checkEmpty() {
		var id = document.searchKeyForm.dept_name;
		id.blur();
		if (id.value == '') {
			alert("Please enter the search value");
			id.focus();
			return false;
		}
		return true;
	}
</script>

</head>
<body onload="focus();autoCompleteForDepartmentNames();">
<h1 style="float:left">Edit Department</h1>
<form name="searchKeyForm" action="show.htm"  >
	<input type="hidden" name="dept_id" id="dept_id" value=""/>
	<table style="float:right; padding-top:8px">
		<tr>
			<td>Enter Name:</td>
			<td>
				<div id="autocomplete" style="width: 100px; padding-bottom: 20px">
					<input type="text"  id="dept_name"  >
					<div id="dept_name_container" style="right: 0px; width: 200px"></div>
				</div>
			</td>
			<td><input type="submit" value="Get Details" onclick="return checkEmpty();"></td>
		</tr>
	</table>

</form>
<div style="clear: both"></div>    

<form action="update.htm" name="editDepartmentForm" method="POST">
	<input type="hidden" name="dept_id" value="${bean.dept_id}"/>
	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Department: </td>
			<td>
				<input type="text" name="dept_name"  value="${bean.dept_name}" onblur="capWords(dept_name);checkduplicate();"
					class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Department Type: </td>
			<td><select name="dept_type_id" class="dropdown">
				<option value="">---Select--</option>
				<c:forEach items="${departmenttypes}" var="departmentTypeList">
					<option value="${departmentTypeList.dept_type_id}" ${bean.dept_type_id == departmentTypeList.dept_type_id ? 'selected' : ''}>${departmentTypeList.dept_type_desc }</option>
				</c:forEach>
				</select>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status: </td>
			<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
		<tr>
			<td class="formlabel">Allowed Gender: </td>
			<td><insta:selectoptions name="allowed_gender" optexts="All,Male,Female" opvalues="ALL,M,F" value="${bean.allowed_gender}"/></td>
		</tr>
		<tr>
			<td class="formlabel">Cost Center Code: </td>
			<td><input type="text" name="cost_center_code" id="cost_center_code" value="${bean.cost_center_code}"/></td>
		</tr>
		<tr>
		 	<td class="formlabel">Send Referral Doctor as Ordering Clinician</td>
		 	<c:choose>
		 		<c:when test="${roleId==1||roleId==2}">
		 			<td><insta:radio name="is_referral_doc_as_ordering_clinician" radioValues="Y,N" value="${bean.is_referral_doc_as_ordering_clinician}" radioText="Yes,No" /></td>
		 		</c:when>
		 		<c:otherwise>
		 			<td><insta:radio name="is_referral_doc_as_ordering_clinician" radioValues="Y,N" value="${bean.is_referral_doc_as_ordering_clinician}" radioText="Yes,No" disabled="true"/></td>
		 		</c:otherwise>
		 	</c:choose>
		</tr>

	</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/${pagePath}/add.htm">Add</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose()">Department List</a></td>
		</tr>
	</table>

</form>
</body>
</html>
