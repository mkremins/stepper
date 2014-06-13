(defproject stepper "0.1.0-SNAPSHOT"
  :description "Visualize Clojure code evaluation, step by step"
  :url "https://github.com/mkremins/stepper"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2227"]
                 [om "0.6.4"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :cljsbuild
  {:builds [{:source-paths ["src"]
             :compiler {:output-to "stepper.js"
                        :optimizations :whitespace}}]})
