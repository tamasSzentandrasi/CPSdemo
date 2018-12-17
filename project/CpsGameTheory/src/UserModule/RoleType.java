

/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package UserModule;

import com.rti.dds.util.Enum;
import com.rti.dds.cdr.CdrHelper;
import java.util.Arrays;
import java.io.ObjectStreamException;

public class RoleType  extends Enum {

    public static final RoleType CRITICAL_CONSUMER = new RoleType("CRITICAL_CONSUMER", 1);
    public static final int _CRITICAL_CONSUMER = 1;
    public static final RoleType CONSUMER = new RoleType("CONSUMER", 2);
    public static final int _CONSUMER = 2;
    public static final RoleType PROVIDER = new RoleType("PROVIDER", 3);
    public static final int _PROVIDER = 3;
    public static final RoleType SWITCH = new RoleType("SWITCH", 0);
    public static final int _SWITCH = 0;
    public static RoleType valueOf(int ordinal) {
        switch(ordinal) {

            case 1: return RoleType.CRITICAL_CONSUMER;
            case 2: return RoleType.CONSUMER;
            case 3: return RoleType.PROVIDER;
            case 0: return RoleType.SWITCH;

        }
        return null;
    }

    public static RoleType from_int(int __value) {
        return valueOf(__value);
    }

    public static int[] getOrdinals() {
        int i = 0;
        int[] values = new int[4];

        values[i] = CRITICAL_CONSUMER.ordinal();
        i++;
        values[i] = CONSUMER.ordinal();
        i++;
        values[i] = PROVIDER.ordinal();
        i++;
        values[i] = SWITCH.ordinal();
        i++;

        return values;
    }

    public int value() {
        return super.ordinal();
    }

    /**
    * Create a default instance
    */  
    public static RoleType create() {

        return valueOf(1);
    }

    /**
    * Print Method
    */     
    public String toString(String desc, int indent) {
        StringBuffer strBuffer = new StringBuffer();

        CdrHelper.printIndent(strBuffer, indent);

        if (desc != null) {
            strBuffer.append(desc).append(": ");
        }

        strBuffer.append(this);
        strBuffer.append("\n");              
        return strBuffer.toString();
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(ordinal());
    }

    private RoleType(String name, int ordinal) {
        super(name, ordinal);
    }
}

