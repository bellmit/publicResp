package com.insta.hms.mdm.role;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * The Class RoleMasterService.
 */
@Service
public class RoleMasterService extends MasterService {

  /** The role repository. */
  @LazyAutowired
  private RoleMasterRepository roleRepository;
  
  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /**
   * Instantiates a new role master service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public RoleMasterService(RoleMasterRepository repository, RoleValidator validator) {
    super(repository, validator);
    roleRepository = repository;
  }

  /**
   * Roles.
   *
   * @return the map
   * @throws SQLException the SQL exception
   */
  public Map<String, Object> roles() throws SQLException {
    Map<String, Object> map = new HashMap();
    Map sessionAttributes = sessionService.getSessionAttributes();
    boolean excludeInstaAdmin = false;
    Integer roleId = (Integer)sessionAttributes.get("roleId");
    Integer loggedInRoleId = (Integer)(sessionAttributes.get("loggedInRoleId") == null 
          ? roleId : sessionAttributes.get("loggedInRoleId"));
    if (loggedInRoleId == 2) {
      excludeInstaAdmin = true;
    }
    map.put("roles", ConversionUtils
        .copyListDynaBeansToMap(roleRepository.roles(excludeInstaAdmin)));
    map.put("sessionMap", sessionService.getSessionAttributes());
    return map;
  }
}
