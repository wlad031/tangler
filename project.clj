(defproject tangler "1.0.0"
  :description "Code block extractor"
  :url "http://github.com/wlad031/tagler"
  :license {:name "DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE Version 2"
            :url  "http://www.wtfpl.net/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [com.taoensso/timbre "4.10.0"]]
  :repl-options {:init-ns tangler.core}
  :plugins [[lein-cljfmt "0.7.0"]
            [lein-kibit "0.1.8"]]
  :main tangler.core)
