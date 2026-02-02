(ns argus.pitch
  (:require [argus.protocol :as p]
            [clojure.spec.alpha :as s]))

;; -----------------------------------------------------------------------------
;; 1. Frame Contract & Specs
;; -----------------------------------------------------------------------------

(s/def ::id any?)
(s/def ::position (s/coll-of number? :kind vector? :count 3))
(s/def ::velocity (s/coll-of number? :kind vector? :count 3))
(s/def ::team #{:home :away})

(s/def ::player (s/keys :req-un [::id ::team ::position ::velocity]))
(s/def ::ball (s/keys :req-un [::position ::velocity]))
(s/def ::timestamp int?)

(s/def ::frame (s/keys :req-un [::p/id ::timestamp ::p/players ::p/ball]))

;; -----------------------------------------------------------------------------
;; 2. State & History Management
;; -----------------------------------------------------------------------------

(defonce match-history (atom []))

(defn init-pitch []
  (reset! match-history []))

(defn get-history []
  @match-history)

;; -----------------------------------------------------------------------------
;; 3. Immutable Transitions (The Core)
;; -----------------------------------------------------------------------------

(defn- update-players
  "Updates players based on diff.
   CRITICAL: If a player is not in the diff, their record is NOT touched, 
   preserving structural sharing in the players map."
  [current-players player-diffs]
  (reduce-kv
    (fn [players team team-diffs]
      (let [updated-team (reduce-kv
                           (fn [team-players player-id new-state]
                             ;; Only assoc if data actually changed to avoid unnecessary path copying
                             ;; though clojure's assoc is already smart, explicit structural sharing intent here.
                             (assoc team-players player-id (p/map->Player new-state)))
                           (get players team)
                           team-diffs)]
        (assoc players team updated-team)))
    current-players
    player-diffs))

(defn next-frame
  "Pure function to produce the next frame from a previous frame and a diff using structural sharing.
   diff format: {:timestamp 123 :ball {...} :players {:home {:p1 {...}} ...}}"
  [prev-frame diff]
  (let [base (or prev-frame (p/->Frame 0 0 {} {} {} {}))
        new-id (inc (:id base))
        ;; Only merge top-level keys that exist in diff
        ;; For deeply nested structures like players, we delegate to specialized updaters
        ;; to maximize sharing.
        
        ;; 1. Basic fields
        next-f (assoc base 
                      :id new-id 
                      :timestamp (:timestamp diff (:timestamp base)))
        
        ;; 2. Ball (atomic replacement usually fine, but let's check)
        next-f (if-let [bola (:ball diff)]
                 (assoc next-f :ball bola)
                 next-f)

        ;; 3. Players (Deep update with sharing)
        next-f (if-let [p-diff (:players diff)]
                 (assoc next-f :players (update-players (:players base) p-diff))
                 next-f)]
    
    next-f))

(defn record-frame!
  "Records a new frame into the immutable history."
  [frame]
  (swap! match-history conj frame))
