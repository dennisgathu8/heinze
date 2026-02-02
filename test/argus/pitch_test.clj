(ns argus.pitch-test
  (:require [clojure.test :refer :all]
            [argus.pitch :as pitch]
            [argus.protocol :as p]))

(deftest structural-sharing-test
  (testing "Unchanged parts of the tree share memory references (identical?)"
    (let [p1 {:id "saka" :team :home :position [0 0 0] :velocity [0 0 0] :role :rw}
          p2 {:id "rice" :team :home :position [10 10 0] :velocity [0 0 0] :role :cm}
          
          frame-0 (p/map->Frame {:id 0 
                                 :timestamp 1000 
                                 :players {:home {:saka p1 :rice p2} :away {}}
                                 :ball {:position [50 50 0]}
                                 :phases {}
                                 :derived {}})
          
          ;; Update ONLY Saka's position
          diff {:timestamp 1001
                :players {:home {:saka {:id "saka" :team :home :position [1 1 0] :velocity [1 0 0] :role :rw}}}}
          
          frame-1 (pitch/next-frame frame-0 diff)]

      (is (= 1 (:id frame-1)))
      (is (= 1001 (:timestamp frame-1)))
      
      ;; 1. Check verified change
      (is (= [1 1 0] (get-in frame-1 [:players :home :saka :position])) "Saka should move")
      
      ;; 2. CRITICAL: Structural Sharing Checks
      (testing "Rice (unchanged player) should be identical object"
        (is (identical? (get-in frame-0 [:players :home :rice])
                        (get-in frame-1 [:players :home :rice]))
            "Rice object should be the exact same pointer in memory"))
      
      (testing "Away team map (unchanged) should be identical object"
        (is (identical? (get-in frame-0 [:players :away])
                        (get-in frame-1 [:players :away]))
            "Away team map should be shared"))
      
      (testing "Phases (unchanged) should be identical"
        (is (identical? (:phases frame-0) (:phases frame-1)))))))

(deftest history-test
  (pitch/init-pitch)
  (let [f0 (p/->Frame 0 100 {} {} {} {})
        f1 (pitch/next-frame f0 {:timestamp 200})]
    (pitch/record-frame! f0)
    (pitch/record-frame! f1)
    
    (is (= 2 (count (pitch/get-history))))
    (is (= f0 (first (pitch/get-history))))))
