package com.insta.hms.mdm.breaktheglass;

import com.insta.hms.mdm.MasterRestController;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("userMrnoAsssociationController")
@RequestMapping("/master/breaktheglass")
public class UserMrnoAssociationController extends MasterRestController {
  
  UserMrnoAssociationService mrnoService;

  public UserMrnoAssociationController(UserMrnoAssociationService service) {
    super(service);
    this.mrnoService = service;
  }
  
  @Override
  @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
  protected ResponseEntity create(HttpServletRequest req, HttpServletResponse resp,
      @RequestBody ModelMap requestBody) {
    BasicDynaBean bean = jsonToBean(requestBody);
    mrnoService.insert(bean, (String)requestBody.get("remarks"));
    return new ResponseEntity(bean.getMap(), HttpStatus.CREATED);
  }
}

