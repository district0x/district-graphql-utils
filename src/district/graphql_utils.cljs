(ns district.graphql-utils
  (:require
    [camel-snake-kebab.core :as cs :include-macros true]
    [camel-snake-kebab.extras :refer [transform-keys]]
    [clojure.string :as string]
    [clojure.walk :as walk]
    [district.cljs-utils :refer [js-obj->clj]]))


(defn kw->gql-name [kw]
  (let [nm (name kw)]
    (str
      (when (string/starts-with? nm "__")
        "__")
      (when (and (keyword? kw)
                 (namespace kw))
        (str (string/replace (cs/->camelCase (namespace kw)) "." "_") "_"))
      (let [first-letter (first nm)
            last-letter (last nm)
            s (if (and (not= first-letter "_")
                       (= first-letter (string/upper-case first-letter)))
                (cs/->PascalCase nm)
                (cs/->camelCase nm))]
        (if (= last-letter "?")
          (.slice s 0 -1)
          s))
      (when (string/ends-with? nm "?")
        "_"))))


(defn gql-name->kw [gql-name]
  (let [k (name gql-name)]
    (if (string/starts-with? k "__")
      (keyword k)
      (let [k (if (string/ends-with? k "_")
                (str (.slice k 0 -1) "?")
                k)
            parts (string/split k "_")
            parts (if (< 2 (count parts))
                    [(string/join "." (butlast parts)) (last parts)]
                    parts)]
        (apply keyword (map cs/->kebab-case parts))))))


(defn clj->js-root-value [root-value & [opts]]
  (let [gql-name->kw (or (:gql-name->kw opts) gql-name->kw)
        kw->gql-name (or (:kw->gql-name opts) kw->gql-name)]
    (if (map? root-value)
      (clj->js (into {} (map (fn [[k v]]
                               [(kw->gql-name k)
                                (if (fn? v)
                                  (fn [params context schema]
                                    (let [parsed-params (transform-keys gql-name->kw (js->clj params))
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


(defn js->clj-response [res & [opts]]
  (let [gql-name->kw (or (:gql-name->kw opts) gql-name->kw)
        resp (js->clj-result-objects res)]
    (update resp :data #(transform-keys gql-name->kw %))))