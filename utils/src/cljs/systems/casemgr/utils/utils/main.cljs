(ns systems.casemgr.utils.utils.main
  (:require [cljs.core.async :as async :refer [chan <! >! timeout pub sub unsub unsub-all put! alts!]]
            [om.core :as om]))

(defn main [root-component]
  (let [tx-chan (chan)
        tx-pub-chan (async/pub tx-chan (fn [_] :txs))
        req-chan (chan)
        update-chan (chan)
        publisher (chan)
        publication (pub publisher #(:topic %))]
    (om/root
      root-component
      {} ;;state/app-state
      {:shared    {:req-chan    req-chan
                   :update-chan update-chan
                   :tx-chan     tx-pub-chan
                   :publisher   publisher
                   :publication publication}
       :tx-listen (fn [tx-data root-cursor]
                    (put! tx-chan [tx-data root-cursor]))
       :target    (. js/document (getElementById "app"))})
    ))
