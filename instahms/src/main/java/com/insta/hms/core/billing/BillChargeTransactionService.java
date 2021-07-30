package com.insta.hms.core.billing;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.order.serviceitems.ServiceOrderItemService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillChargeTransactionService extends BusinessService {

  @LazyAutowired
  private BillChargeTransactionRepository billChargeTransactionRepository;

  @LazyAutowired
  private ServiceOrderItemService serviceOrderService;

  public BasicDynaBean getBean() {
    return  billChargeTransactionRepository.getBean();
  }

  public BasicDynaBean populateServiceCode(BasicDynaBean transactionBean, String serviceId,
      String orgId) {
    BasicDynaBean spclServiceCodeBean = serviceOrderService.getMasterSpecialCodeBean(serviceId,
        orgId);
    if (spclServiceCodeBean != null) {
      String splServiceCode = (String) spclServiceCodeBean.get("special_service_code");
      String splServiceContract = (String) spclServiceCodeBean.get("special_service_contract_name");
      if ((splServiceCode != null && !splServiceCode.isEmpty())
          || (splServiceContract != null && !splServiceContract.isEmpty())) {
        transactionBean.set("transaction_id", billChargeTransactionRepository.getNextSequence());
        transactionBean.set("special_service_code", splServiceCode);
        transactionBean.set("special_service_contract_name", splServiceContract);
      }
    }
    return transactionBean;
  }

  public void insert(BasicDynaBean bean) {
    billChargeTransactionRepository.insert(bean);
  }

  public void insert(List<BasicDynaBean> beans) {
    billChargeTransactionRepository.batchInsert(beans);
  }
}
