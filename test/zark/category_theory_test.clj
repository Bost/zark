(ns zark.category-theory-test
  (:require [clojure.test :refer :all]
            [zark.category-theory :refer :all]))

(deftest test-contract-basic
  (testing "Contract for Strings, Numbers, Booleans"
    (is (= true (cBool true)))
    (is (= false (cBool false)))
    (is (not= true (cBool false)))
    (is (not= "" (cBool false)))
    (is (not= 1 (cBool true)))
    (is (= "" (cStr "")))
    (is (not= "a" (cStr "b")))
    (is (= 0 (cNum 0)))
    (is (not= 0 (cNum 1)))
    (is (thrown? Exception (cBool 1)))
    (is (thrown? Exception (cBool "foo")))
    (is (thrown? Exception (cBool 'Uhu))) ; There is no class called Uhu
    (is (thrown? Exception (cStr 1)))
    (is (thrown? Exception (cNum "")))
    (is (thrown? Exception (cNum false)))))

(deftest test-contract-Class
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
    (is (thrown? Exception (cClass 'Foo))) ; There is no class called Foo
    (is (thrown? Exception (cClass "")))))

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
    (is (thrown? Exception (typeOf `Uhu)))))

(deftest test-contract-cColl
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
      (is (thrown? Exception (cColl 1))))))

(deftest test-contract-cCollOf
  (testing "Collection of Strings"
    (let [cCollStr (cCollOf cStr)]
      (is (= ["a" "1" "b"] (cCollStr ["a" "1" "b"])))
      (is (not= ["b" "2" "d"] (cCollStr ["a" "1" "b"])))
      (is (thrown? Exception (cCollStr ["a" nil])))
      (is (thrown? Exception (cCollStr ["a" 1])))))
  (testing "Collection of Numbers"
    (let [cCollNum (cCollOf cNum)]
      (is (= [1 2 3] (cCollNum [1 2 3])))
      (is (not= [4 5 6] (cCollNum [1 2 3])))
      (is (thrown? Exception (cCollNum [1 nil])))
      (is (thrown? Exception (cCollNum ["a" 1])))))
  (testing "Collection of Booleans"
    (let [cCollBool (cCollOf cBool)]
      (is (= [true false] (cCollBool [true false])))
      (is (not= [true true] (cCollBool [true false])))
      (is (thrown? Exception (cCollBool [nil nil])))
      (is (thrown? Exception (cCollBool ["a" 1]))))))
