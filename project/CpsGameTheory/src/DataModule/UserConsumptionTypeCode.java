
/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package DataModule;

import com.rti.dds.typecode.*;

public class  UserConsumptionTypeCode {
    public static final TypeCode VALUE = getTypeCode();

    private static TypeCode getTypeCode() {
        TypeCode tc = null;
        int __i=0;
        StructMember sm[]=new StructMember[3];

        sm[__i]=new  StructMember("userid", false, (short)-1,  false,(TypeCode) TypeCode.TC_LONGLONG,206802922 , false);__i++;
        sm[__i]=new  StructMember("consumptions", false, (short)-1,  false,(TypeCode) new TypeCode(new int[] {7}, TypeCode.TC_DOUBLE),171558363 , false);__i++;
        sm[__i]=new  StructMember("productions", false, (short)-1,  false,(TypeCode) new TypeCode(new int[] {7}, TypeCode.TC_DOUBLE),131130604 , false);__i++;

        tc = TypeCodeFactory.TheTypeCodeFactory.create_struct_tc("DataModule::UserConsumption",ExtensibilityKind.MUTABLE_EXTENSIBILITY,  sm);        
        return tc;
    }
}

