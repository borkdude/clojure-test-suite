// Runs the squint-compatible subset of the suite, listed in passing.txt.
// Compile first with: npx squint compile (driven by squint.edn).
// Exits non-zero on any failure or error.
import * as t from 'squint-cljs/src/squint/test.js';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const target = join(here, '..', 'target', 'squint');

const namespaces = readFileSync(join(here, 'passing.txt'), 'utf8')
  .split('\n')
  .map((line) => line.trim())
  .filter((line) => line && !line.startsWith('#'));

for (const ns of namespaces) {
  const rel = ns.replaceAll('.', '/').replaceAll('-', '_') + '.mjs';
  await import(pathToFileURL(join(target, rel)).href);
}

const summary = await t.run_tests();
if ((summary.fail || 0) + (summary.error || 0) > 0) {
  process.exit(1);
}
