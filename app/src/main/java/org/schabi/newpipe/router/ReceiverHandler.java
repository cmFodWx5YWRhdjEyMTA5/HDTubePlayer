package org.schabi.newpipe.router;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * Created by liyanju on 16/9/22.
 */
public class ReceiverHandler implements InvocationHandler {

    private Class mReceiverType;

    private Set<Object> mAllReceivers;

    Object mReceiverProxy;

    public ReceiverHandler(Class receiverType, Set<Object> allReceivers) {
        mReceiverType = receiverType;
        mAllReceivers = allReceivers;
        mReceiverProxy = Proxy.newProxyInstance(mReceiverType.getClassLoader(), new Class[]{mReceiverType}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        for (Object receiver : mAllReceivers) {
             if (mReceiverType.isInstance(receiver)) {
                 method = receiver.getClass().getMethod(method.getName(), method.getParameterTypes());
                 return method.invoke(receiver, args);
             }
        }
        return null;
    }
}
