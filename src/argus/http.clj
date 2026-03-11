(ns argus.http
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp]
            [clojure.java.io :as io]))

(defonce server-ref (atom nil))

(defn wrap-ip-whitelist [handler]
  (let [allowed-ip (some-> (System/getenv "ALLOWED_IP") clojure.string/trim)]
    (fn [request]
      (let [uri (:uri request)
            client-ip (some-> (get-in request [:headers "fly-client-ip"]) clojure.string/trim)]
        (println (str "🔍 REQUEST: " uri " | Client IP: " (or client-ip "NONE") " | Allowed IP: " (or allowed-ip "NOT-SET")))
        (if (or (= uri "/health")
                (nil? allowed-ip)
                (= allowed-ip client-ip))
          (let [response (handler request)]
            (-> response
                (resp/header "Cache-Control" "no-store, no-cache, must-revalidate, proxy-revalidate")
                (resp/header "Pragma" "no-cache")
                (resp/header "Expires" "0")))
          (do
            (println (str "❌ BLOCKED: " uri " | Expected: " allowed-ip " | Got: " client-ip))
            {:status 403 
             :headers {"Content-Type" "text/plain" 
                       "Cache-Control" "no-store"}
             :body (str "Forbidden: Unauthorized IP (" (or client-ip "NONE") ")")}))))))

(defn handler [request]
  (let [uri (:uri request)]
    (cond
      ;; Health check for Fly.io - keep public so Fly can monitor
      (= uri "/health")
      {:status 200 :body "ok"}
      
      ;; Serve index.html for root
      (= uri "/")
      (if-let [resource (io/resource "public/index.html")]
        (-> (resp/resource-response "public/index.html")
            (resp/content-type "text/html"))
        {:status 404 :body "Frontend not built yet"})
      
      ;; Serve static files from resources/public
      :else
      (let [path (str "public" uri)]
        (if-let [resource (io/resource path)]
          (resp/resource-response path)
          {:status 404 :body "Not found"})))))

(def app (wrap-ip-whitelist handler))

(defn start-server! [port]
  (when @server-ref
    (.stop @server-ref))
  (let [s (jetty/run-jetty app {:port port :host "0.0.0.0" :join? false})]
    (reset! server-ref s)
    s))

(defn stop-server! []
  (when-let [s @server-ref]
    (.stop s)
    (reset! server-ref nil)))
