<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Out House - Insta HMS</title>

	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script>
	    var method = '${ifn:cleanHtmlAttribute(param._method)}';
	    var templateNamesToDisplay = '${ifn:cleanJavaScript(templateNames)}';
	    var templateNameArray = templateNamesToDisplay
	      .substring(1,templateNamesToDisplay.length-1)
	      .split(', ')
	      .map((s) => {
            let pair = s.split('=');
            let result = {};
            let key = pair[0].substring(1);
            let value = pair[1].substring(0,pair[1].length-1);
            result[key] = value;
            result['id'] = value;
            return result;
        })
	    templateNameArray.push({'TEMPLATE_NAME': 'Built-in Default Template', 'id': 'BUILTIN_HTML'});
	    templateNameArray.push({'TEMPLATE_NAME': 'Built-in Text Template', 'id': 'BUILTIN_TEXT'});

	    templateNameArray.sort((tempName1, tempName2) => {
	      if(tempName1.TEMPLATE_NAME < tempName2.TEMPLATE_NAME) {
	        return -1;
	      }
	      if(tempName1.TEMPLATE_NAME > tempName2.TEMPLATE_NAME) {
	        return 1;
          }
          return 0;
	    });

	    function populateTemplateNames() {
	      var templateNameDropDown = document.getElementById("template_name");
            for(var i = 0; i < templateNameArray.length; i++) {
                var option = templateNameArray[i];
                var element = document.createElement("option");
                element.textContent = option.TEMPLATE_NAME;
                element.value = option.id;
                templateNameDropDown.appendChild(element);
            }
	    }
	    
		function init() {
			setSelectedIndex(document.getElementById("template_name"),'${ifn:cleanJavaScript(templateName)}');
			if(method == 'getOutHouseDetails'){
				var instaouthouse = '${outSourceDestBean.map.outsource_dest_type}';
				if(instaouthouse =='IO'){
					var selectprotocol=document.getElementById("protocol");
					for (var i=0; i<selectprotocol.length; i++){
						  if (selectprotocol.options[i].value == '' )
							  selectprotocol.remove(i);
				    }
				}
			}
			populateTemplateNames();
		}

		function validateForm() {
			if(trim(document.getElementById("oh_name").value)=='') {
				alert("Please enter outhouse name.");
				document.getElementById("oh_name").value = '';
				document.getElementById("oh_name").focus();
				return false;
			}
			if(document.outHouseForm.insta_outhouse.checked && document.getElementById("protocol").value == ''){
				alert("Please select protocol for Insta Outhouse");
				document.getElementById("protocol").focus();
				return false;
			}
			if(document.getElementById("protocol").value == 'hl7' && document.outHouseForm.hl7_interface.value == ''){
				alert("Please select Interface Name for Protocol hl7");
				document.getElementById("hl7_interface").focus();
				return false;
			}
			var status = document.getElementById("status").value;
			if(activeOutHouseTestsExist == 'true'  && status == 'I'){
				alert("There are tests associated with this outhouse. \nPlease inactivate those associations.");
				return false;
			}
			return true;
		}
		
		function selectProtocol(){
			var selectprotocol=document.getElementById("protocol");
			if(document.outHouseForm.insta_outhouse.checked){
				setSelectedIndex(selectprotocol,'hl7');
			  	for (var i=0; i<selectprotocol.length; i++){
				  if (selectprotocol.options[i].value == '--Select--' || selectprotocol.options[i].value == '' )
					  selectprotocol.remove(i);
				  }
			}else{
				var optionexist = false;
				for (var i=0; i<selectprotocol.length; i++){
					  if (selectprotocol.options[i].value == '--Select--' || selectprotocol.options[i].value == '')
						  optionexist = true;
					  break;
					  }
				if(!optionexist){
					var option = document.createElement("option");
				    option.text = '--Select--';
					selectprotocol.add(option);
					setSelectedIndex(selectprotocol,'--Select--');
				}	
			}
		}
		var activeOutHouseTestsExist = '${activeOutHouseTestsExist}';

	</script>
</head>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<body onload="init();" class="yui-skin-sam">
	<div class="pageHeader">${ifn:cleanHtml(param._method=='addNewOutHouse'?'Add':'Edit')} Out House</div>
	<insta:feedback-panel/>
	<form name="outHouseForm" method="POST" action="${cpath}/pages/masters/hosp/diagnostics/OutHouseMaster.do">
		<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(param._method=='addNewOutHouse'?'addNewOutHouseDetails':'updateOutHouseDetails')}">
		<input type="hidden" name="oh_id" id="oh_id" value="${bean.map.oh_id}"/>
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Out House Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Out House Name:</td>
					<td><input type="text" name="oh_name" id="oh_name" value="${bean.map.oh_name}" /></td>
					<td class="formlabel">CLIA No:</td>
					<td>
						<input type="text" name="clia_no" id="clia_no" value="${bean.map.clia_no}"/>
					</td>
				</tr>
				<tr>
				<td class="formlabel">Out House Print Template:</td>
				<td>
						<select id="template_name" name="template_name" Class="dropdown">
						</select>
					</td>

					<td class="formlabel">Status:</td>
					<td>
						<insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" />
					</td>
				</tr>
				<tr>
					<td class="formlabel">Address:</td>
					<td>
						<textarea name="oh_address" id="oh_address" style="width:200px">${bean.map.oh_address}</textarea>
					</td>
					<td class="formlabel">Insta Outhouse:</td>
						<td><input type="checkbox" name="insta_outhouse" value="IO" ${outSourceDestBean.map.outsource_dest_type == 'IO'?'checked':'' } onchange="selectProtocol()" ></td> 
				</tr>
				<tr>
					<td class="formlabel">Protocol:</td>
					<td>
						<insta:selectoptions name="protocol" id="protocol" value="${bean.map.protocol}" opvalues="hl7" optexts="Hl7"  dummyvalue="--Select--" />
					</td>
					<td class="formlabel">Interface Name:</td>
					<td>
						<insta:selectdb table="hl7_lab_interfaces" valuecol="hl7_lab_interface_id" displaycol="interface_name"
							value="${bean.map.hl7_lab_interface_id}" filtercol="status" filtered="true"
							filtervalue="A" name="hl7_lab_interface_id" id="hl7_interface" dummyvalue="--Select--"  />					
					</td>
				</tr>
			</table>
		</fieldset>
		<div class="screenActions">
				<button type="submit" name="save"  accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>
				<c:if test="${param._method=='getOutHouseDetails'}">|
					<a href="${cpath}/pages/masters/hosp/diagnostics/OutHouseMaster.do?_method=addNewOutHouse">Add</a>
				</c:if>
		| <a href="${cpath}/pages/masters/hosp/diagnostics/OutHouseListMaster.do?_method=getOutHouseList&sortOrder=outsource_name
			&sortReverse=false&status=A">OutSource List</a>
		</div>
	</form>
	</body>
</html>
