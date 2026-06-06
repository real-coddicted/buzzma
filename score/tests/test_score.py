from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_score_exact_match_returns_perfect_score():
    response = client.post("/api/v1/score", json=[
        {
            "key": "item-1",
            "payload": [
                {"label": "name", "expected": "apple", "actual": "apple", "weight": 1.0}
            ],
        }
    ])
    assert response.status_code == 200
    results = response.json()
    assert len(results) == 1
    assert results[0]["key"] == "item-1"
    assert results[0]["overall_score"] == 1.0
    assert results[0]["scores"][0]["label"] == "name"
    assert results[0]["scores"][0]["score"] == 1.0


def test_score_overall_reflects_weights():
    response = client.post("/api/v1/score", json=[
        {
            "key": "item-1",
            "payload": [
                {"label": "exact", "expected": "apple", "actual": "apple", "weight": 1.0},
                {"label": "no_match", "expected": "apple", "actual": "zzzzz", "weight": 0.0},
            ],
        }
    ])
    assert response.status_code == 200
    results = response.json()
    assert results[0]["overall_score"] == 1.0


def test_score_multiple_requests():
    response = client.post("/api/v1/score", json=[
        {
            "key": "item-1",
            "payload": [{"label": "name", "expected": "apple", "actual": "apple", "weight": 1.0}],
        },
        {
            "key": "item-2",
            "payload": [{"label": "name", "expected": "banana", "actual": "orange", "weight": 1.0}],
        },
    ])
    assert response.status_code == 200
    results = response.json()
    assert len(results) == 2
    keys = {r["key"] for r in results}
    assert keys == {"item-1", "item-2"}


def test_score_empty_payload_returns_422():
    response = client.post("/api/v1/score", json=[
        {"key": "item-1", "payload": []}
    ])
    assert response.status_code == 422


def test_score_duplicate_keys_returns_422():
    response = client.post("/api/v1/score", json=[
        {"key": "item-1", "payload": [{"label": "name", "expected": "a", "actual": "a", "weight": 1.0}]},
        {"key": "item-1", "payload": [{"label": "name", "expected": "b", "actual": "b", "weight": 1.0}]},
    ])
    assert response.status_code == 422
    assert "item-1" in response.json()["detail"]


def test_score_invalid_algorithm_returns_422():
    response = client.post("/api/v1/score?algorithm=nonexistent", json=[
        {"key": "item-1", "payload": [{"label": "name", "expected": "a", "actual": "a", "weight": 1.0}]}
    ])
    assert response.status_code == 422
