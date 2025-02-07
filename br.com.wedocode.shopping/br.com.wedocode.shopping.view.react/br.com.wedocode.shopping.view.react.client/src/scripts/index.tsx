import * as ReactDOM from 'react-dom/client'

import app from "./App";

import SlotView from './utils/SlotView.js'
import BrowserView from './views/BrowserView.js'
import RootView from './views/RootView.js'
import LoginView from './views/LoginView.js'
import RestrictedView from './views/RestrictedView.js'
import CartView from './views/CartView.js'
import ReciboView from './views/ReceiptView.js'
import ProductView from './views/ProductView.js'

BrowserView.register();
SlotView.register();
RootView.register();
LoginView.register();
RestrictedView.register();
CartView.register();
ReciboView.register();
ProductView.register();

const domContainer = document.querySelector('#root');
const root = ReactDOM.createRoot(domContainer);
root.render(app.createBrowserView());
