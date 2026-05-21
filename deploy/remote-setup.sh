#!/bin/bash
set -euo pipefail

FHIR_ROOT=/opt/fhir

# PostgreSQL user and database
if ! sudo -u postgres psql -tAc "SELECT 1 FROM pg_roles WHERE rolname='fhir_app'" | grep -q 1; then
  sudo -u postgres psql -c "CREATE USER fhir_app WITH PASSWORD 'fhir_deploy_2026';"
fi
if ! sudo -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='fhir_security'" | grep -q 1; then
  sudo -u postgres psql -c "CREATE DATABASE fhir_security OWNER fhir_app;"
fi
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE fhir_security TO fhir_app;"

cat > "${FHIR_ROOT}/application.properties" <<'PROP'
spring.application.name=FHIR Security Testing Platform
fhir.server.url=http://hapi.fhir.org/baseR4
spring.datasource.url=jdbc:postgresql://localhost:5432/fhir_security
spring.datasource.username=fhir_app
spring.datasource.password=fhir_deploy_2026
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
server.port=8080
fhir.security.test.bearer-token=
fhir.security.test.lab-base-url-contains=
fhir.security.test.out-of-scope-patient-id=
PROP

cat > /etc/systemd/system/fhir-backend.service <<'UNIT'
[Unit]
Description=FHIR Security Testing Platform Backend
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/fhir
ExecStart=/usr/bin/java -jar /opt/fhir/app.jar --spring.config.additional-location=file:/opt/fhir/application.properties
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
UNIT

systemctl daemon-reload
systemctl enable fhir-backend
systemctl restart fhir-backend

# Frontend build
mkdir -p "${FHIR_ROOT}/frontend-src"
tar -xzf "${FHIR_ROOT}/frontend.tgz" -C "${FHIR_ROOT}/frontend-src"
cd "${FHIR_ROOT}/frontend-src"
npm ci
npm run build

# Angular application builder outputs static files under dist/frontend/browser
FRONTEND_DIST="${FHIR_ROOT}/frontend-dist"
rm -rf "${FRONTEND_DIST}"
if [ -d dist/frontend/browser ]; then
  cp -a dist/frontend/browser "${FRONTEND_DIST}"
elif [ -f dist/frontend/index.html ]; then
  cp -a dist/frontend "${FRONTEND_DIST}"
else
  echo "Frontend build output not found (expected dist/frontend/browser)" >&2
  find dist -maxdepth 3 -type f -name index.html 2>/dev/null || true
  exit 1
fi

cat > /etc/nginx/sites-available/fhir <<'NGINX'
server {
    listen 8888;
    listen [::]:8888;
    server_name 157.180.112.127;

    root /opt/fhir/frontend-dist;
    index index.html;

    access_log /var/log/nginx/fhir-access.log;
    error_log /var/log/nginx/fhir-error.log;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 120s;
        proxy_send_timeout 120s;
        proxy_read_timeout 120s;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        expires 7d;
        add_header Cache-Control "public";
    }
}
NGINX

ln -sf /etc/nginx/sites-available/fhir /etc/nginx/sites-enabled/fhir
nginx -t
systemctl reload nginx

echo "Setup complete."
