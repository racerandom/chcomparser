#!/bin/bash
emu=(0)
dir_tmp="data/tmp"
train_file="${dir_tmp}/train"
test_file="${dir_tmp}/test"
output_file="${dir_tmp}/output"

ant compile

for elem in ${emu[*]}; do
    time -p java -cp ".:bin:libs/*" -Xmx6000m mstparser.DependencyParser \
    train train-file:"${train_file}${elem}" model-name:"model/dep${elem}.model" \
    order:2 loss-type:nopunc decode-type:proj \
    test test-file:"${test_file}${elem}" output-file:"${output_file}${elem}" \
    eval gold-file:"${test_file}${elem}"
    echo "data_${elem}_completed..."
done

