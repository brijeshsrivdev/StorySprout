FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
COPY services/renderer/package*.json services/renderer/
RUN npm install
COPY . .
RUN npm --workspace services/renderer run build

FROM node:24-alpine
WORKDIR /app
COPY --from=build /app/services/renderer/dist ./dist
EXPOSE 4000
CMD ["node", "dist/server.js"]
