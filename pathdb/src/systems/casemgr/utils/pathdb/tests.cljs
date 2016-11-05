(ns ^:figwheel-always systems.casemgr.utils.pathdb.tests
  (:require
    [cljs.test :refer-macros [deftest is testing run-tests]]
    [devtools.core :as devtools]
    [cuerdas.core :as str]
    [com.rpl.specter :as s]
    [systems.casemgr.utils.pathdb.core :as pdb]
    [cljs.pprint :as pp]
    [hodgepodge.core :refer [local-storage get-item set-item remove-item clear! length]]
    [cljs.reader :as reader]
    ))

(devtools/set-pref! :install-sanity-hints true)             ; this is optional
(devtools/install!)

(enable-console-print!)

(println "<-----------------Start of tests")

;(.log js/console (range 200))

;(println "split-path:" (pdb/split-path "/Shops/shop0001/veh0001/job0001"))
;(println "split-path:" (pdb/split-path "Shops/shop0001/veh0001/job0001"))
;(.log js/console (doall (pdb/split-path "/Shops/shop0001/cust0001/veh0001/job0001")))
;(println "str/split:" (str/split "/Shops/shop0001/cust0001/veh0001/job0001" #"/"))

;(println "list-of-path-segments:" (pdb/list-of-path-segments '("/Shops/shop0001/veh0001/job0001" "/Shops/shop0001/veh0001/job0002" "Shops/shop0001/veh0001")))

(def test-data [
                {:path "/Shops"}
                {:path "/Shops/shop0001"}
                {:path "/Shops/shop0001/customers/cust0001"}
                {:path "/Shops/shop0001/customers/cust0001/veh0001/job0001" :job-id "job0001" :hours 1.0 :notes "notes.job0001"}
                {:path "/Shops/shop0001/customers/cust0001/veh0001/job0002" :job-id "job0002" :hours 3.0}
                {:path "/Shops/shop0001/customers/cust0001" :last-name "Stang" :first-name "Mark"}
                {:path "/Shops/shop0001/customers/cust0001/veh0001" :make "Ford"}
                {:path "/Shops/shop0001/customers/cust0001/veh0002" :make "VW"}
                {:path "/Shops/shop0001/customers/cust0001/veh0001/job0002/task0001" :notes "notes.1" :hours 3.5 :sched-date 20150417}
                {:path "/Shops/shop0001/customers/cust0001/veh0001/job0001/task0002" :notes "notes.2" :hours 53.5 :sched-date 20150417}
                {:path "/Shops/shop0001/customers/cust0001/veh0001/job0001/task0003" :notes "notes.3" :hours 5.5 :sched-date 20150418}
                {:path "/Shops/shop0001/customers/cust0001/veh0001/job0002/task0004" :notes "notes.4" :hours 5.0 :sched-date 20150418}
                ])
(def results (pdb/insert-rows-into-tree test-data {}))

;(pp/pprint results)
;(.log js/console results)

; todo CRUD
;(pp/pprint (pdb/select-one "/" results))
;(pp/pprint (pdb/select-one "/Shops" results))
;(pp/pprint (pdb/select-one "/Shops/shop0001/customers/cust0001/veh0001/job0001" results))
;(pp/pprint (pdb/select-one "/Shops/shop0001/cust0001/veh0002" results))
;(pp/pprint (pdb/select "Shops/shop0001/cust0001/veh0001/job0001" results))
;(pp/pprint (pdb/select "Shops/shop0001/cust0001/veh0001/job0002" results))
;(pp/pprint (pdb/select "Shops/shop0001/cust0001/veh0001" results))
;(pp/pprint (pdb/select "/Shops/shop0001/cust0001/veh0001/job0001" :job-id results))
;(pp/pprint (pdb/select "/Shops/shop0001/cust0001/veh0001/job0001" :path results))
;(pp/pprint (pdb/select "Shops/shop0001/cust0001/veh0002" results))
;(pp/pprint (pdb/delete "/Shops/shop0001/cust0001/veh0001/job0002" results))
;(pp/pprint (pdb/delete "/Shops/shop0001/cust0001/veh0002" results))
;(pp/pprint (pdb/delete "/Shops/shop0001/indices" results))
;(println "find-item-at-end-of-path" (pdb/find-item-at-end-of-path "task" results))

;; todo initial indices Task date, employee object id
(defn index-tasks [path db]
  ;(println "path:" path)
  (let [item (first (pdb/select (str path) db))
        ;_ (println "item:" item)
        sched-date (:sched-date item)
        task-path (pdb/valid-path? "task" item)]
    (when (and (not= nil sched-date) (not= nil task-path))
      ;(println "intex-tasks->contains-path?->path:" path map)
      {:sched-date sched-date :task-path task-path})
    )
  )

(defn re-index [db indices-path index-tasks]
  (let [db-minus-indices (pdb/delete indices-path db)
        items (pdb/find-items-with-path db-minus-indices)
        task-indices (into [] (sort-by :sched-date (filter #(not= nil %) (map #(index-tasks % db-minus-indices) items))))
        merged-indices (group-by :sched-date task-indices)
        results (assoc-in db-minus-indices (pdb/split-path indices-path) merged-indices)
        ]
    results
    ))

;(pp/pprint (re-index results "/Shops/shop0001/indices/task_indices" index-tasks))
;(def reindex-val (re-index results "/Shops/shop0001/indices/task_indices" index-tasks))
;(pp/pprint (get (pdb/select-one "/Shops/shop0001/indices/task_indices" reindex-val) 20150417))
;(pp/pprint (get (pdb/select-one "/Shops/shop0001/indices/task_indices" reindex-val) 20150418))

;; todo this isn't picking up the new :hours and :notes
(println "<-----------------before")
(pp/pprint results)
(def updated-db (pdb/update-db {:path     "/Shops/shop0001/customers/cust0001/veh0002/job0001"
                                :old-path "/Shops/shop0001/customers/cust0001/veh0001/job0001"
                                :job-id   "job0001"
                                :hours    4.0
                                :notes    "notes.veh0002"}
                               "/Shops/shop0001/indices/task_indices"
                               results))
(pp/pprint updated-db)

;(clear! local-storage)
;
;(println (length local-storage))
;;; => 0
;
;(set-item local-storage "foo" "bar")
;(println (length local-storage))
;;; => 1
;
;(println (get-item local-storage "foo"))
;;; => "bar"
;
;(remove-item local-storage "foo")
;(println (length local-storage))
;
;(set-item local-storage "shops" updated-db)
;(println "about to retrieve shops:")
;
;(pp/pprint (reader/read-string (get-item local-storage "shops")))

(println "<-----------------after")


;(println "<-----------------re-index")
;(pp/pprint (re-index updated-db "/Shops/shop0001/indices/task_indices" index-tasks))
;(pp/pprint (pdb/select "/Shops/shop0001/cust0001/veh0002/job0001" updated-db))

;(def drafts-test-data [
;                       {:path "/Shops/shop0001/drafts"}
;                       {:path "/Shops/shop0001/drafts/job0003" :job-id "job0003" :hours 4.0 :notes "notes.3"}
;                       {:path "/Shops/shop0001/drafts/job0003/task0014" :notes "notes.14" :hours 3.5 :sched-date 20150417}
;                       {:path "/Shops/shop0001/drafts/cust0003" :last-name "Bunny" :first-name "Bugs"}
;                       ])

;(def drafts-db (pdb/insert-rows-into-tree drafts-test-data results))
;(println "<-----------------drafts->before")
;(pp/pprint drafts-db)

;(pp/pprint (pdb/select "/Shops/shop0001/drafts/job0003" drafts-db))
;(pp/pprint (pdb/select "/Shops/shop0001/drafts/cust0003" drafts-db))

;; todo we need to be able to move from a drafts job0003 to a cust0003
;(def drafts-db-1 (pdb/move-node "/Shops/shop0001/drafts/job0003"
;                                "/Shops/shop0001/drafts/cust0003/job0003"
;                                drafts-db))

;(pp/pprint drafts-db-1)
;(pp/pprint (pdb/select "/Shops/shop0001/drafts/cust0003" drafts-db-1))

;(def drafts-db-1 (pdb/update-db {:path     "/Shops/shop0001/drafts/cust0003/job0003"
;                                 :old-path "/Shops/shop0001/drafts/job0003"
;                                 :job-id   "job0003"
;                                 :hours    5.0
;                                 :notes    "notes.cust0003"}
;                                "/Shops/shop0001/indices/task_indices"
;                                drafts-db))
;(println "<-----------------drafts-db-1->after")
;(pp/pprint drafts-db-1)

;(println "<-----------------drafts->re-index")
;(pp/pprint (re-index drafts-db-1 "/Shops/shop0001/indices/task_indices" index-tasks))
;(pp/pprint (pdb/select "/Shops/shop0001/cust0003/job0003" drafts-db-1))

;(println "<-----------------drafts-db-2->before")
;(def drafts-db-2 (pdb/move-node "/Shops/shop0001/drafts/cust0003"
;                                "/Shops/shop0001/customers/cust0003"
;                                drafts-db-1))
;(def drafts-db-2 (pdb/update-db {:path     "/Shops/shop0001/cust0003"
;                                 :old-path "/Shops/shop0001/drafts/cust0003"
;                                 :phone    "800-555-1212"}
;                                "/Shops/shop0001/indices/task_indices"
;                                drafts-db-1))
;(println "<-----------------drafts-db-2->after")
;(pp/pprint drafts-db-2)
;(pp/pprint (pdb/select-one "/Shops/shop0001/customers/cust0003" drafts-db-2))
;(pp/pprint (pdb/select-one "/Shops/shop0001/customers/cust0003/job0003" drafts-db-2))
;(pp/pprint (pdb/select-one "/Shops/shop0001/customers/cust0003/job0003/task0014" drafts-db-2))
;(println "<-----------------drafts->re-index")
;(pp/pprint (re-index drafts-db-2 "/Shops/shop0001/indices/task_indices" index-tasks))

(println "<-----------------end of tests")
