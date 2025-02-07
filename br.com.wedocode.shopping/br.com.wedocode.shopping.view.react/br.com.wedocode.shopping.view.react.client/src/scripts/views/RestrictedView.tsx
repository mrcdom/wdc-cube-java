import React, { ReactNode, CSSProperties } from "react";
import app, { type ViewProps } from "../App";
import { formatDate, formatValue, itemImageRequest } from "../utils/Utils";

const { useCallback, useContext, createContext } = React;

// :: Actions

const onExit = (vsid: string) => app.submit(vsid, 1);

const onOpenCart = (vsid: string) => app.submit(vsid, 2);

const onOpenReceipt = (vsid: string, purchaseId: number) => {
  app.setFormField(vsid, "p.purchaseId", purchaseId);
  app.submit(vsid, 3);
};

const onOpenProduct = (vsid: string, productId: number) => {
  app.setFormField(vsid, "p.productId", productId);
  app.submit(vsid, 4);
};

// :: Types

type Product = {
  id: number;
  name: string;
  price: number;
};

type Purchase = {
  id: number;
  date: number;
  total: number;
  items: string[];
};

// :: View

let style: Record<string, CSSProperties> = {
  contentView: {
    marginTop: "25px",
    marginLeft: "25px",
  },
};

export type RestrictedViewState = {
  id: string;
  nickName: string;
  cartItemCount: number;
  products?: Product[];
  purchases?: Purchase[];
  contentViewId?: string;
  errorMessage: string;
};

const RestrictedViewCtx = createContext<RestrictedViewState>(null);

export default function RestrictedView({ vsid }: ViewProps) {
  const { state } = app.bindView<RestrictedViewState>(vsid);

  const handleOnExit = useCallback(onExit.bind(app, vsid), [vsid]);

  const makeContentView = () => {
    if (state.contentViewId) {
      const contentView = app.createView(state.contentViewId, { style: style.contentView });
      if (contentView) {
        return contentView;
      }
    }

    return <RestrictedDefaultContent vsid={vsid} style={style.contentView} />;
  };

  return (
    <RestrictedViewCtx.Provider value={state}>
      <div>
        <HeaderPanel vsid={vsid} cartItemCount={state.cartItemCount} />
        <div className="centerOne">
          <div className="grandeC">
            <div className="boasvindas">
              <p>
                Seja bem vindo, <b>{state.nickName}!</b>
              </p>
              <div className="sair2">
                <button onClick={handleOnExit}>
                  <a className="link">sair</a>
                </button>
              </div>
              <p />
              {makeContentView()}
            </div>
          </div>
        </div>
      </div>
    </RestrictedViewCtx.Provider>
  );
}

RestrictedView.register = () => app.registerView("473dbdd7a36a", (vsid, props) => <RestrictedView vsid={vsid} {...props} />);

// :: Internal -- HeaderPanel

type HeaderPanelProps = {
  vsid: string;
  cartItemCount: number;
};

function HeaderPanel({ vsid, cartItemCount }: HeaderPanelProps) {
  const handleClick = useCallback(onOpenCart.bind(app, vsid), [vsid]);

  return (
    <div className="header">
      <div className="left">
        <img className="logo-internal" src="images/logo.png" alt="Shopping Triplice" />
      </div>
      <div className="right">
        <a className="btnCarrinho use-pointer" onClick={handleClick}>
          <img src="images/carrinho.png" alt="Shopping Triplice" />
          <h5>Carrinho</h5>
          <h6>[{formatValue(cartItemCount, 0)}]</h6>
        </a>
      </div>
    </div>
  );
}

// Internal :: RestrictedDefaultContent

type RestrictedDefaultContentProps = {
  vsid: string;
  className?: string;
  style?: CSSProperties;
};

function RestrictedDefaultContent(props: RestrictedDefaultContentProps) {
  const state = useContext(RestrictedViewCtx);

  const divProdutos: ReactNode[] = [];
  if (state.products) {
    const listaProdutos = state.products;
    for (let i = 0; i < listaProdutos.length; i++) {
      let produto = listaProdutos[i];

      divProdutos.push(<CardProduto key={produto.id} vsid={props.vsid} product={produto} />);
    }
  }

  const divCompras: ReactNode[] = [];
  if (state.purchases) {
    const listaCompras = state.purchases;
    for (let i = 0; i < listaCompras.length; i++) {
      const compra = listaCompras[i];

      divCompras.push(
        <CardHistoricoCompra key={compra.id} vsid={props.vsid} purchase={compra}>
          <ul>
            {compra.items.map((item) => (
              <li key={item}>
                <span>{item}</span>
              </li>
            ))}
          </ul>
        </CardHistoricoCompra>
      );
    }
  }

  return (
    <div className={props.className} style={props.style}>
      <div className="menu">
        <h4>Seu histórico de compras</h4>
        {divCompras}
      </div>
      <div className="contprod">{divProdutos}</div>
    </div>
  );
}

// :: Internal - CardHistoricoCompra

type CardHistoricoCompraProps = {
  vsid: string;
  purchase: Purchase;
  children?: ReactNode;
};

function CardHistoricoCompra({ vsid, purchase, children }: CardHistoricoCompraProps) {
  const handleClick = useCallback(onOpenReceipt.bind(app, vsid, purchase.id), [vsid, purchase.id]);

  return (
    <div className="material-design-card hbox">
      <h1>Compra #{purchase.id}</h1>
      <div className="padding-formatter">
        <h2>Data da compra:</h2>
        <p>{formatDate(purchase.date)}</p>
        <h2>Itens adquiridos:</h2>
        {children}
        <p>
          <b>Valor Total: </b>R$ {formatValue(purchase.total)}
        </p>
      </div>
      <div className="vermais">
        <button>
          <a onClick={handleClick}>VEJA MAIS DETALHES</a>
        </button>
      </div>
    </div>
  );
}

// :: Internal - CardProduto

type CardProdutoProps = {
  vsid: string;
  product: Product;
};

function CardProduto({ vsid, product }: CardProdutoProps) {
  const handleClick = useCallback(onOpenProduct.bind(app, vsid, product.id), [vsid, product.id]);

  return (
    <div className="material-design-card minibox use-pointer" onClick={handleClick}>
      <div className="afoto">
        <img src={itemImageRequest(product.id)} alt={product.name} />
      </div>
      <div className="bfoto">
        <a className="link">
          {product.name}
          <br />
          <b>R$ {formatValue(product.price)}</b>
        </a>
      </div>
    </div>
  );
}
