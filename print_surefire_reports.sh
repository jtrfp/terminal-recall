#!/usr/bin/env sh
echo "DEBUG: Current directory is $(pwd)"
echo "\n========================"
echo "\n=== SUREFIRE REPORTS ===\n"

for F in target/surefire-reports/*.txt
do
    echo $F
    cat $F
    echo
done
