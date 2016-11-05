(ns systems.casemgr.utils.utils.common
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [cljs.core.match :refer-macros [match]]
            [om.core :as om :include-macros true]
            [bootstrap-cljs.core :as bs :include-macros true]
            ;[om-bootstrap.random :as r]
            ;[om-bootstrap.table :refer [table]]
            ;[om-bootstrap.grid :as g]
            [om-tools.dom :as d :include-macros true]
            [dommy.core :as dommy :include-macros true]
            [dommy.core :refer-macros [sel sel1]]
            [om.dom :as dom :include-macros true]
            [systems.casemgr.utils.utils.utils :as utils]
            )
  )

(defn set-owner-state!
  ([owner old-map-key new-map]
   (utils/set-owner-state! owner old-map-key new-map))
  ([owner old-map-key old-map new-map]
   (utils/set-owner-state! owner old-map-key old-map new-map)))

(defn get->e-map [owner]
  (utils/get->e-map owner))

;; header
(defn create-header [heading]
  (fn [stay owner]
    (reify
      om/IRender
      (render [_]
        (bs/page-header {} heading
                       ;(d/small "Subtext for header")
                       )))))

(defn put->job-status-filter [owner job-status]
  (let [publisher (:publisher (om/get-shared owner))]
    (put! publisher {:topic :component-msg :component-id :job-status-filter :message :change-state :job-status job-status :display true})))

(defn on-job-status-change! [owner status-id]
  (let [status (dommy/value (sel1 (str "#" status-id)))
        current-state (om/get-state owner)
        e-map (:e-map current-state)]
    ;(println "on-job-status-change:" "status-id" status-id "status:" status)
    (put->job-status-filter owner status))
  )

;(defn job-status-widget [data owner]
;  (reify
;    om/IInitState
;    (init-state [_]
;      {:e-map {:display (:display data)}})
;    om/IDidMount
;    (did-mount [_]
;      (let [component-message-events (sub (:publication (om/get-shared owner)) :component-msg (chan))]
;        (go-loop []
;                 (let [[v ch] (alts! [component-message-events])
;                       current-state (om/get-state owner)
;                       e-map (:e-map current-state)
;                       message (:message v)
;                       component-id (:component-id v)
;                       display (:display v)
;                       label (:label v)
;                       label-type (:label-type v)
;                       job-status (:job-status v)
;                       xs-size (:xs-size v)]
;                   ;(println "job-status-widget->did-mount->v:" v)
;                   (match [component-id message]
;                          [:job-status-filter :change-state] (set-owner-state! owner :e-map e-map {:job-status job-status})
;                          [:job-status-filter :init-state] (set-owner-state! owner :e-map e-map {:job-status job-status :label label :label-type label-type :xs-size xs-size :display display})
;                          [:job-status-filter :configure-state] (set-owner-state! owner :e-map e-map {:display display})
;                          :else
;                          ;(println "job-status-widget->else->v:" v)
;                          :no-match
;                          )
;                   (recur)))))
;    om/IRenderState
;    (render-state [_ {:keys [e-map]}]
;      ;(println "job-status-widget->render-state->e-map:" e-map)
;      (let [job-status (:job-status e-map)]
;        (g/col {:style  (utils/display (:display e-map))
;                :xs     (if (:xs-size e-map) (:xs-size e-map) 1)
;                :offset 0}
;               (if (= (:label-type e-map) :header)
;                 (d/h5 (d/b (:label e-map)))
;                 (r/label {:bs-size "small" :bs-style "primary"} (:label e-map)))
;               (utils/create-job-status owner "job-status-filter" job-status on-job-status-change!)
;               )))))

(defn end-edit [text owner cb]
  (om/set-state! owner :editing false)
  (cb text))

(defn handle-change [e data edit-key owner]
  (om/transact! data edit-key (fn [_] (.. e -target -value))))

(defn editable [data owner {:keys [edit-key on-edit] :as opts}]
  (reify
    om/IInitState
    (init-state [_]
      {:editing false})
    om/IRenderState
    (render-state [_ {:keys [editing]}]
      (let [text (get data edit-key)]
        (dom/li nil
                (dom/span #js {:style (utils/display (not editing))} text)
                (dom/input
                  #js {:style      (utils/display editing)
                       :value      text
                       :onChange   #(handle-change % data edit-key owner)
                       :onKeyPress #(when (== (.-keyCode %) 13)
                                     (end-edit text owner on-edit))
                       :onBlur     (fn [e]
                                     (when (om/get-state owner :editing)
                                       (end-edit text owner on-edit)))})
                (dom/button
                  #js {:style   (utils/display (not editing))
                       :onClick #(om/set-state! owner :editing true)}
                  "Edit"))))))

(defn date-input [stay-key]
  "Date component"
  (fn [stay owner]
    (reify
      om/IDidMount
      (did-mount
        [_]
        ;(println "date-input->did-mount")
        (->> (om/get-node owner)
             (js-obj "onSelect" #(this-as t (put! (:publisher (om/get-shared owner))
                                                  {:topic        :component-msg
                                                   :message      :change-state
                                                   :component-id (:component-id stay)
                                                   :date         (.format (.getMoment t) "YYYY-MM-DD")}))
                     "format" "YYYY-MM-DD"
                     "minDate" (js/Date. 2009 0 1)
                     "field")
             (js/Pikaday.)))
      om/IRender
      (render [_]
        (let [date (get stay stay-key)
              moment-date (js/moment date)
              formatted-date (.format (js/moment date) "YYYY-MM-DD")]
          ;(println "date-input: stay-key:" stay-key " stay:" stay " date:" date " moment-date:" moment-date " formatted-date:" formatted-date)
          (dom/input #js {:value (.format (js/moment date) "YYYY-MM-DD")}))))))

;(defn column-input-date [data owner]
;  "column input date"
;  (reify
;    om/IInitState
;    (init-state [_]
;      {:e-map {:display      (:display data)
;               :component-id (:component-id data)}})
;    om/IDidMount
;    (did-mount [_]
;      (let [component-message-events (sub (:publication (om/get-shared owner)) :component-msg (chan))]
;        (go-loop []
;                 (let [[v ch] (alts! [component-message-events])
;                       current-state (om/get-state owner)
;                       e-map (:e-map current-state)
;                       message (:message v)
;                       component-id (:component-id v)
;                       display (:display v)
;                       label (:label v)
;                       date (:date v)
;                       label-type (:label-type v)
;                       xs-size (:xs-size v)]
;                   (if (= (:component-id e-map) component-id)
;                     (cond
;                       (= :change-state message) (om/set-state! owner {:e-map (merge e-map {:date date})})
;                       (= :init-state message) (om/set-state! owner {:e-map (merge e-map {:display display :date date :label label :label-type label-type :xs-size xs-size})})
;                       (= :configure-state message) (om/set-state! owner {:e-map (merge e-map {:display display})})))
;                   (recur)))))
;    om/IRenderState
;    (render-state [_ {:keys [e-map]}]
;      ;(println "column-input-date->render-state->e-map:" e-map)
;      (g/col {:style  (utils/display (:display e-map))
;              :xs     (if (:xs-size e-map) (:xs-size e-map) 1)
;              :offset 0}
;             (if (= (:label-type e-map) :header)
;               (d/h5 (d/b (:label e-map)))
;               (r/label {:bs-size "small" :bs-style "primary"} (:label e-map)))
;             (om/build (date-input :date) e-map)))))
