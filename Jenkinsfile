env.ENABLE_SONARSCANNER=true
env.IS_CONTAINER_BUILD="false"
env.STEMCELL="false"
env.JDK_VERSION=17
env.MVN_ARGS="-Dmaven.install.skip=true \
  -Dmaven.javadoc.skip=true \
  -Dprod \
  --strict-checksums \
  --batch-mode \
  --update-snapshots \
  --no-transfer-progress \
  -P!style-enforcer,prod \
  --fail-at-end"
mvnPipeline()
