# district-graphql-utils

[![Build Status](https://travis-ci.org/district0x/district-graphql-utils.svg?branch=master)](https://travis-ci.org/district0x/district-graphql-utils)


Set of helper functions for working with [GraphQL](https://graphql.org/), mostly related to clj->js and js->clj transformations.


## Installation
Add `[district0x/district-graphql-utils "1.0.3"]` into your project.clj  
Include `[district.graphql-utils]` in your CLJS file  

## API Overview
- [district.graphql-utils](#districtgraphql-utils)
  - [kw->gql-name](#kw-gql-name)
  - [gql-name->kw](#gql-name-kw)
  - [clj->js-root-value](#clj-js-root-value)
  - [js->clj-response](#js->clj-response)
  

## district.graphql-utils

#### <a name="kw-gql-name">`kw->gql-name [kw]`
Converts (namespaced) keyword into GraphQL compatible name (no dots, no slashes, no dashes).  
Note, this is opinionated way how to convert namespaced keyword into string. 
```clojure
(graphql-utils/kw->gql-name :profile-picture/image-height)
;; => "profilePicture_imageHeight"

(graphql-utils/kw->gql-name :user.profile-picture/image-height)
;; => "user_profilePicture_imageHeight"
```

#### <a name="gql-name-kw">`gql-name->kw [gql-name]`
Converts GraphQL compatible name into keyword. 
```clojure
(graphql-utils/gql-name->kw "profilePicture_imageHeight")
;; => :profile-picture/image-height

(graphql-utils/gql-name->kw "user_profilePicture_imageHeight")
;; => :user.profile-picture/image-height
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

## Development
```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```