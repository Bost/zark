(ns zark.category-theory-test
  (:require [clojure.test :refer :all]
            [zark.category-theory :refer :all])
  ;; `defprotocol` creates a real Var, which you can `use` or `refer` just like a function.
  ;; `deftype` and `defrecord` both create classes, which you must `import` using the class name.
  ;; Class names have to be compatible with Java, so dashes are converted to underscores.
  (:import [zark.category_theory None Some]))

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

(deftest test-contract-cCollOf
  (testing "Collection of values accepted by guarded fn"
    (let [cGfRepeat (cCollOf gfRepeat)]
      (is (= ["aa" "11" "bb"] (cGfRepeat ["a" "1" "b"])))
      (is (= ["aa" "11" "bb"] (cGfRepeat '["a" "1" "b"])))
      (is (= ["aa" "11" "bb"] (cGfRepeat ['"a" '"1" '"b"])))
      (is (not= ["a" "1" "b"] (cGfRepeat ["a" "1" "b"])))
      (is (thrown? Exception (cGfRepeat [1 2])))
      (is (thrown? Exception (cGfRepeat ['a "a"])))
      (is (thrown? Exception (cGfRepeat ['1 "a"]))))))

(deftest test-maybe
  (testing "TODO separate getOrElse from the rest"
    (is "None" (cstr (None.)))
    (is "None" (cstr (None.)))
    (is "Some" (cstr (Some. "")))
    (is "Some 1" (cstr (Some. 1)))
    (is "Some []" (cstr (Some. [])))
    (is "Some val" (cstr (Some. "val")))

    (is "jane" (getOrElse (None.) "jane"))
    (is "joe" (getOrElse (Some. "joe") "jane"))

    (is (getOrElse (None.) "jane")
        (getOrElse ((maybe gfRepeat) (None.)) "jane"))

    (is ((maybe gfRepeat) (Some. "joe"))
        (getOrElse ((maybe gfRepeat) (Some. "joe")) "jane"))

    (is (cstr (None.)) (cstr ((maybe gfRepeat) (None.))))
    (is (gfRepeat "val") (cstr ((maybe gfRepeat) (Some. "val"))))
    (is (thrown? Exception (cstr ((maybe gfRepeat) (Some. 1)))))
    (is (thrown? Exception (cstr ((maybe gfRepeat) (Some. [])))))
    ))
