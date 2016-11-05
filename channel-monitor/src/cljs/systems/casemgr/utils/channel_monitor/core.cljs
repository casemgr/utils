(ns systems.casemgr.utils.channel-monitor.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [systems.casemgr.utils.channel_monitor.channel_monitor :as cm]
            [bootstrap-cljs.core :as bs :include-macros true]
            [clojure.string :as str]))

(enable-console-print!)

(println "Starting Server...")

(defonce app-state (atom {:text "Hello Chestnut!"}))

(defn root-component [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (let [publisher (:publisher (om/get-shared owner))]
        (println "root-component:did-mount")
        (put! publisher {:topic :component-msg :message :init-state :component-id :selected-daily-date :label "Select a Day" :display false})
        ))
    om/IRender
    (render [_]
      (dom/div
        nil
        (om/build cm/channel-monitor-widget {})
        ))))

(defn main []
  (let [tx-chan (chan)
        tx-pub-chan (async/pub tx-chan (fn [_] :txs))
        req-chan (chan)
        update-chan (chan)
        publisher (chan)
        publication (pub publisher #(:topic %))]
    (om/root
      root-component
      app-state
      {:shared    {:req-chan    req-chan
                   :update-chan update-chan
                   :tx-chan     tx-pub-chan
                   :publisher   publisher
                   :publication publication}
       :tx-listen (fn [tx-data root-cursor]
                    (put! tx-chan [tx-data root-cursor]))
       :target    (. js/document (getElementById "app"))})
    ))
(main)
