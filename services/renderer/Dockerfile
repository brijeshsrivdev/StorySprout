FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
COPY services/renderer/package*.json services/renderer/
COPY packages/editor-model/package.json packages/editor-model/
COPY packages/editor-model/src packages/editor-model/src
COPY services/renderer/tsconfig.json services/renderer/
COPY services/renderer/src services/renderer/src
RUN npm install
RUN npm --workspace services/renderer run build

FROM node:24-alpine
RUN apk add --no-cache ffmpeg ttf-dejavu
WORKDIR /app
COPY --from=build /app/node_modules ./node_modules
COPY --from=build /app/services/renderer/dist ./services/renderer/dist
COPY --from=build /app/services/renderer/package.json ./services/renderer/package.json
COPY --from=build /app/packages/editor-model/src ./packages/editor-model/src
COPY --from=build /app/packages/editor-model/package.json ./packages/editor-model/package.json
EXPOSE 4000
CMD ["node", "--import", "tsx", "services/renderer/dist/server.js"]
