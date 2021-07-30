<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  <style type="text/css">
    @page {
    size: A4 landscape;
    margin: 36pt 36pt 36pt 36pt;
  }
    body {
    font-family : Arial, Helvetica;
    font-size : 10pt;
  }
  </style>
  </head>
[#escape x as x?html]
<body>
  <h1>Stock Take</h1>
  [#setting number_format="#"]
  [#setting datetime_format = "dd-MM-yyyy HH:MM"]
  <table width='100%'>
    <tr>
      <td>Store Name :</td>
      <td>${(store.dept_name)!}</td>
      <td>Stock Take No :</td>
      <td>${stock_take.stock_take_id!}</td>
    </tr>
    <tr>
      <td>Stock Take Date :</td>
      <td>${stock_take.initiated_datetime!}</td>
      <td>Initiated By:</td>
      <td>${stock_take.initiated_by!}</td>
    </tr>
  </table>
  <br/>
  <table width="100%" cellpadding="4" cellspacing="1" border="1">
    <tr>
      <td width="5%">#</td>
      <td width="30%">Item Name</td>
      <td width="12%">Batch No</td>
      <td width="10%" align="right">Physical Qty</td>
      [#if extended_columns?? && extended_columns == "Y"]
      <td width="10%" align="right">System Qty</td>
      <td width="10%" align="right">Variance</td>
      [/#if]
      <td width="23%">Remarks</td>
    </tr>
    [#assign sno = 0]
    [#if stock_take_details??]
    [#list stock_take_details as st]
      <tr>
        [#assign sno = sno+1]
        [#assign physicalQty = '']
        [#assign variance = '']
        <td>${sno?string('#')}</td>
        <td>${st.medicine_name!}</td>
        <td>${st.batch_no!}</td>
        [#if st.physical_stock_qty??]
          [#assign physicalQty = st.physical_stock_qty?string('##0.00')]
          [#assign variance = ((st.physical_stock_qty!0.0000)-(st.system_stock_qty!0.0000))?string('##0.00')]
        [/#if]
        <td align="right">${physicalQty!''}</td>
        [#if extended_columns?? && extended_columns == "Y"]
        <td align="right">${(st.system_stock_qty!0.0000)?string('##0.00')}</td>
        <td align="right">${variance!}</td>
        [/#if]
        <td>${st.adjustment_reason!}</td>
      </tr>
    [/#list]

    [/#if]
  </table>
</body>
[/#escape]
</html>
