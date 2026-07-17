import pandas as pd

from app.calculator.scoring_algorithm import ScoringAlgorithm


def compute_overall_score_by_key(df: pd.DataFrame) -> pd.DataFrame:
    def weighted_avg(g: pd.DataFrame) -> float:
        total_weight = g["weight"].sum()
        if not total_weight:
            return 0.0
        return round((g["score"] * g["weight"]).sum() / total_weight, 2)

    return df.groupby("key").apply(weighted_avg).reset_index(name="overall_score")


def compute_penalized_overall_score_by_key(df: pd.DataFrame) -> pd.DataFrame:
    """Weighted average where each score is raised to the power of its weight.

    At weight=1 the result equals the raw score. For weight>1 the deficit from
    1.0 is amplified (e.g. score=0.86, weight=2 → 0.86²≈0.74), making higher
    weights increasingly demanding of near-perfect scores.
    """
    def penalized_weighted_avg(g: pd.DataFrame) -> float:
        total_weight = g["weight"].sum()
        if not total_weight:
            return 0.0
        penalized = g["score"] ** g["weight"]
        return round((penalized * g["weight"]).sum() / total_weight, 2)

    return df.groupby("key").apply(penalized_weighted_avg).reset_index(name="overall_score")


def compute_min_valued_overall_score_by_key(df: pd.DataFrame) -> pd.DataFrame:
    def min_valued(g: pd.DataFrame) -> float:
        return round(g["score"].min(), 2)

    return df.groupby("key").apply(min_valued).reset_index(name="overall_score")


_ALGORITHM_MAP = {
    ScoringAlgorithm.WEIGHTED_AVERAGE: compute_overall_score_by_key,
    ScoringAlgorithm.PENALIZED_WEIGHTED_AVERAGE: compute_penalized_overall_score_by_key,
    ScoringAlgorithm.MIN_VALUE: compute_min_valued_overall_score_by_key,
}


def compute_overall_score(df: pd.DataFrame, algorithm: ScoringAlgorithm = ScoringAlgorithm.WEIGHTED_AVERAGE) -> pd.DataFrame:
    return _ALGORITHM_MAP[algorithm](df)
