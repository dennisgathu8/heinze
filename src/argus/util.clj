(ns argus.util
  (:require [clojure.edn :as edn]
            [buddy.core.hash :as hash]
            [buddy.core.codecs :as codecs]))

(defn secure-read-string
  "Safely reads EDN string with no code execution allowed.
   Throws ex-info if malicious tags are present."
  [s]
  (edn/read-string {:readers *data-readers* :default (fn [tag value] (throw (ex-info "Unknown tag" {:tag tag})))} s))

(defn content-hash
  "Returns SHA-256 hash of a Clojure data structure."
  [data]
  (-> data
      pr-str
      hash/sha256
      codecs/bytes->hex))
