(ns systems.casemgr.utils.date_scanner.core
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [systems.casemgr.utils.utils.main :as main]
            [systems.casemgr.utils.channel_monitor.channel_monitor :as cm]
            [systems.casemgr.utils.date_scanner.date_scanner :as ds]
            ))

(enable-console-print!)

(defn root-component [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (let [publisher (:publisher (om/get-shared owner))]
        (println "root-component:did-mount")
        ))
    om/IRender
    (render [_]
      (dom/div
        nil
        (om/build cm/channel-monitor-widget {})
        (om/build ds/date-scanner-widget {:component-id :selected-daily-date
                                          :display true
                                          :selected-date "20171014"})
        ))))

(main/main root-component)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

