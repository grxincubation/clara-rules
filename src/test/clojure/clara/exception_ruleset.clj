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
  "Rule with no joins"
  [Temperature (= temperature ?t) (throw (ex-info "fail" {:t ?t}))]
  =>
  (insert! (->ExceptionHandlerError "rule-with-no-joins-binding-exception rhs should never be triggered")))

(defrule rule-with-bindings-test-exception
  "Rule with bindings test exception"
  [Temperature (= temperature ?t)]
  [:test (throw (ex-info "fail" {:t ?t}))]
  =>
  (insert! (->ExceptionHandlerError "rule-with-bindings-test-exception rhs should never be triggered")))

(defrule rule-with-bindings-exists-exception
  "Rule with bindings test exception"
  [Temperature (= location ?l) (= temperature ?t)]
  [:exists
    [WindSpeed (= location ?l) (throw (ex-info "fail" {:t ?t}))]]
  =>
  (insert! (->ExceptionHandlerError "rule-with-bindings-exists-exception rhs should never be triggered")))

(defrule rule-with-bindings-accumulator-exception
  "Rule with bindings accumulator exception"
  [Temperature (= location ?l) (= temperature ?t)]
  [?wind-seq <- (acc/all) :from [WindSpeed (= location ?l) (throw (ex-info "fail" {:t ?t}))]]
  =>
  (insert! (->ExceptionHandlerError (str "rule-with-bindings-accumulator-exception rhs should never be triggered: " ?wind-seq))))

(defrule rule-with-no-joins-accumulator-exception
  "Rule with no joins accumulator exception"
  [?temp-seq <- (acc/all) :from [Temperature (= temperature ?t) (throw (ex-info "fail" {:t ?t}))]]
  =>
  (insert! (->ExceptionHandlerError "rule-with-no-joins-accumulator-exception rhs should never be triggered")))

(defrule negation-node-exception
  "Rule using NegationNode"
  [:not [Temperature (= temperature (throw (ex-info "fail" {:t temperature})))]]
  =>
  (insert! (->ExceptionHandlerError "negation-node-exception rhs should never be triggered")))

(defrule complex-nested-negation-node-exception
  "Rule using complex nested NegationNode"
  [Temperature (= temperature ?t)]
  [:not [:and
         [Temperature (= temperature (throw (ex-info "fail" {:t ?t})))]
         [Temperature (= temperature (throw (ex-info "fail" {:t ?t})))]]]
  =>
  (insert! (->ExceptionHandlerError "complex-nested-negation-node-exception rhs should never be triggered")))

(defrule complex-nested-negation-node-not-triggers
  "Rule using complex nested NegationNode which does not trigger"
  [Temperature (= location ?l)]
  [:not [:and
         [WindSpeed (= location ?l)]]]
  =>
  (insert! (->ExceptionHandlerError "complex-nested-negation-node-not-triggers rhs should never be triggered ensuring negations work")))

(defrule complex-nested-negation-node-triggers
  "Rule using complex nested NegationNode which triggers but fails rhs"
  [Temperature (= location ?l) (= temperature ?t)]
  [:not [:and
         [WindSpeed (not= location ?l)]]]
  =>
  (throw (ex-info "fail" {:t ?t}))
  (insert! (->ExceptionHandlerError "complex-nested-negation-node-triggers rhs is triggered but exception is encountered")))

(defrule negation-node-with-join-filter-node-exception
  "Rule using NegationWithJoinFilterNode"
  [Temperature (= temperature ?t)]
  [:not [Temperature (= temperature (throw (ex-info "fail" {:t ?t})))]]
  =>
  (insert! (->ExceptionHandlerError "negation-node-with-join-filter-node-exception rhs should never be triggered")))

(defquery exception-handler-error-query
  []
  [?output <- ExceptionHandlerError])
