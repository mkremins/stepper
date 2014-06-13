(ns stepper.core
  (:require [clojure.zip :as z]))

(defn forms-zip
  "Constructs and returns a zipper over a tree of Clojure forms."
  [form]
  (z/zipper coll?
            #(if (map? %) (interleave (keys %) (vals %)) (seq %))
            #(cond
               (map? %1) (apply hash-map %2)
               (seq? %1) %2
               (vector? %1) (vec %2))
            form))

(defn constant?
  "Returns true if `form` is either:

   * an immutable atomic value
   * an empty immutable collection
   * an immutable collection whose every item is constant
   * a function

   Otherwise, returns false."
  [form]
  (if (coll? form)
    (or (empty? form) (every? constant? form))
    (or (false? form) (fn? form) (keyword? form) (nil? form) (number? form)
        (string? form) (true? form))))

(defn collapsible?
  "Returns true if `form` is either:

  * a symbol
  * a non-empty seq whose every item is constant

  Otherwise, returns false."
  [form]
  (or (symbol? form)
      (and (seq? form) (seq form) (every? constant? form))))

(defn collapse
  "Given a `form` for which `collapsible?` returns true and an `env` in which
   to look up symbols, resolves and returns `form`'s constant value."
  [form env]
  (if (seq? form)
    (apply (first form) (rest form))
    (env form)))

(defn step
  "A generic approach to traversing a tree of Clojure forms, evaluating it
   gradually as we go. `loc` is a zipper created by `forms-zip`. `env` is a map
   whose every key-value pair is a symbol-constant binding.

   * Non-empty seqs of constants are evaluated as function calls and replaced
     by their return values.
   * Symbols are replaced by their bound values according to `env`.
   * Constants (including collections of only constant items) are skipped over.
   * Non-constant collections are walked, item by item.

   Returns nil to signal that the traversal is complete when the top level of
   the tree has been fully evaluated to a constant."
  [loc env]
  (when loc
    (let [form (z/node loc)]
      (cond
        (collapsible? form) (z/edit loc collapse env)
        (constant? form)
          (when-let [loc' (or (z/right loc) (z/up loc))] (recur loc' env))
        :else (recur (z/down loc) env)))))

(defn steps
  "Given a `loc` and `env` as specified by `step`, returns a sequence of steps
   taken in the process of evaluating the forms under `loc`."
  [loc env]
  (->> loc
    (iterate #(step % env))
    (take-while identity)
    (map #(z/edit % (fn [f] {::selected? true ::form f})))
    (map z/root)
    vec))
