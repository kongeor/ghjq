(defproject ghjq "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-http "3.10.0"]
                 [cheshire "5.9.0"]
                 [environ "1.1.0"]
                 [district0x/graphql-query "1.0.6"]]
  :plugins [[lein-environ "1.1.0"]]
  :repl-options {:init-ns ghjq.core})
