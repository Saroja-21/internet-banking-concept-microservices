# Internet Banking Helm Charts

## v1 - Working Version
Fully functional chart with proper configurations.

## v2 - Faulty Version (For Rollback Testing)
Intentional issues:
- Non-existent image tags (`broken-v2-nonexistent`) → ImagePullBackOff
- Invalid health endpoints (`/non-existent-health`) → Readiness failures
- Wrong container ports → Connection refused
- Low memory limits (32-64Mi) → OOMKilled
- Aggressive health check timing → Fast failure

## Usage
```bash
# Deploy v1 (working)
helm install internet-banking ./helm-charts/v1 -n internet-banking --create-namespace

# Upgrade to v2 (faulty) - triggers rollback
helm upgrade internet-banking ./helm-charts/v2 -n internet-banking
```
