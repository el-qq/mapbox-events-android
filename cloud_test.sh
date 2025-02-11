#!/bin/bash 
set -xe

build_dir="$1"
module=$(echo "$build_dir" | cut -d "/" -f1)
test_apk_path="$2"
results_dir="$3"
gcloud firebase test android models list
gcloud firebase test android run --type instrumentation \
    --app app/build/outputs/apk/okhttp4/debug/app-okhttp4-debug.apk \
    --test "${build_dir}/${test_apk_path}" \
    --results-dir="$results_dir" \
    --device model=Nexus4,version=21,locale=en,orientation=portrait  \
    --device model=hammerhead,version=23,locale=en,orientation=landscape \
    --device model=starqlteue,version=26,locale=en,orientation=portrait \
    --device model=redfin,version=30,locale=en,orientation=portrait \
    --environment-variables coverage=true,coverageFile="/sdcard/${module}_coverage.ec" \
    --directories-to-pull /sdcard \
    --timeout 20m

bucket="test-lab-r47d1tyt8h0hm-iku3c1i8kjrux"
artifacts_path="hammerhead-23-en-landscape/artifacts"
covfile_path="gs://${bucket}/${results_dir}/${artifacts_path}/${module}_coverage.ec"
gsutil cp $covfile_path "${build_dir}/jacoco"
