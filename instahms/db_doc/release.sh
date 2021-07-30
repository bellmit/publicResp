#!/bin/bash

#
# Instructions for making a new release:
#
# 1. Checkout the trunk: all you need is release.sh under db_doc, but the
# latest can be found in trunk only. Ensure you have the latest script.
#
# 2. Open up a terminal console, go to the trunk directory, go to db_doc
# under it.
#
# 3. Decide on the version number. The components of the version number are
# Major, Minor, Update, Build numbers, like Ma.Mi.Up-build. The rule for
# deciding the next version is like this:
#
# (a) We cannot have two versions with same Ma.Mi.Up in the field. Thus, the
# Update number has to increment after every release to customer. Eg, if the
# latest version in the field is 7.4.4, then, the next 7.4 build to testing
# should be 7.4.5.
#
# (b) The build number keeps incrementing for every build (regardless of which
# branch). Thus, if the last build to testing was 7.1.2-1623, even when making
# the next build for 7.4.5, the build number should be 1624.
#
# 4. Decide on the deploy strategy for each schema on each testsvr. The
# following are the options:
#
# (a) Continue, ie, incrementally migrate the schema that exists
# (b) Delete and re-create fresh empty schema (with or without 3 digit)
# (c) Delete and migrate schema from previous version
#
# Mostly, we adopt (a) for all schemas. The strategy that is going to be used
# depends upon the configuration file deploy-hms[XX].cfg which exists
# under /root/bin in each testsvr. Login to testsvr (each of them) and confirm
# that the strategies specified in the cfg file is what you want to happen.
# If it is not what you want, correct it by editing the .cfg file.
#
# 5. Run release.sh like this:
#
#    ./release.sh 7.4.4-1599 1.0.2
#
# The script will checkout, add the hms version number to application.properties.
# It will also find the maximum build number of given api version and adds a property
# insta.api.version to application.properties
# It will then checkin, make a build, and also deploy it to all testsvrs. During the
# process, you will be prompted for passwords:
#
# (a) SVN password: the script will ask for password for all SVN activities.
# There will be one checkout, one checkin, one update: thus, 3 times. This is
# be the password of the user who is running the release.
#
# (b) testsvr[n] root password: For copying (once) and deploying (again) to
# testsvrs, password will be prompted. Note that this is the password of root
# on testsvr (which is usually "redhat"), not the user's password.
#
# During deploy, a confirmation message shows the deploy strategy for each of
# the schemas. This will be based on the deploy-hmsXX.cfg on that server.
# Read the confirmation message and confirm the deploy by pressing y.
#
# You can abort the deploy by pressing n, and do the deploy manually by
# the script "deploy" on the server directly.
#
# Build and/or deploy errors, if any, will be printed on the console.
#
# 6. Re-building: If there are any errors, you may want to re-do the build. For
# re-building, you can use the same version number. But, before you rebuild,
# ensure that the last line (with the version number) in db_changes.sql is
# removed. Checkin the db_changes after you have removed the last line.
#

# load release.conf
source `dirname $0`/release.conf

CUR_DIR=`dirname $0`

BUILDDIR=/home/$USER/workspace/release
BUILD_CACHE_AREA=/home/$USER/workspace/buildcachearea
REPODIR=git@github.com:practo/insta-hms.git
mkdir -p $BUILDDIR

SELF=$0
[ -h $SELF ] && SELF=`readlink $SELF`
DEPLOY_SCRIPT_DIR=`dirname $SELF`
if [ $DEPLOY_SCRIPT_DIR == '.' ] ; then
	DEPLOY_SCRIPT_DIR=`pwd`
fi

BRANCH=
NQ=
INCR=
BG=

HOST=`hostname`
function usage() {
	echo " Usage: release.sh [OPTIONS] <version>"
	echo " OPTIONS: "
	echo " -b <branch> : Branch to use for build. If not specified, figure out from version"
	echo " -n          : Non-interactive, no questions"
	echo " -p          : Deploy all testsvrs parallely"
	echo " -s          : Skip WSDL builds"
	echo " -i          : Ignore deploy-xxx.cfg, do an incremental only build. Implies -n"
	echo "<version>    : full, like 7.5.0-1616 or just 7.5.0 (will use last num+1)"
	echo "   (hint: use full version if re-doing the same build, otherwise use partial)"
}

# copied from install/bin/functions
function get_numeric_version() {
	local ver=$1
	ver=${ver%-*}
	local parts=(${ver//./ })
	echo $(( (${parts[0]}*100+${parts[1]})*100+${parts[2]} ))
}

# getting the major and minor parts of the version as a string ie 10.1

function get_string_maj_min_nopadding() {
	local ver=$1
	ver=${ver%-*}
	local parts=(${ver//./ })
	if [[ ${#parts[1]} -eq 1 ]]
	then
		parts[1]="${parts[1]}"
	fi
	echo "${parts[0]}.${parts[1]}"
}

function get_string_maj_min() {
		local ver=$1
		ver=${ver%-*}
		local parts=(${ver//./ })
		if [[ ${#parts[1]} -eq 1 ]]
		then
			parts[1]="0${parts[1]}"
		fi
		echo "${parts[0]}.${parts[1]}"
}

function send_slack_notification() {
	local message="$1"
	/home/$USER/bin/build_slack_notifications.py "$message" "#insta-build-notify"
}

function error_exit() {
	local msg="$1"
local slack_message=$(cat << EOF
*Release Failure*
$msg
EOF
)
	send_slack_notification "$slack_message"
	exit 1
}

function get_host_hms_options() {
	local deployhost=$1
	local DB=$2
	local optionsfile=options
	local tmplocation=/tmp/${deployhost//./}
	if [ $DB != "hms" ] ; then
		 optionsfile=options.${DB}
	fi
	mkdir -p $tmplocation
	if [ -f $tmplocation/$optionsfile ] ; then
		rm $tmplocation/$optionsfile
	fi
	scp root@${deployhost}:/etc/hms/${optionsfile} $tmplocation > /dev/null 2>&1
}

function get_host_dist_home() {
	local deployhost=$1
	local DB=$2
	local tmplocation=/tmp/${deployhost//./}
	local optionsfile=$tmplocation/options
	if [ $DB != "hms" ] ; then 
		optionsfile=$tmplocation/options.${DB}
	fi
	local dist_dir=/root/
	if [ -f $optionsfile ] ; then
		local dist_dir_row=`grep HMS_WORK_HOME $optionsfile`
		local dist_dir=${dist_dir_row/'HMS_WORK_HOME='/}
	fi
	[[ -z "$dist_dir" ]] && dist_dir=/root/
	echo $dist_dir
}

function get_host_bin_dir() {
	local deployhost=$1
	local DB=$2
	local tmplocation=/tmp/${deployhost//./}
	local optionsfile=$tmplocation/options
	if [ $DB != "hms" ] ; then 
		optionsfile=$tmplocation/options.${DB}
	fi
	local work_dir=/root/bin
	if [ -f $optionsfile ] ; then
		local bin_dir_row=`grep HMS_BIN_DIR $optionsfile`
		local bin_dir=${bin_dir_row/'HMS_BIN_DIR='/}
	fi
	[[ -z "$bin_dir" ]] && bin_dir=/root/bin
	echo $bin_dir
}

function get_host_user() {
	local dist_dir=$1
	local host_user=${dist_dir/'home'/}
	echo ${host_user////}
}

# set java home.
function set_java_home() {
	if [[ $# -ne 1 ]]
	then
		REQUIRED_VERSION="8"
	else
		REQUIRED_VERSION=$1
	fi

	OS=`uname -s`
	OP=""
	if [[ $OS == 'Darwin' ]]
	then 
		OP=$(/usr/libexec/java_home -v $REQUIRED_VERSION -f)
	else
		JAVAC_INSTALLS=`update-alternatives --list javac`
		for JAVAC in $JAVAC_INSTALLS; do
			THIS_JAVA=${JAVAC/'javac'/'java'}
			THIS_VERSION=$($THIS_JAVA -version 2>&1 | grep -i version | cut -d'"' -f2 | sed 's/^1\.//g' | sed 's/\_/\./g' | cut -d'.' -f1)
			THIS_FLAVOUR=$($THIS_JAVA -version 2>&1 | grep -i "runtime environment" | cut -d' ' -f1)
			if [[ ${THIS_VERSION} == $REQUIRED_VERSION && $THIS_FLAVOUR == "OpenJDK" ]]
			then
				OP=${JAVAC/'/bin/javac'/''}
			fi
		done
	fi
	if [[ $OP == "" ]]
	then
		echo "OpenJDK $REQUIRED_VERSION not detected on this machine"
		export JAVA_HOME=""
	else
		echo "Detected OpenJDK $REQUIRED_VERSION at $OP"
		export JAVA_HOME=$OP
	fi
}

while getopts "b:pinmes" opt ; do
	case $opt in
		b) BRANCH=${OPTARG}
			;;
		p) BG="&"
			;;
		i) INCR="-i"
			;;
		n) NQ="-n"
			;;
		s) SKIPWS="-s"
			;;
		:) usage ; exit 1
			;;
		\?) usage ; exit 0
			;;
	esac
done
shift `expr $OPTIND - 1`

# command line options
VERSION=$1
[ -z $VERSION ] && usage && exit 1
MINOR=`get_string_maj_min $VERSION`
MINOR_NOPADDING=`get_string_maj_min_nopadding $VERSION`
TRUNKVERSION=`get_string_maj_min $TRUNK`
MINOR_WITHOUT_DOT=${MINOR/./}
TRUNK_WITHOUT_DOT=${TRUNKVERSION//./}

[ $MINOR_WITHOUT_DOT -lt 1202 ] && "Build for version older than 12.2 are not supported using this version of release.sh" && exit 1

VER=12.5
MVNREPO="-Dmaven.repo.local=/home/$USER/.m2/repository-java8"

if [[ $MINOR_WITHOUT_DOT -lt 1204 ]] ; then
	set_java_home 7
	MVNREPO="-Dmaven.repo.local=/home/$USER/.m2/repository-java7"
else
	set_java_home 8	
fi

DBNAME=hms

if [ $MINOR_WITHOUT_DOT != $TRUNK_WITHOUT_DOT ] ; then
	DBNAME=hms$MINOR_WITHOUT_DOT
fi

echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : JAVA_HOME = $JAVA_HOME : MVNREPO = ${MVNREPO/-Dmaven\.repo\.local=/}"

if [ -z $BRANCH ] ; then
	# figure it out ourselves based on the version given
	if [ $MINOR == $TRUNKVERSION ] ; then
		BRANCH=$TRUNKBRANCH
		DB=hms
	else
		BRANCH=insta$MINOR_NOPADDING
		DB=hms${MINOR/./}
	fi
	echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Auto branch: $BRANCH"
fi

#
# Automatically find out build number from testsvr2 if not given
#
if [[ ! $VERSION =~ '-' ]] ; then
	error_exit "Build Number is not provided."
else
	bld=${VERSION#*-}
fi

BUILD_TYPE="Major upgrade simulation"
if [ "$INCR" == "-i" ] ; then
	BUILD_TYPE="Incremental update"
fi

UILANGS="all supported languages"
[ ! -z "$INSTAUI_LOCALES" ] && UILANGS=$INSTAUI_LOCALES

slack_message=$(cat << EOF
*Insta HMS + API Build Tool*
JDK : $JAVA_HOME
Maven Repository : ${MVNREPO/-Dmaven\.repo\.local=/}
UI Builds for $UILANGS

*Build :* $VERSION | *Version :* $MINOR_NOPADDING | *Build Type   :* $BUILD_TYPE

Build Started

Preparing git branch for version and commit hash checkin and setting up environment
EOF
)
send_slack_notification "$slack_message"

GITURL=$REPODIR/$BRANCH

mkdir -p $BUILDDIR
[ $? -ne 0 ] && error_exit "Unable to create directory: $BUILDDIR"
cd $BUILDDIR

# Sanitize Build GIT Repo from uncommited codes
echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Checking out source from $GITURL"
cd insta
git reset --hard
git clean -f -d
git remote update
git checkout $BRANCH
git pull
cd ..	

cd insta
echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Updating submodules"
git submodule update
cd insta-ui
git reset --hard
git clean -f -d
git remote update
echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : submodule head at $(git rev-parse HEAD)"
echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Switching to $BRANCH on submodule and obtaining latest commit"
git checkout $BRANCH
git pull
echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : submodule head at $(git rev-parse HEAD)"
export UI_COMMIT_HASH=`git rev-parse HEAD`

echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Installing nodejs"
nodeversion=$(grep '"node":' package.json| awk -F: '{ print $2 }' | tr -d '" ,')
if [ ! -f /usr/bin/node ]; then
	wget -qO- https://raw.githubusercontent.com/creationix/nvm/v0.33.1/install.sh | bash
	export NVM_DIR="$HOME/.nvm"
	[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh" # This loads nvm
fi
nvm install $nodeversion
nvm use $nodeversion

echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Install yarn & gulp"
gulpversion=$(grep '"gulp":' package.json| awk -F: '{ print $2 }' | tr -d '" ,')
webpackversion=$(grep '"webpack":' package.json| awk -F: '{ print $2 }' | tr -d '" ,')
yarnversion=$(grep '"yarn":' package.json| awk -F: '{ print $2 }' | tr -d '" ,')

yarninstalled=$(npm list --global yarn | grep yarn@$yarnversion)
webpackinstalled=$(npm list --global webpack | grep webpack@$webpackversion)
gulpinstalled=$(npm list --global gulp | grep gulp@$gulpversion)
if [ -z "$yarninstalled" ] ; then
	npm install --global yarn@$yarnversion
fi
if [ -z "$gulpinstalled" ] ; then
	npm install --global gulp@$gulpversion
fi
if [ -z "$webpackinstalled" ] ; then
	npm install --global webpack@$webpackversion
fi

echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Properties file path"
export PROPERTIES_PATH=../instahms/WEB-INF/src/java/resources
echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Running prod script for insta ui"
		
cd ..
git add insta-ui
cd instahms

# Update build branch once again to avoid merge conflicts as there is delay between clone
# and commit of build changes due to insta-ui submodule fetch
git pull
export HMS_COMMIT_HASH=`git rev-parse HEAD`

# bump up the version number and checkin
sed --in-place -e "s/^insta.software.version.*$/insta.software.version = $VERSION/" \
	src/main/resources/java/resources/application.properties

sed --in-place -e "s/^insta.ui.commit.hash.*$/insta.ui.commit.hash = $UI_COMMIT_HASH/" \
	src/main/resources/java/resources/application.properties

sed --in-place -e "s/^insta.hms.commit.hash.*$/insta.hms.commit.hash = $HMS_COMMIT_HASH/" \
	src/main/resources/java/resources/application.properties

#checking version number in db only if version number is less than 11.12
echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Checking in application.properties"

#Create build commit if uncommited files exists
if [[ -n $(git status -uno --porcelain) ]]; then
	# Delete build branch to avoid issues when retrying build with same build number
	git push origin -d build_$VERSION
	git branch -D build_$VERSION

	# Create new build branch and commit changes
	git checkout -B build_$VERSION
	git add . 
	git commit -m "For build $VERSION"
	git push origin -u build_$VERSION
	$CUR_DIR/merge-to-protected-branch.py $VERSION $BRANCH
	[ $? -ne 0 ] && error_exit "Build cancelled due to PR Automation failure"
fi

#
# build the application: clean distribution
#
MAVEN_PROFILE="-P wsdl-build"
if [ "$SKIPWS" == "-s" ] ; then
	MAVEN_PROFILE=""
fi

rm -rf $BUILD_CACHE_AREA/*

send_slack_notification "Building Insta HMS API..."
rm -rf $BUILDDIR/insta/instahms/target
mvn -f pom.apps.xml clean package -DskipTests=true -Dcheckstyle.skip=true $MVNREPO
[ $? -ne 0 ] && error_exit "Build exited due to maven build failure for API"
mv $BUILDDIR/insta/instahms/target/instaapps $BUILD_CACHE_AREA/instaapps

send_slack_notification "Insta HMS API build completed."
send_slack_notification "Building Insta HMS..."
rm -rf $BUILDDIR/insta/instahms/target
mvn clean package -DskipTests=true -Dcheckstyle.skip=true $MVNREPO $MAVEN_PROFILE
[ $? -ne 0 ] && error_exit "Build exited due to maven build failure for HMS"
mv $BUILDDIR/insta/instahms/target/instahms $BUILD_CACHE_AREA/instahms

send_slack_notification "Insta HMS build completed."
send_slack_notification "Starting deployment to test servers..."

#
# Copy the distribution to testsvrs
#
deploy_servers=
for s in ${servers[@]} ; do
	echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Copying new build to ${s}"
	get_host_hms_options $s $DBNAME
	HOST_DIST_DIR=`get_host_dist_home $s $DBNAME`
	HOST_USER=`get_host_user $HOST_DIST_DIR`
	rsync -za $BUILD_CACHE_AREA/instaapps/ ${HOST_USER}@${s}:${HOST_WORK_DIR}Appsbuilds/$VERSION && rsync -za $BUILD_CACHE_AREA/instahms/ ${HOST_USER}@${s}:${HOST_WORK_DIR}builds/$VERSION
	if [ $? -ne 0 ] ; then
		slack_message="Copying build to ${s} failed. Skipping deployment on this server"
		send_slack_notification "$slack_message"
		echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Copying build to ${s} failed. Skipping deployment on this server"
	else
		if [ -z "$deploy_servers" ] ; then
			deploy_servers="${s}"
		else 
			deploy_servers="$deploy_servers ${s}"
		fi
	fi
done

#
# Automatically deploy a build as per the deploy config on the servers
#
echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Build done and copied to testsvrs."

# deploy on testsvrs
for s in ${deploy_servers[@]} ; do
	echo
	echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : --- Deploying hms on ${s} ---"
	HOST_DIST_DIR=`get_host_dist_home $s $DBNAME`
	HOST_USER=`get_host_user $HOST_DIST_DIR`
	HOST_BIN_DIR=`get_host_bin_dir $s $DBNAME`
	rsync -za $DEPLOY_SCRIPT_DIR/deploy ${HOST_USER}@${s}:${HOST_BIN_DIR}/deploy
	if [ ! -z "$BG" ] ; then
		ssh ${HOST_USER}@${s} ${HOST_BIN_DIR}/deploy $NQ $INCR $VERSION &
	else
		ssh ${HOST_USER}@${s} ${HOST_BIN_DIR}/deploy $NQ $INCR $VERSION
	fi
done

if [ ! -z "$BG" ] ; then
	echo
	echo "$HOST $(date +"%a %m-%d-%Y %H:%M:%S") : Waiting for testsvr deploys to complete"
	echo
	wait
fi

slack_body=`{
if [ "$INCR" == "-i" ] ; then
	echo "All schemas have been incrementally migrated."
else
	echo "Following is the schema status:"
	echo
	for s in ${deploy_servers[@]} ; do
		echo "${s}:"
		ssh root@${s} cat /tmp/deploy_status
	done
fi
}`

slack_message=$(cat << EOF
*Deployment Completed.*
$slack_body
EOF
)
send_slack_notification "$slack_message"

for s in ${deploy_servers[@]} ; do
	SERVER_NAME="${s}:"
	migrate_error=`ssh root@${s} grep "ERROR" /var/log/insta/${DB}/migrate.log`
	if [ -z "$migrate_error" ] ; then
		migrate_error="No errors in migrate.log"
	fi

	slack_message=$(cat << EOF
*$SERVER_NAME*
$migrate_error
EOF
)
	liquibase_error=`ssh root@${s} grep "ERROR:" /var/log/insta/${DB}/liquibase.log`
	if [ -z "$liquibase_error" ] ; then
		liquibase_error="No errors in liquibase.log"
	fi

	slack_message=$(cat << EOF
*$SERVER_NAME*
$liquibase_error
EOF
)
	send_slack_notification "$slack_message"
done

