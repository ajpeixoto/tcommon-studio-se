// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.talend.core.CorePlugin;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.utils.KeywordsValidator;
import org.talend.core.utils.TalendQuoteUtils;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class TalendTextUtils {

    public static final String SINGLE_QUOTE = TalendQuoteUtils.SINGLE_QUOTE;

    public static final String ANTI_QUOTE = TalendQuoteUtils.ANTI_QUOTE;

    public static final String QUOTATION_MARK = TalendQuoteUtils.QUOTATION_MARK;

    public static final String QUOTATION_ESC_MARK = TalendQuoteUtils.QUOTATION_ESC_MARK;

    public static final String LBRACKET = TalendQuoteUtils.LBRACKET;

    public static final String RBRACKET = TalendQuoteUtils.RBRACKET;

    public static final String JAVA_END_STRING = "."; //$NON-NLS-1$

    private static final int LINE_MAX_NUM = 100;

    private final static String DOUBLE_QUOTE = "\""; //$NON-NLS-1$

    private final static String DOUBLE_QUOTE_WITH_BACKSLASH = "\\\""; //$NON-NLS-1$

    private final static String SINGLE_BACKSLASH = "\\"; //$NON-NLS-1$

    private final static String DOUBLE_BACKSLASH = "\\\\"; //$NON-NLS-1$

    private final static String PASS_COVER = "********"; //$NON-NLS-1$

    /**
     * DOC Tao Tao Comment method "addStrInQuery". This method will add double quotes surrounding a String and convert
     * "\\" to "\\\\".
     *
     * @param input
     * @return
     */
    public static String addStrInQuery(String input) {
        String out = convertSlashForSpecialChar(input);
        return addQuotes(out, TalendQuoteUtils.SQL_SCRIPT);
    }

    public static String addQuotes(String text) {
        return TalendQuoteUtils.addQuotes(text);
    }

    public static String declareString(String input) {
        return TalendQuoteUtils.declareString(input);

    }

    public static String addQuotes(String text, String quoteStyle) {
        return TalendQuoteUtils.addQuotes(text, quoteStyle);
    }

    public static String addSQLQuotes(String text, boolean force) {
        if (force) { // in this case, do not consider context params
            return addSQLQuotes(text, QUOTATION_MARK);
        } else {
            return addSQLQuotes(text);
        }
    }

    public static String addSQLQuotes(String text) {
        if (ContextParameterUtils.isContainContextParam(text)) {
            return text;
        }
        return addSQLQuotes(text, QUOTATION_MARK);
    }

    private static String addSQLQuotes(String text, String quoteStyle) {

        String newString = checkAndAddSQLQuote(text, quoteStyle, true);

        // newString = newString.replaceAll("\r", " ");
        // newString = newString.replaceAll("\n", " ");
        // return widenSQLRestrict(newString, quoteStyle);
        return newString;
    }

    /*
     * for cdc component. such as \"field\",
     */
    public static String addSQLFieldQuotes(String text, String quoteStyle) {
        QueryUtil.isContextQuery = false; // clear the flag

        return checkAndAddSQLQuote(text, quoteStyle, false);
    }

    static String checkAndAddSQLQuote(String text, String quoteStyle, boolean internal) {
        String newString;

        String tempText = text;
        tempText = tempText.replaceAll("\r", " "); //$NON-NLS-1$ //$NON-NLS-2$
        tempText = tempText.replaceAll("\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
        tempText = tempText.trim();

        if (quoteStyle.equals(SINGLE_QUOTE)) {
            if (tempText.startsWith(SINGLE_QUOTE) && tempText.endsWith(SINGLE_QUOTE)) {
                newString = text;
            } else {
                newString = SINGLE_QUOTE + (internal ? TalendQuoteUtils.checkStringQuotes(text) : text) + SINGLE_QUOTE;
            }
            if (!internal) {
                newString = TalendQuoteUtils.checkStringQuotes(newString);
            }
        } else if (quoteStyle.equals(ANTI_QUOTE)) {
            newString = ANTI_QUOTE + (internal ? TalendQuoteUtils.checkStringQuotationMarks(text) : text) + ANTI_QUOTE;
            if (!internal) {
                newString = TalendQuoteUtils.checkStringQuotationMarks(newString);
            }
        } else if (quoteStyle.equals(LBRACKET) || quoteStyle.equals(RBRACKET)) {
            newString = LBRACKET + (internal ? TalendQuoteUtils.checkStringQuotationMarks(text) : text) + RBRACKET;
            if (!internal) {
                newString = TalendQuoteUtils.checkStringQuotationMarks(newString);
            }
        } else if (QueryUtil.isContextQuery) {
            newString = text;
            QueryUtil.isContextQuery = false;
        } else {
            if (tempText.startsWith(QUOTATION_MARK) && tempText.endsWith(QUOTATION_MARK)) {
                newString = text;
            } else {
                newString = QUOTATION_MARK + (internal ? TalendQuoteUtils.checkStringQuotationMarks(text) : text)
                        + QUOTATION_MARK;
            }
            if (!internal) {
                newString = TalendQuoteUtils.checkStringQuotationMarks(newString);
            }
        }
        return newString;

    }

    /**
     * DOC Tao Tao Comment method "convertSlashForSpecialChar". This method will convert "\\" to "\\\\", for example,
     * add a slash in String "'\\b'" to String "'\\\\b'".
     *
     * @param input
     * @return
     */
    public static String convertSlashForSpecialChar(String input) {

        if (input.contains(SINGLE_BACKSLASH)) {
            input = input.replace(SINGLE_BACKSLASH, DOUBLE_BACKSLASH);
        }

        if (input.contains(DOUBLE_QUOTE)) {
            input = input.replace(DOUBLE_QUOTE, DOUBLE_QUOTE_WITH_BACKSLASH);
        }

        return input;
    }

    /**
     * DOC Tao Tao Comment method "removeSlashForSpecialChar". This method will convert "\\\\" to "\\", for example,
     * remove a slash in String "'\\\\b'" to String "'\\b'".
     *
     * @param input
     * @return
     */
    public static String removeSlashForSpecialChar(String input) {

        if (input.contains(DOUBLE_BACKSLASH)) {
            input = input.replace(DOUBLE_BACKSLASH, SINGLE_BACKSLASH);
        }

        if (input.contains(DOUBLE_QUOTE_WITH_BACKSLASH)) {
            input = input.replace(DOUBLE_QUOTE_WITH_BACKSLASH, DOUBLE_QUOTE);
        }

        return input;
    }

    /**
     * DOC Tao Tao Comment method "removeStrInQuery". This method will remove String surrounding quotes and convert
     * "\\\\" to "\\".
     *
     * @param input
     * @return
     */
    public static String removeStrInQuery(String input) {
        String out = removeQuotes(input);
        return TalendTextUtils.removeSlashForSpecialChar(out);
    }

    /**
     * DOC qiang.zhang Comment method "widenRestrict".
     *
     * @param newString
     * @param quoteStyle
     * @return
     */
    private static String widenSQLRestrict(String newString, String quoteStyle) {
        String after = ""; //$NON-NLS-1$
        final String[] split = newString.split("\n"); //$NON-NLS-1$
        for (int i = 0; i < split.length; i++) {
            String string = split[i];
            if (i == 0) {
                after += getAfterString(quoteStyle, string);
            } else {
                after += getAfterString(quoteStyle, "\n" + string); //$NON-NLS-1$
            }
        }
        return after;
    }

    /**
     * DOC qiang.zhang Comment method "getAfterString".
     *
     * @param quoteStyle
     * @param string
     * @return
     */
    private static String getAfterString(String quoteStyle, String string) {
        String after = ""; //$NON-NLS-1$
        if (string.length() > LINE_MAX_NUM) {
            String substring = "";
            // bug21945 mssql contains FROM
            if (string.contains(" FROM ")) {
                int length = string.indexOf(" FROM ");
                substring = string.substring(0, length) + " ";
            } else {
                substring = string.substring(0, LINE_MAX_NUM);
            }
            substring = substring.substring(0, getLastWord(string, substring, quoteStyle));
            after += substring + "\n "; //$NON-NLS-1$
            // String temp = substring;
            //            temp = temp.replaceAll(" ", "").replaceAll("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            //            if (!"".equals(temp)) { //$NON-NLS-1$
            //                after += substring + "\"+\n\""; //$NON-NLS-1$
            // } else {
            // after += substring;
            // }
            after += getAfterString(quoteStyle, string.substring(substring.length()));
        } else {
            after += string;
        }
        return after;
    }

    /**
     * DOC qiang.zhang Comment method "getLastWord".
     *
     * @param substring
     * @param quoteStyle
     * @return
     */
    private static int getLastWord(String fullString, String substring, String quoteStyle) {
        if (substring.contains(",")) { //$NON-NLS-1$
            int lastIndexOf3 = substring.lastIndexOf(","); //$NON-NLS-1$
            if ((lastIndexOf3 + 1) < fullString.length()) {
                lastIndexOf3++;
            }
            return lastIndexOf3;
        } else if (substring.contains(" ")) { //$NON-NLS-1$
            int lastIndexOf3 = substring.lastIndexOf(" "); //$NON-NLS-1$
            if ((lastIndexOf3 + 1) < fullString.length()) {
                lastIndexOf3++;
            }
            return lastIndexOf3;
        } else {
            return substring.length();

        }
    }

    public static String addQuotesWithSpaceField(String fieldName, String dbType) {
        if (fieldName == null) {
            fieldName = ""; //$NON-NLS-1$
        }
        if (fieldName.startsWith("\"") && fieldName.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
            return fieldName;
        }
        boolean b = true;
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            b = b && c >= '0' && c <= '9';
        }
        // if fieldName include special symbol,need add quote
        boolean check = Pattern.matches("^.[A-Za-z_]+$", fieldName);//$NON-NLS-1$
        EDatabaseTypeName name = EDatabaseTypeName.getTypeFromDbType(dbType);

        boolean isCheck = false;
        String preferenceValue = CorePlugin.getDefault().getPreferenceStore().getString(ITalendCorePrefConstants.SQL_ADD_QUOTE);
        isCheck = !Boolean.parseBoolean(preferenceValue);
        if (!b) {
            if (isCheck && isPSQLSimilar(name) && check) {
                return fieldName;
            }
        }
        String newFieldName = fieldName;
        String quote = getQuoteByDBType(name);
        if (!newFieldName.contains(quote)) {
            newFieldName = addQuotes(newFieldName, quote);
        }
        return newFieldName;
    }

    public static String addQuotesWithSpaceFieldForSQLString(String fieldName, String dbType, boolean simple) {
        if (fieldName.startsWith("\"") && fieldName.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
            return fieldName;
        }
        boolean b = true;
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            b = b && c >= '0' && c <= '9';
        }
        EDatabaseTypeName name = EDatabaseTypeName.getTypeFromDbType(dbType);
        boolean isCheck = false;
        String preferenceValue = CorePlugin.getDefault().getPreferenceStore().getString(ITalendCorePrefConstants.SQL_ADD_QUOTE);
        isCheck = !Boolean.parseBoolean(preferenceValue);
        if (!b) {
            if (isCheck && isPSQLSimilar(name)) {
                return fieldName;
            }
        }
        String newFieldName = fieldName;
        String quote = getQuoteByDBType(name);
        if (!newFieldName.contains(quote)) {
            newFieldName = TalendQuoteUtils.addQuotesForSQLString(newFieldName, quote, simple);
        }
        return newFieldName;
    }

    public static String addQuotesWithSpaceFieldForSQLStringForce(String fieldName, String dbType, boolean simple) {

        EDatabaseTypeName name = EDatabaseTypeName.getTypeFromDbType(dbType);
        final String quote = getQuoteByDBType(name);
        boolean isCheck = false;
        String preferenceValue = CorePlugin.getDefault().getPreferenceStore().getString(ITalendCorePrefConstants.SQL_ADD_QUOTE);
        isCheck = Boolean.parseBoolean(preferenceValue);
        // added by hyWang(bug 6637),to see if the column name need to be add queotes
        // check the field name.

        String temp = removeQuotes(fieldName);
        Pattern pattern = Pattern.compile("\\w+"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(temp);

        // for bug 11938
        // to see if the table name or column name was start with number
        Pattern pattern2 = Pattern.compile("^[0-9]+[_0-9a-zA-Z]*$"); //$NON-NLS-1$
        Matcher matcher2 = pattern2.matcher(temp);

        // for bug 12092
        boolean isSqlKeyword = KeywordsValidator.isSqlKeyword(temp, name.getProduct());

        /*******************************************
         * This is a temp fix for snowflake database https://jira.talendforge.org/browse/TUP-31883
         *******************************************/
        if (EDatabaseTypeName.GENERAL_JDBC.equals(name) && temp.toUpperCase().equals("KEY")) {
            isSqlKeyword = false;
        }

        boolean isH2 = EDatabaseTypeName.H2 == name;

        // if the database type is IBMDB2/ORACLE and the field name contain lowercase character, should add quotes
        if (((!matcher.matches() || matcher2.matches() || isSqlKeyword) && !isH2 && EDatabaseTypeName.SAS != name && EDatabaseTypeName.IMPALA != name)
                || isDBContainLowerCase(dbType, fieldName)) {
            isCheck = true; // contain other char
        }

        if (!isCheck && isPSQLSimilar(name)
                && !(EDatabaseTypeName.MYSQL.equals(name) || EDatabaseTypeName.AMAZON_AURORA.equals(name))) {
            return fieldName;
        }
        String newFieldName = fieldName;

        newFieldName = TalendQuoteUtils.addQuotesForSQLString(newFieldName, quote, simple);
        return newFieldName;
    }

    /**
     * if the database type is IBM DB2/ORACLE and the field name contains lowercase character return true otherwise
     * return false.
     *
     * @param dbType
     * @param fieldName
     * @return
     */
    private static boolean isDBContainLowerCase(String dbType, String fieldName) {
        if (dbType != null && (dbType.equals(EDatabaseTypeName.IBMDB2.getDisplayName())
                || dbType.equals(EDatabaseTypeName.IBMDB2ZOS.getDisplayName())
                || dbType.equals(EDatabaseTypeName.ORACLEFORSID.getDisplayName())
                || dbType.equals(EDatabaseTypeName.ORACLE_CUSTOM.getDisplayName())
                || dbType.equals(EDatabaseTypeName.ORACLE_OCI.getDisplayName())
                || dbType.equals(EDatabaseTypeName.ORACLESN.getDisplayName()))) {
            String temp = removeQuotes(fieldName);
            Pattern pattern = Pattern.compile("^.*[a-z]+.*$"); //$NON-NLS-1$
            Matcher matcher = pattern.matcher(temp);
            return matcher.matches();
        }
        return false;
    }

    public static String getQuoteByDBType(EDatabaseTypeName name) {
        return TalendQuoteUtils.getQuoteByDBType(name);
    }

    public static String removeQuotesForField(String text, String dbType) {
        if (text == null) {
            return null;
        }
        String newText;
        TalendQuoteUtils.setLeft(true);
        EDatabaseTypeName name = EDatabaseTypeName.getTypeFromDbType(dbType);
        final String quoteByDBType = getQuoteByDBType(name);
        if (quoteByDBType.equals(LBRACKET)) {
            if (text.length() > 2) {
                newText = text.substring(1, text.length() - 1);
                if (text.contains(RBRACKET)) {
                    newText = newText.replaceAll(RBRACKET, ""); //$NON-NLS-1$
                } else {
                    newText = text;
                }
            } else {
                newText = text;
            }

        } else {
            newText = text.replaceAll(quoteByDBType, ""); //$NON-NLS-1$
        }
        return newText;
    }

    /**
     * qzhang Comment method "getQuoteByDBType".
     *
     * @param dbType
     * @param b
     * @return
     */
    public static String getQuoteByDBType(String dbType, boolean b) {
        TalendQuoteUtils.setLeft(b);
        EDatabaseTypeName name = EDatabaseTypeName.getTypeFromDbType(dbType);
        return getQuoteByDBType(name);
    }

    public static String removeQuotes(String text) {
        return TalendQuoteUtils.removeQuotes(text);
    }

    /**
     * @Deprecated use org.talend.utils.string.StudioContextUtils.removeQuotesIfExist instead
     */
    @Deprecated
    public static String removeQuotesIfExist(String text) {
        return org.talend.utils.string.StudioContextUtils.removeQuotesIfExist(text,
                org.talend.utils.string.StudioContextUtils.QUOTATION_MARK);
    }

    /**
     * @Deprecated use org.talend.utils.string.StudioContextUtils.removeQuotes
     *
     * @param text
     * @param quotation_mark2
     * @return
     */
    @Deprecated
    public static String removeQuotes(String text, String quotation) {
        return org.talend.utils.string.StudioContextUtils.removeQuotes(text, quotation);
    }

    public static boolean isEnclosed(String text) {
        return TalendQuoteUtils.isEnclosed(text);
    }

    public static String getStringConnect() {
        return TalendQuoteUtils.getStringConnect();
    }

    public static String getStringDeclare() {
        return TalendQuoteUtils.getStringDeclare();
    }

    /**
     * @Deprecated use org.talend.utils.string.StudioContextUtils.encodeValue instead
     */
    @Deprecated
    public static String trimParameter(String value) {
        return org.talend.utils.string.StudioContextUtils.encodeValue(value);
    }

    public static String getQuoteChar() {
        return TalendQuoteUtils.getQuoteChar();
    }

    /**
     *
     * ggu Comment method "filterQuote".
     *
     * used for the string parsing, will ignore the char \" or \'.
     */
    public static String filterQuote(final String str) {
        return TalendQuoteUtils.filterQuote(str);
    }

    /**
     *
     * judge whether str is null or length is zreo
     *
     * @param str
     * @return
     */
    public static boolean isEmptyString(final String str) {
        return str == null || str.length() <= 0;
    }

    /**
     *
     * DOC YeXiaowei Comment method "hidePassword".
     *
     * @param password
     * @return
     */
    public static String hidePassword(final String password) {
        if (ContextParameterUtils.containContextVariables(password)) {
            return password;
        }

        if(!TalendQuoteUtils.isCommonString(password)){
            return password;
        }

        return PASS_COVER;
    }

    public static String escapeJavaText(final String text) {
        return StringEscapeUtils.escapeJava(text);
    }

    public static String filterSpecialChar(String input) { // for 8115
        if (input != null && !"".equals(input)) {
            for (int i = 0; i < input.length();) {
                char ch = input.charAt(i);
                switch (ch) {
                case 0x0:
                case 0x1:
                case 0x2:
                case 0x3:
                case 0x4:
                case 0x5:
                case 0x6:
                case 0x7:
                case 0x8:
                case 0x9:
                case 0xB:
                case 0xC:
                case 0xE:
                case 0xF:
                case 0x10:
                case 0x11:
                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x17:
                case 0x18:
                case 0x19:
                case 0x1A:
                case 0x1B:
                case 0x1C:
                case 0x1D:
                case 0x1E:
                case 0x1F:
                    input = input.substring(0, i) + input.substring(i + 1, input.length());
                    break;
                default:
                    i++;

                }
            }
        }

        return input;
    }

    /**
     *
     * DOC xye Comment method "isPSQLSimilar".
     *
     * @param name
     * @return
     */
    private static boolean isPSQLSimilar(EDatabaseTypeName name) {
        return !name.equals(EDatabaseTypeName.PSQL)
                && !name.equals(EDatabaseTypeName.GREENPLUM);
    }

    /**
     *
     * DOC YeXiaowei KeyString class global comment. Detailled comment <br/>
     *
     */
    public static final class KeyString {

        private String string = null;

        private boolean key = false;

        public KeyString(final String string, final boolean key) {
            this.string = string;
            this.key = key;
        }

        public boolean isKey() {
            return this.key;
        }

        public String getString() {
            return this.string;
        }

        @Override
        public String toString() {
            return string + " ---> " + key; //$NON-NLS-1$
        }
    }
}
