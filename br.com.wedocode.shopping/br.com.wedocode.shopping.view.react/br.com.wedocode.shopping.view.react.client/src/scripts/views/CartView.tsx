import React, { ReactNode } from "react";
import app, { type ViewProps } from "../App";
import { itemImageRequest, formatValue } from "../utils/Utils.js";

// :: Actions

const onBuy = (vsid: string) => app.submit(vsid, 1);

const onRemoveProduct = (vsid: string, productId: number) => {
  app.setFormField(vsid, "p.productId", productId);
  app.submit(vsid, 2);
};

const onOpenProducts = (vsid: string) => app.submit(vsid, 3);

// :: View

type ItemCarrinho = {
  id: number;
  name: string;
  price: number;
  quantity: number;
};

export type CartViewState = {
  items: ItemCarrinho[];
  errorMessage?: string;
};

export default function CartView({ vsid }: ViewProps) {
  const { state } = app.bindView<CartViewState>(vsid);

  const handleClickFinalizar = React.useCallback(onBuy.bind(app, vsid), [vsid]);
  const handleClickVoltar = React.useCallback(onOpenProducts.bind(app, vsid), [vsid]);
  const handleClickRemove = React.useCallback(
    (e: React.MouseEvent<HTMLElement>) => {
      const imgElm = e.target as HTMLImageElement;
      let sId = imgElm.id;
      const colonIdx = sId.lastIndexOf(":");
      if (colonIdx !== -1) {
        const itemId = Number.parseInt(sId.substring(colonIdx + 1));
        if (Number.isInteger(itemId)) {
          onRemoveProduct(vsid, itemId);
          return;
        }
      }
      console.error(`Código errado de item no carrinho: ${sId}`);
    },
    [vsid]
  );

  const cartList: ReactNode[] = [];
  let valorTotal = 0;
  let carrinhoTotal = 0;

  if (state.items) {
    for (let i = 0; i < state.items.length; i++) {
      const prod = state.items[i];
      const prodHtmlId = `${vsid}:${prod.id}`;

      cartList.push(
        <div key={`${prodHtmlId}-1`} className="carcel2">
          <img className="mini-img-produto" src={itemImageRequest(prod.id)} alt={prod.name} />
          {prod.name}
        </div>
      );
      cartList.push(
        <div key={`${prodHtmlId}-2`} className="carcel2">
          R$ {formatValue(prod.price)}
        </div>
      );
      cartList.push(
        <div key={`${prodHtmlId}-3`} className="carcel2b">
          {" "}
          {formatValue(prod.quantity, 0)}
          <a>
            <img src="images/delet.png" id={prodHtmlId} onClick={handleClickRemove} alt="Remover" />
          </a>{" "}
        </div>
      );

      carrinhoTotal += prod.quantity;
      valorTotal += prod.price * prod.quantity;
    }
  }

  const makeBotaoFinalizar = () => {
    if (cartList.length === 0) {
      return <></>;
    }

    return (
      <div className="finalizacompra">
        <button onClick={handleClickFinalizar}>
          <a className="link">FINALIZAR PEDIDO &gt;</a>
        </button>
      </div>
    );
  };

  const makeErrorMessage = () => {
    if (!state.errorMessage) {
      return <></>;
    }

    return <div className="error" style={{marginTop: 10}}>{state.errorMessage}</div>;
  };

  return (
    <div className="material-design-card padding-formatter boxProdutos">
      <div className="carQtcarro">
        <div className="btnCarrinho">
          <img src="images/carrinho.png" alt="Carrinho" />
          <h5>Carrinho</h5>
          <h6>[{formatValue(carrinhoTotal, 0)}]</h6>
        </div>
        <h2>LISTA DE PRODUTOS</h2>
      </div>
      <div className="carrecibo">
        <div className="carreciboTopo" />
        <div className="carcel1">ITEM</div>
        <div className="carcel1">VALOR UNITÁRIO</div>
        <div className="carcel1">QUANTIDADE</div>
        {cartList}
        <div className="carvalorcompra">VALOR TOTAL: R$ {formatValue(valorTotal)}</div>
      </div>
      {makeErrorMessage()}
      {makeBotaoFinalizar()}
      <div className="naveg">
        <button onClick={handleClickVoltar}>
          <a className="link">&lt; VOLTAR</a>
        </button>
      </div>
    </div>
  );
}

CartView.register = () => app.registerView("7eb485e5f843", (vsid, props) => <CartView vsid={vsid} {...props} />);
