(ns systems.casemgr.utils.channel_monitor.channel_monitor
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as str]
            [bootstrap-cljs.core :as bs :include-macros true]
            )
  )

(defn all [v]
  (println "cm->all:" v)
  )

(defn channel-monitor-widget [data owner]
  (reify
    om/IInitState
    (init-state [_])
    om/IDidMount
    (did-mount [_]
      (let [shared (om/get-shared owner)
            publication (:publication shared)
            tab-events (sub publication :tabs (chan))
            component-msg-events (sub publication :component-msg (chan))
            server-events (sub publication :server-response (chan))
            dispatcher-events (sub publication :dispatcher (chan))
            internal-db-events (sub publication :internal-db (chan))
            ]
        (println "channel-monitor-widget:did-mount")
        (go-loop []
                 (let [[v ch] (alts! [dispatcher-events tab-events component-msg-events server-events internal-db-events])
                       topic (:topic v)]
                   (cond
                     (= topic :server-response) (println "cm->server-response:" v)
                     (= topic :component-msg) (println "cm->component-msg:" v)
                     (= topic :dispatcher) (println "cm->dispatcher:" v)
                     (= topic :internal-db) (println "cm->internal-db:" v)
                     :else
                     ;(all v)
                     nil
                     )
                   (recur)))))
    om/IRenderState
    (render-state [_ {:keys [e-map]}]
      (dom/div
        nil
        ;; so, how about we send a message with a message assigned to
        ;; them so we can use them for testing
        ;(map #(bs/button {:bs-style %} (str/capitalize %))
        ;     ["default" "primary" "success" "info" "warning" "danger"])
        ))))
