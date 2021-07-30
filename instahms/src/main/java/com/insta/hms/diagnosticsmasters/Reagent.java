package com.insta.hms.diagnosticsmasters;

import java.math.BigDecimal;

public class Reagent {
	private Integer itemId;
	private BigDecimal neededQty;
	private BigDecimal actualQty;
	private String itemName;
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public BigDecimal getActualQty() {
		return actualQty;
	}
	public void setActualQty(BigDecimal actualQty) {
		this.actualQty = actualQty;
	}
	public Integer getItemId() {
		return itemId;
	}
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}
	public BigDecimal getNeededQty() {
		return neededQty;
	}
	public void setNeededQty(BigDecimal neededQty) {
		this.neededQty = neededQty;
	}

}
