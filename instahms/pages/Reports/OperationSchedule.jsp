<%@page import="java.util.List"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
<title>Insta HMS</title>
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


  function daywise(){
	    document.getElementById('mrnodiv').style.display='none' ;
	    document.getElementById('deptdiv').style.display='none' ;
	    document.getElementById('daydiv').style.display='block' ;
	    document.getElementById('namediv').style.display='none' ;
	}
	function mrnowise(){
	    document.getElementById('mrnodiv').style.display='block' ;
	    document.getElementById('deptdiv').style.display='none' ;
	    document.getElementById('daydiv').style.display='none' ;
	    document.getElementById('namediv').style.display='none' ;
	}
	function deptwise(){
	    document.getElementById('mrnodiv').style.display='none' ;
	    document.getElementById('deptdiv').style.display='block' ;
	    document.getElementById('daydiv').style.display='none' ;
	    document.getElementById('namediv').style.display='none' ;
	}
	function namewise(){
	    document.getElementById('mrnodiv').style.display='none' ;
	    document.getElementById('deptdiv').style.display='none' ;
	    document.getElementById('daydiv').style.display='none' ;
	    document.getElementById('namediv').style.display='block' ;
	}
	function print(){
	var mrno = "";
	var dept = "";
	var todate="";
	var fromdate="";
	var name="";
	    if(document.forms[0].type[0].checked){
	        if(validateForm()){
	            fromdate=document.forms[0].fromDate.value;
	            todate=document.forms[0].toDate.value;


	            var url="report.do?method=getOperationCatReport&kor=OS&type=day&fromDate="+fromdate+"&toDate="+todate;
	            //alert(url);
	            window.open(url);
	            }
	       }else if(document.forms[0].type[1].checked){
	         if(document.forms[0].mrnot.value==""){
	         alert("Enter the MR No");
	         return false;
	         }
	         mrno=document.forms[0].mrnot.value;
	         //alert(mrno);
	        var url="report.do?method=getOperationCatReport&kor=OS&type=mrno&mrno="+mrno;
	        //alert(url);
	        window.open(url);
	      }else if(document.forms[0].type[2].checked){
	         if(document.forms[0].deptt.value==""){
	         alert("Select Department");
	         return false;
	         }
	       dept=document.forms[0].deptt.value;
	       deptName=document.forms[0].deptt.options[document.forms[0].deptt.options.selectedIndex].text;

	       var url="report.do?method=getOperationCatReport&kor=OS&type=dept&dept="+dept+"&deptName="+deptName;
	       // alert(url);
	        window.open(url);
	    }else if(document.forms[0].type[3].checked){
	         if(document.forms[0].namet.value==""){
	         alert("Enter the PatientName");
	         return false;
	         }
	         name=document.forms[0].namet.value;
	        // alert(name);
	        var url="report.do?method=getOperationCatReport&kor=OS&type=name&name="+name;
	       // alert(url);
	        window.open(url);
	      } else {
	       alert("Select any Report Type");
	      }


	}


    function dateDiff(id1,id2)
     {

   var date1, date2;
   var month1, month2;
   var year1, year2;


  var x = document.getElementById(id2).value;//  to date
   var y = document.getElementById(id1).value;// from date



   date2 = y.substring (0, y.indexOf ("-"));
   month2 = findMonth(y.substring (y.indexOf ("-")+1, y.lastIndexOf ("-")));
   year2 = y.substring (y.lastIndexOf ("-")+1, y.length);

   date1 = x.substring (0, x.indexOf ("-"));
   month1 = findMonth(x.substring (x.indexOf ("-")+1, x.lastIndexOf ("-")));
   year1 = x.substring (x.lastIndexOf ("-")+1, x.length);

   if (year1 > year2) return 1;
   else if (year1 < year2) return -1;
   else if (month1 > month2) return 1;
   else if (month1 < month2) return -1;
   else if (date1 > date2) return 1;
   else if (date1 < date2) return -1;
   else return 0;


  }


  function validateForm()
  {
   if(document.forms[0].fromDate.value=="")
	  {
	  alert("Enter from date");
	  document.forms[0].fromDate.focus();
	  return false;
	  }

	  if(document.forms[0].fromDate.value!="")
	  {

	     if(isFutureDate("fdate")< 0)
	     {
	      alert("Selected from date should not be greater than current date");
	      return false;
	      }
	  }
	  if(document.forms[0].toDate.value=="")
	  {
	  alert("Enter to date");
	  document.forms[0].toDate.focus();
	  return false;
	  }


	  if(document.forms[0].toDate.value!="")
	  {
	     if(isFutureDate("todate")<0)
	     {
	      alert("Selected Todate should not be greater than current date");
	      return false;
	      }
	  }
	  if(dateDiff("fdate","todate")<0)
	  {
	  alert("From date should be less than to date")
	  return false;
	  }


  return true;
  }
  function isFutureDate(id)
	{

	var date1, date2;
   var month1, month2;
   var year1, year2;
   var today = new Date();

   var selectedDate = document.getElementById(id).value;
   date2 = selectedDate.substring (0, selectedDate.indexOf ("-"));
   month2 = findMonth(selectedDate.substring (selectedDate.indexOf ("-")+1, selectedDate.lastIndexOf ("-")));
   year2 = selectedDate.substring (selectedDate.lastIndexOf ("-")+1, selectedDate.length);
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

	</script>

</head>
<body  oncopy="return false" onbeforecut="return false" oncut="return false" >

<form method="POST" >





<!-- Main tab--> <TABLE width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                    <TR>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img01.gif"/></TD>
                      <TD height="20" background="../../images/sub_img03.gif" class="subtitle" width="100%" align="center">Operation Schedule Report</TD>
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

                                  <tr width="100%" valign="top" style="display:none" id="daydiv" height='30px'><td align='center'>
                                         <span style="font-family:Verdana; font-size:12px; color:#800000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;From</span>&nbsp;&nbsp;&nbsp;
                          				 <input type="text" name="fromDate" size="27" id="fdate" onclick="javascript:show_calendar('forms[0].fromDate');"  class="forminput" >

                             			 <span style="font-family:Verdana; font-size:12px; color:#800000">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; To</span>&nbsp;&nbsp;&nbsp;
                          				 <input type="text" name="toDate" size="27" id="todate" onclick="javascript:show_calendar('forms[0].toDate');"  class="forminput" ></td></tr>

                                  <tr width="100%" valign="top" style="display:none" id="mrnodiv"  height='30px'><td align='center'>
                                          <span style="font-family:Verdana; font-size:12px; color:#800000">
                                                   Enter MR No :</span><input type="text" name="mrnot" size="27" id="mrnot"   class="forminput" onblur="upperCase(mrnot)"></td></tr>

                                  <tr width="100%" valign="top" style="display:none" id="namediv" height='30px'><td align='center'>
                                          <span style="font-family:Verdana; font-size:12px; color:#800000">
                                                   Enter Patient Name :</span><input type="text" name="namet" size="40" id="namet"   class="forminput" onblur="upperCase(namet)"></td></tr>

                                  <tr width="100%" valign="top" style="display:none" id="deptdiv" height='30px'><td align='center'>
                                          <span style="font-family:Verdana; font-size:12px; color:#800000">
                                                   Select Department :</span>
                          				<select name="deptt" class="forminput" style="width:158">
                          				<option value="">..............Select..............</option>
                          				    <logic:present name="deptList">
                          						<logic:iterate id="dep" name ="deptList">
                          							<option value='<bean:write name="dep" property="DEPT_ID" />'><bean:write name="dep" property="DEPT_NAME"/>
                          						</logic:iterate>
                          				    </logic:present>
                          				 </select>    </td></tr>

                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                        	&nbsp;&nbsp;&nbsp;
                                			<input type=radio name="type" value="gen" onclick="daywise()" ><span style="font-family:Verdana; font-size:12px; color:#800000">Date Wise</span> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                			<input type=radio name="type" value="mrno" onclick="mrnowise()" ><span style="font-family:Verdana; font-size:12px; color:#800000">MR No Wise </span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                			<input type=radio name="type" value="dept" onclick="deptwise()" ><span style="font-family:Verdana; font-size:12px; color:#800000">Department Wise</span> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                			<input type=radio name="type" value="name" onclick="namewise()" ><span style="font-family:Verdana; font-size:12px; color:#800000">PatientName Wise</span> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                        </td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                        <INPUT class=button type="BUTTON"    value="REPORT" name=Submit10  onclick="print()"></td></tr>
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