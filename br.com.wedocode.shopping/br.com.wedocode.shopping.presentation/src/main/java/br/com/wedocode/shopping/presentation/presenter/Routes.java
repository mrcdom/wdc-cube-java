package br.com.wedocode.shopping.presentation.presenter;

import br.com.wedocode.framework.commons.function.ThrowingBiFunction;
import br.com.wedocode.framework.commons.util.Promise;
import br.com.wedocode.framework.webflow.WebFlowIntent;
import br.com.wedocode.framework.webflow.WebFlowPlace;
import br.com.wedocode.shopping.presentation.ShoppingApplication;
import br.com.wedocode.shopping.presentation.ShoppingContext;
import br.com.wedocode.shopping.presentation.presenter.nonrestricted.LoginPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.CartPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ProductPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.ReceiptPresenter;
import br.com.wedocode.shopping.presentation.presenter.restricted.RestrictedPresenter;

/**
 * This class is a singleton path factory for the application. Every time the application needs to go to a specific
 * place, this factory will provide a way to get a path that can walk through there.
 */
public final class Routes {

    /**
     * Constructor. No one must create instance of this class.
     */
    private Routes() {
    }

    private static int levelGen = 0;

    // Navigation

    public enum Place implements WebFlowPlace {
        // Level 0
        ROOT("root", levelGen++),

        // Level 1
        LOGIN("login", levelGen++, Routes::login),
        RESTRICTED("restricted", levelGen++, Routes::restricted),

        // Level 2
        CART("cart", levelGen++, Routes::cart),
        PRODUCT("product", levelGen++, Routes::product),
        RECEIPT("receipt", levelGen++, Routes::receipt);

        private final String name;

        private final Integer id;

        Place(String name, int level) {
            this.name = name;
            this.id = level;
        }

        Place(String name, int level,
                ThrowingBiFunction<ShoppingApplication, WebFlowIntent, Promise<Boolean>> goAction) {
            this.name = name;
            this.id = level;
            ShoppingContext.registerPlace(name, goAction);
        }

        @Override
        public Integer getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    /*
     * Simplified places
     */

    public static Promise<Boolean> login(ShoppingApplication app) {
        return login(app, app.newPlace());
    }

    public static Promise<Boolean> restricted(ShoppingApplication app) {
        return restricted(app, app.newPlace());
    }

    public static Promise<Boolean> cart(ShoppingApplication app) {
        return cart(app, app.newPlace());
    }

    /*
     * Not restricted places
     */

    public static Promise<Boolean> login(ShoppingApplication app, WebFlowIntent place) {
        //@formatter:off
		return app.navigate()
			.step(Place.ROOT, place, false, RootPresenter::new)
			.step(Place.LOGIN, place, true, LoginPresenter::new)
			.promise();
		//@formatter:on
    }

    /*
     * Restricted places
     */

    public static Promise<Boolean> restricted(ShoppingApplication app, WebFlowIntent place) {
        //@formatter:off
		return app.navigate()
				.step(Place.ROOT, place, false, RootPresenter::new)
				.step(Place.RESTRICTED, place, true, RestrictedPresenter::new)
				.promise();
		//@formatter:on
    }

    public static Promise<Boolean> cart(ShoppingApplication app, WebFlowIntent place) {
        //@formatter:off
		return app.navigate()
				.step(Place.ROOT, place, false, RootPresenter::new)
				.step(Place.RESTRICTED, place, false, RestrictedPresenter::new)
				.step(Place.CART, place, true, CartPresenter::new)
				.promise();
		//@formatter:on
    }

    public static Promise<Boolean> product(ShoppingApplication app, WebFlowIntent place) {
        //@formatter:off
		return app.navigate()
				.step(Place.ROOT, place, false, RootPresenter::new)
				.step(Place.RESTRICTED, place, false, RestrictedPresenter::new)
				.step(Place.PRODUCT, place, true, ProductPresenter::new)
				.promise();
		//@formatter:on
    }

    public static Promise<Boolean> receipt(ShoppingApplication app, WebFlowIntent place) {
        //@formatter:off
		return app.navigate()
				.step(Place.ROOT, place, false, RootPresenter::new)
				.step(Place.RESTRICTED, place, false, RestrictedPresenter::new)
				.step(Place.RECEIPT, place, true, ReceiptPresenter::new)
				.promise();
		//@formatter:on
    }

}
