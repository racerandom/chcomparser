#!/bin/bash
train_file="data/datafull_withchinese.half"
model_file="model/datafull.model"

ant compile

time -p java -cp ".:bin:libs/*" -Xmx6000m mstparser.DependencyParser \
train train-file:$train_file model-name:$model_file \
order:2 loss-type:punc decode-type:proj \
#test test-file:"${test_file}${elem}" output-file:"${output_file}${elem}" \
#eval gold-file:"${test_file}${elem}"


