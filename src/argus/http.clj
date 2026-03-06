(ns argus.http
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp]
            [clojure.java.io :as io]))

(defonce server-ref (atom nil))

(defn handler [request]
  (let [uri (:uri request)]
    (cond
      ;; Health check for Fly.io
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

(defn start-server! [port]
  (when @server-ref
    (.stop @server-ref))
  (let [s (jetty/run-jetty handler {:port port :join? false})]
    (reset! server-ref s)
    s))

(defn stop-server! []
  (when-let [s @server-ref]
    (.stop s)
    (reset! server-ref nil)))
