# spring-ai-app

docker run -d -p 8000:8000 --name chroma chromadb/chroma

curl [http://localhost:8000/api/v2/heartbeat](http://localhost:8000/api/v2/heartbeat "http://localhost:8000/api/v2/heartbeat")

docker start chroma

`​curl -X POST [http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections](http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections "http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections") -H 'Content-Type: application/json' -d '{"name":"SpringAiCollection","get_or_create":true}'`