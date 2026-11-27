# Probe for the SCRUM-237 hosting test. Not the production grader image.
FROM node:22-bookworm-slim AS pyodide
WORKDIR /build
RUN npm pack pyodide@314.0.8 --silent \
 && tar -xzf pyodide-314.0.8.tgz \
 && mv package /pyodide

FROM denoland/deno:2.9.6
COPY --from=pyodide /pyodide /opt/pyodide
COPY child.js snap_make.js server.js /opt/grader/
# Build step: start Pyodide once, without user code, and save the memory snapshot.
RUN mkdir -p /opt/grader/empty \
 && deno run --no-prompt --allow-read=/opt/pyodide --allow-write=/opt/grader/pyodide.snapshot \
      /opt/grader/snap_make.js /opt/pyodide /opt/grader/pyodide.snapshot \
 && chmod -R a+rX /opt/pyodide /opt/grader
USER deno
WORKDIR /opt/grader
ENV PORT=10000
EXPOSE 10000
# --allow-all: Deno 2.9 reads /proc only with it (memory measurement). The child keeps the strict flags.
CMD ["deno", "run", "--no-prompt", "--allow-all", "/opt/grader/server.js"]
