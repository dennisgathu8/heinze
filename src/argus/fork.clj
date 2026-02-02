(ns argus.fork
  (:require [argus.protocol :as p]
            [argus.pitch :as pitch]))

;; -----------------------------------------------------------------------------
;; 1. Fork Core
;; -----------------------------------------------------------------------------

(defn fork-at
  "O(1) Fork: Creates a Ghost record from a base frame.
   No deep copying of the frame happens here, just a reference."
  [frame params seed]
  (p/map->Ghost
    {:ghost-id (java.util.UUID/randomUUID)
     :base-frame-id (:id frame)
     :params params
     :seed seed
     :outcomes []
     :probability-tree {}}))

;; -----------------------------------------------------------------------------
;; 2. Probabilistic Simulation
;; -----------------------------------------------------------------------------

(defn simulate-step
  "Deterministic step function based on seed.
   Returns a NEW frame (simulated) 1 tick in the future."
  [frame params seed tick]
  ;; Trivial simulation logic for demo purposes:
  ;; Move ball based on params and pseudo-randomness from seed+tick
  (let [r (java.util.Random. (+ seed tick))
        dx (- (.nextDouble r) 0.5)
        dy (- (.nextDouble r) 0.5)
        [bx by bz] (get-in frame [:ball :position] [0 0 0])
        new-ball {:position [(+ bx (* dx (:tempo params 1))) 
                             (+ by (* dy (:tempo params 1))) 
                             bz]
                  :velocity [dx dy 0]}
        
        ;; Use pitch/next-frame logic mostly, but inject our simulated ball
        ;; This reuses the structural sharing of pitch/next-frame
        diff {:timestamp (+ (:timestamp frame) 100)
              :ball new-ball}]
    
    (pitch/next-frame frame diff)))

(defn run-simulation
  "Runs a single simulation rollout for N steps."
  [base-frame params seed duration-ticks]
  (loop [current-frame base-frame
         tick 0
         history []]
    (if (>= tick duration-ticks)
      history
      (let [next-f (simulate-step current-frame params seed tick)]
        (recur next-f (inc tick) (conj history next-f))))))

;; -----------------------------------------------------------------------------
;; 3. Parallel Execution (pmap)
;; -----------------------------------------------------------------------------

(defn simulate-parallel
  "Runs M rollouts in parallel using pmap.
   Each rollout gets a different derived seed."
  [base-frame params base-seed num-rollouts duration-ticks]
  (let [seeds (range base-seed (+ base-seed num-rollouts))
        ;; PMAP MAGIC: Runs in parallel on available cores
        results (pmap (fn [seed]
                        (run-simulation base-frame params seed duration-ticks))
                      seeds)]
    (doall results))) ;; Force realization
