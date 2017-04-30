#!/bin/sh
echo "utils"
cd utils
lein clean
lein cljsbuild once
lein install
cd ..
echo "channel-monitor"
cd channel-monitor  
lein clean
lein cljsbuild once
lein install
cd ..
echo "are_you_sure"
cd are_you_sure  
lein clean
lein cljsbuild once
lein install
cd ..
echo "date_scanner"
cd date_scanner  
lein clean
lein cljsbuild once
lein install
cd ..
echo "pathdb"
cd pathdb
lein clean
lein cljsbuild once
lein install
cd ..

