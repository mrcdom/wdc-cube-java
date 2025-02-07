import React, { ReactNode } from "react";
import app, { type ViewProps } from "../App";
import { formatValue } from "../utils/Utils.js";

// :: Actions

let onOpenProducts = (vsid: string) => app.submit(vsid, 1);

// View

type ReceiptItem = {
  description: string;
  value: number;
  quantity: number;
};

type ReceiptForm = {
  date: number;
  items: ReceiptItem[];
  total: number;
};

export type ReceiptViewState = {
  receipt: ReceiptForm;
  notifySuccess?: boolean;
};

export default function ReceiptViewView({ vsid }: ViewProps) {
  const { state } = app.bindView<ReceiptViewState>(vsid);

  const handleOnOpenProducts = React.useCallback(onOpenProducts.bind(app, vsid), [vsid]);

  var cellArray = [];
  var reciboItems = state.receipt.items;

  for (let i = 0; i < reciboItems.length; i++) {
    let reciboObj = reciboItems[i];
    cellArray.push(
      <div key={i + "-1"} className="cel1">
        {" "}
        {reciboObj.description}{" "}
      </div>
    );

    cellArray.push(
      <div key={i + "-2"} className="cel1">
        R$ {formatValue(reciboObj.value)}{" "}
      </div>
    );

    cellArray.push(
      <div key={i + "-3"} className="cel1">
        {" "}
        {formatValue(reciboObj.quantity, 0)}{" "}
      </div>
    );
  }

  let notifySuccessElm: ReactNode;
  if (state.notifySuccess) {
    notifySuccessElm = <h1>COMPRA EFETUADA COM SUCESSO</h1>;
  }

  return (
    <div className="material-design-card boxProdutos">
      {notifySuccessElm}
      <h2>IMPRIMA SEU RECIBO:</h2>
      <div className="recibo">
        <div className="reciboTopo">
          <h3>STELA SHOPPING - SUA COMPRA CERTA NA INTERNET</h3>
          <h3>Recibo de compra</h3>
        </div>
        <div className="cel1">ITEM</div>
        <div className="cel1">VALOR UNITÁRIO</div>
        <div className="cel1">QUANTIDADE</div>
        <div className="borda" />
        {cellArray}
        <div className="borda2" />
        <div className="borda" />
        <div className="valorcompra">VALOR TOTAL: R$ {formatValue(state.receipt.total)}</div>
      </div>
      <div className="naveg">
        <button onClick={handleOnOpenProducts}>
          <a className="link">&lt; VOLTAR</a>
        </button>
      </div>
    </div>
  );
}

ReceiptViewView.register = () => app.registerView("e8d0bd8ae3bc", (vsid, props) => <ReceiptViewView vsid={vsid} {...props} />);
