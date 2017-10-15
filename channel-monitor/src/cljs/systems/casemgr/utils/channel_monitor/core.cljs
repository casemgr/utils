(ns systems.casemgr.utils.channel-monitor.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [systems.casemgr.utils.utils.main :as main]
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

(main/main root-component)
