(ns clojure.core-test.disj
  (:require [clojure.test :refer [are deftest is testing]]
            #?@(:squint [[clojure.core-test.squint-immutable :as sqim]])
            [clojure.core-test.portability #?(:cljs :refer-macros :default :refer) [when-var-exists] :as p]))

(when-var-exists disj
  (deftest test-disj
    (testing "nominal cases"
      (are [expected set keys] (= expected (apply disj set keys))
                               nil nil [nil]
                               #{} #{} [nil]
                               #{} #{1} [1]
                               #{} #{1} [1 1 1]
                               #{} #{1 2 3} [1 2 3]
                               #{3} #{1 2 3} [1 2]
                               #{1 2 3} #{1 2 3} [4 5 6]
                               #?@(:squint [#{[1 1] [3 3]}] :default [#{[3 3]}]) #{[1 1] 2 [3 3]} [[1 1] 2]
                               #{:a :b} #{:a :b :c} [:c]
                               #{true nil} #{true false nil} [false]))
    (when-var-exists sorted-set
      (testing "sorted preservation"
        (is (sorted? (disj (sorted-set 1 2 3) 1 2 3)))))
    (testing "meta preservation"
      (let [test-meta {:me "ta"}
            with-test-meta #(with-meta % test-meta)
            with-test-meta? #(= test-meta (meta %))]
        (is (with-test-meta? (disj (with-test-meta #{1 2 3}) 1 2 3)))))
    (testing "bad shape"
      (are [set keys] (p/thrown? (apply disj set keys))
                      '(1) [1]
                      [1] [1]
                      {:a 1} [:a]
                      42 [42]
                      3.14 [3.14]
                      "string" [\s \t]))
    #?(:squint
       (testing "immutable.js through the squint protocols"
         (is (= (sqim/iset #{[3 3]}) (disj (sqim/iset #{[1 1] 2 [3 3]}) (sqim/->imm [1 1]) 2)))
         (is (= 1 (count (disj (sqim/iset #{1 2}) 1))))))))
