(ns systems.casemgr.utils.utils.calendar
  (:require [cljs.reader :as reader]
            [cljs-time.format :as format]
            [cljs-time.local :as local]
            [cuerdas.core :as str]))

(defn str->int
  [s]
  (if (number? s)
    s
    (when (and (string? s)
               (re-find #"^[0-9]+$" s))
      (reader/read-string s))))

;; date utils

;; these are labels for the days of the week
(def cal-days-labels ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"])

;; these are human-readable month name labels, in order
(def cal-months-labels ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"])

;; these are the number of days in a month, except for leap-year, which we calculate
(def cal-days-in-month [31 28 31 30 31 30 31 31 30 31 30 31])

(def basic-date-time (format/formatter "yyyyMMdd'T'HHmmss.SSSZ"))

(def custom-formatter (format/formatter "yyyy-MM-dd"))

(def custom-formatter-yyyyMMdd (format/formatter "yyyyMMdd"))

(def custom-formatter-timehhmmampm (format/formatter "h:mm A"))

(defn fmt-date [d]
  (.format (js/moment d) "MMMM Do, YYYY"))

(defn fmt-date-iso [d]
  (.format (js/moment d) "YYYY-MM-DD"))

(defn fmt-date-YYYYMMDD [d]
  (.format (js/moment d) "YYYYMMDD"))

(defn fmt-time [t]
  (.format (js/moment t) "h:mm A"))

(defn convert-date-yyyyMMdd->yyyy-MM-dd [date]
  ;(println "utils/date:" date)
  (let [parsed-date (format/parse custom-formatter-yyyyMMdd (str date))]
    ;(println "utils/parsed-date:" parsed-date)
    (format/unparse custom-formatter parsed-date)))

(defn convert-date-yyyy-MM-dd->yyyyMMdd [date]
  (format/unparse custom-formatter-yyyyMMdd (format/parse custom-formatter date)))

(defn tasks=date [hover-date task]
  ;(println "task:" task)
  (= (:date task) (str->int (fmt-date-YYYYMMDD hover-date))))

(defn get-now []
  (format/unparse custom-formatter (local/local-now)))

(defn calc-number-of-days-in-month [current-year current-month]
  "Check for leap-year, otherwise it comes from the list"
  (if (and
        (= current-month 1)
        (or
          (and
            (= (mod current-year 4) 0)
            (not (= (mod current-year 100) 0)))
          (= (mod current-year 400) 0)))
    29
    (get cal-days-in-month current-month)))

(defn convert-date-to-parts [date]
  (let [;_ (println "test->date:" date)
        ;_ (println "test->moment-date:"  (js/moment date))
        parsed-date (js/moment date "YYYY-MM-DD")
        ;_ (println "isValid:" (. parsed-date isValid))
        current-year (. parsed-date year)
        current-month (. parsed-date month)
        current-date (. parsed-date date)
        current-day (. parsed-date day)
        day-of-week (get cal-days-labels current-day)
        first-day (js/Date. current-year current-month 1)
        starting-day (. first-day getDay)
        month-length (calc-number-of-days-in-month current-year current-month)
        month-name (get cal-months-labels current-month)]
    ;(println "test->values:" parsed-date current-year current-month current-date current-day day-of-week (get cal-days-labels starting-day) month-length month-name)
    {:parsed-date   parsed-date
     :current-year  current-year
     :current-month current-month
     :current-date  current-date
     :current-day   current-day
     :day-of-week   day-of-week
     :first-day     first-day
     :starting-day  starting-day
     :month-length  month-length
     :month-name    month-name}))

(defn create-date-from-parts [year month day]
  (str year (str/pad (str month) {:length 2 :padding "0" :type :left}) (str/pad (str day) {:length 2 :padding "0" :type :left})))

(defn create-first-of-the-month-date [date-parts]
  (let [current-year (:current-year date-parts)
        current-month (+ 1 (:current-month date-parts))]
    (create-date-from-parts current-year current-month 1)
    ))

(defn create-end-of-the-month-date [date-parts]
  (let [current-year (:current-year date-parts)
        current-month (+ 1 (:current-month date-parts))
        month-length (:month-length date-parts)]
    (create-date-from-parts current-year current-month month-length)
    ))

(defn calc-start-and-end-dates [query-type date]
  ;(println "calc-start-and-end-dates:" query-type date)
  (let [date-parts (convert-date-to-parts date)
        current-day (:current-day date-parts)
        parsed-date (:parsed-date date-parts)
        current-year (:current-year date-parts)
        current-month (+ 1 (:current-month date-parts))]
    ;(println "calc-start-and-end-dates->date-parts:" date-parts)
    (case query-type
      :daily {:start-date (fmt-date-YYYYMMDD parsed-date)
              :end-date   (fmt-date-YYYYMMDD parsed-date)}
      :scheduler {:start-date (create-first-of-the-month-date date-parts)
                  :end-date   (create-end-of-the-month-date date-parts)}
      :weekly {:start-date (fmt-date-YYYYMMDD (. (:parsed-date date-parts) subtract current-day "days"))
               :end-date   (fmt-date-YYYYMMDD (. (:parsed-date date-parts) add 6 "days"))}
      :jobs {:start-date (create-first-of-the-month-date date-parts)
             :end-date   (create-end-of-the-month-date date-parts)})))

(defn calc-date-range [type date]
  (let [dates (calc-start-and-end-dates type date)
        start-date (:start-date dates)
        start (str->int start-date)
        start-date-parts (convert-date-to-parts start-date)
        end-date (:end-date dates)
        end (str->int end-date)
        end-date-parts (convert-date-to-parts end-date)
        ]
    (if (= (:current-month start-date-parts) (:current-month end-date-parts)) (doall (range start (+ 1 end)))
                                                                              (let [end-of-the-month (str->int (create-end-of-the-month-date start-date-parts))
                                                                                    first-of-the-month (str->int (create-first-of-the-month-date end-date-parts))]
                                                                                (doall (flatten (list (range start (+ 1 end-of-the-month)) (range first-of-the-month (+ 1 end)))))))))


