(ns stepper.app
  (:require [stepper.core :refer [forms-zip steps]]
            [om.core :as om]
            [om.dom :as dom]))

(def app-state
  (atom {:step 0
         :steps (steps (forms-zip '(+ (reduce + [1 2 3]) 4 5))
                       {'+ + 'reduce reduce})}))

(defn classify [form]
  (condp apply [form]
    false? :bool
    fn? :fn
    keyword? :keyword
    map? :map
    nil? :nil
    number? :number
    seq? :seq
    string? :string
    symbol? :symbol
    true? :bool
    vector? :vector))

(defn form-view [data owner]
  (reify om/IRender
    (render [_]
      (let [selected? (when (map? data) (:stepper.core/selected? data))
            form (if selected? (:stepper.core/form data) data)]
        (apply dom/div
          #js {:className (str "form "
                               (when selected? "selected ")
                               (name (classify form)))}
          (cond
            (fn? form) "#<function>"
            (map? form)
              (om/build-all form-view (interleave (keys form) (vals form)))
            (coll? form) (om/build-all form-view form)
            :else (pr-str form)))))))

(defn main-view [data owner]
  (reify om/IRender
    (render [_]
      (let [{:keys [step steps]} data]
        (dom/div #js {:className "stepper"}
          (dom/input
            #js {:type "range"
                 :min 0
                 :max (-> steps count dec)
                 :value step
                 :onChange #(om/update! data :step
                              (js/parseInt (.. % -target -value)))})
          (om/build form-view (nth steps step)))))))

(defn init []
  (om/root main-view app-state
           {:target (.getElementById js/document "demo")}))

(init)
