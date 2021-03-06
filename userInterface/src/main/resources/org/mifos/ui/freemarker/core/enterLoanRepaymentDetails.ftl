[#ftl]
[#--
* Copyright (c) 2005-2011 Grameen Foundation USA
*  All rights reserved.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
*  implied. See the License for the specific language governing
*  permissions and limitations under the License.
*
*  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
*  explanation of the license and how it is applied.
--]

[@layout.webflow currentTab="ClientsAndAccounts"
                 currentState="createLoanAccount.flowState.reviewAndSubmit" 
                 states=["createLoanAccount.flowState.reviewAndSubmit"]
]
                         
<span id="page.id" title="ApplyLoanRepayment"></span>

[@form.errors "loanRepaymentFormBean.*"/]
<form action="${flowExecutionUrl}" method="post" class="two-columns">
    <fieldset>
    <div class="row">
        <label for="paymentDate">Date of Transaction:</label>
        [@form.input path="loanRepaymentFormBean.paymentDate" id="paymentDate" attributes="" /]
    </div>
    <div class="row">
        <label for="paymentAmount">Amount:</label>
        [@form.input path="loanRepaymentFormBean.paymentAmount" id="paymentAmount" attributes="" /]
    </div>
    </fieldset>
    <div class="row">
        [@form.submitButton label="widget.form.buttonLabel.submit" id="cust_search_account.button.search" webflowEvent="detailsEntered" /]
        [@form.cancelButton label="widget.form.buttonLabel.cancel" webflowEvent="cancel" /]
    </div>
</form>
<br/>

[/@layout.webflow]