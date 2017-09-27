(ns cljs.user
  (:require [systems.casemgr.utils.dispatcher.core]
            [systems.casemgr.utils.dispatcher.system :as system]))

(def go system/go)
(def reset system/reset)
(def stop system/stop)
(def start system/start)
