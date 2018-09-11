package org.schabi.newpipe.router;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by liyanju on 16/9/22.
 */
public class Router {

    private Set<Object> mAllReceivers = new CopyOnWriteArraySet<>();

    private Map<Class<?>,ReceiverHandler> mReceiverHandlerByType = new ConcurrentHashMap<>();

    private static class InstanceHolder {
        private static Router sInstance = new Router();
    }

    public static Router getInstance() {
        return InstanceHolder.sInstance;
    }

    public void register(Object receiver) {
        mAllReceivers.add(receiver);
    }

    public <T> T getReceiver(Class<T> receiverType) throws Exception {
        if (!receiverType.isInterface()) {
            throw new Exception(String.format("receiverType must be a interface , " +
                    "%s is not a interface",receiverType.getName()));
        }

        ReceiverHandler receiverHandler = mReceiverHandlerByType.get(receiverType);

        if (receiverHandler == null) {
            receiverHandler = new ReceiverHandler(receiverType, mAllReceivers);
            mReceiverHandlerByType.put(receiverType, receiverHandler);
        }
        return (T)receiverHandler.mReceiverProxy;
    }

    public void unregister(Object receiver) {
        mAllReceivers.remove(receiver);

        Iterator iterator = mReceiverHandlerByType.keySet().iterator();
        while (iterator.hasNext()) {
            Class type = (Class) iterator.next();
            if(type.isInstance(receiver)) {
                iterator.remove();
            }
        }
    }
}


