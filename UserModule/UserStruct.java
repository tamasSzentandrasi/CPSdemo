

/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package UserModule;

import com.rti.dds.infrastructure.*;
import com.rti.dds.infrastructure.Copyable;
import java.io.Serializable;
import com.rti.dds.cdr.CdrHelper;

public class UserStruct   implements Copyable, Serializable{

    public UserModule.RoleType [] role= null;
    public double distance= 0;
    public UserModule.CableType transmissionType = (UserModule.CableType)UserModule.CableType.create();
    public int switchRef= 0;

    public UserStruct() {

    }
    public UserStruct (UserStruct other) {

        this();
        copy_from(other);
    }

    public static Object create() {

        UserStruct self;
        self = new  UserStruct();
        self.clear();
        return self;

    }

    public void clear() {

        role=null; 
        distance= 0;
        transmissionType = UserModule.CableType.create();
        switchRef= 0;
    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }        

        if(getClass() != o.getClass()) {
            return false;
        }

        UserStruct otherObj = (UserStruct)o;

        if ((role == null && otherObj.role != null) ||
        (role != null && otherObj.role == null)) {
            return false;
        }
        if (role != null) {
            for(int i1__ = 0; i1__< 2; ++i1__){

                if(!role[i1__].equals(otherObj.role[i1__])) {
                    return false;
                }
            }

        }
        if(distance != otherObj.distance) {
            return false;
        }
        if(!transmissionType.equals(otherObj.transmissionType)) {
            return false;
        }
        if(switchRef != otherObj.switchRef) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int __result = 0;
        if (role != null) {
            for(int i1__ = 0; i1__< 2; ++i1__){

                __result += role[i1__].hashCode(); 
            }

        }
        __result += (int)distance;
        __result += transmissionType.hashCode(); 
        __result += (int)switchRef;
        return __result;
    }

    /**
    * This is the implementation of the <code>Copyable</code> interface.
    * This method will perform a deep copy of <code>src</code>
    * This method could be placed into <code>UserStructTypeSupport</code>
    * rather than here by using the <code>-noCopyable</code> option
    * to rtiddsgen.
    * 
    * @param src The Object which contains the data to be copied.
    * @return Returns <code>this</code>.
    * @exception NullPointerException If <code>src</code> is null.
    * @exception ClassCastException If <code>src</code> is not the 
    * same type as <code>this</code>.
    * @see com.rti.dds.infrastructure.Copyable#copy_from(java.lang.Object)
    */
    public Object copy_from(Object src) {

        UserStruct typedSrc = (UserStruct) src;
        UserStruct typedDst = this;

        if (typedDst.role == null && typedSrc.role !=null){
            typedDst.role = 
            new UserModule.RoleType [2];
            for(int i1__ = 0; i1__< 2; ++i1__){

                typedDst.role[i1__] = 
                (UserModule.RoleType) UserModule.RoleType.create();    
            }

        }
        if(typedSrc.role !=null){
            for(int i1__ = 0; i1__< 2; ++i1__){

                typedDst.role[i1__] = (UserModule.RoleType) typedDst.role[i1__].copy_from(typedSrc.role[i1__]);
            }

        } else{
            typedDst.role=null;
        }
        typedDst.distance = typedSrc.distance;
        typedDst.transmissionType = (UserModule.CableType) typedDst.transmissionType.copy_from(typedSrc.transmissionType);
        typedDst.switchRef = typedSrc.switchRef;

        return this;
    }

    public String toString(){
        return toString("", 0);
    }

    public String toString(String desc, int indent) {
        StringBuffer strBuffer = new StringBuffer();        

        if (desc != null) {
            CdrHelper.printIndent(strBuffer, indent);
            strBuffer.append(desc).append(":\n");
        }

        if (role != null) {
            CdrHelper.printIndent(strBuffer, indent+1);
            strBuffer.append("role:\n");
            for(int i1__ = 0; i1__< 2; ++i1__){

                strBuffer.append(role[i1__].toString(
                    "["+Integer.toString(i1__)+"]",indent+2));
            }

        } else {
            CdrHelper.printIndent(strBuffer, indent+1);
            strBuffer.append("role: null\n");
        }
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("distance: ").append(distance).append("\n");  
        strBuffer.append(transmissionType.toString("transmissionType ", indent+1));
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("switchRef: ").append(switchRef).append("\n");  

        return strBuffer.toString();
    }

}
