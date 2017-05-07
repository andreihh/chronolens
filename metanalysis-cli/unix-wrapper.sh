#!/usr/bin/env sh

this=`which "$0" 2>/dev/null`
[ $? -ne 0 ] && [ -x "$0" ] && this="./$0"

[ -n "$JAVA_HOME" ] && \
    java="$JAVA_HOME/bin/java" || \
    java="java"

exec "$java" $JAVA_ARGS -jar "$this" "$@"
exit 1
