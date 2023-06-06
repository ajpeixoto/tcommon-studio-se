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
package org.talend.core.model.metadata.builder.connection;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Tacokit Database Connection</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see org.talend.core.model.metadata.builder.connection.ConnectionPackage#getTacokitDatabaseConnection()
 * @model
 * @generated
 */
public interface TacokitDatabaseConnection extends DatabaseConnection {
    
    String KEY_DRIVER = "configuration.jdbcDriver";
    
    String KEY_DRIVER_CLASS = "configuration.jdbcClass";
    
    String KEY_URL = "configuration.jdbcUrl";
    
    String KEY_PORT = "configuration.port";
    
    String KEY_HOST = "configuration.dataSet.dataStore.host";
    
    String KEY_USER_ID = "configuration.userId";
    
    String KEY_PASSWORD = "configuration.password";
    
    String KEY_DATABASE_MAPPING = "configuration.dataSet.dataStore.dbMapping";
    
    String KEY_USE_SHARED_DB_CONNECTION = "configuration.dataSet.dataStore.useSharedDBConnection";
    
    String KEY_SHARED_DB_CONNECTION = "configuration.dataSet.dataStore.sharedDBConnectionName";
    
    String KEY_USE_DATASOURCE_NAME = "configuration.dataSet.dataStore.useDataSource";
    
    String KEY_DATASOURCE_NAME = "configuration.dataSet.dataStore.dataSourceAlias"; 
    
    String KEY_AUTHENTICATION_TYPE = "configuration.dataSet.dataStore.authenticationType";
    
    String KEY_USE_AUTO_COMMIT = "configuration.dataSet.dataStore.useAutoCommit";
    
    String KEY_AUTO_COMMIT = "configuration.dataSet.dataStore.autoCommit";
    
    public String getDatabaseMappingFile();
    
    public boolean useSharedDBConnection();
    
    public String getSharedDBConnectionName();
    
    public boolean useDatasourceName();
    
    public String getDatasourceName();
    
    public String getAuthenticationType();
    
    public boolean useAutoCommit();
    
    public boolean autoCommit();
    
    public String getHost();

} // TacokitDatabaseConnection
