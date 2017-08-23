BEGIN {header=0}
$1 == "package" {header=1; print_header()}
header==1 {print}

function print_header() {
	while ((getline line < license_file) > 0)
       print line
	print ""
}