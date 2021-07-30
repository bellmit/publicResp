package com.insta.hms.mdm.complainttypes;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URLRoute.COMPLAINT_TYPE_MASTER_PATH)
public class ComplaintTypesController extends MasterRestController {

  @LazyAutowired private ComplaintTypesService complaintTypeService;

  public ComplaintTypesController(ComplaintTypesService service) {
    super(service);
  }
}
