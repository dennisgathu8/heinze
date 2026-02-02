(ns argus.heinze
  (:require [argus.protocol :as p]
            [argus.util :as util]))

;; -----------------------------------------------------------------------------
;; 1. Detectors (Pure Functions)
;; -----------------------------------------------------------------------------

(defn detect-high-line
  "Detector: HIGH_LINE_EXPOSURE
   Trigger: Defensive line > 45m AND Opponent pace threat > 7m/s"
  [frame window]
  (let [def-line-height (get-in frame [:derived :defensive-line-height] 0)
        opp-pace (get-in frame [:derived :opponent-max-pace] 0)]
    (when (and (> def-line-height 45)
               (> opp-pace 7))
      (p/map->Alert {:type :high-line-exposure
                     :severity :high
                     :recommendation "Drop line depth or switch to low block"
                     :confidence 0.85
                     :frame-hash (util/content-hash frame)}))))

(defn calculate-press-success-rate
  "Helper: Calculates press success rate over a window of frames."
  [window]
  (let [press-events (filter #(get-in % [:phases :press-event]) window)
        successful (filter #(get-in % [:phases :press-success]) press-events)
        total (count press-events)]
    (if (pos? total)
      (double (/ (count successful) total))
      1.0))) ;; Default to 1.0 if no press events to avoid false positive fatigue

(defn detect-press-fatigue
  "Detector: PRESS_FATIGUE
   Trigger: Press success rate < 30% over 5-minute window"
  [frame window]
  ;; Assuming window contains enough history for 5 mins or is the sliding window itself
  (let [success-rate (calculate-press-success-rate window)]
    (when (< success-rate 0.30)
      (p/map->Alert {:type :press-fatigue
                     :severity :medium
                     :recommendation "Trigger mid-block rest phase"
                     :confidence 0.75
                     :frame-hash (util/content-hash frame)}))))

(defn count-players-in-zone
  [players x-start x-end y-start y-end]
  (count (filter (fn [p]
                   (let [[x y] (:position p)]
                     (and (>= x x-start) (< x x-end)
                          (>= y y-start) (< y y-end))))
                 (vals players))))

(defn detect-flank-overload
  "Detector: FLANK_OVERLOAD
   Trigger: Opponent concentrates > 4 players in a flank zone (15m width)"
  [frame window]
  (let [opp-players (get-in frame [:players :away]) ;; Assuming analysis is for home team perspective
        ;; Simplified zones for demo
        left-flank-count (count-players-in-zone opp-players 0 100 0 15)
        right-flank-count (count-players-in-zone opp-players 0 100 85 100)]
    
    (cond
      (> left-flank-count 4)
      (p/map->Alert {:type :flank-overload
                     :severity :high
                     :recommendation "Shift defensive block left"
                     :confidence 0.90
                     :frame-hash (util/content-hash frame)})
      
      (> right-flank-count 4)
      (p/map->Alert {:type :flank-overload
                     :severity :high
                     :recommendation "Shift defensive block right"
                     :confidence 0.90
                     :frame-hash (util/content-hash frame)})
      
      :else nil)))

;; -----------------------------------------------------------------------------
;; 2. Analysis Engine
;; -----------------------------------------------------------------------------

(def detectors [detect-high-line detect-press-fatigue detect-flank-overload])

(defn analyze-frame
  "Applies all detectors to a frame context.
   Returns a sequence of Alert records."
  [frame window]
  (->> detectors
       (map (fn [f] (f frame window)))
       (remove nil?)))
