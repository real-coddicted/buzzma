# buzzma-score

Scoring service for fuzzy text similarity matching.

## Setup

Install [uv](https://docs.astral.sh/uv/) if you don't have it:

```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
```

Then install dependencies from the lockfile:

```bash
uv sync
```

## Run

```bash
uv run uvicorn app.main:app --host 0.0.0.0 --port 8082 --reload
```

API docs available at `http://localhost:8082/docs`.

## Examples

```bash
curl -X POST http://localhost:8082/api/v1/score \
    -H "Content-Type: application/json" \
    -d '[{"key":"test-jordan-200","payload":[{"label":"product_name","expected":"Nike Jordan 200 Men","actual":"Nike Men Jordan 200 Wide Size | US 10","weight":1.0}]}]'
```

```bash
curl -X POST http://localhost:8082/api/v1/score \
    -H "Content-Type: application/json" \
    -d '[{"key":"test-jordan-200","payload":[{"label":"product_name","expected":"Nike Jordan 200 Men","actual":"Nike Women Jordan 200 Wide Size | US 10","weight":1.0}]}]'
```

### Response

```json
[
  {
    "key": "test-jordan-200",
    "overall_score": 0.86,
    "scores": [
      {
        "label": "product_name",
        "score": 0.86
      }
    ]
  }
]
```

## Tests

```bash
uv run pytest tests/
```
