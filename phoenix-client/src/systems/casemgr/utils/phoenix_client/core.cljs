(ns systems.casemgr.utils.phoenix-client.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.pprint :as pp]
            [cljsjs.phoenix]
            [goog.events :as events]
            )
  )

(enable-console-print!)

(println "starting...")
;(println "This text is printed from src/systems.casemgr.utils.phoenix-client/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(def socket (js/Phoenix.Socket. "ws://localhost:4000/socket"  (clj->js {"params:" {"userToken:" "123"}})))
                                        ;(println (js-keys socket))
(def connected-socket (. socket connect))


(def channel (. socket channel "room:lobby" ))

(defn receive [params]
  (println "in receive:")
  (println (js-keys params))
  (println (aget params "token"))
  (println (aget params "path"))
  (println (aget params "body"))
  )

;(. channel on )
;(doto (. channel join)
;  (events/listen "receive" receive))
;(def joined-channel (. channel join))
;(-> channel .join (.receive "ok" #(js/console.log "ok" %)) (.receive "error" #(js/console.log "error" %)))
(-> channel .join (.receive "ok" receive) )
;something like (-> channel .join (.receive "ok" #(js/console.log "meh" %))) should work
;(-> channel .leave)

(-> channel .canPush)

(. channel push "new_msg" (clj->js {:token "hello world" :path "/shop1/cust1"}))
(. channel on "new_msg" receive)
(pp/pprint {:test "hello world" :other {:token "key123"}})
;(pp/pprint (js-keys joined-channel))

;(. joined-channel receive (fn [msgs] (println "hello world")))

                                        ;(do
                                        ;(println (.-payload msgs))
                                        ;                                        ;(.dir js/-keys (clj->js msgs))
                                        ;)
                                        ;(pp/pprint msgs)
(println "done")

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
