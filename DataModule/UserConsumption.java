

/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package DataModule;

import com.rti.dds.infrastructure.*;
import com.rti.dds.infrastructure.Copyable;
import java.io.Serializable;
import com.rti.dds.cdr.CdrHelper;

public class UserConsumption   implements Copyable, Serializable{

    public double [] consumptions=  new double [7];
    public double [] productions=  new double [7];

    public UserConsumption() {

    }
    public UserConsumption (UserConsumption other) {

        this();
        copy_from(other);
    }

    public static Object create() {

        UserConsumption self;
        self = new  UserConsumption();
        self.clear();
        return self;

    }

    public void clear() {

        for(int i1__ = 0; i1__< 7; ++i1__){

            consumptions[i1__] =  0;
        }

        for(int i1__ = 0; i1__< 7; ++i1__){

            productions[i1__] =  0;
        }

    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }        

        if(getClass() != o.getClass()) {
            return false;
        }

        UserConsumption otherObj = (UserConsumption)o;

        for(int i1__ = 0; i1__< 7; ++i1__){

            if(consumptions[i1__] != otherObj.consumptions[i1__]) {
                return false;
            }
        }

        for(int i1__ = 0; i1__< 7; ++i1__){

            if(productions[i1__] != otherObj.productions[i1__]) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        int __result = 0;
        for(int i1__ = 0; i1__< 7; ++i1__){

            __result += (int)consumptions[i1__];
        }

        for(int i1__ = 0; i1__< 7; ++i1__){

            __result += (int)productions[i1__];
        }

        return __result;
    }

    /**
    * This is the implementation of the <code>Copyable</code> interface.
    * This method will perform a deep copy of <code>src</code>
    * This method could be placed into <code>UserConsumptionTypeSupport</code>
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

        UserConsumption typedSrc = (UserConsumption) src;
        UserConsumption typedDst = this;

        System.arraycopy(typedSrc.consumptions,0,
        typedDst.consumptions,0,
        typedSrc.consumptions.length); 

        System.arraycopy(typedSrc.productions,0,
        typedDst.productions,0,
        typedSrc.productions.length); 

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

        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("consumptions: ");
        for(int i1__ = 0; i1__< 7; ++i1__){

            strBuffer.append(consumptions[i1__]).append(", ");
        }

        strBuffer.append("\n");
        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("productions: ");
        for(int i1__ = 0; i1__< 7; ++i1__){

            strBuffer.append(productions[i1__]).append(", ");
        }

        strBuffer.append("\n");

        return strBuffer.toString();
    }

}
