(ns argus.voice-test
  (:require [clojure.test :refer :all]
            [argus.voice :as voice]
            [clojure.edn :as edn])
  (:import [org.java_websocket.client WebSocketClient]
           [java.net URI]))

(deftest websocket-server-test
  (testing "Server starts, accepts connections, and handles messages"
    (voice/start-server! 8081)
    ;; Give server time to start (important in CI)
    (Thread/sleep 500)
    
    (let [received (promise)
          client (proxy [WebSocketClient] [(URI. "ws://localhost:8081")]
                   (onOpen [handshake] )
                   (onMessage [message] (deliver received message))
                   (onClose [code reason remote] )
                   (onError [ex] ))]
      
      (try
        (.connectBlocking client)
        
        (is (.isOpen client) "Client should be connected")
        
        (when (.isOpen client)
          ;; Send command to server
          (.send client "{:command :fork :param 123}")
          
          (let [resp (edn/read-string (deref received 3000 nil))]
            (is (= :ack (:status resp)))
            (is (= {:command :fork :param 123} (:received resp)))))
        
        (finally
          (when (.isOpen client) (.close client))
          (voice/stop-server!))))))
