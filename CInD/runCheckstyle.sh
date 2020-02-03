#!/bin/sh
#
# usage:  runCheckstyle.sh <repo root>
#    ex:  runCheckstyle.sh /var/lib/jenkins/workspace/sparcin-continuous-build

ROOT=$1

if [ -d "$ROOT" ] ; then
  echo "Running Checkstyle on root directory: $ROOT"
else
  echo "Error: specified root directory does not exist."
  exit 1
fi

cd $ROOT
java -jar CInD/checkstyle-5.5-all.jar com.puppycrawl.tools.checkstyle.Main -c CInD/sun_checks.xml */*/*/*/*.java */*/*/*.java */*/*.java */*.java -f xml -o CInD/Checkstyle_SPARCin.xml
cd -
echo
echo "Done!  Output file is: $(pwd)/Checkstyle_SPARCin.xml"
echo
