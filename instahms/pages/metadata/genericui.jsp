<%@ taglib uri="/WEB-INF/taglibs-datagrid.tld" prefix="ui"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>

<head>
<title>Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="date_go.js" />


<script>
		var deptWiseForms = <%= request.getAttribute("deptWiseForms")%>;
		var gServerNow;

		/*
		 *	populates all the form names available for the selected departement.
		 */
		function populateForms(dept){

			if(dept == ""){
				alert("please select the department");
				document.forms[0].deptName.focus();
				return false;
			}else{
				var k=1;
				var selObj = document.forms[0].formName;


					selObj.length = 1;
					selObj.options[0].text = "--------select---------";
					selObj.options[0].value = "";

					for (var i=0; i<deptWiseForms.length; i++) {
					var deptform = deptWiseForms[i];
						if(dept == deptform.DEPT_ID){

							selObj.length = k+1;
							selObj.options[k].text = deptform.FORM_TITLE;
							selObj.options[k].value = deptform.FORM_CODE;
							document.forms[0].formdescription.value=deptform.FORM_DESCRIPTION;
							k++;
						}
					}

			}
		}

		/*
		 * makes the ajax request to get the selected form data
		 */
		function getSelectedForm(){
			var dept = document.forms[0].deptName.options[document.forms[0].deptName.selectedIndex].value;
			var formCode = document.forms[0].formName.options[document.forms[0].formName.selectedIndex].value;
			if(dept == ""){
				alert("please select the department");
				document.forms[0].deptName.focus();
				return false;
			}
			if(formCode == ""){
				alert("please select the form");
				document.forms[0].formName.focus();
				return false;
			}
			var ajaxreq = newXMLHttpRequest();
			var url = "<%=request.getContextPath()%>/pages/metadata/genericui.do?method=getFormData&formCode="+formCode+"&dept="+dept;
			getResponseHandlerText(ajaxreq, displayForm, url);
		}

		var noOfDtFieldsArr = new Array(); // this is used to hold the no of date fields present in form.
		//and this will be used to validate the date fields at the time of submitting form.

		var dateFieldsIndex = 0; // array length of noOfDtFieldsArr. after submitting reinitialize the value to 0.

		/*
		 * used to display the form data
		 */
		function displayForm(responseData) {
			var tableObj = document.getElementById("displayTable");


			while(tableObj.childNodes.length > 0){
				tableObj.removeChild(tableObj.childNodes[0]);
			}

			var sendBtnObj = document.getElementById('send');
			var clearBtnObj = document.getElementById('clear');
			if(sendBtnObj != null){
				var parent = sendBtnObj.parentNode.parentNode;
				parent.removeChild(sendBtnObj.parentNode);

				var clearPar = clearBtnObj.parentNode.parentNode;
				clearPar.removeChild(clearBtnObj.parentNode);
			}

			var jsonExpression = "(" + responseData + ")";
			var formData = eval(jsonExpression);
			var heading = "";
			var subheading = "";
			var tdObj = "", trObj = "";
			var headerTable = "";
			var hTdObj = "", hTrObj = "";
			var fieldSet = "";

			for (var i=0; i<formData.length; i++) {
			for (var j=0; j<deptWiseForms.length; j++) {
			if(document.forms[0].formName.options[document.forms[0].formName.selectedIndex].value==deptWiseForms[j].FORM_CODE){
			document.forms[0].formdescription.value=deptWiseForms[j].FORM_DESCRIPTION;
			}
			}
				if (i == 0) {
					var tableHeader = document.createElement("th");
					tableHeader.setAttribute("align", "center");
					var formCaption = document.forms[0].formdescription.value;
					var headerText = document.createTextNode(formCaption);
					tableHeader.appendChild(headerText);
					tableObj.appendChild(tableHeader);
				}

				var formDetails = formData[i];
				if (heading == formDetails.HEADING_ID) {
				} else{
					trObj = tableObj.insertRow(-1);
					tdObj = trObj.insertCell(-1);
					fieldSet = document.createElement("fieldset");
					var legend = document.createElement("legend");
					legend.appendChild(document.createTextNode(formDetails.HEADING_LABEL));
					fieldSet.appendChild(legend);
					headerTable = document.createElement("TABLE");
					hTrObj = headerTable.insertRow(-1);
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '&nbsp;';
				}
				hTrObj = headerTable.insertRow(-1);
				if ((subheading == formDetails.SUBHEADING_ID) || (formDetails.HAS_SUBHEADING == 'N')) {
				} else{
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '&nbsp;';
					hTrObj = headerTable.insertRow(-1);
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<span class="fieldSetLabel">' + formDetails.SUBHEADING_LABEL + '</span>';
					hTrObj = headerTable.insertRow(-1);
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '&nbsp;';
					hTrObj = headerTable.insertRow(-1);
				}
				tdObj.appendChild(fieldSet);
				fieldSet.appendChild(headerTable);
				insertAttributes(formDetails, hTrObj,headerTable);
				heading = formDetails.HEADING_ID;
				subheading = formDetails.SUBHEADING_ID;

			}
			if (formData.length > 0) {
					var mainTabObj = document.getElementById("buttontable");
					var mTrObj = mainTabObj.insertRow(-1);
					mTdObj = mTrObj.insertCell(-1);
					mTdObj.innerHTML = '<input type="submit" name="send" id="send" value="submit" />';
					mTdObj = mTrObj.insertCell(-1);
					mTdObj.innerHTML = '<input type="button" name="clear" id="clear" value="clear" onclick="return clearAllFields();"/>'

			}
		}

		/*
		 * This method is used to clear the all form field values.
		 */
		function clearAllFields(){
			var _Els = document.metadata.elements;

			for(var i=0; i<_Els.length; i++){
				var _el = _Els[i];
				var type = _el.type;
				var name = _el.name;
				if (type == "text") {
					_el.value = "";
				} if (type == "checkbox") {
					_el.checked = false;
				} if (type == "radio") {
					_el.checked = false;
				} if (type ==  "select-one") {
					if((name == "formName") || (name == "deptName")) {

					} else {
						_el.selectedIndex = 0;
					}
				} if (type == "textarea") {
					_el.value = "";
				}

			}
		}

		/*
		 * used to insert the columns into the perticaular row based on the attribute types
		 */
		function insertAttributes(formDetails, hTrObj,headerTable){
			switch (formDetails.ATTRIBUTE_TYPE) {

				case "TEXT_1VALUE" :
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<span class="label">' + formDetails.DISPLAY_TEXT + '&nbsp;</span>'
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<input type="text" name="'+ formDetails.ATTRIBUTE_ID +'" id="'+ formDetails.ATTRIBUTE_ID +'" class="forminput">';

					if (formDetails.NOTES == 'Y') {
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '<span class="label">&nbsp;&nbsp;&nbsp;&nbsp;Notes</span>';
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '&nbsp;<input type="text" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput"/>'
					}
					break;

				case "TEXT_2_VALUES" :
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<span class="label">' + formDetails.DISPLAY_TEXT + '&nbsp;:&nbsp;</span>'
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<input type="text" name="'+ formDetails.ATTRIBUTE_ID +'" id="'+ formDetails.ATTRIBUTE_ID +'" class="forminput">' +
										'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
					hTrObj = headerTable.insertRow(-1);
					hTdObj = hTrObj.insertCell(-1);
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML ='<input type="text" name="'+ formDetails.ATTRIBUTE_ID +'" id="'+ formDetails.ATTRIBUTE_ID +'" class="forminput">';

					if (formDetails.NOTES == 'Y') {
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '<span class="label">&nbsp;&nbsp;&nbsp;&nbsp;Notes</span>';
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '&nbsp;<input type="text" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput"/>'
					}
					break;

				case "TEXTWITH_2LABELS_2FIELDS" :
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<span class="label">' + formDetails.DISPLAY_TEXT + '&nbsp;&nbsp;&nbsp;</span>'
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<span class="label">' + formDetails.LABEL1 + '</span>&nbsp;&nbsp;&nbsp' +
										'<input type="text" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput">' +
										'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
					hTrObj = headerTable.insertRow(-1);
					hTdObj = hTrObj.insertCell(-1);
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML='<span class="label">' + formDetails.LABEL2 + '</span> &nbsp;&nbsp;&nbsp' +
										'<input type="text" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput">'

					if (formDetails.NOTES == 'Y') {
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '<span class="label">&nbsp;&nbsp;&nbsp;&nbsp;Notes</span>';
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '&nbsp;<input type="text" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput"/>'
					}
					break;

				case "DROPDOWN" :
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<span class="label">' + formDetails.DISPLAY_TEXT + '&nbsp;&nbsp;&nbsp;</span>'
					var datadicarr = formDetails.DATADIC_VALUE.split(',');
					hTdObj = hTrObj.insertCell(-1);
					var selectbox = '<select name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'">' ;
						selectbox = selectbox + '<option value="">--------select--------</option>'
					for (var i=0; i<datadicarr.length; i++) {
						selectbox = selectbox + '<option value="'+datadicarr[i]+'">' + datadicarr[i] + '</option>'
					}
					selectbox = selectbox + '</select>'
					hTdObj.innerHTML = selectbox;

					if (formDetails.NOTES == 'Y') {
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '<span class="label">&nbsp;&nbsp;&nbsp;&nbsp;Notes</span>';
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '&nbsp;<input type="text" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput"/>'
					}
					break;

				case "CHECKBOX" :
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<span class="label">' + formDetails.DISPLAY_TEXT + '&nbsp;&nbsp;&nbsp;</span>'
					var datadicarr = formDetails.DATADIC_VALUE.split(',');
					hTdObj = hTrObj.insertCell(-1);
					var checkbox = '';
					for (var i=0; i<datadicarr.length; i++) {
						checkbox = checkbox + '<input type="checkbox" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" value="'+datadicarr[i]+'"/>'+datadicarr[i]
						checkbox = checkbox + '&nbsp;&nbsp;&nbsp;';
					}
					hTdObj.innerHTML = checkbox;

					if (formDetails.NOTES == 'Y') {
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '<span class="label">&nbsp;&nbsp;&nbsp;&nbsp;Notes</span>';
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '&nbsp;<input type="text" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput"/>'
					}
					break;

				case "RADIO" :
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<span class="label">' + formDetails.DISPLAY_TEXT + '&nbsp;&nbsp;&nbsp;</span>'
					var datadicarr = formDetails.DATADIC_VALUE.split(',');
					hTdObj = hTrObj.insertCell(-1);
					var radio = '';
					for (var i=0; i<datadicarr.length; i++) {
						radio = radio + '<input type="radio" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" value="'+datadicarr[i]+'"/>'+datadicarr[i];
					}

					hTdObj.innerHTML = radio;
					if (formDetails.NOTES == 'Y') {
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '<span class="label">&nbsp;&nbsp;&nbsp;&nbsp;Notes</span>';
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '&nbsp;<input type="text" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput"/>'
					}
					break;
				case "NUMBER" :
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<span class="label">' + formDetails.DISPLAY_TEXT + '&nbsp;&nbsp;&nbsp;</span>'
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<input type="text" style="width: 90" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput" onkeypress="return enterNumOnlyANDdot(event)"/>'

					if (formDetails.NOTES == 'Y') {
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '<span class="label">&nbsp;&nbsp;&nbsp;&nbsp;Notes</span>';
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '&nbsp;<input type="text" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput" />'
					}
					break;

				case "DATE" :
					hTdObj = hTrObj.insertCell(-1);
					hTdObj.innerHTML = '<span class="label">' + formDetails.DISPLAY_TEXT + '&nbsp;&nbsp;&nbsp;</span>';
					hTdObj = hTrObj.insertCell(-1);

					gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
					var name = formDetails.ATTRIBUTE_ID;
					hTdObj.innerHTML = getDateWidget(name, name, gServerNow,
						null, null, true, false);
					makePopupCalendar(name);
					noOfDtFieldsArr[dateFieldsIndex++] = name;

					if (formDetails.NOTES == 'Y') {
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '<span class="label">&nbsp;&nbsp;&nbsp;&nbsp;Notes</span>';
						hTdObj = hTrObj.insertCell(-1);
						hTdObj.innerHTML = '&nbsp;<input type="text" name="'+formDetails.ATTRIBUTE_ID+'" id="'+formDetails.ATTRIBUTE_ID+'" class="forminput" />'
					}
			}
		}

		/*
		 * this method clears the date field value, if the user enters wrong date
		 */
		function clearIfnotValid(){

			for (var i=0; i<noOfDtFieldsArr.length; i++){
				var dateVal = document.getElementById(noOfDtFieldsArr[i]).value;
				var msg = validateDateStr(dateVal);
				if (msg == null) {

				} else {
					alert("incorrect date or date format");
					document.getElementById(noOfDtFieldsArr[i]).focus();
					return false
				}

			}

			noOfDtFieldsArr.length = 0;
			dateFieldsIndex = 0;
		}


	</script>

</head>
<body>
<form action="<%=request.getContextPath()%>/pages/metadata/genericui.do"
	name="metadata" onsubmit="return clearIfnotValid();"><input
	type="hidden" name="method" value="saveFormData" />
	<input type="hidden" name="formdescription" id="formdescription"/>
<table border="0" class="totalBG" id="mainTable" width="100%" >

	<tr>
		<td  height="2%" class="label">&nbsp; <span
			class="resultMessage"> <logic:present name="msg"
			scope="request">
			<bean:write name="msg" scope="request" />
		</logic:present> </span></td>
	</tr>
	<tr>
		<td><table><tr><td>
		<fieldset  class="fieldSetBorder" style="width: 680; height: 65"><legend
			class="fieldSetLabel">Personal&nbsp;Details</legend> <logic:present
			name="patDetails">
			<logic:notEmpty name="patDetails">
				<logic:iterate name="patDetails" id="patDetails">
					<table border="0">
						<tr>
							<td class="label">&nbsp;MR&nbsp;No.&nbsp;</td>
							<td class="fieldSetLabel">:&nbsp;<bean:write
								name="patDetails" property="MR_NO" /> <input type="hidden"
								name="mrno" id="mrno"
								value="<bean:write name="patDetails" property="MR_NO"/>"
								readonly /></td>
							<td>&nbsp;</td>
							<td class="label">&nbsp;Visit&nbsp;Id&nbsp;</td>
							<td class="fieldSetLabel">:&nbsp;<bean:write
								name="patDetails" property="PATIENT_ID" /> <input type="hidden"
								name="patientid" id="patientid"
								value="<bean:write name="patDetails" property="PATIENT_ID"/>"
								readonly /></td>
							<td>&nbsp;</td>
							<td class="label">&nbsp;Name&nbsp;</td>
							<td class="fieldSetLabel">:&nbsp;<bean:write
								name="patDetails" property="PATIENT_NAME" /></td>
						</tr>
						<tr>
							<td class="label">&nbsp;Age&nbsp;</td>
							<td class="fieldSetLabel">:&nbsp;<bean:write
								name="patDetails" property="PATIENT_AGE" /></td>
							<td>&nbsp;</td>
							<td class="label">&nbsp;Gender&nbsp;</td>
							<td class="fieldSetLabel">:&nbsp;<bean:write
								name="patDetails" property="PATIENT_GENDER" /></td>
							<td>&nbsp;</td>
							<td class="label">&nbsp;Contact&nbsp;No.&nbsp;</td>
							<td class="fieldSetLabel">:&nbsp;<bean:write
								name="patDetails" property="PATIENT_PHONE" /></td>
						</tr>
					</table>
				</logic:iterate>
			</logic:notEmpty>
		</logic:present></fieldset></td></tr></table>
		</td>
	</tr>
	<tr><td  ><fieldset class="fieldSetBorder" style="width: 680; height: 45"><table><tr>
      <td class="label" >Select&nbsp;Department</td>
		<td>&nbsp;</td>
		<td><select id="deptName" name="deptName"
			onchange="return populateForms(this.value);">
			<option value="">----------select---------</option>
			<logic:present name="depts">
				<logic:iterate name="depts" id="depts">
					<option value="<bean:write name="depts" property="DEPT_ID"/>"><bean:write
						name="depts" property="DEPT_NAME" /></option>
				</logic:iterate>
			</logic:present>
		</select></td>
		<td class="label">Select&nbsp;Speciality&nbsp;Template</td>
		<td>&nbsp;</td>
		<td><select id="formName" name="formName"
			onchange="return getSelectedForm();">
			<option value="">--------select--------</option>
		</select></td></tr></table></fieldset></td>
	</tr>
	<tr>
		<td colspan="6">&nbsp;</td>
	</tr>
	<tr>
		<td colspan="6">
		<table border="0" id="displayTable"></table>
		</td>
		<table border="0" id="buttontable"></table>
		</td>
	</tr>
	<tr>
		<td colspan="6">&nbsp;</td>
	</tr>


</table>
</form>
</body>
</html>
