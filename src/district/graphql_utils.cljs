(ns district.graphql-utils
  (:require
    [camel-snake-kebab.core :as cs :include-macros true]
    [camel-snake-kebab.extras :refer [transform-keys]]
    [clojure.string :as string]
    [clojure.walk :as walk]
    [district.cljs-utils :refer [js-obj->clj]]))


(defn kw->gql-name [kw]
  (str
    (when (string/starts-with? (name kw) "__")
      "__")
    (when (and (keyword? kw)
               (namespace kw))
      (str (string/replace (cs/->camelCase (namespace kw)) "." "_") "_"))
    (let [k (name kw)
          first-letter (first k)]
      (if (and (not= first-letter "_")
               (= first-letter (string/upper-case first-letter)))
        (cs/->PascalCase k)
        (cs/->camelCase k)))))


(defn gql-name->kw [gql-name]
  (let [k (name gql-name)]
    (if (string/starts-with? k "__")
      (keyword k)
      (let [parts (string/split k "_")
            parts (if (< 2 (count parts))
                    [(string/join "." (butlast parts)) (last parts)]
                    parts)]
        (apply keyword (map cs/->kebab-case parts))))))


(defn clj->js-root-value [root-value & [{:keys [:gql-name->kw-fn :kw->gql-name-fn]}]]
  (let [gql-name->kw-fn (or gql-name->kw-fn gql-name->kw)
        kw->gql-name-fn (or kw->gql-name-fn kw->gql-name)]
    (if (map? root-value)
      (clj->js (into {} (map (fn [[k v]]
                               [(kw->gql-name-fn k)
                                (if (fn? v)
                                  (fn [params context schema]
                                    (let [parsed-params (transform-keys gql-name->kw-fn (js->clj params))
                                          result (clj->js-root-value (v parsed-params context schema))]
                                      result))
                                  v)])
                             root-value)))
      (if (sequential? root-value)
        (clj->js (map clj->js-root-value root-value))
        root-value))))


(defn- js->clj-result-objects [res]
  (walk/prewalk (fn [x]
                  (if (and (nil? (type x))
                           (seq (js-keys x)))
                    (js-obj->clj x)
                    (js->clj x)))
                (js->clj res :keywordize-keys true)))


(defn js->clj-response [res & [{:keys [:gql-name->kw-fn]}]]
  (let [gql-name->kw-fn (or gql-name->kw-fn gql-name->kw)
        resp (js->clj-result-objects res)]
    (update resp :data #(transform-keys gql-name->kw-fn %))))