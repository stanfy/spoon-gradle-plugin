#!/bin/sh

BRANCH=`git rev-parse --abbrev-ref HEAD`
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_BRANCH}" = "master" ]; then
  echo "Deploying to maven..."
  TERM=dumb ./gradlew -PnexusUsername=${SONATYPE_USER} -PnexusPassword=${SONATYPE_PASS} upload || exit 1
fi
