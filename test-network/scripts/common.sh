HTTPIE=${HTTPIE:-http}
HTTPIE_FLAGS=${HTTPIE_FLAGS:-'--check-status -v'}
JQ=${JQ:-jq}

CC_HOST=${CC_HOST:-localhost}
CC_PORT=${CC_PORT:-8801}
CC_CHAN=${CC_CHAN:-my-channel1}
CC_NAME=${CC_NAME:-tpcc}

_erho() {
	>&2 echo "$@"
}

assert_cmd() {
	cmd=$1
	command -v "$cmd" >/dev/null 2>&1 || {
		_erho "$cmd not found on PATH"
		exit 1
	}
}

get_token() {
	$HTTPIE \
	    "http://$CC_HOST:$CC_PORT/user/enroll" \
	    id=admin secret=adminpw \
	| $JQ .token \
	| tr -d '"'
}

_call_cc() {
	method=$1
	shift

	[ -n "$TOKEN" ] || { _erho 'TOKEN must be set'; return; }
	$HTTPIE \
	    $HTTPIE_FLAGS \
	    -A bearer -a "$TOKEN" \
	    "http://$CC_HOST:$CC_PORT/$method/$CC_CHAN/$CC_NAME" $@
}
invoke() { _call_cc invoke $@; }
query() { _call_cc query $@; }
