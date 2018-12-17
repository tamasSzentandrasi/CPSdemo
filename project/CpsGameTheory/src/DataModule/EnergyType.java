

/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package DataModule;

import com.rti.dds.util.Enum;
import com.rti.dds.cdr.CdrHelper;
import java.util.Arrays;
import java.io.ObjectStreamException;

public class EnergyType  extends Enum {

    public static final EnergyType SONAR = new EnergyType("SONAR", 0);
    public static final int _SONAR = 0;
    public static final EnergyType WIND = new EnergyType("WIND", 1);
    public static final int _WIND = 1;
    public static final EnergyType HYDRO = new EnergyType("HYDRO", 2);
    public static final int _HYDRO = 2;
    public static final EnergyType BIOMASS = new EnergyType("BIOMASS", 3);
    public static final int _BIOMASS = 3;
    public static final EnergyType NUCLEAR = new EnergyType("NUCLEAR", 4);
    public static final int _NUCLEAR = 4;
    public static final EnergyType COAL = new EnergyType("COAL", 5);
    public static final int _COAL = 5;
    public static final EnergyType NATURAL_GAS = new EnergyType("NATURAL_GAS", 6);
    public static final int _NATURAL_GAS = 6;
    public static EnergyType valueOf(int ordinal) {
        switch(ordinal) {

            case 0: return EnergyType.SONAR;
            case 1: return EnergyType.WIND;
            case 2: return EnergyType.HYDRO;
            case 3: return EnergyType.BIOMASS;
            case 4: return EnergyType.NUCLEAR;
            case 5: return EnergyType.COAL;
            case 6: return EnergyType.NATURAL_GAS;

        }
        return null;
    }

    public static EnergyType from_int(int __value) {
        return valueOf(__value);
    }

    public static int[] getOrdinals() {
        int i = 0;
        int[] values = new int[7];

        values[i] = SONAR.ordinal();
        i++;
        values[i] = WIND.ordinal();
        i++;
        values[i] = HYDRO.ordinal();
        i++;
        values[i] = BIOMASS.ordinal();
        i++;
        values[i] = NUCLEAR.ordinal();
        i++;
        values[i] = COAL.ordinal();
        i++;
        values[i] = NATURAL_GAS.ordinal();
        i++;

        return values;
    }

    public int value() {
        return super.ordinal();
    }

    /**
    * Create a default instance
    */  
    public static EnergyType create() {

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

    private EnergyType(String name, int ordinal) {
        super(name, ordinal);
    }
}

