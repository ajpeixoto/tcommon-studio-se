/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package org.talend.designer.business.model.business.provider;

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;
import org.talend.designer.business.model.business.BusinessFactory;
import org.talend.designer.business.model.business.BusinessPackage;
import org.talend.designer.business.model.business.Repository;

/**
 * This is the item provider adapter for a {@link org.talend.designer.business.model.business.Repository} object. <!--
 * begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public class RepositoryItemProvider extends ItemProviderAdapter implements IEditingDomainItemProvider,
        IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public static final String copyright = "";

    /**
     * This constructs an instance from a factory and a notifier.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public RepositoryItemProvider(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    /**
     * This returns the property descriptors for the adapted class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public List getPropertyDescriptors(Object object) {
        if (itemPropertyDescriptors == null) {
            super.getPropertyDescriptors(object);

        }
        return itemPropertyDescriptors;
    }

    /**
     * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
     * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
     * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
     * <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * @generated
     */
    public Collection getChildrenFeatures(Object object) {
        if (childrenFeatures == null) {
            super.getChildrenFeatures(object);
            childrenFeatures.add(BusinessPackage.Literals.REPOSITORY__TALENDITEMS);
        }
        return childrenFeatures;
    }

    /**
     * This returns Repository.gif.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public Object getImage(Object object) {
        return overlayImage(object, getResourceLocator().getImage("full/obj16/Repository"));
    }

    /**
     * This returns the label text for the adapted class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public String getText(Object object) {
        return getString("_UI_Repository_type");
    }

    /**
     * This handles model notifications by calling {@link #updateChildren} to update any cached children and by creating
     * a viewer notification, which it passes to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated
     */
    public void notifyChanged(Notification notification) {
        updateChildren(notification);

        switch (notification.getFeatureID(Repository.class)) {
            case BusinessPackage.REPOSITORY__TALENDITEMS:
                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
                return;
        }
        super.notifyChanged(notification);
    }

    /**
     * This adds to the collection of {@link org.eclipse.emf.edit.command.CommandParameter}s
     * describing all of the children that can be created under this object.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected void collectNewChildDescriptors(Collection newChildDescriptors, Object object) {
        super.collectNewChildDescriptors(newChildDescriptors, object);

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createBusinessProcess()));

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createProcess()));

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createRoutine()));

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createDocumentation()));

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createDatabaseMetadata()));

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createTableMetadata()));

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createFileDelimitedMetadata()));

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createFilePositionalMetadata()));

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createFileRegexpMetadata()));

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createFileXmlMetadata()));

        newChildDescriptors.add
            (createChildParameter
                (BusinessPackage.Literals.REPOSITORY__TALENDITEMS,
                 BusinessFactory.eINSTANCE.createFileLdifMetadata()));
    }

    /**
     * Return the resource locator for this item provider's resources.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public ResourceLocator getResourceLocator() {
        return BusinessEditPlugin.INSTANCE;
    }

}
