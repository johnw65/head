/*
 * Copyright (c) 2005-2009 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.framework.util.helpers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;

import org.mifos.application.master.business.MifosCurrency;
import org.mifos.config.AccountingRules;
import org.mifos.core.CurrencyMismatchException;

/**
 * This class represents Money objects in the system, it should be used for all
 * financial operations like addition,subtraction etc of money. As of now it
 * deals with only one currency but later it can be extended to handle currency
 * conversions while performing operations.This is an immutable class as the
 * money object is not supposed to be modified .
 * 
 * 
 */
public final class Money implements Serializable {
    // adding a comparator, if currency are different throws a RuntimeException
    // saying cannot compare
    // otherwise delegates to BigDecimal.compareTo for comparision
    public static Comparator<Money> DEFAULT_COMPARATOR = new Comparator<Money>() {
        public int compare(Money m1, Money m2) {
            if (m1.isCurrencyDifferent(m2)) {
                throw new CurrencyMismatchException("Cannot compare money in differenct currencies");
            }
            return m1.amount.compareTo(m2.amount);
        }
    };

    /**
     * The precision used for internal calculations.
     */
    private static int internalPrecision = 13;
    /**
     * The rounding mode used for internal calculations.
     */
    private static RoundingMode internalRoundingMode = RoundingMode.HALF_UP;
    private static MathContext internalPrecisionAndRounding = new MathContext(internalPrecision, internalRoundingMode);

    private static MifosCurrency defaultCurrency = null;

    public static MifosCurrency getDefaultCurrency() {
        return defaultCurrency;
    }

    public static void setDefaultCurrency(MifosCurrency defaultCurrency) {
        Money.defaultCurrency = defaultCurrency;
    }

    public static int getInternalPrecision() {
        return internalPrecision;
    }

    public static MathContext getInternalPrecisionAndRounding() {
        return internalPrecisionAndRounding;
    }

    private final MifosCurrency currency;

    private final BigDecimal amount;

    public Money(BigDecimal amount) {
        this(getDefaultCurrency(), amount.setScale(internalPrecision, internalRoundingMode));
    }

    /**
     * This creates a Money object with currency set to MFICurrency and amount
     * set to zero.
     */
    public Money(MifosCurrency currency) {
        this(currency, new BigDecimal(0, internalPrecisionAndRounding));
    }

    public Money(String amount) {
        this(getDefaultCurrency(), amount);
    }
    
    public Money(MifosCurrency currency, String amount) {
        this(currency, new BigDecimal(amount, internalPrecisionAndRounding));
    }

    public Money(MifosCurrency currency, BigDecimal amount) {
        // FIXME null currency should not be allowed at Money creation
        //checkCurrencyNotNull(currency);
        checkAmountNotNull(amount);
        this.currency = currency;
        this.amount = amount.setScale(internalPrecision, internalRoundingMode);
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public double getAmountDoubleValue() {
        return amount.doubleValue();
    }

    public MifosCurrency getCurrency() {
        return currency;
    }

    /**
     * Returns true if currency is different.
     */
    private boolean isCurrencyDifferent(Money money) {
        return !this.currency.equals(money.getCurrency());
    }

    /**
     * If the object passed as parameter is null or if its currency or amount is
     * null it returns this else performs the required operation and returns a
     * new Money object corresponding to the value.
     */
    public Money add(Money money) {
        if (null != money) {
            if (isCurrencyDifferent(money)) {
                throw new CurrencyMismatchException(ExceptionConstants.ILLEGALMONEYOPERATION);
            }
        }
        // FIXME null currency should not be allowed at Money creation
        if (money == null || money.getCurrency() == null) {
            return this;
        }
        return new Money(currency, amount.add(money.getAmount()));
    }

    /**
     * If the object passed as parameter is null or if its currency or amount is
     * null it returns this else performs the required operation and returns a
     * new Money object corresponding to the value.
     */
    public Money subtract(Money money) {
        if (null != money) {
            if (isCurrencyDifferent(money)) {
                throw new CurrencyMismatchException(ExceptionConstants.ILLEGALMONEYOPERATION);
            }
        }
        // FIXME null currency should not be allowed at Money creation
        if (money == null || money.getCurrency() == null) {
            return this;
        }

        return new Money(currency, amount.subtract(money.getAmount()));

    }

    /**
     * If the object passed as parameter is null or if its currency or amount is
     * null it returns this else performs the required operation and returns a
     * new Money object corresponding to the value.
     */
    public Money multiply(Money money) {
        if (null != money) {
            if (isCurrencyDifferent(money)) {
                throw new CurrencyMismatchException(ExceptionConstants.ILLEGALMONEYOPERATION);
            }
        }
        // FIXME null currency should not be allowed at Money creation
        if (money == null || money.getCurrency() == null) {
            return this;
        }

        return new Money(currency, amount.multiply(money.getAmount()).setScale(
                internalPrecisionAndRounding.getPrecision(), internalPrecisionAndRounding.getRoundingMode()));
    }

    public Money multiply(Double factor) {
        if (factor == null) {
            throw new CurrencyMismatchException(ExceptionConstants.ILLEGALMONEYOPERATION);
        }
        return new Money(currency, amount.multiply(new BigDecimal(factor)).setScale(
                internalPrecisionAndRounding.getPrecision(), internalPrecisionAndRounding.getRoundingMode()));
    }

    public Money multiply(BigDecimal factor) {
        if (factor == null) {
            throw new CurrencyMismatchException(ExceptionConstants.ILLEGALMONEYOPERATION);
        }
        return new Money(currency, amount.multiply(factor).setScale(internalPrecisionAndRounding.getPrecision(),
                internalPrecisionAndRounding.getRoundingMode()));
    }

    /**
     * Dividing by Money doesn't seem to make sense. It should be dividing by a
     * BigDecimal. This method should be eliminated.
     */
    public Money divide(Money money) {
        if (null != money) {
            if (isCurrencyDifferent(money)) {
                throw new CurrencyMismatchException(ExceptionConstants.ILLEGALMONEYOPERATION);
            }
        }
        // FIXME null currency should not be allowed at Money creation
        if (money == null || money.getCurrency() == null) {
            return this;
        }

        return new Money(currency, amount.divide(money.getAmount(), internalPrecisionAndRounding));
    }

    public Money divide(BigDecimal factor) {
        return new Money(currency, amount.divide(factor, internalPrecisionAndRounding));
    }

    
    public Money negate() {
     // no need to set scale since negation preserves scale
        return new Money(currency, amount.negate());
    }

    /**
     * This method returns a new Money object with currency same as current
     * currency and amount calculated after rounding based on rounding mode and
     * roundingAmount where in both are obtained from MifosCurrency object.
     * 
     * The rounding calculation is as follows:- Lets say we want to round 142.34
     * to nearest 50 cents and and rounding mode is ceil (i.e. to greater
     * number) we will divide 142.34 by .5 which will result in 284.68 now we
     * will round this to a whole number using ceil mode which will result in
     * 285 and then multiply 285 by 0.5 resulting in 142.5.
     * 
     */
    public static Money round(Money money) {
        if (null != money) {
            BigDecimal roundingAmount = new BigDecimal(money.getCurrency().getRoundingAmount().doubleValue(),
                    internalPrecisionAndRounding);
            BigDecimal nearestFactor = money.getAmount().divide(roundingAmount, internalPrecisionAndRounding);

            nearestFactor = nearestFactor.setScale(0, money.getCurrency().getRoundingModeEnum());

            BigDecimal roundedAmount = nearestFactor.multiply(roundingAmount);
            return new Money(money.getCurrency(), roundedAmount);
        }
        return money;
    }

    public static Money round(Money money, BigDecimal roundOffMultiple, RoundingMode roundingMode) {
        // should we allow a null money or throw and exception instead?
        if (null != money) {
            // insure that we are using the correct internal precision
            BigDecimal roundingAmount = roundOffMultiple.round(internalPrecisionAndRounding);
            BigDecimal nearestFactor = money.getAmount().divide(roundingAmount, internalPrecisionAndRounding);

            nearestFactor = nearestFactor.setScale(0, roundingMode);

            BigDecimal roundedAmount = nearestFactor.multiply(roundingAmount);
            return new Money(money.getCurrency(), roundedAmount);
        }
        return money;
    }

    /**
     * This method return true if the currency associated with the two money
     * objects is equal and also the compareTo method of BigDecimal return 0 for
     * the amount of the two money objects. It is not advisable to use equals
     * method of BigDecimal because it would return false for numbers like 10.0
     * and 10.00 instead we should use compareTo.
     * 
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Money)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        Money money = (Money) obj;
        return this.currency.equals(money.getCurrency()) && (this.amount.compareTo(money.getAmount()) == 0);
    }

    @Override
    public int hashCode() {
    // FIXME null currency should not be allowed at Money creation
        if (currency == null) {
            return System.identityHashCode(null);
        }
        return this.currency.getCurrencyId() * 100 + this.amount.intValue();
    }

    @Override
    public String toString() {
    // FIXME null currency should not be allowed at Money creation
    // FIXME string formating based on Accounting rule should be done in MoneyUtil class
    //only string representation of BigDecimal should be returned here
        if (currency != null) {
            double doubleValue = amount.doubleValue();
            String format = "%." + AccountingRules.getDigitsAfterDecimal().toString() + "f";
            String formatStr = String.format(Locale.ENGLISH, format, 0.0);
            NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
            DecimalFormat decimalFormat = null;
            if (numberFormat instanceof DecimalFormat) {
                decimalFormat = ((DecimalFormat) numberFormat);
                decimalFormat.applyPattern(formatStr);
                return decimalFormat.format(doubleValue);
            }
            return numberFormat.format(doubleValue);
        }
        return "0";
    }

    public boolean isGreaterThan(Money money) {
        return Money.DEFAULT_COMPARATOR.compare(this, money) > 0;
    }

    public boolean isLessThan(Money money) {
        return Money.DEFAULT_COMPARATOR.compare(this, money) < 0;
    }
    
    private void checkCurrencyNotNull(MifosCurrency currency) {
        if(currency == null) {
            throw new NullPointerException("Currency must not be null");
        }
    }
    
    private void checkAmountNotNull(BigDecimal amount) {
        if(amount == null) {
            throw new NullPointerException("Amount must not be null");
        }
        
    }

}