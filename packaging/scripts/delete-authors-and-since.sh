#!/usr/bin/env bash
dir=`dirname $0`
dir=`cd $dir;pwd`

# avoid tr objecting to certain characters
export LC_ALL=C

for f in $(find . -iname "*.java" -or -iname "*.aj")
do
fin=$f
fout="$f.out"

# ensure newlines are used in place of carriage returns so we do not corrupt the file
tr '\r' '\n' < $fin | awk 'match($0,"@author") == 0 && match($0,"@since") == 0 {print $0}' > $fout
mv $fout $fin
done
