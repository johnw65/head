 [#ftl]
[#--
* Copyright (c) 2005-2010 Grameen Foundation USA
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
[#include "layout.ftl"]
<div align='center'>
<form>
From Date: <input type="text" id="fromDate" name="fromDate" readonly="readonly" />
To Date: <input type="text" id="toDate" name="toDate" readonly="readonly" />
          <div class="clear">&nbsp;</div>
          <div class="buttonsSubmitCancel margin20right">
             <input type="button" class="buttn" value="[@spring.message "submit"/]" onclick="javascript:submitAccountingDataForm()" />
             <input type="button" class="buttn2" value="[@spring.message "cancel"/]" onclick="javascript:getAccountingDataForm()" />
           </div>
           <div class="clear">&nbsp;</div>
</form>
</div>