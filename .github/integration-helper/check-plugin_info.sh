#! /bin/bash

# Wait for a GoCD HTTP server and download the plugin info.
# Parameters:
# 1: FILE
#
# Return code:
# 0: ok
# != 0: error

FILE=$1

grep template ${FILE} || exit 1
grep condition ${FILE} || exit 1
grep webhook_url ${FILE} || exit 1
grep proxy_url ${FILE} || exit 1
