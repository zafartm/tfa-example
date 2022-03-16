(defproject tfa-example "0.1.0-SNAPSHOT"
  :description "Two factor Auths (2FA) Example"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [metosin/compojure-api "1.1.13"]
                 [org.clojure/tools.logging "1.2.4"]
                 ;[mount "0.1.16"]
                 [cprop "0.1.19"]
                 [migratus "1.2.8"]
                 [hikari-cp "2.13.0"]
                 [mysql/mysql-connector-java "8.0.28"]
                 [buddy/buddy-hashers "1.8.158"]
                 [com.taoensso/carmine "3.1.0"]
                 [selmer "1.12.50"]
                 [com.sun.mail/javax.mail "1.6.2"]
                 [one-time "0.7.0"]

                 ; Use Logback as the main logging implementation:
                 [ch.qos.logback/logback-classic "1.2.10"]
                 [ch.qos.logback/logback-core "1.2.10"]

                 ;; Logback implements the SLF4J API:
                 [org.slf4j/slf4j-api "1.7.36"]

                 ;; Redirect Apache Commons Logging to Logback via the SLF4J API:
                 [org.slf4j/jcl-over-slf4j "1.7.36"]

                 ;; Redirect Log4j 1.x to Logback via the SLF4J API:
                 [org.slf4j/log4j-over-slf4j "1.7.36"]

                 ;; Redirect Log4j 2.x to Logback via the SLF4J API:
                 [org.apache.logging.log4j/log4j-to-slf4j "2.17.2"]

                 ;; Redirect OSGI LogService to Logback via the SLF4J API
                 [org.slf4j/osgi-over-slf4j "1.7.36"]

                 ;; Redirect java.util.logging to Logback via the SLF4J API.
                 ;; Requires installing the bridge handler, see README:
                 [org.slf4j/jul-to-slf4j "1.7.36"]]

  :exclusions [;; Exclude transitive dependencies on all other logging
               ;; implementations, including other SLF4J bridges.
               commons-logging
               log4j
               org.apache.logging.log4j/log4j
               org.slf4j/simple
               org.slf4j/slf4j-jcl
               org.slf4j/slf4j-nop
               org.slf4j/slf4j-log4j12
               org.slf4j/slf4j-log4j13]


  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/jul-factory"]

  :ring {:handler tfa-example.handler/app
         :port    3000
         :nrepl   {:port   7000
                   ;:host   "127.0.0.1"
                   :start? true}}
  :uberjar-name "server.jar"

  :migratus {:store         :database
             :migration-dir "migrations"
             :db            {:connection-uri ~(get (System/getenv) "DATABASE_URL")}}


  :profiles {:dev {:jvm-opts     ["-Dconf=./config.edn"]
                   :dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]
                   :plugins      [[lein-ring "0.12.5"]
                                  [migratus-lein "0.7.3"]]}})

