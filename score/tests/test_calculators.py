import pandas as pd
import pytest

from app.calculator.similarity_scoring import compute_text_similarity
from app.calculator.overall_scoring import (
    compute_overall_score_by_key,
    compute_penalized_overall_score_by_key,
    compute_overall_score,
    ScoringAlgorithm,
)


def make_df(rows: list[dict]) -> pd.DataFrame:
    return pd.DataFrame(rows)


# --- compute_text_similarity ---

def test_similarity_exact_match():
    df = make_df([{"expected": "apple", "actual": "apple"}])
    result = compute_text_similarity(df)
    assert result["score"].iloc[0] == 1.0


def test_similarity_case_insensitive():
    df = make_df([{"expected": "Apple", "actual": "APPLE"}])
    result = compute_text_similarity(df)
    assert result["score"].iloc[0] == 1.0


def test_similarity_empty_expected_returns_zero():
    df = make_df([{"expected": "", "actual": "apple"}])
    result = compute_text_similarity(df)
    assert result["score"].iloc[0] == 0.0


def test_similarity_empty_actual_returns_zero():
    df = make_df([{"expected": "apple", "actual": ""}])
    result = compute_text_similarity(df)
    assert result["score"].iloc[0] == 0.0


def test_similarity_no_match_returns_low_score():
    df = make_df([{"expected": "apple", "actual": "zzzzz"}])
    result = compute_text_similarity(df)
    assert result["score"].iloc[0] < 0.5


def test_similarity_does_not_mutate_input():
    df = make_df([{"expected": "apple", "actual": "apple"}])
    original_columns = set(df.columns)
    compute_text_similarity(df)
    assert set(df.columns) == original_columns


def test_similarity_multiple_rows():
    df = make_df([
        {"expected": "apple", "actual": "apple"},
        {"expected": "banana", "actual": "zzzzz"},
    ])
    result = compute_text_similarity(df)
    assert result["score"].iloc[0] == 1.0
    assert result["score"].iloc[1] < 0.5


# --- compute_overall_score_by_key ---

def test_overall_single_item():
    df = make_df([{"key": "k1", "score": 0.8, "weight": 1.0}])
    result = compute_overall_score_by_key(df)
    assert result.set_index("key")["overall_score"]["k1"] == 0.8


def test_overall_equal_weights():
    df = make_df([
        {"key": "k1", "score": 1.0, "weight": 1.0},
        {"key": "k1", "score": 0.0, "weight": 1.0},
    ])
    result = compute_overall_score_by_key(df)
    assert result.set_index("key")["overall_score"]["k1"] == 0.5


def test_overall_weighted_average():
    df = make_df([
        {"key": "k1", "score": 1.0, "weight": 3.0},
        {"key": "k1", "score": 0.0, "weight": 1.0},
    ])
    result = compute_overall_score_by_key(df)
    assert result.set_index("key")["overall_score"]["k1"] == 0.75


def test_overall_zero_weight_returns_zero():
    df = make_df([{"key": "k1", "score": 1.0, "weight": 0.0}])
    result = compute_overall_score_by_key(df)
    assert result.set_index("key")["overall_score"]["k1"] == 0.0


def test_overall_multiple_keys():
    df = make_df([
        {"key": "k1", "score": 1.0, "weight": 1.0},
        {"key": "k2", "score": 0.5, "weight": 1.0},
    ])
    result = compute_overall_score_by_key(df).set_index("key")["overall_score"]
    assert result["k1"] == 1.0
    assert result["k2"] == 0.5


# --- compute_penalized_overall_score_by_key ---

def test_penalized_weight_one_unchanged():
    df = make_df([{"key": "k1", "score": 0.86, "weight": 1.0}])
    result = compute_penalized_overall_score_by_key(df)
    assert result.set_index("key")["overall_score"]["k1"] == 0.86


def test_penalized_weight_two_reduces_score():
    df = make_df([{"key": "k1", "score": 0.86, "weight": 2.0}])
    result = compute_penalized_overall_score_by_key(df)
    assert result.set_index("key")["overall_score"]["k1"] < 0.86


def test_penalized_perfect_score_unchanged_at_any_weight():
    df = make_df([{"key": "k1", "score": 1.0, "weight": 5.0}])
    result = compute_penalized_overall_score_by_key(df)
    assert result.set_index("key")["overall_score"]["k1"] == 1.0


def test_penalized_zero_weight_returns_zero():
    df = make_df([{"key": "k1", "score": 0.86, "weight": 0.0}])
    result = compute_penalized_overall_score_by_key(df)
    assert result.set_index("key")["overall_score"]["k1"] == 0.0


def test_penalized_higher_weight_more_penalty():
    df1 = make_df([{"key": "k1", "score": 0.86, "weight": 2.0}])
    df2 = make_df([{"key": "k1", "score": 0.86, "weight": 3.0}])
    score_w2 = compute_penalized_overall_score_by_key(df1).set_index("key")["overall_score"]["k1"]
    score_w3 = compute_penalized_overall_score_by_key(df2).set_index("key")["overall_score"]["k1"]
    assert score_w3 < score_w2


def test_penalized_multiple_items_mixed_weights():
    df = make_df([
        {"key": "k1", "score": 1.0, "weight": 1.0},
        {"key": "k1", "score": 0.86, "weight": 2.0},
    ])
    result = compute_penalized_overall_score_by_key(df).set_index("key")["overall_score"]["k1"]
    # penalized: (1.0^1*1 + 0.86^2*2) / 3 = (1.0 + 1.4792) / 3 ≈ 0.83
    assert 0.8 < result < 0.9


# --- compute_overall_score dispatcher ---

def test_dispatcher_default_is_weighted_average():
    df = make_df([{"key": "k1", "score": 0.86, "weight": 2.0}])
    assert compute_overall_score(df).equals(compute_overall_score_by_key(df))


def test_dispatcher_weighted_average():
    df = make_df([{"key": "k1", "score": 0.86, "weight": 2.0}])
    assert compute_overall_score(df, ScoringAlgorithm.WEIGHTED_AVERAGE).equals(compute_overall_score_by_key(df))


def test_dispatcher_penalized_weighted_average():
    df = make_df([{"key": "k1", "score": 0.86, "weight": 2.0}])
    assert compute_overall_score(df, ScoringAlgorithm.PENALIZED_WEIGHTED_AVERAGE).equals(compute_penalized_overall_score_by_key(df))
