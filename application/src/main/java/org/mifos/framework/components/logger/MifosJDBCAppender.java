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

package org.mifos.framework.components.logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.jdbc.JDBCAppender;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.mifos.application.personnel.persistence.PersonnelPersistence;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;

/**
 * Enable the logger to log to a database (by extending JDBCAppender provided by
 * log4j).
 */
public class MifosJDBCAppender extends JDBCAppender {

    /**
     * This is the marker defining the statement, containing the logged user
     * within the log statement.
     */
    private static String userIdMarker;
    /** This is the marker defining the logged user office */
    private static String officeMarker;

    // Connection con;

    @Override
    public Connection getConnection() {
        return new PersonnelPersistence().getConnection();
    }

    /**
     * Close the connection to the database.
     */
    @Override
    public void closeConnection(Connection con) {
        // flushBuffer();

        try {
            if (con != null && !con.isClosed()) {
                StaticHibernateUtil.closeSession();
            }
        }// end-try
        catch (SQLException sqle) {
            errorHandler.error("Error closing connection", sqle, ErrorCode.GENERIC_FAILURE);
        }// end-catch
    }// end-method closeConnection

    /**
     * Method that obtains the sql as specified in the configuration xml file.
     * This is passed to a format function which returns a formatted sql which
     * includes inserting logged user and office of the user
     * 
     * @param event
     *            Log event generated by the logger
     * @return Formatted sql
     */
    @Override
    public String getLogStatement(LoggingEvent event) {
        // obtains the log message
        String rawMessage = (String) event.getMessage();

        // obtains the sql from the configuration file
        String rawSql = getLayout().format(event);

        // checks if the sql generated has any single quote character, it is
        // converted in to two single quotes which
        // is the escape sequence for databases.
        try {
            rawSql = rawSql.replaceAll(rawMessage, rawMessage.replaceAll("'", "''"));
        } catch (PatternSyntaxException pse) {
            pse.printStackTrace();
        }

        String formattedSql = format(rawMessage, rawSql);
        return formattedSql;
    }// end-method getLogStatement

    /**
     * Method that formats the sql. The userid and the officeid of the person
     * logging the statement are retrieved from the message. This is then added
     * to the database field corresponding to userid and officeid
     * 
     * @param rawMessage
     *            The message containing the logged user and the office id
     * @return Formatted sql
     */
    public String format(String rawMessage, String rawSql) {

        String userId = null;
        String officeId = null;

        // Obtaining the userIdMarker and officeIdMarker. The message string is
        // parsed on the basis of these markers
        // to obtain the logged user and office. Eg:
        // "The logged user is XYZ from Aditi". Here "The logged user is" is
        // the userIdMarker and "from" is the officeIdMarker. Parsing on the
        // basis of this will retrieve XYZ and Aditi
        // Obtained only once
        if (userIdMarker == null || officeMarker == null) {
            userIdMarker = ApplicationConfig.getUserIdMarker();
            officeMarker = ApplicationConfig.getOfficeMarker();
        }
        // parsing the string based on userIdMarker and officeIdMarker.
        int indexOfUserIdMarker = rawMessage.indexOf(userIdMarker);
        if (indexOfUserIdMarker >= 0) {
            // obtains the portion of the message containing logged user and
            // office
            String userAndOffice = rawMessage.substring(indexOfUserIdMarker, rawMessage.length());

            int firstIndexOfOfficeMarker = userAndOffice.indexOf(officeMarker);

            // Obtaining the logged user from the statement which contains the
            // userid and officeid
            userId = userAndOffice.substring(0 + userIdMarker.length(), firstIndexOfOfficeMarker).trim();

            // Obtaining the logged user office from the statement which
            // contains the userid and officeid
            officeId = userAndOffice
                    .substring(firstIndexOfOfficeMarker + officeMarker.length(), userAndOffice.length()).trim();
        }
        StringBuilder rawSQL = new StringBuilder(rawSql);

        // formatting the sql to include the new columns of userid and officeid
        rawSQL.insert(rawSQL.indexOf(")"), ",userid,officeid");
        // inserting the values of the two new fields
        rawSQL.insert(rawSQL.lastIndexOf(")") - 1, "', '" + userId + "', '" + officeId);

        return rawSQL.toString();
    }// end-method format

    @Override
    protected void execute(String sql) throws SQLException {
        if (null != getConnection()) {
            super.execute(sql);
        }
    }

}