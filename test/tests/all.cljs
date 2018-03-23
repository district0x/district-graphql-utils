(ns tests.all
  (:require
    [cljs.test :refer [deftest is testing run-tests async use-fixtures]]
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
    (is (= res {:data {:profile-picture/image-height 100}}))))


