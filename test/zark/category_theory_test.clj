(ns zark.category-theory-test
  (:require [clojure.test :refer :all]
            [zark.ct4js :refer :all])
  (:import [zark.ct4js None Some]))

(deftest test-contract-basic
  (testing "Contract for Strings, Numbers, Booleans"
    (is (= true (c-bool true)))
    (is (= false (c-bool false)))
    (is (not= true (c-bool false)))
    (is (not= "" (c-bool false)))
    (is (not= 1 (c-bool true)))
    (is (= "" (c-str "")))
    (is (not= "a" (c-str "b")))
    (is (= 0 (c-num 0)))
    (is (not= 0 (c-num 1)))
    (is (thrown? Exception (c-bool 1)))
    (is (thrown? Exception (c-bool "foo")))
    (is (thrown? Exception (c-bool 'Uhu))) ; There is no class called Uhu
    (is (thrown? Exception (c-str 1)))
    (is (thrown? Exception (c-num "")))
    (is (thrown? Exception (c-num false)))))

(deftest test-contract-Class
  (testing "Contract Classes"
    (is (= java.lang.Class (c-class java.lang.Class)))
    (is (not= Object (c-class java.lang.Class)))
    (is (= Boolean (c-class Boolean)))
    (is (= Number (c-class Number)))
    (is (not= Number (c-class Boolean)))
    (is (= String (c-class String)))
    (is (= Object (c-class Object)))
    (is (not= Object (c-class String)))
    (is (= clojure.lang.IPersistentCollection
        (c-class clojure.lang.IPersistentCollection)))
    (is (not= Number
        (c-class clojure.lang.IPersistentCollection)))
    (is (thrown? Exception (c-class 1)))
    (is (thrown? Exception (c-class 'Foo))) ; There is no class called Foo
    (is (thrown? Exception (c-class "")))))

(deftest test-contract-creator-type-of
  (testing
      (str "Test if contracts created by the type-of(T) are"
           "contract for classes of type T")
    (is (= (c-bool true) ((type-of Boolean) true)))
    (is (= (c-bool false) ((type-of Boolean) false)))
    (is (= (c-num 0) ((type-of Number) 0)))
    (is (= (c-str "uhu") ((type-of String) "uhu")))
    (is (= (c-class String) ((type-of java.lang.Class) String)))
    (is (c-class Object) (type-of Object))
    (is (c-class clojure.lang.IPersistentCollection)
        (type-of clojure.lang.IPersistentCollection))
    (is (thrown? Exception (type-of "Uhu")))
    (is (thrown? Exception (type-of 'Uhu)))
    (is (thrown? Exception (type-of `Uhu)))))

(deftest test-contract-c-coll
  (testing ""
    (let [c-coll (type-of clojure.lang.IPersistentCollection)]
      (is [] (c-coll []))
      (is [1 2 3] (c-coll [1 2 3]))
      (is '[1 2 3] (c-coll '[1 2 3]))
      (is `[1 2 3] (c-coll `[1 2 3]))
      (is {} (c-coll {}))
      (is () (c-coll ()))
      (is #{} (c-coll #{}))
      (is (thrown? Exception (c-coll "[1 2 3]")))
      (is (thrown? Exception (c-coll 1))))))

(deftest test-contract-c-coll-of
  (testing "Collection of Strings"
    (let [c-coll-str (c-coll-of c-str)]
      (is (= ["a" "1" "b"] (c-coll-str ["a" "1" "b"])))
      (is (not= ["b" "2" "d"] (c-coll-str ["a" "1" "b"])))
      (is (thrown? Exception (c-coll-str ["a" nil])))
      (is (thrown? Exception (c-coll-str ["a" 1])))))
  (testing "Collection of Numbers"
    (let [c-collNum (c-coll-of c-num)]
      (is (= [1 2 3] (c-collNum [1 2 3])))
      (is (not= [4 5 6] (c-collNum [1 2 3])))
      (is (thrown? Exception (c-collNum [1 nil])))
      (is (thrown? Exception (c-collNum ["a" 1])))))
  (testing "Collection of Booleans"
    (let [c-collBool (c-coll-of c-bool)]
      (is (= [true false] (c-collBool [true false])))
      (is (not= [true true] (c-collBool [true false])))
      (is (thrown? Exception (c-collBool [nil nil])))
      (is (thrown? Exception (c-collBool ["a" 1]))))))

(deftest test-contract-c-coll-of
  (testing "Collection of values accepted by guarded fn"
    (let [c-gf-repeat (c-coll-of gf-repeat)]
      (is (= ["aa" "11" "bb"] (c-gf-repeat ["a" "1" "b"])))
      (is (= ["aa" "11" "bb"] (c-gf-repeat '["a" "1" "b"])))
      (is (= ["aa" "11" "bb"] (c-gf-repeat ['"a" '"1" '"b"])))
      (is (not= ["a" "1" "b"] (c-gf-repeat ["a" "1" "b"])))
      (is (thrown? Exception (c-gf-repeat [1 2])))
      (is (thrown? Exception (c-gf-repeat ['a "a"])))
      (is (thrown? Exception (c-gf-repeat ['1 "a"]))))))

(deftest test-maybe-functor
  (testing "Test the Maybe protocol and types"
    (is "None" (cstr (None.)))
    (is "Some" (cstr (Some. "")))
    (is "Some 1" (cstr (Some. 1)))
    (is "Some []" (cstr (Some. [])))
    (is "Some val" (cstr (Some. "val")))))

(deftest test-get-or-else
  (testing "Test the get-or-else method"
    (is (thrown? Exception (get-or-else nil "jane")))
    (is "jane" (get-or-else (None.) "jane"))
    (is "joe" (get-or-else (Some. "joe") "jane"))))

(deftest test-maybe-alt
  (testing "Test: maybe-alt, contract for collections"
    (let [c-gf-repeat (c-coll-of gf-repeat)]
      (is (thrown? Exception ((maybe-alt c-gf-repeat) ["1" "2"])))
      (is (thrown? Exception ((maybe-alt c-gf-repeat) [(None.)])))
      (is (thrown? Exception ((maybe-alt c-gf-repeat) [(Some. "jim")])))
      (is (thrown? Exception ((maybe-alt c-gf-repeat) (Some. "jim"))))
      (is (thrown? Exception ((maybe-alt c-gf-repeat) (Some. ["jim" 1]))))

      (is (= (cstr (Some. ["jimjim" "jackjack"]))
             (cstr ((maybe-alt c-gf-repeat) (Some. ["jim" "jack"])))))
      (is (= (cstr (Some. [(gf-repeat "jim") (gf-repeat "jack")]))
             (cstr ((maybe-alt c-gf-repeat) (Some. ["jim" "jack"]))))))))

(deftest test-get-or-else
  (testing "Test get-or-else"
    (let [c-gf-repeat (c-coll-of gf-repeat)]
      (is (= [(gf-repeat "jim") (gf-repeat "jack")]
             (get-or-else ((maybe-alt c-gf-repeat) (Some. ["jim" "jack"]))
                        "jane"))))))

(deftest test-maybe-alt-with-protocol-Maybe
  (testing "Test: maybe-alt, protocol-Maybe"
    ;;> (= (java.lang.Object.) (java.lang.Object.))
    ;; => false
    (is (= (cstr (maybe (Some. "jim") gf-repeat))
           (cstr ((maybe-alt gf-repeat) (Some. "jim")))
           (cstr (Some. "jimjim"))))))

(deftest test-all
  (testing "Test: maybe-alt, contract for collections, get-or-else"
    (let [c-gf-repeat (c-coll-of gf-repeat)]
      (is (= (get-or-else (None.) "jane")
             (get-or-else ((maybe-alt c-gf-repeat) (None.)) "jane")))
      (is (= (cstr (Some. ["jimjim" "jackjack"]))
             (cstr (maybe (Some. ["jim" "jack"]) c-gf-repeat))
             (cstr (Some. [(gf-repeat "jim") (gf-repeat "jack")]))))

      (is (not= 1
                (cstr (maybe (Some. ["jim" "jack"]) c-gf-repeat)))))))

(deftest test-flatten
  (testing "Test: flatten"
    (is (= [[1 2 3] [4 5]]
           ((c-coll-of (c-coll-of c-any)) [[1 2 3] [4 5]])))
    (is (= [1 2 3 4 5]
           ((coll-of-flatten c-any) [[1 2 3] [4 5]])))))

(deftest test-maybe-flatten
  (testing "Test: maybe-flatten"
    (is (= (cstr (Some. [1 2 3 4 5]))
           (cstr (maybe-flatten (Some. [[1 2 3] [4 5]])))))))
