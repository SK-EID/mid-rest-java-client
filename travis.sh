#!/bin/bash

echo $TRAVIS_PULL_REQUEST
echo $TRAVIS_TAG

VERSION_REGEX='^([0-9]+\.){1,3}([0-9]+)$'

if [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_TAG" != "" ] && ![ "$TRAVIS_TAG" =~ $VERSION_REGEX ]; then
  echo "error: invalid version number. Version number must only contain numbers (and some dots in between)."
  exit 1
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_TAG" != "" ]; then
  echo "Starting to publish"
  ./publish.sh
  echo "Finished"
else
  mvn test
  mvn org.owasp:dependency-check-maven:check
  mvn spotbugs:check
fi
