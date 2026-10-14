# spring-ai-app
### 1.Install Ollama
brew install --cask ollama-app  
ollama --version  
ollama pull gemma2:2b  
ollama run gemma2:2b

### 2.  Install ChromaDB
docker run -d -p 8000:8000 --name chroma chromadb/chroma

docker start chroma

curl [http://localhost:8000/api/v2/heartbeat](http://localhost:8000/api/v2/heartbeat "http://localhost:8000/api/v2/heartbeat")



curl -X POST [http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections](http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections "http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections") -H 'Content-Type: application/json' -d '{"name":"SpringAiCollection","get_or_create":true}'`