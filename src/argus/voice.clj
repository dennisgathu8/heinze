(ns argus.voice
  (:require [clojure.edn :as edn]
            [argus.util :as util])
  (:import [java.net InetSocketAddress]
           [org.java_websocket.server WebSocketServer]
           [org.java_websocket WebSocket]))

(defonce server-ref (atom nil))
(defonce clients (atom #{}))

;; -----------------------------------------------------------------------------
;; 1. Server Logic
;; -----------------------------------------------------------------------------

(defn on-open [conn handshake]
  (swap! clients conj conn)
  (println "New connection:" (.getRemoteSocketAddress conn)))

(defn on-close [conn code reason remote]
  (swap! clients disj conn)
  (println "Closed connection:" (.getRemoteSocketAddress conn)))

(defn on-message [conn message]
  (try
    (let [data (util/secure-read-string message)]
      (println "Received command:" data)
      ;; In a real impl, we would dispatch to Agent Delta here
      ;; For now, just echo validation success
      (.send conn (pr-str {:status :ack :received data})))
    (catch Exception e
      (println "Invalid message received:" (.getMessage e))
      (.send conn (pr-str {:status :error :message "Invalid EDN"})))))

(defn create-server [port]
  (proxy [WebSocketServer] [(InetSocketAddress. port)]
    (onOpen [conn handshake] (on-open conn handshake))
    (onClose [conn code reason remote] (on-close conn code reason remote))
    (onMessage [conn message] (on-message conn message))
    (onError [conn ex] (println "Error:" (.getMessage ex)))
    (onStart [] (println "Server started on port" port))))

(defn start-server! [port]
  (when @server-ref
    (.stop @server-ref))
  
  (let [s (create-server port)]
    (.start s)
    (reset! server-ref s)
    (reset! clients #{})
    s))

(defn stop-server! []
  (when-let [s @server-ref]
    (.stop s 1000) ;; Timeout 1s
    (reset! server-ref nil)
    (reset! clients #{})))

(defn broadcast!
  "Sends EDN data to all connected clients."
  [data]
  (let [msg (pr-str data)]
    (doseq [client @clients]
      (when (.isOpen client)
        (.send client msg)))))
