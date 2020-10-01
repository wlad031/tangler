(defproject tangler "1.0.0"
  :description "Code block extractor"
  :url "http://github.com/wlad031/tagler"
  :license {:name "DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE Version 2"
            :url  "http://www.wtfpl.net/"}
  :dependencies [[org.clojure/clojure "1.10.2-alpha2"]
                 [org.clojure/tools.cli "1.0.194"]
                 [com.taoensso/timbre "4.10.0"]]
  :repl-options {:init-ns tangler.core}
  :plugins [[lein-cljfmt "0.7.0"]
            [lein-kibit "0.1.8"]
            [io.taylorwood/lein-native-image "0.3.1"]]
  :native-image {:name "tangler"
                 :opts ["--verbose" 
                        "--no-fallback" 
                        "--initialize-at-build-time" 
                        "--report-unsupported-elements-at-runtime"]} 
  :profiles {:native-image {
                      :dependencies [[borkdude/clj-reflector-graal-java11-fix "0.0.1-graalvm-20.2.0"]]
                      :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :main tangler.core)
