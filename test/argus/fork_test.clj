(ns argus.fork-test
  (:require [clojure.test :refer :all]
            [argus.fork :as fork]
            [argus.protocol :as p]
            [argus.pitch :as pitch]))

(deftest fork-semantics-test
  (testing "Fork is O(1) - creates record without deep copy logic"
    (let [frame (p/map->Frame {:id 100 :timestamp 5000 :players {} :ball {}})
          ghost (fork/fork-at frame {:tempo 1.5} 12345)]
      (is (= 100 (:base-frame-id ghost)))
      (is (= 12345 (:seed ghost))))))

(deftest determinism-test
  (testing "Simulation is deterministic given same seed"
    (let [frame (p/map->Frame {:id 1 :timestamp 0 :ball {:position [50 50 0]} :players {}})
          params {:tempo 1.0}
          seed 42
          
          run-1 (fork/run-simulation frame params seed 10)
          run-2 (fork/run-simulation frame params seed 10)]
      
      (is (= (count run-1) (count run-2)))
      ;; Check final state equality
      (is (= (last run-1) (last run-2))))))

(deftest parallel-performance-test
  (testing "pmap parallel execution works"
    (let [frame (p/map->Frame {:id 1 :timestamp 0 :ball {:position [50 50 0]} :players {}})
          params {:tempo 1.0}
          start (System/currentTimeMillis)
          ;; Run 100 rollouts of 50 ticks
          results (fork/simulate-parallel frame params 100 100 50)
          end (System/currentTimeMillis)]
      
      (is (= 100 (count results)))
      (is (= 50 (count (first results))))
      (println "Simulated 5000 frames (100x50) in" (- end start) "ms"))))
