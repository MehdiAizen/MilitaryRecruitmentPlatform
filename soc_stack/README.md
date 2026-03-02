# Stack SOC — Wazuh + Suricata + ELK
# Guide de démarrage rapide

## Structure des fichiers

```
soc_stack/
├── docker-compose.soc.yml        ← Fichier principal (tout est là)
│
├── nginx-dmz/
│   └── nginx.conf                ← Nginx Zone DMZ (WAF basique + proxy)
│                                    ≠ Nginx de ton frontend React
│
├── suricata/
│   └── rules/
│       └── military-platform.rules  ← Règles IDS personnalisées pour ton app
│
├── wazuh/
│   └── config/
│       └── rules/
│           └── military-platform-rules.xml  ← Règles SIEM pour Spring Boot
│
├── filebeat/
│   └── filebeat.yml              ← Collecte automatique de tous les logs Docker
│
├── logstash/
│   └── pipeline/
│       └── military-platform.conf  ← Traitement et enrichissement des logs
│
└── prometheus/
    └── prometheus.yml            ← Config scraping métriques
```

## Ce que tu n'as PAS à modifier dans ton projet Spring Boot

- ❌ Aucun code Java à changer
- ❌ Aucune dépendance Maven à ajouter (tu as déjà actuator + prometheus)
- ❌ Aucun fichier React à modifier

## Lancement

### Prérequis système (une seule fois)
```bash
# Elasticsearch a besoin de cette config système
sudo sysctl -w vm.max_map_count=262144
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
```

### Démarrer uniquement la SOC
```bash
cd soc_stack/
docker compose -f docker-compose.soc.yml up -d
```

### Démarrer TOUT (ton app + la SOC)
```bash
# Depuis la racine de ton projet
docker compose -f docker-compose.yml -f soc_stack/docker-compose.soc.yml up -d
```

### Vérifier que tout tourne
```bash
docker compose -f docker-compose.soc.yml ps
```

## Interfaces disponibles après démarrage

| Interface | URL | Login |
|-----------|-----|-------|
| **Kibana** (logs ELK) | http://localhost:5601 | pas de login (xpack désactivé) |
| **Wazuh Dashboard** (SIEM) | http://localhost:5602 | admin / admin |
| **Grafana** (métriques) | http://localhost:3001 | admin / admin123 |
| **Prometheus** (raw) | http://localhost:9090 | pas de login |

## Comment ça supervise tes containers

```
Ton container Spring Boot (backend)
    │  produit des logs JSON dans stdout
    ↓
Filebeat (lit /var/lib/docker/containers/*.log)
    ↓
Logstash (parse, enrichit, détecte les événements sécurité)
    ↓
Elasticsearch (stocke dans l'index military-military-recruitment-backend-2026.02.xx)
    ↓
Kibana → tu crées des dashboards, tu cherches les logs

En parallèle :
Suricata surveille le réseau → alertes → Filebeat → ELK
Wazuh analyse les logs → alertes → Wazuh Dashboard
Prometheus collecte métriques → Grafana affiche les graphes
```

## Scénarios de test pour la soutenance

### Tester Suricata (brute force)
```bash
# Simule 10 tentatives de login en boucle
for i in {1..10}; do
  curl -X POST http://localhost:80/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"wrong"}' &
done
# → Alerte Suricata dans Kibana (index military-suricata-ids-*)
```

### Tester Wazuh (auth failure)
```bash
# Idem — Wazuh va corréler les échecs et lever une alerte niveau 12
# Visible dans le Wazuh Dashboard → Security Events
```

### Tester le WAF Nginx DMZ
```bash
# Tentative injection SQL → doit retourner 403
curl "http://localhost/api/candidatures?search=1'%20OR%20'1'='1"

# Tentative XSS → doit retourner 403
curl "http://localhost/?q=<script>alert(1)</script>"
```
