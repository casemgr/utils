(ns systems.casemgr.utils.utils.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [systems.casemgr.utils.utils.utils :as utils]
            [systems.casemgr.utils.utils.common :as common]
            ))

(enable-console-print!)

(defonce app-state (atom {:text "Hello Chestnut!"}))

(defn root-component [app owner]
  (reify
    om/IRender
    (render [_]
      (utils/log "hello world")
      (dom/div nil (dom/h1 nil (utils/footer-text)))
      ;(bs/form-group {:control-id "formControlsSelect"}
      ;               (bs/control-label "Select")
      ;               (bs/form-control {:component-class "select" :placeholder "select"}
      ;                                (dom/option {:value "select"} "select")
      ;                                (dom/option {:value "other"} "...")))
;      (common/create-header "Hello World Header")
      )))

(om/root
  root-component
  app-state
  {:target (js/document.getElementById "app")})
