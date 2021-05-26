#!/bin/bash
# deploy_python_in_venv.sh: deployment script for setting up a project inside a virtual environment making the
# scripts executable

### config
# pip: relative or absolute path to pip
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

git_element=$1

if [ "$git_element" == "master" ]
then
    deployment_suffix=""
else
    deployment_suffix="@$git_element"
fi

echo "Deploying $git_element from $repo"


if [ -d "./$git_element" ]
then
  chmod -R a+w "./$git_element"
  rm -rf "./$git_element"
fi

echo "Create the virtual environment"
$python_source -m venv "$git_element"

echo "Upgrade pip"
$git_element/bin/pip install -q --upgrade pip

echo "Install $git_element"
$git_element/bin/pip install -q "git+$repo$deployment_suffix"

chmod -R a-w "$git_element"

echo "Setting the link"
ln -fnvs "$git_element" "$environment"

cd "$PWD"
echo "Done"
