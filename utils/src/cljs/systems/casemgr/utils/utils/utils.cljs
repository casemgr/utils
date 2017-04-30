(ns systems.casemgr.utils.utils.utils
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [cljs.core.match :refer-macros [match]])
  (:require [cljs.core.async :as async :refer [chan <! >! close! timeout pub sub unsub unsub-all put! alts!]]
            [cljs.reader :as reader]
            [bootstrap-cljs.core :as bs :include-macros true]
            [om.dom :as dom :include-macros true]

            ;[om-bootstrap.table :refer [table]]
            ;[om-bootstrap.input :as i]

            [om-tools.dom :as d :include-macros true]
            [dommy.core :as dommy :include-macros true]
            [dommy.core :refer-macros [sel sel1]]
            [cuerdas.core :as str]
            [cljs-uuid-utils.core :as uuid]
            [om.core :as om]))

(enable-console-print!)

;(declare get-vehicle)

(defn set-value!
  "Set the value of `elem` to `value` if not nil"
  [elem value]
  (if (not= nil elem)
    (dommy/set-value! elem value)))

;; console utils
(defn ->prn [x]
  (js/console.log x)
  x)

(defn log [s]
  (.log js/console (str s)))


(defn get->e-map [owner]
  (let [current-state (om/get-state owner)
        e-map (:e-map current-state)]
    e-map))

(defn set-owner-state!
  ([owner old-map-key new-map]
   (om/set-state! owner {old-map-key new-map}))
  ([owner old-map-key old-map new-map]
   (om/set-state! owner {old-map-key (merge old-map new-map)}))
  )

;; localStorage persistence
(defn store [k obj]
  (.setItem js/localStorage k (js/JSON.stringify (clj->js obj))))

(defn keywordify [m]
  (cond
    (map? m) (into {} (for [[k v] m] [(keyword k) (keywordify v)]))
    (coll? m) (vec (map keywordify m))
    :else m))

(defn fetch [k default]
  (let [item (.getItem js/localStorage k)]
    (if item
      (-> (.getItem js/localStorage k)
          (or (js-obj))
          (js/JSON.parse)
          (js->clj)
          (keywordify))
      default)))

;; Miscellaneous Utils
(defn if-not-equal-return-new [old new]
  ;(println "old:" old "new:" new)
  (if (= (str old) (str new)) nil new))

(defn display [show]
  (if show
    #js {}
    #js {:display "none"}))

(defn onKeyPress [evt]
  (let [charCode (.-charCode evt)]
    charCode))

(defn list-contains? [coll value]
  (let [s (seq coll)]
    (if s
      (if (= (first s) value) true (recur (rest s) value))
      false)))

(defn match-event?
  ([v topic]
   (= topic (:topic v)))
  ([v topic component-id]
   (and (match-event? v topic)
        (= component-id (:component-id v))))
  ([v topic component-id messages]
   (and (match-event? v topic component-id)
        (list-contains? messages (:message v)))))

(defn checked? [id]
  (let [element (.getElementById js/document id)]
    ;(println "in checked?" id element)
    (if element (.-checked element) false)))

(defn check-the-box [id value]
  (let [element (.getElementById js/document id)]
    ;(println "in checked-the-box:" id element)
    (if element (set! (.-checked element) value))
    ))

(defn footer-text []
  "Copyright \u00a9 2014-2017 Case Manager Systems - All Rights Reserved")

;; used for testing what is passed to on-change function
(defn on-change
  ([id]
   (println (str "on-change: " id " selected:"
                 ;(dommy/value (sel1 (str "#" id)))
                 )))
  ([id app-state]
   (println (str "on-change: " id " selected:"
                 ;(dommy/value (sel1 (str "#" id)))
                 )))
  )

(defn cancel-edit [id app-state]
  (-> (sel1 id) (dommy/set-style! :display "none")))

(defn id [rowdata]
  (:id rowdata))

(def ALPHA_NUMERIC_STRING "abcdefghijklmnopqrstuvwxyz0123456789-_")

(defn get-random-alphanumeric []
  (. ALPHA_NUMERIC_STRING charAt (rand-int (.-length ALPHA_NUMERIC_STRING))))

(defn create-unique-name [prefix]
  (if (= prefix "cust")
    (str prefix (uuid/uuid-string (uuid/make-random-uuid)))
    (str prefix (str/join (for [_ (range 0 5)] (get-random-alphanumeric))))))

(defn get-sub-path [path item]
  (when item
    (let [location (. path lastIndexOf item)
          location-plus-one (+ location 1)
          next-slash (. path indexOf "/" location-plus-one)
          sub-string (if (= next-slash -1) path (. path substring 0 next-slash))
          ]
      ;(println path location location-plus-one next-slash)
      sub-string)))

(defn extract-id-by-type [type full-path]
  (when full-path
    (let [path (get-sub-path full-path type)
          last-slash (. path lastIndexOf "/")
          ;_ (println "path:" path "last-slash:" last-slash)
          ;_ (println "length:" (.-length path))
          sub-string (if (= last-slash -1) path (. path substring (+ last-slash 1) (.-length path)))]
      sub-string)))

;; Generic Input
;(defn create-input
;  ([id default-value placeholder]
;    ;(println "id:" id "default-value:" default-value "placeholder:" placeholder)
;   (i/input {:id            id
;             :feedback?     false
;             :type          "text"
;             :default-value default-value
;             :value         default-value
;             :placeholder   placeholder}))
;  ([id default-value label placeholder]
;   (i/input {:id                id
;             :feedback?         false
;             :type              "text"
;             :default-value     default-value
;             :label             label
;             :placeholder       placeholder
;             :group-classname   "group-class"
;             :wrapper-classname "wrapper-class"
;             :label-classname   "label-class"})))

;(defn create-input-ro
;  ([id default-value placeholder]
;   (i/input {:id          id
;             :feedback?   false
;             :type        "text"
;             :value       default-value
;             :placeholder placeholder}))
;  ([id default-value label placeholder]
;   (i/input {:id                id
;             :feedback?         false
;             :type              "text"
;             :value             default-value
;             :label             label
;             :placeholder       placeholder
;             :group-classname   "group-class"
;             :wrapper-classname "wrapper-class"
;             :label-classname   "label-class"})))
