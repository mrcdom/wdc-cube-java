import React from "react";
import * as history from "history";
import { deleteProperties, makeUniqueId } from "./utils/Utils";
import BigIntUtils from "./utils/BigIntUtils";
import RSA from "./utils/RSA";
import UTF8 from "./utils/UTF8";
import Base64 from "./utils/Base64";
import CookieConstructor from 'universal-cookie'

// :: Constants

const Cookie = new CookieConstructor()

const NOOP_VOID = () => void 0 as void;

const CAUTHED = (reason: unknown) => {
  console.error('Unexpected', reason)
}

export const BROWSER_VID = "7b32e816a191";
const BROWSER_VSID = `${BROWSER_VID}:0`;

//const KEEP_ALIVE_INTERVAL = 4 * 60 * 1000;
const KEEP_ALIVE_INTERVAL = 30 * 1000;

// :: Types

export type BrowserViewState = {
  contentViewId?: string;

  alertMessage?: {
    id: number;
    args?: string[];
  };

  error?: {
    delay: number;
    cause: unknown;
    numAttempt: number;
  };
};

type IViewFactory = (vid: string, props: Record<string, unknown>) => React.ReactNode;

export type ViewProps = {
  vsid: string;
  className?: string;
  style?: React.CSSProperties;
};

// :: App

type FormMapType = {
  requestId?: number;
  event?: string[];
};

class WdcApplication {
  readonly id: string;
  readonly history = history.createHashHistory();

  readonly viewFactory = new Map<string, IViewFactory>();
  readonly viewMap = new Map<string, ViewScope>();

  formMap: FormMapType = {};

  isConnected = false;
  path = "";
  baseWebSocketUtl = "";
  unlistenHistory = NOOP_VOID;

  readonly dataSecurity = new DataSecurity()
  readonly contextExchanger: FlushRequestContext;
  readonly reconnectController: ReconnectController;
  readonly viewGarbageCollector: ViewGarbageCollector;

  readyToStart = Promise.resolve(true);

  constructor() {
    this.viewMap.set(BROWSER_VSID, new ViewScope(BROWSER_VSID));

    const appIdFromCookie = Cookie.get("app_id");
    if (appIdFromCookie) {
      Cookie.remove('app_id')
    }

    let appId = sessionStorage.getItem("app_id");
    if (!appId) {
      appId = appIdFromCookie
      if (appId) {
        sessionStorage.setItem("app_id", appId)
      }
      appId = makeUniqueId();
    }

    this.id = appId;

    const appSKey = Cookie.get("app_skey");
    if (appSKey) {
      this.dataSecurity.updateSecurityKey(appSKey);
      Cookie.remove('app_skey')

      const previousReadyToStart = this.readyToStart;
      this.readyToStart = (async () => {
        try {
          if (!await previousReadyToStart) {
            return false;
          }

          await this.dataSecurity.updateSecretWithRandomPassword()

          Cookie.set('app_signature', this.dataSecurity.getSignature())

          return true;
        } catch (error) {
          CAUTHED(error)
        }
      })();
    }

    this.baseWebSocketUtl = (document.location.protocol === 'http:' ? 'ws://' : 'wss://')
      + document.location.host
      + document.location.pathname

    this.contextExchanger = new FlushRequestContext(this);
    this.reconnectController = new ReconnectController(this);
    this.viewGarbageCollector = new ViewGarbageCollector(this);
  }

  getBaseWebSocketUrl() {
    return this.baseWebSocketUtl;
  }

  registerView(viewId: string, factory: IViewFactory) {
    this.viewFactory.set(viewId, factory);
  }

  createView(vsid: string, props?: Record<string, unknown>) {
    const parts = vsid.split(/:/g);
    const viewCreator = this.viewFactory.get(parts[0]);
    if (!viewCreator) {
      throw new Error(`Nenhuma view registrada para a viewId: "${parts[0]}"`);
    }

    let viewScope = this.viewMap.get(vsid);
    if (!viewScope) {
      viewScope = new ViewScope(vsid);
      this.viewMap.set(vsid, viewScope);
    }

    return viewCreator(vsid, props);
  }

  connect() {
    this.reconnectController.checkNow();
  }

  bindView<T>(vsid: string) {
    let scope = this.viewMap.get(vsid);
    if (!scope) {
      scope = this.viewGarbageCollector.recover(vsid);
      if (!scope) {
        throw new Error(`Missing View Scope for id(${vsid})`);
      }
    }

    const [updateCount, setUpdateCount] = React.useState(0);
    scope.forceUpdate = () => setUpdateCount(updateCount + 1);

    React.useEffect(() => {
      // attached
      return () => {
        // detached
        this.viewGarbageCollector.mark(scope);
      };
    }, []);

    return { state: scope.getState() as T, scope };
  }

  onStart() {
    const action = async () => {
      // Wait construction async initialization
      await this.readyToStart;

      this.assureContextExchangerIsConnected();

      this.unlistenHistory();
      this.unlistenHistory = this.history.listen(({ action, location }) => {
        if (action === "POP") {
          let path = `${location.pathname}${location.search ? location.search : ""}`;
          if (this.path !== path) {
            this.onHistoryChange(path);
          }
        }
      });

      const hash = window.location.hash;
      this.path = hash && hash.length > 1 ? hash.substring(1) : "/";

      this.setFormField(BROWSER_VSID, "p.path", this.path);
      this.submit(BROWSER_VSID, -1);
    }

    action().catch(CAUTHED)
  };


  onStop() {
    this.unlistenHistory();
    this.unlistenHistory = NOOP_VOID;
  }

  onHistoryChange(path: string) {
    this.setFormField(BROWSER_VSID, "p.path", path);
    this.submit(BROWSER_VSID, -2);
  }

  onKeepAlive() {
    this.submit(BROWSER_VSID, 2);
  }

  applyViewStates(stateList: { id: string }[]) {
    for (let i = 0, ilen = stateList.length; i < ilen; i++) {
      let viewState = stateList[i];
      if (!viewState || !viewState.id) {
        continue;
      }
      const vsid = viewState.id;
      this.viewGarbageCollector.recover(vsid);

      let viewScope = this.viewMap.get(vsid);
      if (!viewScope) {
        viewScope = new ViewScope(vsid);
        this.viewMap.set(vsid, viewScope);
      }

      viewScope.setState(viewState);
    }
  }

  submit(vsid: string, eventId: number) {
    const oldFormMap = this.formMap;
    this.formMap = {};
    this.contextExchanger.submit(oldFormMap, vsid, eventId);
  }

  setFormField(vsid: string, fieldName: string, fieldValue: unknown) {
    var formData = this.formMap[vsid];
    if (!formData) {
      formData = {};
      this.formMap[vsid] = formData;
    }
    formData[fieldName] = fieldValue;
  }

  readonly assureContextExchangerIsConnected = () => {
    this.contextExchanger.open(this.reconnectController.url);
  };
}

class ViewScope {
  private readonly __svid: string;
  private readonly __viewState: Record<string, unknown> = {};

  forceUpdate = NOOP_VOID;

  constructor(svid: string) {
    this.__svid = svid;
  }

  getId() {
    return this.__svid;
  }

  getState() {
    return this.__viewState;
  }

  setState(newViewState: Record<string, unknown>) {
    deleteProperties(this.__viewState);
    Object.assign(this.__viewState, newViewState);
    this.forceUpdate();
  }
}

class RsaHelper {
  private __rsa: RSA;

  constructor(skey: string) {
    const [exponent, key] = skey.split(/:/);

    const publicExponent = BigIntUtils.parse(exponent, 36);
    const privateKey = 0n;
    const publicKey = BigIntUtils.parse(key, 36);
    this.__rsa = new RSA(publicExponent, privateKey, publicKey);
  }

  encryptToBase36(message: Uint8Array) {
    const messageAsSafeBytes = UTF8.encode(Base64.encode(message));
    const messageAsBigint = BigIntUtils.fromBuffer(messageAsSafeBytes);

    const messageEncryptedAsBigInt = this.__rsa.encrypt(messageAsBigint);
    return messageEncryptedAsBigInt.toString(36);
  }
}

class DataSecurity {

  private __iv: Uint8Array<ArrayBuffer>;
  private __key: CryptoKey;
  private __signature: string
  private __rsa: RsaHelper

  updateSecurityKey(appSKey: string) {
    this.__rsa = new RsaHelper(appSKey)
  }

  async updateSecretWithRandomPassword() {
    const pwd = Base64.encodeUrlSafe(window.crypto.getRandomValues(new Uint8Array(12)));
    const pwdBuf = UTF8.encode(pwd);
    await this.updateSecret(pwdBuf)
  }

  async updateSecret(password: Uint8Array) {
    // Generate salt and IV
    const salt = crypto.getRandomValues(new Uint8Array(16));
    this.__iv = crypto.getRandomValues(new Uint8Array(12));

    // Derive key from password
    const key = await crypto.subtle.importKey(
      'raw',
      password,
      { name: 'PBKDF2' },
      false,
      ['deriveKey']
    );

    this.__key = await crypto.subtle.deriveKey(
      {
        name: 'PBKDF2',
        salt: salt,
        iterations: 250000,
        hash: 'SHA-256'
      },
      key,
      { name: 'AES-GCM', length: 256 },
      false,
      ['encrypt', 'decrypt']
    );

    const cryptedPwd = this.__rsa.encryptToBase36(password)

    this.__signature = `${cryptedPwd}.${Base64.encodeUrlSafe(salt)}.${Base64.encodeUrlSafe(this.__iv)}`;
    console.log('signature=' + this.__signature);
  }

  getSignature() {
    return this.__signature
  }

  async b64Cipher(text: string) {
    const textAsUtf8Array = UTF8.encode(text)
    const cipheredText = await crypto.subtle.encrypt(
      {
        name: "AES-GCM",
        iv: this.__iv
      },
      this.__key,
      textAsUtf8Array
    );
    return Base64.encode(new Uint8Array(cipheredText))
  }

  async b64Decipher(b64CipheredText: string) {
    const cipheredText = Base64.decode(b64CipheredText)
    const textAsUtf8Array = await crypto.subtle.decrypt(
      {
        name: "AES-GCM",
        iv: this.__iv
      },
      this.__key,
      cipheredText
    );
    return UTF8.decode(textAsUtf8Array);
  }

}

class ReconnectController {
  app: WdcApplication;
  url = "";
  count = 0;
  reconnectHandler = 0;
  delay = 0;
  cause: unknown;

  constructor(app: WdcApplication) {
    this.app = app;
    this.url = app.getBaseWebSocketUrl() + "dispatcher/" + app.id;
  }

  close() {
    clearInterval(this.reconnectHandler);
  }

  reconnect(cause: unknown) {
    const app = this.app;

    this.count++;
    this.delay = Math.min(2000 * this.count, 120000);
    this.cause = cause;

    {
      const bvScope = app.viewMap.get(BROWSER_VSID);
      const bvState = bvScope.getState() as BrowserViewState;
      bvState.error = {
        cause: this.cause,
        numAttempt: this.count,
        delay: this.delay,
      };
      bvScope.forceUpdate();
    }

    if (this.reconnectHandler === 0) {
      this.reconnectHandler = window.setInterval(() => this.check(), 1000);
    }
  }

  check() {
    const app = this.app;
    const bvScope = app.viewMap.get(BROWSER_VSID);

    if (app.isConnected) {
      this.reset();
      return;
    }

    const bvState = bvScope.getState() as BrowserViewState;
    bvState.error = {
      cause: this.cause,
      numAttempt: this.count,
      delay: this.delay,
    };

    if (this.delay > 0) {
      this.delay -= 1000;
      this.delay < 0 && (this.delay = 0);
      bvState.error.delay = this.delay;
    }

    bvScope.forceUpdate();

    if (this.delay <= 0) {
      window.setTimeout(app.assureContextExchangerIsConnected, 16);
    }
  }

  reset() {
    const app = this.app;
    const browserView = app.viewMap.get(BROWSER_VSID);
    var browserViewState = browserView.getState();
    browserViewState.error = undefined;
    browserView.setState(browserViewState);

    this.count = 0;
    this.delay = 0;
    this.cause = null;

    clearInterval(this.reconnectHandler);
  }

  checkNow() {
    this.delay = 0;
    this.check();
  }
}

class FlushRequestContext {
  private readonly app: WdcApplication;

  socket: WebSocket;
  requestMap = new Map<number, FormMapType>();
  lastSentRequestId = -1;
  requestCount = 0;
  lastProcessedId = -1;
  keepAliveHandler = 0;

  constructor(app: WdcApplication) {
    this.app = app;
  }

  submit(formMap: FormMapType, vsid: string, eventId: number) {
    formMap.requestId = this.requestCount++;
    if (!formMap.event) {
      formMap.event = [vsid + ":" + eventId];
    } else {
      formMap.event.push(vsid + ":" + eventId);
    }
    this.requestMap.set(formMap.requestId, formMap);

    this.flush();
  }

  flush() {
    type RequestObjType = {
      requestId: number;
      event: string[];
    };

    const { socket, lastSentRequestId, requestCount, requestMap } = this;

    if (socket && socket.readyState === WebSocket.OPEN) {
      const requestObj: Partial<RequestObjType> = { event: [] };
      let hasData = false;
      for (let i = lastSentRequestId + 1; i < requestCount; i++) {
        const requestItemObj = requestMap.get(i);
        if (!requestItemObj) {
          continue;
        }

        const keys = Object.keys(requestItemObj);
        for (let j = 0; j < keys.length; j++) {
          const key = keys[j];
          const value = requestItemObj[key] as unknown;
          if (value) {
            if ("event" === key) {
              const valueArray = value as string[];
              for (let k = 0; k < valueArray.length; k++) {
                requestObj.event.push(valueArray[k]);
              }
            } else {
              let formData = requestObj[key] as object;
              if (!formData) {
                formData = {};
                requestObj[key] = formData;
              }
              Object.assign(formData, value as object);
            }
          }
        }
        requestObj.requestId = i;

        this.lastSentRequestId = i;

        hasData = true;
      }

      if (hasData) {
        socket.send(JSON.stringify(requestObj));
      }
    }
  }

  open(url: string) {
    if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
      return;
    }

    const me = this;
    const app = this.app;



    const socket = this.socket = new WebSocket(url, ["wdc"]);
    (socket as unknown as Record<string, unknown>).withCredentials = true;

    socket.onopen = () => {
      app.isConnected = true;
      app.reconnectController.reset();
      this.initKeepAliveChecks();

      if (this.requestCount > 1) {
        socket.send(
          JSON.stringify({
            ping: true,
            requestId: this.lastProcessedId,
            path: app.path,
            secret: app.dataSecurity.getSignature()
          })
        );
      } else {
        this.flush();
      }
    };

    socket.onerror = (error) => {
      app.isConnected = false;
      app.reconnectController.reconnect(error);
    };

    // Log messages from the server
    socket.onmessage = (e) => {
      const response = JSON.parse(e.data);

      //console.log(response);

      if (response.ping) {
        me.lastSentRequestId = response.requestId;
        me.lastProcessedId = response.requestId;
      }

      for (let i = me.lastProcessedId + 1; i <= response.requestId; i++) {
        me.requestMap.delete(i);
        me.lastProcessedId = i;
      }

      if (response.uri) {
        app.path = response.uri;
        window.location.href = `#${response.uri}`
      }

      if (response.states) {
        app.applyViewStates(response.states);
      }

      me.flush();
    };

    socket.onclose = (error) => {
      app.isConnected = false;
      app.reconnectController.reconnect(error);
    };
  }

  close() {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }

  private initKeepAliveChecks() {
    window.clearTimeout(this.keepAliveHandler);
    this.keepAliveHandler = window.setTimeout(this.keepAlive, KEEP_ALIVE_INTERVAL);
  }

  private readonly keepAlive = () => {
    window.clearTimeout(this.keepAliveHandler);
    this.app.onKeepAlive();
    this.keepAliveHandler = window.setTimeout(this.keepAlive, KEEP_ALIVE_INTERVAL);
  };
}

class ViewGarbageCollector {
  private readonly app: WdcApplication;
  private readonly garbageViewMap = new Map<string, ViewScope>();

  private taskHandler = 0;

  constructor(app: WdcApplication) {
    this.app = app;
  }

  mark(scope: ViewScope) {
    const vsid = scope.getId();
    if (vsid !== BROWSER_VSID) {
      this.garbageViewMap.set(vsid, scope);
      this.scheduleCollection();
    }
  }

  recover(vsid: string) {
    const scope = this.garbageViewMap.get(vsid);
    if (scope) {
      privateApp.viewMap.set(vsid, scope);
      this.garbageViewMap.delete(vsid);
      this.cancelSchedulingIfEmpty();
      return scope;
    } else {
      return null;
    }
  }

  private scheduleCollection() {
    clearTimeout(this.taskHandler);
    this.taskHandler = setTimeout(this.doCollection, 4000);
  }

  private readonly doCollection = () => {
    clearTimeout(this.taskHandler);

    if (this.garbageViewMap.size > 0) {
      this.garbageViewMap.forEach((scope) => {
        this.app.viewMap.delete(scope.getId());
      });
      this.garbageViewMap.clear();
    }
  };

  private cancelSchedulingIfEmpty() {
    if (this.garbageViewMap.size === 0) {
      clearTimeout(this.taskHandler);
    }
  }
}

async function static_updateAllViewStates(app: WdcApplication, vsids: string[]) {
  var url = `view-state`;

  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      "X-Application-Id": app.id
    },
    body: JSON.stringify(vsids),
  });

  const viewStates = (await resp.json()) as { id: string }[];
  app.applyViewStates(viewStates);
}

const privateApp = new WdcApplication();

const publicApp = new (class {
  getBaseUrl() {
    return privateApp.baseWebSocketUtl;
  }

  getViewState<T>(vsid: string) {
    var viewScope = privateApp.viewMap.get(vsid);
    return viewScope ? (viewScope.getState() as T) : ({} as T);
  }

  connect() {
    privateApp.connect();
  }

  submit(vsid: string, eventId: number) {
    privateApp.submit(vsid, eventId);
  }

  async cipher(value: string) {
    return privateApp.dataSecurity.b64Cipher(value)
  }

  setFormField(vsid: string, fieldName: string, fieldValue: unknown) {
    privateApp.setFormField(vsid, fieldName, fieldValue);
  }

  registerView(viewId: string, factory: IViewFactory) {
    privateApp.registerView(viewId, factory);
  }

  createView(vsid: string, props?: Record<string, unknown>) {
    return privateApp.createView(vsid, props);
  }

  createBrowserView() {
    return privateApp.createView(BROWSER_VSID);
  }

  bindView<T>(vsid: string) {
    return privateApp.bindView<T>(vsid);
  }

  updateViewState(vsid: string) {
    static_updateAllViewStates(privateApp, [vsid]);
  }

  onStart() {
    privateApp.onStart();
  }

  onStop() {
    privateApp.onStop();
  }
})();

export default publicApp;
