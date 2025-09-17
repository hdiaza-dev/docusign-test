# ===== Build stage =====
FROM node:20-bullseye AS build
WORKDIR /app

COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# ===== Run stage =====
FROM nginx:1.27-alpine AS run
# Copia artefacto Vite
COPY --from=build /app/dist /usr/share/nginx/html

# Nginx config para SPA (fallback a index.html)
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Render inyecta PORT; nginx usa 8080 aquí para respetarlo
ENV PORT=8080
EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
