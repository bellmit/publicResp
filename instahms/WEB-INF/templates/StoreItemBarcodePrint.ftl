[#list ItemDetails?keys as med]
 [#list 1..ItemDetails[med].qty as x]
^Q38,3
^W75
^H15
^P1
^S4
^AT
^C1
^R0
~Q+0
^O0
^D0
^E12
~R200
^L
Dy2-me-dd
Th:m:s
BA,63,146,3,7,100,0,0,${ItemDetails[med].item_barcode_id}
AC,25,38,1,1,0,0,${ItemDetails[med].medicine_name}
AC,130,38,1,1,0,0,${ItemDetails[med].item_barcode_id}
AC,25,81,1,1,0,0,${ItemDetails[med].mrp}
[#if  (ItemDetails[med].batch_no!"") != ""] 
AC,25,81,1,1,0,0,${ItemDetails[med].batch_no} 
[/#if] 
[#if  (ItemDetails[med].exp_dt??)] 
AC,25,81,1,1,0,0,${ItemDetails[med].exp_dt} 
[/#if] 

  [/#list]
[/#list]

