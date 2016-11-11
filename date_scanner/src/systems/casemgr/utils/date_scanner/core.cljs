(ns systems.casemgr.utils.date_scanner.core
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
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
        ;(dom/h1 nil "hello world")
        (om/build cm/channel-monitor-widget {})
        (om/build ds/date-scanner-widget {:component-id :selected-daily-date
                                          :display true
                                          :selected-date "20161110"})
        ))))

(defn main []
  (let [tx-chan (chan)
        tx-pub-chan (async/pub tx-chan (fn [_] :txs))
        req-chan (chan)
        update-chan (chan)
        publisher (chan)
        publication (pub publisher #(:topic %))]
    ;(state/subscribe-to-requests publication)
    (om/root
      root-component
      {}                                                    ;;state/app-state
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

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

