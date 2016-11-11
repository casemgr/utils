(ns scheduler_server.validators
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [cljs.core.match :refer-macros [match]]
            [cljs.reader :as reader]
            [cuerdas.core :as str]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [systems.casemgr.utils.utils.calendar :as cal]))

(def hours-validator
  {:hours [v/number v/positive]})

(defn str->decnum
  [s]
  (if (number? s)
    s
    (when (and (string? s)
               (re-find #"^([0-9]+)?(\.[0-9]{1,2})?$" s))
      (reader/read-string s))))

(defn str->year
  [s]
  (if (number? s)
    s
    (when (and (string? s)
               (re-find #"^([0-9]{4})$" s))
      (reader/read-string s))))

(defn validate-date
  [s]
  (if (not= nil s) (if (re-find #"^([0-9]{4}-[0-9]{2}-[0-9]{2})$" s) true false) false))

  (defn validate-priority [priority]
    (let [str->int (cal/str->int priority)]
      ;(println "priority:" priority "str->int:" str->int)
      (if (or (str/blank? priority) (= nil priority))
        [nil]
        (if str->int
          (b/validate {:priority str->int}
                      :priority [v/number])
          [{:priority "Priority is not in the correct format (0, 1, 2, 3...)"}]
          )
        )))

  (defn validate-hours [hours]
    (let [str->decnum (str->decnum hours)]
      ;(println "hours:" hours "str->decnum:" str->decnum)
      (if (or (str/blank? hours) (= nil hours))
        [nil]
        (if str->decnum
          (b/validate {:hours str->decnum}
                      :hours [v/number])
          [{:hours "Hours is not in the correct format (.5, 0.5, 1.50, 99)"}]
          )
        )))

  (defn validate-year [year]
    (let [str->year (str->year year)]
      ;(println "year:" year "str->year:" str->year)
      (if (or (str/blank? year) (= nil year))
        [{:year "Year is required (2015)"}]
        (if str->year
          (b/validate {:year str->year}
                      :year [v/number v/positive])
          [{:year "Year is not in the correct format (2015)"}]
          )
        )))
