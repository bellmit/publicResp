package com.insta.hms.mdm.systemmessage;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class SystemMessageRepository.
 */
@Repository
public class SystemMessageRepository extends MasterRepository<Integer> {

  /**
    * Instantiates a new system message repository.
    */
  SystemMessageRepository() {
    super("system_messages", "message_id");
  }

  /** The Constant GET_SYS_MSG_LIST. */
  private static final String GET_SYS_MSG_LIST = "SELECT * FROM system_messages "
      + "WHERE system_type = 'User' ORDER BY display_order";

  /**
    * Gets the sys messages.
    *
    * @return the sys messages
    */
  public List<BasicDynaBean> getSysMessages() {
    return DatabaseHelper.queryToDynaList(GET_SYS_MSG_LIST);
  }

}
