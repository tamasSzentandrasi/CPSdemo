

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

public class CableType  extends Enum {

    public static final CableType MICROVAWE = new CableType("MICROVAWE", 0);
    public static final int _MICROVAWE = 0;
    public static final CableType POWERLINE = new CableType("POWERLINE", 1);
    public static final int _POWERLINE = 1;
    public static CableType valueOf(int ordinal) {
        switch(ordinal) {

            case 0: return CableType.MICROVAWE;
            case 1: return CableType.POWERLINE;

        }
        return null;
    }

    public static CableType from_int(int __value) {
        return valueOf(__value);
    }

    public static int[] getOrdinals() {
        int i = 0;
        int[] values = new int[2];

        values[i] = MICROVAWE.ordinal();
        i++;
        values[i] = POWERLINE.ordinal();
        i++;

        return values;
    }

    public int value() {
        return super.ordinal();
    }

    /**
    * Create a default instance
    */  
    public static CableType create() {

        return valueOf(0);
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

    private CableType(String name, int ordinal) {
        super(name, ordinal);
    }
}

