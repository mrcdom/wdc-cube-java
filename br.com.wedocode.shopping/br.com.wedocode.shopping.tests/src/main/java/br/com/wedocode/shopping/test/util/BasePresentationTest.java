package br.com.wedocode.shopping.test.util;

import br.com.wedocode.shopping.test.mock.ShoppingApplicationMock;

public class BasePresentationTest extends BaseBusinessTest {

    protected ShoppingApplicationMock app;

    @Override
    public void before() throws Exception {
        super.before();
        this.app = new ShoppingApplicationMock();
    }

    @Override
    public void after() {
        super.after();
        this.app.release();
    }

}
