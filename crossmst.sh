#!/bin/bash
#emu=(0)
num=10
dir_tmp="data/tmp/"

order=2
ant compile
#for elem in ${emu[*]}; do
for((elem=0;elem<num;elem++))
do
    echo "[cross-validation $elem] start..."
    time java -d64 -Xms512m -Xmx6g -cp ".:bin:libs/*" -Xmx6000M mstparser.DependencyParser \
    train train-file:"${dir_tmp}train${elem}" model-name:"model/dep${elem}.model" \
    order:$order loss-type:nopunc decode-type:proj \
    test test-file:"${dir_tmp}test${elem}" output-file:"${dir_tmp}output${elem}" \
    echo "[cross-validation $elem] completed..."
done

java -cp "bin" mstparser.DependencyBatchEvaluator "${dir_tmp}test" "${dir_tmp}output" $num
