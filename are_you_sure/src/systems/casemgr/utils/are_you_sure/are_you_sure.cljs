(ns systems.casemgr.utils.are_you_sure.are_you_sure
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

    ;[cljs.core.match :refer-macros [match]]
    ;[om-bootstrap.button :as b]
    ;[om-bootstrap.grid :as g]
    ;[om-bootstrap.panel :as p]
    ;[om-tools.dom :as d :include-macros true]
    ;[scheduler_server.utils :as utils]
    ;[scheduler_server.common :as common]
            ))

(defn parameters-list [owner e-map display v]
  ;(println "trigger->all->e-map:" e-map)
  (let [publisher (:publisher (om/get-shared owner))
        component-id (:component-id e-map)
        delete-map {:topic        (:topic e-map)
                    :component-id component-id
                    :message      (:message e-map)}
        ]
    ;(println "merged:" (merge temp-map (:data e-map)))
    (put! publisher (merge delete-map (:data e-map)))
    ;; assuming that the caller was a component-msg, if not...
    ;; need to send in called by, mulitple maps of params
    (put! publisher {:topic :component-msg :component-id component-id :message :change-state :display false})
    (common/set-owner-state! owner :e-map e-map {:display display})
    ))

(defn are-you-sure-widget [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:e-map {:display false}})
    om/IDidMount
    (did-mount [_]
      (let [shared (om/get-shared owner)
            publication (:publication shared)
            component-msg-events (sub publication :component-msg (chan))
            ]
        (println "are-you-sure-widget: did-mount")
        (go-loop []
                 (let [[v ch] (alts! [component-msg-events])
                       _ (pp/pprint v)
                       current-state (om/get-state owner)
                       e-map (:e-map current-state)
                       topic (:topic v)
                       message (:message v)
                       component-id (:component-id v)
                       display (:display v)
                       data (dissoc v :topic :component-id :message :original-topic :original-comp-id :original-msg :dialog-text :display)
                       state-to-save {:display      display
                                      :topic        (:original-topic v)
                                      :component-id (:original-comp-id v)
                                      :message      (:original-msg v)
                                      :dialog-text  (:dialog-text v)
                                      :data         data}]
                   ;; TODO onCancel we need to send a message back to the sender, saying cancelled
                   ;; TODO onDelete we need to send a message back to the sender, saying do it!
                   ;; TODO Need "ontop" class
                   (cond
                     (and (= topic :component-msg) (= component-id :are_you_sure) (= message :are-you-sure?)) (common/set-owner-state! owner :e-map e-map state-to-save)
                     (and (= topic :component-msg) (= component-id :are_you_sure) (= message :change-state)) (common/set-owner-state! owner :e-map e-map {:display display})
                     (and (= topic :component-msg) (= component-id :are_you_sure) (= message :yes_im_sure)) (parameters-list owner e-map display v)
                     :else nil)
                   (recur)))))
    om/IRenderState
    (render-state
      [_ {:keys [e-map]}]
      (println "are-you-sure->e-map:" e-map)
      (let [publisher (:publisher (om/get-shared owner))
            dialog-text (:dialog-text e-map)]
        (dom/div {:style {:display (if (:display e-map) "block" "none")}
                  :class "ontop"}
                 (bs/panel
                   {:id       "are-you-sure-panel"
                    :style    {:display (if (not (:display e-map)) "none")}
                    :header   (dom/h2 "Are you sure?")
                    :bs-style "primary"
                    :footer   (utils/footer-text)}
                   (bs/row {}
                           (bs/col {:xs 12} dialog-text))
                   (dom/br)
                   (bs/row {}
                           (bs/col {:xs 12}
                                   ;(bs/toolbar {}
                                   (bs/button-group {}
                                                    (bs/button {:onClick  #(put! publisher {:topic :component-msg :component-id :are_you_sure :message :yes_im_sure :display false})
                                                                :bs-size  "small"
                                                                :bs-style "success"} "DELETE")
                                                    )
                                   (bs/button-group {}
                                                    (bs/button {:onClick  #(put! publisher {:topic :component-msg :component-id :are_you_sure :message :change-state :display false})
                                                                :bs-size  "small"
                                                                :bs-style "warning"} "Cancel"))
                                   ;)
                                   )
                           )
                   )
                 )
        ))))
