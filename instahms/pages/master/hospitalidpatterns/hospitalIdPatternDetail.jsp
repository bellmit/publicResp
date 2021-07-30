<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<jsp:useBean id="seqResetFreqDisplay" class="java.util.HashMap"/>
<c:set target="${seqResetFreqDisplay}" property="D" value="Daily"/>
<c:set target="${seqResetFreqDisplay}" property="M" value="Monthly"/>
<c:set target="${seqResetFreqDisplay}" property="Y" value="Calender Year"/>
<c:set target="${seqResetFreqDisplay}" property="F" value="Financial Year"/>

<insta:link type="script" file="master/sequences/hospitalIdPatterns.js"/>
	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Hospital Id Pattern</legend>
	<table class="formtable" >
		<tr>
	
			<td class="formlabel">Standard Prefix:</td>
			<td>
				<input type="text" id="std_prefix" name="std_prefix" value="${hospBean.std_prefix}" length="50" disabled />
			</td>
			<td class="formlabel">Sequence Name:</td>
			<td>
				<input type="text" id="sequence_name" name="sequence_name" value="${hospBean.sequence_name}" length="50" disabled />
			</td>
			<td class="formlabel">Number Pattern:</td>
			<td>
				<label id="num_pattern">${hospBean.num_pattern}</label>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Sequence Reset Frequency:</td>
			<td>
				<select id="sequence_reset_freq" name="sequence_reset_freq" disabled>
					<option value="">Select</option>
					<c:forEach items="${seqResetFreqDisplay}" var="entry">
						<option value="${entry.key}" ${entry.key == hospBean.sequence_reset_freq ? 'selected' : ''}>${entry.value}</option>
					</c:forEach>
				</select>
			</td>
			<td class="formlabel">Date Prefix Pattern:</td>
			<td>
				<label id="date_prefix_pattern">${hospBean.date_prefix_pattern}</label>
			</td>
			<td class="formlabel">Date Prefix:</td>
			<td>
				<label id="date_prefix">${hospBean.date_prefix}</label>
			</td>
			
		</tr>
	</table>
	</fieldset>
