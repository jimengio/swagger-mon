
Swagger Mon
----

> generate mock data from swagger schema


Site http://fe.jimu.io/swagger-mon/

### Usages

[![Clojars Project](https://img.shields.io/clojars/v/jimengio/swagger-mon.svg)](https://clojars.org/jimengio/swagger-mon)

```edn
[jimengio/swagger-mon "0.0.1-a2"]
```

```clojure
(swagger-mon.core/gen-data schema-obj) ; returns json object
```

For swagger definition:

```json
{"$schema":"http://json-schema.org/draft-04/schema#","type":"object","properties":{"id":{"type":"string"},"categoryId":{"type":"string"},"name":{"type":"string"},"varname":{"type":"string"},"type":{"type":"string"},"unit":{"type":"string"},"readonly":{"type":"boolean"},"reportType":{"type":"string"},"intervalMsec":{"type":"number"},"description":{"type":"string"},"createdAt":{"type":"string"},"updatedAt":{"type":"string"}}}
```

Generate data with random values:

```json
{
  "readonly": false,
  "updatedAt": "2020-03-04T04:25:36.505Z",
  "reportType": "深圳",
  "createdAt": "2020-03-04T04:25:36.505Z",
  "id": "PN8R1ToO",
  "name": "菏泽",
  "type": "Quanzhou 黄山 嘉兴",
  "varname": "Yinchuan",
  "unit": "Hainan 舟山 保山",
  "intervalMsec": 69,
  "description": "Yichun 张家界 石家庄 Neijiang",
  "categoryId": "ks6MVtWng"
}
```

Mock strings are based on https://github.com/brightgems/china_city_dataset .

### CLI

```bash
yarn global add @jimengio/swagger-mon
```

Then run command `swagger-api.json`:

```bash
# prepare a swagger-api.json first!

PORT=7801 swagger-mon # read...
```

### Workflow

Workflow https://github.com/mvc-works/calcit-workflow

### License

MIT
