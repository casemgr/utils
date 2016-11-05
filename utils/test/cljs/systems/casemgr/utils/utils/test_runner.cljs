(ns systems.casemgr.utils.utils.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [systems.casemgr.utils.utils.core-test]
   [systems.casemgr.utils.utils.common-test]))

(enable-console-print!)

(doo-tests 'systems.casemgr.utils.utils.core-test
           'systems.casemgr.utils.utils.common-test)
