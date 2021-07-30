<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<html>
<head>
<title>Send Message - Message Type Selection</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="messaging/messaging.js"/>

<style>
.scrolForContainer .yui-ac-content{
	 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
    _height:11em; /* ie6 */
}

.yui-ac {
	padding-bottom: 2em;
}
</style>


<script>

var messageTypeList = ${messageTypeList};
var providerMap = ${providerMap};
</script>
  <script>
		contextPath = "${pageContext.request.contextPath}";
		publicPath = contextPath + "/ui/";
  </script>	
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<body>
<div id="storecheck" style="display: block;" >

<h1>Send Message</h1>

<form name="MessageForm" method="POST" action="Message.do">
	<input type="hidden" name="_method" value="saveMessageType"/>
	<fieldset class="fieldSetBorder" >
		<legend class="fieldSetLabel">Select Message Type</legend>
		<table class="formtable" align="left" cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr>
				<td class="formlabel">Message Type:</td>
				<td>
				<insta:selectdb name="message_type_id" table="message_types"
				dummyvalue="---Select---" dummyvalueId="" value="${message_type_id}" valuecol="message_type_id"
				displaycol="message_type_name"
				filtered="true" filtercol="status,event_id" filtervalue="A,ui_trigger"
				onchange="messageTypeChange(this, messageTypeList);"/><span class="star">*</span>
				</td>
				<td colspan="4"></td>
			</tr>
			<tr>
				<td class="formlabel">Mode: </td>
				<td>
					<insta:selectdb name="message_mode" table="message_group_dispatcher_view" id="message_mode"
						dummyvalue="---Select---" dummyvalueId="" value="${message_mode}"
						valuecol="message_mode" displaycol="display_name" /><span class="star">*</span>
				</td>
				<td colspan="4"></td>
			</tr>
			<tr>
				<td class="formlabel">Recipients:</td>
				<td>
				<select class="dropdown" id="provider_name" name="provider_name">
				<option value="">----Select----</option>
				</select>
				<span class="star">*</span>
				</td>
				<td colspan="4"></td>
			</tr>
		</table>
	</fieldset>
	<div class="screenActions">
	<button type="button"  class="button" accesskey="N"  onclick="return saveMessageType();"><b><u>N</u></b>ext</button>
	</div>
</form>
</div>
</body>
</html>
