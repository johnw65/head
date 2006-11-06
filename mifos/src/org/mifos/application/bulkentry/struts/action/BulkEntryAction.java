/**

 * BulkEntryAction.java    version: 1.0

 

 * Copyright (c) 2005-2006 Grameen Foundation USA

 * 1029 Vermont Avenue, NW, Suite 400, Washington DC 20005

 * All rights reserved.

 

 * Apache License 
 * Copyright (c) 2005-2006 Grameen Foundation USA 
 * 

 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 *

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the 

 * License. 
 * 
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an explanation of the license 

 * and how it is applied.  

 *

 */

package org.mifos.application.bulkentry.struts.action;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.mifos.application.accounts.loan.util.helpers.LoanAccountsProductView;
import org.mifos.application.accounts.savings.business.SavingsBO;
import org.mifos.application.bulkentry.business.BulkEntryBO;
import org.mifos.application.bulkentry.business.BulkEntryView;
import org.mifos.application.bulkentry.business.service.BulkEntryBusinessService;
import org.mifos.application.bulkentry.struts.actionforms.BulkEntryActionForm;
import org.mifos.application.bulkentry.util.helpers.BulkEntryConstants;
import org.mifos.application.bulkentry.util.helpers.BulkEntrySavingsCache;
import org.mifos.application.customer.business.CustomerView;
import org.mifos.application.customer.client.business.ClientBO;
import org.mifos.application.customer.util.helpers.CustomerAccountView;
import org.mifos.application.customer.util.helpers.CustomerConstants;
import org.mifos.application.master.business.PaymentTypeEntity;
import org.mifos.application.master.business.PaymentTypeView;
import org.mifos.application.master.business.service.MasterDataService;
import org.mifos.application.master.util.helpers.MasterConstants;
import org.mifos.application.office.business.OfficeView;
import org.mifos.application.office.persistence.OfficePersistence;
import org.mifos.application.office.util.resources.OfficeConstants;
import org.mifos.application.personnel.business.PersonnelView;
import org.mifos.application.personnel.util.helpers.PersonnelConstants;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.business.service.ServiceFactory;
import org.mifos.framework.components.configuration.business.Configuration;
import org.mifos.framework.components.logger.LoggerConstants;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.components.logger.MifosLogger;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.struts.action.BaseAction;
import org.mifos.framework.struts.tags.DateHelper;
import org.mifos.framework.util.helpers.BusinessServiceName;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TransactionDemarcate;

public class BulkEntryAction extends BaseAction {

	private MasterDataService masterService;

	private static MifosLogger logger = MifosLogManager
			.getLogger(LoggerConstants.BULKENTRYLOGGER);

	public BulkEntryAction() {
		masterService = (MasterDataService) ServiceFactory.getInstance()
				.getBusinessService(BusinessServiceName.MasterDataService);
	}

	@Override
	protected BusinessService getService() {
		return new BulkEntryBusinessService();
	}

	@Override
	protected boolean startSession() {
		return false;
	}

	@Override
	protected boolean skipActionFormToBusinessObjectConversion(String method) {
		return method.equals(BulkEntryConstants.CREATEMETHOD);
	}

	/**
	 * This method is called before the load page for center is called It sets
	 * this information in session and context.This should be removed after
	 * center was successfully created.
	 */
	@TransactionDemarcate(saveToken = true)
	public ActionForward load(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			request.getSession().setAttribute(
					BulkEntryConstants.BULKENTRYACTIONFORM, null);
			request.getSession().setAttribute(Constants.BUSINESS_KEY, null);
			UserContext userContext = getUserContext(request);
			List<OfficeView> activeBranches = masterService
					.getActiveBranches(userContext.getBranchId());
			SessionUtils.setAttribute(OfficeConstants.OFFICESBRANCHOFFICESLIST,
					activeBranches, request);
			boolean isCenterHeirarchyExists = Configuration.getInstance()
					.getCustomerConfig(
							new OfficePersistence().getHeadOffice()
									.getOfficeId()).isCenterHierarchyExists();
			SessionUtils.setAttribute(
					BulkEntryConstants.ISCENTERHEIRARCHYEXISTS,
					isCenterHeirarchyExists ? Constants.YES : Constants.NO,
					request);
			if (activeBranches.size() == 1) {
				List<PersonnelView> loanOfficers = loadLoanOfficersForBranch(
						userContext, activeBranches.get(0).getOfficeId());
				SessionUtils.setAttribute(CustomerConstants.LOAN_OFFICER_LIST,
						loanOfficers, request);
				if (loanOfficers.size() == 1) {
					List<CustomerView> parentCustomerList = loadCustomers(
							loanOfficers.get(0).getPersonnelId(),
							activeBranches.get(0).getOfficeId());
					SessionUtils.setAttribute(BulkEntryConstants.CUSTOMERSLIST,
							parentCustomerList, request);
					request.setAttribute(BulkEntryConstants.REFRESH,
							Constants.NO);
				} else {
					SessionUtils.setAttribute(BulkEntryConstants.CUSTOMERSLIST,
							new ArrayList<CustomerView>(), request);
					request.setAttribute(BulkEntryConstants.REFRESH,
							Constants.YES);
				}
			} else {
				SessionUtils.setAttribute(CustomerConstants.LOAN_OFFICER_LIST,
						new ArrayList<PersonnelView>(), request);
				SessionUtils.setAttribute(BulkEntryConstants.CUSTOMERSLIST,
						new ArrayList<CustomerView>(), request);
				request.setAttribute(BulkEntryConstants.REFRESH, Constants.YES);
			}
			SessionUtils
					.setAttribute(BulkEntryConstants.PAYMENT_TYPES_LIST,
							masterService.retrieveMasterEntities(
									PaymentTypeEntity.class, userContext
											.getLocaleId()), request);
			SessionUtils.setAttribute(
					BulkEntryConstants.ISBACKDATEDTRXNALLOWED, Constants.NO,
					request);

		} catch (Exception e) {
		}
		return mapping.findForward(BulkEntryConstants.LOADSUCCESS);
	}

	/**
	 * This method retrieves the last meeting date for the chosen customer. This
	 * meeting date is put as the default date for the tranasaction date in the
	 * search criteria
	 * 
	 */
	@TransactionDemarcate(joinToken = true)
	public ActionForward getLastMeetingDateForCustomer(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		BulkEntryActionForm actionForm = (BulkEntryActionForm) form;

		boolean isBackDatedTrxnAllowed = false;
		if (actionForm.getOfficeId() != null)
			isBackDatedTrxnAllowed = Configuration.getInstance()
					.getAccountConfig(Short.valueOf(actionForm.getOfficeId()))
					.isBackDatedTxnAllowed();
		Date meetingDate = new BulkEntryBusinessService()
				.getLastMeetingDateForCustomer(Integer.valueOf(actionForm
						.getCustomerId()));
		if (meetingDate != null && isBackDatedTrxnAllowed) {
			actionForm.setTransactionDate(DateHelper.getUserLocaleDate(
					getUserLocale(request), meetingDate.toString()));
		} else {
			actionForm.setTransactionDate(DateHelper
					.getCurrentDate(getUserLocale(request)));
		}
		SessionUtils.setAttribute("LastMeetingDate", meetingDate, request);
		SessionUtils.setAttribute(BulkEntryConstants.ISBACKDATEDTRXNALLOWED,
				isBackDatedTrxnAllowed ? Constants.YES : Constants.NO, request);

		return mapping.findForward(BulkEntryConstants.LOADSUCCESS);
	}

	@TransactionDemarcate(joinToken = true)
	public ActionForward loadLoanOfficers(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		UserContext userContext = getUserContext(request);
		BulkEntryActionForm bulkEntryActionForm = (BulkEntryActionForm) form;
		Short officeId = Short.valueOf(bulkEntryActionForm.getOfficeId());
		List<PersonnelView> loanOfficers = loadLoanOfficersForBranch(
				userContext, officeId);
		SessionUtils.setAttribute(CustomerConstants.LOAN_OFFICER_LIST,
				loanOfficers, request);
		return mapping.findForward(BulkEntryConstants.LOADSUCCESS);
	}

	@TransactionDemarcate(joinToken = true)
	public ActionForward loadCustomerList(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		BulkEntryActionForm bulkEntryActionForm = (BulkEntryActionForm) form;
		Short personnelId = Short.valueOf(bulkEntryActionForm
				.getLoanOfficerId());
		Short officeId = Short.valueOf(bulkEntryActionForm.getOfficeId());
		List<CustomerView> parentCustomerList = loadCustomers(personnelId,
				officeId);
		SessionUtils.setAttribute(BulkEntryConstants.CUSTOMERSLIST,
				parentCustomerList, request);
		boolean isCenterHeirarchyExists = Configuration.getInstance()
				.getCustomerConfig(officeId).isCenterHierarchyExists();
		SessionUtils
				.setAttribute(BulkEntryConstants.ISCENTERHEIRARCHYEXISTS,
						isCenterHeirarchyExists ? Constants.YES : Constants.NO,
						request);
		return mapping.findForward(BulkEntryConstants.LOADSUCCESS);
	}

	/**
	 * This method is called once the search criterias have been entered by the
	 * user to generate the bulk entry details for a particular customer It
	 * retrieves the loan officer office, and parent customer that was selected
	 * and sets them into the bulk entry business object. The list of attendance
	 * types and product list associated with the center, and its children are
	 * also retrieved
	 */
	@TransactionDemarcate(joinToken = true)
	public ActionForward get(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		UserContext userContext = getUserContext(request);
		Date meetingDate = (Date) SessionUtils.getAttribute("LastMeetingDate",
				request);
		BulkEntryBO bulkEntry = (BulkEntryBO) request.getSession()
				.getAttribute(Constants.BUSINESS_KEY);
		PersonnelView loanOfficer = getSelectedLO(request, form);
		bulkEntry.setLoanOfficer(loanOfficer);
		bulkEntry.setOffice(getSelectedBranchOffice(request, form));
		bulkEntry.setPaymentType(getSelectedPaymentType(request, form));
		CustomerView parentCustomer = getSelectedCustomer(request, form);
		bulkEntry.buildBulkEntryView(parentCustomer);
		bulkEntry.setLoanProducts(masterService.getLoanProductsAsOfMeetingDate(
				meetingDate, parentCustomer.getCustomerSearchId(), loanOfficer
						.getPersonnelId()));
		bulkEntry.setSavingsProducts(masterService
				.getSavingsProductsAsOfMeetingDate(meetingDate, parentCustomer
						.getCustomerSearchId(), loanOfficer.getPersonnelId()));
		SessionUtils.setAttribute(BulkEntryConstants.BULKENTRY, bulkEntry,
				request);
		SessionUtils
				.setAttribute(
						BulkEntryConstants.CUSTOMERATTENDANCETYPES,
						masterService
								.getMasterData(
										MasterConstants.ATTENDENCETYPES,
										userContext.getLocaleId(),
										"org.mifos.application.master.business.CustomerAttendance",
										"attendanceId").getLookUpMaster(),
						request);

		return mapping.findForward(BulkEntryConstants.GETSUCCESS);
	}

	@TransactionDemarcate(joinToken = true)
	public ActionForward preview(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		BulkEntryBO bulkEntry = (BulkEntryBO) SessionUtils.getAttribute(
				BulkEntryConstants.BULKENTRY, request);
		List<ClientBO> clients = new ArrayList<ClientBO>();
		List<SavingsBO> savingsAccounts = new ArrayList<SavingsBO>();
		List<String> customerNames = new ArrayList<String>();
		List<String> savingsDepNames = new ArrayList<String>();
		List<String> savingsWithNames = new ArrayList<String>();
		Map<Integer, BulkEntrySavingsCache> savingsCache = new HashMap<Integer, BulkEntrySavingsCache>();
		List<LoanAccountsProductView> loanAccprdViews = new ArrayList<LoanAccountsProductView>();
		List<CustomerAccountView> customerAccViews = new ArrayList<CustomerAccountView>();
		Date meetingDate = Date.valueOf(DateHelper
				.convertUserToDbFmt(((BulkEntryActionForm) form)
						.getTransactionDate(), "dd/MM/yyyy"));
		List<BulkEntryView> customerViews = new ArrayList<BulkEntryView>();
		setData(bulkEntry.getBulkEntryParent(), loanAccprdViews,
				customerAccViews, customerViews);
		new BulkEntryBusinessService().setData(customerViews, savingsCache,
				clients, savingsDepNames, savingsWithNames, customerNames,
				getUserContext(request).getId(), bulkEntry.getReceiptId(),
				bulkEntry.getPaymentType().getPaymentTypeId(), bulkEntry
						.getReceiptDate(), bulkEntry.getTransactionDate(),
				meetingDate);
		for (Integer accountId : savingsCache.keySet()) {
			BulkEntrySavingsCache bulkEntrySavingsCache = savingsCache
					.get(accountId);
			if (bulkEntrySavingsCache.getYesNoFlag().equals(YesNoFlag.YES))
				savingsAccounts.add(bulkEntrySavingsCache.getAccount());
		}

		SessionUtils.setAttribute(BulkEntryConstants.CLIENTS, clients, request);
		SessionUtils.setAttribute(BulkEntryConstants.SAVINGS, savingsAccounts,
				request);
		SessionUtils.setAttribute(BulkEntryConstants.LOANS, loanAccprdViews,
				request);
		SessionUtils.setAttribute(BulkEntryConstants.CUSTOMERACCOUNTS,
				customerAccViews, request);
		SessionUtils.setAttribute(BulkEntryConstants.ERRORCLIENTS,
				customerNames, request);
		SessionUtils.setAttribute(BulkEntryConstants.ERRORSAVINGSDEPOSIT,
				savingsDepNames, request);
		SessionUtils.setAttribute(BulkEntryConstants.ERRORSAVINGSWITHDRAW,
				savingsWithNames, request);
		return mapping.findForward(BulkEntryConstants.PREVIEWSUCCESS);
	}

	private void setData(BulkEntryView parent,
			List<LoanAccountsProductView> loanAccprdViews,
			List<CustomerAccountView> customerAccViews,
			List<BulkEntryView> customerViews) {
		List<BulkEntryView> children = parent.getBulkEntryChildren();
		Short levelId = parent.getCustomerDetail().getCustomerLevelId();
		if (null != children) {
			for (BulkEntryView child : children) {
				setData(child, loanAccprdViews, customerAccViews, customerViews);
			}
		}
		customerViews.add(parent);
		if (!levelId.equals(CustomerConstants.CENTER_LEVEL_ID)) {
			loanAccprdViews.addAll(parent.getLoanAccountDetails());
		}
		customerAccViews.add(parent.getCustomerAccountDetails());
	}

	@TransactionDemarcate(joinToken = true)
	public ActionForward previous(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(BulkEntryConstants.PREVIUOSSUCCESS);
	}

	@TransactionDemarcate(validateAndResetToken = true)
	public ActionForward cancel(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		return mapping.findForward(ActionForwards.cancel_success.toString());
	}

	@TransactionDemarcate(joinToken = true)
	public ActionForward validate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String forward = null;
		String methodCalled = request.getParameter(BulkEntryConstants.METHOD);
		String input = request.getParameter("input");
		if (null != methodCalled) {
			if ("load".equals(input))
				forward = BulkEntryConstants.LOADSUCCESS;
			else if ("get".equals(input))
				forward = BulkEntryConstants.GETSUCCESS;
			else if ("preview".equals(input))
				forward = BulkEntryConstants.PREVIEWSUCCESS;
		}
		if (null != forward)
			return mapping.findForward(forward);
		return null;
	}

	@TransactionDemarcate(validateAndResetToken = true)
	public ActionForward create(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.debug("create ");
		// To enable cache
		BulkEntryBusinessService bulkEntryService = new BulkEntryBusinessService();
		List<String> loanAccountNums = new ArrayList<String>();
		List<String> customerAccountNums = new ArrayList<String>();
		List<String> savingsDepositAccountNums = (List<String>) SessionUtils
				.getAttribute(BulkEntryConstants.ERRORSAVINGSDEPOSIT, request);
		List<String> savingsWithdrawalsAccountNums = (List<String>) SessionUtils
				.getAttribute(BulkEntryConstants.ERRORSAVINGSWITHDRAW, request);
		List<String> customerNames = (List<String>) SessionUtils.getAttribute(
				BulkEntryConstants.ERRORCLIENTS, request);
		StringBuilder builder = new StringBuilder();
		ActionErrors actionErrors = new ActionErrors();

		BulkEntryBO bulkEntry = (BulkEntryBO) SessionUtils.getAttribute(
				BulkEntryConstants.BULKENTRY, request);
		logger.debug("transactionDate "
				+ ((BulkEntryActionForm) form).getTransactionDate());
		Short personnelId = getUserContext(request).getId();
		List<ClientBO> clients = (List<ClientBO>) SessionUtils.getAttribute(
				BulkEntryConstants.CLIENTS, request);
		List<SavingsBO> savings = (List<SavingsBO>) SessionUtils.getAttribute(
				BulkEntryConstants.SAVINGS, request);
		List<LoanAccountsProductView> loans = (List<LoanAccountsProductView>) SessionUtils
				.getAttribute(BulkEntryConstants.LOANS, request);
		List<CustomerAccountView> customerAccounts = (List<CustomerAccountView>) SessionUtils
				.getAttribute(BulkEntryConstants.CUSTOMERACCOUNTS, request);
		bulkEntryService.saveData(loans, personnelId, bulkEntry.getReceiptId(),
				bulkEntry.getPaymentType().getPaymentTypeId(), bulkEntry
						.getReceiptDate(), bulkEntry.getTransactionDate(),
				loanAccountNums, savings, savingsDepositAccountNums, clients,
				customerNames, customerAccounts, customerAccountNums);
		request.setAttribute(BulkEntryConstants.CENTER, bulkEntry
				.getBulkEntryParent().getCustomerDetail().getDisplayName());
		if (loanAccountNums.size() > 0 || savingsDepositAccountNums.size() > 0
				|| savingsWithdrawalsAccountNums.size() > 0
				|| customerAccountNums.size() > 0 || customerNames.size() > 0) {
			getErrorString(builder, loanAccountNums, "Loan");
			getErrorString(builder, savingsDepositAccountNums,
					"Savings Deposit");
			getErrorString(builder, savingsWithdrawalsAccountNums,
					"Savings Withdrawal");
			getErrorString(builder, customerAccountNums, "A/C collections");
			getErrorString(builder, customerNames, "Attendance");
			builder.append("<br><br>");
			actionErrors.add(BulkEntryConstants.ERRORSUPDATE,
					new ActionMessage(BulkEntryConstants.ERRORSUPDATE, builder
							.toString()));
			request.setAttribute(Globals.ERROR_KEY, actionErrors);
		}
		// TO clear bulk entry cache in persistence service
		bulkEntryService = null;
		return mapping.findForward(BulkEntryConstants.CREATESUCCESS);
	}

	private void getErrorString(StringBuilder builder,
			List<String> accountNums, String message) {
		if (accountNums.size() != 0) {
			ListIterator<String> iter = accountNums.listIterator();
			builder.append("<br>");
			builder.append(message + "-	");
			while (iter.hasNext()) {
				builder.append(iter.next());
				if (iter.hasNext())
					builder.append(", ");
			}
		}
	}

	private List<PersonnelView> loadLoanOfficersForBranch(
			UserContext userContext, Short branchId) throws Exception {
		return masterService.getListOfActiveLoanOfficers(
				PersonnelConstants.LOAN_OFFICER, branchId, userContext.getId(),
				userContext.getLevelId());
	}

	/**
	 * This method loads either the centers or groups under a particular loan
	 * officer as this list of parent customer If the center hierarchy exists,
	 * then the list of centers under the loan officer is retrieved as the list
	 * of parent customers, else it is the list of groups.
	 * 
	 */
	private List<CustomerView> loadCustomers(Short personnelId, Short officeId)
			throws Exception {
		Short customerLevel;
		if (Configuration.getInstance().getCustomerConfig(officeId)
				.isCenterHierarchyExists())
			customerLevel = new Short(CustomerConstants.CENTER_LEVEL_ID);
		else
			customerLevel = new Short(CustomerConstants.GROUP_LEVEL_ID);
		List<CustomerView> activeParentUnderLoanOfficer = masterService
				.getListOfActiveParentsUnderLoanOfficer(personnelId,
						customerLevel, officeId);
		return activeParentUnderLoanOfficer;
	}

	/**
	 * This method retrieves the loan officer which was selected from the list
	 * of loan officers
	 * 
	 */
	private PersonnelView getSelectedLO(HttpServletRequest request,
			ActionForm form) throws Exception {
		BulkEntryActionForm bulkEntryForm = (BulkEntryActionForm) form;
		Short personnelId = Short.valueOf(bulkEntryForm.getLoanOfficerId());
		List<PersonnelView> loanOfficerList = (List<PersonnelView>) SessionUtils
				.getAttribute(CustomerConstants.LOAN_OFFICER_LIST, request);
		for (PersonnelView loanOfficer : loanOfficerList) {
			if (personnelId.shortValue() == loanOfficer.getPersonnelId()
					.shortValue()) {
				return loanOfficer;
			}
		}
		return null;
	}

	/**
	 * This method retrieves the branch office which was selected from the list
	 * of branch offices
	 */
	private OfficeView getSelectedBranchOffice(HttpServletRequest request,
			ActionForm form) throws Exception {
		BulkEntryActionForm bulkEntryForm = (BulkEntryActionForm) form;
		Short officeId = Short.valueOf(bulkEntryForm.getOfficeId());
		List<OfficeView> branchList = (List<OfficeView>) SessionUtils
				.getAttribute(OfficeConstants.OFFICESBRANCHOFFICESLIST, request);
		for (OfficeView branch : branchList) {
			if (officeId.shortValue() == branch.getOfficeId().shortValue()) {
				return branch;
			}
		}
		return null;
	}

	/**
	 * This method retrieves the parent customer which was selected from the
	 * list of customers which belong to a particualr branch and have a
	 * particular loan officer
	 * 
	 */
	private CustomerView getSelectedCustomer(HttpServletRequest request,
			ActionForm form) throws Exception {
		int i = 0;
		BulkEntryActionForm bulkEntryForm = (BulkEntryActionForm) form;
		Integer customerId = Integer.valueOf(bulkEntryForm.getCustomerId());
		List<CustomerView> parentCustomerList = (List<CustomerView>) SessionUtils
				.getAttribute(BulkEntryConstants.CUSTOMERSLIST, request);
		for (i = 0; i < parentCustomerList.size(); i++) {
			if (customerId.intValue() == parentCustomerList.get(i)
					.getCustomerId().intValue()) {
				break;
			}
		}
		return parentCustomerList.get(i);
	}

	/**
	 * This method retrieves the payment type which was selected from the list
	 * of payement types
	 * 
	 */
	private PaymentTypeView getSelectedPaymentType(HttpServletRequest request,
			ActionForm form) throws Exception {
		int i = 0;
		BulkEntryActionForm bulkEntryForm = (BulkEntryActionForm) form;
		Short paymentTypeId = Short.valueOf(bulkEntryForm.getPaymentId());
		List<PaymentTypeEntity> paymentTypeList = (List<PaymentTypeEntity>) SessionUtils
				.getAttribute(BulkEntryConstants.PAYMENT_TYPES_LIST, request);
		for (i = 0; i < paymentTypeList.size(); i++) {
			if (paymentTypeId.shortValue() == paymentTypeList.get(i).getId()
					.shortValue()) {
				break;
			}
		}
		PaymentTypeView paymentType = new PaymentTypeView();
		paymentType.setPaymentTypeId(paymentTypeList.get(i).getId()
				.shortValue());
		paymentType.setPaymentTypeValue(paymentTypeList.get(i).getName());
		return paymentType;
	}

	protected Locale getUserLocale(HttpServletRequest request) {
		Locale locale = null;
		UserContext userContext = getUserContext(request);
		if (null != userContext) {
			locale = userContext.getPereferedLocale();
			if (null == locale) {
				locale = userContext.getMfiLocale();
			}
		}
		return locale;
	}

}
