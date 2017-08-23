#!/usr/bin/env bash
dir=`dirname $0`
dir=`cd $dir;pwd`

template="$dir/header.awk"
license="$dir/license.txt"

# avoid tr objecting to certain characters
export LC_ALL=C

for f in $(find . -iname "*.java" -or -iname "*.aj")
do
fin=$f
fout="$f.out"
fbak="$f.bak"
cp $fin $fbak

# ensure newlines are used in place of carriage returns so we do not corrupt the file
tr '\r' '\n' < $fin | awk -f $template -v license_file=$license > $fout
mv $fout $fin
done
