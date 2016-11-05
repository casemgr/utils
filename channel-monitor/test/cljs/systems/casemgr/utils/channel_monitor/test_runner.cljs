(ns systems.casemgr.utils.channel-monitor.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [systems.casemgr.utils.channel-monitor.core-test]
   [systems.casemgr.utils.channel-monitor.common-test]))

(enable-console-print!)

(doo-tests 'systems.casemgr.utils.channel-monitor.core-test
           'systems.casemgr.utils.channel-monitor.common-test)
