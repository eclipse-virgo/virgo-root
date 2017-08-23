BEGIN {header=0}
header==0 {print}
$1 == "<?xml" {print_header()}

function print_header() {
	while ((getline line < license_file) > 0)
       print line
}