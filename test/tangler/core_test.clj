(ns tangler.core-test
  (:require [clojure.test :refer :all]
            [tangler.core :refer :all]))

(deftest org-regexes-matching-test
  (testing "Test org block begin matches"
    (is (true? (matches? (:b-begin org-regexes) "#+BEGIN_SRC")))
    (is (true? (matches? (:b-begin org-regexes) "#+BEGIN_SRC :tangle no")))
    (is (true? (matches? (:b-begin org-regexes) "#+BEGIN_SRC :tangle filename")))
    (is (true? (matches? (:b-begin org-regexes) "#+BEGIN_SRC emacs-lisp :tangle filename")))
    (is (true? (matches? (:b-begin org-regexes) "#+BEGIN_SRC emacs-lisp")))
    (is (true? (matches? (:b-begin org-regexes) "#+BEGIN_SRC emacs-lisp :tangle no")))))
