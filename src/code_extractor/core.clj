(ns code-extractor.core
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def cli-options
  [["-f" "--file FILE" "File to tangle"
    :id :file
    :validate [#(.exists (clojure.java.io/file %)) "File doesn't exist"]]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :update-fn inc]
   ["-h" "--help"]])

(defrecord block [id begin-line end-line skip filename data])

(defn matches? [regex string]
  (let [[_ fn ln] (re-matches regex string)] (not (nil? fn))))

(defn match-first-group-or-default [regex string default]
  (let [[_ fn ln] (re-matches regex string)] fn))

(defn extract-regex [default-output b-regexes data]

  (defn extract-regex-iter [lines line-number block-id block-data is-block-started block-filename block-skip block-begin-line is-block-first-line is-newline-added result]

    (defn extract-regex-iter-skip []
      (extract-regex-iter (rest lines) (inc line-number) block-id block-data is-block-started block-filename block-skip block-begin-line is-block-first-line is-newline-added result))

    (defn extract-regex-iter-begin []
      (let [skip     (matches? (:b-skip b-regexes) (first lines))
            filename (if skip nil (match-first-group-or-default (:b-file b-regexes) (first lines) default-output))]
        (extract-regex-iter (rest lines) (inc line-number) (inc block-id) "" true filename skip
                            (inc line-number) true false result)))

    (defn extract-regex-iter-end []
      (extract-regex-iter (rest lines) (inc line-number) block-id "" false default-output false 0 true false (conj result (->block block-id block-begin-line (dec line-number) block-skip block-filename block-data))))

    (defn extract-regex-iter-new-code-line [block-data is-newline-added]
      (extract-regex-iter (rest lines) (inc line-number) block-id block-data true block-filename block-skip block-begin-line false is-newline-added result))
    (if (empty? lines) result
        (cond
          (matches? (:b-begin b-regexes) (first lines)) (extract-regex-iter-begin)
          (matches? (:b-end b-regexes) (first lines))   (extract-regex-iter-end)
          is-block-started
          (extract-regex-iter-new-code-line
           (str
            block-data
            (if (not is-block-first-line) "\n"
                (if (and (not is-newline-added) (not (empty? result))) "\n"))
            (first lines))
           (and
            is-block-first-line
            (not is-newline-added)
            (not (empty? result))))
          :else                                  (extract-regex-iter-skip))))
  (extract-regex-iter (str/split data #"\n") 0 -1 "" false default-output false 0 true false '()))

(def org-regexes
  {:b-begin       #"^\s*#\+BEGIN_SRC.*$"
   :b-end         #"^\s*#\+END_SRC.*$"
   :b-skip        #"^\s*#\+BEGIN_SRC.*\s:tangle\ no.*$"
   :b-file        #"^\s*#\+BEGIN_SRC.*\s:tangle\s?(\S*)\s?.*$"})

(defn extract-regex-org [default-output data]
  (extract-regex default-output org-regexes data))

(defn -main [& args]
  (let [x (parse-opts args cli-options)
        options (:options x)
        file (:file options)
        default-output (str file ".output")
        file-content (slurp file)
        extracted-blocks (reverse (extract-regex-org default-output file-content))
        not-skipped-blocks (filter #(false? (:skip %)) extracted-blocks)
        to-output-blocks (map #(:data %) not-skipped-blocks)
        to-output (reduce #(str %1 "\n" %2) to-output-blocks)]
    (spit default-output to-output)))
