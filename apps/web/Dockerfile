# syntax=docker/dockerfile:1
FROM node:24-alpine
WORKDIR /app
COPY package*.json ./
COPY apps/web/package*.json apps/web/
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "--workspace", "apps/web", "run", "dev"]
