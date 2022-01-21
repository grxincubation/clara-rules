(ns clara.exception-ruleset
  (:require [clara.rules :refer :all]
            [clara.rules.accumulators :as acc]
            [clara.rules.testfacts :refer :all])
  (:import [clara.rules.testfacts
            Temperature
            WindSpeed])
  (:import [clara.rules.engine
            ErrorResult]))

(defrecord ExceptionHandlerError [message])

(defrecord Failure [code])

(defrule rule-with-no-joins-binding-exception
  "Rule with no joins always throws NullPointerException"
  [Temperature (= temperature ?t) (throw (ex-info "fail" {:t ?t}))]
  =>
  (insert! (->ExceptionHandlerError "rule-with-no-joins-binding-exception rhs should never be triggered")))

(defrule negation-node-exception
  "Rule using NegationNode"
  [:not [Temperature (= temperature (throw (ex-info "fail" {:t temperature})))]]
  [:not [ErrorResult (= rule-name ::negation-node-exception)]]
  =>
  (insert! (->ExceptionHandlerError "negation-node-exception rhs should never be triggered")))

(defrule negation-node-with-join-filter-node-exception
  "Rule using NegationWithJoinFilterNode"
  [Temperature (= temperature ?t)]
  [:not [Temperature (= temperature (throw (ex-info "fail" {:t ?t})))]]
  [:not [ErrorResult (= rule-name ::negation-node-with-join-filter-node-exception)]]
  =>
  (insert! (->ExceptionHandlerError "negation-node-with-join-filter-node-exception rhs should never be triggered")))

(defquery exception-handler-error-query
  []
  [?output <- ExceptionHandlerError])
