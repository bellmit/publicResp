<%@page import="com.insta.hms.usermanager.UserDAO"%>
<%@page import="com.insta.hms.master.DoctorMaster.DoctorMasterDAO"%>
<%
JSONSerializer js = new JSONSerializer().exclude("class");
request.setAttribute("doctors_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(
			new DoctorMasterDAO().getAllDoctorNames())));
request.setAttribute("user_doctor_id", UserDAO.getUserBean((String) session.getAttribute("userId")).get("doctor_id"));
%>
<%@page import="flexjson.JSONSerializer"%>
<%@page import="com.insta.hms.common.ConversionUtils"%>

<script>
var diagnosis_doctors_json= ${doctors_json};
</script>