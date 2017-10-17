(ns systems.casemgr.utils.dispatcher.core
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [cljs.core.match :refer-macros [match]]
            [goog.events :as ev]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.reader :as reader]
            [clojure.string :as string]
  ;          [scheduler_server.state :as state]
            )
  (:import goog.net.WebSocket))

(defn ^:private startup
  "Actually connects the web socket to the server and starts the local event loop."
  [global-state {:keys [socket] :as local-state}]
  (let [doc-uri (.-location js/window)
        host (.-host doc-uri)
        pathname (.-pathname doc-uri)
        slash (if (= (.-length pathname) 1) "" "/")
        ws-uri (str "ws://" host pathname slash "happiness")
        ]
    (println "host:" host "pathname:" pathname)
    (.open socket ws-uri)))

(defn ^:private shutdown
  "Tears down the web socket connection."
  [{:keys [socket]}]
  (.close socket))

(defn ^:private make-init-state
  "Creates the initial state for the web socket widget.  In particular, this
  instantiates a WebSocket instance and creates a core.async channel for
  handling events from the WebSocket. "
  [owner]
  (let [socket (WebSocket.)
        publisher (:publisher (om/get-shared owner))]
    (ev/listen socket
               #js [WebSocket.EventType.CLOSED
                    WebSocket.EventType.ERROR
                    WebSocket.EventType.MESSAGE
                    WebSocket.EventType.OPENED]
               (fn [e]
                 ;(.log js/console (str "log->" (.-type e)))
                 ;(.log js/console (str "log->" (.-message e)))
                 (cond
                  (not= (.-message e) nil) (let [response (reader/read-string(.-message e))]
                                             ;(println "dispatcher->response:" response)
                                             (put! publisher (merge {:publisher publisher} response)))
                  (not= (.-data e) nil) (put! publisher (.-data e))
                  :else (let [type (.-type e)
                              kind (first (string/split type "_"))]
                          ;(println "kind:" kind)
                          ;; TODO where do we store and what are we storing
                          ;(reset! state/app-state (assoc @state/app-state :socket-state kind))
                          (cond
                           (= kind "opened") (put! publisher {:topic :dispatcher :component-id :socket :message :opened})
                           (= kind "error") (put! publisher {:topic :dispatcher :component-id :socket :message :error})
                           (= kind "closed") (put! publisher {:topic :dispatcher :component-id :socket :message :closed})
                           ))
                  )
                 ;;(println "socket-state:" (:socket-state @state/app-state))
                 ;;(println "app-state:" @state/app-state)
                 ))
    {:socket socket}))

(defn socket-opened [socket publisher v]
  ;(println "dispatcher->socket-opened->v:" v)
  )

(defn generic-server-msg [socket v]
  ;(println "dispatcher->generic-server-msg->v:" v)
  (.send socket v))

(defn user-logged-in [socket publisher v]
  ;(println "dispatcher->user-logged-in->v:" v)
  (when (:logged-in v)
    (put! publisher {:topic :server-msg :component-id :shop-data :message :get})
    (put! publisher {:topic :server-msg :component-id :employees :message :get})
    (put! publisher {:topic :tabs :active-tab :daily})
    ;(put! publisher {:topic :tabs :active-tab :weekly})
  ))

(defn ws-widget [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
     (make-init-state owner))
    om/IWillMount
    (will-mount [_]
      (startup cursor (om/get-state owner)))
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
            ;(println "current-state:" current-state)
            (match [destination topic            component-id         message  ]
                   [_           :server-msg      _                    _        ] (generic-server-msg socket v)
                   [_           :dispatcher      :socket              :opened  ] (socket-opened socket publisher v)
                   [_           :server-response :edit-login          :validate] (user-logged-in socket publisher v)
                   :else
                   ;(println "dispatcher->match->else->v:" v)
                   :no-match
                   )
            (recur)))))
    om/IWillUnmount
    (will-unmount [_]
      (shutdown (om/get-state owner)))
    om/IRender
    (render [_] ; We must render somthing…
      (dom/span nil ""))
    om/IDisplayName
    (display-name [_] "ws-widget")))

