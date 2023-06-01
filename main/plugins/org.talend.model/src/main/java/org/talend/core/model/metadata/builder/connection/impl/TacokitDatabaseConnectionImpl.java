/**
 */
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
        String jdbcDriverData = (String) this.getProperties().get("configuration.jdbcDriver");
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
                if (arrs.length == 2 && "configuration.jdbcDriver[].path".equals(arrs[0])) {
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
        String jdbcDriverData = (String) this.getProperties().get("configuration.jdbcDriver");
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
                if (arrs.length == 2 && "configuration.jdbcDriver[].path".equals(arrs[0])) {
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
            this.getProperties().put("configuration.jdbcDriver", sb.toString());
        }// TODO --KK
    }

    /**
     * @generated NOT
     */
    @Override
    public String getDriverClass() {
        return (String) this.getProperties().get("configuration.jdbcClass");
    }

    /**
     * @generated NOT
     */
    @Override
    public void setDriverClass(String value) {
        this.getProperties().put("configuration.jdbcClass", value);
    }

    /**
     * @generated NOT
     */
    @Override
    public String getURL() {
        return (String) this.getProperties().get("configuration.jdbcUrl");
    }

    /**
     * @generated NOT
     */
    @Override
    public void setURL(String value) {
        this.getProperties().put("configuration.jdbcUrl", value);
    }

    /**
     * @generated NOT
     */
    @Override
    public String getPort() {
        return (String) this.getProperties().get("configuration.port");
    }

    /**
     * @generated NOT
     */
    @Override
    public void setPort(String value) {
        this.getProperties().put("configuration.port", value);
    }

    /**
     * @generated NOT
     */
    @Override
    public String getUsername() {
        return (String) this.getProperties().get("configuration.userId");
    }

    /**
     * @generated NOT
     */
    @Override
    public void setUsername(String value) {
        this.getProperties().put("configuration.userId", value);

    }

    
    @Override
    public void setPassword(String newPassword) {
        this.password = newPassword;
        this.getProperties().put("configuration.password", newPassword);
    }

    @Override
    public void setRawPassword(String value) {
        super.setRawPassword(value);
        this.getProperties().put("configuration.password", this.password);
    }

    /**
     * @generated NOT
     */
    @Override
    public String getPassword() {
        return (String) this.getProperties().get("configuration.password");
    }

    @Override
    public String getRawPassword() {
        this.password = getPassword();
        return super.getRawPassword();
    }
    
    
} //TacokitDatabaseConnectionImpl
