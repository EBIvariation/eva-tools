# Deployment scripts

## Deployment scripts for python projects

The following scripts are templates for deploying python project directly from github.
The can deploy from a branch or a tag and create independent directory for each install.

### Configuration 

Both script needs to be configured with information about the project they will deploy 
and the environment in which the deployment will occur.

Example:
```bash
repo=https://github.com/EBIvariation/eva-tools.git
environment=production
```

### `deploy_python_project_as_dir.sh` 

This script will deploy a frozen copy of the project in a directory. 
It requires an additional configuration to specify the path to a python virtual environment:

```bash
pip=/path/to/pip
```

The script expect to find a `requirements.txt` file in the top directory of the project. 
It will deploy in the same directory as the deployment script.

Once configured just run:
```bash 
deploy.sh v0.4 
```


### deploy_python_project_in_venv.sh

This script will deploy a frozen copy of the project in an independent virtual environment. 
It requires an additional configuration to specify the path to a source python.

```bash
python_source=/path/to/python/bin/python3
```

The script expect the project to contain a properly configured `setup.py` in the top directory of the project. 
It will deploy in the same directory as the deployment script.

Once configured just run:
```bash 
deploy.sh v0.4 
```