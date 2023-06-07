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
    
    String KEY_HOST = "configuration.host";
    
    String KEY_USER_ID = "configuration.userId";
    
    String KEY_PASSWORD = "configuration.password";
    
    String KEY_DATABASE_MAPPING = "configuration.dbMapping";
    
    String KEY_USE_SHARED_DB_CONNECTION = "configuration.useSharedDBConnection";
    
    String KEY_SHARED_DB_CONNECTION = "configuration.sharedDBConnectionName";
    
    String KEY_USE_DATASOURCE_NAME = "configuration.useDataSource";
    
    String KEY_DATASOURCE_NAME = "configuration.dataSourceAlias"; 
    
    String KEY_AUTHENTICATION_TYPE = "configuration.authenticationType";
    
    String KEY_USE_AUTO_COMMIT = "configuration.useAutoCommit";
    
    String KEY_AUTO_COMMIT = "configuration.autoCommit";
    
    String KEY_ENABLE_DB_TYPE = "configuration.enableDBType";
    
    String KEY_DB_TYPE = "configuration.dbType";
    
    String KEY_DATASTORE_DRIVER = "configuration.dataSet.dataStore.jdbcDriver";
    
    String KEY_DATASTORE_DRIVER_CLASS = "configuration.dataSet.dataStore.jdbcClass";
    
    String KEY_DATASTORE_URL = "configuration.dataSet.dataStore.jdbcUrl";
    
    String KEY_DATASTORE_PORT = "configuration.dataSet.dataStore.port";
    
    String KEY_DATASTORE_HOST = "configuration.dataSet.dataStore.host";
    
    String KEY_DATASTORE_USER_ID = "configuration.dataSet.dataStore.userId";
    
    String KEY_DATASTORE_PASSWORD = "configuration.dataSet.dataStore.password";
    
    String KEY_DATASTORE_DATABASE_MAPPING = "configuration.dataSet.dataStore.dbMapping";
    
    String KEY_DATASTORE_USE_SHARED_DB_CONNECTION = "configuration.dataSet.dataStore.useSharedDBConnection";
    
    String KEY_DATASTORE_SHARED_DB_CONNECTION = "configuration.dataSet.dataStore.sharedDBConnectionName";
    
    String KEY_DATASTORE_USE_DATASOURCE_ALIAS = "configuration.dataSet.dataStore.useDataSource";
    
    String KEY_DATASTORE_DATASOURCE_ALIAS = "configuration.dataSet.dataStore.dataSourceAlias"; 
    
    String KEY_DATASTORE_AUTHENTICATION_TYPE = "configuration.dataSet.dataStore.authenticationType";
    
    String KEY_DATASTORE_USE_AUTO_COMMIT = "configuration.dataSet.dataStore.useAutoCommit";
    
    String KEY_DATASTORE_AUTO_COMMIT = "configuration.dataSet.dataStore.autoCommit";
    
    String KEY_DATASTORE_ENABLE_DB_TYPE = "configuration.dataSet.dataStore.enableDBType";
    
    String KEY_DATASTORE_DB_TYPE = "configuration.dataSet.dataStore.dbType";
    
    public String getDatabaseMappingFile();
    
    public boolean useSharedDBConnection();
    
    public String getSharedDBConnectionName();
    
    public boolean useDatasource();
    
    public String getDatasourceAlias();
    
    public String getAuthenticationType();
    
    public boolean useAutoCommit();
    
    public boolean autoCommit();

    public boolean enableDBType();
    
} // TacokitDatabaseConnection
