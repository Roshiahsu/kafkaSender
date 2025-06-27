package com.example.KafkaSender;

import com.example.KafkaSender.common.KafkaSenderConstants;
import com.example.KafkaSender.handler.IMQHandler;
import com.example.KafkaSender.handler.OrderRepullHandler;
import com.example.KafkaSender.handler.ProductRepullHandler;
import com.example.KafkaSender.handler.UpdateProductListHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KafkaSenderFactory {

    @Autowired
    private OrderRepullHandler orderRepullHandler;
    @Autowired
    private ProductRepullHandler productRepullHandler;
    @Autowired
    private UpdateProductListHandler updateProductListHandler;

    public IMQHandler getKafkaSender(String repullType) {
        switch (repullType) {
            case KafkaSenderConstants.ORDER_REPULL:
                return orderRepullHandler;
            case KafkaSenderConstants.PRODUCT_REPULL:
                return productRepullHandler;
            case KafkaSenderConstants.ITEM_LIST_UPDATE:
//                return updateProductListHandler;
                //功能還不能用
                throw new UnsupportedOperationException();
        }
        return null;
    }
}
