#Red Hat Data Grid Clustered Lock Demo

This project demonstrated the use of Data Grid's Clustered Lock feature to synchronize across multiple pods on OpenShift.

##Prerequisites

Setup OpenShift project

* Create a new project

    oc new-project clusterlockdemo

* Add view permission to the default service account

    oc policy add-role-to-user view system:serviceaccount:$(oc project -q):default -n $(oc project -q)

* Create a shared NFS PV
 Note: The example steps uses the AWS EFS share I created for my environment. 

** Create nfs storage class (as cluster-admin)

    oc create -f nfsStorageClass.yaml

** Create nfs pv (as cluster-admin) Note: the example file will need to be edited to point to the correct nfs server.

    oc create -f efspv.yaml

** Claim nfs pv

    oc create -f efspvc.yaml

* Create Ping Services

    oc create -f pingService.yaml

##Deploy app using maven fabric8

    mvn fabric8:deploy

## Test app using curl

* Read File

    curl -X GET -i -H 'Content-Type: application/text' 'http://<route>/filehandler/<myfilename>'

* Update File

    curl -X POST -i -H 'Content-Type: application/text' 'http://<route>/filehandler/<myfilename>' --data 'some data'

* Delete File

    curl -X DELETE -i -H 'Content-Type: application/text' 'http://<route>/filehandler/<myfilename>'
    
## Deployment Config Environment Variable options

* CLUSTER_LOCK
Set this to true or false. True will enable to use of the cluster lock before writing to the file. False will perform unsafe writes to the file.

* BASE_PATH
Set this to the path of the nfs volume mount

* DEMO_WRITE_DELAY
Sets the delay (in ms) between writing the start message block, the data, and the end message block. Note: a value of 1000 will lead to a three second response time.

### Todo

* add more logging and documentation to the code
* create unit/functional tests.


