package com.example.KafkaSender;

import com.example.KafkaSender.handler.IMQHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class KafkaSenderFactory implements ApplicationContextAware {

    private final Map<String, IMQHandler> handlers = new HashMap<>();

    /**
     * 實現動態注入
     *
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IMQHandler> handlerBeans = applicationContext.getBeansOfType(IMQHandler.class);
        for (IMQHandler handler : handlerBeans.values()) {
            String repullType = handler.getSupportedRepullType();
            if (repullType != null) {
                handlers.put(repullType, handler);
            }
        }
    }

    public IMQHandler getKafkaSender(String repullType) {
        if (repullType == null) {
            throw new IllegalArgumentException("repullType cannot be null");
        }
        IMQHandler handler = handlers.get(repullType);
        if (handler == null) {
            throw new UnsupportedOperationException("Invalid repullType: " + repullType);
        }
        return handler;
    }

}
