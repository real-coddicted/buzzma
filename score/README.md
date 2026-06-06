# buzzma-score

Scoring service for fuzzy text similarity matching.

## Setup

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Run

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8082 --reload
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
pytest tests/
```
