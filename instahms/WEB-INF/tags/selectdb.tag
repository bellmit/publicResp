<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="false" %>
<%@ attribute name="values" required="false" type="java.lang.String[]" %>
<%@ attribute name="table" required="true" %>
<%@ attribute name="valuecol" required="true" %>
<%@ attribute name="displaycol" required="true" %>
<%@ attribute name="filtercol" required="false" %>
<%@ attribute name="filtervalue" required="false" %>
<%@ attribute name="dummyvalue" required="false" %>
<%@ attribute name="dummyvalueId" required="false" %>
<%@ attribute name="filtered" required="false" %>
<%@ attribute name="usecache" required="false" %>
<%@ attribute name="orderby" required="false" %>
<%@ attribute name="optionTitle" required="false" %>
<%@ attribute name="splitOnComma" required="false" %>

<%--
Generates a select tag using records from a single database table. The option with the value will be marked as selected.
Parameters:
name: the html name attribute
value: the current value of select
values: the current values of select
table: the database table name which should be queried.
valuecol: the table column which should be mapped to the value attribute of option tag
displaycol: the table column which should be mapped to the text of the option tag
filtercol: the table column which should be used to filter the query record (WHERE clause) defaults to 'status' column
filtervalue: the value for the filter column (used in WHERE clause)  defaults to 'A'
dummyvalue: a dummy option text to be inserted at the begining.
dummyvalueId: a dummy option value to be inserted at the begining.
filtered: flag to indicate if you want the db records to be filtered or not. Defaults to filtered="true".
usecache: flag to indicate if you want the db records to be cacheable or not. Defaults to usecache="false".
orderby:specified column orderby the query ASC.

Apart from these options you can specify any of the valid html attributes, which will be passed through to the generated select tag

	Example Usage:
	To display a combobox containing a list of only active states queries from state_master, we would specify the tag as:
        <insta:selectdb  name="state" value="" table="state_master" valuecol="state_id" displaycol="state_name" ></insta:selectmaster>

	This will output all the active states in the state_master table
	<select name="state">
	  <option value="ST0001">XYZ</option>
	  ....
	</select>
--%>
<%-- Found that if table do not have column name = "status", filtercol will give error i:e "status column does not exit",
To resovle this error pass filtercol attribute also along with other attribute like valuecol and displaycol etc.
For example Table "u_role" have status column with different name as "role_status" which give error
while using this tag. So to make it work pass additional attribute as "filtercol" along with other attribute.

To display a combobox containing a list of only active role queries from u_role, we would specify the tag as:
<insta:selectdb  name="role" value="" table="u_role" valuecol="role_id" displaycol="role_name" filtercol="role_status" ></insta:selectmaster>

--%>
<%if(filtercol != null){ filtercol = filtercol;} // Added this condition to make this tag work with the table having different name of column status.
else if (filtercol == null) filtercol = "status";
if (filtervalue == null) filtervalue = "A";
if (dummyvalueId == null) dummyvalueId= "";
java.util.List columns = new java.util.ArrayList();
columns.add(valuecol);
columns.add(displaycol);
com.insta.hms.common.GenericDAO dao = null;
if ("true".equals(usecache)) {
  dao = new com.insta.hms.common.BasicCachingDAO(table);
}
else {
  dao = new com.insta.hms.common.GenericDAO(table);
}
java.util.List beans = null;

if ("false".equals(filtered)) {
	if (orderby != null) beans = dao.listAll(columns,orderby);
	else beans = dao.listAll(columns);
} else {
	java.util.HashMap <String,Object>map =  new java.util.HashMap();
	if(filtercol.contains(",")){
		String filterColArray[] =filtercol.split(",");
		String filterValueArray[] = filtervalue.split(",");

		for(int i =0;i<filterColArray.length;i++){
			if(filterColArray[i].equals("center_id")) {
				map.put(filterColArray[i],Integer.valueOf(filterValueArray[i]));
			} else {
				map.put(filterColArray[i],filterValueArray[i]);
			}
		}
	}else{
		if(filtercol.equals("center_id")) {
			map.put(filtercol,Integer.valueOf(filtervalue));
		} else {
			map.put(filtercol,filtervalue);
		}
	}
	if (orderby != null)
		beans = dao.listAll(columns,map,orderby);
	 else
		beans = dao.listAll(columns,map,null);

}
request.setAttribute("tagbeans", beans);
%>
<c:if test="${fn:length(value) gt 0 && empty splitOnComma}">
  <c:set var="values" value="${fn:split(value,',')}"/>
</c:if>

<c:if test="${not empty splitOnComma}">
   <c:set var="values" value="${fn:split(value,'')}"/>
</c:if>

<c:set var="defaultClass" value="${not empty dynattrs.multiple ? 'listbox' : 'dropdown'}"/>
<c:set var="classToUse" value="${(empty dynattrs['class']) ? defaultClass : ''}"/>

<select <c:if test="${not empty classToUse}">class="${classToUse}"</c:if>
<c:forEach items="${dynattrs}" var="a">
 ${a.key}="${ifn:cleanHtml(a.value)}"
</c:forEach>
 name="${name}" >
<c:if test="${dummyvalue != null}">
<option value="${dummyvalueId}" <c:if test="${empty values}">selected='true'</c:if>>${dummyvalue}</option>
</c:if>
<c:forEach items="${tagbeans}" var="option" varStatus="status">
	<c:choose>
		<c:when test="${ifn:arrayFind(values,option.map[valuecol]) ne -1}">
			<c:set var="attr" value="selected='true'"/>
		</c:when>
		<c:otherwise>
               		<c:set var="attr" value=""/>
        	</c:otherwise>
	</c:choose>
	<option value='<c:out value="${option.map[valuecol]}"/>'${attr}<c:if test="${not empty optionTitle}">onmouseover='this.title = "${fn:escapeXml(option.map[displaycol])}"'</c:if>>${option.map[displaycol]}</option>
</c:forEach>
</select>
