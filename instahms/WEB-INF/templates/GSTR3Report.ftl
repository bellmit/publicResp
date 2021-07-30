<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <title>GSTR3 Report</title>

    <style type="text/css">
        @page {
            size: A4;
            margin: 15pt 15pt 15pt 15pt;
        }
        
        body {
            font-family: Arial, sans-serif;
            font-size: 8pt;
        }
        
        table.report {
            empty-cells: show;
        }
        
        table.report {
            border-collapse: collapse;
            border: 1px solid black;
        }
        
        table.report th {
            border: 1px solid black;
            padding: 2px 8px 2px 3px;
        }
        
        table.report td {
            padding: 2px 4px 2px 4px;
            border: 1px solid black;
        }
        
        table.report td.number {
            text-align: right;
        }
        
        table.report td.totnumber {
            text-align: right;
        }
        
        p.noresult {
            font-weight: bold;
        }
        
        p.heading {
            font-size: 8pt;
            font-weight: bold;
        }
    </style>

</head>

<body>

    <table align="center" width="100%">
        <tbody>

            <tr>
                <td style="font-size:15pt" align="center"><strong>GSTR3 Report</strong></td>
            </tr>

            <tr>
                <td style="font-size:10pt" align="center">${fromDate} - ${toDate}</td>
            </tr>
            <tr>
                <td ></td>
            </tr>
        

            <#if dept_name?has_content>
            <tr>
                <td style="font-size:10pt" align="center">Store=${dept_name!?html}</td>
            </tr>
            <br/>
            </#if>

        </tbody>
    </table>

    <table style="border-collapse:collapse;" border="1" width="100%">
        <tbody>
            <#assign query=" select tax_rate,sum(igst) as igst,sum(cgst)as cgst,sum(sgst) as sgst,dept_name from( select g.grn_no,g.tax_rate,s.dept_name, CASE WHEN isg.item_subgroup_name ilike 'igst%' OR isg.item_subgroup_name ilike 'i-gst%' then (sgtd.tax_amt) else 0 end as igst, CASE WHEN isg.item_subgroup_name ilike 'cgst%' then (sgtd.tax_amt) else 0 end as cgst, CASE WHEN isg.item_subgroup_name ilike 'sgst%' then (sgtd.tax_amt) else 0 end as sgst FROM store_grn_details g JOIN store_grn_main gm USING (grn_no) JOIN stores s on s.dept_id=gm.store_id LEFT JOIN store_grn_tax_details sgtd on sgtd.grn_no =g.grn_no and sgtd.medicine_id=g.medicine_id and sgtd.item_batch_id=g.item_batch_id LEFT JOIN item_sub_groups isg on isg.item_subgroup_id=sgtd.item_subgroup_id WHERE debit_note_no is null and gm.grn_date::date>='${fromDate}' and gm.grn_date::date
            <='${toDate}' and (s.dept_name::text='${dept_name}' OR ( '${dept_name}'='' )) 
            UNION ALL 
            select g.grn_no,(g.tax_rate),s.dept_name, CASE WHEN isg.item_subgroup_name ilike 'igst%' OR isg.item_subgroup_name ilike 'i-gst%' then -(sgtd.tax_amt) else 0 end as igst, CASE WHEN isg.item_subgroup_name ilike 'cgst%' then -(sgtd.tax_amt) else 0 end as cgst, CASE WHEN isg.item_subgroup_name ilike 'sgst%' then -(sgtd.tax_amt) else 0 end as sgst FROM store_debit_note d JOIN store_grn_main gm USING (debit_note_no) JOIN store_grn_details g USING (grn_no) JOIN stores s on s.dept_id=gm.store_id LEFT JOIN store_grn_tax_details sgtd on sgtd.grn_no=g.grn_no and sgtd.medicine_id=g.medicine_id and sgtd.item_batch_id=g.item_batch_id LEFT JOIN item_sub_groups isg on isg.item_subgroup_id=sgtd.item_subgroup_id WHERE d.debit_note_date::date>='${fromDate}' and d.debit_note_date::date
                <='${toDate}' and (s.dept_name::text='${dept_name}' OR ( '${dept_name}'='' )))as foo group by tax_rate ,dept_name order by tax_rate ">

            <#assign queryList=queryToDynaList(query)!empty>

            <#assign query1="select dept_name,tax_rate ,sum(sgst) as sgst,sum(cgst) as cgst FROM( select sd.sale_item_id,sd.tax_rate,CASE WHEN isg.item_subgroup_name ilike 'cgst%' then (sstd.tax_amt) else 0 end as cgst, CASE WHEN isg.item_subgroup_name ilike 'sgst%' then (sstd.tax_amt) else 0 end as sgst,s.dept_name FROM store_sales_main sm LEFT JOIN store_sales_details sd on sd.sale_id=sm.sale_id JOIN stores s on s.dept_id=sm.store_id LEFT JOIN store_sales_tax_details sstd on sstd.sale_item_id=sd.sale_item_id LEFT JOIN item_sub_groups isg on isg.item_subgroup_id=sstd.item_subgroup_id WHERE sale_date::date>='${fromDate}' and sale_date::date
                    <='${toDate}' and (s.dept_name::text='${dept_name}' OR ( '${dept_name}'='' ))) as foo group by tax_rate, dept_name order by tax_rate ">
            <#assign queryList1=queryToDynaList(query1)!empty>

                    <tr>
                    <th width="15% " colspan="4 " align="center " ><strong>INPUT(Purchase/Return)</strong></th>
                    <th width="15% " colspan="2 " align="center "><strong>OUTPUT(Sales/Return)</strong></th>
                    <th width="15% " colspan="3 " align="center "><strong>GST Payable</strong></th>
                    </tr>
                    <tr>
                    <th width="5% " align="center "><strong>GST(%)</strong></th>
                    <th width="5% "  align="center "><strong>IGST</strong></th>
                    <th width="5% " align="center "><strong>CGST</strong></th>
                    <th width="5% " align="center "><strong>SGST</strong></th>
                    <th width="5% " align="center "><strong>CGST</strong></th>
                    <th width="5% " align="center "><strong>SGST</strong></th>
                    <th width="5% " align="center "><strong>IGST</strong></th>
                    <th width="5% " align="center "><strong>CGST</strong></th>
                    <th width="5% " align="center "><strong>SGST</strong></th>
                    </tr>
                    <#assign zcgst_i=0>
                    <#assign zsgst_i=0>
                    <#assign zigst_i=0>
                    <#assign zcgst_o=0>
                    <#assign zsgst_o=0>

                    <#assign fcgst_i=0>
                    <#assign fsgst_i=0>
                    <#assign figst_i=0>
                    <#assign fcgst_o=0>
                    <#assign fsgst_o=0>

                    <#assign twcgst_i=0>
                    <#assign twsgst_i=0>
                    <#assign twigst_i=0>
                    <#assign twcgst_o=0>
                    <#assign twsgst_o=0>

                    <#assign ecgst_i=0>
                    <#assign esgst_i=0>
                    <#assign eigst_i=0>
                    <#assign ecgst_o=0>
                    <#assign esgst_o=0>

                    <#assign tcgst_i=0>
                    <#assign tsgst_i=0>
                    <#assign tigst_i=0>
                    <#assign tcgst_o=0>
                    <#assign tsgst_o=0>


<!--Tax Rate =0 -->

<#list queryList as query>
    <#if query.tax_rate=0.00> 
        <#assign zigst_i=zigst_i+query.igst> 
        <#assign zcgst_i=zcgst_i+query.cgst> 
        <#assign zsgst_i=zsgst_i+query.sgst> 
    </#if>
</#list> 

<#list queryList1 as query1>
    <#if query1.tax_rate=0.00>
        <#assign zcgst_o=zcgst_o+query1.cgst> 
        <#assign zsgst_o=zsgst_o+query1.sgst>     
    </#if>     
</#list>

                    <tr>

                    <td width="5% ">0</td>

                    <td width="5% ">${zigst_i}</td>
                    <td width="5% ">${zcgst_i}</td>
                    <td width="5% ">${zsgst_i}</td>

                    <td width="5% ">${zcgst_o}</td>
                    <td width="5% ">${zsgst_o}</td>

                    <td width="5% ">${zigst_i}</td>
                    <td width="5% ">${(zcgst_i-zcgst_o)}</td>
                    <td width="5% ">${(zsgst_i-zsgst_o)}</td>
                    </tr>

<!--Tax Rate =5 -->

<#list queryList as query>
    <#if query.tax_rate=5.00> 
        <#assign figst_i=figst_i+query.igst> 
        <#assign fcgst_i=fcgst_i+query.cgst> 
        <#assign fsgst_i=fsgst_i+query.sgst> 
    </#if>
</#list> 


<#list queryList1 as query1>
    <#if query1.tax_rate=5.00>
        <#assign fcgst_o=fcgst_o+query1.cgst> 
        <#assign fsgst_o=fsgst_o+query1.sgst>     
    </#if>     
</#list>

                    <tr>

                    <td width="5% ">5</td>

                    <td width="5% ">${figst_i!}</td>
                    <td width="5% ">${fcgst_i!}</td>
                    <td width="5% ">${fsgst_i!}</td>

                    <td width="5% ">${fcgst_o!}</td>
                    <td width="5% ">${fsgst_o!}</td>

                    <td width="5% ">${figst_i!}</td>
                    <td width="5% ">${(fcgst_i-fcgst_o)}</td>
                    <td width="5% ">${(fsgst_i-fsgst_o)}</td>
                    </tr>

                    
<!--Tax Rate =12 -->

<#list queryList as query>
    <#if query.tax_rate=12.00> 
        <#assign twigst_i=twigst_i+query.igst> 
        <#assign twcgst_i=twcgst_i+query.cgst> 
        <#assign twsgst_i=twsgst_i+query.sgst> 
    </#if>
</#list> 

<#list queryList1 as query1>
    <#if query1.tax_rate=12.00>
        <#assign twcgst_o=twcgst_o+query1.cgst> 
        <#assign twsgst_o=twsgst_o+query1.sgst>     
    </#if>     
</#list>

                    <tr>

                    <td width="5% ">12</td>

                    <td width="5% ">${twigst_i!}</td>
                    <td width="5% ">${twcgst_i!}</td>
                    <td width="5% ">${twsgst_i!}</td>
                    <td width="5% ">${twcgst_o!}</td>
                    <td width="5% ">${twsgst_o!}</td>

                    <td width="5% ">${twigst_i!}</td>
                    <td width="5% ">${(twcgst_i-twcgst_o)}</td>
                    <td width="5% ">${(twsgst_i-twsgst_o)}</td>
                    </tr>

<!--Tax Rate =18 -->

<#list queryList as query>
    <#if query.tax_rate=18.00> 
        <#assign eigst_i=eigst_i+query.igst> 
        <#assign ecgst_i=ecgst_i+query.cgst> 
        <#assign esgst_i=esgst_i+query.sgst> 
    </#if>
</#list>

 <#list queryList1 as query1>
    <#if query1.tax_rate=18.00>
        <#assign ecgst_o=ecgst_o+query1.cgst> 
        <#assign esgst_o=esgst_o+query1.sgst>     
    </#if>     
</#list>

                    <tr>

                    <td width="5% ">18</td>

                    <td width="5% ">${eigst_i!}</td>
                    <td width="5% ">${ecgst_i!}</td>
                    <td width="5% ">${esgst_i!}</td>

                    <td width="5% ">${ecgst_o!}</td>
                    <td width="5% ">${esgst_o!}</td>

                    <td width="5% ">${eigst_i!}</td>
                    <td width="5% ">${(ecgst_i-ecgst_o)}</td>
                    <td width="5% ">${(esgst_i-esgst_o)}</td>
                    </tr>

<!--Tax Rate =28 -->

<#list queryList as query>
    <#if query.tax_rate=28.00> 
        <#assign tigst_i=tigst_i+query.igst> 
        <#assign tcgst_i=tcgst_i+query.cgst> 
        <#assign tsgst_i=tsgst_i+query.sgst> 
    </#if>
</#list> 

<#list queryList1 as query1>
    <#if query1.tax_rate=28.00>
        <#assign tcgst_o=tcgst_o+query1.cgst> 
        <#assign tsgst_o=tsgst_o+query1.sgst>     
    </#if>     
</#list>            

                    <tr>

                    <td width="5% ">28</td>
                    <td width="5% ">${tigst_i!}</td>
                    <td width="5% ">${tcgst_i}</td>
                    <td width="5% ">${tsgst_i}</td>

                    <td width="5% ">${tcgst_o}</td>
                    <td width="5% ">${tsgst_o}</td>

                    <td width="5% ">${tigst_i}</td>
                    <td width="5% ">${(tcgst_i-tcgst_o)}</td>
                    <td width="5% ">${(tsgst_i-tsgst_o)}</td>
                    </tr>
               </tbody>
        </table>

    </body>

</html>