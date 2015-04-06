package net.javapla.jawn.templatemanagers.stringtemplate;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

class AnchorAdaptor implements ModelAdaptor {

    @Override
    public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName) throws STNoSuchPropertyException {
        
        System.err.println(String.format("%s, %s, %s", o, property, propertyName));
        
        switch (propertyName) {
        case "asimage":
            return String.format("%s/%s/%s","static","images", o.toString());
        }
        
        throw new STNoSuchPropertyException(null, property, propertyName);
    }

}
