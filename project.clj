(defproject tfa-be "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [metosin/compojure-api "1.1.13"]
                 [org.clojure/tools.logging "1.2.4"]]

  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/jul-factory"]

  :ring {:handler tfa-be.handler/app
         :port    3001
         :nrepl   {:port   7001
                   :host   "127.0.0.1"
                   :start? true}}
  :uberjar-name "server.jar"
  :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]
                   :plugins      [[lein-ring "0.12.5"]]}})

