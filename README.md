Red Hat Data Grid Clustered Lock Demo

This project demonstrated the use of Data Grid's Clustered Lock feature to synchronize across multiple pods on OpenShift.

Notes:
For the jgroups cluster to start up set the following permissions

oc policy add-role-to-user view system:serviceaccount:$(oc project -q):default -n $(oc project -q)


Create a shared NFS PV

If dynamic provisioning is enabled create nfs storage class

create nfs pv

claim nfs pv

Create Ping Services

oc create -f pingService.yaml

Deploy app using maven fabric8

mvn fabric8:deploy

