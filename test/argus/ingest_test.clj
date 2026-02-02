(ns argus.ingest-test
  (:require [clojure.test :refer :all]
            [argus.ingest :as ingest]
            [argus.util :as util]))

(deftest secure-read-test
  (testing "Securely reads valid EDN"
    (is (= {:foo "bar"} (util/secure-read-string "{:foo \"bar\"}"))))

  (testing "Rejects malicious code"
    (is (thrown? clojure.lang.ExceptionInfo
                 (util/secure-read-string "#java.util.Date[]")))))

(deftest deduplication-test
  (reset! ingest/ingest-history #{})
  (let [data "{:event :goal :player \"Saka\"}"]
    (testing "First ingest returns data"
      (is (= {:event :goal :player "Saka"} (ingest/ingest-stream :api/opta data))))
    
    (testing "Second ingest returns nil (duplicate)"
      (is (nil? (ingest/ingest-stream :api/opta data))))))

(deftest ingest-dispatch-test
   (testing "Dispatches to correct method"
     (reset! ingest/ingest-history #{})
     (is (= {:source "whoscores"} (ingest/ingest-stream :web/whoscores "{:source \"whoscores\"}")))))
