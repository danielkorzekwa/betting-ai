export DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
export JMX="-Dcom.sun.management.jmxremote.port=20000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
java $JMX $DEBUG -Xmx512m -cp ./lib/market-simulator-${project.version}.jar dk.bettingai.marketsimulator.SimulatorApp $*
