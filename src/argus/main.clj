(ns argus.main
  (:require [argus.secure :as secure]
            [argus.ingest :as ingest]
            [argus.voice :as voice]
            [argus.pitch :as pitch]
            [argus.fork :as fork]
            [argus.heinze :as heinze]
            [argus.http :as http])
  (:gen-class))

(defn shutdown-hook []
  (Thread.
    (fn []
      (println "\n🛑 SIGTERM received. Shutting down gracefully...")
      (voice/stop-server!)
      (http/stop-server!)
      (println "👋 Argus offline."))))

(defn -main [& args]
  (println "👁️ ARGUS: The 100-Eyed Tactical Oracle 👁️")
  (println "========================================")
  
  (try
    ;; 1. Security Audit
    (secure/audit-source!)
    (secure/verify-integrity!)
    
    ;; 2. Register shutdown hook
    (.addShutdownHook (Runtime/getRuntime) (shutdown-hook))
    
    ;; 3. Initialize Subsystems
    (println "🚀 Initializing Agents...")
    
    (pitch/init-pitch)
    (println " -> Agent Beta (Pitch) initialized.")
    
    ;; 4. Start Voice Server (WebSocket)
    (voice/start-server! 8081)
    (println " -> Agent Epsilon (Voice) WebSocket on port 8081.")
    
    ;; 5. Start HTTP Server (serves frontend + proxies health)
    (http/start-server! 8080)
    (println " -> HTTP Server on port 8080.")
    
    ;; 6. Ready
    (println "🟢 System Ready. Dashboard: http://localhost:8080")
    
    ;; Keep thread alive
    (loop []
        (Thread/sleep 5000)
        (recur))
        
  (catch Exception e
    (println "❌ FATAL ERROR:" (.getMessage e))
    (when-let [d (ex-data e)]
      (println "   Details:" d))
    (System/exit 1))))
