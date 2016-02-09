#!/bin/bash

# make working directory
mkdir copy-project

# create target project
mvn archetype:generate -B \
 -DarchetypeCatalog=http://repo.terasoluna.org/nexus/content/repositories/terasoluna-gfw-releases \
 -DarchetypeGroupId=org.terasoluna.gfw.blank \
 -DarchetypeArtifactId=terasoluna-gfw-multi-web-blank-mybatis3-archetype \
 -DarchetypeVersion=5.1.0.RC3 \
 -DgroupId=org.terasoluna.securelogin \
 -DartifactId=secure-login \
 -Dversion=5.1.0.RC3

mv secure-login ./copy-project/secure-login-demo

# copy source files
find ./secure-login-demo/ -name "*.java" | xargs -i cp -pf --parent {} ./copy-project/
find ./secure-login-demo/ -name "*.properties" | xargs -i cp -pf --parent {} ./copy-project/
find ./secure-login-demo/ -name "*.sql" | xargs -i cp -pf --parent {} ./copy-project/
find ./secure-login-demo/ -name "*.jsp" | xargs -i cp -pf --parent {} ./copy-project/
find ./secure-login-demo/ -name "*.css" | xargs -i cp -pf --parent {} ./copy-project/

# get difference of setting files from source to target
diff -qwr secure-login-demo copy-project/secure-login-demo --exclude="*.merge.*" > copy-project/diff-result
# copy new setting files in source project
cat copy-project/diff-result | grep -e "^Only in secure-login-demo/" | sed -e "s/^Only in //g" | sed -e "s|: |/|g" | xargs -i cp -r --parents {} ./copy-project/
# remove unnecessary setting files in target project
cat copy-project/diff-result | grep -e "^Only in copy-project/" | sed -e "s/^Only in //g" | sed -e "s|: |/|g" | xargs -i rm -r {} 
cat copy-project/diff-result | grep -e " differ$" > copy-project/modified-files
rm copy-project/diff-result
sed -i -e "s/^Files//g;s/ and / /g;s/differ$//g" copy-project/modified-files
cp github/xml-merge/target/xml-merge-0.1.0-SNAPSHOT.jar copy-project/xml-merge.jar
cat copy-project/modified-files | while read line; do(
    SOURCE_FILE=`echo $line | cut -d' ' -f1`
    if echo $SOURCE_FILE | grep "\.xml$" > /dev/null; then
        echo "processing ${SOURCE_FILE} ..."
        java -jar copy-project/xml-merge.jar $line
        sh ${SOURCE_FILE}.merge.sh
    fi
    )done
