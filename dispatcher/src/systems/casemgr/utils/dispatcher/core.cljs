(ns systems.casemgr.utils.dispatcher.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [systems.casemgr.utils.utils.main :as main]
            [systems.casemgr.utils.dispatcher.dispatcher :as dispatcher]
            ))

(enable-console-print!)

;(println "This text is printed from src/systems.casemgr.utils.dispatcher/core.cljs. Go ahead and edit it and see reloading in action, ooooh!")

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello world!"}))

;(om/root
;  (fn [data owner]
;    (reify om/IRender
;      (render [_]
;        (dom/div nil
;                 (dom/h1 nil (:text data))
;                 (dom/h3 nil "Edit this and watch it change! Don't change..."))
;              )
;           )
;      )
;  app-state
;  {:target (. js/document (getElementById "app"))})

;(defn root-component [app owner]
  ;(reify
    ;om/IDidMount
    ;(did-mount [_]
      ;(println "root-component:did-mount")
      ;)
    ;om/IRender
    ;(render [_]
      ;(dom/div nil
               ;;(dom/h1 nil (:text data))
               ;(dom/h3 nil "Edit this and watch it change!"))
      ;)))

(defn root-component [app owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (println "root-component:did-mount")
      (let [publisher (:publisher (om/get-shared owner))]
        ;(put! publisher {:topic :component-msg :message :init-state :component-id :selected-daily-date :label "Select a Day" :display false})
        ))
    om/IRender
    (render [_]
      (dom/div
       nil
       (om/build dispatcher/ws-widget {:type :cursor :port 4000})
       ))))

(main/main root-component)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
