package com.insta.hms.core.cashlimit;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

/**
 * The Class CashLimitController.
 */

@Controller("cashLimitController")
@RequestMapping("/cashlimit")
public class CashLimitController extends BaseRestController {

	private static final String GET_CASH_LIMIT = "/getcashlimit";

	private static final String GET_DEPSOIT_CASH_LIMIT = "/getdepositcashlimit";

	/** The service. */
	@LazyAutowired
	private CashLimitService cashLimitService;

	@GetMapping(value = GET_CASH_LIMIT)
	public ModelMap getCashLimitPayments(@RequestParam(name = "mr_no") String mrNo,
			@RequestParam(required = true, name = "visit_id") String visitId,
			@RequestParam(required = true, name = "cash_payment") BigDecimal amt,
			@RequestParam(name = "refund_payment") BigDecimal refundAmt,
			@RequestParam(name = "gen_deposit_setoff") BigDecimal generalDepositSetoff,
			@RequestParam(name = "ip_deposit_setoff") BigDecimal ipDepositSetoff,
			@RequestParam(name = "package_deposit_setoff") BigDecimal packageDepositSetoff) {
		ModelMap modelMap = new ModelMap();
		Map<String, BigDecimal> cashLimitBean = cashLimitService.cashLimitPayments(mrNo, visitId, amt, refundAmt,
				generalDepositSetoff, ipDepositSetoff, packageDepositSetoff);
		return modelMap.addAttribute("cash_limit_details", cashLimitBean == null ? null : cashLimitBean);
	}

	@GetMapping(value = GET_DEPSOIT_CASH_LIMIT)
	public ModelMap getDepositCashLimitPayments(@RequestParam(name = "mr_no") String mrNo,
			@RequestParam(required = true, name = "deposit_cash_payment") BigDecimal amt,
			@RequestParam(name = "deposit_refund_payment") BigDecimal refundAmt) {
		ModelMap modelMap = new ModelMap();
		Map<String, BigDecimal> depositCashLimitBean = cashLimitService.depositCashLimitPayments(mrNo, amt, refundAmt);
		return modelMap.addAttribute("deposit_cash_limit_details",
				depositCashLimitBean == null ? null : depositCashLimitBean);
	}
}
