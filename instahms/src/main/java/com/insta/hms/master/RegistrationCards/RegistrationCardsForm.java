package com.insta.hms.master.RegistrationCards;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class RegistrationCardsForm extends ActionForm {

	private transient FormFile custom_reg_card_template;
	private FormFile odtFile;
	private String cardName;
	private String ratePlan;
	private boolean sortReverse;
	private String pageNum;
	private String sortOrder;
	private boolean ratecardAll;
	private boolean ratecardActive;
	private boolean ratecardInActive;
	private String cardId;

	public FormFile getOdtFile() {
		return odtFile;
	}
	public void setOdtFile(FormFile odtFile) {
		this.odtFile = odtFile;
	}
	public FormFile getCustom_reg_card_template() {return custom_reg_card_template;}
	public void setCustom_reg_card_template(FormFile custom_reg_card_template) {this.custom_reg_card_template = custom_reg_card_template;}

	public String getCardName() {return cardName;}
	public void setCardName(String v) {this.cardName = v;}

	public String getPageNum() {return pageNum;}
	public void setPageNum(String v) {this.pageNum = v;}

	public boolean isRatecardActive() {return ratecardActive;}
	public void setRatecardActive(boolean ratecardActive) {this.ratecardActive = ratecardActive;}

	public boolean isRatecardAll() {return ratecardAll;}
	public void setRatecardAll(boolean ratecardAll) {this.ratecardAll = ratecardAll;}

	public boolean isRatecardInActive() {return ratecardInActive;}
	public void setRatecardInActive(boolean ratecardInActive) {this.ratecardInActive = ratecardInActive;}

	public String getRatePlan() {return ratePlan;}
	public void setRatePlan(String v) {this.ratePlan = v;}

	public String getSortOrder() {return sortOrder;}
	public void setSortOrder(String v) {this.sortOrder = v;}

	public boolean isSortReverse() {return sortReverse;}
	public void setSortReverse(boolean sortReverse) {this.sortReverse = sortReverse;}

	public static final String RATE_PLAN_ACTIVE = "A";
	public static final String RATE_PLAN_INACTIVE = "I";

	public String getCardId() {return cardId;}
	public void setCardId(String v) {this.cardId = v;}

}
