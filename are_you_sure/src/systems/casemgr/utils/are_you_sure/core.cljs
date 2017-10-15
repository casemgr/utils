(ns systems.casemgr.utils.are_you_sure.core
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [systems.casemgr.utils.utils.main :as main]
            [systems.casemgr.utils.channel_monitor.channel_monitor :as cm]
            [systems.casemgr.utils.are_you_sure.are_you_sure :as ays]
            ))

(enable-console-print!)

(defn delete-job [owner component-id e-map]
  (let [publisher (:publisher (om/get-shared owner))
        path (:path e-map)]
    (put! publisher {:topic            :component-msg
                     :original-topic   :server-msg
                     :component-id     :are_you_sure
                     :original-comp-id :job
                     :message          :are-you-sure?
                     :original-msg     :delete
                     :display          true
                     :dialog-text      "Are you sure you want to delete this Work Order, this can't be undone"
                     :params           {:path path}})
    ))

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
        (om/build ays/are-you-sure-widget {})
        (delete-job owner "" {:path "test/path"})
        ))))

(main/main root-component)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

