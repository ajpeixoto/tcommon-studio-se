/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.cwm.relational;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.talend.cwm.relational.RelationalPackage
 * @generated
 */
public interface RelationalFactory extends EFactory {
    /**
     * The singleton instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    RelationalFactory eINSTANCE = org.talend.cwm.relational.impl.RelationalFactoryImpl.init();

    /**
     * Returns a new object of class '<em>Td Table</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Td Table</em>'.
     * @generated
     */
    TdTable createTdTable();

    /**
     * Returns a new object of class '<em>Td View</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Td View</em>'.
     * @generated
     */
    TdView createTdView();

    /**
     * Returns a new object of class '<em>Td Catalog</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Td Catalog</em>'.
     * @generated
     */
    TdCatalog createTdCatalog();

    /**
     * Returns a new object of class '<em>Td Schema</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Td Schema</em>'.
     * @generated
     */
    TdSchema createTdSchema();

    /**
     * Returns a new object of class '<em>Td Column</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Td Column</em>'.
     * @generated
     */
    TdColumn createTdColumn();

    /**
     * Returns a new object of class '<em>Td Sql Data Type</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Td Sql Data Type</em>'.
     * @generated
     */
    TdSqlDataType createTdSqlDataType();

    /**
     * Returns the package supported by this factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the package supported by this factory.
     * @generated
     */
    RelationalPackage getRelationalPackage();

} //RelationalFactory
