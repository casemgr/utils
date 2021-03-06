(defproject systems.casemgr.utils/pathdb "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [sablono "0.3.6"]
                 [org.omcljs/om "0.9.0"]
                 ;[com.rpl/specter "0.7.1"]
                 [com.rpl/specter "0.8.0"]
                 [funcool/cuerdas "0.6.0"]
                 [binaryage/devtools "0.4.1"]
                 [instar "1.0.10" :exclusions [org.clojure/clojure]]
                 [fipp "0.6.3"]
                 [funcool/hodgepodge "0.1.4"]
                 ]

  :plugins [[lein-cljsbuild "1.1.6"]
            [lein-figwheel "0.5.10" :exclusions [org.clojure/clojure org.codehaus.plexus/plexus-utils]]
           ]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]

              :figwheel { :on-jsload "systems.casemgr.utils.pathdb.core/on-js-reload" }

              :compiler {:main systems.casemgr.utils.pathdb.core
                         :asset-path "js/compiled/out"
                         :output-to "resources/public/js/compiled/systems/casemgr/utils/pathdb.js"
                         :output-dir "resources/public/js/compiled/out"
                         :source-map-timestamp true }}
             {:id "min"
              :source-paths ["src"]
              :compiler {:output-to "resources/public/js/compiled/systems.casemgr.utils.pathdb.js"
                         :main systems.casemgr.utils.pathdb.core
                         :optimizations :advanced
                         :pretty-print false}}]}

  :figwheel {
             ;; :http-server-root "public" ;; default and assumes "resources" 
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1" 

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log" 
             })
