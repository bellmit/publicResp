<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit District Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
  <script>
    function doClose() {
      window.location.href = "${cpath}/master/districts/list.htm?sortOrder=district_name&sortReverse=false&districtstatus=A";
    }

    var chkDistrictList = ${ifn:convertListToJson(districtList)};
    var hiddenDistrictId = '${bean.district_id}';

    function checkduplicate(){
      var newDistrictName = trimAll(document.forms[0].district_name.value);
      var newStateId = trimAll(document.forms[0].state_id.value);
      for(var i=0;i<chkDistrictList.length;i++){
        item = chkDistrictList[i];
        if(hiddenDistrictId!=item.district_id){
           var actualDistrictName = item.district_name;
            if (newDistrictName.toLowerCase() == actualDistrictName.toLowerCase() && item.state_id == newStateId) {
              alert(document.forms[0].district_name.value+" already exists please enter other name");
              document.forms[0].district_name.value='';
              document.forms[0].district_name.focus();
              return false;
            }
           }
      }
      return true;
      }
    
    function restrictInactiveDistrict() {
      if (!checkduplicate()) return false;
      if (!checkInactiveCityMapping()) return false;
      return true;
    }
    
    function checkInactiveCityMapping() {
		var districtId = document.forms[0].district_id.value;
		var districtName =  document.forms[0].district_name.value;
		var status = document.forms[0].status.value;
		var cityList = ${ifn:convertListToJson(citiesList)};
		if(status == 'I'){
			for (var i=0;i<cityList.length;i++) {
				if(cityList[i].district_id == districtId && cityList[i].status == 'A') {
					alert("Active cities are mapped with this district "+districtName+". Hence, it can not be marked as Inactive.");
						return false;
				}
			}
		}
		return true;
	}
    
  </script>

</head>
<body>

<c:set var="actionUrl" value="${cpath}/master/districts/update.htm?district_id=${bean.district_id}"/>
<form action="${actionUrl}" method="POST">
    <input type="hidden" name="district_id" value="${bean.district_id}"/>

  <div class="pageHeader">Edit District</div>
  <insta:feedback-panel/>
  <fieldset class="fieldsetborder">

    <table class="formtable">
      <tr>
        <td class="formlabel">State:</td>
        <td>
           <insta:selectdb name="state_id" value="${bean.state_id}" table="state_master" valuecol="state_id" displaycol="state_name" orderby="state_name"/>
        </td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
      </tr>

      <tr>
        <td class="formlabel">District:</td>
        <td>
          <input type="text" name="district_name" value="${bean.district_name}" onblur="capWords(district_name);checkduplicate();" class="required validate-length" length="50" title="Name is required and max length of name can be 50" />
        </td>
      </tr>

      <tr>
        <td class="formlabel">Status</td>
        <td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
      </tr>

    </table>

  </fieldset>

    <table class="screenActions">
    <tr>
      <td><button type="submit" accesskey="S" onclick="return restrictInactiveDistrict();"><b><u>S</u></b>ave</button></td>
      <td>&nbsp;|&nbsp;</td>
      <td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/districts/add.htm'">Add</a>
      <td>&nbsp;|&nbsp;</td>
      <td><a href="javascript:void(0)" onclick="doClose();">District List</a></td>
    </tr>
  </table>

</form>

</body>
</html>
