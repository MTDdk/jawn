#!/bin/bash

properties="$HOME/.gradle/gradle.properties"


# $1 - key name, matched at beginning of line
prop () {
  grep "^${1}" ${properties} | cut -d'=' -f2
}

user=$(prop 'ossrhUsername')
pass=$(prop 'ossrhPassword')

bear=$(printf "$user:$pass" | base64)

curl -XPOST -i -H "Authorization: Bearer $bear" "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/net.javapla.jawn"
