<%@page import="java.util.List"%>
<%@taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>
<head>
<title>Daily Purchase - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<insta:link type="css" file="hms.css"/>
<insta:link type="css" file="aw.css"/>
<insta:link type="script" file="DatePick.js"/>
<insta:link type="script" file="date_go.js"/>

	<script language="javascript" type="text/javascript">



	function report(){
	if(validateForm()){

	date=document.forms[0].date.value;
	window.open("inventoryReport.do?&method=getDailyPurchaseReport&date="+date);
				document.forms[0].date.value="";
	}
	}

  function validateForm()
  {
   if(document.forms[0].date.value=="")
	  {
	  alert("Enter the date");
	  document.forms[0].date.focus();
	  return false;
	  }

	  if(document.forms[0].date.value!="")
	  {

	     if(isFutureDate("fdate")< 0)
	     {
	      alert("Selected  date should not be greater than current date");
	      return false;
	      }
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
<body   >

<FORM method="POST">




<!-- Main tab--> <TABLE width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
                    <TR>
                      <TD width="10" height="20"></TD>
                      <TD height="20"  class="pageHeader" width="100%" align="center">Daily Purchase Report</TD>
                      <TD width="10" height="20"></TD>
                    </TR>

                    <TR>
                      <TD width="10"  height="100%"></TD>
                      <TD valign="top" width="100%" height="100%">
     <!-- Center tab-->    <TABLE width="100%" border="0" cellpadding="5" cellspacing="0" class="tabletext" height="100%">
                              <TR><TD height="9" width="100%"></TD></TR>
                              <TR><TD  width="100%" valign="top">
                              <table width="100%" height="100" cellspacing="0" cellpadding="0" border="0">
                              <tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                              <tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                              <tr width="100%" height="19">
                                     <td width="50%" align="right"><span style="font-family:Verdana; font-size:12px; color:#2a7d17">Enter the Date</span>&nbsp;&nbsp;
                          			     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                                     <td width="50%"><input type="text" name="date" size="27" id="fdate" onclick="javascript:show_calendar('forms[0].date');"  class="forminput" ></td></tr>
                              <tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                              <tr width="100%" height="19"><td colspan="2" align="center">&nbsp;<input type="button" value="Report" name="B1" class="button" onclick="report()"></td></tr>
                              <tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                               <tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                               <tr width="100%" height="19"><td colspan="2" align="center"></td></tr>
                               <tr width="100%" height="19"><td colspan="2">&nbsp;</td></tr>
                              </table>

                              </TD></TR>
     <!-- Center tab-->    </TABLE>
                       </TD>
                      <TD width="10"  height="100%"></TD>
                    </TR>

                    <TR>
                      <TD width="10" height="1"></TD>
                     <TD height="1" align="center" width="796"><font face="Arial" style="font-size: 11"><jsp:include page="/pages/frame/footer.jsp"/></font></TD>
                      <TD width="10" height="1"></TD>
                    </TR>

<!-- Main tab--> </TABLE>


        </FORM>
  </body>
</html>
