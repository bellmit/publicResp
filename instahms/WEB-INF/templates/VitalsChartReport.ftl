<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>IP Record</title>

</head>
<body>
[#setting number_format="#"]
<div align="center" style="font-size: 12pt;"><b>Vitals Chart</b></div>
<div class="patientHeader">
       [#include "VisitDetailsHeader.ftl"]
</div>
<br/><br/>
<div>
		 [#if vitals?has_content]
		 [#assign runningcount = 0]
	     [#assign vitalDisplaySize = 6]
	     [#assign endidx = 0]
	     [#assign startidx = 0]
	 	 [#assign vitalstables = vitals?size / vitalDisplaySize]	
	 	 [#assign vitalsrem = vitals?size % vitalDisplaySize]
	 	 [#if vitalsrem?number != 0]
			[#assign vitalstables = vitalstables+1]
		 [/#if] 
	     [#list 1..vitalstables as i]
	     		[#assign endidx = runningcount + vitalDisplaySize - 1]
	     		[#if endidx > vitals?size - 1]
	     		[#assign endidx = vitals?size - 1]
	     		[/#if]
	     		[#if runningcount != 0]
	     		[#assign startidx = runningcount]
	     		[/#if]
			    <h3 style="margin-top: 10px"><u>Vitals</u></h3>
			    <table cellspacing='0' cellpadding='1' style='margin-top: 5px; empty-cells: show; border: 1px solid; border-collapse:separate;' width="100%" >
			      <tbody>
			        <tr>
			          <th style="border: 1px solid;">Vitals</th>
			    		[#list 1..vitalDisplaySize as j] 
			    			[#list vitals as vital] 
			    	          [#if vital?index = runningcount]
			            		<th style="border: 1px solid;">${vital['date_time']?string('dd-MM-yyyy HH:mm')}
			            		</th>
			             	   [/#if]
			    	 	    [/#list] 			    
			             [#assign runningcount = runningcount+1]
			      	[/#list]
			        </tr> 			       	
		    
			        		[#list vital_params as param]
                    		<tr>                   
                      			<td style="border: 1px solid">${param.param_label!?html}</td> 
                      			[#list startidx..endidx as vi]
                            		<td style="border: 1px solid">${vitals[vi][param.param_label]!?html} ${param.param_uom!}</td>
                      			[/#list]
                    		</tr>
                     		[/#list]
                     		<tr>                   
                      			<td style="border: 1px solid">User</td>
                      				[#list startidx..endidx as v]
                            			<td style="border: 1px solid">${vitals[v]['user_name']}</td>
                            		[/#list]
                    		</tr>	
			      </tbody>
			    </table>    			    
			  [/#list] 
	 [/#if] 
</div>
</body>
</html>
