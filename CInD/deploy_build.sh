#!/bin/sh 
#
# deploy_build.sh - Manually deploy a SPARCet build to an environment
#  -- SPARCin is different as it’s a Play! Framework app.  We don't have build artifacts.
#     Instead, we just push the code from git to Heroku.  The trick is to let the user
#     specify a tag or a SHA1 key to use as the "build" to push.
#
# usage: deploy_build.sh <env> <tag|sha1>
#

DEP_ENV=$1
DEP_TAG=$2

echo
echo "SPARCin Self-Service Deployment Tool"
echo

if [ "$DEP_TAG" == "" ] || [ "$DEP_ENV" == "" ] ; then
  echo "Error: one or more args missing."
  echo "usage: deploy_build <env> <tag|sha1>"
  echo
  exit 1
fi

# 0 - save current branch
currentBranch=$(git branch | grep "^*" | awk '{print $2}')
if [ "$currentBranch" == "(no" ] ; then
  currentBranch=$(git log|head -1 | awk '{print $2}')
fi

# 1 - checkout the tag/commit
/bin/echo -n "Checking out the specified TAG/Commit ...                      "
hmm=$(git checkout $DEP_TAG 2>&1)
if [ "$?" != "0" ] ; then
  echo "[FAILED]"
  echo
  echo $hmm
  echo
  exit 1
else
  echo "[OK]"
fi

# 2 - create temp branch to specify in the git push
/bin/echo -n "Creating temporary branch for git push to Heroku...            "
tmpBranch="tmp_herokupush_$DEP_TAG"
hmm=$(git branch -D $tmpBranch 2>&1)
hmm=$(git checkout -b $tmpBranch 2>&1)
if [ "$?" != "0" ] ; then
  echo "[FAILED]"
  echo
  echo $hmm
  echo
  exit 1
else
  echo "[OK]"
fi


# 3 - push the new branch to heroku env
echo "Pushing $tmpBranch to Heroku for deployment... [PENDING]"
echo
git push git@heroku.com:${DEP_ENV}.git $tmpBranch:master --force

if [ "$?" != "0" ] ; then
  echo ; echo "Failed!  Details above." ; echo
else
  echo ; echo "Done!" ; echo
fi

# 4 - cleanup
/bin/echo -n "Resetting git environment...                                   "
hmm=$(git checkout $currentBranch 2>&1)
if [ "$?" != "0" ] ; then
  echo "[FAILED]"
  echo $hmm
  echo
else
  echo "[OK]"
  echo ; echo "Done!" ; echo
fi

