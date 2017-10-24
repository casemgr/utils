(ns systems.casemgr.utils.phoenix-client.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljsjs.phoenix]
            )
  )

(enable-console-print!)

(println "This text is printed from src/systems.casemgr.utils.phoenix-client/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(def socket (js/Phoenix.Socket. "ws://localhost:4000/socket"  {:params {:userToken "123"}}))
(println socket)
(def connected-socket (. socket connect))


(def channel (. socket channel "room:lobby" ))
(def joined-channel (. channel join))

;       .receive("ok", ({messages}) => console.log("catching up", messages) )
;       .receive("error", ({reason}) => console.log("failed join", reason) )
;       .receive("timeout", () => console.log("Networking issue. Still waiting...") )

(om/root
 (fn [data owner]
   (reify om/IRender
     (render [_]
       (dom/div nil
                (dom/h1 nil (:text data))
                (dom/h3 nil "Edit this!")))))
 app-state
 {:target (. js/document (getElementById "app"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
