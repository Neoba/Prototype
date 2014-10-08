JAVA_HOME=/usr/lib/jvm/java-openjdk/
export JAVA_HOME
nohup ant -f build-cli.xml</dev/null 1>/tmp/stdout.log 2>/tmp/stderr.log &
