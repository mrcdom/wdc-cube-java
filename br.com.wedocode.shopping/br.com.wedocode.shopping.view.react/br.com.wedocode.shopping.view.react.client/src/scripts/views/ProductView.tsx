import React from "react";
import app, { type ViewProps } from "../App";
import { itemImageRequest, formatValue } from "../utils/Utils";

// :: Actions

const onOpenProducts = (vsid: string) => app.submit(vsid, 1);

const onAddToCart = (vsid: string, quantity: number) => {
  app.setFormField(vsid, "p.quantity", quantity);
  app.submit(vsid, 2);
};

// :: View

type Product = {
  id: number;
  name: string;
  description: string;
  price: number;
};

const DefaultProduct: Product = {
  id: -1,
  name: '',
  description: '',
  price: 0
}

export type ProductViewState = {
  product: Product;
  quantity: number
  errorMessage?: string
};

export default function ProductView({ vsid }: ViewProps) {
  const { state, scope } = app.bindView<ProductViewState>(vsid);
  state.quantity = state.quantity ?? 1

  const handleOnAddToCart = React.useCallback(() => {
    onAddToCart(vsid, state.quantity);
  }, [vsid, state]);

  const handleOnGoHome = React.useCallback(onOpenProducts.bind(app, vsid), [vsid]);

  const handleQtaChanged = React.useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      state.quantity = Number.parseInt(e.target.value.trim() || "0") | 0;
      app.setFormField(vsid, "quantity", state.quantity);
      scope.forceUpdate()
    },
    [vsid, state]
  );

  const makeErrorMessage = () => {
    if (!state.errorMessage) {
      return <></>;
    }

    return <div className="error" style={{marginTop: 10}}>{state.errorMessage}</div>;
  };

  const product = state.product ?? DefaultProduct;

  return (
    <div className="material-design-card boxProdutos">
      <div className="breadcrumb">
        <p>
          <b>Produtos &gt; {product.name}</b>
        </p>
      </div>
      <div className="fotoproduto">
        <img className="logo" src={itemImageRequest(product.id)} alt={product.name} />
      </div>
      <div className="gdescricao">
        <div className="tituloProduto">
          <h1>{product.name}</h1>
          <h2>R$ {formatValue(product.price)}</h2>
          <div className="quantidade">
            Quantidade: <input type="text" name="quantidade" value={state.quantity} onChange={handleQtaChanged} />
          </div>
          <div className="descricao" style={{ paddingTop: 30 }}>
            <h1>DESCRIÇÃO DO PRODUTO</h1>
            <div>
              <div dangerouslySetInnerHTML={{ __html: product.description }}></div>
            </div>
            {makeErrorMessage()}
            <div className="comprar">
              <button onClick={handleOnAddToCart}>
                <a className="link">ADICIONAR AO CARRINHO</a>
              </button>
            </div>
          </div>
        </div>
      </div>
      <div className="naveg">
        <button onClick={handleOnGoHome}>
          <a className="link">&lt; VOLTAR</a>
        </button>
      </div>
    </div>
  );
}

ProductView.register = () => app.registerView("48b693f67410", (vsid, props) => <ProductView vsid={vsid} {...props} />);
