/* $Id$ */

package com.insta.hms.extension.accounting.zoho.books.parser;

import com.insta.hms.extension.accounting.zoho.books.model.Comment;
import com.insta.hms.extension.accounting.zoho.books.model.CommentList;
import com.insta.hms.extension.accounting.zoho.books.model.Expense;
import com.insta.hms.extension.accounting.zoho.books.model.ExpenseList;
import com.insta.hms.extension.accounting.zoho.books.model.PageContext;
import com.insta.hms.extension.accounting.zoho.books.model.RecurringExpense;
import com.insta.hms.extension.accounting.zoho.books.model.RecurringExpenseList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * RecurringExpenseParser is used to parse the json response and make it into the respective
 * objects.
 */

public class RecurringExpenseParser {

  /**
   * Parse the json response and returns the RecurringExpense object.
   *
   * @param response
   *          This json response contains the recurring expense details.
   * @return Returns the RecurringExpense object.
   * @throws Exception
   *           the exception
   */

  public RecurringExpense getRecurringExpense(String response) throws Exception {
    RecurringExpense recurringExpense = new RecurringExpense();

    JSONObject jsonObject = new JSONObject(response.trim());

    JSONObject recExpense = jsonObject.getJSONObject("recurring_expense"); // No I18N

    recurringExpense.setRecurringExpenseId(recExpense.getString("recurring_expense_id"));
    recurringExpense.setRecurrenceName(recExpense.getString("recurrence_name"));
    recurringExpense.setStartDate(recExpense.getString("start_date"));
    recurringExpense.setEndDate(recExpense.getString("end_date"));
    recurringExpense.setRecurrenceFrequency(recExpense.getString("recurrence_frequency"));
    recurringExpense.setRepeatEvery(recExpense.getInt("repeat_every")); // No I18N
    recurringExpense.setLastCreatedDate(recExpense.getString("last_created_date"));
    recurringExpense.setNextExpenseDate(recExpense.getString("next_expense_date"));
    recurringExpense.setAccountId(recExpense.getString("account_id"));
    recurringExpense.setAccountName(recExpense.getString("account_name"));
    recurringExpense.setPaidThroughAccountId(recExpense.getString("paid_through_account_id"));
    recurringExpense.setPaidThroughAccountName(recExpense.getString("paid_through_account_name"));
    recurringExpense.setVendorId(recExpense.getString("vendor_id"));
    recurringExpense.setVendorName(recExpense.getString("vendor_name"));
    recurringExpense.setTaxId(recExpense.getString("tax_id"));
    recurringExpense.setTaxName(recExpense.getString("tax_name"));
    recurringExpense.setTaxPercentage(recExpense.getDouble("tax_percentage")); // No I18N
    recurringExpense.setCurrencyId(recExpense.getString("currency_id"));
    recurringExpense.setCurrencyCode(recExpense.getString("currency_code"));
    recurringExpense.setExchangeRate(recExpense.getDouble("exchange_rate")); // No I18N
    recurringExpense.setTaxAmount(recExpense.getDouble("tax_amount")); // No I18N
    recurringExpense.setSubTotal(recExpense.getDouble("sub_total")); // No I18N
    recurringExpense.setTotal(recExpense.getDouble("total")); // No I18N
    recurringExpense.setBcyTotal(recExpense.getDouble("bcy_total")); // No I18N
    recurringExpense.setAmount(recExpense.getDouble("amount")); // No I18N
    recurringExpense.setIsInclusiveTax(recExpense.getBoolean("is_inclusive_tax")); // No I18N
    recurringExpense.setIsBillable(recExpense.getBoolean("is_billable")); // No I18N
    recurringExpense.setDescription(recExpense.getString("description"));
    recurringExpense.setCustomerId(recExpense.getString("customer_id"));
    recurringExpense.setCustomerName(recExpense.getString("customer_name"));
    recurringExpense.setCreatedTime(recExpense.getString("created_time"));
    recurringExpense.setLastModifiedTime(recExpense.getString("last_modified_time"));
    recurringExpense.setStatus(recExpense.getString("status"));
    recurringExpense.setProjectId(recExpense.getString("project_id"));
    recurringExpense.setProjectName(recExpense.getString("project_name"));

    return recurringExpense;
  }

  /**
   * Parse the json response and returns the RecurringExpenseList object.
   *
   * @param response
   *          This json response contains list of recurring expenses details.
   * @return Returns the RecurringExpenseList object.
   * @throws Exception
   *           the exception
   */

  public RecurringExpenseList getRecurringExpenses(String response) throws Exception {

    RecurringExpenseList recurringExpenseList = new RecurringExpenseList();

    JSONObject jsonObject = new JSONObject(response.trim());

    JSONArray jsonArray = jsonObject.getJSONArray("recurring_expenses"); // No I18N

    for (int i = 0; i < jsonArray.length(); i++) {
      RecurringExpense recurringExpense = new RecurringExpense();

      recurringExpense.setRecurringExpenseId(jsonArray.getJSONObject(i).getString(
          "recurring_expense_id"));
      recurringExpense.setRecurrenceName(jsonArray.getJSONObject(i).getString("recurrence_name"));
      recurringExpense.setRecurrenceFrequency(jsonArray.getJSONObject(i).getString(
          "recurrence_frequency"));
      // No I18N
      recurringExpense.setRepeatEvery(jsonArray.getJSONObject(i).getInt("repeat_every"));
      recurringExpense
          .setLastCreatedDate(jsonArray.getJSONObject(i).getString("last_created_date"));
      recurringExpense
          .setNextExpenseDate(jsonArray.getJSONObject(i).getString("next_expense_date"));
      recurringExpense.setAccountName(jsonArray.getJSONObject(i).getString("account_name"));
      recurringExpense.setPaidThroughAccountName(jsonArray.getJSONObject(i).getString(
          "paid_through_account_name"));
      recurringExpense.setDescription(jsonArray.getJSONObject(i).getString("description"));
      recurringExpense.setCurrencyId(jsonArray.getJSONObject(i).getString("currency_id"));
      recurringExpense.setCurrencyCode(jsonArray.getJSONObject(i).getString("currency_code"));
      recurringExpense.setTotal(jsonArray.getJSONObject(i).getDouble("total")); // No I18N
      recurringExpense.setIsBillable(jsonArray.getJSONObject(i).getBoolean("is_billable"));
      recurringExpense.setCustomerName(jsonArray.getJSONObject(i).getString("customer_name"));
      recurringExpense.setVendorName(jsonArray.getJSONObject(i).getString("vendor_name"));
      recurringExpense.setStatus(jsonArray.getJSONObject(i).getString("status"));
      recurringExpense.setCreatedTime(jsonArray.getJSONObject(i).getString("created_time"));

      recurringExpenseList.add(recurringExpense);
    }

    PageContext pageContext = new PageContext();

    JSONObject pagecontext = jsonObject.getJSONObject("page_context"); // No I18N

    pageContext.setPage(pagecontext.getInt("page")); // No I18N
    pageContext.setPerPage(pagecontext.getInt("per_page")); // No I18N
    pageContext.setHasMorePage(pagecontext.getBoolean("has_more_page")); // No I18N
    pageContext.setReportName(pagecontext.getString("report_name"));
    pageContext.setAppliedFilter(pagecontext.getString("applied_filter"));
    pageContext.setSortColumn(pagecontext.getString("sort_column"));
    pageContext.setSortOrder(pagecontext.getString("sort_order"));

    recurringExpenseList.setPageContext(pageContext);

    return recurringExpenseList;
  }

  /**
   * Parse the json response and returns the ExpenseList object.
   *
   * @param response
   *          This json response contains list of expenses details for recurring expense.
   * @return Returns the ExpenseList object.
   * @throws Exception
   *           the exception
   */

  public ExpenseList getExpensehistory(String response) throws Exception {

    ExpenseList expenseList = new ExpenseList();

    JSONObject jsonObject = new JSONObject(response.trim());

    JSONArray jsonArray = jsonObject.getJSONArray("expensehistory"); // No I18N

    for (int i = 0; i < jsonArray.length(); i++) {
      Expense expense = new Expense();

      expense.setExpenseId(jsonArray.getJSONObject(i).getString("expense_id"));
      expense.setDate(jsonArray.getJSONObject(i).getString("date"));
      expense.setAccountName(jsonArray.getJSONObject(i).getString("account_name"));
      expense.setVendorName(jsonArray.getJSONObject(i).getString("vendor_name"));
      expense.setPaidThroughAccountName(jsonArray.getJSONObject(i).getString(
          "paid_through_account_name"));
      expense.setCustomerName(jsonArray.getJSONObject(i).getString("customer_name"));
      expense.setTotal(jsonArray.getJSONObject(i).getDouble("total")); // No I18N
      expense.setStatus(jsonArray.getJSONObject(i).getString("status"));

      expenseList.add(expense);
    }

    PageContext pageContext = new PageContext();

    JSONObject pagecontext = jsonObject.getJSONObject("page_context"); // No I18N

    pageContext.setPage(pagecontext.getInt("page")); // No I18N
    pageContext.setPerPage(pagecontext.getInt("per_page")); // No I18N
    pageContext.setHasMorePage(pagecontext.getBoolean("has_more_page")); // No I18N
    pageContext.setReportName(pagecontext.getString("report_name"));
    if (pagecontext.has("applied_filter")) {
      pageContext.setAppliedFilter(pagecontext.getString("applied_filter"));
    }
    pageContext.setSortColumn(pagecontext.getString("sort_column"));
    pageContext.setSortOrder(pagecontext.getString("sort_order"));

    expenseList.setPageContext(pageContext);

    return expenseList;
  }

  /**
   * Parse the json response and returns the CommentList object.
   *
   * @param response
   *          This json response contains list of comments details for recurring expense.
   * @return Returns the CommentList object.
   * @throws Exception
   *           the exception
   */

  public CommentList getComments(String response) throws Exception {

    CommentList commentList = new CommentList();

    JSONObject jsonObject = new JSONObject(response.trim());

    JSONArray jsonArray = jsonObject.getJSONArray("comments"); // No I18N

    for (int i = 0; i < jsonArray.length(); i++) {
      Comment comment = new Comment();

      comment.setCommentId(jsonArray.getJSONObject(i).getString("comment_id"));
      comment.setRecurringExpenseId(jsonArray.getJSONObject(i).getString("recurring_expense_id"));
      comment.setDescription(jsonArray.getJSONObject(i).getString("description"));
      comment.setCommentedById(jsonArray.getJSONObject(i).getString("commented_by_id"));
      comment.setCommentedBy(jsonArray.getJSONObject(i).getString("commented_by"));
      comment.setDate(jsonArray.getJSONObject(i).getString("date"));
      comment.setDateDescription(jsonArray.getJSONObject(i).getString("date_description"));
      comment.setTime(jsonArray.getJSONObject(i).getString("time"));
      comment.setOperationType(jsonArray.getJSONObject(i).getString("operation_type"));
      comment.setTransactionId(jsonArray.getJSONObject(i).getString("transaction_id"));
      comment.setTransactionType(jsonArray.getJSONObject(i).getString("transaction_type"));

      commentList.add(comment);
    }

    return commentList;
  }

  /**
   * Parse the json response and returns a string object.
   *
   * @param response
   *          This json response contains the success message of post or update or delete requests.
   * @return Returns the success message.
   * @throws Exception
   *           the exception
   */

  public String getMessage(String response) throws Exception {
    JSONObject jsonObject = new JSONObject(response.trim());

    String success = jsonObject.getString("message");

    return success;
  }
}
