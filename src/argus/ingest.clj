(ns argus.ingest
  (:require [argus.protocol :as p]
            [argus.util :as util]))

;; Ingest State for deduplication
(defonce ingest-history (atom #{}))

(defn seen?
  "Checks if data has been seen before using content hashing."
  [data]
  (let [h (util/content-hash data)]
    (contains? @ingest-history h)))

(defn mark-seen!
  "Marks data as seen."
  [data]
  (let [h (util/content-hash data)]
    (swap! ingest-history conj h)))

(defmulti ingest-stream
  "Ingests data from a source type."
  (fn [source-type data] source-type))

(defmethod ingest-stream :default [_ _]
  (throw (ex-info "Unknown source type" {})))

(defmethod ingest-stream :api/opta
  [_ data]
  (if (seen? data)
    nil ;; Duplicate, ignore
    (do
      (mark-seen! data)
      (let [clean-data (util/secure-read-string data)]
         ;; In a real impl, we'd map this to a Frame
         ;; For now, just return the raw map
         clean-data))))

(defmethod ingest-stream :web/whoscores
  [_ data]
    (if (seen? data)
    nil
    (do
      (mark-seen! data)
      (util/secure-read-string data))))
