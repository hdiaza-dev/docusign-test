# ===== Build =====
FROM node:20-bullseye AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# ===== Run =====
FROM nginx:1.27-alpine AS run
WORKDIR /app

# Copiamos el estático
COPY --from=build /app/dist /usr/share/nginx/html

# Copiamos plantilla con ${PORT} (no el config final)
COPY default.conf.template /etc/nginx/templates/default.conf.template

# Render inyecta PORT en runtime
ENV PORT=8080
EXPOSE 8080

# Generamos el config real sustituyendo ${PORT} y lanzamos nginx
CMD sh -c "envsubst '\$PORT' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"
