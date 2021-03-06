(defproject clj-clos "0.1.0-SNAPSHOT"
  :description "CLOS-like tools for Clojure multimethods"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles
  {:dev {:dependencies [[midje "1.5.0"]]
         :plugins [[lein-midje "3.0.1"]]}}
  :test-paths ["test"]
  :source-paths ["src"])

