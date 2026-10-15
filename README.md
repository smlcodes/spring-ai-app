# spring-ai-app
### 1.Install Ollama
```
brew install --cask ollama-app  
ollama --version  
ollama pull gemma2:2b  
ollama run gemma2:2b
```

### 2.  Install ChromaDB

```
git clone https://github.com/BlackyDrum/chromadb-ui.git

cd chromadb-ui

cat > config.yaml <<'EOF'
persist_path: "/data"
cors_allow_origins:
- "http://localhost:8090"
- "https://blackydrum.github.io"
  EOF

docker compose up -d --build

docker compose ps

curl -i http://localhost:8080/api/v2/heartbeat

open http://localhost:8090


curl -X POST localhost:8080/api/v2/tenants/default_tenant/databases/default_database/collections -H 'Content-Type: application/json' -d '{"name":"SpringAiCollection","get_or_create":true}'
```
