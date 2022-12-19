cat docs/openapi/api-internal-v1.yaml \
    | grep -v "# NO EXTERNAL" \
    | sed -e '/# ONLY EXTERNAL/s/^#//' \
    > docs/openapi/api-external-v1.yaml