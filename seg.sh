#!/bin/bash
train_file="data_full"
test_file="pku_train_words.conll"
output_file="pku_train_words.out"

ant compile

java -cp ".:bin:libs/*" -Xmx6000m mstparser.DependencyParser \
test model-name:"model/${train_file}.model" test-file:"data/${test_file}" output-file:"data/${output_file}" 


