package com.insta.hms.jobs;

import com.insta.hms.common.BaseController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.master.URLRoute;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.CronExpression;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class CronJobController.
 */
@Controller
@RequestMapping(URLRoute.CRON_JOB_PATH)
public class CronJobController extends BaseController {

  /** The job service. */
  @LazyAutowired
  private CronJobService jobService;
  
  private static final String JOB_INDEX = "job_index";

  /**
   * Gets the scheduler screen.
   *
   * @param request the request
   * @param response the response
   * @return the scheduler screen
   * @throws ParseException the parse exception
   */
  @RequestMapping(value = { "show", "" }, method = RequestMethod.GET)
  public ModelAndView getSchedularScreen(HttpServletRequest request, HttpServletResponse response)
      throws ParseException {

    ModelAndView mav = new ModelAndView();
    List<BasicDynaBean> scheduledJobs = jobService.getAllScheduledJob();
    for (int i = 0; i < scheduledJobs.size(); i++) {
      String jobTime = (String) scheduledJobs.get(i).get("job_time");
      Timestamp nextRunTime = getNextRunTime(jobTime);
      scheduledJobs.get(i).set("job_next_runtime", nextRunTime);
    }
    mav.addObject("scheduledJobList", ConversionUtils.copyListDynaBeansToMap(scheduledJobs));
    mav.setViewName(URLRoute.CRON_JOB_SHOW);
    return mav;
  }

  /**
   * Update scheduler cron job.
   *
   * @param request the request
   * @param response the response
   * @param redirectAttributes the redirect attributes
   * @return the model and view
   */
  @RequestMapping(value = "update", method = RequestMethod.POST)
  public ModelAndView updateSchedulerCronJob(HttpServletRequest request,
      HttpServletResponse response, RedirectAttributes redirectAttributes) {

    Map<String, String[]> params = request.getParameterMap();
    BasicDynaBean schBasicDynaBean = jobService.toBean(params);
    ConversionUtils.copyToDynaBean(params, schBasicDynaBean);
    Map<String, Object> keys = new HashMap<>();
    keys.put("job_id", Integer.parseInt(request.getParameter("job_id")));
    jobService.update(schBasicDynaBean, keys);
    redirectAttributes.addAttribute(JOB_INDEX, params.get(JOB_INDEX)[0]);
    ModelAndView mav = new ModelAndView();
    mav.setViewName(URLRoute.CRON_JOB_SHOW_REDIRECT);
    return mav;
  }

  /**
   * Reschedule.
   *
   * @param request the request
   * @param response the response
   * @param redirectAttributes the redirect attributes
   * @return the model and view
   */
  @RequestMapping(value = "reschedule", method = RequestMethod.GET)
  public ModelAndView reschedule(HttpServletRequest request, HttpServletResponse response,
      RedirectAttributes redirectAttributes) {
    ModelAndView mav = new ModelAndView();
    jobService.reschedule();
    redirectAttributes.addAttribute(JOB_INDEX, request.getParameter(JOB_INDEX));
    mav.setViewName(URLRoute.CRON_JOB_SHOW_REDIRECT);
    return mav;
  }

  /**
   * Gets the next run time.
   *
   * @param jobTime the job time
   * @return the next run time
   * @throws ParseException the parse exception
   */
  private Timestamp getNextRunTime(String jobTime) throws ParseException {
    CronExpression cronExpression = new CronExpression(jobTime);
    Date nextRunTime = cronExpression.getNextValidTimeAfter(new Date());
    return new Timestamp(nextRunTime.getTime());
  }
}
