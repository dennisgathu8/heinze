(ns argus.ui.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [argus.ui.ws :as ws]))

;; ---- State ----
(defonce fork-params (r/atom {:press-height 55
                               :line-depth 40
                               :tempo 1.0}))
(defonce alerts (r/atom []))
(defonce ghost-results (r/atom []))
(defonce frame-count (r/atom 0))

;; ---- Helpers ----
(defn format-time [d]
  (let [h (.getHours d)
        m (.getMinutes d)
        s (.getSeconds d)]
    (str (when (< h 10) "0") h ":"
         (when (< m 10) "0") m ":"
         (when (< s 10) "0") s)))

;; ---- Components ----
(defn header []
  [:div.header
   [:div.header-brand
    [:span.header-logo "👁️"]
    [:div
     [:div.header-title "HEINZE"]
     [:div.header-subtitle "THE 100-EYED TACTICAL ORACLE"]]]
   [:div.header-status
    [:div {:class (str "status-dot " (when-not @ws/connected? "offline"))}]
    [:span (if @ws/connected? "CONNECTED" "OFFLINE")]]])

(defn stat-card [icon value label]
  [:div.stat-card
   [:span.stat-icon icon]
   [:div.stat-content
    [:span.stat-value value]
    [:span.stat-label label]]])

(defn stats-bar []
  [:div.stats-bar
   [stat-card "📡" (str (count @ws/messages)) "Events Received"]
   [stat-card "⚡" (str @frame-count) "Frames Processed"]
   [stat-card "🚨" (str (count @alerts)) "Active Alerts"]
   [stat-card "👻" (str (count @ghost-results)) "Ghost Matches"]])

(defn live-feed-panel []
  [:div.panel
   [:div.panel-header
    [:span.panel-title "Live Feed"]
    [:span {:class "panel-badge live"} "● LIVE"]]
   [:div.panel-body
    (if (empty? @ws/messages)
      [:div.empty-state
       [:div.empty-state-icon "📡"]
       [:span "Awaiting data stream..."]]
      (for [[idx msg] (map-indexed vector (reverse @ws/messages))]
        ^{:key idx}
        [:div.feed-item
         [:span.feed-timestamp (format-time (:timestamp msg))]
         [:span.feed-content (pr-str (:data msg))]]))]])

(defn alert-card-component [{:keys [type severity recommendation confidence]}]
  [:div {:class (str "alert-card " (name severity))}
   [:div.alert-type (name type)]
   [:div.alert-recommendation recommendation]
   [:div.alert-confidence
    [:div.confidence-bar
     [:div.confidence-fill {:style {:width (str (* confidence 100) "%")}}]]
    [:span.confidence-value (str (int (* confidence 100)) "%")]]])

(defn alerts-panel []
  [:div.panel
   [:div.panel-header
    [:span.panel-title "Tactical Alerts"]
    [:span {:class "panel-badge live"} (str (count @alerts))]]
   [:div.panel-body
    (if (empty? @alerts)
      [:div.empty-state
       [:div.empty-state-icon "🛡️"]
       [:span "No vulnerabilities detected"]]
      (for [[idx alert] (map-indexed vector @alerts)]
        ^{:key idx}
        [alert-card-component alert]))]])

(defn fork-panel []
  [:div.panel
   [:div.panel-header
    [:span.panel-title "Fork Reality"]
    [:span {:class "panel-badge live"} "GHOST"]]
   [:div.panel-body
    
    [:div.fork-section
     [:div.fork-label "Press Height"]
     [:input.fork-slider {:type "range" :min 20 :max 80
                          :value (:press-height @fork-params)
                          :on-change #(swap! fork-params assoc :press-height (int (.. % -target -value)))}]
     [:div.fork-value (str (:press-height @fork-params) "m")]]
    
    [:div.fork-section
     [:div.fork-label "Defensive Line Depth"]
     [:input.fork-slider {:type "range" :min 20 :max 60
                          :value (:line-depth @fork-params)
                          :on-change #(swap! fork-params assoc :line-depth (int (.. % -target -value)))}]
     [:div.fork-value (str (:line-depth @fork-params) "m")]]
    
    [:div.fork-section
     [:div.fork-label "Tempo"]
     [:input.fork-slider {:type "range" :min 5 :max 20 :step 1
                          :value (int (* 10 (:tempo @fork-params)))
                          :on-change #(swap! fork-params assoc :tempo (/ (int (.. % -target -value)) 10.0))}]
     [:div.fork-value (str (:tempo @fork-params) "x")]]
    
    [:button.fork-btn
     {:on-click #(ws/send! {:command :fork :params @fork-params})
      :disabled (not @ws/connected?)}
     "⚡ FORK REALITY"]
    
    (for [[idx result] (map-indexed vector (reverse @ghost-results))]
      ^{:key idx}
      [:div.ghost-result
       [:div.ghost-result-title (str "Ghost #" (inc idx))]
       [:div.ghost-result-data (pr-str result)]])]])

(defn app []
  [:div
   [header]
   [:div.dashboard
    [stats-bar]
    [live-feed-panel]
    [alerts-panel]
    [fork-panel]]])

;; ---- Process incoming messages ----
(defn process-messages! []
  (doseq [{:keys [data]} @ws/messages]
    (when-let [t (:type data)]
      (cond
        (#{:high-line-exposure :press-fatigue :flank-overload} t)
        (swap! alerts (fn [a] (vec (take-last 10 (conj a data)))))
        
        (= :ghost-result t)
        (swap! ghost-results (fn [g] (vec (take-last 5 (conj g data)))))))
    
    (when (:frame-id data)
      (reset! frame-count (:frame-id data)))))

;; ---- Init ----
(defn init! []
  (ws/connect!)
  ;; Process messages periodically
  (js/setInterval process-messages! 1000)
  (rdom/render [app] (.getElementById js/document "app")))
