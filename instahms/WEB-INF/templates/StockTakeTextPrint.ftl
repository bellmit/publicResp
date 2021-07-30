[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]

[#assign header='Remarks' hlen=7]
[#if extended_columns?? && extended_columns == "Y"]
[#assign header='Sys.Qty Variance Remarks' hlen=25]
[/#if]
[@cfill "Stock Take" 74/]

---------------------------------------------------------------------------------
Store Name     : [@lfill store.dept_name!"" 20 /] Stock Take No: [@lfill stock_take.stock_take_id!"" 20 /] 
Stock Take Date: [@lfill stock_take.initiated_datetime!"" 20 /] Initiated By: [@lfill stock_take.initiated_by!"" 20 /]

---------------------------------------------------------------------------------
#  Item/Med                Batch No.        Phy.Qty [@rfill header hlen/]
---------------------------------------------------------------------------------

[#assign sno=1 total_qty_variance=0]
[#if stock_take_details??]
[#list stock_take_details as s]
[#assign phyQty='' variance='']
[#if s.physical_stock_qty??]
  [#assign phyQty=s.physical_stock_qty?string("##0.00")]
  [#assign variance=(s.physical_stock_qty!0 - s.system_stock_qty!0)?string("##0.00")]
[/#if]
[@rfill sno?string("#") 2/] [@lfill s.medicine_name!"" 23/] [@lfill s.batch_no!"" 15/] [#t]
 [@rfill phyQty 8/] [#t]
 [#if extended_columns?? && extended_columns == "Y"]
 [@rfill (s.system_stock_qty!0)?string("##0.00") 8/] [@rfill variance 8/] [#t]
 [/#if]
 [@lfill s.adjustment_reason!"" 10/]

[#assign total_qty_variance=(total_qty_variance+s.physical_stock_qty!0-s.system_stock_qty!0) sno=sno+1]

[/#list]
[/#if]
----------------------------------------------------------------------------------

Reconciled By         : [@lfill stock_take.reconciled_by!"" 25/] On [@lfill stock_take.reconciled_datetime!"" 18/]
Approved & Adjusted By: [@lfill stock_take.approved_by!"" 25/] On [@lfill stock_take.approved_datetime!"" 18/]

