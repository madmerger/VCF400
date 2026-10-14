"""Shared helpers for the Phase 5 cross-validation runners (PUB400 / Java / iPad)."""
import json
import os
import time

HERE = os.path.dirname(os.path.abspath(__file__))
CASES = os.path.join(HERE, "cases.json")
RESULTS_DIR = os.path.join(HERE, "results")


def load_cases(only=None):
    cases = json.load(open(CASES, encoding="utf-8"))["cases"]
    if only:
        wanted = set(only.split(","))
        cases = [c for c in cases if c["id"] in wanted]
    return cases


class Results:
    """Collects the observed behaviour per case and writes results/<env>.json."""

    def __init__(self, env):
        self.env = env
        self.data = {"env": env, "started": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()), "cases": {}}
        os.makedirs(RESULTS_DIR, exist_ok=True)

    def record(self, case, observed, error=None):
        entry = {"observed": observed}
        if error:
            entry["error"] = error
        self.data["cases"][case["id"]] = entry
        print(f"[{self.env}] {case['id']} {case['title']}\n    -> {json.dumps(observed, ensure_ascii=False)}"
              + (f"\n    !! {error}" if error else ""), flush=True)
        self.save()

    def save(self):
        self.data["finished"] = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
        with open(os.path.join(RESULTS_DIR, f"{self.env}.json"), "w", encoding="utf-8") as f:
            json.dump(self.data, f, indent=2, ensure_ascii=False)


def banner(text):
    line = "=" * 78
    print(f"\n{line}\n{text}\n{line}", flush=True)
