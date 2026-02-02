(ns argus.secure-test
  (:require [clojure.test :refer :all]
            [argus.secure :as secure]))

(deftest audit-test
  (testing "Detects forbidden tokens in code"
    (is (thrown? clojure.lang.ExceptionInfo
                 (secure/scan-line "(eval '(+ 1 1))" 1 "test.clj"))))

  (testing "Ignores forbidden tokens in comments"
    (is (nil? (secure/scan-line ";; verify eval is not used" 1 "test.clj"))))
    
  (testing "Ignores read-string in util.clj (allowlist)"
    (is (nil? (secure/scan-line "(edn/read-string ...)" 1 "src/argus/util.clj")))))
