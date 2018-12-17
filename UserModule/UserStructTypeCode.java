
/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package UserModule;

import com.rti.dds.typecode.*;

public class  UserStructTypeCode {
    public static final TypeCode VALUE = getTypeCode();

    private static TypeCode getTypeCode() {
        TypeCode tc = null;
        int __i=0;
        StructMember sm[]=new StructMember[4];

        sm[__i]=new  StructMember("role", false, (short)-1,  false,(TypeCode) new TypeCode(new int[] {2}, UserModule.RoleTypeTypeCode.VALUE),82421545 ,true);__i++;
        sm[__i]=new  StructMember("distance", false, (short)-1,  false,(TypeCode) TypeCode.TC_DOUBLE,97078951 , false);__i++;
        sm[__i]=new  StructMember("transmissionType", false, (short)-1,  false,(TypeCode) UserModule.CableTypeTypeCode.VALUE,136653229 , false);__i++;
        sm[__i]=new  StructMember("switchRef", false, (short)-1,  false,(TypeCode) TypeCode.TC_LONG,255660682 , false);__i++;

        tc = TypeCodeFactory.TheTypeCodeFactory.create_struct_tc("UserModule::UserStruct",ExtensibilityKind.MUTABLE_EXTENSIBILITY,  sm);        
        return tc;
    }
}

