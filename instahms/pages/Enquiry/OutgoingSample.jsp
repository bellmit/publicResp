<%@page import="java.util.List"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
<title>Outgoing Sample - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="tableSearch.js"/>
<insta:link type="script" file="ajax.js"/>

	<script language="javascript" type="text/javascript">

		function openReport(){
	if(validateForm()){
	    var condition1 ="";
	    for(var i =0;i<document.forms[0].condition.length;i++){
	    	if(document.forms[0].condition[i].checked){
	    		condition1=document.forms[0].condition[i].value;
	    		break;
	        }

	    }
		//alert("condition--->"+condition1+"         condition value"+document.forms[0].conditionValue.value);
	    window.open("report.do?condition="+condition1+"&conditionValue="+document.forms[0].conditionValue.value.toUpperCase()+"&method=searchDiagReport&fromDate="+document.forms[0].fromDate.value+"&toDate="+document.forms[0].toDate.value);
	    document.forms[0].conditionValue.value="";
	    document.forms[0].fromDate.value="";
	    document.forms[0].toDate.value="";
	 }
	 return false;
	}
			   function dateDiff(id1,id2)
  {

   var date1, date2;
   var month1, month2;
   var year1, year2;


  var x = document.getElementById(id2).value;//  to date
   var y = document.getElementById(id1).value;// from date



   date2 = y.substring (0, y.indexOf ("."));
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

   function getReport(){
     var todate="";
     var fromdate="";
     fromdate=document.forms[0].fromDate.value;
     todate=document.forms[0].toDate.value;

      if(validateForm()==false){
	  return false;
	  }else if(document.forms[0].condition[0].checked){
        window.open("report.do?method=getOutGoingSampleCollection&fromDate="+fromdate+"&toDate="+todate );

      }else if(document.forms[0].condition[1].checked){
        window.open("report.do?method=getOutGoingNotConductingSampleCollection&fromDate="+fromdate+"&toDate="+todate );

      }else{
        alert("Select Test Type");
        return false;
      }
   }
	</script>

</head>
<body  oncopy="return false" onbeforecut="return false" oncut="return false" >

<form method="POST">




<!-- Main tab--> <TABLE width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                    <TR>
                      <TD width="10" height="20"><insta:link type="image" file="sub_img01.gif"/></TD>
                      <TD height="20" background="../../images/sub_img03.gif" class="subtitle" width="100%" align="center">OutGoingSampleCollection</TD>
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
                                  <tr width="100%" valign="top"><td align='center' >
                                        <span style="font-family:Verdana; font-size:12px; color:#800000">From&nbsp;</span>
					<label>From</label>
					Date :<insta:datewidget name="fromDate" id="fdate"  />
					<label>To</label>
					Date :<insta:datewidget name="toDate" id="todate" />
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                      <span style="font-family:Verdana; font-size:12px; color:#800000">
                                      <INPUT type="radio" name="condition" value="doc1.doctor_name" >   TestConducted&nbsp;&nbsp;
                                      <INPUT type="radio" name="condition" value="sc.sample_sno" > TestNotConducted</span></td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td>&nbsp;</td></tr>
                                  <tr width="100%" valign="top"><td align='center'>
                                            <input type="button" value="Report" name="B1" class="button" onclick="getReport()"></td></tr>
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
