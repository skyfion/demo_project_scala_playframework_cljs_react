(ns main
  (:require [rum.core :as rum]))

;; application state
(def state (atom nil))

(def initial-state
  {:cards (group-by :id [{:id          1
                          :name        "test1"
                          :description "desc 1"
                          :status      "todo"}
                         {:id          2
                          :name        "test2"
                          :description "desc 2"
                          :status      "done"}
                         {:id          3
                          :name        "test3"
                          :description "desc 333awd awdaw awdawd awd aw"
                          :status      "done"}])})

(rum/defc card-form < rum/reactive [card-ref]
  (let [card (first @card-ref)
        {:keys [name description status]} card]
    [:div.card-form
     [:label (str "name: " name)]
     [:label (str "description: " description)]
     [:label "status: "]
     [:select {:on-change (fn [e] (let [val (.-value (.-target e))]
                                    (swap! card-ref assoc-in [0 :status] val))) :value status}
      [:option {:value "todo"}  "TODO"]
      [:option {:value "in_progress"}  "INPROGRESS"]
      [:option {:value "done"} "DONE"]]
     [:button.button.button-outline "Edit"]
     [:button.button.button-outline "Delete"]]))

(rum/defc card < rum/reactive [card-ref]
  (let [card (first (rum/react card-ref))
        status (:status card)
        id (:id card)
        status? #(when (= % status) (card-form card-ref))]
    [:div.row {:id id}
     [:div.column {:id "todo"} (status? "todo")]
     [:div.column {:id "in_progress"} (status? "in_progress")]
     [:div.column {:id "done"} (status? "done")]]))

(rum/defc app < rum/reactive []
  [:div.container
   [:div.row [:h3 "Bug tracker app"]]
   [:div.row
    [:div.column [:h5 "TODO"]]
    [:div.column [:h5 "IN PROGRESS"]]
    [:div.column [:h5 "DONE"]]]
   (for [k (keys (:cards @state))]
     (card (rum/cursor-in state [:cards k])))
   ])

(reset! state initial-state)

(rum/mount
  (app)
  (.getElementById js/document "app"))

