(ns zark.category-theory-test
  (:require [clojure.test :refer :all]
            [zark.category-theory :refer :all]))

(deftest test-contract-str
  (testing "The contract-str"
    (is "" (contract-str ""))
    (is (thrown? Exception (contract-str 1)))))

