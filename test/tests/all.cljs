(ns tests.all
  (:require
   [bignumber.core :as bn]
   [cljs-time.core :as t]
   [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
   [cljsjs.graphql]
   [district.graphql-utils :as graphql-utils]))

(deftest tests
  (is (= "abc" (graphql-utils/kw->gql-name :abc)))
  (is (= "__typename" (graphql-utils/kw->gql-name :__typename)))
  (is (= "profilePicture_imageHeight" (graphql-utils/kw->gql-name :profile-picture/image-height)))
  (is (= "user_profilePicture_imageHeight" (graphql-utils/kw->gql-name :user.profile-picture/image-height)))
  (is (= "user_active_" (graphql-utils/kw->gql-name :user/active?)))

  (is (= :abc (graphql-utils/gql-name->kw "abc")))
  (is (= :__typename (graphql-utils/gql-name->kw "__typename")))
  (is (= :profile-picture/image-height (graphql-utils/gql-name->kw "profilePicture_imageHeight")))
  (is (= :user.profile-picture/image-height (graphql-utils/gql-name->kw "user_profilePicture_imageHeight")))
  (is (= :user/active? (graphql-utils/gql-name->kw "user_active_")))

  (let [root-value (graphql-utils/clj->js-root-value {:a (fn [] {:b 1})})]
    (is (object? root-value))
    (is (= 1 (aget ((aget root-value "a")) "b"))))

  (let [res (graphql-utils/js->clj-response (clj->js {"data" {"profilePicture_imageHeight" 100}}))]
    (is (= res {:data {:profile-picture/image-height 100}})))

  (let [schema-ast (js/GraphQL.buildSchema "type User {name: String}")]
    (graphql-utils/add-fields-to-schema-types schema-ast [{:type js/GraphQL.GraphQLID
                                                           :name "userId"
                                                           :args []}])
    (is (object? (aget (.getFields (aget (.getTypeMap schema-ast) "User")) "userId"))))

  (let [schema-ast (js/GraphQL.buildSchema "scalar BigNumber
                                            type User {age: BigNumber}
                                            type Query {user: User}")
        root-value (graphql-utils/clj->js-root-value
                     {:user (constantly
                              {:age (bn/number "10e10")})})
        query "{user {age}}"]

    (is (bn/= (bn/number "10e10") (-> schema-ast
                                    graphql-utils/add-bignumber-type
                                    (js/GraphQL.graphqlSync query root-value)
                                    graphql-utils/js->clj-response
                                    :data
                                    :user
                                    :age)))

    (is (bn/= (bn/number "10e10")
          (-> schema-ast
            (graphql-utils/add-bignumber-type {:disable-serialize? true})
            (js/GraphQL.graphqlSync query root-value)
            graphql-utils/js->clj-response
            :data
            :user
            :age))))

  (let [schema-ast (js/GraphQL.buildSchema "scalar Date
                                            type User {birth: Date}
                                            type Query {user: User}")
        root-value (graphql-utils/clj->js-root-value
                     {:user (constantly
                              {:birth (t/date-time 2018 05 05)})})
        query "{user {birth}}"]

    (is (= 1525478400000 (-> schema-ast
                           graphql-utils/add-date-type
                           (js/GraphQL.graphqlSync query root-value)
                           graphql-utils/js->clj-response
                           :data
                           :user
                           :birth)))

    (is (t/equal? (t/date-time 2018 05 05)
          (-> schema-ast
            (graphql-utils/add-date-type {:disable-serialize? true})
            (js/GraphQL.graphqlSync query root-value)
            graphql-utils/js->clj-response
            :data
            :user
            :birth))))

  (let [schema-ast (js/GraphQL.buildSchema "scalar Keyword
                                            type User {status: Keyword}
                                            type Query {user: User}")
        root-value (graphql-utils/clj->js-root-value
                     {:user (constantly
                              {:status :user.status/active})})
        query "{user {status}}"]

    (is (= "user.status/active" (-> schema-ast
                                  graphql-utils/add-keyword-type
                                  (js/GraphQL.graphqlSync query root-value)
                                  graphql-utils/js->clj-response
                                  :data
                                  :user
                                  :status)))

    (is (= :user.status/active (-> schema-ast
                                 (graphql-utils/add-keyword-type {:disable-serialize? true})
                                 (js/GraphQL.graphqlSync query root-value)
                                 graphql-utils/js->clj-response
                                 :data
                                 :user
                                 :status)))))


