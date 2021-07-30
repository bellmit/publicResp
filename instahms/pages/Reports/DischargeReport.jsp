<%@page import="java.util.List"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
<title>Discharge Report - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="css" file="hms.css"/>
<insta:link type="css" file="aw.css"/>
<insta:link type="script" file="DatePick.js"/>
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="aw.js"/>

	<script language="javascript" type="text/javascript">

	function isFutureDate(id)
	{

	var date1, date2;
   var month1, month2;
   var year1, year2;
   var today = new Date();

   var selectedDate = document.getElementById(id).value;
   date2 = selectedDate.substring (0, selectedDate.indexOf ("-"));
   month2 = findMonth(selectedDate.substring (selectedDate.indexOf ("-")+1, selectedDate.lastIndexOf ("-")));
   year2 = selectedDate.substring(selectedDate.lastIndexOf ("-")+1, selectedDate.length);
   month1 = today.getMonth()+1;
   date1 = today.getDate();
   year1 = today.getFullYear();

   if (year1 > year2) return 1;
   else if (year1 < year2) return -1;
   else if (month1 > month2) return 1;
   else if (month1 < month2) return -1;
   else if (date1 > date2) return 1;
   else if (date1 < date2) return -1;
   else return 0;
}
 function findMonth(month){
     var mon;

if(month=='JAN'){
mon=1;
}
else if(month=='FEB'){
mon=2;
}
else if(month=='MAR'){
mon=3;
}
else if(month=='APR'){
mon=4;
}
else if(month=='MAY'){
mon=5;
}
else if(month=='JUN'){
mon=6;
}
else if(month=='JUL'){
mon=7;
}
else if(month=='AUG'){
mon=8;
}
else if(month=='SEP'){
mon=9;
}
else if(month=='OCT'){
mon=10;
}
else if(month=='NOV'){
mon=11;
}
else if(month=='DEC'){
mon=12;
}
        return mon;


      }
      var type="";
  function validateForm(){
  	  if(document.forms[0].ward.value==""){
  	  	 alert("Select ward");
  	  	 return false;
  	  }
  	var typeObj = document.forms[0].type;
  	for(var i=0;i<typeObj.length;i++){
  	  if(typeObj[i].checked){
  	     type=typeObj[i].value;
  	     break;
  	  }
  	 }
  	  if(type=="daily"){
  	  	if(document.forms[0].toDate.value==""){
  	  		alert("Select date ");
  	  		return false;
      	}
      	if((isFutureDate("todate")<0)){
      	 	alert("Selected date cannot be greater than current date");
      	 	return false;
      	}
      }
      return true;
  }
  function report(){
  	if(validateForm()){
  	var url="";
  	var leap="0";
  	var days="";
  	if(type=="daily"){
  	 url="bedmanagementreport.do?method=getWardWiseDischargeCountReport&type="+type+"&wardNo="+document.forms[0].ward.value+"&wardName="+document.forms[0].ward.options[document.forms[0].ward.options.selectedIndex].text+"&month=";
  	  var toDate = document.forms[0].toDate.value;
  	  var month = toDate.substring(toDate.indexOf("-")+1,toDate.lastIndexOf("-"));
  	  var year = toDate.substring(toDate.lastIndexOf("-")+1);

  	 if((year%4)==0){
			leap="1";
			if( ((year%100)==0) && !((year%400)==0)){
				leap="0";
			}
		}

  	  if( (month=="APR") || (month=="JUN") || (month=="SEP") || (month=="NOV") )
  	  {
  	     days="30";
  	  }else if( (month=="FEB")  )
  	    {
  	    days="28";
  	    }else {
  	    days="31";
  	    }

  	  url+=month+"&year="+year+"&leap="+leap+"&days="+days;
  	}else{
  	 url="bedmanagementreport.do?method=getWardWiseDischargeCountReport&type="+type+"&wardNo="+document.forms[0].ward.value+"&wardName="+document.forms[0].ward.options[document.forms[0].ward.options.selectedIndex].text;
  	}
  // alert(url);
   window.open(url);

   document.forms[0].ward.value="";
   document.forms[0].toDate.value="";
  }

 }

	</script>

</head>
<body  oncopy="return false" onbeforecut="return false" oncut="return false" >
<form>





<!-- Main tab--> <TABLE width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                    <TR>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img01.gif"/></TD>
                      <TD height="20" background="../../images/sub_img03.gif" class="subtitle" width="100%" align="center">Discharge Count Report</TD>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img02.gif"/></TD>
                    </TR>

                    <TR>
                      <TD width="10" background="../../images/sub_img07.gif" height="100%"><insta:link type="image" file="spacer.gif"/></TD>
                      <TD valign="top" bgcolor="#fbe6d3" width="100%" height="100%">
     <!-- Center tab-->    <TABLE width="100%" border="0" cellpadding="0" cellspacing="0" class="tabletext" height="100%">
                              <TR><TD height="9" width="100%"></TD></TR>
                              <TR><TD class="leftnavbg2" width="100%" valign="top" align="middle">
                                 <table width="820" height="1" border="0" cellpadding="0" cellspacing="0" bordercolor='white'>
           <!-- Constant Cell-->  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                           <span style="color:#800000;font-family:verdhana:font-size:12px">Ward
                          				   <select name="ward" class="forminput" style="width:158">
                          				     <option value="">..............Select..............</option>
                          					 <logic:present name="wardList">
                          				     <logic:iterate id="ward" name ="wardList">
                          					   <option value='<bean:write name="ward" property="WARD_NO" />'><bean:write name="ward" property="WARD_NAME"/> </option>
                          					 </logic:iterate>
                          					 </logic:present>
                          				   </select></span></td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                            <span style="font-size:12px;font-family:Verdhana;color:#800000">
                                            <input type="radio" name="type" value="daily" onclick="document.getElementById('date').style.visibility='visible'">Daily
                          			        <input type="radio" name="type" value="monthly" checked onclick="document.getElementById('date').style.visibility='hidden'">Month
                          			        <input type="radio" name="type" value="yearly" onclick="document.getElementById('date').style.visibility='hidden'">Yearly</span></td></tr>

                          		  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top" style="visibility:hidden" id="date"><td align='center'>
                                             <span style="font-size:12px;font-family:Verdhana;color:#800000">Date</span>&nbsp;&nbsp;
                                             <input type="text" name="toDate" size="27" id="todate" onclick="javascript:show_calendar('forms[0].toDate');"  class="forminput" /></td></tr>

                                 <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                 <tr width="100%" valign="top"><td align='center'><input type="button" class="button" value="Report" onclick ="report()"/></td></tr>
                                 <tr width="100%" valign="top"><td>&nbsp;</td></tr>
           <!-- Constant Cell-->  </table>
                               </TD></TR>
     <!-- Center tab-->    </TABLE>
                       </TD>
                      <TD width="10" background="../../images/sub_img08.gif" height="100%"><insta:link type="image" file="spacer.gif"/></TD>
                    </TR>

                    <TR>
                      <TD width="10" height="1"><insta:link type="image" file="sub_img04.gif"/></TD>
                      <TD height="1" background="../../images/sub_img06.gif" width="796"><insta:link type="image" file="spacer.gif"/></TD>
                      <TD width="10" height="1"><insta:link type="image" file="sub_img05.gif"/></TD>
                    </TR>

<!-- Main tab--> </TABLE>


        </FORM>
  </body>
</html>
