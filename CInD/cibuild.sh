#!/bin/sh
#
# Build script called by Jenkins for sparcin-continuous-build job.  Some paths may be specific to the jenkins build server.

PATH=$PATH:/usr/local/play play dependencies --sync
rm -rf test-result/result.passed pmd.xml /tmp/sparcin
mkdir -p /var/lib/jenkins/workspace/sparcin-continuous-build/test-result/code-coverage
mkdir -p /var/lib/jenkins/jobs/sparcin-continuous-build/htmlreports/Cobertura_Report
rm -rf /var/lib/jenkins/workspace/sparcin-continuous-build/cobertura.ser.lock
git checkout master
git merge origin/master
PATH=$PATH:/usr/local/play play auto-test

if [ "$?" != "0" ] ; then
	echo "Error during play auto-test -- aborting."
	exit 1
fi

if [ -f test-result/result.passed ] ; then 
	# create new temp branch to make version change in
	git branch -D tmp_build_info  >/dev/null 2>&1
	git checkout -b tmp_build_info
	# update conf/build-info.conf with build data
	echo "Updating build-info.conf ..."
	echo "version=$JOB_NAME-$BUILD_NUMBER" > conf/build-info.conf
	echo "commitId=$GIT_COMMIT" >> conf/build-info.conf
	echo "buildDate=$BUILD_ID" >> conf/build-info.conf
	echo "Committing changes ..."
	git add conf/build-info.conf
	git commit -am "Jenkins build version increment for job $JOB_NAME-$BUILD_NUMBER"
	# push to Heroku - this goes away once we're fully setup in AWS for dev and staging
	git push git@heroku.com:sparcin-dev.git tmp_build_info:master --force 
	git checkout master
	git branch -D tmp_build_info
	# create war file for archiving and deploying to AWS
	#play war . -o /tmp/sparcin --zip --%prod
	#rm -rf /tmp/sparcin
	#JOB_NAME_SHORT=$(echo $JOB_NAME | sed 's/-build//g')
	#rm -rf $JOB_NAME_SHORT-*.war
	#mv /tmp/sparcin.war $JOB_NAME_SHORT-$BUILD_NUMBER-$GIT_COMMIT.war
else 
	exit 1 
fi 
mkdir -p test-result/code-coverage
touch test-result/code-coverage/index.html
