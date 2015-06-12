if [ $# -eq 2 ]
then
    ELASTICSEARCH_SERVER=$1
    INDEX=$2
else
    ELASTICSEARCH_SERVER='http://localhost:9200'
    INDEX='ache_data'
fi

curl -XPUT ${ELASTICSEARCH_SERVER}/${INDEX} -d '{
  "settings": {
    "index": {
      "number_of_shards": 1,
      "number_of_replicas": 1
    }
  },
  "mappings": {
    "page": {
      "properties": {
        "domain": {
          "type": "string",
          "index": "not_analyzed"
        },
        "words": {
          "type": "string",
          "index": "not_analyzed"
        },
        "wordsMeta": {
          "type": "string",
          "index": "not_analyzed"
        },
        "retrieved": {
          "format": "dateOptionalTime",
          "type": "date"
        },
        "text": {
          "type": "string"
        },
        "title": {
          "type": "string"
        },
        "url": {
          "type": "string",
          "index": "not_analyzed"
        },
        "topPrivateDomain": {
          "type": "string",
          "index": "not_analyzed"
        }
      }
    }
  }
}'