#!/bin/bash
# Set JAVA_HOME to the locally installed JDK 21 so Maven can build the project.
# Usage: source scripts/dev-env.sh

if [ -d "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home" ]; then
  export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
  export PATH="$JAVA_HOME/bin:$PATH"
  echo "JAVA_HOME=$JAVA_HOME"
  java -version
else
  echo "JDK 21 not found at /Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home" >&2
  return 1 2>/dev/null || exit 1
fi
