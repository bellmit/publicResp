package com.insta.hms.core.inventory.stocks;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.BulkDataController;
import com.insta.hms.mdm.BulkDataResponseRouter;

import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(URLRoute.STOCK_UPLOAD_PATH)
public class StockUploadController extends BulkDataController {
	private static final String FILE_NAME = "store_stock_details";

	public StockUploadController(StockUploadService service) {
		super(service, BulkDataResponseRouter.STOCK_UPLOAD_ROUTER, FILE_NAME);
	}
	@Override
	protected String getImportResponseFormat(
			Map<String, MultiValueMap<Object, Object>> feedback) {
		return RESPONSE_FORMAT_CSV;
	}
	
	@Override
	@RequestMapping(value = {""} , method = RequestMethod.GET)
	public ModelAndView list(HttpServletRequest req, HttpServletResponse resp) {
	  ModelAndView modelView = new ModelAndView();
      modelView.setViewName(router.route("list"));
      return modelView;
	}
}