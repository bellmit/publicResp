package com.insta.hms.mdm.regularexpression;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RegularExpressionService extends MasterService {

  public RegularExpressionService(RegularExpressionRepository repo,
      RegularExpressionValidator validator) {
    super(repo, validator);
  }

  public List<BasicDynaBean> listAll() {
    return getRepository().listAll();
  }

}
