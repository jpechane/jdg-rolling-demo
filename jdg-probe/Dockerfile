FROM brew-pulp-docker01.web.prod.ext.phx2.redhat.com:8888/jboss-datagrid-6/datagrid65-openshift:1.2
COPY scripts/improvedReadinessProbe.sh /opt/datagrid/bin/improvedReadinessProbe.sh
USER 0
RUN chmod 777 /opt/datagrid/bin/improvedReadinessProbe.sh
RUN chown jboss:jboss /opt/datagrid/bin/improvedReadinessProbe.sh
USER jboss
