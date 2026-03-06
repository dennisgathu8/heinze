(ns argus.ui.ws
  (:require [cljs.reader :as reader]))

(defonce ws-conn (atom nil))
(defonce connected? (atom false))
(defonce messages (atom []))

(defn parse-edn [s]
  (try
    (reader/read-string s)
    (catch :default _ nil)))

(defn on-message [e]
  (when-let [data (parse-edn (.-data e))]
    (swap! messages (fn [msgs]
                      (vec (take-last 50 (conj msgs {:timestamp (js/Date.)
                                                      :data data})))))))

(defn on-open [_]
  (reset! connected? true)
  (js/console.log "WebSocket connected"))

;; Forward declare connect! for use in on-close
(declare connect!)

(defn on-close [_]
  (reset! connected? false)
  (js/console.log "WebSocket disconnected")
  (js/setTimeout connect! 3000))

(defn on-error [e]
  (js/console.error "WebSocket error:" e))

(defn connect! []
  (let [proto (if (= "https:" (.-protocol js/location)) "wss:" "ws:")
        host (.-host js/location)
        ws-url (if (= "localhost" (.-hostname js/location))
                 "ws://localhost:8081"
                 (str proto "//" host "/ws"))]
    (try
      (let [ws (js/WebSocket. ws-url)]
        (set! (.-onopen ws) on-open)
        (set! (.-onclose ws) on-close)
        (set! (.-onerror ws) on-error)
        (set! (.-onmessage ws) on-message)
        (reset! ws-conn ws))
      (catch :default e
        (js/console.error "Failed to connect:" e)
        (js/setTimeout connect! 3000)))))

(defn send! [data]
  (when-let [ws @ws-conn]
    (when (= 1 (.-readyState ws))
      (.send ws (pr-str data)))))
