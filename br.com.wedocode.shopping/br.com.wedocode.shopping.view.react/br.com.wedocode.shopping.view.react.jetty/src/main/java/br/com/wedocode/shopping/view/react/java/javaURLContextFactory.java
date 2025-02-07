package br.com.wedocode.shopping.view.react.java;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import org.apache.commons.lang3.StringUtils;

import br.com.wedocode.framework.commons.util.Rethrow;

public class javaURLContextFactory implements ObjectFactory {

    public static Context defaultContext;

    public static void bind(String path, String name, Object obj) {
        try {
            var ctx = defaultContext;
            for (var dirName : StringUtils.split("java:" + path, '/')) {
                try {
                    ctx = (Context) ctx.lookup(dirName);
                } catch (NamingException e) {
                    ctx = ctx.createSubcontext(dirName);

                }
            }

            ctx.bind(name, obj);
        } catch (NamingException e1) {
            Rethrow.emit(e1);
        }
    }

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
            throws Exception {
        if (name == null) {
            return defaultContext;
        }
        // TODO Auto-generated method stub
        return null;
    }

}
