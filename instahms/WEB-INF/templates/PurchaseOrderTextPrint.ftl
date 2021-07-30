[#include "TextPrintMacros.ftl"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]

[@cfill "Purchase Order" 74/]

Hospital Reg No:  [@lfill hospital_service_regn_no !"" 20 /] [@rfill "Hospital Pan No:" 17/] [@rfill hospital_pan !"" 20 /]
Hospital Tin No:  [@lfill hospital_tin !"" 20 /]             [@rfill "Credit Period:" 17/] [@rfill items[0].credit_period!"" 20 /]
Indent Process No:[@lfill indentNo!"" 20 /]                  [@rfill "Delivery Date:" 17/] [@rfill items[0].delivery_date!"" 20 /]
Auth/Amnd Dt:      [@lfill poBean.approved_time!"" 20 /]
PO No:     [@lfill items[0].po_no 20 /] [@rfill "PO Date:" 17/]  [@rfill items[0].po_date 20 /]
Quotation No:     [@lfill items[0].qut_no!"" 20 /]                  [@rfill "Quotation Date:" 17/] [@rfill items[0].qut_date 20 /]
Supplier Address:     [@lfill items[0].supplier_name 20 /]       [@rfill "Reference:" 17/] [@rfill items[0].reference!"" 20 /]
                      [@lfill items[0].supplier_address!"" 20 /]
                      [@lfill items[0].city_name!"" 20 /]
                      [@lfill items[0].state_name!"" 20 /]
                      [@lfill items[0].country_name!"" 20 /]
Store Name:    [@lfill items[0].store_name 20 /]
Dept. Name:    [@lfill dept_name!"" 20 /] [@rfill "Remarks:" 17/] [@rfill poBean.remarks!"" 20 /]

---------------------------------------------------------------------------------
#  Item/Med               Unt  Qty   BQty  MRP  AdjMRP  Rate TAX%  TAX Disc  Amt
---------------------------------------------------------------------------------

[#assign sno=1 total_qty=0 total_discount=0 total_tax_amt=0 total_amt=0]

[#list items as s]
[@rfill sno?string("#") 2/] [@lfill s.medicine_name 25/]  [#if s.issue_units?has_content]${s.issue_units}[/#if] [#t]
 [@lfill (s.qty_req/s.po_pkg_size)?string("##.00") 6/]  [@lfill (s.bonus_qty_req/s.po_pkg_size)?string("##.00") 6/] [@rfill s.mrp 8/][#t]
 [@rfill s.adj_mrp 8/] [@rfill s.cost_price 8/] [@rfill s.vat_rate?string("##.00") 8/] [@rfill s.discount 8/] [@rfill s.med_total 8/]

[#assign total_qty=(total_qty+s.qty_req) total_tax_amt=(total_tax_amt+s.vat) total_discount=(total_discount+s.discount) total_amt=(total_amt+s.med_total+poBean.map.transportation_charges) sno=sno+1]

[/#list]
----------------------------------------------------------------------------------

[@rfill "Totals:" 17/] [@rfill total_tax_amt 20 /] [@rfill total_discount 20 /] [@rfill total_amt 20 /]
Discount:      [@lfill poBean.map.discount 20 /]
TCS Amt:       [@lfill poBean.map.tcs_amount 20 /]
Round Off:      [@lfill poBean.map.round_off 20 /]
Transportation Charges:      [@lfill poBean.map.transportation_charges 20 /]
Grand Total:    [@lfill total_amt - poBean.map.discount + poBean.map.tcs_amount + poBean.map.round_off 20 /]

Supplier Terms And Conditions:  [@lfill items[0].supplier_terms!"" 100 /]
Hospital Terms And Conditions:  [@lfill items[0].hospital_terms!"" 100 /]

User:  [@lfill items[0].user_id!"" 25/] [@lfill items[0].actual_po_date!"" 25 /]
Prepared By:
Verified By:
Authorized By:

