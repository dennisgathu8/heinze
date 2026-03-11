(ns argus.http
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp]
            [clojure.java.io :as io]))

(defonce server-ref (atom nil))

(defn wrap-ip-whitelist [handler]
  (let [allowed-ip (System/getenv "ALLOWED_IP")]
    (when (nil? allowed-ip)
      (println "⚠️ WARNING: ALLOWED_IP environment variable is not set. Security is wide open!"))
    (fn [request]
      (let [client-ip (get-in request [:headers "fly-client-ip"])]
        (if (or (nil? allowed-ip)
                (= "localhost" (:server-name request))
                (= allowed-ip client-ip))
          (handler request)
          (do
            (println (str "❌ BLOCKED: IP mismatch. Expected: " allowed-ip " | Got: " client-ip))
            {:status 403 :body "Forbidden: Unauthorized IP"}))))))

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
