(defproject lein-cljsbuild-example "1.2.3"
  :plugins [[lein-cljsbuild "1.1.7"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.4"]
                 ;[cljs-ajax "0.7.3"]
                 ;[cljs-http "0.1.45"]
                 [funcool/promesa "1.8.1"]
                 [funcool/httpurr "1.0.0"]
                 [rum "0.11.2"]]
  :cljsbuild {
              :builds [{:id "main"
                        :source-paths ["src/frontend/"]
                        :compiler {
                                   :output-to "public/javascripts/main.js"
                                   :optimizations :advanced
                                   :main "main"
                                   :pretty-print false}}
                       {:id "dev"
                        :source-paths ["src/frontend/"]
                        :compiler {
                                   :output-to "public/javascripts/main.js"
                                   :output-dir "public/javascripts"
                                   :asset-path "assets/javascripts"
                                   :optimizations :none
                                   :pretty-print true
                                   :main "main"
                                   :source-map true}}]})