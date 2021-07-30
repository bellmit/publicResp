package com.insta.hms.core.clinical.consultation;

import com.insta.hms.mdm.formcomponents.FormComponentsService;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(URLRoute.FOLLOWUP_CONSULTATION_URL)

public class FollowUpConsultationFormController extends ConsultationFormController {
  public FollowUpConsultationFormController(ConsultationFormService service) {
    super(service);
  }

  @Autowired
  BeanFactory beanFactory;

  @Autowired
  public void setConsultationService() {
    super.service = beanFactory.getBean(ConsultationFormService.class,
        FormComponentsService.FormType.Form_OP_FOLLOW_UP_CONS);
  }
}
