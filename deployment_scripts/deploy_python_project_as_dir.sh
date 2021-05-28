#!/bin/bash
# deploy_python_project_as_dir.sh: deployment script for setting up a project in a frozen directory

### config
# pip: relative or absolute path to pip
pip=

# repo: remote url of the git repository
repo=

# developement or production: only used to create a link at the end
environment=

# any preliminary commands, exports, etc

if [ ! $pip ] || [ ! $repo ]
then
    echo "Invalid config: ensure that pip and repo are populated"
    exit 1
fi
### end config


if [ $# == 0 ]
then
    echo "Usage: $0 tag/branch/master"
    exit
fi

scriptpath=$(dirname $(readlink -f $0))
PWD=$(pwd)

cd "$scriptpath"
git_branch_or_tag=$1

if [ ! -d git_repo ]
then
    echo "Initialising local Git repo"
    git init --bare --shared git_repo
fi

echo "Deploying $git_branch_or_tag from $repo"

git --git-dir=git_repo fetch $repo "$git_branch_or_tag:$git_branch_or_tag"

if [ -d "./$git_branch_or_tag" ]
then
  chmod -R a+w "./$git_branch_or_tag"
  rm -rf "./$git_branch_or_tag"
fi

mkdir "$git_branch_or_tag"
git --git-dir=git_repo --work-tree="$git_branch_or_tag" checkout -f "$git_branch_or_tag"

chmod -R a-w "$git_branch_or_tag"

echo "Upgrade all packages"
$pip freeze | xargs $pip uninstall -y
$pip install -q --upgrade pip
$pip install -q -r "$git_branch_or_tag"/requirements.txt

echo "Setting the link"
ln -fnvs "$git_branch_or_tag" "$environment"

cd "$PWD"
echo "Done"
