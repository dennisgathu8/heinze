(ns argus.heinze-test
  (:require [clojure.test :refer :all]
            [argus.heinze :as heinze]
            [argus.protocol :as p]))

(deftest detect-high-line-test
  (testing "Detects high line exposure"
    (let [frame (p/map->Frame {:derived {:defensive-line-height 50
                                         :opponent-max-pace 8}
                               :id 1
                               :timestamp 100})
          alerts (heinze/analyze-frame frame [])]
      (is (= 1 (count alerts)))
      (is (= :high-line-exposure (:type (first alerts))))))

  (testing "Ignores safe line"
    (let [frame (p/map->Frame {:derived {:defensive-line-height 30
                                         :opponent-max-pace 8}
                               :id 1
                               :timestamp 100})
          alerts (heinze/analyze-frame frame [])]
      (is (empty? alerts)))))

(deftest detect-press-fatigue-test
  (testing "Detects fatigue when success rate < 30%"
    (let [frame (p/map->Frame {:id 100 :timestamp 1000})
          ;; Create a window of 10 frames where only 2 were successful presses
          window (into [] (concat (repeat 2 {:phases {:press-event true :press-success true}})
                                  (repeat 8 {:phases {:press-event true :press-success false}})))
          alerts (heinze/analyze-frame frame window)]
      
      (is (= 1 (count alerts)))
      (is (= :press-fatigue (:type (first alerts))))))

  (testing "No fatigue when success rate is high"
    (let [frame (p/map->Frame {:id 100 :timestamp 1000})
          window (repeat 10 {:phases {:press-event true :press-success true}})
          alerts (heinze/analyze-frame frame window)]
      (is (empty? alerts)))))

(deftest detect-flank-overload-test
  (testing "Detects right flank overload"
    (let [opp-players {:p1 {:position [50 90]} 
                       :p2 {:position [60 92]}
                       :p3 {:position [70 88]}
                       :p4 {:position [55 95]}
                       :p5 {:position [80 86]}} ;; 5 players in y > 85
          frame (p/map->Frame {:players {:away opp-players}
                               :id 1 :timestamp 100})
          alerts (heinze/analyze-frame frame [])]
      
      (is (= 1 (count alerts)))
      (is (= :flank-overload (:type (first alerts))))
      (is (= "Shift defensive block right" (:recommendation (first alerts)))))))
