<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<script language="javascript" type="text/javascript">


 function nameValidation(){
	 document.getElementById("outHouseName").value = trimAll(document.getElementById("outHouseName").value);
 	 var tval=document.getElementById("outHouseName").value;
       <logic:present name="outhousename">
	      <logic:iterate id="housename" name="outhousename">
		      var unit='<bean:write name="housename" property="OH_NAME"/>';
		      if(tval==unit){
			      alert("The Name you entered already exists");
			      return false;
		      }
	      </logic:iterate>
      </logic:present>
      return true;
 }

   	function closeWindow()
	{
		window.close();
	}

    function topWindow(){

		 if(document.getElementById("outHouseName").value=="") {
		 	alert("enter OutHouse Name");
		 	return false;
		 }

		 if(!nameValidation()){
		 	return false;
		 }

	      myOption=new Option();
	      var unitval=document.getElementById("outHouseName").value;
	      myOption.text=unitval;
	      myOption.value=unitval;
	      var len=opener.document.OutHouseActionForm.name.options.length;
	      var insertIndex=len+1;
	      opener.document.OutHouseActionForm.name.options[1].text=myOption.text;
		  opener.document.OutHouseActionForm.name.options[1].value=myOption.value;
		  window.close();
	}

</script>
</head>
<body class="setMargin">
<form name="cityform" onsubmit="return topWindow()"  >
<table>

<tr><td align=center valign="middle">
                <table width="100%" border="0"  cellpadding="0" cellspacing="0" class="totalBG">
                    <tr >
                     <td class="leftLine">&nbsp;&nbsp;</td>
	  <td align="center" class="topLineDownSpace"><span class="pageHeader">Out&nbsp;House&nbsp;Master</span></td>
	  <td class="rightLine">&nbsp;&nbsp;&nbsp;</td>
                       </tr>
                    <tr  >
                      <td height="449px" class="leftLine"></td>
                      <td valign="top"   height="151" width="320">
                      <table width="229"     border="0" cellpadding="5" cellspacing="0" class="tabletext" height="87">
                        <tr>
                          <td height="19" width="243"><div align="right">&nbsp;</div></td>
                        </tr>
                        <tr>
                          <td  height="48" width="243">
                          <div align="center" >
                            <center>
                            <table width="243"  border="0" cellpadding="3" cellspacing="0"  style="border-collapse: collapse"  height="40">
                            <tr>
                              <td width="87" align="center" height="34" class="label">&nbsp;OutHouseName</td>
                              <td width="169" height="34">
                              <input name="outHouseName" id="outHouseName" type="text" class="forminput" size="21" onblur="upperCase(outHouseName);nameValidation()"><span style="position: absolute; left: 238; top: 232"> </span>
                              <span style="position: absolute; left: 89; top: 134">
							  </span></td>
							  </tr>
							  <tr>
							  </tr>
							  <tr>

							  <td >

                              </td>
                              <td>

                              <input type="submit" value="  OK  "  class="button" onclick="topWindow()" >
                              &nbsp;<input type="button" value="Cancel"  class="button" onclick="closeWindow()" ></td>
                            </tr>


                          </table></center>
                          </div>
                          </td>
                        </tr>
                        </table></td>
                     <td height="449px" class="rightLine"></td>
                    </tr>
                    <tr>

	                  <td width="10" height="10" class="bottomLeftCurve"></td>
                      <td height="10" class="bottomLine"></td>
                      <td width="10" height="10" class="bottomRightCurve" ></td>
                    </tr>
                  </table></td>

</tr></table>

 <input type="hidden" name="method"  >

 <input type="hidden" name="org" value="" id="org">
</form>
</body>
</html>
