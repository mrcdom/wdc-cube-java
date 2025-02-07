package br.com.wedocode.shopping.presentation;

import java.util.HashMap;
import java.util.Map;

import br.com.wedocode.framework.commons.concurrent.ScheduledExecutor;
import br.com.wedocode.framework.commons.function.ThrowingBiFunction;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.webflow.WebFlowIntent;
import br.com.wedocode.shopping.presentation.shared.business.ShoppingDAO;

public class ShoppingContext {

    private static final Map<String, ThrowingBiFunction<ShoppingApplication, WebFlowIntent, Promise<Boolean>>> goActionMap = new HashMap<>();

    private static ShoppingDAO dao;

    private static ScheduledExecutor executor;

    public static final ShoppingDAO getDAO() {
        return dao;
    }

    public static final ScheduledExecutor getExecutor() {
        return executor;
    }

    public static void registerPlace(String tag,
            ThrowingBiFunction<ShoppingApplication, WebFlowIntent, Promise<Boolean>> goAction) {
        goActionMap.put(tag, goAction);
    }

    public static class Internals {

        static Promise<Boolean> go(ShoppingApplication app, WebFlowIntent place) throws Exception {
            var goAction = goActionMap.get(place.getPlace().getName());
            if (goAction != null) {
                return goAction.apply(app, place);
            }
            return null;
        }

        public static void setDAO(ShoppingDAO dao) {
            ShoppingContext.dao = dao;
        }

        public static void setExecutor(ScheduledExecutor executor) {
            ShoppingContext.executor = executor;
        }

    }

}
