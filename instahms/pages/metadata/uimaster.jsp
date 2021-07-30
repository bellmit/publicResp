<%@ page import="java.util.ArrayList"%>
<%@page import="java.util.ArrayList,java.util.Iterator"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<jsp:include page="/pages/sessionCheck.jsp" />
<html>

<head>

<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="script" file="widgets.js" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="instagrid.js" />
<script type="text/javascript">
	var Heading = '';
	var SubHeading = '';
	var Attributes = '';
	var sum=0;
	var myarray_heading=new Array(10);
	var myarray_subheading=new Array(10);
	var myarray_attributes=new Array(100);
	var serial=0;
    var countofrows=0;
    var heading ;
    var subheading;
    var attribute;
    var attributetype;
    var attributeorder;
    var atttype;
    var dropdowndata;
    var heading_order;
    var subheading_status;
    var label1;
    var label2;
    var formtitle;
    var formDescription;
    var department;
    var headingSubmit ;
    var headingorder;
    var subheadingonsubmit;
    var subheadingorderonsubmit;
    var headingofsubheading;
    var notesStatus ;
    var validations;
    var deptWiseForms = <%= request.getAttribute("deptWiseForms")%>;


	var headingGrid = new Insta.Grid("headingGrid");
	YAHOO.util.Event.addListener(window, "load", function() {headingGrid.init()});
	headingGrid.setSize(370,110);
	var myColumns = [ "Heading","Display Order"];
	headingGrid.setHeaderHeight(20);
	headingGrid.setHeaderText(myColumns);
	headingGrid.setColumnCount(1);
	headingGrid.setRowCount(0);
	headingGrid.setCellEditable(true,1);
	for (var i=0; i<2; i++){
		headingGrid.setColumnWidth(125, i);
	}
	headingGrid.setCellEditable(false);
	headingGrid.onRowAdded = function(row){
		window.status = "Row added: " + row;
		this.setCellText(heading, 0, row);
		this.setCellText(heading_order, 1, row);
		if(subheading_status=="Y"){
		addOptionForHeading(document.forms[0].heading1,heading,heading);
		}
		addOptionForHeadingForAttriute(document.forms[0].attheading,heading,heading);
		document.forms[0].headingcaption.value="";
		document.forms[0].headingorder.value="";
		headingGrid.render();

	}


	var subheadingGrid = new Insta.Grid("subheadingGrid");
	YAHOO.util.Event.addListener(window, "load", function() {subheadingGrid.init()});
	subheadingGrid.setSize(370,110);
	var mySubColumns = [ "Heading","Sub Heading","Display Order"];
	subheadingGrid.setHeaderHeight(20);
	subheadingGrid.setHeaderText(mySubColumns);
	subheadingGrid.setColumnCount(2);
	subheadingGrid.setRowCount(0);
	subheadingGrid.setCellEditable(true,2);
	for (var j=0; j<3; j++){
		subheadingGrid.setColumnWidth(125, j);
	}
	subheadingGrid.setCellEditable(false);
	subheadingGrid.onRowAdded = function(row){
		window.status = "Row added: " + row;
		this.setCellText(document.forms[0].heading1.value, 0, row);
		this.setCellText(subheading, 1, row);
		this.setCellText(displayorder, 2, row);
		addOptionForSubHeading(document.forms[0].attsubheading,subheading,subheading);
		subheadingGrid.render();

	}


	var attributeGrid=new Insta.Grid("attributeGrid");
	YAHOO.util.Event.addListener(window,"load",function(){attributeGrid.init()});
	attributeGrid.setSize(370,110);
	var myAttColumns = ["Display Order","Form Name","Department","Heading","Sub Heading","Attribute ","Attribute Type","Data Group","Label1","Label2"];
	attributeGrid.setHeaderHeight(20);
	attributeGrid.setHeaderText(myAttColumns);
	attributeGrid.setRowCount(1);
	attributeGrid.setCellEditable(true,0);
    for(var k =0;k<6;k++){
       attributeGrid.setColumnWidth(125,k);
    }
	attributeGrid.onRowAdded = function(row){
		window.status = "Row added: " + row;
		this.setCellText(attributeorder,0,row);
		this.setCellText(document.forms[0].formtitle.value, 1, row);
		this.setCellText(document.forms[0].department.value, 2, row);
		this.setCellText(document.forms[0].attheading.value, 3, row);
		this.setCellText(document.forms[0].attsubheading.value, 4, row);
		this.setCellText(attribute, 5, row);
		this.setCellText(attributetype,6, row);
		this.setCellText(dropdowndata, 7, row);
		this.setCellText(label1, 8, row);
		this.setCellText(label2, 9, row);

		attributeGrid.render();

	}
	attributeGrid.setCellEditable(false);

var countofrows_heading = 0;
	function addValues(){
		if(document.forms[0].headingcaption.value==""){
		alert("please enter heading");
		return false;
		}
		if(document.forms[0].headingorder.value==""){
		alert("please enter Heading Display Order");
		return false;
		}
		heading = document.forms[0].headingcaption.value;
		heading_order=document.forms[0].headingorder.value;
		subheading_status = document.forms[0].subheadingstatus.value;

		  myarray_heading[(countofrows_heading)]=new Array(1);
		  myarray_heading[(countofrows_heading)][0]=heading;
		  myarray_heading[(countofrows_heading)][1]=heading_order;
		  myarray_heading[(countofrows_heading)][2]=subheading_status;
		  headingGrid.addRow(serial++);
		  countofrows_heading++;
		  headingGrid.render();

	      document.forms[0].headingcaption.value="";
	      document.forms[0].headingorder.value="";
	      document.forms[0].subheadingstatus.value="";
	      return true;

		}
var countofrows_sub = 0;
var headingonsubmit;
	function addSubheading(){
	 if(document.getElementById("heading1").options.selectedIndex==0 && document.getElementById("heading1").options.selectedIndex==""){
	   alert("please select Heading");
	   return false;
	 }
	 if(document.forms[0].subheadingcaption.value==""){
	 alert("Please enter sub heading");
	 return false;
	 }
	 if(document.forms[0].subheadingorder.value==""){
	 alert("Please enter sub heading Display Order");
	 return false;
	 }
          headingonsubmit =document.forms[0].heading1.value;
	      subheading = document.forms[0].subheadingcaption.value;
	      displayorder = document.forms[0].subheadingorder.value;
	      myarray_subheading[(countofrows_sub)]=new Array(7);
	      myarray_subheading[(countofrows_sub)][0]= headingonsubmit;
		  myarray_subheading[(countofrows_sub)][1]=subheading;
		  myarray_subheading[(countofrows_sub)][2]=displayorder;
		  subheadingGrid.addRow(serial++);
		  countofrows_sub++;
		  subheadingGrid.render();
	      document.forms[0].subheadingcaption.value="";
	      document.forms[0].subheadingorder.value="";
	      return true;

	}
var countofrows_att = 0;
		function addAttributes(){
		if(document.forms[0].atttype.value=='DROPDOWN' || document.forms[0].atttype.value=='CHECKBOX' || document.forms[0].atttype.value=='RADIO'){
		  if(document.forms[0].dropdowndata.options[0].selected){
		  alert("Please select any data group");
		  return false;
		  }
		}
			if(document.forms[0].atttype.value=='TEXTWITH_2LABELS_2FIELDS'){
				if(document.forms[0].label1.value=="" || document.forms[0].label2.value=="" ){
				alert("Please Enter both the labels");
				return false;
			}
			}

     if(document.forms[0].attheading.value==""){
		 alert("Please Select Heading for Attribute");
		 return false;
	 }
	 if(!document.forms[0].attsubheading.disabled){
	 if(document.forms[0].attsubheading.value==""){
		 alert("Please enter sub heading for Attribute");
		 return false;
	 }
	 }
	 if(document.forms[0].attcaption.value==""){
		 alert("Please enter Caption For attribute");
		 return false;
	 }
	 if(document.forms[0].atttype.value==""){
		 alert("Please Select Attribute Type");
		 return false;
	 }
	 if(document.forms[0].attdisorder.value==""){
		 alert("Please enter Attribute Display Order");
		 return false;
	 }

	  formtitle = document.forms[0].formtitle.value;
	  attribute = document.forms[0].attcaption.value;
	  heading = document.forms[0].attheading.value;
	  subheading = document.forms[0].attsubheading.value;
	  attributetype=document.forms[0].atttype.value;
	  attributeorder=document.forms[0].attdisorder.value;
	  atttype=document.forms[0].atttype.value;
	  dropdowndata=document.forms[0].dropdowndata.value;
	  label1=document.forms[0].label1.value;
	  label2=document.forms[0].label2.value;
	  formtitle=document.forms[0].formtitle.value;
	  department=document.forms[0].department.value;

	  if(document.getElementById("notesreq").checked){
	  notesStatus="Y";
	  }else if(document.getElementById("notesnotreq").checked){
	   notesStatus="N";
	  }

	   myarray_attributes[(countofrows_att)]=new Array(7);
	   myarray_attributes[(countofrows_att)][0]=attributeorder;
	   myarray_attributes[(countofrows_att)][1]=formtitle;
	   myarray_attributes[(countofrows_att)][2]=heading;
	   myarray_attributes[(countofrows_att)][3]=subheading;
       myarray_attributes[(countofrows_att)][4]=attribute;
       myarray_attributes[(countofrows_att)][5]=attributetype;
       myarray_attributes[(countofrows_att)][6]=dropdowndata;
       myarray_attributes[(countofrows_att)][7]=label1;
       myarray_attributes[(countofrows_att)][8]=label2;
       myarray_attributes[(countofrows_att)][10]=notesStatus;

       myarray_attributes[(countofrows_att)][9]=department;
		  attributeGrid.addRow(serial++);
		  attributeGrid.render();
		  countofrows_att++;
		  document.forms[0].atttype.value="";
		  document.forms[0].attcaption.value="";
		  	   document.forms[0].label1.value="";
		  	    document.forms[0].label2.value="";
		  	    document.forms[0].attdisorder.value="";
		  	    document.getElementById('dropdownvalues').style.display='none';
		  	    document.getElementById('labels').style.display='none';
	      return true;

	}
	function addHeadingOnSubmit(){
    for(j=0;j<headingGrid.getRowCount();j++){
    headingSubmit = myarray_heading[j][0];
    headingorder = myarray_heading[j][1];
    subheadingstatus = myarray_heading[j][2];
    if(headingSubmit!="" && headingorder!=""){
    innerHeadingHtml();

    }

    }
     return true;

	}
	function addSubheadingonSubmit(){

	for(k = 0; k<subheadingGrid.getRowCount();k++){
	 headingofsubheading=myarray_subheading[k][0];
      subheadingonsubmit = myarray_subheading[k][1];
      subheadingorderonsubmit = myarray_subheading[k][2];

      if(headingofsubheading!="" ){
      innerSubheadingHtml();

      }
	}
	return true;
	}

	function innerSubheadingHtml(){
    var oTable = document.getElementById('tabdisplay');
      oTR = oTable.insertRow(0);
        oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="0"  name="headingofsubheading" value="'+headingofsubheading +'" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="0"  name="subheadingonsubmit" value="'+subheadingonsubmit +'" readonly>';

	    oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="0"  name="subheadingorderonsubmit" value="'+ subheadingorderonsubmit +'" readonly>';
	}
	function innerHeadingHtml(){
	var oTable = document.getElementById('tabdisplay');
      oTR = oTable.insertRow(0);

		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="0"  name="innerHeadingSubmit" value="'+headingSubmit +'" readonly>';

	    oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="0"  name="innerheadingorder" value="'+ headingorder +'" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="0"  name="innersubheadingstatus" value="'+ subheadingstatus +'" readonly>';
	}

	function addValuesonSubmit()

		{
		if(validateForm()){
		if(addHeadingOnSubmit()){
		if(addSubheadingonSubmit()){
		for(i=0;i<attributeGrid.getRowCount();i++)
		{
		 attributeorder=myarray_attributes[i][0];
         formtitle = document.forms[0].formtitle.value;
		 heading=myarray_attributes[i][2];
		 subheading=myarray_attributes[i][3];
		 attribute=myarray_attributes[i][4];
		 attributetype=myarray_attributes[i][5];
		 dropdowndata = myarray_attributes[i][6];
		 label1 = myarray_attributes[i][7];
		 label2 = myarray_attributes[i][8];
		 notesstatus=myarray_attributes[i][10];
		 formDescription = document.forms[0].titledescription.value;;


		 department = document.forms[0].department.value;


		if((heading!=""))
		{

		innerhtml();
		}
	   }

	   document.forms[0].action = "../../pages/metadata/genericui.do?method=saveMetaData";
	   document.forms[0].submit();
	  }
	  }
	  }
	  }

	  function validateForm(){

	    if(document.forms[0].formtitle.value==""){
	    alert("Please Enter Form Name");
	    return false;
	    }
	    if(document.forms[0].titledescription.value == ""){
	    alert("Please Enter Form Title");
	    return false;
	    }
	    if(document.forms[0].department.value==""){
	    alert("Please Select Department");
	    return false;
	    }
	    if(headingGrid.getRowCount()==0){
	    alert("Please Enter Atleast one heading");
	    return false;

	    }

	    if(attributeGrid.getRowCount()==0){
	    alert("Please Enter Atleast one attribute heading");
	    return false;

	    }
		document.forms[0].headingorder.value="";
		document.forms[0].heading1.value="";
		document.forms[0].subheadingcaption.value="";
		document.forms[0].subheadingorder.value="";
		document.forms[0].attcaption.value="";
		document.forms[0].atttype.options[0].selected=true;
		document.forms[0].attdisorder.value="";
		document.forms[0].attsubheading.disabled=false;
		return true;

	  }
	function innerhtml()
	  {
		var oTable = document.getElementById('tabdisplay');
		oTR = oTable.insertRow(0);
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="0"  name="innerheading" value="'+heading +'" readonly>';
	    oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="0"  name="innersubheading" value="'+ subheading +'" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="innerattribute" value="'+attribute+ '" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="innerattributetype" value="'+attributetype+ '" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="innerattributeorder" value="'+attributeorder+ '" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="innerdatagroup" value="'+dropdowndata+ '" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="innerlabel1" value="'+label1+ '" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="innerlabel2" value="'+label2+ '" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="innerformtitle" value="'+formtitle+ '" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="innerdepartment" value="'+department+ '" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="notesstatus" value="'+notesstatus+ '" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="validations" value="'+validations+ '" readonly>';
		oTD = oTR.insertCell(-1);
		oTD.innerHTML = '<input type="hidden" size="17"  name="innerformDescription" value="'+formDescription+ '" readonly>';
		}
	function dropDown(obj){labels
	if(document.forms[0].atttype.value=='TEXTWITH_2LABELS_2FIELDS'){
      document.getElementById('labels').style.display='block';
      }else{
      document.getElementById('labels').style.display='none';
      }
       if(document.forms[0].atttype.value=='DROPDOWN' || document.forms[0].atttype.value=='RADIO' || document.forms[0].atttype.value=='CHECKBOX' ){
       document.getElementById('dropdownvalues').style.display='block';
       }else{
       document.getElementById('dropdownvalues').style.display='none';
       }



	}
	function openWindow(){
	var url='../../pages/metadata/datadictionary.jsp';
	window.open(url,'Popup_Window',"width=750,height=370,status=no,resizable=no,top=200,left=150,scrollbars=yes");
}
function add(group,grp_id,list){
return false;
}
var u = 1;
var k = 1;
var l = 1;
function addOptionForHeadingForAttriute(selectbox,text,value )
{

selectbox.options[u] = new Option(value,text);
selectbox.options[u].value=value;
selectbox.options[u].text=text;
u++;
}
function addOptionForHeading(selectbox,text,value )
{

selectbox.options[l] = new Option(value,text);
selectbox.options[l].value=value;
selectbox.options[l].text=text;
l++;
}
var o = 1;
function addOption(selectbox,text,value )
{
selectbox.options[o] = new Option(value,text);
selectbox.options[o].value=value;
selectbox.options[o].text=text;
o++;
}
function addOptionForSubHeading(selectbox,text,value )
{

selectbox.options[k] = new Option(value,text);
selectbox.options[k].value=value;
selectbox.options[k].text=text;
k++;
}
function checksubheadingStatus(heading){
for(var j=0;j<headingGrid.getRowCount();j++){
    if(myarray_heading[j][0]==heading){
   if( myarray_heading[j][2]=="N"){
   document.forms[0].attsubheading.disabled=true;
   }else{
   document.forms[0].attsubheading.disabled=false;
   }
    }
    }

}


function clearFields(){
document.forms[0].formtitle.value="";
document.forms[0].titledescription.value="";
document.forms[0].department.value="";
document.forms[0].headingcaption.value="";
document.forms[0].headingorder.value="";
document.forms[0].heading1.value="";
document.forms[0].subheadingcaption.value="";
document.forms[0].subheadingorder.value="";
document.forms[0].attcaption.value="";
document.forms[0].atttype.options[0].selected=true;
document.forms[0].attdisorder.value="";
document.forms[0].attsubheading.disabled=false;
document.forms[0].notesreq.checked=true;

}
function notesRquired(required){
document.forms[0].notes.value=required;

}
function notesNotRquired(notrequired){
document.forms[0].notes.value=notrequired;
}
function checkEmpty(val){
if(val==""){
alert("Please enter label");
return false;

}

}
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
							k++;
						}
					}

			}
		}
		function deleteForm(){
			var dept = document.forms[0].deptName.options[document.forms[0].deptName.selectedIndex].value;
			var formCode = document.forms[0].formName.options[document.forms[0].formName.selectedIndex].value;
			if(dept=="")
			{
			alert("please select any department");
			return false;
			}
			if(formCode=="")
			{
			alert("please select any form");
			return false;
			}
			var ajaxreq = new XMLHttpRequest();
			var url = "<%=request.getContextPath()%>/pages/metadata/genericui.do?method=deleteForm&dept="+dept+"&formCode="+formCode;
			getResponseHandlerText(ajaxreq, displaystatus, url);
		}
		function displaystatus(responseData){
			var jsonExpression = "(" + responseData + ")";
			var result = eval(jsonExpression);
			if(result){
			alert("Form removed sucessfully");
			document.forms[0].deptName.selectedIndex=0;
			document.forms[0].formName.selectedIndex=0;
			document.forms[0].action = "../../pages/metadata/genericui.do?method=getUIMaster";
			document.forms[0].submit();
			}


		}
</script>
</head>
<body onload="clearFields();">
<form name="UIForm" method="POST">
<table>
	<tr>
		<td>
		<table>

			<tr>
				<td width="100%" height="10px" align="center"><span
					class="pageHeader">Speciality Forms Template</span></td>
				<td width="100%" height="5%" colspan="" class="topLineDownSpace">&nbsp;
				</td>
			</tr>

			<tr>
				<td width="100%" height="2%" class="label" colspan="3">&nbsp; <span
					class="resultMessage"> <logic:present name="msg"
					scope="request">
					<bean:write name="msg" scope="request" />
				</logic:present> </span></td>
			</tr>
			<tr>
				<td>
				<fieldset style="width: 345;height: 70"><legend
					class="label"><b>FORM DETAILS</b>
				<table>
					<tr>
						<td class="label">Form Name</td>
						<td><input type="text" name="formtitle" id="formtitle"
							class="forminput" /><font class="star">*</font></td>
					</tr>
					<tr>
						<td class="label">Form Title</td>
						<td><input type="text" name="titledescription"
							id="titledescription" class="forminput" /><font class="star">*</font></td>
					</tr>
					<tr>
						<td class="label">Department</td>
						<td><select name="department" id="department"
							class="forminput">
							<option value="">.....Select.....</option>
							<logic:iterate id="dept" name="availdepts">
								<option value='<bean:write name="dept" property="DEPT_ID"/>'><bean:write
									name="dept" property="DEPT_NAME" /></option>
							</logic:iterate>

						</select>&nbsp;<font class="star">*</font></td>
					</tr>
				</table>
				</legend></fieldset>
				</td>

			</tr>

		</table>
		</td>
	</tr>
	<tr>
		<td>
		<table>
			<tr>
				<td>
				<fieldset style="width: 345;height: 80"><legend
					class="label"><b>HEADINGS</b>
				<table>
					<tr>
						<td class="label">Heading Caption</td>
						<td><input type="text" name="headingcaption"
							id="headingcaption" class="forminput" /><font class="star">*</font></td>
					</tr>
					<tr>
						<td class="label">Display Order</td>
						<td><input type="text" name="headingorder" id="headingorder"
							class="forminput" onkeypress="return enterNumOnly(event)" /><font
							class="star">*</font></td>
					</tr>
					<tr>
						<td class="label">Has Subheading</td>
						<td><select name="subheadingstatus" id="subheadingstatus"
							class="forminput">
							<option value="Y">Yes</option>
							<option value="N">No</option>


						</select><font class="star">*</font></td>
						<td><input type="button" name="headingadd" id="headingadd"
							class="button" value="Add" onclick="return addValues();" /></td>
					</tr>

				</table>
				</legend></fieldset>
				</td>
				<td><script>
					document.write(headingGrid);
				</script></td>

			</tr>
		</table>
		</td>



	</tr>

	<tr>
		<td>
		<div id="subheadingdiv" style="display: block">
		<table>
			<tr>
				<td>
				<fieldset style="width: 345;height: 80"><legend
					class="label"><b>SUB HEADINGS</b>
				<table>
					<tr>
						<td class="label">Heading</td>
						<td><select name="heading1" id="heading1" class="forminput">
							<option value="">......Select......</option>


						</select>&nbsp;<font class="star">*</font></td>
					</tr>
					<tr>
						<td class="label">SubHeading Caption</td>
						<td><input type="text" name="subheadingcaption"
							id="subheadingcaption" class="forminput" /><font class="star">*</font></td>
					</tr>
					<tr>
						<td class="label">Display Order</td>
						<td><input type="text" name="subheadingorder"
							id="subheadingorder" onkeypress="return enterNumOnly(event)"
							class="forminput" /><font class="star">*</font></td>
						<td><input type="button" name="subheadingadd"
							id="subheadingadd" class="button" value="ADD"
							onclick="return addSubheading();" /></td>
					</tr>
				</table>
				</legend></fieldset>
				</td>
				<td><script>
					document.write(subheadingGrid);
				</script></td>

			</tr>
		</table>
		</div>
		</td>



	</tr>

	<tr>
		<td>
		<table>
			<tr>
				<td>
				<fieldset style="width: auto;height: auto"><legend
					class="label"><b>ATTRIBUTES</b>
				<table>
					<tr>
						<td class="label">Heading</td>
						<td><select name="attheading" id="attheading"
							class="forminput" onchange="checksubheadingStatus(this.value)">
							<option value="">....Select.....</option>
						</select>&nbsp;<font class="star">*</font></td>
					</tr>
					<tr>
						<td class="label">
						<div id="subheadinglabeldiv">Sub Heading :</div>
						</td>
						<td>
						<div id="subheadingfielddiv"><select name="attsubheading"
							id="attsubheading" class="forminput" onchange="">
							<option value="">.....Select.....</option>
						</select><font class="star">*</font></div>
						</td>
					</tr>
					<tr>
						<td class="label">Attribute Caption</td>
						<td><input type="text" name="attcaption" id="attcaption"
							class="forminput" /><font class="star">*</font></td>
					</tr>
					<tr>
						<td class="label">Attribute Type</td>
						<td><select name="atttype" id="atttype" class="forminput"
							onchange="dropDown(this);">
							<option value="">.....Select.....</option>
							<logic:iterate id="datatypes" name="datatypes">
								<option
									value='<bean:write name="datatypes" property="DATATYPE_NAME"/>'><bean:write
									name="datatypes" property="DATATYPE_NAME" /></option>
							</logic:iterate>


						</select>&nbsp;<font class="star">*</font></td>
						<td class="label">
						<div id="labels" style="display: none">
						<table>
							<tr>
								<td>label1<input type="text" name="label1" id="label1"
									class="forminput" onblur="return checkEmpty(this.value)" /></td>
							</tr>
							<tr>
								<td>label2<input type="text" name="label2" id="label2"
									class="forminput" onblur="return checkEmpty(this.value)" /></td>
							</tr>
						</table>
						</div>
						</td>
						<td>
						<div id="dropdownvalues" style="display: none" class="label">DataGroup
						<select name="dropdowndata" id="dropdowndata" class="forminput">

							<option value="">....Select.....</option>
							<logic:iterate id="dropdowndataheader" name="dropdowndataheader">
								<option
									value='<bean:write name="dropdowndataheader" property="HEADER_ID"/>'><bean:write
									name="dropdowndataheader" property="NAME" /></option>
							</logic:iterate>


						</select>&nbsp;<font class="star">*</font><input type="button"
							name="dropdownvaluesadd" id="dropdownvaluesadd" class="button"
							value="ADD" onclick="openWindow();return false;" /></div>
						</td>
						<td>
						<div id="datadiv" style="display: none"><select
							multiple="multiple" name="list" id="list" size="10"></select></div>
						</td>
					</tr>
					<tr>
						<td class="label">Display Order</td>
						<td><input type="text"
							onkeypress="return enterNumOnly(event)" name="attdisorder"
							id="attdisorder" class="forminput" /><font class="star">*</font></td>
					</tr>
					<tr>
						<td class="label">Notes</td>
						<td class="label"><input type="radio" name="notes"
							id="notesreq" name="notes" value="Y" class="forminput"
							onclick="notesRquired(this.value);">Required <input
							type="radio" id="notesnotreq" name="notes" value="N"
							onclick="notesNotRquired(this.value);">Not Required</td>
							<td>&nbsp;<input type="button" name="subheadingadd"
							id="subheadingadd" class="button" value="ADD"
							onclick="return addAttributes();" /></td>
					</tr>
					<tr>


					</tr>




				</table>
				</legend></fieldset>
				</td>
				<td><script>
						document.write(attributeGrid);
					</script></td>
			</tr>


			<tr>

				<td>
				<table id="tabdisplay">
					<tr>
						<td style="width: 300"></td>
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<td><input type="button" name="save" id="save" value="Save"
							class="button" onclick="return addValuesonSubmit();" /></td>
				</table>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
				<td>
				<div class="stwMain">
				<div class="stwHeader stwClosed"
					id="filter" onclick="stwToggle(this);"><label>Delete&nbsp;Form(Form&nbsp;which&nbsp;are&nbsp;not&nbsp;connected&nbsp;to&nbsp;any&nbsp;patients&nbsp;can&nbsp;only&nbsp;available&nbsp;for&nbsp;delete)</label>
				</div>
				<div id="filter_content"
					class="stwContent stwHidden">
				<table align="center" class="search" width="100%">
					<tr>
						<td class="label" align="right">Select&nbsp;Department</td>
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
						<td class="label" align="right">Select&nbsp;Speciality&nbsp;Template</td>
						<td>&nbsp;</td>
						<td><select id="formName" name="formName">
							<option value="">--------select--------</option>
						</select></td>
						<td><input type="button" name="delete" id="delete"
							value="Delete" onclick="deleteForm();" />
					</tr>
					</div>
				</table>
				</div>
				</td>
			</tr>
</table>
</form>

</body>
</html>
