#!/bin/sh
set -e
/opt/datagrid/bin/readinessProbe.sh
curl -sk -H "Authorization: Bearer `cat /run/secrets/kubernetes.io/serviceaccount/token`" "https://${KUBERNETES_SERVICE_HOST}:${KUBERNETES_SERVICE_PORT}/api/v1/namespaces/${OPENSHIFT_KUBE_PING_NAMESPACE}/pods/https:${HOSTNAME}:8778/proxy/jolokia/read/jboss.infinispan:type=Cache,name=%22${READINESS_CACHE}%22,manager=%22clustered%22,component=StateTransferManager/joinComplete" | grep -q \"value\":true
exit 0
