# district-graphql-utils

[![Build Status](https://travis-ci.org/district0x/district-graphql-utils.svg?branch=master)](https://travis-ci.org/district0x/district-graphql-utils)


Set of helper functions for working with [GraphQL](https://graphql.org/), mostly related to clj->js and js->clj transformations.


## Installation
Add `[district0x/district-graphql-utils "1.0.5"]` into your project.clj
For browser usage also add `[cljsjs/graphql "0.13.1-0"]` (or latest version) into your project.clj
Include `[district.graphql-utils]` in your CLJS file  
For browser usage also add `[cljsjs.graphql]` in your CLJS file

## API Overview
- [district.graphql-utils](#districtgraphql-utils)
  - [kw->gql-name](#kw-gql-name)
  - [gql-name->kw](#gql-name-kw)
  - [clj->js-root-value](#clj-js-root-value)
  - [js->clj-response](#js->clj-response)
  - [add-fields-to-schema-types](#add-fields-to-schema-types)
  - [build-schema](#build-schema)

## district.graphql-utils

#### <a name="kw-gql-name">`kw->gql-name [kw]`
Converts (namespaced) keyword into GraphQL compatible name (no dots, no slashes, no dashes).  
Note, this is opinionated way how to convert namespaced keyword into string. 
```clojure
(graphql-utils/kw->gql-name :profile-picture/image-height)
;; => "profilePicture_imageHeight"

(graphql-utils/kw->gql-name :user.profile-picture/image-height)
;; => "user_profilePicture_imageHeight"

(graphql-utils/kw->gql-name :user/active?)
;; => "user_active_"
```

#### <a name="gql-name-kw">`gql-name->kw [gql-name]`
Converts GraphQL compatible name into keyword. 
```clojure
(graphql-utils/gql-name->kw "profilePicture_imageHeight")
;; => :profile-picture/image-height

(graphql-utils/gql-name->kw "user_profilePicture_imageHeight")
;; => :user.profile-picture/image-height

(graphql-utils/gql-name->kw "user_active_")
;; => :user/active?
```

#### <a name="clj-js-root-value">`clj->js-root-value [root-value]`
Converts [root value](http://graphql.org/graphql-js/graphql/#graphql) data structure into proper JS format, 
so it can be passed into a graphql-js library. It also converts field arguments passed to each resolver function into
clj.

Optionally as a seconds arg you can pass map with `:gql-name->kw` & `:kw->gql-name` for custom name conversion functions.

```clojure
(def root-value (graphql-utils/clj->js-root-value {:a (fn [] {:b 1})}))
(aget ((aget root-value "a")) "b")
;; => 1
```

#### <a name="js-clj-response">`js->clj-response [res]`
Converts GraphQL response into clj data structures, since GraphQL returns each object in a tree as an instance of a class. 

Optionally as a seconds arg you can pass map with `:gql-name->kw` for custom name conversion function. 
```clojure
(graphql-utils/js->clj-response (clj->js {"data" {"profilePicture_imageHeight" 100}}))
;; => {:data {:profile-picture/image-height 100}}
```

#### <a name="add-fields-to-schema-types">`add-fields-to-schema-types [schema-ast fields]`
Will add given fields to user defined types in schema AST. 
 
```clojure
(let [schema-ast (js/GraphQL.buildSchema "type User {name: String}")]
  (graphql-utils/add-fields-to-schema-types schema-ast [{:type js/GraphQL.GraphQLID
                                                         :name "userId"
                                                         :args []}])
  (object? (aget (.getFields (aget (.getTypeMap schema-ast) "User")) "userId")))
;; => true    
```

#### <a name="build-schema">`build-schema [schema-str resolvers-map {:keys [kw->gql-name gql-name->kw]}]`
Builds a GraphQLSchema from a schema string and a resolvers map.
- schema-str: A string containig a graphql schema definition.
- resolvers-map: A map like {:Type {:field1 resolver-fn}}.
- kw->gql-name: A fn for serializing keywords to gql names.
- gql-name->kw: A fn for parsing keywords from gql names.

```clojure
(let [schema "type Author {
                         id: ID! 
                         firstName: String
                         lastName: String
                         posts: [Post]
                       }
                     
                       type Post {
                         id: ID!
                         title: String
                         author: Author
                         votes: Int
                       }
                     
                       type Query {
                         posts(minVotes: Int): [Post]
                       }
                     
                       type Mutation {
                         upvotePost (postId: ID!): Post
                       }
                     
                       schema {
                         query: Query
                         mutation: Mutation
                       }"
      resolvers {:Query {:posts (fn [obj {:keys [min-votes] :as args}])}
                 :Mutation {:upvote-post (fn [obj {:keys [post-id] :as args}])}
                 :Author {:posts (fn [{:keys [posts] :as author}])}
                 :Post {:author (fn [{:keys [author] :as post}])}}]
  
  (build-schema schema resolvers {:kw->gql-name kw->gql-name
                                  :gql-name->kw gql-name->kw}))
```

## Development
```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```
