(ns zark.category-theory-test
  (:require [clojure.test :refer :all]
            [zark.category-theory :refer :all]))

(deftest test-contract-String
  (testing "Contract for strings"
    (is "" (cStr ""))
    (is (thrown? Exception (cStr 1)))))

(deftest test-contract-Number
  (testing "Contract for numbers"
    (is 1 (cNum 1))
    (is (thrown? Exception (cNum "")))))

(deftest test-contract-Class
  (testing "Contract Classes"
    (is String (cClass String))
    (is Number (cClass Number))
    (is (thrown? Exception (cClass 1)))
    (is (thrown? Exception (cClass "")))
    ))

(deftest test-contract-creator-typeOf
  (testing ""
    (is Boolean (typeOf Boolean))
    (is Number (typeOf Number))
    (is String (typeOf String))
    (is Object (typeOf Object))
    (is clojure.lang.IPersistentCollection (typeOf clojure.lang.IPersistentCollection))
    ;; Compile error: Unable to resolve symbol: IPersistentCollection in this context
    ;; (is IPersistentCollection (typeOf IPersistentCollection))
    (is (thrown? Exception (typeOf "Uhu")))
    (is (thrown? Exception (typeOf 'Uhu)))
    (is (thrown? Exception (typeOf `Uhu)))
    ))

(deftest test-cColl
  (testing ""
    (let [cColl (typeOf clojure.lang.IPersistentCollection)]
      (is clojure.lang.IPersistentCollection (cColl []))
      (is clojure.lang.IPersistentCollection (cColl [1 2 3]))
      (is clojure.lang.IPersistentCollection (cColl '[1 2 3]))
      (is clojure.lang.IPersistentCollection (cColl `[1 2 3]))
      (is clojure.lang.IPersistentCollection (cColl {}))
      (is clojure.lang.IPersistentCollection (cColl ()))
      (is clojure.lang.IPersistentCollection (cColl #{}))
      (is (thrown? Exception (cColl "[1 2 3]")))
      (is (thrown? Exception (cColl 1)))
      )))
