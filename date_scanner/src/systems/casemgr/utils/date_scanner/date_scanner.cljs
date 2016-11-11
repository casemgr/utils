(ns systems.casemgr.utils.date_scanner.date_scanner
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [bootstrap-cljs.core :as bs :include-macros true]
            [systems.casemgr.utils.utils.utils :as utils]
            [systems.casemgr.utils.utils.common :as common]
            [systems.casemgr.utils.utils.calendar :as cal]
            [cuerdas.core :as str]
            [cljs.pprint :as pp]
            ))

(defn create-date [day date_parts]
  (let [year (:current-year date_parts)
        current-month (+ (:current-month date_parts) 1)
        month (str/pad (str current-month) {:length 2 :padding "0" :type :left})
        str-day (str/pad (str day) {:length 2 :padding "0" :type :left})
        date (str year "-" month "-" str-day)]
    date))

(defn put!->change-state [evt publisher component-id day date_parts]
  (if-not (utils/list-contains? cal/cal-days-labels day)
    (let [date (create-date day date_parts)]
      (put! publisher {:topic :component-msg :component-id component-id :message :change-state :date date})))
  (. evt stopPropagation))

(defn put!->set-hover-date [evt publisher component-id day date_parts]
  (if-not (utils/list-contains? cal/cal-days-labels day)
    (let [date (create-date day date_parts)]
      (put! publisher {:topic :component-msg :component-id component-id :message :change-hover-date :date date})))
  (. evt stopPropagation))

(defn put!->reset-hover-date [evt publisher component-id day date_parts]
  (if-not (utils/list-contains? cal/cal-days-labels day)
    (put! publisher {:topic :component-msg :component-id component-id :message :change-hover-date :date nil}))
  (. evt stopPropagation))

(defn create-style [day date_parts hover-date_parts]
  (let [selected-date (str (:current-date date_parts))
        hover-date (str (:current-date hover-date_parts))
        background-color (cond
                           (= day selected-date) "cyan"
                           (= day hover-date) "yellow"
                           :else "white")]
    {:text-align "center" :background-color background-color :color "black"}))

(defn format-row-for-tbody [week date_parts hover-date_parts owner]
  ;(println "format-row-for-tbody->week:" week)
  (let [publisher (:publisher (om/get-shared owner))
        e-map (:e-map (om/get-state owner))
        component-id (:component-id e-map)
        count (+ (count week) 1)]
    (dom/tr
      (if (< 1 count)
        (let [day (nth week 0)]
          (dom/td {:style       (create-style day date_parts hover-date_parts)
                   :onClick     (fn [evt] (put!->change-state evt publisher component-id day date_parts))
                   :onMouseOver (fn [evt] (put!->set-hover-date evt publisher component-id day date_parts))
                   :onMouseOut  (fn [evt] (put!->reset-hover-date evt publisher component-id day date_parts))
                   } day)))
      (if (< 2 count)
        (let [day (nth week 1)]
          (dom/td {:style       (create-style day date_parts hover-date_parts)
                   :onClick     (fn [evt] (put!->change-state evt publisher component-id day date_parts))
                   :onMouseOver (fn [evt] (put!->set-hover-date evt publisher component-id day date_parts))
                   :onMouseOut  (fn [evt] (put!->reset-hover-date evt publisher component-id day date_parts))
                   } day)))
      (if (< 3 count)
        (let [day (nth week 2)]
          (dom/td {:style       (create-style day date_parts hover-date_parts)
                   :onClick     (fn [evt] (put!->change-state evt publisher component-id day date_parts))
                   :onMouseOver (fn [evt] (put!->set-hover-date evt publisher component-id day date_parts))
                   :onMouseOut  (fn [evt] (put!->reset-hover-date evt publisher component-id day date_parts))
                   } day)))
      (if (< 4 count)
        (let [day (nth week 3)]
          (dom/td {:style       (create-style day date_parts hover-date_parts)
                   :onClick     (fn [evt] (put!->change-state evt publisher component-id day date_parts))
                   :onMouseOver (fn [evt] (put!->set-hover-date evt publisher component-id day date_parts))
                   :onMouseOut  (fn [evt] (put!->reset-hover-date evt publisher component-id day date_parts))
                   } day)))
      (if (< 5 count)
        (let [day (nth week 4)]
          (dom/td {:style       (create-style day date_parts hover-date_parts)
                   :onClick     (fn [evt] (put!->change-state evt publisher component-id day date_parts))
                   :onMouseOver (fn [evt] (put!->set-hover-date evt publisher component-id day date_parts))
                   :onMouseOut  (fn [evt] (put!->reset-hover-date evt publisher component-id day date_parts))
                   } day)))
      (if (< 6 count)
        (let [day (nth week 5)]
          (dom/td {:style       (create-style day date_parts hover-date_parts)
                   :onClick     (fn [evt] (put!->change-state evt publisher component-id day date_parts))
                   :onMouseOver (fn [evt] (put!->set-hover-date evt publisher component-id day date_parts))
                   :onMouseOut  (fn [evt] (put!->reset-hover-date evt publisher component-id day date_parts))
                   } day)))
      (if (< 7 count)
        (let [day (nth week 6)]
          (dom/td {:style       (create-style day date_parts hover-date_parts)
                   :onClick     (fn [evt] (put!->change-state evt publisher component-id day date_parts))
                   :onMouseOver (fn [evt] (put!->set-hover-date evt publisher component-id day date_parts))
                   :onMouseOut  (fn [evt] (put!->reset-hover-date evt publisher component-id day date_parts))
                   } day)))
      )))

(defn format-rows-for-tbody [view date_parts hover-date_parts owner]
  (loop [results []
         days view]
    (let [a-week (take 7 days)
          formatted-row (if (not (empty? a-week)) (format-row-for-tbody a-week date_parts hover-date_parts owner) nil)]
      (if (empty? days) results
                        (recur (conj results formatted-row) (drop 7 days)))))
  )

(defn format-tbody [view date_parts hover-date_parts owner]
  ;(println "format-tbody:" view)
  (dom/tbody
    (format-rows-for-tbody view date_parts hover-date_parts owner)))

(defn add-days [old-vector number]
  (loop [results old-vector
         count 0]
    (let [new-count (+ count 1)
          str-count (str new-count)]
      (if (> new-count number) results
                               (recur (conj results str-count) new-count)))))

(defn add-spaces [old-vector number]
  (loop [results old-vector
         count 0]
    (let [new-count (+ count 1)]
      (if (> new-count number) results
                               (recur (conj results " ") new-count)))))

(defn- build-view [date_parts]
  (let [day_labels cal/cal-days-labels
        starting-day (:starting-day date_parts)
        spaces-view (add-spaces day_labels starting-day)
        days-view (add-days spaces-view (:month-length date_parts))
        ]
    days-view))

(defn put!->init-state [evt publisher component-id day date_parts]
  (if-not (utils/list-contains? cal/cal-days-labels day)
    (let [date (create-date day date_parts)]
      (put! publisher {:topic :component-msg :component-id component-id :message :init-state :date date})
      ))
      (. evt stopPropagation))

(defn previous-month [evt date_parts owner]
  (let [publisher (:publisher (om/get-shared owner))
        e-map (:e-map (om/get-state owner))
        component-id (:component-id e-map)
        date (cal/fmt-date-iso (. (:parsed-date date_parts) subtract 1 "month"))]
    (put! publisher {:topic :component-msg :component-id component-id :message :change-state :date date})
    (println "prev" date)
    (. evt stopPropagation)))

(defn next-month [evt date_parts owner]
  (let [publisher (:publisher (om/get-shared owner))
        e-map (:e-map (om/get-state owner))
        component-id (:component-id e-map)
        date (cal/fmt-date-iso (. (:parsed-date date_parts) add 1 "month"))]
    (put! publisher {:topic :component-msg :component-id component-id :message :change-state :date date})
    (println "next" date)
    (. evt stopPropagation)))

(defn process-scheduler-date-message [owner v message]
  (let [e-map (:e-map (om/get-state owner))]
    (cond
      (= message :init-state) (common/set-owner-state! owner :e-map e-map {:selected-date (:date v)})
      (= message :configure-state) (common/set-owner-state! owner :e-map e-map {:display (:display v)})
      (= message :change-state) (common/set-owner-state! owner :e-map e-map {:selected-date (:date v)})
      (= message :change-hover-date) (common/set-owner-state! owner :e-map e-map {:hover-date (:date v)})
      :else (println "process-scheduler-date-message->v:" v)
      )
    ))

(defn date-scanner-widget [data owner]
  "This is the calendar that users will use to pick a date"
  (reify
    om/IInitState
    (init-state [_]
      {:e-map {:display       (:display data)
               :component-id  (:component-id data)
               :selected-date (:selected-date data)
               :hover-date    nil}})
    om/IDidMount
    (did-mount [_]
      (println "date-scanner-widget: did-mount")
      (let [component-message-events (sub (:publication (om/get-shared owner)) :component-msg (chan))]
        (go-loop []
                 (let [[v ch] (alts! [component-message-events])
                       topic (:topic v)
                       message (:message v)
                       component-id (:component-id v)
                       e-map->component-id (:component-id (:e-map (om/get-state owner)))
                       ]
                   ;(println "date_scanner->v:" v)
                   (cond
                     (and (= topic :component-msg) (= component-id e-map->component-id)) (process-scheduler-date-message owner v message)
                     :else nil
                     )
                   (recur)))))
    om/IRenderState
    (render-state [_ {:keys [e-map]}]
      (let [view nil
            selected-date (:selected-date e-map)
            selected-date_parts (if selected-date (cal/convert-date-to-parts selected-date))
            hover-date (:hover-date e-map)
            hover-date_parts (if hover-date (cal/convert-date-to-parts hover-date))
            ]
        (println "date_scanner->render-state->e-map:" e-map)
        ;(println "date_scanner->render-state->selected-date_parts:" selected-date_parts)
        ;(println "date_scanner->render-state->hover-date_parts:" hover-date_parts)
        (bs/table {:class "table-bordered calendar-table table-condensed table-hover" :display (if (not (:display e-map)) "none")}
                  ;(table {:style #js {:display (if (not (:display e-map)) "none")}
                  ;        :class "calendar-table" :bordered? true :condensed? true :hover? true
                  ;        ;:striped? true
                  ;        }
                  (dom/thead nil
                             (dom/th {:col-span 7
                                      :style    {:text-align       "center"
                                                 :background-color "cyan"}}
                                     (bs/button {:onClick (fn [evt] (previous-month evt selected-date_parts owner)) :bs-size "xsmall"}
                                                ;(dom/span {:class "glyphicon glyphicon-arrow-left"})
                                                "prev") " "
                                     (:month-name selected-date_parts) " " (:current-year selected-date_parts) " "
                                     (bs/button {:onClick (fn [evt] (next-month evt selected-date_parts owner)) :bs-size "xsmall"}
                                                ;(bs/glyphicon {:glyph "arrow-right"})
                                                "next")
                                     ))
                  (format-tbody (build-view selected-date_parts) selected-date_parts hover-date_parts owner)
                  )
        ))))
