(ns ^:figwheel-always systems.casemgr.utils.pathdb.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.walk :as walk]
            [cuerdas.core :as str]
            [com.rpl.specter :as s]
            [devtools.core :as devtools]
            [cljs.pprint :as pp]
            ))

(devtools/set-pref! :install-sanity-hints true)             ; this is optional
(devtools/install!)

(enable-console-print!)

;(println "<-----------------Start of core")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text ""}))

(defn length>0 [string]
  (> (.-length string) 0))

(defn split-path [path]
  (filter length>0 (str/split path #"/")))

(defn path-selector [path]
  (s/comp-paths (mapv s/keypath (split-path path))))

(defn list-of-path-segments [paths]
  ;(map #(filter length>0 %) (map #(str/split % #"/") paths))
  (map split-path paths)
  )

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

(defn valid-path? [prefix map]
  ;(println "valid-path?->map:" map)
  (let [path (:path map)]
    (when path
      ;(println "valid-path?->path:" path)
      (let [list-of-path-segments (first (list-of-path-segments (list path)))
            ;_ (println "valid-path?->list-of-path-segments:" list-of-path-segments)
            last (last list-of-path-segments)
            ;_ (println "valid-path?->last:" last)
            ]
        (when
          (. last startsWith prefix)
          path)
        )
      )
    )
  )

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn merge-nodes [current-node new-data]
  ;(println "current-node:" current-node "new-data:" new-data)
  (merge current-node new-data)
  )

(defn insert-node-into-tree [current-state node]
  ;(println "insert-node-into-tree->node:" node)
  (if node
    (let [path (:path node)
          path-segments (split-path path)
          ;path-segment list-of-path-segments
          ;_ (println "list-of-path-segments:" list-of-path-segments)
          ;last-segment (last list-of-path-segments)
          new-state (update-in current-state path-segments merge-nodes node)
          ]
      new-state
      )
    current-state
    )
  )

(defn insert-rows-into-tree [rows current-state]
  ;(println "current-state:" current-state "rows:" rows)
  (loop [db current-state
         rows rows]
    (let [row (first rows)
          ;_ (println "rows:" rows)
          new-state (insert-node-into-tree db row)
          ]
      (if (empty? rows)
        new-state
        (recur new-state (rest rows))
        )
      )
    )
  )

(defn select-one [path tree]
  (s/select-one (path-selector path) tree))

(defn select
  ([path tree]
   (s/select (path-selector path) tree))
  ([path keyword tree]
   (s/select [(mapv s/keypath (split-path path)) keyword] tree)
    )
  )

(defn find-item-at-end-of-path [item tree_database]
  (let [results (atom #{})]
    (walk/postwalk #(do (if-let [valid-path (valid-path? item %)] (swap! results conj valid-path)) %) tree_database)
    (vec @results))
  )

;; TODO look for indices and other places where these might exist
(defn delete [path tree]
  (dissoc-in tree (split-path path))
  )

(defn starts-with-path? [search-path node]
  ;(println "contains-path?->node:" node)
  (let [path (:path node)]
    (when (str/starts-with? path search-path)
      ;(println "contains-path?->path:" path node)
      path
      )
    )
  )

(defn find-item-that-starts-with-path [item tree]
  (let [results (atom #{})]
    (walk/postwalk #(do (if-let [contains-path (starts-with-path? item %)] (swap! results conj contains-path)) %) tree)
    (vec @results))
  )

;(defn testfn [data]
;  (println "data:" data))

;(pp/pprint
;  (walk/postwalk #(do (println "visiting:" %) %) results)
;)

(defn has-path? [node tree]
  ;(println "contains-path?->node:" node)
  (let [path (:path node)]
    (when (not= nil path)
      ;(println "contains-path?->path:" path node)
      path
      )
    )
  )

(defn find-items-with-path [tree]
  (let [results (atom #{})]
    (walk/postwalk #(do (if-let [contains-path (has-path? % tree)] (swap! results conj contains-path)) %) tree)
    (vec @results))
  )

(defn update-path-in-item [new-path item]
  ;(println "update-path-in-item->path:" (:path item))
  (assoc item :path new-path)
  )

(defn transform-paths-in-tree [old-path new-path tree]
  ;(println "old-path:" old-path "new-path:" new-path)
  (loop [current-state tree
         incorrect-paths (find-item-that-starts-with-path old-path tree)]
    ;incorrect-path: /Shops/shop0001/drafts/job0003/task0014
    (let [incorrect-path (first incorrect-paths)
          ;_ (println "incorrect-path:" incorrect-path)
          path-to-node (str/replace-first incorrect-path old-path new-path)
          ;_ (println "path-to-node:" path-to-node)
          new-state (if incorrect-path (s/transform [(mapv s/keypath (split-path path-to-node))]
                                                    (partial update-path-in-item path-to-node)
                                                    current-state
                                                    ))
          ;_ (if incorrect-path (pp/pprint new-state))
          ]
      (if (empty? incorrect-paths)
        current-state
        (recur new-state (rest incorrect-paths))
        )
      )
    )
  )

(defn move-node [old-path new-path tree]
  ;(println "old-path:" old-path "new-path:" new-path)
  (let [old-node (select-one old-path tree)
        ;_ (println "old-node:")
        ;_ (pp/pprint old-node)
        new-node (assoc old-node :path new-path)
        ;_ (println "new-node:")
        ;_ (pp/pprint new-node)
        tree-minus-old-node (delete old-path tree)
        ;_ (println "tree-minus-old-node:")
        ;_ (pp/pprint tree-minus-old-node)
        new-tree (insert-rows-into-tree [new-node] tree-minus-old-node)
        ;_ (println "new-tree:")
        ;_ (pp/pprint new-tree)
        transformed-tree (transform-paths-in-tree old-path new-path new-tree)
        ;_ (println "transformed-tree:")
        ;_ (pp/pprint transformed-tree)
        ]
    transformed-tree
    )
  )

;; todo copy-node
;; todo we need to copy the contents, similar to move without the delete
;; todo replace all the paths with a new "parent"
;; todo example: canned jobs to a customer

;; todo everything after this is old news and not used
(defn create-lookup-map [child-path old-path new-path]
  {child-path (str/replace-first child-path old-path new-path)}
  )

(defn convert-old-path->new-path [child-list old-path new-path]
  ;(println "child-list:" child-list "old-path:" old-path "new-path:" new-path)
  (map #(create-lookup-map % old-path new-path) child-list)
  )

(defn update-map-with-new-path [map new-child-map]
  ;(println "new-child-map:" new-child-map)
  (let [path (:path map)
        ;_ (println "path:" path)
        new-path (get new-child-map path)
        ;_ (println "new-path:" new-path)
        ]
    (assoc map :path new-path)
    )
  )

(defn add-children-to-new-db [child-list old-path new-path old-db new-db]
  ;(pp/pprint old-db)
  ;(println "child-list:" child-list "count:" (count child-list))
  (let [old-children (map #(select % old-db) child-list)
        ;(into [] (flatten ))
        new-child-map (into {} (convert-old-path->new-path child-list old-path new-path))
        ;_ (println "new-child-map:" new-child-map)
        ]
    ;(println "old-children:")
    ;(pp/pprint old-children)
    (insert-rows-into-tree (map #(update-map-with-new-path % new-child-map) (into [] (flatten (map #(select % old-db) child-list)))) new-db)
    )
  )

(defn create-new-path [old-path new-path item]
  ;(println "old-path:" old-path "new-path:" new-path)
  (let [path (:path item)
        new-path (str/replace-first path old-path new-path)
        ]
    ;(println "results: path:" path "new-path:" new-path)
    (assoc item :path new-path)
    )
  )

(defn update-path [old-path new-path current-state path]
  ;(println "update-path->path:" path)
  (let [path-segments (split-path path)
        ;path-segment list-of-path-segments
        ;_ (println "list-of-path-segments:" list-of-path-segments)
        ;last-segment (last list-of-path-segments)
        new-state (update-in current-state path-segments (partial create-new-path old-path new-path))
        ]
    new-state
    )
  )

(defn update-db [new-map indices-path db]
  (let [old-path (:old-path new-map)
        ;_ (println "old-path:" old-path)
        new-map-minus-old-path (dissoc new-map :old-path)
        ;_ (println "new-map-minus-old-path")
        ;_ (pp/pprint new-map-minus-old-path)
        new-db (insert-rows-into-tree [new-map-minus-old-path] {})
        ;_ (println "new-db:")
        ;_ (pp/pprint new-db)
        new-path (:path new-map-minus-old-path)
        ;_ (println "new-path:" new-path)
        old-map (select-one old-path db)
        ;_ (println "old-map:")
        ;_ (pp/pprint old-map)
        db-minus-old-path (delete old-path db)
        ;_ (println "db-minus-old-path:")
        ;_ (pp/pprint db-minus-old-path)
        ;old-path-children (filter #(not= old-path %) (find-item-that-starts-with-path old-path db))
        old-path-children (find-item-that-starts-with-path old-path old-map)
        ;_ (println "old-path-children:" old-path-children)
        test (map #(update-path old-path new-path db %) old-path-children)
        ;_ (pp/pprint test)
        new-db-with-children (add-children-to-new-db old-path-children old-path new-path db new-db)
        ;_ (pp/pprint new-db-with-children)
        new-map-from-new-db (first (select new-path new-db-with-children))
        db-minus-old-path (delete old-path db)
        final-db (insert-rows-into-tree [new-map-from-new-db] db-minus-old-path)
        final-db-minus-indices (delete indices-path final-db) ;; todo this should be first...
        ]
    final-db-minus-indices
    )
  )


;Usage:

;Simple select
;(pp/pprint (s/select-one (path-selector "/Shops/shop-2/cust-2") db))
;; =>
;; "PATH: Shops/shop-2/cust-2"

;(pp/pprint (s/setval [(path-selector "Shops/shop-2/cust-2")] "CHANGED PATH: Shops/shop-2/cust-2" db))

;user> (use 'com.rpl.specter)
;user> (transform [ALL :a even?]
;                 inc
;                 [{:a 1} {:a 2} {:a 4} {:a 3}])
;[{:a 1} {:a 3} {:a 5} {:a 3}]


;(defn test-fn [item]
;  (println "item:" item)
;  item
;  )
;
;(pp/pprint db)
;(pp/pprint (s/transform [(mapv s/keypath (split-path "/Shops/shop-2"))]
;                        test-fn
;                        db))
;(pp/pprint (s/setval [(mapv s/keypath (split-path "/Shops/shop-2"))] (test-fn) db))

;(pp/pprint (s/select-one (path-selector "/Shops/shop-2/cust-2") db))
;(pp/pprint (s/setval [(path-selector "/Shops/shop-2/cust-2")] nil db))

;(pp/pprint
;  (s/compiled-transform (s/comp-paths (path-selector "Shops/shop-2/cust-2")
;                                      (s/putval "mjs 2 CHANGED "))
;                        str
;                        db))

;(pp/pprint
;  (s/transform [(path-selector "Shops/shop-2")
;                (s/multi-path (path-selector "cust-1")
;                              (path-selector "cust-2"))
;                (s/putval "CHANGED ")]
;               str
;               db))

;(println (s/select [(s/keypath "Shops")(s/keypath "shop0001")] treedatabase))
;(.log js/console (s/select [(s/keypath "Shops")(s/keypath "shop0001")] treedatabase))

;(println "find-item-that-contains-path" (find-item-that-contains-path "/Shops/shop0001/cust0001/veh0001/job0001" results))

;(def thing {:page/tags [{:tag/category "lslsls"}]})
;(println "thing:" thing (walk/postwalk #(if (keyword? %) (keyword (name %)) %) thing))
;{:tags [{:category "lslsls"}]}

;(defn find-containing-paths [db path]
;    (loop [results []
;           current-state initial-map
;           path-segments list-of-path-segments]
;      (let [path-segment (first path-segments)
;            ;_ (println path-segment)
;            last-map (get lookup-map (last path-segment))
;            ;_ (println "last-map:" last-map "current-state:" current-state)
;            ;new-state (build-tree-from-paths-map current-state path-segment last-map)
;            new-state (update-in current-state path-segment merge-nodes last-map)
;            _ (println "new-state:" new-state)
;            ]
;        (if (empty? path-segments)
;          current-state
;          (recur new-state (rest path-segments))
;          )
;        )
;      )
;    )
;
;(println "find-containing-pahts:" (find-containing-paths results "/Shops/shop0001/cust0001/veh0001/job0001"))

;; TODO can we replace find-leaf with a s/select?
;; TODO need a place where we can call this to test
;(println (s/select [s/ALL :a even?] [{:a 1} {:a 2} {:a 4} {:a 3}]))
;(println (s/setval (s/srange 2 4)
;                   [99]
;                   [0 1 2 3 4 5 6 7 8 9]))
;(println (s/setval (s/keypath "Shops")
;                   {"shop0001" {}}
;                   {"Shops" {}}
;                   ))
;(println (s/setval nil
;                   {"Shops" {}}
;                   {}
;                   ))

;(.log js/console (str "Hey Seymore! what is goin' on? " (js/Date.)))

;(println "<-----------------end of core")

;(om/root
;  (fn [data owner]
;    (reify om/IRender
;      (render [_]
;        (dom/h1 nil (:text data)))))
;  app-state
;  {:target (. js/document (getElementById "app"))})

