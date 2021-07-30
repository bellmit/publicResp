package com.insta.hms.core.inventory.procurement;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author irshadmohammed
 *
 */
@Repository
public class PurchaseOrderTaxDetailsRepository extends GenericRepository {
	
	public PurchaseOrderTaxDetailsRepository() {
		super("store_po_tax_details");
	}
	
	private static String INSERT_PO_TAX_DETAILS = "INSERT INTO store_po_tax_details(medicine_id, po_no, item_subgroup_id, tax_rate, tax_amt) values(?, ?, ?, ?, ?)";
	
	private static String UPDATE_PO_DETAILS = "UPDATE store_po SET adj_mrp = ?, vat_rate = ?, vat = ?, med_total = ? WHERE po_no = ? AND medicine_id = ?";
	
	public boolean updateTaxDetails(Map<String, Object> taxMap, String poNo, int medicine_id, BasicDynaBean newPOBean) throws SQLException {
		boolean status = false;
		Iterator<Entry<String, Object>> taxMapIterator = taxMap.entrySet().iterator();
		BigDecimal totalTaxRate = BigDecimal.ZERO;
		BigDecimal totalTaxAmount = BigDecimal.ZERO;
		while(taxMapIterator.hasNext()) {
			Entry<String, Object> taxMapEntry = taxMapIterator.next();
			Map<String, String> taxDetailsMap = (Map<String, String>)taxMapEntry.getValue();
			BigDecimal taxRate = new BigDecimal(taxDetailsMap.get("rate"));
			BigDecimal taxAmount = new BigDecimal(taxDetailsMap.get("amount"));
			totalTaxRate = totalTaxRate.add(taxRate);
			totalTaxAmount = totalTaxAmount.add(taxAmount);
			status = DatabaseHelper.update(INSERT_PO_TAX_DETAILS, new Object[]{medicine_id, poNo, taxDetailsMap.get("subgroup_id"), taxRate, taxAmount}) > 0;
		}
		if(status) {
			BigDecimal qty = (BigDecimal)newPOBean.get("qty_req");
			BigDecimal costPrice = (BigDecimal)newPOBean.get("cost_price");
			BigDecimal discountAmt = (BigDecimal)newPOBean.get("discount");
			BigDecimal pkgSize = (BigDecimal)newPOBean.get("po_pkg_size");
			BigDecimal mrp = (BigDecimal)newPOBean.get("mrp");
			BigDecimal medTotal = BigDecimal.ZERO;
			// adjMrp = mrp/(1 + taxRate/100);
			BigDecimal adjMrp = mrp.divide(BigDecimal.ONE.add((totalTaxRate.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_DOWN))), 2, RoundingMode.HALF_DOWN);
			//med total = costPrice * qty / pkgsize - discount Amount + tax Amount
			medTotal = costPrice.multiply((qty.divide(pkgSize, 2, RoundingMode.HALF_DOWN))).subtract(discountAmt).add(totalTaxAmount);
			status = DatabaseHelper.update(UPDATE_PO_DETAILS, new Object[]{adjMrp, totalTaxRate, totalTaxAmount, medTotal, poNo, medicine_id}) > 0;
		}
		return status;
	}
}