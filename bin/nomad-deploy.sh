#!/bin/bash

set -ex

export NOMAD_TOKEN=${NOMAD_TOKEN}

git checkout -B "$CI_BUILD_REF_NAME" "$CI_BUILD_REF"

envsubst < deployments/battery-measurements-api.${CI_ENVIRONMENT_SLUG}.json > battery-measurements-api.json
git add battery-measurements-api.json

echo "deploying... "

curl --header "X-Nomad-Token: ${NOMAD_TOKEN}" \
     --request POST \
     --data @battery-measurements-api.json http://116.203.80.3:4646/v1/jobs

echo "deployment success"
