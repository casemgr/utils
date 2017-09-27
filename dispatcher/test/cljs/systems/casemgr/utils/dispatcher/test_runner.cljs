(ns systems.casemgr.utils.dispatcher.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [systems.casemgr.utils.dispatcher.core-test]
   [systems.casemgr.utils.dispatcher.common-test]))

(enable-console-print!)

(doo-tests 'systems.casemgr.utils.dispatcher.core-test
           'systems.casemgr.utils.dispatcher.common-test)
