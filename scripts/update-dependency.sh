#!/usr/bin/env bash
find . -name "*.java" -or -name ".classpath" -or -name "*.versions" -or -name "*.properties" | xargs grep -l $1 | grep -v "ivy-cache\|target" | xargs sed -ie s/$1/$2/g
find . -name "*.javae" -or -name ".classpathe" -or -name "*.versionse" -or -name "*.propertiese" | xargs rm

