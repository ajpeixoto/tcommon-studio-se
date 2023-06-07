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
package org.talend.core.model.metadata.builder.connection.impl;

import org.eclipse.emf.ecore.EClass;
import org.talend.core.model.metadata.builder.connection.ConnectionPackage;
import org.talend.core.model.metadata.builder.connection.TacokitDatabaseConnection;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Tacokit Database Connection</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class TacokitDatabaseConnectionImpl extends DatabaseConnectionImpl implements TacokitDatabaseConnection {

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected TacokitDatabaseConnectionImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return ConnectionPackage.Literals.TACOKIT_DATABASE_CONNECTION;
    }

    /**
     * @generated NOT
     */
    @Override
    public String getDriverJarPath() {
        String jdbcDriverData = (String) this.getProperties().get(KEY_DRIVER);
        String jdbcDriverPath = null;
        if (jdbcDriverData != null) {
            if (jdbcDriverData.startsWith("[") && jdbcDriverData.endsWith("]")) {
                jdbcDriverData = jdbcDriverData.substring(1, jdbcDriverData.length() - 1);
            }
            if (jdbcDriverData.startsWith("{") && jdbcDriverData.endsWith("}")) {
                jdbcDriverData = jdbcDriverData.substring(1, jdbcDriverData.length() - 1);
            }
            String[] arrays = jdbcDriverData.split(",");
            for (String array : arrays) {
                String[] arrs = array.split("=");
                if (arrs.length == 2 && "configuration.jdbcDriver[].path".equals(arrs[0].trim())) {
                    jdbcDriverPath = arrs[1];
                    break;
                }
            }
        }
        return jdbcDriverPath;
    }

    /**
     * @generated NOT
     */
    @Override
    public void setDriverJarPath(String value) {
        String jdbcDriverData = (String) this.getProperties().get(KEY_DRIVER);
        boolean hasSquareBrackets = false, hasBracket = false;

        if (jdbcDriverData != null) {
            if (jdbcDriverData.startsWith("[") && jdbcDriverData.endsWith("]")) {
                jdbcDriverData = jdbcDriverData.substring(1, jdbcDriverData.length() - 1);
                hasSquareBrackets = true;
            }
            if (jdbcDriverData.startsWith("{") && jdbcDriverData.endsWith("}")) {
                jdbcDriverData = jdbcDriverData.substring(1, jdbcDriverData.length() - 1);
                hasBracket = true;
            }
            String[] arrays = jdbcDriverData.split(",");
            for (int i = 0; i < arrays.length; i++) {
                String array = arrays[i];
                String[] arrs = array.split("=");
                if (arrs.length == 2 && "configuration.jdbcDriver[].path".equals(arrs[0].trim())) {
                    arrays[i] = "configuration.jdbcDriver[].path=" + value;
                    break;
                }
            }
            StringBuffer sb = new StringBuffer();
            if (hasSquareBrackets) {
                sb.append("[");
            }
            if (hasBracket) {
                sb.append("{");
            }
            for (int i = 0; i < arrays.length; i++) {
                sb.append(arrays[i]);
                if (i+1 < arrays.length) {
                    sb.append(",");
                }
            }
            if (hasSquareBrackets) {
                sb.append("]");
            }
            if (hasBracket) {
                sb.append("}");
            }
            this.getProperties().put(KEY_DRIVER, sb.toString());
        }
    }

    /**
     * @generated NOT
     */
    @Override
    public String getDriverClass() {
        return (String) this.getProperties().get(KEY_DRIVER_CLASS);
    }

    /**
     * @generated NOT
     */
    @Override
    public void setDriverClass(String value) {
        this.getProperties().put(KEY_DRIVER_CLASS, value);
    }

    /**
     * @generated NOT
     */
    @Override
    public String getURL() {
        return (String) this.getProperties().get(KEY_URL);
    }

    /**
     * @generated NOT
     */
    @Override
    public void setURL(String value) {
        this.getProperties().put(KEY_URL, value);
    }

    /**
     * @generated NOT
     */
    @Override
    public String getPort() {
        return (String) this.getProperties().get(KEY_PORT);
    }

    /**
     * @generated NOT
     */
    @Override
    public void setPort(String value) {
        this.getProperties().put(KEY_PORT, value);
    }

    /**
     * @generated NOT
     */
    @Override
    public String getUsername() {
        return (String) this.getProperties().get(KEY_USER_ID);
    }

    /**
     * @generated NOT
     */
    @Override
    public void setUsername(String value) {
        this.getProperties().put(KEY_USER_ID, value);

    }

    
    @Override
    public void setPassword(String newPassword) {
        this.password = newPassword;
        this.getProperties().put(KEY_PASSWORD, newPassword);
    }

    @Override
    public void setRawPassword(String value) {
        super.setRawPassword(value);
        this.getProperties().put(KEY_PASSWORD, this.password);
    }

    /**
     * @generated NOT
     */
    @Override
    public String getPassword() {
        return (String) this.getProperties().get(KEY_PASSWORD);
    }

    /**
     * @generated NOT
     */
    @Override
    public String getRawPassword() {
        this.password = getPassword();
        return super.getRawPassword();
    }

    /**
     * @generated NOT
     */
    @Override
    public String getDatabaseMappingFile() {
        if (this.getProperties().containsKey(KEY_DATABASE_MAPPING)) {
            return (String) this.getProperties().get(KEY_DATABASE_MAPPING);
        }
        return null;
    }

    /**
     * @generated NOT
     */
    @Override
    public boolean useSharedDBConnection() {
        if (this.getProperties().containsKey(KEY_USE_SHARED_DB_CONNECTION)) {
            return Boolean.valueOf((String)this.getProperties().get(KEY_USE_SHARED_DB_CONNECTION));
        }
        return false;
    }

    /**
     * @generated NOT
     */
    @Override
    public String getSharedDBConnectionName() {
        if (this.getProperties().containsKey(KEY_SHARED_DB_CONNECTION)) {
            return (String) this.getProperties().get(KEY_SHARED_DB_CONNECTION);
        }
        return null;
    }

    /**
     * @generated NOT
     */
    @Override
    public boolean useDatasource() {
        if (this.getProperties().containsKey(KEY_USE_DATASOURCE_NAME)) {
            return Boolean.valueOf((String)this.getProperties().get(KEY_USE_DATASOURCE_NAME));
        }
        return false;
    }
    
    /**
     * @generated NOT
     */
    @Override
    public String getDatasourceAlias() {
        if (this.getProperties().containsKey(KEY_DATASOURCE_NAME)) {
            return (String) this.getProperties().get(KEY_DATASOURCE_NAME);
        }
        return null;
    }
    

    /**
     * @generated NOT
     */
    @Override
    public String getAuthenticationType() {
        if (this.getProperties().containsKey(KEY_AUTHENTICATION_TYPE)) {
            return (String) this.getProperties().get(KEY_AUTHENTICATION_TYPE);
        }
        return null;
    }

    /**
     * @generated NOT
     */
    @Override
    public boolean useAutoCommit() {
        if (this.getProperties().containsKey(KEY_USE_AUTO_COMMIT)) {
            return Boolean.valueOf((String)this.getProperties().get(KEY_USE_AUTO_COMMIT));
        }
        return false;
    }

    /**
     * @generated NOT
     */
    @Override
    public boolean autoCommit() {
        if (this.getProperties().containsKey(KEY_AUTO_COMMIT)) {
            return Boolean.valueOf((String)this.getProperties().get(KEY_AUTO_COMMIT));
        }
        return false;
    }

    @Override
    public String getServerName() {
        if (this.getProperties().containsKey(KEY_HOST)) {
            return (String) this.getProperties().get(KEY_HOST);
        }
        return null;
    }

    @Override
    public void setServerName(String newServerName) {
        this.getProperties().put(KEY_HOST, newServerName);
    }
    
    public String getDatabaseType() {
        if (this.getProperties().containsKey(KEY_DB_TYPE)) {
            return (String) this.getProperties().get(KEY_DB_TYPE);
        }
        return null;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public void setDatabaseType(String newDatabaseType) {
        this.getProperties().put(KEY_DB_TYPE, newDatabaseType);
    }

    @Override
    public boolean enableDBType() {
        if (this.getProperties().containsKey(KEY_ENABLE_DB_TYPE)) {
            return Boolean.valueOf((String)this.getProperties().get(KEY_ENABLE_DB_TYPE));
        }
        return false;
    }
    
} //TacokitDatabaseConnectionImpl
