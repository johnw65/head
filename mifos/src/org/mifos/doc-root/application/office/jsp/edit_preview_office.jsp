<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<tiles:insert definition=".view">
	<tiles:put name="body" type="string">
		<!-- Next is code for setting the hidden veriable to cancel -->
		<script language="javascript" type="text/javascript">
function goToCancelPage(id){
	document.offActionForm.method.value="get";
	document.offActionForm.officeId.value=id;
	offActionForm.submit();
  }
  function goToPreviousPage()
  {
	document.offActionForm.method.value="editprevious";
	offActionForm.submit();
  
  }
     function submitViewOfficesLink(){
	document.offActionForm.method.value="getAllOffices";
	document.offActionForm.action="offAction.do";
	offActionForm.submit();
  }
function getOffice(officeid){
	document.offActionForm.method.value="get";
	document.offActionForm.officeId.value=officeid;	
	offActionForm.submit();
  }  
function  submitAdminLink()
{
		document.offActionForm.method.value="load";
		document.offActionForm.action="AdminAction.do";
		offActionForm.submit();
}
</script>
		<html-el:form action="/offAction.do" method="POST">

			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="bluetablehead05"><span class="fontnormal8pt"><html-el:link
						href="javascript:submitAdminLink()">
						<mifos:mifoslabel name="office.labelLinkAdmin"
							bundle="OfficeResources"></mifos:mifoslabel>
					</html-el:link> / <html-el:link
						href="javascript:submitViewOfficesLink()">
						<mifos:mifoslabel name="office.labelLinkViewOffices"
							bundle="OfficeResources"></mifos:mifoslabel>

					</html-el:link> / <html-el:link
						href="javascript:getOffice(${BusinessKey.officeId})">
						<c:out value="${offActionForm.officeName}"></c:out>
					</html-el:link> </span></td>
				</tr>
			</table>
			<table table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td width="70%" align="left" valign="top" class="paddingL15T15">

					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td class="headingorange"><span class="heading"> <c:out
								value="${offActionForm.officeName}"></c:out> </span> - <mifos:mifoslabel
								name="Office.labelPreviewOfficeInformation"
								/></td>
						</tr>
					</table>
					<table width="93%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<font class="fontnormalRedBold"><html-el:errors
								bundle="OfficeResources" /> </font>
						</tr>
						<tr>
							<!-- actual information starts from here -->
							<td height="23" class="fontnormalbold"><mifos:mifoslabel
								name="Office.labelOfficeName"/> <span class="fontnormal"> <c:out
								value="${offActionForm.officeName}"></c:out> <br>
							</span> <mifos:mifoslabel name="Office.labelOfficeShortName"
								/><span
								class="fontnormal"> <c:out value="${offActionForm.shortName}"></c:out><br>
							</span> <mifos:mifoslabel name="Office.labelOfficeType"
								/> <span
								class="fontnormal"> <!--  code for setting the correct value for type-->

							<c:forEach var="level" items="${OfficeLevelList}">
								<c:if test="${level.levelId == offActionForm.officeLevel }">
									<c:out value="${level.levelName}"></c:out>
								</c:if>
							</c:forEach> <!--  END code for setting the correct value for type-->
							</span> <br>
							<span class="fontnormal"> </span><mifos:mifoslabel
								name="Office.labelParentOffice" /><span
								class="fontnormal"> <!-- logic for showing the correct parent -->
							<c:forEach var="parent" items="${Parents}">
								<c:if test="${parent.officeId == offActionForm.parentOfficeId }">
									<c:out value="${parent.displayName}"></c:out>
								</c:if>
							</c:forEach> </span><br>
							<!-- End for showing the correct parent --> <!-- office status  -->
							<span class="fontnormalbold"> </span><mifos:mifoslabel
								name="Office.labelStatus" />:<span
								class="fontnormal"> <c:forEach var="status"
								items="${OfficeStatusList}">
								<c:if test="${status.levelId  ==offActionForm.officeStatus}">
									<c:out value="${status.levelName}"></c:out>
								</c:if>
							</c:forEach> <!-- end office status  --> </span><br>
							<span class="fontnormal"></span><span class="fontnormal"> </span><span
								class="fontnormal"></span><br>
							<mifos:mifoslabel name="Office.labelAddress"
								/><span
								class="fontnormal"><br>
							</span><span class="fontnormal"><c:out
								value="${offActionForm.address.line1}"></c:out><c:if
								test="${not empty offActionForm.address.line1 &&(not empty offActionForm.address.line2||not empty offActionForm.address.line3)}">, </c:if><c:if
								test="${not empty offActionForm.address.line2}">${offActionForm.address.line2}</c:if><c:if
								test="${not empty offActionForm.address.line3&&not empty offActionForm.address.line2}">, </c:if><c:if
								test="${not empty offActionForm.address.line3}">${offActionForm.address.line3}</c:if><br>
							</span>
						</tr>
						<tr>
							<td class="fontnormalbold"><mifos:mifoslabel
								name="${ConfigurationConstants.CITY}" bundle="OfficeResources"></mifos:mifoslabel>:<span
								class="fontnormal"> <c:out value="${offActionForm.address.city}"></c:out><br>
							</span>
						</tr>
						<tr id="Office.State">
							<td class="fontnormalbold"><mifos:mifoslabel
								name="${ConfigurationConstants.STATE}" bundle="OfficeUIResources"
								keyhm="Office.State" isColonRequired="yes"
								isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel><span
								class="fontnormal"> <c:out
								value="${offActionForm.address.state}"></c:out><br>
							</span>
						</tr>
						<tr id="Office.Country">
							<td class="fontnormalbold"><mifos:mifoslabel
								name="Office.labelCountry" 
								keyhm="Office.Country" isManadatoryIndicationNotRequired="yes"/><span
								class="fontnormal"> <c:out
								value="${offActionForm.address.country}"></c:out><br>
							</span>
						</tr>
						<tr id="Office.PostalCode">
							<td class="fontnormalbold"><mifos:mifoslabel
								name="${ConfigurationConstants.POSTAL_CODE}"
								bundle="OfficeResources" keyhm="Office.PostalCode"
								isColonRequired="yes" isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel><span
								class="fontnormal"> <c:out value="${offActionForm.address.zip}"></c:out><br>
							<br>
							</span>
						</tr>
						<tr>
							<td class="fontnormalbold"><mifos:mifoslabel
								name="Office.labelTelephone" /><span
								class="fontnormal"> <c:out
								value="${offActionForm.address.phoneNumber}"></c:out></span><br>
									<c:if test="${!empty offActionForm.customFields}">
										<mifos:mifoslabel name="Office.labelAdditionInformation" />
									</c:if> <span class="fontnormal"><br>
									 <c:forEach var="cfdef"
										items="${offActionForm.customFields}">
										<c:forEach var="cf" items="${sessionScope.customFields}">
											<c:if test="${cfdef.fieldId==cf.fieldId}">
											
												<font class="fontnormalBold"> 
												
												<mifos:mifoslabel
											name="${cf.lookUpEntity.entityType}"
											bundle="OfficeResources"></mifos:mifoslabel>
												:
												</font>
												<span class="fontnormal"><c:out value="${cfdef.fieldValue}" /><br>
												</span>
											</c:if>
										</c:forEach>
									</c:forEach> <span class="fontnormal"></span> <br>
							<br>
							<span class="fontnormal"></span> <html-el:button
								onclick="goToPreviousPage();" property="cancelButton"
								styleClass="insidebuttn" style="width:150px">
								<mifos:mifoslabel name="Office.edit"
									/>
							</html-el:button></span></td>
						</tr>
					</table>


					<table width="95%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center" class="blueline">&nbsp;</td>
						</tr>
					</table>
					<br>

					<table width="93%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center">&nbsp; <!-- Next are submit and cancel button -->

							<html-el:submit styleClass="buttn" style="width:70px;"></html-el:submit>
							&nbsp; <html-el:button onclick="goToCancelPage(${BusinessKey.officeId});"
								property="cancelButton" value="Cancel" styleClass="cancelbuttn"
								style="width:70px">
								<mifos:mifoslabel name="Office.cancel"
									/>
							</html-el:button></td>
						</tr>
					</table>

					<br>

					<br>
					</td>
				</tr>
			</table>
			<!-- hidden veriable which will be set to method -->
			<html-el:hidden property="method" value="update" />
			<html-el:hidden property="officeId" value="${BusinessKey.officeId}" />
		</html-el:form>
	</tiles:put>
</tiles:insert>
