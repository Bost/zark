(ns zark.category-theory-test
  (:require [clojure.test :refer :all]
            [zark.category-theory :refer :all]))

(deftest test-basic-contracts
  (testing "Contract for strings, numbers, booleans"
    ;; (is true (cBool true))
    (is (= true (cBool true)))
    (is (= false (cBool false)))
    (is (not= true (cBool false)))
    (is (= "" (cStr "")))
    (is (not= "a" (cStr "b")))
    (is (= 0 (cNum 0)))
    (is (not= 0 (cNum 1)))
    (is (thrown? Exception (cStr 1)))
    (is (thrown? Exception (cNum "")))
    ;; (is "" (cBool false))
    ))

(deftest test-class-contract
  (testing "Contract Classes"
    (is (= java.lang.Class (cClass java.lang.Class)))
    (is (not= Object (cClass java.lang.Class)))
    (is (= Boolean (cClass Boolean)))
    (is (= Number (cClass Number)))
    (is (not= Number (cClass Boolean)))
    (is (= String (cClass String)))
    (is (= Object (cClass Object)))
    (is (not= Object (cClass String)))
    (is (= clojure.lang.IPersistentCollection
        (cClass clojure.lang.IPersistentCollection)))
    (is (not= Number
        (cClass clojure.lang.IPersistentCollection)))
    (is (thrown? Exception (cClass 1)))
    (is (thrown? Exception (cClass 'Foo))) ; There's no class called Foo
    (is (thrown? Exception (cClass "")))
    ))

(deftest test-contract-creator-typeOf
  (testing
      (str "Test if contracts created by the typeOf(T) are"
           "contract for classes of type T")
    (is (= (cBool true) ((typeOf Boolean) true)))
    (is (= (cBool false) ((typeOf Boolean) false)))
    (is (= (cNum 0) ((typeOf Number) 0)))
    (is (= (cStr "uhu") ((typeOf String) "uhu")))
    (is (= (cClass String) ((typeOf java.lang.Class) String)))
    (is (cClass Object) (typeOf Object))
    (is (cClass clojure.lang.IPersistentCollection)
        (typeOf clojure.lang.IPersistentCollection))
    (is (thrown? Exception (typeOf "Uhu")))
    (is (thrown? Exception (typeOf 'Uhu)))
    (is (thrown? Exception (typeOf `Uhu)))
    ))

(deftest test-cColl
  (testing ""
    (let [cColl (typeOf clojure.lang.IPersistentCollection)]
      (is [] (cColl []))
      (is [1 2 3] (cColl [1 2 3]))
      (is '[1 2 3] (cColl '[1 2 3]))
      (is `[1 2 3] (cColl `[1 2 3]))
      (is {} (cColl {}))
      (is () (cColl ()))
      (is #{} (cColl #{}))
      (is (thrown? Exception (cColl "[1 2 3]")))
      (is (thrown? Exception (cColl 1)))
      )))

(deftest test-cCollOf
  (testing "Collection of Strings"
    (let [cCollStr (cCollOf cStr)]
      (is  ["a" "1" "b"] (cCollStr ["a" "1" "b"]))
      (is  ["a" "1" "b"] (cCollStr ["a" "1" "b"]))
      (is (thrown? Exception (cCollStr ["a" 1 "b"])))
      )))
