#!/bin/bash
emu=(0)
dir_tmp="data/tmp"
train_file="${dir_tmp}/train"
test_file="${dir_tmp}/test"
output_file="${dir_tmp}/output"

<<<<<<< HEAD
ant compile

for elem in ${emu[*]}; do
    time -p $JAVA_HOME/bin/java -cp ".:bin:libs/*" -Xmx6000m mstparser.DependencyParser \
=======
for elem in ${emu[*]}; do
    $JAVA_HOME/bin/java -cp ".:bin:libs/*" -Xmx6000m mstparser.DependencyParser \
>>>>>>> d71e48d7522e2e889145e2ad0b483ae917db0549
    train train-file:"${train_file}${elem}" model-name:"model/dep${elem}.model" \
    order:2 loss-type:nopunc decode-type:proj \
    test test-file:"${test_file}${elem}" output-file:"${output_file}${elem}" \
    eval gold-file:"${test_file}${elem}"
    echo "data_${elem}_completed..."
done

