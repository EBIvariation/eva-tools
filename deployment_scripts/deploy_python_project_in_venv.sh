#!/bin/bash
# deploy_python_project_in_venv.sh: deployment script for setting up a project inside a virtual environment making the
# scripts executable

### config
# python_source: relative or absolute path to base install python executable
python_source=

# repo: remote url of the git repository
repo=

# developement or production: only used to create a link at the end
environment=

# any preliminary commands, exports, etc
if [ ! $python_source ] || [ ! $repo ]
then
    echo "Invalid config: ensure that python_source and repo are populated"
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

if [ "$git_branch_or_tag" == "master" ]
then
    deployment_suffix=""
else
    deployment_suffix="@$git_branch_or_tag"
fi

echo "Deploying $git_branch_or_tag from $repo"


if [ -d "./$git_branch_or_tag" ]
then
  chmod -R a+w "./$git_branch_or_tag"
  rm -rf "./$git_branch_or_tag"
fi

echo "Create the virtual environment"
$python_source -m venv "$git_branch_or_tag"

echo "Upgrade pip"
$git_branch_or_tag/bin/pip install -q --upgrade pip

echo "Install $git_branch_or_tag"
$git_branch_or_tag/bin/pip install -q "git+$repo$deployment_suffix"

chmod -R a-w "$git_branch_or_tag"

echo "Setting the link"
ln -fnvs "$git_branch_or_tag" "$environment"

cd "$PWD"
echo "Done"
