package org.mifos.application.acceptedpaymenttype.persistence;

import java.util.List;
import java.util.ArrayList;
import org.mifos.application.accounts.TestAccount;
import org.mifos.framework.hibernate.helper.HibernateUtil;
import org.mifos.application.acceptedpaymenttype.business.AcceptedPaymentType;
import org.mifos.application.acceptedpaymenttype.business.TransactionTypeEntity;
import org.mifos.application.acceptedpaymenttype.persistence.AcceptedPaymentTypePersistence;
import org.mifos.application.master.business.PaymentTypeEntity;
import org.mifos.application.master.util.helpers.PaymentTypes;
import org.mifos.application.util.helpers.TrxnTypes;
import org.mifos.application.acceptedpaymenttype.persistence.helper.TransactionAcceptedPaymentTypes;


public class AcceptedPaymentTypePersistenceTest extends TestAccount {

	private List<TransactionAcceptedPaymentTypes> currentAcceptedPaymentTypes = null;
	private AcceptedPaymentTypePersistence acceptedPaymentTypePersistence = null;
	private List<TransactionAcceptedPaymentTypes> allAcceptedPaymentTypes = null;
	private Short DEFAULT_LOCALE_ID = 1;
	
	@Override
	protected void setUp() throws Exception {
		acceptedPaymentTypePersistence = new AcceptedPaymentTypePersistence();
	}

	@Override
	protected void tearDown() throws Exception {
		HibernateUtil.closeSession();
		super.tearDown();
	}
	
	public void testGetAcceptedPaymentTypes() throws Exception {
		// get all accepted payment types and store in currentAcceptedPaymentTypes
		currentAcceptedPaymentTypes = new ArrayList<TransactionAcceptedPaymentTypes>();
		allAcceptedPaymentTypes = new ArrayList<TransactionAcceptedPaymentTypes>();
		for (TrxnTypes transactionType : TrxnTypes.values()) 
		{
			List<AcceptedPaymentType> acceptedPaymentTypes = acceptedPaymentTypePersistence.getAcceptedPaymentTypesForATransaction(transactionType.getValue());
			TransactionAcceptedPaymentTypes transactionAcceptedPaymentTypes = new TransactionAcceptedPaymentTypes();
			transactionAcceptedPaymentTypes.setAcceptedPaymentTypes(acceptedPaymentTypes);
			transactionAcceptedPaymentTypes.setTransactionType(transactionType);
			currentAcceptedPaymentTypes.add(transactionAcceptedPaymentTypes);
			TransactionAcceptedPaymentTypes transactionAcceptedPaymentTypes2 = new TransactionAcceptedPaymentTypes();
			List<AcceptedPaymentType> acceptedPaymentTypes2 = new ArrayList(acceptedPaymentTypes);
			transactionAcceptedPaymentTypes2.setAcceptedPaymentTypes(acceptedPaymentTypes2);
			transactionAcceptedPaymentTypes2.setTransactionType(transactionType);
			allAcceptedPaymentTypes.add(transactionAcceptedPaymentTypes2);
		}
	}
	
	private boolean Find(PaymentTypes paymentType, List<AcceptedPaymentType> acceptedPaymentTypes)
	{
		for (AcceptedPaymentType acceptedPaymentType : acceptedPaymentTypes)
		{
			if (acceptedPaymentType.getPaymentTypeEntity().getId().shortValue() == paymentType.getValue().shortValue())
				return true;
		}
		return false;
	}
	
	private List<AcceptedPaymentType> GetSavePaymentTypes(TrxnTypes transType)
	{
		for (TransactionAcceptedPaymentTypes transactionAcceptedPaymentTypes : allAcceptedPaymentTypes)
		{
			if (transType.equals(transactionAcceptedPaymentTypes.getTransactionType()))
				return transactionAcceptedPaymentTypes.getAcceptedPaymentTypes();
		}
		return null;
	}
	
	private void addAcceptedPaymentTypeForATransaction(List<AcceptedPaymentType> addAcceptedPaymentTypes,
			TrxnTypes transactionType) throws Exception
	{
		
		for (TransactionAcceptedPaymentTypes transactionAcceptedPaymentTypes : currentAcceptedPaymentTypes)
		{
			TrxnTypes transType = transactionAcceptedPaymentTypes.getTransactionType();
			if (transType.equals(transactionType))
			{
				List<AcceptedPaymentType> paymentTypes = GetSavePaymentTypes(transType);
				List<AcceptedPaymentType> acceptedPaymentTypes = transactionAcceptedPaymentTypes.getAcceptedPaymentTypes();
				if ((acceptedPaymentTypes != null) &&(acceptedPaymentTypes.size() > 0))
				{
					for (PaymentTypes  paymentType : PaymentTypes.values())
						if (Find(paymentType, acceptedPaymentTypes) == false)
						{
							AcceptedPaymentType acceptedPaymentType = new AcceptedPaymentType();
							Short paymentTypeId = paymentType.getValue();
							PaymentTypeEntity paymentTypeEntity = new PaymentTypeEntity(paymentTypeId);
							acceptedPaymentType.setPaymentTypeEntity(paymentTypeEntity);
							TransactionTypeEntity transactionEntity = new TransactionTypeEntity();
							transactionEntity.setTransactionId(transType.getValue());
							acceptedPaymentType.setTransactionTypeEntity(transactionEntity);
							addAcceptedPaymentTypes.add(acceptedPaymentType);
							paymentTypes.add(acceptedPaymentType);
						}
				}
			}
		}
	}
	
	private void verify(List<AcceptedPaymentType> savedAcceptedPaymentTypes, List<AcceptedPaymentType> 
	                   acceptedPaymentTypes)
	{
		assertTrue(savedAcceptedPaymentTypes.size() == acceptedPaymentTypes.size());
		for (AcceptedPaymentType acceptedPaymentType : savedAcceptedPaymentTypes)
		{
			assertTrue(FindAcceptedPaymentType(acceptedPaymentType, acceptedPaymentTypes));
		}
	}
	
	private boolean FindAcceptedPaymentType(AcceptedPaymentType acceptedPaymentType, List<AcceptedPaymentType> 
    				acceptedPaymentTypes )
	{
		for (AcceptedPaymentType newAcceptedPaymentType : acceptedPaymentTypes)
			if ((newAcceptedPaymentType.getTransactionTypeEntity().getTransactionId().shortValue() 
					== acceptedPaymentType.getTransactionTypeEntity().getTransactionId().shortValue())
			 && (newAcceptedPaymentType.getPaymentTypeEntity().getId().shortValue()) 
			 == acceptedPaymentType.getPaymentTypeEntity().getId().shortValue())
				return true;
		return false;
	}
	
	private List<AcceptedPaymentType> GetBeforeTestPaymentTypes(TrxnTypes transType)
	{
		for (TransactionAcceptedPaymentTypes transactionAcceptedPaymentTypes : currentAcceptedPaymentTypes)
		{
			if (transType.equals(transactionAcceptedPaymentTypes.getTransactionType()))
				return transactionAcceptedPaymentTypes.getAcceptedPaymentTypes();
		}
		return null;
	}
	
	private boolean IsDeleted(AcceptedPaymentType a, List<AcceptedPaymentType> list)
	{
		if ((list == null) || (list.size() == 0))
			return true;
		for (AcceptedPaymentType type : list)
			if ((type.getTransactionTypeEntity().getTransactionId().shortValue() == 
				a.getTransactionTypeEntity().getTransactionId().shortValue())
				&& (type.getPaymentTypeEntity().getId().shortValue() == 
					a.getPaymentTypeEntity().getId().shortValue()))
				return false;
		return true;
	}
	
	private void deleteAcceptedPaymentTypeForATransaction(List<AcceptedPaymentType> deleteAcceptedPaymentTypes,
			TrxnTypes transactionType) throws Exception
	{
		
		List<AcceptedPaymentType> acceptedPaymentTypesFromDB =  acceptedPaymentTypePersistence.getAcceptedPaymentTypesForATransaction(transactionType.getValue());
		List<AcceptedPaymentType> acceptedPaymentTypes = GetBeforeTestPaymentTypes(transactionType);
		for (AcceptedPaymentType a : acceptedPaymentTypesFromDB)
		{
			if (IsDeleted(a, acceptedPaymentTypes))
				deleteAcceptedPaymentTypes.add(a);
		}
		
	}

	public void testAddDeleteAcceptedPaymentTypes() throws Exception
	{
		testGetAcceptedPaymentTypes();
		List<AcceptedPaymentType> addAcceptedPaymentTypes = new ArrayList<AcceptedPaymentType>();
		for (TrxnTypes transactionType : TrxnTypes.values())
		{
			addAcceptedPaymentTypeForATransaction(addAcceptedPaymentTypes, transactionType);	
		}
		if (addAcceptedPaymentTypes.size() > 0)
			acceptedPaymentTypePersistence.addAcceptedPaymentTypes(addAcceptedPaymentTypes);
		// verify results
		for (TrxnTypes transactionType : TrxnTypes.values()) 
		{
			List<AcceptedPaymentType> acceptedPaymentTypes = acceptedPaymentTypePersistence.getAcceptedPaymentTypesForATransaction(transactionType.getValue());
			List<AcceptedPaymentType> savedAcceptedPaymentTypes = GetSavePaymentTypes(transactionType);
			verify(savedAcceptedPaymentTypes, acceptedPaymentTypes);
		}
		
		// delete the added records to get back to before tests
		List<AcceptedPaymentType> deleteAcceptedPaymentTypes = new ArrayList<AcceptedPaymentType>();
		for (TrxnTypes transactionType : TrxnTypes.values())
		{
			deleteAcceptedPaymentTypeForATransaction(deleteAcceptedPaymentTypes, transactionType);	
		}
		if (deleteAcceptedPaymentTypes.size() > 0)
			acceptedPaymentTypePersistence.deleteAcceptedPaymentTypes(deleteAcceptedPaymentTypes);
//		 verify results
		for (TrxnTypes transactionType : TrxnTypes.values()) 
		{
			List<AcceptedPaymentType> acceptedPaymentTypes = acceptedPaymentTypePersistence.getAcceptedPaymentTypesForATransaction(transactionType.getValue());
			List<AcceptedPaymentType> savedAcceptedPaymentTypes = GetBeforeTestPaymentTypes(transactionType);
			verify(savedAcceptedPaymentTypes, acceptedPaymentTypes);
		}

		
	}
	
	private boolean FindEntity(List<PaymentTypeEntity> entityList, PaymentTypeEntity entity)
	{
		for (PaymentTypeEntity e : entityList)
			if ((e.getId().shortValue() == entity.getId().shortValue())
					&&(e.getName().equals(entity.getName())))
				return true;
		return false;
	}
	
	private void compare(List<PaymentTypeEntity> entityList, List<AcceptedPaymentType> acceptedPaymentTypeList)
	{
		assertTrue(entityList.size() == acceptedPaymentTypeList.size());
		for (AcceptedPaymentType acceptedPaymentType : acceptedPaymentTypeList)
			assertTrue(FindEntity(entityList, acceptedPaymentType.getPaymentTypeEntity()));
		
	}
	
	public void testRetrieveAcceptedPaymentTypes() throws Exception {
		for (TrxnTypes transactionType : TrxnTypes.values()) 
		{
			Short transactionId = transactionType.getValue();
			List<AcceptedPaymentType> acceptedPaymentTypes = 
				acceptedPaymentTypePersistence.getAcceptedPaymentTypesForATransaction(transactionId);
			List<PaymentTypeEntity> paymentTypeEntities = 
				acceptedPaymentTypePersistence.getAcceptedPaymentTypesForATransaction(DEFAULT_LOCALE_ID, transactionId);
			compare(paymentTypeEntities, acceptedPaymentTypes);
		}
		
	}

	
}

