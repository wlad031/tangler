(ns tangler.core
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [taoensso.timbre :as log])
  (:gen-class))

(def cli-options
  [["-f" "--file FILE" "File to tangle"
    :id :file
    :validate [#(.exists (io/file %)) "File doesn't exist"]]
   ["-h" "--help"]])

(defrecord Block [id begin-line end-line skip filename data])
(defrecord ExtractionResult [not-skipped-blocks skipped-blocks])

(defn total-skipped-blocks [extraction-result]
  (reduce + (map count (vals (:skipped-blocks extraction-result)))))

(defn make-extraction-result [all-blocks-list]
  (let
   [grouped-blocks (group-by :skip all-blocks-list)]
    (->ExtractionResult
     (group-by :filename (get grouped-blocks false))
     (group-by :filename (get grouped-blocks true)))))

(defn matches? [regex string]
  (let [[_ fn _] (re-matches regex string)] (not (nil? fn))))

(defn match-first-group-or-default [regex string default]
  (let [[_ fn _] (re-matches regex string)] (if (nil? fn) default fn)))

(defn extract-regex [default-output b-regexes data]

  (letfn
   [(extract-regex-iter
      [lines line-number block-id block-data is-block-started block-filename
       block-skip block-begin-line is-block-first-line is-newline-added result]

      (letfn
       [(extract-regex-iter-skip []
          (extract-regex-iter
           (rest lines) (inc line-number) block-id block-data is-block-started
           block-filename block-skip block-begin-line is-block-first-line
           is-newline-added result))

        (extract-regex-iter-begin []
          (let [skip     (matches? (:b-skip b-regexes) (first lines))
                filename (when-not skip
                           (match-first-group-or-default
                            (:b-file b-regexes) (first lines) default-output))]
            (extract-regex-iter
             (rest lines) (inc line-number) (inc block-id) "" true filename skip
             (inc line-number) true false result)))

        (extract-regex-iter-end []
          (extract-regex-iter
           (rest lines) (inc line-number) block-id "" false default-output false
           0 true false
           (conj result
                 (->Block block-id block-begin-line (dec line-number) block-skip
                          block-filename block-data))))

        (extract-regex-iter-new-code-line [block-data is-newline-added]
          (extract-regex-iter
           (rest lines) (inc line-number) block-id block-data true
           block-filename block-skip block-begin-line false is-newline-added
           result))]

        (if (empty? lines) result
            (cond

              (matches? (:b-begin b-regexes) (first lines))
              (extract-regex-iter-begin)

              (matches? (:b-end b-regexes) (first lines))
              (extract-regex-iter-end)

              is-block-started
              (extract-regex-iter-new-code-line
               (str
                block-data
                (if-not is-block-first-line "\n"
                        (when (and (not is-newline-added) (seq result)) "\n"))
                (first lines))
               (and
                is-block-first-line
                (not is-newline-added)
                (seq result)))

              :else (extract-regex-iter-skip)))))]

    (make-extraction-result
     (reverse (extract-regex-iter
               (str/split data #"\n") 0 -1 "" false default-output
               false 0 true false '())))))

(def org-regexes
  {:b-begin       #"^\s*#\+(?i)(BEGIN_SRC).*$"
   :b-end         #"^\s*#\+(?i)(END_SRC).*$"
   :b-skip        #"^\s*#\+(?i)(BEGIN_SRC).*\s:tangle\ no.*$"
   :b-file        #"^\s*#\+(?i)(BEGIN_SRC).*\s:tangle\s?(\S*)\s?.*$"})

(defn extract-regex-org [default-output data]
  (extract-regex default-output org-regexes data))

(defn -main [& args]
  (log/merge-config! {:output-fn (fn [{:keys [msg_]}] (print (force msg_)))})
  (let [options           (:options (parse-opts args cli-options))
        file-path         (:file options)
        default-output    (str file-path ".tangled")
        file-content      (slurp file-path)
        extraction-result (extract-regex-org default-output file-content)]
    (log/infof "Skipped %d blocks" (total-skipped-blocks extraction-result))
    (doseq [[file-path blocks] (:not-skipped-blocks extraction-result)]
      (log/infof "File %s: tangled %d blocks" file-path (count blocks))
      (spit file-path (reduce #(str %1 "\n" %2) (map :data blocks))))))
