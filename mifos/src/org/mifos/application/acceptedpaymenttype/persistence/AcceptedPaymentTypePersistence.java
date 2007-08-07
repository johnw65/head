package org.mifos.application.acceptedpaymenttype.persistence;

import org.mifos.application.acceptedpaymenttype.business.AcceptedPaymentType;
import org.hibernate.Session;
import org.mifos.application.NamedQueryConstants;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.hibernate.helper.HibernateUtil;
import org.mifos.framework.persistence.Persistence;
import org.mifos.application.master.business.PaymentTypeEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;




	public class AcceptedPaymentTypePersistence extends Persistence {

		public AcceptedPaymentType getAcceptedPaymentType(Short paymentTypeId) {
			Session session = HibernateUtil.getSessionTL();
			return (AcceptedPaymentType) session.get(AcceptedPaymentType.class, paymentTypeId);
		}
		
		public List<AcceptedPaymentType> getAcceptedPaymentTypesForATransaction(Short transactionId) throws PersistenceException {
			HashMap<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("transactionId", transactionId);
			
			return executeNamedQuery(
				NamedQueryConstants.GET_ACCEPTED_PAYMENT_TYPES_FOR_A_TRANSACTION, queryParameters);
			
		}
		
		public void addAcceptedPaymentTypes(List<AcceptedPaymentType> acceptedPaymentTypeList) throws PersistenceException {
			for (AcceptedPaymentType paymentType : acceptedPaymentTypeList)
				createOrUpdate(paymentType);
		}


		// delete a list of accepted payment type for account actions
		public void deleteAcceptedPaymentTypes(List<AcceptedPaymentType> acceptedPaymentTypeList) throws PersistenceException {
			for (AcceptedPaymentType paymentType : acceptedPaymentTypeList)
				delete(paymentType);
		}
		
		
		public List<PaymentTypeEntity> getAcceptedPaymentTypesForATransaction(Short localeId, 
				Short transactionId) throws Exception {

			HashMap<String, Object> queryParameters = new HashMap<String, Object>();
			queryParameters.put("transactionId", transactionId);
			
			List<AcceptedPaymentType> acceptedPaymentTypeList =  executeNamedQuery(
				NamedQueryConstants.GET_ACCEPTED_PAYMENT_TYPES_FOR_A_TRANSACTION, queryParameters);
			List<PaymentTypeEntity> paymentTypeList = new ArrayList<PaymentTypeEntity>();
			for (AcceptedPaymentType acceptedPaymentType :  acceptedPaymentTypeList)
			{
				PaymentTypeEntity paymentTypeEntity = acceptedPaymentType.getPaymentTypeEntity();
				// the localeId is set so when the paymentTypeEntity.getName is called the localeId will be used
				// to determine what the name to match with that localeId
				paymentTypeEntity.setLocaleId(localeId);
				paymentTypeList.add(paymentTypeEntity);		
			}
			
			return paymentTypeList;
		}

		
	}

