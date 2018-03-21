(ns tests.all
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
    [district.graphql-utls :as graphql-utls]))

(deftest tests
  (is (= "abc" (graphql-utls/kw->gql-name :abc)))
  (is (= "__typename" (graphql-utls/kw->gql-name :__typename)))
  (is (= "profilePicture_imageHeight" (graphql-utls/kw->gql-name :profile-picture/image-height)))
  (is (= "user_profilePicture_imageHeight" (graphql-utls/kw->gql-name :user.profile-picture/image-height)))

  (is (= :abc (graphql-utls/gql-name->kw "abc")))
  (is (= :__typename (graphql-utls/gql-name->kw "__typename")))
  (is (= :profile-picture/image-height (graphql-utls/gql-name->kw "profilePicture_imageHeight")))
  (is (= :user.profile-picture/image-height (graphql-utls/gql-name->kw "user_profilePicture_imageHeight")))

  (let [root-value (graphql-utls/clj->js-root-value {:a (fn [] {:b 1})})]
    (is (object? root-value))
    (is (= 1 (aget ((aget root-value "a")) "b"))))

  (let [res (graphql-utls/js->clj-response (clj->js {"data" {"profilePicture_imageHeight" 100}}))]
    (is (= res {:data {:profile-picture/image-height 100}}))))


