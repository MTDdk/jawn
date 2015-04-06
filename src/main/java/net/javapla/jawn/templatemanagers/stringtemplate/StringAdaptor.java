package net.javapla.jawn.templatemanagers.stringtemplate;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

class StringAdaptor implements ModelAdaptor {

    @Override
    public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName) throws STNoSuchPropertyException {
        
        switch (propertyName) {
        case "asimage":
            return String.format("%s/%s/%s","static","images", o.toString());
        }
        
        throw new STNoSuchPropertyException(null, property, propertyName);
    }

}
