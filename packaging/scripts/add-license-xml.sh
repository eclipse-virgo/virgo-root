#!/usr/bin/env bash
dir=`dirname $0`
dir=`cd $dir;pwd`

template="$dir/header-xml.awk"
license="$dir/license-xml.txt"

# avoid tr objecting to certain characters
export LC_ALL=C

for f in $(find . -iname "*.xml")
do
fin=$f
fout="$f.out"

# ensure newlines are used in place of carriage returns so we do not corrupt the file
tr '\r' '\n' < $fin | awk -f $template -v license_file=$license > $fout
mv $fout $fin
done
