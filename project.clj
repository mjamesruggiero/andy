(defproject andy "0.1.0-SNAPSHOT"
  :description "Experimenting with Redis"
  :url "http://mjamesruggiero.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.taoensso/carmine "2.14.0"]]
  :main ^:skip-aot andy.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
