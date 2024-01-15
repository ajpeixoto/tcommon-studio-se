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
package org.talend.core.model.metadata.builder.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.utils.PasswordEncryptUtil;
import org.talend.core.IRepositoryContextService;
import org.talend.core.database.conn.ConnParameterKeys;
import org.talend.core.database.conn.DatabaseConnStrUtil;
import org.talend.core.database.conn.HiveConfKeysForTalend;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.TacokitDatabaseConnection;
import org.talend.core.model.metadata.builder.database.dburl.SupportDBUrlStore;
import org.talend.core.model.metadata.builder.database.dburl.SupportDBUrlType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.cwm.helper.TaggedValueHelper;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.utils.security.StudioEncryption;
import org.talend.utils.sql.ConnectionUtils;
import org.talend.utils.sugars.ReturnCode;
import org.talend.utils.sugars.TypedReturnCode;

import metadata.managment.i18n.Messages;

/**
 * @author scorreia
 *
 * This utility class provides methods that convert CWM object into java.sql object. It is a kind of inverse of the
 * DatabaseContentRetriever class.
 */
public final class JavaSqlFactory {

    public static final String DEFAULT_USERNAME = "root"; //$NON-NLS-1$

    @SuppressWarnings("unused")
    private static Logger log = Logger.getLogger(JavaSqlFactory.class);

    private JavaSqlFactory() {
    }

    // the cache used for prompt context variables
    public static boolean haveSetPromptContextVars = false;

    // key = group name + context id + context variable name
    public static Map<String, String> promptContextVars = new HashMap<String, String>();

    public static String getPromptConVarsMapKey(Connection conn, String variableName) {
        return getPromptConVarsMapKey(conn.getContextName(), conn.getContextId(), variableName);
    }

    public static String getPromptConVarsMapKey(String contextGroupName, String uniqueId, String variableName) {
        // if connection, key = group name + context id + context variable name
        // if analysis internal context, key = group name + ResourceHelper.getUUID(cpt) + context variable name
        return contextGroupName + "-" + uniqueId + "-" + variableName; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static void savePromptConVars2Cache(Connection conn, IContextParameter param, List<IContext> iContexts) {
        if (param != null && (param.isPromptNeeded() || ContextUtils.isPromptNeeded(iContexts, param.getName()))) {
            String promptConVarsMapKey = getPromptConVarsMapKey(conn, "context." + param.getName()); //$NON-NLS-1$
            String paramValue = param.getValue();
            if (PasswordEncryptUtil.isPasswordType(param.getType())) {
                if (StudioEncryption.hasEncryptionSymbol(paramValue)) {
                    paramValue = StudioEncryption.getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                            .decrypt(paramValue);
                }
            }
            promptContextVars.put(promptConVarsMapKey, paramValue);
        }
    }

    public static void clearPromptContextCache() {
        if (Platform.isRunning()) {
            haveSetPromptContextVars = false;
            promptContextVars.clear();
        }
    }

    public static void saveReportPromptConVars2Cache(String groupName, IContextParameter param,
            List<IContext> iContexts) {
        if (param != null && (param.isPromptNeeded() || ContextUtils.isPromptNeeded(iContexts, param.getName()))) {
            String promptConVarsMapKey =
                    getPromptConVarsMapKey(groupName, param.getSource(), "context." + param.getName()); //$NON-NLS-1$
            String paramValue = param.getValue();
            if (PasswordEncryptUtil.isPasswordType(param.getType())) {
                if (StudioEncryption.hasEncryptionSymbol(paramValue)) {
                    paramValue = StudioEncryption.getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM)
                            .decrypt(paramValue);
                }
            }
            promptContextVars.put(promptConVarsMapKey, paramValue);
        }
    }

    public static String getReportPromptConValueFromCache(String groupName, String contextId, String contextVarName) {
        String promptConVarsMapKey = getPromptConVarsMapKey(groupName, contextId, contextVarName);
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            return promptContextVars.get(promptConVarsMapKey);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Method "createConnection" returns the connection with {@link ReturnCode#getObject()} if {@link ReturnCode#isOk()} is true.
     * This is the behaviour when everything goes ok.
     * <p>
     * When something goes wrong, {@link ReturnCode#isOk()} is false and {@link ReturnCode#getMessage()} gives the error message.
     * <p>
     * The created connection must be closed by the caller. (use {@link ConnectionUtils#closeConnection(Connection)})
     *
     * @param providerConnection the provider connection
     * @return a ReturnCode (never null)
     * @deprecated {@link #createConnection(Connection)}
     */
    @Deprecated
    public static TypedReturnCode<java.sql.Connection> createConnection(DatabaseConnection providerConnection) {
        TypedReturnCode<java.sql.Connection> rc = new TypedReturnCode<java.sql.Connection>(false);
        String url = providerConnection.getURL();
        if (url == null) {
            rc.setMessage(Messages.getString("JavaSqlFactory.DatabaseConnectionNull")); //$NON-NLS-1$
            rc.setOk(false);
        }
        String driverClassName = providerConnection.getDriverClass();
        Properties props = new Properties();
        props.put(TaggedValueHelper.USER, providerConnection.getUsername());
        props.put(TaggedValueHelper.PASSWORD, providerConnection.getRawPassword());
        String pass = props.getProperty(TaggedValueHelper.PASSWORD);
        if (pass != null) {
            String clearTextPassword = providerConnection.getRawPassword();
            if (clearTextPassword == null) {
                rc.setMessage(Messages.getString("JavaSqlFactory.UnableDecryptPassword")); //$NON-NLS-1$
                rc.setOk(false);
            } else {
                props.setProperty(TaggedValueHelper.PASSWORD, clearTextPassword);
            }
        }
        try {
            java.sql.Connection connection = ConnectionUtils.createConnection(url, driverClassName, props);
            rc.setObject(connection);
            rc.setOk(true);
        } catch (SQLException e) {
            rc.setReturnCode(e.getMessage(), false);
        } catch (InstantiationException e) {
            rc.setReturnCode(e.getMessage(), false);
        } catch (IllegalAccessException e) {
            rc.setReturnCode(e.getMessage(), false);
        } catch (ClassNotFoundException e) {
            rc.setReturnCode(e.getMessage(), false);
        }
        return rc;
    }

    /**
     * Method "createConnection" returns the connection with {@link ReturnCode#getObject()} if {@link ReturnCode#isOk()} is true.
     * This is the behaviour when everything goes ok.
     * <p>
     * When something goes wrong, {@link ReturnCode#isOk()} is false and {@link ReturnCode#getMessage()} gives the error message.
     * <p>
     * The created connection must be closed by the caller. (use {@link ConnectionUtils#closeConnection(Connection)})
     *
     * @param connection the connection (DatabaseConnection MDMConnection or others)
     * @return a ReturnCode (never null)
     */
    public static TypedReturnCode<java.sql.Connection> createConnection(Connection connection) {
        TypedReturnCode<java.sql.Connection> rc = new TypedReturnCode<java.sql.Connection>(false);
        String url = getURL(connection);
        if (url == null || !(connection instanceof DatabaseConnection)) {
            rc.setMessage(Messages.getString("JavaSqlFactory.DatabaseConnectionNull")); //$NON-NLS-1$
            rc.setOk(false);
            return rc; // MOD scorreia 2010-10-20 bug 16403 avoid NPE while importing items
        }
        return MetadataConnectionUtils.createConnection((DatabaseConnection) connection);
    }

    /**
     * DOC xqliu Comment method "getDriverClass".
     *
     * @param conn
     * @return driver class name of the connection or null
     */
    public static String getDriverClass(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            String driverClassName = getOriginalValueConnection(dbConn).getDriverClass();
            // SG : issue http://talendforge.org/bugs/view.php?id=16199
            if (driverClassName == null) {// no drive is specified so let us try
                                          // to guess it
                SupportDBUrlType dbType = SupportDBUrlStore.getInstance().findDBTypeByName(dbConn.getDatabaseType());
                if (dbType != null) {
                    driverClassName = dbType.getDbDriver();
                }// else we keep the drive class to null, we do not know how to
                 // guess it anymore.
            } // else we are ok
            return driverClassName;
        }
        DelimitedFileConnection dfConn = SwitchHelpers.DELIMITEDFILECONNECTION_SWITCH.doSwitch(conn);
        if (dfConn != null) {
            return ""; //$NON-NLS-1$
        }
        return null;
    }

    /**
     * set Url of connection. when the connection is MDM connection, set its pathname, when the connection is file
     * connection, set its filepath.
     *
     * @param conn
     * @param url
     */
    public static void setURL(Connection conn, String url) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            dbConn.setURL(url);
        }
        // MOD qiongli 2011-1-9 feature 16796
        DelimitedFileConnection dfConnection = SwitchHelpers.DELIMITEDFILECONNECTION_SWITCH.doSwitch(conn);
        if (dfConnection != null) {
            dfConnection.setFilePath(url);
        }
    }

    /**
     * DOC xqliu Comment method "getUsername".
     *
     * @param conn
     * @return username of the connection or null
     */
    public static String getUsername(Connection conn) {
        String userName = "";//$NON-NLS-1$
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            userName = getOriginalValueConnection(dbConn).getUsername();// root
        }
        if (userName == null) {
            userName = "";//$NON-NLS-1$
        }
        return userName;
    }

    /**
     * DOC xqliu Comment method "getPassword".
     *
     * @param conn
     * @return password of the connection or null
     */
    public static String getPassword(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        String psw = "";//$NON-NLS-1$
        if (dbConn != null) {
            String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, dbConn.getPassword());
            if (Platform.isRunning() && haveSetPromptContextVars && promptContextVars.containsKey(promptConVarsMapKey)) {// context.a2_Password
                psw = promptContextVars.get(promptConVarsMapKey);
            } else {
                psw = getOriginalValueConnection(dbConn).getRawPassword();// ""
            }
        }
        if (psw == null) {
            psw = "";//$NON-NLS-1$
        }
        return psw;
    }

    /**
     * DOC xqliu Comment method "setUsername".
     *
     * @param conn
     * @param username
     */
    public static void setUsername(Connection conn, String username) {
        ConnectionHelper.setUsername(conn, username);
    }

    /**
     * DOC xqliu Comment method "setPassword".
     *
     * @param conn
     * @param password
     */
    public static void setPassword(Connection conn, String password) {
        ConnectionHelper.setPassword(conn, password);
    }

    /**
     * set connection prompt context values from cache.
     * 
     * @param conn
     */
    public static void setPromptContextValues(Connection conn) {
        if (Platform.isRunning()) {
            DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
            if (dbConn != null) {
                // for TCK JDBC
                // username,password, jdbcUrl,jdbcDriver,jdbcClass
                if (dbConn instanceof TacokitDatabaseConnection) {
                    setPromptContextUrl(dbConn);
                    setPromptContextDriverClass(dbConn);
                    setPromptContextDriverJarPath(dbConn);
                }
                setPromptContextPassword(dbConn);
                setPromptContextUsername(dbConn);
                setPromptContextServerName(dbConn);
                setPromptContextPort(dbConn);
                setPromptContextSID(dbConn);
                setPromptContextAdditionalParams(dbConn);
                setPromptContextUiSchema(dbConn);
            }

            DelimitedFileConnection fileConn = SwitchHelpers.DELIMITEDFILECONNECTION_SWITCH.doSwitch(conn);
            if (fileConn != null) {
                setPromptContextFilePath(fileConn);
                setPromptContextFileEncoding(fileConn);
                setPromptContextFileRowSeparator(fileConn);
                setPromptContextFileFieldSeparator(fileConn);
                setPromptContextFileHeader(fileConn);
                setPromptContextFileFooter(fileConn);
                setPromptContextFileLimitValue(fileConn);
            }
        }
    }

    /**
     * DOC msjian Comment method "setPromptContextFileLimitValue".
     * 
     * @param fileConn
     */
    private static void setPromptContextFileLimitValue(DelimitedFileConnection fileConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(fileConn, fileConn.getLimitValue());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            fileConn.setLimitValue(promptConVarsMapKey);
        }
    }

    /**
     * DOC msjian Comment method "setPromptContextFileFooter".
     * 
     * @param fileConn
     */
    private static void setPromptContextFileFooter(DelimitedFileConnection fileConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(fileConn, fileConn.getFooterValue());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            fileConn.setFooterValue(promptContextVars.get(promptConVarsMapKey));
        }
    }

    /**
     * DOC msjian Comment method "setPromptContextFileHeader".
     * 
     * @param fileConn
     */
    private static void setPromptContextFileHeader(DelimitedFileConnection fileConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(fileConn, fileConn.getHeaderValue());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            fileConn.setHeaderValue(promptContextVars.get(promptConVarsMapKey));
        }
    }

    /**
     * DOC msjian Comment method "setPromptContextFieldSeparator".
     * 
     * @param fileConn
     */
    private static void setPromptContextFileFieldSeparator(DelimitedFileConnection fileConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(fileConn, fileConn.getFieldSeparatorValue());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            fileConn.setFieldSeparatorValue(promptContextVars.get(promptConVarsMapKey));
        }
    }

    /**
     * DOC msjian Comment method "setPromptContextFileRowSeparator".
     * 
     * @param fileConn
     */
    private static void setPromptContextFileRowSeparator(DelimitedFileConnection fileConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(fileConn, fileConn.getRowSeparatorValue());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            fileConn.setRowSeparatorValue(promptContextVars.get(promptConVarsMapKey));
        }
    }

    /**
     * DOC msjian Comment method "setPromptContextFileEncoding".
     * 
     * @param fileConn
     */
    private static void setPromptContextFileEncoding(DelimitedFileConnection fileConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(fileConn, fileConn.getEncoding());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            fileConn.setEncoding(promptContextVars.get(promptConVarsMapKey));
        }
    }

    /**
     * DOC msjian Comment method "setPromptContextFilePath".
     * 
     * @param fileConn
     */
    private static void setPromptContextFilePath(DelimitedFileConnection fileConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(fileConn, fileConn.getFilePath());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            fileConn.setFilePath(promptContextVars.get(promptConVarsMapKey));
        }
    }

    private static void setPromptContextPassword(DatabaseConnection dbConn) {
        // format like: Default-_NtX8IG5LEe6Fac08UAbwqg-context.context_jdbcmysql21_password
        String variableName =
                dbConn instanceof TacokitDatabaseConnection ? dbConn.getRawPassword() : dbConn.getPassword();
        String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, variableName);
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            dbConn.setRawPassword(promptContextVars.get(promptConVarsMapKey));
        }
    }

    private static void setPromptContextUsername(DatabaseConnection dbConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, dbConn.getUsername());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            dbConn.setUsername(promptContextVars.get(promptConVarsMapKey));
        }
    }

    private static void setPromptContextUrl(DatabaseConnection dbConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, dbConn.getURL());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            dbConn.setURL(promptContextVars.get(promptConVarsMapKey));
        }
    }

    private static void setPromptContextDriverClass(DatabaseConnection dbConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, dbConn.getDriverClass());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            dbConn.setDriverClass(promptContextVars.get(promptConVarsMapKey));
        }
    }

    private static void setPromptContextDriverJarPath(DatabaseConnection dbConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, dbConn.getDriverJarPath());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            dbConn.setDriverJarPath(promptContextVars.get(promptConVarsMapKey));
        }
    }

    /**
     * // host
     * 
     * @param dbConn
     */
    private static void setPromptContextServerName(DatabaseConnection dbConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, dbConn.getServerName());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            dbConn.setServerName(promptContextVars.get(promptConVarsMapKey));
        }
    }

    private static void setPromptContextPort(DatabaseConnection dbConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, dbConn.getPort());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            dbConn.setPort(promptContextVars.get(promptConVarsMapKey));
        }
    }

    private static void setPromptContextSID(DatabaseConnection dbConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, dbConn.getSID());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            dbConn.setSID(promptContextVars.get(promptConVarsMapKey));
        }
    }

    private static void setPromptContextUiSchema(DatabaseConnection dbConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, dbConn.getUiSchema());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            dbConn.setUiSchema(promptContextVars.get(promptConVarsMapKey));
        }
    }

    private static void setPromptContextAdditionalParams(DatabaseConnection dbConn) {
        String promptConVarsMapKey = getPromptConVarsMapKey(dbConn, dbConn.getAdditionalParams());
        if (promptContextVars.containsKey(promptConVarsMapKey)) {
            dbConn.setAdditionalParams(promptContextVars.get(promptConVarsMapKey));
        }
    }

    /**
     * get Url of connection. when the connection is MDM connection, return its pathname, when the connection is file
     * connection, return its filepath.
     *
     * @param conn
     * @return url string of the connection or null
     */
    public static String getURL(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        String url = null;
        if (dbConn != null) {
            url = dbConn.getURL();
            if (conn.isContextMode()) {
                IRepositoryContextService repositoryContextService = CoreRuntimePlugin.getInstance()
                        .getRepositoryContextService();
                if (repositoryContextService != null) {
                    // get the original value and select the defalut context
                    String contextName = conn.getContextName();
                    DatabaseConnection origValueConn = repositoryContextService.cloneOriginalValueConnection(dbConn,
                            contextName == null ? true : false, contextName);
                    url = DatabaseConnStrUtil.getURLString(origValueConn);
                }
            }
            return url;
        }
        // MOD qiongli 2011-1-11 feature 16796.
        DelimitedFileConnection dfConnection = SwitchHelpers.DELIMITEDFILECONNECTION_SWITCH.doSwitch(conn);
        if (dfConnection != null) {
            return getOriginalValueConnection(dfConnection).getFilePath();
        }
        return null;
    }

    /**
     *
     * get driver jar path only for GENERAL JDBC type.
     *
     * @param conn
     * @return
     */
    public static String getDriverJarPath(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        String driveJarPath = null;
        if (dbConn != null) {
            driveJarPath = getOriginalValueConnection(dbConn).getDriverJarPath();
        }
        return driveJarPath;
    }

    /**
     * Just for hive pre-setup, some configurations are required to set up to the properties of system. It is just for
     * Hive embedded mode.Added by Marvin Wang on Nov 22, 2012.(Just a reminder: TDQ-6462)
     *
     * @param conn
     */
    public static void doHivePreSetup(Connection connection) {
        Connection conn = connection;
        if (conn instanceof DatabaseConnection) {
            // put to diffirent folder in case it will conflict when create connection with diffirent distribution
            String id = connection.getId();
            if (id == null) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                id = factory.getNextId();
            }
            String fullPathTemp;
            if (Platform.isRunning()) {
                IProject project = ProjectManager.getInstance().getResourceProject(
                        ProjectManager.getInstance().getCurrentProject().getEmfProject());
                fullPathTemp = project.getFolder("temp").getLocation().append("metastore_db").append(id).toPortableString(); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                fullPathTemp = new Path(System.getProperty("java.io.tmpdir")).append("metastore_db").append(id).toPortableString();//$NON-NLS-1$ //$NON-NLS-2$
            }

            System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_URL.getKey(), "jdbc:derby:;databaseName=" //$NON-NLS-1$
                    + fullPathTemp + ";create=true"); //$NON-NLS-1$

            DatabaseConnection dbConn = (DatabaseConnection) conn;

            // for dq, need to set this as the user to run mapreduce job when run analysis.
            String userName = dbConn.getUsername();
            if (StringUtils.isNotBlank(userName)) {
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HADOOP_USER_NAME.getKey(), dbConn.getUsername());
            }
            // TODO with thrift way, we must enable the two parameters below whereas in JDBC way, we don't need it.
            // If metastore is local or not.
            System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_LOCAL.getKey(), "false"); //$NON-NLS-1$

            // for dq if connection is not converted
            if (conn.isContextMode()) {
                IRepositoryContextService repositoryContextService = CoreRuntimePlugin.getInstance()
                        .getRepositoryContextService();
                if (repositoryContextService != null) {
                    // get the original value and select the defalut context
                    String contextName = conn.getContextName();
                    conn = repositoryContextService.cloneOriginalValueConnection(dbConn, contextName == null ? true : false,
                            contextName);
                }
            }

            // metastore uris
            String thriftURL = "thrift://" + dbConn.getServerName() + ":" + dbConn.getPort(); //$NON-NLS-1$//$NON-NLS-2$
            System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_URI.getKey(), thriftURL);
            System.setProperty("hive.metastore.warehouse.dir", "/user/hive/warehouse"); //$NON-NLS-1$ //$NON-NLS-2$
            // ugi
            System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_EXECUTE_SETUGI.getKey(), "true"); //$NON-NLS-1$

            // hdfs
            System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_FS_DEFAULT_NAME.getKey(),
                    dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_NAME_NODE_URL));

            boolean useYarn = Boolean.valueOf(dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_USE_YARN));
            if (useYarn) { // yarn
                //                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_MAPREDUCE_FRAMEWORK_NAME.getKey(), "yarn"); //$NON-NLS-1$
                // System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_YARN_RESOURCEMANAGER_ADDRESS.getKey(), dbConn
                // .getParameters().get(ConnParameterKeys.CONN_PARA_KEY_JOB_TRACKER_URL));
            } else { // job tracker
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_MAPRED_JOB_TRACKER.getKey(),
                        dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_JOB_TRACKER_URL));
            }

            boolean useKerberos = Boolean.valueOf(dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_USE_KRB));
            if (useKerberos) {
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_SASL_ENABLED.getKey(), "true"); //$NON-NLS-1$
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_DRIVERNAME.getKey(), dbConn.getParameters()
                        .get(ConnParameterKeys.HIVE_AUTHENTICATION_DRIVERCLASS));
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_SECURITY_AUTHORIZATION_ENABLED.getKey(), "true"); //$NON-NLS-1$
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_URL.getKey(),
                        dbConn.getParameters().get(ConnParameterKeys.HIVE_AUTHENTICATION_METASTOREURL));
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_USERNAME.getKey(), dbConn.getParameters()
                        .get(ConnParameterKeys.HIVE_AUTHENTICATION_USERNAME));
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_PASSWORD.getKey(), dbConn.getParameters()
                        .get(ConnParameterKeys.HIVE_AUTHENTICATION_PASSWORD));
                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_KERBEROS_PRINCIPAL.getKey(), dbConn
                        .getParameters().get(ConnParameterKeys.HIVE_AUTHENTICATION_HIVEPRINCIPLA));
            }

            // hive mode for talend
            // String hiveMode = dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_HIVE_MODE);
            // if (HiveConnVersionInfo.MODE_EMBEDDED.getKey().equals(hiveMode)) {
            //                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_TALEND_HIVE_MODE.getKey(), "true"); //$NON-NLS-1$
            // } else {
            //                System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_TALEND_HIVE_MODE.getKey(), "false"); //$NON-NLS-1$
            // }
            // For metastore infos.
            // url
            // System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_URL.getKey(),
            // dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_METASTORE_CONN_URL));
            // // user name
            // System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_USERNAME.getKey(),
            // dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_METASTORE_CONN_USERNAME));
            // // password
            // System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_PASSWORD.getKey(),
            // dbConn.getParameters().get(ConnParameterKeys.CONN_PARA_KEY_METASTORE_CONN_PASSWORD));
            // // driver name
            // System.setProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_DRIVERNAME.getKey(),
            // dbConn.getParameters()
            // .get(ConnParameterKeys.CONN_PARA_KEY_METASTORE_CONN_DRIVER_NAME));
        }
    }

    /**
     * For these which are pre-set up, we need to clear these. Added by Marvin Wang on Nov 22, 2012.
     */
    public static void doHiveConfigurationClear() {
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_LOCAL.getKey());
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_URI.getKey());
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HIVE_METASTORE_EXECUTE_SETUGI.getKey());
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_FS_DEFAULT_NAME.getKey());
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_MAPRED_JOB_TRACKER.getKey());
        System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_HADOOP_USER_NAME.getKey());
        // System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_TALEND_HIVE_MODE.getKey());
        // System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_URL.getKey());
        // System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_USERNAME.getKey());
        // System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_PASSWORD.getKey());
        // System.clearProperty(HiveConfKeysForTalend.HIVE_CONF_KEY_JDO_CONNECTION_DRIVERNAME.getKey());
    }

    /**
     * get Server Name from connection.
     *
     * @param conn
     * @return server name of the connection or null
     */
    public static String getServerName(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            return getOriginalValueConnection(dbConn).getServerName();
        }
        return null;
    }

    /**
     * DOC xqliu Comment method "setDriverClass".
     *
     * @param conn
     * @param driverClass
     */
    public static void setDriverClass(Connection conn, String driverClass) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            dbConn.setDriverClass(driverClass);
        }
    }

    /**
     * DOC xqliu Comment method "setServerName".
     *
     * @param conn
     * @param serverName
     */
    public static void setServerName(Connection conn, String serverName) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            dbConn.setServerName(serverName);
        }
    }

    /**
     * get Port from connection.
     *
     * @param conn
     * @return port of the connection or null
     */
    public static String getPort(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            return getOriginalValueConnection(dbConn).getPort();
        }
        return null;
    }

    /**
     * DOC xqliu Comment method "setPort".
     *
     * @param conn
     * @param port
     */
    public static void setPort(Connection conn, String port) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            dbConn.setPort(port);
        }
    }

    /**
     * DOC xqliu Comment method "getSID".
     *
     * @param conn
     * @return sid of the connection or null
     */
    public static String getSID(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            return getOriginalValueConnection(dbConn).getSID();
        }
        return null;
    }

    /**
     * set SID for connection.
     *
     * @param conn
     * @param sid
     */
    public static void setSID(Connection conn, String sid) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        if (dbConn != null) {
            dbConn.setSID(sid);
        }
    }

    /**
     * get dbmsid from connection.
     *
     * @param conn
     * @return sid of the connection or null
     */
    public static String getDbmsId(Connection conn) {
        DatabaseConnection dbConn = SwitchHelpers.DATABASECONNECTION_SWITCH.doSwitch(conn);
        String dbmsid = null;
        if (dbConn != null) {
            dbmsid = getOriginalValueConnection(dbConn).getDbmsId();
        }
        return dbmsid;
    }

    /**
     * get Original Value Connection.
     *
     * @param fileconnection
     * @param headValue
     * @return
     */
    private static DatabaseConnection getOriginalValueConnection(DatabaseConnection conn) {
        if (conn.isContextMode()) {
            IRepositoryContextService repositoryContextService = CoreRuntimePlugin.getInstance().getRepositoryContextService();
            if (repositoryContextService != null) {
                // get the original value and select the defalut context
                String contextName = conn.getContextName();
                return repositoryContextService.cloneOriginalValueConnection(conn, contextName == null ? true : false,
                        contextName);
            }
        }
        return conn;
    }

    /**
     * get Original Value Connection.
     *
     * @param fileconnection
     * @param headValue
     * @return
     */
    private static DelimitedFileConnection getOriginalValueConnection(DelimitedFileConnection fileconnection) {
        if (fileconnection.isContextMode()) {
            IRepositoryContextService repositoryContextService = CoreRuntimePlugin.getInstance().getRepositoryContextService();
            if (repositoryContextService != null) {
                // get the original value Connection
                return (DelimitedFileConnection) repositoryContextService.cloneOriginalValueConnection(fileconnection);
            }
        }
        return fileconnection;
    }

    /**
     * get HeadValue for fileconnection.
     *
     * @param fileconnection
     * @return
     */
    public static int getHeadValue(DelimitedFileConnection fileconnection) {
        String headValue = null;
        if (fileconnection != null) {
            headValue = getOriginalValueConnection(fileconnection).getHeaderValue();
        }
        return Integer.parseInt(headValue == null || PluginConstant.EMPTY_STRING.equals(headValue) ? "0" : headValue); //$NON-NLS-1$
    }

    /**
     * get FooterValue for fileconnection.
     *
     * @param fileconnection
     * @return
     */
    public static int getFooterValue(DelimitedFileConnection fileconnection) {
        String footerValue = null;
        if (fileconnection != null) {
            footerValue = getOriginalValueConnection(fileconnection).getFooterValue();
        }
        return Integer.parseInt(footerValue == null || PluginConstant.EMPTY_STRING.equals(footerValue) ? "0" : footerValue); //$NON-NLS-1$

    }

    /**
     * get LimitValue for fileconnection.
     *
     * @param delimitedFileconnection
     * @return
     */
    public static int getLimitValue(DelimitedFileConnection fileconnection) {
        String limitValue = null;
        if (fileconnection != null) {
            limitValue = getOriginalValueConnection(fileconnection).getLimitValue();
        }
        return Integer
                .parseInt(limitValue == null || PluginConstant.EMPTY_STRING.equals(limitValue) || "0".equals(limitValue) ? "-1" : limitValue); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * get FieldSeparator Value for fileconnection.
     *
     * @param fileconnection
     * @return
     */
    public static String getFieldSeparatorValue(DelimitedFileConnection fileconnection) {
        String separator = null;
        if (fileconnection != null) {
            separator = getOriginalValueConnection(fileconnection).getFieldSeparatorValue();
        }
        return separator;
    }

    /**
     * get Encoding for fileconnection.
     *
     * @param fileconnection
     * @return
     */
    public static String getEncoding(DelimitedFileConnection fileconnection) {
        String encoding = null;
        if (fileconnection != null) {
            encoding = getOriginalValueConnection(fileconnection).getEncoding();
        }
        return encoding;
    }

    /**
     * get RowSeparator Value for fileconnection.
     *
     * @param fileconnection
     * @return
     */
    public static String getRowSeparatorValue(DelimitedFileConnection fileconnection) {
        String rowSeparatorValue = null;
        if (fileconnection != null) {
            rowSeparatorValue = getOriginalValueConnection(fileconnection).getRowSeparatorValue();
        }
        return rowSeparatorValue;
    }

}
