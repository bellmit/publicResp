package com.insta.hms.api.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name="vouchers")
public class HmsAccountinngInfoList {

    protected List<HmsAccountingInfoXMLBinding> accountingList;


    @XmlElement(name="voucher")
    public List<HmsAccountingInfoXMLBinding> getAccountingList(){
        return this.accountingList;
    }

    public void setAccountingList(List<HmsAccountingInfoXMLBinding> accountingList){
        this.accountingList = accountingList;
    }
}
