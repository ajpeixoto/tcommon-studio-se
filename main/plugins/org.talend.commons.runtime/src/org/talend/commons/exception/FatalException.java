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
package org.talend.commons.exception;

/**
 * Defines fatal exception - Use or extends this class when an error occurs application can't keep running anymore.<br/>
 *
 * $Id$
 *
 */
public class FatalException extends RuntimeException {

    @SuppressWarnings("unused")//$NON-NLS-1$
    private static final long serialVersionUID = 1L;

    public static final int CODE_INCOMPATIBLE_UPDATE = 10;

    private int code;

    public FatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public FatalException(int code, String message) {
        super(message);
        this.code = code;
    }

    public FatalException(String message) {
        super(message);
    }

    public FatalException(Throwable cause) {
        super(cause);
    }

    public int getCode() {
        return code;
    }

}
