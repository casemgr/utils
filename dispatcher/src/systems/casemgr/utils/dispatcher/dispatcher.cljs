(ns systems.casemgr.utils.dispatcher.dispatcher
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.reader :as reader]
            [clojure.string :as string]
            [cljs.pprint :as pp]
            [cljsjs.phoenix]
            [goog.events :as events]
            )
  )

(defn receive-ok [params]
  (println "in receive-ok:")
  (println (js-keys params))
  (println (aget params "token"))
  (println (aget params "path"))
  (println (aget params "body"))
  )

(defn receive-error [params]
  (println "in receive-error:")
  (println (js-keys params))
  (println (aget params "token"))
  (println (aget params "path"))
  (println (aget params "body"))
  )
;; todo we need to replace this
(defn ^:private startup
  "Actually connects the web socket to the server and starts the local event loop."
  [cursor owner]
  (let [current-state (om/get-state owner)
        _ (println "current-state:" current-state)
        e-map (:e-map current-state)
        socket (:socket e-map)
        lobby_channel (. socket channel "room:lobby" )
                                        ;joined-channel (. lobby_channel join)
        ]
    (println "socket:" socket)
    (println "lobby_channel:" lobby_channel)
    (-> lobby_channel .join (.receive "ok" receive-ok) (.receive "error" receive-error) )
    (-> lobby_channel .canPush)
    (. lobby_channel push "new_msg" (clj->js {:token "hello world" :path "/shop1/cust1"}))
    (. lobby_channel on "new_msg" receive-ok)
    )
  )

;; call close on the channel or the socket?
(defn ^:private shutdown
  "Tears down the web socket connection."
  [{:keys [socket]}]
                                        ; (.close socket)
  )

;; this is where we do the socket connection and room:lobby
;; how do we connect to a different channel?
;; log in should id the shop
;; one per shop
(defn ^:private make-init-state
  "Creates the initial state for the web socket widget.  In particular, this
  instantiates a WebSocket instance and creates a core.async channel for
  handling events from the WebSocket. "
  [owner cursor]
  (let [
        publisher (:publisher (om/get-shared owner))
        socket (js/Phoenix.Socket. "ws://localhost:4000/socket"
                                        ; (clj->js {"params:" {"userToken:" "123"}}) ;; should we pass the ":"
                                   )
        connected-socket (. socket connect)
        ]
    (println (js-keys socket))
                                        ;  (let [doc-uri (.-location js/window)
                                        ;        _ (println "doc-uri:" doc-uri)
                                        ;        host (.-host doc-uri)
                                        ;        pathname (.-pathname doc-uri)
                                        ;        slash (if (= (.-length pathname) 1) "" "/")
                                        ;        ws-uri (str "ws://" host pathname slash "happiness")
                                        ;        ]
                                        ;    (println "host:" host "pathname:" pathname)
                                        ;    (println "ws-uri:" ws-uri)
                                        ;                                        ;    (def socket (js/Phoenix.Socket. "ws://localhost:4000/socket"  (clj->js {"params:" {"userToken:" "123"}})))
                                        ;                                        ;(println (js-keys socket))
                                        ;                                        ;(.open socket ws-uri)
                                        ;                                        ;(.open socket "ws://localhost:4000/socket")
                                        ;    )
    {:e-map {:display true :socket socket :port (:port cursor)}}
    )
  )

(defn socket-opened [socket publisher v]
  (println "dispatcher->socket-opened->v:" v)
  )

(defn generic-server-msg [socket v]
  (println "dispatcher->generic-server-msg->v:" v)
                                        ; (.send socket v)
  )

(defn user-logged-in [socket publisher v]
  (println "dispatcher->user-logged-in->v:" v)
  (when (:logged-in v)
    ))

(defn ws-widget [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      (println "ws-widget->init-state")
      (println "init-state->cursor" cursor)
      (make-init-state owner cursor)
      )
    om/IWillMount
    (will-mount [_]
      (println "will-mount->cursor" cursor)
      (startup cursor owner))
    om/IDidMount
    (did-mount [_]
      (let [publication (:publication (om/get-shared owner))
            server-message-events (sub publication :server-msg (chan))
            dispatcher-events (sub publication :dispatcher (chan))
            server-events (sub publication :server-response (chan))
            component-msg-events (sub publication :component-msg (chan))]
        (go-loop []
          (let [[v ch] (alts! [dispatcher-events server-message-events server-events component-msg-events])
                publisher (:publisher (om/get-shared owner))
                current-state (om/get-state owner)
                socket (:socket current-state)
                destination (:destination v)
                topic (:topic v)
                component-id (:component-id v)
                message (:message v)]
            (println "current-state:" current-state)
            ;; get rid of match
                                        ;            (match [destination topic            component-id         message  ]
                                        ;                   [_           :server-msg      _                    _        ] (generic-server-msg socket v)
                                        ;                   [_           :dispatcher      :socket              :opened  ] (socket-opened socket publisher v)
                                        ;                   [_           :server-response :edit-login          :validate] (user-logged-in socket publisher v)
                                        ;                   :else
                                        ;                   (println "dispatcher->match->else->v:" v)
                                        ;                                        ;                   :no-match
                                        ;                   )
            (recur))))
      )
    om/IWillUnmount
    (will-unmount [_]
      (shutdown (om/get-state owner)))
    om/IRenderState
    (render-state [_ {:keys [e-map]}]
      (let [display (:display e-map)]
        (pp/pprint e-map)
        (dom/span nil (if display "hello world" ""))
        )
      )
    om/IDisplayName
    (display-name [_] "ws-widget")))

                                        ;    (ev/listen socket
                                        ;               #js [WebSocket.EventType.CLOSED
                                        ;                    WebSocket.EventType.ERROR
                                        ;                    WebSocket.EventType.MESSAGE
                                        ;                    WebSocket.EventType.OPENED]
                                        ;               (fn [e]
                                        ;                 ;(.log js/console (str "log->" (.-type e)))
                                        ;                 ;(.log js/console (str "log->" (.-message e)))
                                        ;                 (cond
                                        ;                  (not= (.-message e) nil) (let [response (reader/read-string(.-message e))]
                                        ;                                             ;(println "dispatcher->response:" response)
                                        ;                                             (put! publisher (merge {:publisher publisher} response)))
                                        ;                  (not= (.-data e) nil) (put! publisher (.-data e))
                                        ;                  :else (let [type (.-type e)
                                        ;                              kind (first (string/split type "_"))]
                                        ;                          ;(println "kind:" kind)
                                        ;                          ;; TODO where do we store and what are we storing
                                        ;                          ;(reset! state/app-state (assoc @state/app-state :socket-state kind))
                                        ;                          (cond
                                        ;                           (= kind "opened") (put! publisher {:topic :dispatcher :component-id :socket :message :opened})
                                        ;                           (= kind "error") (put! publisher {:topic :dispatcher :component-id :socket :message :error})
                                        ;                           (= kind "closed") (put! publisher {:topic :dispatcher :component-id :socket :message :closed})
                                        ;                           ))
                                        ;                  )
                                        ;                 ;;(println "socket-state:" (:socket-state @state/app-state))
                                        ;                 ;;(println "app-state:" @state/app-state)
                                        ;                 ))
                                        ;    {:socket socket}

                                        ;(put! publisher {:topic :server-msg :component-id :shop-data :message :get})
                                        ;(put! publisher {:topic :server-msg :component-id :employees :message :get})
                                        ;(put! publisher {:topic :tabs :active-tab :daily})
                                        ;(put! publisher {:topic :tabs :active-tab :weekly})
