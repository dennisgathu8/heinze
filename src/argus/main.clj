(ns argus.main
  (:require [argus.secure :as secure]
            [argus.ingest :as ingest]
            [argus.voice :as voice]
            [argus.pitch :as pitch]
            [argus.fork :as fork]
            [argus.heinze :as heinze])
  (:gen-class))

(defn -main [& args]
  (println "👁️ ARGUS: The 100-Eyed Tactical Oracle 👁️")
  (println "========================================")
  
  (try
    ;; 1. Security Audit
    (secure/audit-source!)
    (secure/verify-integrity!)
    
    ;; 2. Initialize Subsystems
    (println "🚀 Initializing Agents...")
    
    (pitch/init-pitch)
    (println " -> Agent Beta (Pitch) initialized.")
    
    ;; 3. Start Voice Server
    (voice/start-server! 8080)
    (println " -> Agent Epsilon (Voice) listening on port 8080.")
    
    ;; 4. Wait for match start (Simulation Loop)
    (println "🟢 System Ready. Awaiting data stream...")
    
    ;; Keep thread alive
    (loop []
        (Thread/sleep 5000)
        ;; Health check logging could go here
        (recur))
        
  (catch Exception e
    (println "❌ FATAL ERROR:" (.getMessage e))
    (when-let [d (ex-data e)]
      (println "   Details:" d))
    (System/exit 1))))
