(ns argus.protocol)

(defrecord Frame [id timestamp players ball phases derived])
(defrecord Player [id team position velocity role])
(defrecord Alert [type severity recommendation confidence frame-hash])
(defrecord Ghost [ghost-id base-frame-id params seed outcomes probability-tree])

(defprotocol Ingest
  (ingest-stream [this source-type data] "Ingest data from a source and normalize it."))
